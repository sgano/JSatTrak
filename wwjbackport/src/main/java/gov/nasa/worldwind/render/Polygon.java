/*
        Copyright (C) 2001, 2009 United States Government
        as represented by the Administrator of the
        National Aeronautics and Space Administration.
        All Rights Reserved.
        */

package gov.nasa.worldwind.render;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.xml.stream.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.*;
import java.util.*;
import java.util.List;

/**
 * /** A 3D polygon. The polygon may be complex with multiple internal but not intersecting contours. Standard lighting
 * is applied by default and may optionally be disabled.
 * <p/>
 * Polygons are safe to share among World Windows. They should not be shared among layers in the same World Window.
 *
 * @author tag
 * @version $Id: Polygon.java 14179 2010-12-03 00:35:40Z tgaskins $
 */
public class Polygon extends AVListImpl
    implements OrderedRenderable, GeographicExtent, Disposable, Highlightable, Exportable // TODO: Movable, ExtentHolder
{
    /** The default interior color. */
    protected static final Material DEFAULT_INTERIOR_MATERIAL = Material.LIGHT_GRAY;
    /** The default outline color. */
    protected static final Material DEFAULT_OUTLINE_MATERIAL = Material.DARK_GRAY;
    /** The default highlight color. */
    protected static final Material DEFAULT_HIGHLIGHT_MATERIAL = Material.WHITE;
    /** The default geometry regeneration interval, in milliseconds. */
    protected static final long DEFAULT_GEOMETRY_GENERATION_INTERVAL = 2000;
    /** The default altitude mode. */
    protected static final int DEFAULT_ALTITUDE_MODE = WorldWind.ABSOLUTE;

    /** The attributes used if attributes are not specified. */
    protected static final ShapeAttributes defaultAttributes;

    static
    {
        defaultAttributes = new BasicShapeAttributes();
        defaultAttributes.setInteriorMaterial(DEFAULT_INTERIOR_MATERIAL);
        defaultAttributes.setOutlineMaterial(DEFAULT_OUTLINE_MATERIAL);
    }

    /** Holds information for each contour of the polygon. The vertex values are updated at every geometry regeneration. */
    protected static class BoundaryInfo
    {
        protected List<? extends LatLon> locations; // the defining locations or positions of the boundary
        protected Vec4[] vertices; // computed vertices TODO: eliminate need for this; use the vertex buffer instead
        protected IntBuffer fillIndices; // indices identifying the polygon vertices in the vertex buffer
        protected IntBuffer edgeIndices; // indices identifying edges in the vertex buffer
        protected FloatBuffer vertexBuffer; // vertices passed to OpenGL
        protected FloatBuffer normalBuffer; // vertex normals if lighting is applied

        public BoundaryInfo()
        {
            this.locations = new ArrayList<Position>(); // so users don't have to check for null
        }
    }

    /** Holds boundaries and computed information for a polygon. */
    protected class BoundarySet implements Iterable<BoundaryInfo>
    {
        protected List<BoundaryInfo> boundaries = new ArrayList<BoundaryInfo>();
        protected Vec4 referencePoint; // the Cartesian coordinates of the reference point
        protected Extent extent; // the shape's extent
        protected FloatBuffer vertexBuffer;
        protected FloatBuffer normalBuffer;
        protected FloatBuffer textureCoordsBuffer; // texture coords if texturing

        protected BoundarySet()
        {
        }

        protected BoundarySet(BoundarySet bSet)
        {
            for (BoundaryInfo boundary : bSet)
            {
                BoundaryInfo newBoundary = new BoundaryInfo();

                newBoundary.locations = boundary.locations;

                this.boundaries.add(newBoundary);
            }
        }

        public Iterator<BoundaryInfo> iterator()
        {
            return this.boundaries.iterator();
        }
    }

    /**
     * This static hash map holds the vertex indices that define the shape's visual outline. The contents depend only on
     * the number of locations in the source polygon, so they can be reused by all shapes with the same location count.
     */
    protected static HashMap<Integer, IntBuffer> edgeIndexBuffers = new HashMap<Integer, IntBuffer>();

    protected Object imageSource; // image source for the optional texture
    protected WWTexture texture; // an optional texture for the base polygon
    protected Double rotation; // in degrees; positive is CCW
    protected Position referencePosition; // the location/position to use as the shape's reference point

    protected ShapeAttributes normalAttributes;
    protected ShapeAttributes highlightAttributes;
    protected ShapeAttributes activeAttributes = new BasicShapeAttributes();
    protected int altitudeMode = DEFAULT_ALTITUDE_MODE;
    protected boolean visible = true;
    protected boolean highlighted = false;
    protected boolean enableDepthOffset = false;
    protected int outlinePickWidth = 10;
    protected boolean enableBatchRendering = true;
    protected boolean enableBatchPicking = true;
    protected long geometryRegenerationInterval = DEFAULT_GEOMETRY_GENERATION_INTERVAL;

    protected BoundarySet boundarySet = new BoundarySet();

    // These values are determined with every geometry regeneration.
    protected long frameID; // the ID of the most recent rendering frame
    protected long visGeomRegenFrame = -1;
    protected double previousExaggeration = -1;
    protected int totalNumLocations; // number of specified locations in the entire polygon
    protected Matrix rotationMatrix;
    protected double eyeDistance;
    protected Sector sector; // the shape's bounding sector
    protected Layer pickLayer; // shape's layer when ordered renderable was created
    protected OGLStackHandler BEogsh = new OGLStackHandler();

    // Tessellation fields
    protected IntBuffer fillIndices;
    protected ArrayList<IntBuffer> fillIndexBuffers;
    protected ArrayList<Integer> primTypes;

    protected PickSupport pickSupport = new PickSupport();

    /** Polygons are drawn as {@link gov.nasa.worldwind.render.OutlinedShape}s. */
    protected OutlinedShape outlineShapeRenderer = new OutlinedShape()
    {
        public boolean isDrawOutline(DrawContext dc, Object shape)
        {
            return ((Polygon) shape).mustDrawOutline();
        }

        public boolean isDrawInterior(DrawContext dc, Object shape)
        {
            return ((Polygon) shape).mustDrawInterior();
        }

        public boolean isEnableDepthOffset(DrawContext dc, Object shape)
        {
            return ((Polygon) shape).isEnableDepthOffset();
        }

        public void drawOutline(DrawContext dc, Object shape)
        {
            ((Polygon) shape).drawOutline(dc);
        }

        public void drawInterior(DrawContext dc, Object shape)
        {
            ((Polygon) shape).drawInterior(dc);
        }

        public Double getDepthOffsetFactor(DrawContext dc, Object shape)
        {
            return null;
        }

        public Double getDepthOffsetUnits(DrawContext dc, Object shape)
        {
            return null;
        }
    };

    /** Construct polygon with empty boundaries. */
    public Polygon()
    {
    }

    /**
     * Construct a polygon for a specified outer boundary.
     *
     * @param corners the list of locations defining the polygon.
     *
     * @throws IllegalArgumentException if the location list is null.
     */
    public Polygon(Iterable<? extends Position> corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(corners);
    }

    /**
     * Construct a polygon for a specified list of outer-boundary positions.
     *
     * @param corners the list of positions -- latitude longitude and altitude -- defining the polygon. The current
     *                altitude mode determines whether the positions are considered relative to mean sea level (they are
     *                "absolute") or the ground elevation at the associated latitude and longitude.
     *
     * @throws IllegalArgumentException if the position list is null.
     */
    public Polygon(Position.PositionList corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(corners.list);
    }

    /** Void any computed data. Called when a factor affecting the computed data is changed. */
    protected void reinitialize()
    {
        // Assumes that the boundary list has already been established.

        for (BoundaryInfo boundary : this.boundarySet)
        {
            //noinspection StringEquality
            if (WWMath.computeWindingOrderOfLocations(boundary.locations) != AVKey.COUNTER_CLOCKWISE)
                Collections.reverse(boundary.locations);

            boundary.edgeIndices = this.getEdgeIndices(boundary.locations.size());
        }

        this.totalNumLocations = 0;
        this.boundarySet.extent = null;
        this.sector = null;
        this.rotationMatrix = null;
        this.previousIntersectionBoundarySet = null;
        this.previousIntersectionTerrain = null;
    }

    /** {@inheritDoc} */
    public void dispose()
    {
        // Remove references to NIO buffers
        this.fillIndices = null;
        if (this.fillIndexBuffers != null)
            this.fillIndexBuffers.clear();

        this.boundarySet.boundaries.clear();
    }

    /**
     * Returns a reference to the outer boundary of the polygon.
     *
     * @return the polygon's outer boundary, or null if there is no outer boundary or it has no locations.
     */
    protected BoundaryInfo outerBoundary()
    {
        return this.boundarySet.boundaries.size() > 0 ? this.boundarySet.boundaries.get(0) : null;
    }

    /**
     * Returns the list of locations or positions defining the polygon's outer boundary.
     *
     * @return the polygon's positions, or null if the outer boundary has no positions.
     */
    public Iterable<? extends LatLon> getOuterBoundary()
    {
        return this.outerBoundary() != null ? this.outerBoundary().locations : null;
    }

    /**
     * Specifies the latitude, longitude and altitude of the positions defining the polygon.
     *
     * @param corners the polygon positions. A duplicate of the first position is appended to the list if the list's
     *                last position is not identical to the first.
     *
     * @throws IllegalArgumentException if the location list is null or contains fewer than three locations.
     */
    public void setOuterBoundary(Iterable<? extends Position> corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BoundaryInfo outerBoundary = new BoundaryInfo();
        if (this.boundarySet.boundaries.size() > 0)
            this.boundarySet.boundaries.set(0, outerBoundary);
        else
            this.boundarySet.boundaries.add(outerBoundary);

        ArrayList<LatLon> list = new ArrayList<LatLon>();
        for (LatLon corner : corners)
        {
            if (corner != null)
                list.add(corner);
        }

        if (list.size() < 3)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Close the list if not already closed.
        if (list.size() > 0 && !list.get(0).equals(list.get(list.size() - 1)))
            list.add(list.get(0));

        list.trimToSize();
        outerBoundary.locations = list;

        this.reinitialize();
    }

    /**
     * Add an inner boundary to the polygon. A duplicate of the first position is appended to the list if the list's
     * last position is not identical to the first.
     *
     * @param corners the polygon positions.
     *
     * @throws IllegalArgumentException if the location list is null or contains fewer than three locations.
     */
    public void addInnerBoundary(Iterable<? extends Position> corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BoundaryInfo boundary = new BoundaryInfo();
        this.boundarySet.boundaries.add(boundary);

        ArrayList<LatLon> list = new ArrayList<LatLon>();
        for (LatLon corner : corners)
        {
            if (corner != null)
                list.add(corner);
        }

        if (list.size() < 3)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Close the list if not already closed.
        if (list.size() > 0 && !list.get(0).equals(list.get(list.size() - 1)))
            list.add(list.get(0));

        list.trimToSize();
        boundary.locations = list;

        this.reinitialize();
    }

    /**
     * Returns the texture image source.
     *
     * @return the texture image source, or null if no source has been specified.
     */
    public Object getTextureImageSource()
    {
        return this.imageSource;
    }

    /**
     * Get the texture applied to the polygon. The texture is loaded on a background thread. This method will return
     * null until the texture has been loaded.
     *
     * @return The texture or null if there is no texture, or if texture is not yet available.
     */
    protected WWTexture getTexture()
    {
        if (this.texture != null)
            return this.texture;
        else
            return this.initializeTexture();
    }

    /**
     * Returns the texture coordinates for the polygon.
     *
     * @return the texture coordinates, or null if no texture coordinates have been specified.
     */
    public float[] getTextureCoords()
    {
        if (this.boundarySet.textureCoordsBuffer == null)
            return null;

        float[] retCoords = new float[this.boundarySet.textureCoordsBuffer.limit()];
        this.boundarySet.textureCoordsBuffer.get(retCoords, 0, retCoords.length);

        return retCoords;
    }

    /**
     * Specifies the texture to apply to the polygon.
     *
     * @param imageSource   the texture image source. May be a {@link String} identifying a file path or URL, a {@link
     *                      File}, or a {@link java.net.URL}.
     * @param texCoords     the (s, t) texture coordinates aligning the image to the polygon. There must be one texture
     *                      coordinate pair, (s, t), for each polygon location in the polygon's outer boundary.
     * @param texCoordCount the number of texture coordinates, (s, v) pairs, specified.
     *
     * @throws IllegalArgumentException if the image source is not null and either the texture coordinates are null or
     *                                  inconsistent with the specified texture-coordinate count, or there are fewer
     *                                  than three texture coordinate pairs.
     */
    public void setTextureImageSource(Object imageSource, float[] texCoords, int texCoordCount)
    {
        if (imageSource == null)
        {
            this.texture = null;
            this.boundarySet.textureCoordsBuffer = null;
            return;
        }

        if (texCoords == null)
        {
            String message = Logging.getMessage("generic.ListIsEmpty");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (texCoordCount < 3 || texCoords.length < 2 * texCoordCount)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageSource = imageSource;
        this.texture = null; // New image source, need to reload texture

        // Determine whether the tex-coord list needs to be closed.
        boolean closeIt = texCoords[0] != texCoords[texCoordCount - 2] || texCoords[1] != texCoords[texCoordCount - 1];

        this.boundarySet.textureCoordsBuffer = BufferUtil.newFloatBuffer(2 * (texCoordCount + (closeIt ? 1 : 0)));
        for (int i = 0; i < 2 * texCoordCount; i++)
        {
            this.boundarySet.textureCoordsBuffer.put(texCoords[i]);
        }

        if (closeIt)
        {
            this.boundarySet.textureCoordsBuffer.put(this.boundarySet.textureCoordsBuffer.get(0));
            this.boundarySet.textureCoordsBuffer.put(this.boundarySet.textureCoordsBuffer.get(1));
        }
    }

    /**
     * Create and initialize the texture from the image source. If the image is not in memory this method will request
     * that it be loaded and return null.
     *
     * @return The texture, or null if the texture is not yet available.
     */
    protected WWTexture initializeTexture()
    {
        Object imageSource = this.getTextureImageSource();
        if (imageSource instanceof String || imageSource instanceof URL)
        {
            URL imageURL = WorldWind.getDataFileStore().requestFile(imageSource.toString());
            if (imageURL != null)
            {
                this.texture = new BasicWWTexture(imageURL, true);
                return this.texture;
            }
            // Else wait for the retriever to retrieve the image before creating the texture
        }
        else if (imageSource != null)
        {
            this.texture = new BasicWWTexture(imageSource, true);
            return this.texture;
        }

        return null;
    }

    /**
     * Counts the total number of positions in the polygon, including all positions in all boundaries.
     *
     * @return the number of positions in the polygon.
     */
    protected int countPositions()
    {
        int count = 0;

        for (BoundaryInfo boundary : this.boundarySet.boundaries)
        {
            count += boundary.locations.size();
        }

        return count;
    }

    /**
     * Indicates whether the polygon should be drawn.
     *
     * @return true if the polygon is drawn, otherwise false.
     */
    public boolean isVisible()
    {
        return this.visible;
    }

    /**
     * Specifies whether the polygon should be drawn.
     *
     * @param visible true if the polygon is drawn, otherwise false.
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * Indicates whether the filled sides of the polygons should be offset towards the viewer to help eliminate
     * artifacts when two or more faces of this or other filled shapes are coincident.
     *
     * @return true if depth offset is applied, otherwise false.
     */
    public boolean isEnableDepthOffset()
    {
        return this.enableDepthOffset;
    }

    /**
     * Specifies whether the filled sides of the polygons should be offset towards the viewer to help eliminate
     * artifacts when two or more faces of this or other filled shapes are coincident. See {@link
     * gov.nasa.worldwind.render.Offset}.
     *
     * @param enableDepthOffset true if depth offset is applied, otherwise false.
     */
    public void setEnableDepthOffset(boolean enableDepthOffset)
    {
        this.enableDepthOffset = enableDepthOffset;
    }

    /** {@inheritDoc} */
    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    /** {@inheritDoc} */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    /** {@inheritDoc} * */
    public double getDistanceFromEye()
    {
        return this.eyeDistance;
    }
//
//    /**
//     * Indicates the position to use as a reference position for computed geometry.
//     *
//     * @return the reference position, or null if no reference position has been specified.
//     */
//    public Position getReferencePosition()
//    {
//        return this.referencePosition;
//    }

    /**
     * Indicates the position to use as a reference position for computed geometry.
     *
     * @return the reference position, or null if no reference position has been specified.
     */
    public Position getReferencePosition()
    {
        if (this.referencePosition != null)
            return this.referencePosition;

        if (this.outerBoundary() != null && this.outerBoundary().locations != null
            && this.outerBoundary().locations.size() > 0)
        {
            if (this.outerBoundary().locations.get(0) instanceof Position)
                this.referencePosition = (Position) this.outerBoundary().locations.get(0);
            else
                this.referencePosition = new Position(this.outerBoundary().locations.get(0), 0);
        }

        return this.referencePosition;
    }

    /**
     * Specifies the position to use as a reference position for computed geometry. This value should typically left to
     * the default value of the first position in the polygon's outer boundary.
     *
     * @param referencePosition the reference position. May be null, in which case the first position of the outer
     *                          boundary is the reference position.
     */
    public void setReferencePosition(Position referencePosition)
    {
        this.referencePosition = referencePosition;
    }

    /**
     * Indicates the outline line width to use during picking. A larger width than the display width typically makes the
     * outline easier to pick.
     *
     * @return the outline line width used during picking. The default is 10.
     */
    public int getOutlinePickWidth()
    {
        return this.outlinePickWidth;
    }

    /**
     * Specifies the outline line width to use during picking. A larger width than display width typically makes the
     * outline easier to pick.
     *
     * @param outlinePickWidth the outline pick width.
     *
     * @throws IllegalArgumentException if the width is less than 0.
     */
    public void setOutlinePickWidth(int outlinePickWidth)
    {
        if (outlinePickWidth < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlinePickWidth = outlinePickWidth;
    }

    /**
     * Returns the attributes applied to the polygon when it's not highlighted.
     *
     * @return the polygon's atributes.
     */
    public ShapeAttributes getAttributes()
    {
        return this.normalAttributes;
    }

    /**
     * Specifies the attributes applied to the polygon when it's not highlighted.
     *
     * @param attributes the polygon's atributes.
     */
    public void setAttributes(ShapeAttributes attributes)
    {
        if (attributes == null)
        {
            String message = "nullValue.AttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.normalAttributes = attributes;
    }

    /**
     * Returns the attributes applied to the polygon when it is highlighted.
     *
     * @return the polygon's highlight atributes.
     */
    public ShapeAttributes getHighlightAttributes()
    {
        return this.highlightAttributes;
    }

    /**
     * Specifies the attributes applied to the polygon when it is highlighted.
     *
     * @param attributes the polygon's highlight atributes. May be null, in which case default attributes are used for
     *                   highlighting.
     *
     * @throws IllegalArgumentException if attributes is null.
     */
    public void setHighlightAttributes(ShapeAttributes attributes)
    {
        this.highlightAttributes = attributes;
    }

    /**
     * Each time the polygon is rendered the appropriate attributes for the current mode are determined. This method
     * returns the resolved attributes.
     *
     * @return the polygon's currently active attributes.
     */
    protected ShapeAttributes getActiveAttributes()
    {
        return this.activeAttributes;
    }

    /**
     * Returns the polygon's altitude mode, one of {@link WorldWind#RELATIVE_TO_GROUND} or {@link WorldWind#ABSOLUTE}.
     * The altitude mode [@link WorldWind#CLAMP_TO_GROUND} is not supported. The default altitude mode is {@link
     * WorldWind#ABSOLUTE}.
     * <p/>
     * Note: Subclasses may recognize additional altitude modes or may not recognize the ones described above.
     *
     * @return the polygon's altitude mode.
     */
    public int getAltitudeMode()
    {
        return this.altitudeMode;
    }

    /**
     * Specifies the polygon's altitude mode, one of {@link WorldWind#RELATIVE_TO_GROUND} or {@link WorldWind#ABSOLUTE}.
     * The altitude mode [@link WorldWind#CLAMP_TO_GROUND} is not supported. The default altitude mode is {@link
     * WorldWind#ABSOLUTE}.
     * <p/>
     * Note: Subclasses may recognize additional altitude modes or may not recognize the ones described above.
     *
     * @param altitudeMode the polygon's altitude mode.
     */
    public void setAltitudeMode(int altitudeMode)
    {
        this.altitudeMode = altitudeMode;
    }

    /**
     * Indicates whether batch rendering is enabled.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchRendering(boolean).
     */
    public boolean isEnableBatchRendering()
    {
        return this.enableBatchRendering;
    }

    /**
     * Specifies whether adjacent Polygons in the ordered renderable list may be rendered together if they are contained
     * in the same layer. This increases performance and there is seldom a reason to disable it.
     *
     * @param enableBatchRendering true to enable batch rendering, otherwise false.
     */
    public void setEnableBatchRendering(boolean enableBatchRendering)
    {
        this.enableBatchRendering = enableBatchRendering;
    }

    /**
     * Indicates whether batch picking is enabled.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchPicking(boolean).
     */
    public boolean isEnableBatchPicking()
    {
        return this.enableBatchPicking;
    }

    /**
     * Specifies whether adjacent Polygons in the ordered renderable list may be pick-tested together if they are
     * contained in the same layer. This increases performance but allows only the top-most of the polygons to be
     * reported in a {@link gov.nasa.worldwind.event.SelectEvent} even if several of the polygons are at the pick
     * position.
     * <p/>
     * Batch rendering ({@link #setEnableBatchRendering(boolean)}) must be enabled in order for batch picking to occur.
     *
     * @param enableBatchPicking true to enable batch rendering, otherwise false.
     */
    public void setEnableBatchPicking(boolean enableBatchPicking)
    {
        this.enableBatchPicking = enableBatchPicking;
    }

    /**
     * Indicates the amount of rotation applied to the polygon.
     *
     * @return the rotation in degrees, or null if no rotation is specified.
     */
    public Double getRotation()
    {
        return this.rotation;
    }

    /**
     * Specifies the amount of rotation to apply to the polygon. Positive rotation is counter-clockwise.
     *
     * @param rotation the amount of rotation to apply, in degrees, or null to apply no rotation.
     */
    public void setRotation(Double rotation)
    {
        this.rotation = rotation;
        this.rotationMatrix = null;
    }

    /**
     * Indicates the maximum length of time between geometry regenerations. See {@link
     * #setGeometryRegenerationInterval(long)} for the regeneration-interval's description.
     *
     * @return the geometry regeneration interval, in milliseconds.
     *
     * @see #setGeometryRegenerationInterval(long)
     */
    public long getGeometryRegenerationInterval()
    {
        return geometryRegenerationInterval;
    }

    /**
     * Specifies the maximum length of time between geometry regenerations. The geometry is regenerated when the
     * polygon's altitude mode is {@link WorldWind#RELATIVE_TO_GROUND} in order to capture changes to the terrain. (The
     * terrain changes when its resolution changes or when new elevation data is returned from a server.) Decreasing
     * this value causes the geometry to more quickly track terrain changes, but at the cost of performance. Increasing
     * this value often does not have much effect because there are limiting factors other than geometry regeneration.
     *
     * @param geometryRegenerationInterval the geometry regeneration interval, in milliseconds.
     */
    public void setGeometryRegenerationInterval(long geometryRegenerationInterval)
    {
        this.geometryRegenerationInterval = geometryRegenerationInterval;
    }

    /**
     * Returns the polygon's extent.
     *
     * @return the polygon's extent.
     */
    protected Extent getExtent()
    {
        return this.boundarySet.extent;
    }

    /**
     * Computes the polygon's extent. Uses the <code>vertices</code> field of the outer boundary, so there is no need
     * for subclasses to override this method.
     *
     * @param boundary The boundary to compute the extent for.
     * @param refPoint the shape's reference point.
     *
     * @return the polygon's extent. Returns null if the polygon's vertices have not been computed.
     */
    protected Extent computeExtent(BoundaryInfo boundary, Vec4 refPoint)
    {
        if (boundary == null || boundary.vertices == null)
            return null;

        // The bounding box is computed relative to the polygon's reference point, so it needs to be translated to
        // model coordinates in order to indicate its model-coordinate extent.
        Box boundingBox = Box.computeBoundingBox(Arrays.asList(boundary.vertices));
        return boundingBox != null ? boundingBox.translate(refPoint) : null;
    }

    /** {@inheritDoc} */
    public Sector getSector()
    {
        if (this.sector == null && this.getOuterBoundary() != null)
            this.sector = Sector.boundingSector(this.getOuterBoundary());

        return this.sector;
    }

    /**
     * Indicates whether the polygon's interior must be drawn.
     *
     * @return true if an interior must be drawn, otherwise false.
     */
    protected boolean mustDrawInterior()
    {
        return this.getActiveAttributes().isDrawInterior();
    }

    /**
     * Indicates whether the polygon's outline must be drawn.
     *
     * @return true if the outline should be drawn, otherwise false.
     */
    protected boolean mustDrawOutline()
    {
        return this.getActiveAttributes().isDrawOutline();
    }

    /**
     * Indicates whether texture must be applied, which it must when textures are specified.
     *
     * @param dc the current draw context.
     *
     * @return true if texture must be applied, otherwise false.
     */
    protected boolean mustApplyTexture(DrawContext dc)
    {
        return !dc.isPickingMode() && this.getTexture() != null && this.boundarySet.textureCoordsBuffer != null;
    }

    /**
     * Indicates whether standard lighting must be applied.
     *
     * @param dc the current draw context
     *
     * @return true if lighting must be applied, otherwise false.
     */
    protected boolean mustApplyLighting(DrawContext dc)
    {
        return !dc.isPickingMode() && this.activeAttributes.isEnableLighting();
    }

    @SuppressWarnings({"UnusedDeclaration", "SimplifiableIfStatement"})
    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        if (this.boundarySet.vertexBuffer == null || dc.getVerticalExaggeration() != this.previousExaggeration)
            return true;

        return this.getAltitudeMode() != WorldWind.ABSOLUTE
            && this.frameID - this.visGeomRegenFrame > this.getGeometryRegenerationInterval();
    }

    /**
     * Create a {@link gov.nasa.worldwind.pick.PickedObject} for this polygon. The PickedObject returned by this method
     * will be added to the pick list to represent the current polygon.
     *
     * @param dc        Active draw context.
     * @param pickColor Unique color for this PickedObject.
     *
     * @return A new picked object.
     */
    protected PickedObject createPickedObject(DrawContext dc, Color pickColor)
    {
        return new PickedObject(pickColor.getRGB(), this);
    }

    /** Determines which attributes -- normal, highlight or default -- to use each frame. */
    protected void determineActiveAttributes()
    {
        if (this.isHighlighted())
        {
            if (this.getHighlightAttributes() != null)
                this.activeAttributes.copy(this.getHighlightAttributes());
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // to cause highlighting.
                if (this.getAttributes() != null)
                    this.activeAttributes.copy(this.getAttributes());
                else
                    this.activeAttributes.copy(defaultAttributes);

                this.activeAttributes.setOutlineMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeAttributes.setInteriorMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
            }
        }
        else
        {
            if (this.getAttributes() != null)
                this.activeAttributes.copy(this.getAttributes());
            else
                this.activeAttributes.copy(defaultAttributes);
        }
    }

    /**
     * Indicates whether the shape is visible in the current view.
     *
     * @param dc the draw context.
     *
     * @return true if the shape is visible, otherwise false.
     */
    @SuppressWarnings({"RedundantIfStatement"})
    protected boolean intersectsFrustum(DrawContext dc)
    {
        Extent extent = this.getExtent();
        if (extent == null)
            return true; // don't know the visibility, shape hasn't been computed yet

        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(extent);

        return dc.getView().getFrustumInModelCoordinates().intersects(extent);
    }

    /** {@inheritDoc} */
    public void pick(DrawContext dc, Point pickPoint)
    {
        // This method is called only when ordered renderables are being drawn.
        // Arg checked within call to render.

        this.pickSupport.clearPickList();
        try
        {
            this.pickSupport.beginPicking(dc);
            this.render(dc);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, dc.getCurrentLayer());
        }
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        // This render method is called three times during frame generation. It's first called as a {@link Renderable}
        // during <code>Renderable</code> picking. It's called again during normal rendering. And it's called a third
        // time as an OrderedRenderable. The first two calls determine whether to add the polygon to the ordered
        // renderable list during pick and render. The third call just draws the ordered renderable.
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (this.boundarySet.boundaries.size() == 0 || this.outerBoundary().locations.size() < 3)
            return;

        if (!this.isVisible())
            return;

        if (this.getExtent() != null)
        {
            if (!this.intersectsFrustum(dc))
                return;

            // If the polygon is less that a pixel in size, don't render it.
            if (dc.isSmall(this.getExtent(), 1))
                return;
        }

        if (dc.isOrderedRenderingMode())
            this.drawOrderedRenderable(dc);
        else
            this.makeOrderedRenderable(dc);

        this.frameID = dc.getFrameTimeStamp();
    }

    /**
     * Prepares the shape as an {@link OrderedRenderable} and adds it to the draw context's ordered renderable list.
     *
     * @param dc the draw context.
     */
    protected void makeOrderedRenderable(DrawContext dc)
    {
        if (dc.getFrameTimeStamp() != this.frameID)
            this.determineActiveAttributes();

        // Re-use values already calculated this frame.
        if (this.mustRegenerateGeometry(dc)) // TODO: account for multi-window
        {
            if (dc.getSurfaceGeometry() == null || this.boundarySet.boundaries.size() < 1)
                return;

            if (this.totalNumLocations == 0)
                this.totalNumLocations = this.countPositions();
            if (this.totalNumLocations < 3)
                return;

            if (this.getRotation() != null && this.rotationMatrix == null)
                this.computeRotationMatrix(dc.getGlobe());

            this.createMinimalGeometry(dc);

            if (this.getExtent() == null)
                return;

            // If the shape is less that a pixel in size, don't render it.
            if (dc.isSmall(this.getExtent(), 1))
                return;

            if (!this.intersectsFrustum(dc))
                return;

            this.createVisualGeometry(dc, dc.getTerrain(), this.boundarySet, true);
            this.visGeomRegenFrame = dc.getFrameTimeStamp();
            this.previousExaggeration = dc.getVerticalExaggeration();
        }

        if (!this.intersectsFrustum(dc))
            return;

        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();

        dc.addOrderedRenderable(this);
    }

    /**
     * Draws the shape as an ordered renderable.
     *
     * @param dc the current draw context.
     */
    protected void drawOrderedRenderable(DrawContext dc)
    {
        this.beginDrawing(dc);
        try
        {
            this.doDrawOrderedRenderable(dc);

            if (this.isEnableBatchRendering())
                this.drawBatched(dc);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    /**
     * Establish the OpenGL state needed to draw the shape.
     * <p/>
     * Note: This method is called only at the beginning of batch rendering or picking. Its settings apply to all shapes
     * rendered or picked in batch with this one, so it should not make any instance-specific settings.
     *
     * @param dc the current draw context.
     */
    protected void beginDrawing(DrawContext dc)
    {
        GL gl = dc.getGL();
        this.BEogsh.clear();

        int attrMask = GL.GL_CURRENT_BIT | GL.GL_DEPTH_BUFFER_BIT
            | GL.GL_LINE_BIT | GL.GL_HINT_BIT // for outline
            | GL.GL_POLYGON_BIT // for interior
            | GL.GL_COLOR_BUFFER_BIT
            | GL.GL_TEXTURE_BIT | GL.GL_TRANSFORM_BIT; // for texture

        this.BEogsh.pushAttrib(gl, attrMask);

        if (!dc.isPickingMode())
        {
            dc.beginStandardLighting();

            gl.glEnable(GL.GL_LINE_SMOOTH);

            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);

            // Push an identity texture matrix. This prevents drawSides() from leaking GL texture matrix state. The
            // texture matrix stack is popped from OGLStackHandler.pop(), in the finally block below.
            this.BEogsh.pushTextureIdentity(gl);
        }

        gl.glDisable(GL.GL_CULL_FACE);

        this.BEogsh.pushClientAttrib(gl, GL.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY); // all drawing uses vertex arrays

        dc.getView().pushReferenceCenter(dc, this.boundarySet.referencePoint);
    }

    /**
     * Pop the state set in beginDrawing. If batch rendering is enabled, this method is called once at the end of batch
     * rendering.
     *
     * @param dc the current draw context.
     */
    protected void endDrawing(DrawContext dc)
    {
        dc.getView().popReferenceCenter(dc);

        if (!dc.isPickingMode())
            dc.endStandardLighting();

        this.BEogsh.pop(dc.getGL());
    }

    /**
     * Draws this ordered renderable. If batch rendering is enabled, draws all subsequently adjacent Polygon
     * ordered renderables in the ordered renderable. In picking mode with batch rendering and batch picking enabled,
     * only adjacent shapes with the same parent layer are batched.
     *
     * @param dc the current draw context.
     */
    protected void drawBatched(DrawContext dc)
    {
        // Draw as many as we can in a batch to save ogl state switching.
        Object nextItem = dc.peekOrderedRenderables();

        if (!dc.isPickingMode())
        {
            while (nextItem != null && nextItem.getClass() == this.getClass())
            {
                Polygon p = (Polygon) nextItem;
                if (!p.isEnableBatchRendering())
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                p.doDrawOrderedRenderable(dc);

                nextItem = dc.peekOrderedRenderables();
            }
        }
        else if (this.isEnableBatchPicking())
        {
            Layer currentLayer = dc.getCurrentLayer();

            while (nextItem != null && nextItem.getClass() == this.getClass())
            {
                Polygon p = (Polygon) nextItem;
                if (!p.isEnableBatchRendering() || !p.isEnableBatchPicking())
                    break;

                if (p.pickLayer != currentLayer) // batch pick only within a single layer
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                p.doDrawOrderedRenderable(dc);

                nextItem = dc.peekOrderedRenderables();
            }
        }
    }

    /**
     * Draw this ordered renderable.
     *
     * @param dc the current draw context.
     */
    protected void doDrawOrderedRenderable(DrawContext dc)
    {
        GL gl = dc.getGL();

        dc.getView().setReferenceCenter(dc, this.boundarySet.referencePoint);

        if (this.mustApplyLighting(dc))
        {
            gl.glEnable(GL.GL_LIGHTING);
            gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        }
        else
        {
            gl.glDisable(GL.GL_LIGHTING);
            gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        }

        if (dc.isPickingMode())
        {
            Color pickColor = dc.getUniquePickColor();
            this.pickSupport.addPickableObject(pickColor.getRGB(), this, null);
            gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
        }

        dc.drawOutlinedShape(this.outlineShapeRenderer, this);
    }

    // TODO: Try to consolidate the state changes in the below drawing methods

    /**
     * Draws the shape's edges. Assumes the vertex buffer has already been set in the OpenGL context.
     * <p/>
     * This base implementation draws the outline of the basic polygon. Subclasses should override it to draw their
     * outline or an alternate outline of the basic polygon.
     *
     * @param dc the draw context.
     */
    public void drawOutline(DrawContext dc)
    {
        ShapeAttributes activeAttrs = this.getActiveAttributes();

        if (!activeAttrs.isDrawOutline())
            return;

        GL gl = dc.getGL();

        if (!dc.isPickingMode())
        {
            Material material = activeAttrs.getOutlineMaterial();
            if (material == null)
                material = defaultAttributes.getOutlineMaterial();

            if (this.mustApplyLighting(dc))
            {
                material.apply(gl, GL.GL_FRONT_AND_BACK, (float) this.getActiveAttributes().getOutlineOpacity());
            }
            else
            {
                Color sc = material.getDiffuse();
                double opacity = activeAttrs.getOutlineOpacity();
                gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));
            }

            gl.glHint(GL.GL_LINE_SMOOTH_HINT, activeAttrs.isEnableAntialiasing() ? GL.GL_NICEST : GL.GL_DONT_CARE);
        }

        if (dc.isPickingMode() && activeAttrs.getOutlineWidth() < this.getOutlinePickWidth())
            gl.glLineWidth(this.getOutlinePickWidth());
        else
            gl.glLineWidth((float) activeAttrs.getOutlineWidth());

        if (activeAttrs.getOutlineStippleFactor() > 0)
        {
            gl.glEnable(GL.GL_LINE_STIPPLE);
            gl.glLineStipple(activeAttrs.getOutlineStippleFactor(), activeAttrs.getOutlineStipplePattern());
        }

        for (BoundaryInfo boundary : this.boundarySet.boundaries)
        {
            if (this.mustApplyLighting(dc))
                gl.glNormalPointer(GL.GL_FLOAT, 0, boundary.normalBuffer.rewind());

            IntBuffer indices = boundary.edgeIndices;
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, boundary.vertexBuffer.rewind());
            gl.glDrawElements(GL.GL_LINES, indices.limit(), GL.GL_UNSIGNED_INT, indices.rewind());
        }
        //
        //        // Diagnostic to show the normal vectors.
        //        if (this.mustApplyLighting(dc))
        //            dc.drawNormals(1000, this.vertexBuffer, this.normalBuffer);
    }

    /**
     * Draws the shape's filled interior. Assumes the vertex buffer has already been set in the OpenGL context.
     * <p/>
     * This base implementation draws the interior of the basic polygon. Subclasses should override it to draw their
     * interior or an alternate interior of the basic polygon.
     *
     * @param dc the draw context.
     */
    public void drawInterior(DrawContext dc)
    {
        ShapeAttributes activeAttrs = this.getActiveAttributes();

        if (!activeAttrs.isDrawInterior())
            return;

        GL gl = dc.getGL();

        if (!dc.isPickingMode())
        {
            Material material = activeAttrs.getInteriorMaterial();
            if (material == null)
                material = defaultAttributes.getInteriorMaterial();

            if (this.activeAttributes.isEnableLighting())
            {
                material.apply(gl, GL.GL_FRONT_AND_BACK, (float) activeAttrs.getInteriorOpacity());
                gl.glNormalPointer(GL.GL_FLOAT, 0, this.boundarySet.normalBuffer.rewind());
            }
            else
            {
                Color sc = material.getDiffuse();
                double opacity = activeAttrs.getInteriorOpacity();
                gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));
            }
        }

        WWTexture texture = this.getTexture();
        if (!dc.isPickingMode() && texture != null && this.boundarySet.textureCoordsBuffer != null)
        {
            texture.bind(dc);
            texture.applyInternalTransform(dc);

            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, this.boundarySet.textureCoordsBuffer.rewind());
            dc.getGL().glEnable(GL.GL_TEXTURE_2D);
            gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        }
        else
        {
            dc.getGL().glDisable(GL.GL_TEXTURE_2D);
            gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        }

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, this.boundarySet.vertexBuffer.rewind());

        for (int i = 0; i < this.primTypes.size(); i++)
        {
            IntBuffer ib = this.fillIndexBuffers.get(i);
            gl.glDrawElements(this.primTypes.get(i), ib.limit(), GL.GL_UNSIGNED_INT, ib.rewind());
        }
    }

    protected int vertexVBOId[];

    public void drawInteriorVBO(DrawContext dc)
    {
        ShapeAttributes activeAttrs = this.getActiveAttributes();

        if (!activeAttrs.isDrawInterior())
            return;

        GL gl = dc.getGL();

        if (!dc.isPickingMode())
        {
            Material material = activeAttrs.getInteriorMaterial();
            if (material == null)
                material = defaultAttributes.getInteriorMaterial();

            if (this.activeAttributes.isEnableLighting())
            {
                material.apply(gl, GL.GL_FRONT_AND_BACK, (float) activeAttrs.getInteriorOpacity());
                gl.glNormalPointer(GL.GL_FLOAT, 0, this.boundarySet.normalBuffer.rewind());
            }
            else
            {
                Color sc = material.getDiffuse();
                double opacity = activeAttrs.getInteriorOpacity();
                gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));
            }
        }

        if (!dc.isPickingMode() && this.boundarySet.textureCoordsBuffer != null)
        {
            WWTexture texture = this.getTexture();
            texture.bind(dc);
            texture.applyInternalTransform(dc);

            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, this.boundarySet.textureCoordsBuffer.rewind());
            dc.getGL().glEnable(GL.GL_TEXTURE_2D);
            gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        }
        else
        {
            dc.getGL().glDisable(GL.GL_TEXTURE_2D);
            gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        }

        boolean fillBuffers = false;
        if (dc.getGLRuntimeCapabilities().isVertexBufferObjectAvailable())
        {
            if (this.vertexVBOId == null)
            {
                fillBuffers = true;
                this.vertexVBOId = new int[this.primTypes.size() + 1];

                gl.glGenBuffers(this.vertexVBOId.length, vertexVBOId, 0);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, this.vertexVBOId[0]);
                gl.glBufferData(GL.GL_ARRAY_BUFFER, this.boundarySet.vertexBuffer.limit() * 4,
                    this.boundarySet.vertexBuffer.rewind(),
                    GL.GL_DYNAMIC_DRAW);
            }

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, this.vertexVBOId[0]);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
        }
        else
        {
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, this.boundarySet.vertexBuffer.rewind());
        }

        for (int i = 0; i < this.primTypes.size(); i++)
        {
            IntBuffer ib = this.fillIndexBuffers.get(i);
            if (dc.getGLRuntimeCapabilities().isVertexBufferObjectAvailable())
            {
                if (fillBuffers)
                {
                    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, this.vertexVBOId[i + 1]);
                    gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, ib.limit() * 4, ib.rewind(), GL.GL_DYNAMIC_DRAW);
                }
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, this.vertexVBOId[i + 1]);
                gl.glDrawElements(this.primTypes.get(i), ib.limit(), GL.GL_UNSIGNED_INT, 0);
            }
            else
            {
                gl.glDrawElements(this.primTypes.get(i), ib.limit(), GL.GL_UNSIGNED_INT, ib.rewind());
            }
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    protected void computeRotationMatrix(Globe globe)
    {
        if (this.rotation != null)
        {
            Sector s = this.getSector();
            if (s != null)
            {
                // Using the four corners of the sector to compute the rotation axis avoids any problems with dateline
                // spanning polygons.
                Vec4[] verts = s.computeCornerPoints(globe, 1);
                Vec4 normalVec = verts[2].subtract3(verts[0]).cross3(verts[3].subtract3(verts[1])).normalize3();
                this.rotationMatrix = Matrix.fromAxisAngle(Angle.fromDegrees(this.rotation), normalVec);
            }
        }
        else
        {
            this.rotationMatrix = null;
        }
    }

    /**
     * Compute enough geometry to determine the polygon's extent, reference point and eye distance.
     *
     * @param dc the current draw context.
     */
    protected void createMinimalGeometry(DrawContext dc)
    {
        this.computeReferencePoint(dc.getTerrain(), this.boundarySet);
        if (this.boundarySet.referencePoint == null)
            return;

        // Need only the outer-boundary vertices.
        this.computeBoundaryVertices(dc.getTerrain(), this.outerBoundary(), this.boundarySet.referencePoint);

        if (this.getExtent() == null || this.getAltitudeMode() != WorldWind.ABSOLUTE)
            this.boundarySet.extent = this.computeExtent(this.outerBoundary(), this.boundarySet.referencePoint);

        this.eyeDistance = this.computeEyeDistance(dc);
    }

    /**
     * Computes the minimum distance between the shape and the eye point.
     *
     * @param dc the draw context.
     *
     * @return the minimum distance from the shape to the eye point.
     */
    protected double computeEyeDistance(DrawContext dc)
    {
        double minDistance = Double.MAX_VALUE;
        Vec4 eyePoint = dc.getView().getEyePoint();

        for (Vec4 point : this.outerBoundary().vertices)
        {
            double d = point.add3(this.boundarySet.referencePoint).distanceTo3(eyePoint);
            if (d < minDistance)
                minDistance = d;
        }

        return minDistance;
    }

    protected void computeReferencePoint(Terrain terrain, BoundarySet boundarySet)
    {
        Position refPos = this.getReferencePosition();
        if (refPos == null)
            return;

        boundarySet.referencePoint = terrain.getSurfacePoint(refPos.getLatitude(), refPos.getLongitude(), 0);

        if (this.rotationMatrix != null)
            boundarySet.referencePoint = boundarySet.referencePoint.transformBy3(this.rotationMatrix);
    }

    /**
     * Computes a boundary set's full geometry.
     *
     * @param dc                the current draw context.
     * @param terrain           the terrain to use when computing the geometry.
     * @param boundarySet       the boundary set to compute the geometry for.
     * @param skipOuterBoundary true if outer boundaries vertices do not need to be calculated, otherwise false.
     */
    protected void createVisualGeometry(DrawContext dc, Terrain terrain, BoundarySet boundarySet,
        boolean skipOuterBoundary)
    {
        this.createVertices(terrain, boundarySet, skipOuterBoundary);
        this.createGeometry(dc, boundarySet);

        if (this.mustApplyLighting(dc))
            this.createNormals(boundarySet);
    }

    protected void createVertices(Terrain terrain, BoundarySet boundarySet, boolean skipOuterBoundary)
    {
        for (int i = 0; i < boundarySet.boundaries.size(); i++)
        {
            BoundaryInfo boundary = boundarySet.boundaries.get(i);

            if (i > 0 || !skipOuterBoundary)
                this.computeBoundaryVertices(terrain, boundary, boundarySet.referencePoint);
        }
    }

    /**
     * Compute the vertices associated with a specified boundary.
     *
     * @param terrain  the terrain to use when calculating vertices relative to the ground.
     * @param boundary the boundary to compute vertices for.
     * @param refPoint the reference point. Vertices are computed relative to this point, which is usually the shape's
     *                 reference point.
     */
    protected void computeBoundaryVertices(Terrain terrain, BoundaryInfo boundary, Vec4 refPoint)
    {
        int n = boundary.locations.size();
        Vec4[] boundaryVertices = new Vec4[n];

        for (int i = 0; i < n; i++)
        {
            if (this.rotationMatrix == null)
                boundaryVertices[i] = this.computePoint(terrain, boundary.locations.get(i)).subtract3(refPoint);
            else
                boundaryVertices[i] = this.computePoint(terrain, boundary.locations.get(i)).transformBy3(
                    this.rotationMatrix).subtract3(refPoint);
        }

        boundary.vertices = boundaryVertices;
    }

    /**
     * Computes a model-coordinate point from a position, applying the path's altitude mode.
     *
     * @param terrain  the terrain object to use when computing terrain-relative points.
     * @param location the location or {@link Position} to compute a point for.
     *
     * @return the model-coordinate point corresponding to the position and the path's path type.
     */
    protected Vec4 computePoint(Terrain terrain, LatLon location)
    {
        Position pos = location instanceof Position ? (Position) location : new Position(location, 0);

        if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            return terrain.getSurfacePoint(pos);

        double height = pos.getElevation() * terrain.getVerticalExaggeration();

        return terrain.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), height);
    }

    /**
     * Compute the cap geometry.
     *
     * @param dc          the current draw context.
     * @param boundarySet boundary vertices are calculated during {@link #createMinimalGeometry(DrawContext)}).
     */
    protected void createGeometry(DrawContext dc, BoundarySet boundarySet)
    {
        if (boundarySet.vertexBuffer != null && boundarySet.vertexBuffer.capacity() >= this.totalNumLocations * 3)
            boundarySet.vertexBuffer.clear();
        else
            boundarySet.vertexBuffer = BufferUtil.newFloatBuffer(this.totalNumLocations * 3);

        // Fill the vertex buffer. Simultaneously create individual buffer slices for each boundary. These are used to
        // draw the outline.
        for (BoundaryInfo boundary : boundarySet)
        {
            boundary.vertexBuffer = this.fillVertexBuffer(boundary.vertices, boundarySet.vertexBuffer.slice());
            boundarySet.vertexBuffer.position(boundarySet.vertexBuffer.position() + boundary.vertexBuffer.limit());
        }

        if (this.fillIndices == null) // need to tessellate only once
            this.createTessllationGeometry(dc, boundarySet);
    }

    protected void createNormals(BoundarySet boundarySet)
    {
        if (boundarySet.normalBuffer != null
            && boundarySet.normalBuffer.capacity() >= this.totalNumLocations * 3)
            boundarySet.normalBuffer.clear();
        else
            boundarySet.normalBuffer = BufferUtil.newFloatBuffer(boundarySet.vertexBuffer.capacity());

        for (BoundaryInfo boundary : boundarySet)
        {
            boundary.normalBuffer = this.computeBoundaryNormals(boundary, boundarySet.normalBuffer.slice());
            boundarySet.normalBuffer.position(boundarySet.normalBuffer.position() + boundary.normalBuffer.limit());
        }
    }

    /**
     * Copy a boundary's vertex coordinates to a specified vertex buffer.
     *
     * @param vertices the vertices to copy.
     * @param vBuf     the buffer to copy the vertices to. Must have enough remaining space to hold the vertices.
     *
     * @return the buffer specified as input, with its limit incremented by the number of vertices copied, and its
     *         position set to 0.
     */
    protected FloatBuffer fillVertexBuffer(Vec4[] vertices, FloatBuffer vBuf)
    {
        for (Vec4 v : vertices)
        {
            vBuf.put((float) v.x).put((float) v.y).put((float) v.z);
        }

        vBuf.flip(); // sets the limit to the position and then the position to 0.

        return vBuf;
    }

    /**
     * Compute normal vectors for a boundary's vertices.
     *
     * @param boundary the boundary to compute normals for.
     * @param nBuf     the buffer in which to place the computed normals. Must have enough remaining space to hold the
     *                 normals.
     *
     * @return the buffer specified as input, with its limit incremented by the number of vertices copied, and its
     *         position set to 0.
     */
    protected FloatBuffer computeBoundaryNormals(BoundaryInfo boundary, FloatBuffer nBuf)
    {
        int nVerts = boundary.locations.size();
        Vec4[] verts = boundary.vertices;
        double avgX, avgY, avgZ;

        // Compute normal for first point of boundary.
        Vec4 va = verts[1].subtract3(verts[0]);
        Vec4 vb = verts[nVerts - 2].subtract3(verts[0]); // nverts - 2 because last and first are same
        avgX = (va.y * vb.z) - (va.z * vb.y);
        avgY = (va.z * vb.x) - (va.x * vb.z);
        avgZ = (va.x * vb.y) - (va.y * vb.x);

        // Compute normals for interior boundary points.
        for (int i = 1; i < nVerts - 1; i++)
        {
            va = verts[i + 1].subtract3(verts[i]);
            vb = verts[i - 1].subtract3(verts[i]);
            avgX += (va.y * vb.z) - (va.z * vb.y);
            avgY += (va.z * vb.x) - (va.x * vb.z);
            avgZ += (va.x * vb.y) - (va.y * vb.x);
        }

        avgX /= nVerts - 1;
        avgY /= nVerts - 1;
        avgZ /= nVerts - 1;
        double length = Math.sqrt(avgX * avgX + avgY * avgY + avgZ * avgZ);

        for (int i = 0; i < nVerts; i++)
        {
            nBuf.put((float) (avgX / length)).put((float) (avgY / length)).put((float) (avgZ / length));
        }

        nBuf.flip();

        return nBuf;
    }

    /**
     * Returns the indices defining the vertices of each shape edge.
     *
     * @param n the number of positions in the polygon.
     *
     * @return a buffer of indices that can be passed to OpenGL to draw all the shape's edges.
     */
    protected IntBuffer getEdgeIndices(int n)
    {
        IntBuffer ib = edgeIndexBuffers.get(n);
        if (ib != null)
            return ib;

        // The edges are two-point lines connecting vertex pairs.
        ib = BufferUtil.newIntBuffer(2 * (n - 1) * 3);
        for (int i = 0; i < n - 1; i++)
        {
            ib.put(i).put(i + 1);
        }

        edgeIndexBuffers.put(n, ib);

        return ib;
    }

    /**
     * Tessellates the polygon.
     * <p/>
     * This method catches {@link OutOfMemoryError} exceptions and if the draw context is not null calls {@link
     * #handleUnsuccessfulCapCreation(BoundarySet)} when they occur.
     *
     * @param dc          the draw context.
     * @param boundarySet the boundary set to tessellate
     */
    protected void createTessllationGeometry(DrawContext dc, BoundarySet boundarySet)
    {
        // Wrap polygon tessellation in a try/catch block. We do this to catch and handle OutOfMemoryErrors caused during
        // tessellation of the polygon vertices. If the polygon cannot be tessellated, we replace the polygon's locations
        // with an empty list to prevent subsequent tessellation attempts, and to avoid rendering a misleading
        // representation by omitting the polygon.
        try
        {
            this.tessellatePolygon(boundarySet.boundaries, boundarySet.referencePoint.normalize3());
        }
        catch (OutOfMemoryError e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileTessellating", this);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);

            if (dc != null)
            {
                //noinspection ThrowableInstanceNeverThrown
                dc.addRenderingException(new WWRuntimeException(message, e));
                this.handleUnsuccessfulCapCreation(boundarySet);
            }
        }
    }

    protected void handleUnsuccessfulCapCreation(BoundarySet boundarySet)
    {
        // If creating the polygon cap was unsuccessful, we modify the polygon to avoid any additional creation
        // attempts, and free any resources that the polygon won't use. This is done to gracefully handle
        // OutOfMemoryErrors throws while tessellating the polygon cap.

        // Replace the polygon's locations with an empty list. This ensures that any rendering code won't attempt to
        // re-create the polygon's cap.
        boundarySet.boundaries.clear();

        // Reinitialize the polygon, since we've replaced its locations with an empty list.
        this.reinitialize();
    }

    /**
     * Tessellates the polygon from its vertices.
     *
     * @param boundaries the polygon boundaries.
     * @param normal     a unit normal vector for the plane containing the polygon vertices. Even though the the
     *                   vertices might not be coplanar, only one representative normal is used for tessellation.
     */
    protected void tessellatePolygon(List<BoundaryInfo> boundaries, Vec4 normal)
    {
        GLU glu = new GLU();

        GLUTessellatorSupport glts = new GLUTessellatorSupport();
        GLUTessellatorSupport.CollectIndexListsCallback cb = new GLUTessellatorSupport.CollectIndexListsCallback();

        glts.beginTessellation(glu, cb, normal);
        try
        {
            double[] coords = new double[3];

            glu.gluTessBeginPolygon(glts.getGLUtessellator(), null);

            int k = 0;
            for (BoundaryInfo boundary : boundaries)
            {
                glu.gluTessBeginContour(glts.getGLUtessellator());
                FloatBuffer vBuf = boundary.vertexBuffer;
                for (int i = 0; i < boundary.locations.size(); i++)
                {
                    coords[0] = vBuf.get(i * 3);
                    coords[1] = vBuf.get(i * 3 + 1);
                    coords[2] = vBuf.get(i * 3 + 2);

                    glu.gluTessVertex(glts.getGLUtessellator(), coords, 0, k++);
                }
                glu.gluTessEndContour(glts.getGLUtessellator());
            }

            glu.gluTessEndPolygon(glts.getGLUtessellator());
        }
        finally
        {
            // Free any heap memory used for tessellation immediately. If tessellation has consumed all available
            // heap memory, we must free memory used by tessellation immediately or subsequent operations such as
            // message logging will fail.
            glts.endTessellation(glu);
        }

        this.makeIndexLists(cb);
    }

    protected void makeIndexLists(GLUTessellatorSupport.CollectIndexListsCallback cb)
    {
        if (this.fillIndices == null || this.fillIndices.capacity() < cb.getNumIndices())
            this.fillIndices = BufferUtil.newIntBuffer(cb.getNumIndices());
        else
            this.fillIndices.clear();

        if (this.fillIndexBuffers == null || this.fillIndexBuffers.size() < cb.getPrimTypes().size())
            this.fillIndexBuffers = new ArrayList<IntBuffer>(cb.getPrimTypes().size());
        else
            this.fillIndexBuffers.clear();

        this.primTypes = cb.getPrimTypes();

        for (ArrayList<Integer> prim : cb.getPrims())
        {
            IntBuffer ib = this.fillIndices.slice();
            for (Integer i : prim)
            {
                ib.put(i);
            }
            ib.flip();
            this.fillIndexBuffers.add(ib);
            this.fillIndices.position(this.fillIndices.position() + ib.limit());
        }
    }

    protected Terrain previousIntersectionTerrain;
    protected BoundarySet previousIntersectionBoundarySet;

    protected boolean isSameAsPreviousTerrain(Terrain terrain)
    {
        if (terrain == null || terrain != this.previousIntersectionTerrain)
            return false;

        if (terrain.getVerticalExaggeration() != this.previousIntersectionTerrain.getVerticalExaggeration())
            return false;

        // TODO: More accurate equality checking for Globe, e.g., the globe's state key
        return terrain.getGlobe() == this.previousIntersectionTerrain.getGlobe();
    }

    /**
     * Compute the intersections of a specified line with this polygon. If the polygon's altitude mode is other than
     * {@link WorldWind#ABSOLUTE}, the polygon's geometry is created relative to the specified terrain rather than the
     * terrain used during rendering, which may be at lower level of detail than required for accurate intersection
     * determination.
     *
     * @param line    the line to intersect.
     * @param terrain the {@link Terrain} to use when computing the polygon's geometry.
     *
     * @return a list of intersections identifying where the line intersects the polygon, or null if the line does not
     *         intersect the polygon.
     *
     * @throws InterruptedException if the operation is interrupted.
     * @see Terrain
     */
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
    {
        Position refPos = this.getReferencePosition();
        if (refPos == null)
            return null;

        if (this.boundarySet.boundaries.size() < 1)
            return null;

        // Reuse the previously computed high-res boundary set if the terrain is the same.
        BoundarySet highResBoundarySet = this.isSameAsPreviousTerrain(terrain) ? this.previousIntersectionBoundarySet
            : null;

        if (highResBoundarySet == null)
        {
            highResBoundarySet = this.createIntersectionGeometry(terrain);
            if (highResBoundarySet == null)
                return null;

            this.previousIntersectionBoundarySet = highResBoundarySet;
            this.previousIntersectionTerrain = terrain;
        }

        if (highResBoundarySet.extent != null && highResBoundarySet.extent.intersect(line) == null)
            return null;

        final Line localLine = new Line(line.getOrigin().subtract3(highResBoundarySet.referencePoint),
            line.getDirection());
        List<Intersection> intersections = new ArrayList<Intersection>();

        this.intersect(localLine, highResBoundarySet, intersections);

        if (intersections.size() == 0)
            return null;

        for (Intersection intersection : intersections)
        {
            Vec4 pt = intersection.getIntersectionPoint().add3(highResBoundarySet.referencePoint);
            intersection.setIntersectionPoint(pt);

            // Compute intersection position relative to ground.
            Position pos = terrain.getGlobe().computePositionFromPoint(pt);
            Vec4 gp = terrain.getSurfacePoint(pos.getLatitude(), pos.getLongitude(), 0);
            intersection.setIntersectionPosition(new Position(pos, gp.distanceTo3(pt)));

            intersection.setObject(this);
        }

        return intersections;
    }

    protected BoundarySet createIntersectionGeometry(Terrain terrain)
    {
        BoundarySet boundarySet = new BoundarySet(this.boundarySet);

        this.computeReferencePoint(terrain, boundarySet);
        if (boundarySet.referencePoint == null)
            return null;

        // Compute the boundary vertices first.
        this.createVertices(terrain, boundarySet, false);
        this.createGeometry(null, boundarySet);

        boundarySet.extent = this.computeExtent(boundarySet.boundaries.get(0), boundarySet.referencePoint);

        return boundarySet;
    }

    protected void intersect(Line line, BoundarySet boundarySet, List<Intersection> intersections)
        throws InterruptedException
    {
        if (this.primTypes == null)
            return;

        for (int i = 0; i < this.primTypes.size(); i++)
        {
            IntBuffer ib = this.fillIndexBuffers.get(i);
            ib.rewind();
            List<Intersection> ti = Triangle.intersectTriangleTypes(line, boundarySet.vertexBuffer, ib,
                this.primTypes.get(i));

            if (ti != null && ti.size() > 0)
                intersections.addAll(ti);
        }
    }

    /** {@inheritDoc} */
    public String isExportFormatSupported(String format)
    {
        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(format))
            return Exportable.FORMAT_SUPPORTED;
        else
            return Exportable.FORMAT_NOT_SUPPORTED;
    }

    /**
     * Export the Polygon. The {@code output} object will receive the exported data. The type of this object depends on
     * the export format. The formats and object types supported by this class are:
     * <p/>
     * <pre>
     * Format                                         Supported output object types
     * ================================================================================
     * KML (application/vnd.google-earth.kml+xml)     java.io.Writer
     *                                                java.io.OutputStream
     *                                                javax.xml.stream.XMLStreamWriter
     * </pre>
     *
     * @param mimeType MIME type of desired export format.
     * @param output   An object that will receive the exported data. The type of this object depends on the export
     *                 format (see above).
     *
     * @throws java.io.IOException If an exception occurs writing to the output object.
     */
    public void export(String mimeType, Object output) throws IOException
    {
        if (mimeType == null)
        {
            String message = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (output == null)
        {
            String message = Logging.getMessage("nullValue.OutputBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(mimeType))
        {
            try
            {
                exportAsKML(output);
            }
            catch (XMLStreamException e)
            {
                Logging.logger().throwing(getClass().getName(), "export", e);
                throw new IOException(e);
            }
        }
        else
        {
            String message = Logging.getMessage("Export.UnsupportedFormat", mimeType);
            Logging.logger().warning(message);
            throw new UnsupportedOperationException(message);
        }
    }

    /**
     * Export the placemark to KML as a {@code <Placemark>} element. The {@code output} object will receive the data.
     * This object must be one of: java.io.Writer java.io.OutputStream javax.xml.stream.XMLStreamWriter
     *
     * @param output Object to receive the generated KML.
     *
     * @throws XMLStreamException If an exception occurs while writing the KML
     * @throws IOException        if an exception occurs while exporting the data.
     * @see #export(String, Object)
     */
    protected void exportAsKML(Object output) throws IOException, XMLStreamException
    {
        XMLStreamWriter xmlWriter = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        boolean closeWriterWhenFinished = true;

        if (output instanceof XMLStreamWriter)
        {
            xmlWriter = (XMLStreamWriter) output;
            closeWriterWhenFinished = false;
        }
        else if (output instanceof Writer)
        {
            xmlWriter = factory.createXMLStreamWriter((Writer) output);
        }
        else if (output instanceof OutputStream)
        {
            xmlWriter = factory.createXMLStreamWriter((OutputStream) output);
        }

        if (xmlWriter == null)
        {
            String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        xmlWriter.writeStartElement("Placemark");

        String property = (String) getValue(AVKey.DISPLAY_NAME);
        if (property != null)
        {
            xmlWriter.writeStartElement("name");
            xmlWriter.writeCharacters(property);
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeStartElement("visibility");
        xmlWriter.writeCharacters(KMLExportUtil.kmlBoolean(this.isVisible()));
        xmlWriter.writeEndElement();

        String shortDescription = (String) getValue(AVKey.SHORT_DESCRIPTION);
        if (shortDescription != null)
        {
            xmlWriter.writeStartElement("Snippet");
            xmlWriter.writeCharacters(shortDescription);
            xmlWriter.writeEndElement();
        }

        String description = (String) getValue(AVKey.BALLOON_TEXT);
        if (description != null)
        {
            xmlWriter.writeStartElement("description");
            xmlWriter.writeCharacters(description);
            xmlWriter.writeEndElement();
        }

        // KML does not allow separate attributes for cap and side, so just use the cap attributes.
        final ShapeAttributes normalAttributes = getAttributes();
        final ShapeAttributes highlightAttributes = getHighlightAttributes();

        // Write style map
        if (normalAttributes != null || highlightAttributes != null)
        {
            xmlWriter.writeStartElement("StyleMap");
            KMLExportUtil.exportAttributesAsKML(xmlWriter, KMLConstants.NORMAL, normalAttributes);
            KMLExportUtil.exportAttributesAsKML(xmlWriter, KMLConstants.HIGHLIGHT, highlightAttributes);
            xmlWriter.writeEndElement(); // StyleMap
        }

        // Write geometry
        xmlWriter.writeStartElement("Polygon");

        xmlWriter.writeStartElement("extrude");
        xmlWriter.writeCharacters("0");
        xmlWriter.writeEndElement();

        final String altitudeMode = KMLExportUtil.kmlAltitudeMode(getAltitudeMode());
        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters(altitudeMode);
        xmlWriter.writeEndElement();

        this.writeKMLBoundaries(xmlWriter);

        xmlWriter.writeEndElement(); // Polygon
        xmlWriter.writeEndElement(); // Placemark

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    }

    /** {@inheritDoc} */
    protected void writeKMLBoundaries(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        // Outer boundary
        Iterable<? extends LatLon> outerBoundary = this.getOuterBoundary();
        if (outerBoundary != null)
        {
            xmlWriter.writeStartElement("outerBoundaryIs");
            exportBoundaryAsLinearRing(xmlWriter, outerBoundary);
            xmlWriter.writeEndElement(); // outerBoundaryIs
        }

        // Inner boundaries
        Iterator<Polygon.BoundaryInfo> boundaryIterator = this.boundarySet.boundaries.iterator();
        if (boundaryIterator.hasNext())
            boundaryIterator.next(); // Skip outer boundary, we already dealt with it above

        while (boundaryIterator.hasNext())
        {
            BoundaryInfo boundary = boundaryIterator.next();

            xmlWriter.writeStartElement("innerBoundaryIs");
            exportBoundaryAsLinearRing(xmlWriter, boundary.locations);
            xmlWriter.writeEndElement(); // innerBoundaryIs
        }
    }

    /**
     * Writes the boundary in KML as either a list of lat, lon, altitude tuples or lat, lon tuples, depending on the
     * type originally specified.
     *
     * @param xmlWriter the XML writer.
     * @param boundary  the boundary to write.
     *
     * @throws XMLStreamException if an error occurs during writing.
     */
    protected void exportBoundaryAsLinearRing(XMLStreamWriter xmlWriter, Iterable<? extends LatLon> boundary)
        throws XMLStreamException
    {
        xmlWriter.writeStartElement("LinearRing");
        xmlWriter.writeStartElement("coordinates");
        for (LatLon location : boundary)
        {
            if (location instanceof Position)
            {
                xmlWriter.writeCharacters(String.format("%f,%f,%f ",
                    location.getLongitude().getDegrees(),
                    location.getLatitude().getDegrees(),
                    ((Position) location).getAltitude()));
            }
            else
            {
                xmlWriter.writeCharacters(String.format("%f,%f ",
                    location.getLongitude().getDegrees(),
                    location.getLatitude().getDegrees()));
            }
        }
        xmlWriter.writeEndElement(); // coordinates
        xmlWriter.writeEndElement(); // LinearRing
    }
}
