/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.util.*;

/**
 * This class renders fast multiple surface polylines in one pass. It relies on a {@link CompoundVecBuffer}.
 *
 * @author Dave Collins
 * @author Patrick Murris
 * @version $Id: SurfacePolylines.java 13892 2010-09-28 14:13:11Z dcollins $
 */
public class SurfacePolylines extends AbstractSurfaceShape implements Disposable // TODO: Review
{
    protected List<Sector> sectors;
    protected CompoundVecBuffer buffer;
    protected int outlineDisplayList;
    protected boolean needsOutlineTessellation = true;
    protected boolean crossesDateLine = false;

    public SurfacePolylines(CompoundVecBuffer buffer)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.buffer = buffer;
    }

    public SurfacePolylines(Sector sector, CompoundVecBuffer buffer)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sectors = Arrays.asList(sector);
        this.buffer = buffer;
    }

    /**
     * Get the underlying {@link CompoundVecBuffer} describing the geometry.
     *
     * @return the underlying {@link CompoundVecBuffer}.
     */
    public CompoundVecBuffer getBuffer()
    {
        return this.buffer;
    }

    @Override
    public List<Sector> getSectors(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // SurfacePolylines does not interpolate between caller specified positions, therefore it has no path type.
        if (this.sectors == null)
            this.sectors = this.computeSectors(dc);

        return this.sectors;
    }

    public Iterable<? extends LatLon> getLocations(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.getLocations();
    }

    protected List<List<LatLon>> createGeometry(Globe globe, SurfaceTileDrawContext sdc)
    {
        // SurfacePolylines does not invoke this method, so return null indicating this method is not supported.
        // We avoid invoking computeGeometry by overriding determineActiveGeometry below.
        return null;
    }

    public Iterable<? extends LatLon> getLocations()
    {
        return this.buffer.getLocations();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setLocations(Iterable<? extends LatLon> iterable)
    {
        throw new UnsupportedOperationException();
    }

    public Position getReferencePosition()
    {
        Iterator<? extends LatLon> iterator = this.getLocations().iterator();
        if (iterator.hasNext())
            return new Position(iterator.next(), 0);

        return null;
    }

    protected void doMoveTo(Position oldReferencePosition, Position newReferencePosition)
    {
        for (int i = 0; i < this.buffer.size(); i++)
        {
            VecBuffer vb = this.buffer.subBuffer(i);

            for (int pos = 0; pos < vb.getSize(); pos++)
            {
                LatLon ll = vb.getLocation(pos);
                Angle heading = LatLon.greatCircleAzimuth(oldReferencePosition, ll);
                Angle pathLength = LatLon.greatCircleDistance(oldReferencePosition, ll);
                vb.putLocation(pos, LatLon.greatCircleEndPosition(newReferencePosition, heading, pathLength));
            }
        }

        this.onGeometryChanged();
    }

    protected void onGeometryChanged()
    {
        this.sectors = null;
        this.needsOutlineTessellation = true;
        super.onShapeChanged();
    }

    public void dispose()
    {
        // TODO: This is not a proper way to dispose of GL resources because there may not be a current GL context or
        // the current one may not be the one holding the resources.
        GLContext glContext = GLContext.getCurrent();
        if (glContext == null)
            return;

        if (this.outlineDisplayList > 0)
        {
            glContext.getGL().glDeleteLists(this.outlineDisplayList, 1);
            this.outlineDisplayList = 0;
        }
    }

    protected void determineActiveGeometry(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Intentionally left blank in order to override the superclass behavior with nothing.
    }

    protected void drawInterior(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Intentionally left blank; SurfacePolylines does not render an interior.
    }

    protected void drawOutline(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Exit immediately if the Polyline has no coordinate data.
        if (this.buffer.size() == 0)
            return;

        Position referencePos = this.getReferencePosition();
        if (referencePos == null)
            return;

        int hemisphereSign = (int) Math.signum(sdc.getSector().getCentroid().getLongitude().degrees);

        // Attempt to tessellate the Polyline's outline if the Polyline's outline display list is uninitialized, or if
        // the Polyline is marked as needing tessellation.
        if (this.outlineDisplayList <= 0 || this.needsOutlineTessellation)
            this.tessellateOutline(dc, referencePos);

        // Exit immediately if the Polyline's interior failed to tessellate. The cause has already been logged by
        // tessellateInterior.
        if (this.outlineDisplayList <= 0)
            return;

        GL gl = dc.getGL();
        this.applyOutlineState(dc, this.getActiveAttributes());
        gl.glCallList(this.outlineDisplayList);

        if (this.crossesDateLine)
        {
            gl.glPushMatrix();
            try
            {
                // Apply hemisphere offset and draw again
                gl.glTranslated(360 * hemisphereSign, 0, 0);
                gl.glCallList(this.outlineDisplayList);
            }
            finally
            {
                gl.glPopMatrix();
            }
        }
    }

    protected void tessellateOutline(DrawContext dc, LatLon referenceLocation)
    {
        GL gl = dc.getGL();
        this.crossesDateLine = false;

        if (this.outlineDisplayList <= 0)
            this.outlineDisplayList = gl.glGenLists(1);

        gl.glNewList(this.outlineDisplayList, GL.GL_COMPILE);
        try
        {
            // Tessellate each part, note if crossing date line
            for (int i = 0; i < this.buffer.size(); i++)
            {
                if (this.tessellatePart(gl, this.buffer.subBuffer(i), referenceLocation))
                    this.crossesDateLine = true;
            }
        }
        finally
        {
            gl.glEndList();
        }

        this.needsOutlineTessellation = false;
    }

    protected boolean tessellatePart(GL gl, VecBuffer vecBuffer, LatLon referenceLocation)
    {
        Iterable<double[]> iterable = vecBuffer.getCoords(3);
        boolean dateLineCrossed = false;

        gl.glBegin(GL.GL_LINE_STRIP);
        try
        {
            int sign = 0; // hemisphere offset direction
            double previousLongitude = 0;

            for (double[] coords : iterable)
            {
                if (Math.abs(previousLongitude - coords[0]) > 180)
                {
                    // Crossing date line, sum departure point longitude sign for hemisphere offset
                    sign += (int) Math.signum(previousLongitude);
                    dateLineCrossed = true;
                }

                previousLongitude = coords[0];

                double lonDegrees = coords[0] - referenceLocation.getLongitude().degrees;
                double latDegrees = coords[1] - referenceLocation.getLatitude().degrees;
                lonDegrees += sign * 360; // apply hemisphere offset
                gl.glVertex3d(lonDegrees, latDegrees, 0d);
            }
        }
        finally
        {
            gl.glEnd();
        }

        return dateLineCrossed;
    }
}
