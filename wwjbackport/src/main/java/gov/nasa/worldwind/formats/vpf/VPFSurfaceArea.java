/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import java.nio.IntBuffer;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: VPFSurfaceArea.java 13893 2010-09-28 14:40:46Z dcollins $
 */
public class VPFSurfaceArea extends SurfacePolygon implements Disposable // TODO: consolidate with SurfacePolygons
{
    protected VPFFeature feature;
    protected VPFPrimitiveData primitiveData;
    protected VecBufferSequence buffer;
    protected int interiorDisplayList;
    protected LatLon referenceLocation;

    public VPFSurfaceArea(VPFFeature feature, VPFPrimitiveData primitiveData)
    {
        this.feature = feature;
        this.primitiveData = primitiveData;
        this.buffer = computeAreaFeatureCoords(feature, primitiveData);
        this.referenceLocation = feature.getBounds().toSector().getCentroid();
    }

    protected static VecBufferSequence computeAreaFeatureCoords(VPFFeature feature, VPFPrimitiveData primitiveData)
    {
        final int numEdges = traverseAreaEdges(feature, primitiveData, null);
        final IntBuffer edgeIds = IntBuffer.wrap(new int[numEdges]);

        traverseAreaEdges(feature, primitiveData, new EdgeListener()
        {
            public void nextEdge(int edgeId, VPFPrimitiveData.EdgeInfo edgeInfo)
            {
                edgeIds.put(edgeId);
            }
        });

        edgeIds.rewind();

        VecBufferSequence buffer = primitiveData.getPrimitiveCoords(VPFConstants.EDGE_PRIMITIVE_TABLE);
        return (VecBufferSequence) buffer.slice(edgeIds.array(), 0, numEdges);
    }

    protected interface EdgeListener
    {
        void nextEdge(int edgeId, VPFPrimitiveData.EdgeInfo edgeInfo);
    }

    protected static int traverseAreaEdges(VPFFeature feature, VPFPrimitiveData primitiveData, EdgeListener listener)
    {
        int count = 0;

        String primitiveName = feature.getFeatureClass().getPrimitiveTableName();

        for (int id : feature.getPrimitiveIds())
        {
            VPFPrimitiveData.FaceInfo faceInfo = (VPFPrimitiveData.FaceInfo) primitiveData.getPrimitiveInfo(
                primitiveName, id);

            VPFPrimitiveData.Ring outerRing = faceInfo.getOuterRing();
            count += traverseRingEdges(outerRing, primitiveData, listener);

            for (VPFPrimitiveData.Ring ring : faceInfo.getInnerRings())
            {
                count += traverseRingEdges(ring, primitiveData, listener);
            }
        }

        return count;
    }

    protected static int traverseRingEdges(VPFPrimitiveData.Ring ring, VPFPrimitiveData primitiveData,
        EdgeListener listener)
    {
        int count = 0;

        for (int edgeId : ring.edgeId)
        {
            VPFPrimitiveData.EdgeInfo edgeInfo = (VPFPrimitiveData.EdgeInfo)
                primitiveData.getPrimitiveInfo(VPFConstants.EDGE_PRIMITIVE_TABLE, edgeId);

            if (!edgeInfo.isOnTileBoundary())
            {
                if (listener != null)
                    listener.nextEdge(edgeId, edgeInfo);
                count++;
            }
        }

        return count;
    }

    protected List<Sector> computeSectors(Globe globe)
    {
        Sector s = this.feature.getBounds().toSector();
        if (s == null || s.equals(Sector.EMPTY_SECTOR))
            return null;

        return Arrays.asList(s);
    }

    public Iterable<? extends LatLon> getLocations()
    {
        return this.buffer.getLocations();
    }

    public void setLocations(Iterable<? extends LatLon> iterable)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Position getReferencePosition()
    {
        return new Position(this.referenceLocation, 0d);
    }

    public void dispose()
    {
        GLContext glContext = GLContext.getCurrent();
        if (glContext == null)
            return;

        if (this.interiorDisplayList > 0)
        {
            glContext.getGL().glDeleteLists(this.interiorDisplayList, 1);
            this.interiorDisplayList = 0;
        }
    }

    @Override
    protected void applyModelviewTransform(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Apply the geographic to surface tile coordinate transform.
        Matrix modelview = sdc.getModelviewMatrix();
        dc.getGL().glMultMatrixd(modelview.toArray(new double[16], 0, false), 0);
    }

    @Override
    protected ShapeAttributes createActiveAttributes()
    {
        return new VPFSymbolAttributes();
    }

    protected void determineActiveGeometry(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Intentionally left blank in order to override the superclass behavior with nothing.
    }

    protected void drawInterior(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Concave shape makes no assumptions about the nature or structure of the shape's vertices. The interior is
        // treated as a potentially complex polygon, and this code will do its best to rasterize that polygon. The
        // outline is treated as a simple line loop, regardless of whether the shape's vertices actually define a
        // closed path.

        // Apply interior attributes using a reference location of (0, 0), because VPFSurfaceArea's coordinates
        // are not offset with respect to a reference location.
        GL gl = dc.getGL();
        this.applyInteriorState(dc, sdc, this.getActiveAttributes(), this.getInteriorTexture(), LatLon.ZERO);

        if (this.interiorDisplayList <= 0)
        {
            this.interiorDisplayList = gl.glGenLists(1);
            gl.glNewList(this.interiorDisplayList, GL.GL_COMPILE);
            // Tessellate the interior vertices using a reference location of (0, 0), because VPFSurfaceArea's
            // coordinates do not neet to be offset with respect to a reference location.
            this.tessellateInterior(dc);
            gl.glEndList();
        }

        gl.glCallList(this.interiorDisplayList);
    }

    protected void drawOutline(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        this.applyOutlineState(dc, this.getActiveAttributes());

        // Edges features are not necessarily closed loops, therefore each edge must be rendered as separate line strip.
        this.buffer.bindAsVertexBuffer(dc);
        this.buffer.multiDrawArrays(dc, GL.GL_LINE_STRIP);
    }

    protected WWTexture getInteriorTexture()
    {
        if (this.getActiveAttributes().getInteriorImageSource() == null)
        {
            this.texture = null;
        }
        else if (this.texture == null
            || this.texture.getImageSource() != this.getActiveAttributes().getInteriorImageSource())
        {
            this.texture = new BasicWWTexture(this.getActiveAttributes().getInteriorImageSource(),
                ((VPFSymbolAttributes) this.getActiveAttributes()).isMipMapIconImage());
        }

        return this.texture;
    }

    //**************************************************************//
    //********************  Interior Tessellation  *****************//
    //**************************************************************//

    protected void tessellateInteriorVertices(GLU glu, GLUtessellator tess)
    {
        // Setup the winding order to correctly tessellate the outer and inner rings. The outer ring is specified
        // with a clockwise winding order, while inner rings are specified with a counter-clockwise order. Inner
        // rings are subtracted from the outer ring, producing an area with holes.
        glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NEGATIVE);
        glu.gluTessBeginPolygon(tess, null);

        String primitiveName = this.feature.getFeatureClass().getPrimitiveTableName();

        for (int id : this.feature.getPrimitiveIds())
        {
            VPFPrimitiveData.FaceInfo faceInfo = (VPFPrimitiveData.FaceInfo) primitiveData.getPrimitiveInfo(
                primitiveName, id);

            this.tessellateRing(glu, tess, faceInfo.getOuterRing());

            for (VPFPrimitiveData.Ring ring : faceInfo.getInnerRings())
            {
                this.tessellateRing(glu, tess, ring);
            }
        }

        glu.gluTessEndPolygon(tess);
    }

    protected void tessellateRing(GLU glu, GLUtessellator tess, VPFPrimitiveData.Ring ring)
    {
        glu.gluTessBeginContour(tess);

        CompoundVecBuffer buffer = this.primitiveData.getPrimitiveCoords(VPFConstants.EDGE_PRIMITIVE_TABLE);
        int numEdges = ring.getNumEdges();

        for (int i = 0; i < numEdges; i++)
        {
            VecBuffer vecBuffer = buffer.subBuffer(ring.getEdgeId(i));
            Iterable<double[]> iterable = (ring.getEdgeOrientation(i) < 0) ?
                vecBuffer.getReverseCoords(3) : vecBuffer.getCoords(3);

            for (double[] coords : iterable)
            {
                glu.gluTessVertex(tess, coords, 0, coords);
            }
        }

        glu.gluTessEndContour(tess);
    }

    /**
     * Overridden to clear the shape's coordinate buffer upon an unsuccessful tessellation attempt. This ensures the
     * shape won't attempt to re-tessellate itself each frame.
     *
     * @param dc the current DrawContext.
     */
    @Override
    protected void handleUnsuccessfulInteriorTessellation(DrawContext dc)
    {
        super.handleUnsuccessfulInteriorTessellation(dc);

        // If tessellating the shape's interior was unsuccessful, we modify the shape to avoid any additional
        // tessellation attempts, and free any resources that the shape won't use.

        // Free the OGL display list used to store the shape's tessellated contents. This display list is invalid
        // we can avoid consuming additional OGL resources by freeing it here.
        if (this.interiorDisplayList > 0)
        {
            dc.getGL().glDeleteLists(this.interiorDisplayList, 1);
            this.interiorDisplayList = 0;
        }

        // Replace the shape's coordinate buffer with an empty VecBufferSequence . This ensures that any rendering
        // code won't attempt to re-tessellate this shape.
        this.buffer = VecBufferSequence.emptyVecBufferSequence(2);
        // Flag the shape as having changed, since we've replaced its coordinate buffer with an empty VecBufferSequence.
        this.onShapeChanged();
    }
}
