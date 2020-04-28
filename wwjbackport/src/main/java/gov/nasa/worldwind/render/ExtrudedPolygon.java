/*
Copyright (C) 2001, 2010 United States Government
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
 * A multi-sided 3D shell formed by a base polygon in latitude and longitude extruded from the terrain to either a
 * specified height or an independent height per location. The base polygon may be complex with multiple internal but
 * not intersecting contours. Extruded polygons may optionally be textured. Textures may be applied to both the faces of
 * the outer and inner boundaries or just the outer boundaries. Texture can not be applied to the cap. Standard lighting
 * is optionally applied. ExtrudedPolygon side faces and cap have independent attributes for both normal and highlighted
 * drawing.
 * <p/>
 * When specifying a single height, the height is relative to a reference location designated by one of the specified
 * polygon locations in the outer boundary. The default reference location is the first one in the polygon's outer
 * boundary. An alternative location may be specified by calling {@link #setReferenceLocation(LatLon)}. The extruded
 * polygon is capped with a plane at the specified height and tangent to the ellipsoid at the reference location. Since
 * location other than the reference position may resolve to points at elevations other than that at the reference
 * location, the distances from those points to the cap are adjusted so that the adjacent sides precisely meet the cap.
 * When specifying polygons using a single height, only the latitude and longitudes of polygon boundary positions must
 * be specified.
 * <p/>
 * Independent per-location heights may be specified via the <i>altitude</i> field of {@link Position}s defining the
 * polygon's inner and outer boundaries. Depending on the specified altitude mode, the position altitudes may be
 * interpreted as altitudes relative to mean sea level or altitudes above the ground at the associated latitude and
 * longitude locations.
 * <p/>
 * <p/>
 * Extruded polygons are safe to share among World Windows. They should not be shared among layers in the same World
 * Window.
 *
 * @author tag
 * @version $Id: ExtrudedPolygon.java 14179 2010-12-03 00:35:40Z tgaskins $
 */
public class ExtrudedPolygon extends AVListImpl
    implements OrderedRenderable, GeographicExtent, Disposable, Highlightable, Exportable // TODO: Movable, ExtentHolder
{
    // TODO: Implement rotation
    /** The default interior color. */
    protected static final Material DEFAULT_INTERIOR_MATERIAL = Material.LIGHT_GRAY;
    /** The default outline color. */
    protected static final Material DEFAULT_OUTLINE_MATERIAL = Material.DARK_GRAY;
    /** The default highlight color. */
    protected static final Material DEFAULT_HIGHLIGHT_MATERIAL = Material.WHITE;
    /** The default geometry regeneration interval. */
    protected static final long DEFAULT_GEOMETRY_GENERATION_INTERVAL = 2000;
    /** The default interior color for sides. */
    protected static final Material DEFAULT_SIDES_INTERIOR_MATERIAL = Material.LIGHT_GRAY;
    /** The default altitude mode. */
    protected static final int DEFAULT_ALTITUDE_MODE = WorldWind.CONSTANT;

    /** The attributes used if attributes are not specified. */
    protected static final ShapeAttributes defaultAttributes;
    protected static final ShapeAttributes defaultSideAttributes;

    static
    {
        defaultAttributes = new BasicShapeAttributes();
        defaultAttributes.setInteriorMaterial(DEFAULT_INTERIOR_MATERIAL);
        defaultAttributes.setOutlineMaterial(DEFAULT_OUTLINE_MATERIAL);
        defaultSideAttributes = new BasicShapeAttributes();

        defaultSideAttributes.setInteriorMaterial(DEFAULT_SIDES_INTERIOR_MATERIAL);
        defaultSideAttributes.setOutlineMaterial(DEFAULT_OUTLINE_MATERIAL);
    }

    /** Holds information for each contour of the polygon. The vertex values are updated every frame. */
    protected static class ExtrudedBoundaryInfo
    {
        protected List<? extends LatLon> locations; // the defining locations or positions of the boundary
        protected int faceCount;

        protected Vec4[] capVertices; // computed vertices TODO: eliminate need for this; use the vertex buffer instead
        protected Vec4[] baseVertices; // computed terrain vertices

        protected IntBuffer capFillIndices; // indices identifying the cap vertices in the vertex buffer
        protected IntBuffer capIndices; // indices identifying cap edges in the vertex buffer
        protected FloatBuffer capVertexBuffer; // vertices passed to OpenGL
        protected FloatBuffer capNormalBuffer; // vertex normals if lighting is applied
        protected FloatBuffer capTextureCoordsBuffer; // cap texture coords if texturing the cap

        protected IntBuffer sideIndices; // indices identifying side faces in the vertex buffer
        protected IntBuffer sideEdgeIndices; // indices identifying edges in the vertex buffer
        protected FloatBuffer sideVertexBuffer; // vertices passed to OpenGL
        protected FloatBuffer sideNormalBuffer; // vertex normals if lighting is applied
        protected FloatBuffer sideTextureCoords; // texture coords if texturing
        protected List<WWTexture> sideTextures;

        public ExtrudedBoundaryInfo()
        {
            this.locations = new ArrayList<LatLon>(); // so users don't have to check for null
        }
    }

    protected class BoundarySet implements Iterable<ExtrudedBoundaryInfo>
    {
        protected List<ExtrudedBoundaryInfo> boundaries = new ArrayList<ExtrudedBoundaryInfo>();
        protected Vec4 referencePoint; // the Cartesian coordinates of the reference point
        protected Extent extent; // the shape's extent
        protected FloatBuffer capVertexBuffer;
        protected FloatBuffer capNormalBuffer;
        protected FloatBuffer capTextureCoordsBuffer; // texture coords if texturing
        protected FloatBuffer sideVertexBuffer;
        protected FloatBuffer sideNormalBuffer;
        protected FloatBuffer sideTextureCoordsBuffer; // texture coords if texturing

        protected BoundarySet()
        {
        }

        protected BoundarySet(BoundarySet bSet)
        {
            for (ExtrudedBoundaryInfo boundary : bSet)
            {
                ExtrudedBoundaryInfo newBoundary = new ExtrudedBoundaryInfo();

                newBoundary.locations = boundary.locations;
                newBoundary.faceCount = boundary.faceCount;

                this.boundaries.add(newBoundary);
            }
        }

        public Iterator<ExtrudedBoundaryInfo> iterator()
        {
            return this.boundaries.iterator();
        }
    }

    // This static hash map hold the vertex indices that define the shape geometry. Their contents depend only on the
    // number of locations in the source polygon, so they can be reused by all shapes with the same location count.
    protected static HashMap<Integer, IntBuffer> capEdgeIndexBuffers = new HashMap<Integer, IntBuffer>();
    protected static HashMap<Integer, IntBuffer> sideFillIndexBuffers = new HashMap<Integer, IntBuffer>();
    protected static HashMap<Integer, IntBuffer> sideEdgeIndexBuffers = new HashMap<Integer, IntBuffer>();

    protected double height = 1;
    protected Position referencePosition; // the location/position to use as the shape's reference point
    protected int totalNumLocations; // number of specified locations in the entire polygon
    protected int totalFaceCount;
    protected Sector sector; // the shape's bounding sector
    protected double eyeDistance;

    protected ShapeAttributes capAttributes;
    protected ShapeAttributes capHighlightAttributes;
    protected ShapeAttributes activeCapAttributes = new BasicShapeAttributes();

    protected ShapeAttributes sideAttributes;
    protected ShapeAttributes sideHighlightAttributes;
    protected ShapeAttributes activeSideAttributes = new BasicShapeAttributes();

    protected WWTexture capTexture; // TODO: implement cap texturing
    protected Object capImageSource; // image source for the optional texture

    protected boolean enableCap = true;
    protected boolean enableSides = true;

    protected BoundarySet boundarySet = new BoundarySet();

    protected int altitudeMode = DEFAULT_ALTITUDE_MODE;
    protected boolean visible = true;
    protected boolean highlighted = false;
    protected boolean enableDepthOffset = false;
    protected boolean enableBatchRendering = true;
    protected boolean enableBatchPicking = true;
    protected int outlinePickWidth = 10;
    protected long geometryRegenerationInterval = DEFAULT_GEOMETRY_GENERATION_INTERVAL;

    // These values are determined every frame, thus they are safe for multi-window usage.
    protected long frameID; // the ID of the most recent rendering frame
    protected long visGeomRegenFrame = -1;
    protected double previousExaggeration = -1;
    protected Layer pickLayer; // shape's layer when ordered renderable was created
    protected OGLStackHandler BEogsh = new OGLStackHandler();

    // Tessellation fields
    protected IntBuffer capFillIndices;
    protected ArrayList<IntBuffer> capFillIndexBuffers;
    protected ArrayList<Integer> primTypes;

    protected PickSupport pickSupport = new PickSupport();

    /** Polygons are drawn as {@link gov.nasa.worldwind.render.OutlinedShape}s. */
    protected OutlinedShape outlineShapeRenderer = new OutlinedShape()
    {
        public boolean isDrawOutline(DrawContext dc, Object shape)
        {
            return ((ExtrudedPolygon) shape).mustDrawOutline();
        }

        public boolean isDrawInterior(DrawContext dc, Object shape)
        {
            return ((ExtrudedPolygon) shape).mustDrawInterior();
        }

        public boolean isEnableDepthOffset(DrawContext dc, Object shape)
        {
            return ((ExtrudedPolygon) shape).isEnableDepthOffset();
        }

        public void drawOutline(DrawContext dc, Object shape)
        {
            ((ExtrudedPolygon) shape).drawOutline(dc);
        }

        public void drawInterior(DrawContext dc, Object shape)
        {
            ((ExtrudedPolygon) shape).drawInterior(dc);
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

    /** Construct an extruded polygon with an empty position list and a default height of 1 meter. */
    public ExtrudedPolygon()
    {
    }

    /**
     * Construct an extruded polygon for a specified list of locations and a height.
     *
     * @param corners the list of locations defining the polygon.
     * @param height  the shape height, in meters. May be null, in which case a height of 1 is used when the altitude
     *                mode is {@link WorldWind#CONSTANT}, the default altitude mode.
     *
     * @throws IllegalArgumentException if the location list is null or the height is less than or equal to zero.
     */
    public ExtrudedPolygon(Iterable<? extends LatLon> corners, Double height)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height != null && height <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "height <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(corners, height);
    }

    /**
     * Construct an extruded polygon for a specified list of locations and a height, and apply specified textures to its
     * outer faces.
     *
     * @param corners      the list of locations defining the polygon.
     * @param height       the shape height, in meters. May be null, in which case a height of 1 is used when the
     *                     altitude mode is {@link WorldWind#CONSTANT}.
     * @param imageSources textures to apply to the polygon's outer faces. One texture for each face must be included.
     *                     May also be null.
     *
     * @throws IllegalArgumentException if the location list is null or the height is less than or equal to zero.
     */
    public ExtrudedPolygon(Iterable<? extends LatLon> corners, double height, Iterable<?> imageSources)
    {
        this(corners, height);

        if (imageSources != null)
            this.addImageSourcesToBoundary(this.outerBoundary(), imageSources);
    }

    /**
     * Construct an extruded polygon for a specified list of positions.
     *
     * @param corners the list of positions -- latitude longitude and altitude -- defining the polygon. The altitude
     *                mode determines whether the positions are considered relative to mean sea level (they are
     *                "absolute") or the ground elevation at the associated latitude and longitude.
     *
     * @throws IllegalArgumentException if the position list is null.
     */
    public ExtrudedPolygon(Iterable<? extends Position> corners)
    {
        this(corners, 1d); // the height field is ignored when positions are specified, so any value will do
    }

    /**
     * Construct an extruded polygon for a specified list of positions.
     *
     * @param corners the list of positions -- latitude longitude and altitude -- defining the polygon. The altitude
     *                mode determines whether the positions are considered relative to mean sea level (they are
     *                "absolute") or the ground elevation at the associated latitude and longitude.
     *
     * @throws IllegalArgumentException if the position list is null.
     */
    public ExtrudedPolygon(Position.PositionList corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(corners.list, null);
    }

    /**
     * Construct an extruded polygon for a specified list of positions, and apply specified textures to its outer
     * faces.
     *
     * @param corners      the list of positions -- latitude longitude and altitude -- defining the polygon. The
     *                     altitude mode determines whether the positions are considered relative to mean sea level
     *                     (they are "absolute") or the ground elevation at the associated latitude and longitude.
     * @param imageSources textures to apply to the polygon's outer faces. One texture for each face must be included.
     *                     May also be null.
     *
     * @throws IllegalArgumentException if the position list is null.
     */
    public ExtrudedPolygon(Iterable<? extends Position> corners, Iterable<?> imageSources)
    {
        this(corners);

        if (imageSources != null)
            this.addImageSourcesToBoundary(this.outerBoundary(), imageSources);
    }

    /** Void any computed data. Called when a factor affecting the computed data is changed. */
    protected void reinitialize()
    {
        for (ExtrudedBoundaryInfo boundary : this.boundarySet.boundaries)
        {
            if (!WWMath.computeWindingOrderOfLocations(boundary.locations).equals(AVKey.COUNTER_CLOCKWISE))
                Collections.reverse(boundary.locations);

            boundary.capIndices = this.getCapIndices(boundary.locations.size());
            boundary.sideIndices = this.getSideIndices(boundary.locations.size());
            boundary.sideEdgeIndices = this.getSideEdgeIndices(boundary.locations.size());
        }

        this.totalNumLocations = 0;
        this.totalFaceCount = 0;
        this.boundarySet.extent = null;
        this.sector = null;
        this.previousIntersectionBoundarySet = null;
        this.previousIntersectionTerrain = null;
    }

    /** {@inheritDoc} */
    public void dispose()
    {
        // Remove references to textures and NIO buffers. Not necessary, but prevents dangling references to large
        // chunks of memory.
        for (ExtrudedBoundaryInfo boundary : this.boundarySet)
        {
            if (boundary.sideTextures != null)
                boundary.sideTextures.clear();
            boundary.sideTextures = null;
        }

        // Remove references to NIO buffers
        this.capFillIndices = null;
        if (this.capFillIndexBuffers != null)
            this.capFillIndexBuffers.clear();

        this.boundarySet.boundaries.clear();
    }

    /**
     * Returns a reference to the outer boundary of the polygon.
     *
     * @return the polygon's outer boundary, or null if there is no outer boundary has no locations.
     */
    protected ExtrudedBoundaryInfo outerBoundary()
    {
        return this.boundarySet.boundaries.size() > 0 ? this.boundarySet.boundaries.get(0) : null;
    }

    /**
     * Specifies the latitude and longitude and optional altitude of the locations defining the polygon. To specify
     * altitudes, pass {@link Position}s rather than {@link LatLon}s.
     *
     * @param corners the polygon locations.
     * @param height  the shape height, in meters.
     *
     * @throws IllegalArgumentException if the location list is null or contains fewer than three locations.
     */
    public void setOuterBoundary(Iterable<? extends LatLon> corners, Double height)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ExtrudedBoundaryInfo outerBoundary = new ExtrudedBoundaryInfo();
        if (this.boundarySet.boundaries.size() > 0)
            this.boundarySet.boundaries.set(0, outerBoundary);
        else
            this.boundarySet.boundaries.add(outerBoundary);

        if (height != null)
            this.height = height;

        ArrayList<LatLon> list = new ArrayList<LatLon>();
        for (LatLon corner : corners)
        {
            if (corner != null)
                list.add(corner);
        }

        if (list.size() < 3)
        {
            String message = Logging.getMessage("nullValue.LocationInListIsNull");
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
     * Specifies the latitude, longitude and optional altitude of the locations defining the polygon, and specified
     * textures to apply the the polygon's outer faces. To specify altitudes, pass {@link Position}s rather than {@link
     * LatLon}s.
     *
     * @param corners      the polygon locations.
     * @param height       the shape height, in meters.
     * @param imageSources textures to apply to the outer faces. One texture must be specified for each face. May be
     *                     null.
     *
     * @throws IllegalArgumentException if the position list is null.
     */
    public void setOuterBoundary(Iterable<? extends LatLon> corners, Double height, Iterable<?> imageSources)
    {
        this.setOuterBoundary(corners, height);

        if (imageSources != null)
            this.addImageSourcesToBoundary(this.outerBoundary(), imageSources);
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
     * Add an inner boundary to the polygon. A duplicate of the first location is appended to the boundary if the
     * boundary's last location is not identical to the first.
     *
     * @param corners the polygon coordinates.
     *
     * @throws IllegalArgumentException if the location list is null or contains fewer than three locations.
     */
    public void addInnerBoundary(Iterable<? extends LatLon> corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.LocationInListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ExtrudedBoundaryInfo boundary = new ExtrudedBoundaryInfo();
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
     * Add an inner boundary to the polygon and specify textures to apply to each of its faces. Specify {@link LatLon}s
     * to use the polygon's single height, or {@link Position}s, to include individual altitudes.
     *
     * @param corners      the polygon locations.
     * @param imageSources textures to apply to the boundary's faces. One texture must be specified for each face. May
     *                     be null.
     *
     * @throws IllegalArgumentException if the location list is null.
     */
    public void addInnerBoundary(Iterable<? extends LatLon> corners, Iterable<?> imageSources)
    {
        this.addInnerBoundary(corners);

        if (imageSources != null)
            this.addImageSourcesToBoundary(this.boundarySet.boundaries.get(this.boundarySet.boundaries.size() - 1),
                imageSources);
    }

    protected void addImageSourcesToBoundary(ExtrudedBoundaryInfo boundary, Iterable<?> imageSources)
    {
        boundary.sideTextures = new ArrayList<WWTexture>(boundary.locations.size());

        for (Object source : imageSources)
        {
            if (source != null)
                boundary.sideTextures.add(new BasicWWTexture(source, true));
            else
                boundary.sideTextures.add(null);
        }
    }

    /**
     * Returns the texture image source.
     *
     * @return the texture image source, or null if no source has been specified.
     */
    public Object getCapImageSource()
    {
        return this.capTexture;
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
    public void setCapTextureImageSource(Object imageSource, float[] texCoords, int texCoordCount)
    {
        if (imageSource == null)
        {
            this.capTexture = null;
            this.boundarySet.capTextureCoordsBuffer = null;
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

        this.capImageSource = imageSource;
        this.capTexture = null; // New image source, need to reload texture

        // Determine whether the tex-coord list needs to be closed.
        boolean closeIt = texCoords[0] != texCoords[texCoordCount - 2] || texCoords[1] != texCoords[texCoordCount - 1];

        this.boundarySet.capTextureCoordsBuffer = BufferUtil.newFloatBuffer(2 * (texCoordCount + (closeIt ? 1 : 0)));
        for (int i = 0; i < 2 * texCoordCount; i++)
        {
            this.boundarySet.capTextureCoordsBuffer.put(texCoords[i]);
        }

        if (closeIt)
        {
            this.boundarySet.capTextureCoordsBuffer.put(this.boundarySet.capTextureCoordsBuffer.get(0));
            this.boundarySet.capTextureCoordsBuffer.put(this.boundarySet.capTextureCoordsBuffer.get(1));
        }
    }

    /**
     * Returns the texture image source.
     *
     * @return the texture image source, or null if no source has been specified.
     */
    public Object getTextureImageSource()
    {
        return this.capImageSource;
    }

    /**
     * Returns the texture coordinates for the polygon.
     *
     * @return the texture coordinates, or null if no texture coordinates have been specified.
     */
    public float[] getTextureCoords()
    {
        if (this.boundarySet.capTextureCoordsBuffer == null)
            return null;

        float[] retCoords = new float[this.boundarySet.capTextureCoordsBuffer.limit()];
        this.boundarySet.capTextureCoordsBuffer.get(retCoords, 0, retCoords.length);

        return retCoords;
    }

    /**
     * Create and initialize the texture from the image source. If the image is not in memory this method will request
     * that it be loaded and return null.
     *
     * @return The texture, or null if the texture is not yet available.
     */
    protected WWTexture initializeCapTexture()
    {
        Object imageSource = this.getTextureImageSource();
        if (imageSource instanceof String || imageSource instanceof URL)
        {
            URL imageURL = WorldWind.getDataFileStore().requestFile(imageSource.toString());
            if (imageURL != null)
            {
                this.capTexture = new BasicWWTexture(imageURL, true);
                return this.capTexture;
            }
            // Else wait for the retriever to retrieve the image before creating the texture
        }
        else if (imageSource != null)
        {
            this.capTexture = new BasicWWTexture(imageSource, true);
            return this.capTexture;
        }

        return null;
    }

    /**
     * Get the texture applied to the extruded polygon's cap. The texture is loaded on a background thread. This method
     * will return null until the texture has been loaded.
     *
     * @return The texture or null if there is no texture, or if texture is not yet available.
     */
    protected WWTexture getCapTexture()
    {
        if (this.capTexture != null)
            return this.capTexture;
        else
            return this.initializeCapTexture();
    }

    /**
     * Counts the total number of positions in the polygon, including all positions in all boundaries.
     *
     * @return the number of positions in the polygon.
     */
    protected int countLocations()
    {
        int count = 0;

        for (ExtrudedBoundaryInfo boundary : this.boundarySet)
        {
            count += boundary.locations.size();
        }

        this.totalFaceCount = count - this.boundarySet.boundaries.size();

        return count;
    }

    /**
     * Returns the specified shape height.
     *
     * @return the shape height originally specified, in meters.
     */
    public double getHeight()
    {
        return height;
    }

    /**
     * Specifies the shape height.
     *
     * @param height the shape height, in meters.
     *
     * @throws IllegalArgumentException if the height is less than or equal to zero.
     */
    public void setHeight(double height)
    {
        if (height <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "height <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.height = height;
        this.reinitialize();
    }

    /**
     * Indicates whether the cap should be drawn.
     *
     * @return true to draw the cap, otherwise false.
     */
    public boolean isEnableCap()
    {
        return enableCap;
    }

    /**
     * Specifies whether the cap should be drawn.
     *
     * @param enableCap true to draw the cap, otherwise false.
     */
    public void setEnableCap(boolean enableCap)
    {
        this.enableCap = enableCap;
    }

    /**
     * Inicates whether the sides should be drawn.
     *
     * @return true to draw the sides, othewise false.
     */
    public boolean isEnableSides()
    {
        return enableSides;
    }

    /**
     * Specifies whether to draw the sides.
     *
     * @param enableSides true to draw the sides, otherwise false.
     */
    public void setEnableSides(boolean enableSides)
    {
        this.enableSides = enableSides;
    }

    /**
     * Indicates whether the polygon should be drawn.
     *
     * @return true if the polygon is drawn, otherwise false.
     */
    public boolean isVisible()
    {
        return visible;
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
        return enableDepthOffset;
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
        return highlighted;
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
     * Specifies the position to use as a reference position for computed geometry. This value should typically be left
     * to the default value of the first position in the polygon's outer boundary.
     *
     * @param referencePosition the reference position. May be null, in which case the first position of the polygon's
     *                          outer boundary is the reference position.
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
        return outlinePickWidth;
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
     * Returns the attributes applied to the extruded polygon's cap when it's not highlighted.
     *
     * @return the polygon's cap attributes.
     */
    public ShapeAttributes getAttributes()
    {
        return this.capAttributes;
    }

    /**
     * Specifies the attributes applied to the extruded polygon's cap when it's not highlighted.
     *
     * @param attributes the polygon's cap attributes.
     */
    public void setAttributes(ShapeAttributes attributes)
    {
        if (attributes == null)
        {
            String message = "nullValue.AttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.capAttributes = attributes;
    }

    /**
     * Returns the attributes applied to the polygon's side faces.
     *
     * @return the polygon's side atributes.
     */
    public ShapeAttributes getSideAttributes()
    {
        return this.sideAttributes;
    }

    /**
     * Specifies the attributes applied to the polygon's side faces.
     *
     * @param attributes the polygon's side attributes.
     */
    public void setSideAttributes(ShapeAttributes attributes)
    {
        if (attributes == null)
        {
            String message = "nullValue.AttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sideAttributes = attributes;
    }

    /**
     * Returns the attributes applied to the extruded polygon's cap when it is highlighted.
     *
     * @return the polygon's highlight attributes.
     */
    public ShapeAttributes getHighlightAttributes()
    {
        return this.capHighlightAttributes;
    }

    /**
     * Specifies the attributes applied to the extruded polygon's cap when it is highlighted.
     *
     * @param attributes the polygon's highlight attributes. May be null, in which case default attributes are used for
     *                   highlighting.
     *
     * @throws IllegalArgumentException if attributes is null.
     */
    public void setHighlightAttributes(ShapeAttributes attributes)
    {
        this.capHighlightAttributes = attributes;
    }
//
//    /**
//     * Each time the extruded polygon's cap is rendered the appropriate attributes for the current mode are determined.
//     * This method returns the resolved attributes.
//     *
//     * @return the polygon's currently active attributes.
//     */
//    protected ShapeAttributes getActiveAttributes()
//    {
//        return this.activeCapAttributes;
//    }

    /**
     * Indicates whether batch rendering is enabled.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchRendering(boolean).
     */
    public boolean isEnableBatchRendering()
    {
        return enableBatchRendering;
    }

    /**
     * Specifies whether adjacent extruded polygons in the ordered renderable list may be rendered together if they are
     * contained in the same layer. This increases performance and there is seldom a reason to disable it.
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
        return enableBatchPicking;
    }

    /**
     * Specifies whether adjacent extruded polygons in the ordered renderable list may be pick-tested together if they
     * are contained in the same layer. This increases performance but allows only the top-most of the polygons in the
     * shared layer to be reported in a {@link gov.nasa.worldwind.event.SelectEvent} even if several of the polygons in
     * that layer are at the pick position.
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
     * Indicates the maximum length of time between geometry regenerations. See {@link
     * #setGeometryRegenerationInterval(long)} for the regeneration-interval's description.
     *
     * @return the geometry regeneration interval, in milliseconds.
     *
     * @see #setGeometryRegenerationInterval(long)
     */
    public long getGeometryRegenerationInterval()
    {
        return this.geometryRegenerationInterval;
    }

    /**
     * Specifies the maximum length of time between geometry regenerations. The geometry is regenerated when the
     * altitude mode is {@link WorldWind#CONSTANT} or {@link WorldWind#RELATIVE_TO_GROUND} in order to capture changes
     * to the terrain. (The terrain changes when its resolution changes or when new elevation data is returned from a
     * server.) Decreasing this value causes the geometry to more quickly track terrain changes, but at the cost of
     * performance. Increasing this value often does not have much effect because there are limiting factors other than
     * geometry regeneration.
     *
     * @param geometryRegenerationInterval the geometry regeneration interval, in milliseconds.
     */
    public void setGeometryRegenerationInterval(long geometryRegenerationInterval)
    {
        this.geometryRegenerationInterval = geometryRegenerationInterval;
    }

    /**
     * Returns the attributes applied to the polygon's cap.
     *
     * @return the polygon's cap attributes.
     */
    public ShapeAttributes getCapAttributes()
    {
        return this.getAttributes();
    }

    /**
     * Specifies the attributes applied to the polygon's cap.
     *
     * @param attributes the polygon's cap attributes.
     *
     * @throws IllegalArgumentException if attributes is null.
     */
    public void setCapAttributes(ShapeAttributes attributes)
    {
        this.setAttributes(attributes);
    }

    /**
     * Returns the highlight attributes applied to the polygon's side faces.
     *
     * @return the polygon's side highlight atributes.
     */
    public ShapeAttributes getSideHighlightAttributes()
    {
        return sideHighlightAttributes;
    }

    /**
     * Specifies the highlight attributes applied to the polygon's side faces.
     *
     * @param attributes the polygon's side highlight atributes.
     *
     * @throws IllegalArgumentException if attributes is null.
     */
    public void setSideHighlightAttributes(ShapeAttributes attributes)
    {
        if (attributes == null)
        {
            String message = "nullValue.AttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sideHighlightAttributes = attributes;
    }

    /**
     * Returns the highlight attributes applied to the polygon's cap.
     *
     * @return the polygon's cap highlight atributes.
     */
    public ShapeAttributes getCapHighlightAttributes()
    {
        return this.getHighlightAttributes();
    }

    /**
     * Specifies the highlight attributes applied to the polygon's cap.
     *
     * @param attributes the polygon's cap highlight atributes.
     *
     * @throws IllegalArgumentException if attributes is null.
     */
    public void setCapHighlightAttributes(ShapeAttributes attributes)
    {
        this.setHighlightAttributes(attributes);
    }

    /**
     * Each time the polygon is rendered the appropriate attributes for the current mode are determined. This method
     * returns the resolved attributes.
     *
     * @return the currently active attributes for the polygon's side faces.
     */
    protected ShapeAttributes getActiveSideAttributes()
    {
        return this.activeSideAttributes;
    }

    /**
     * Each time the polygon is rendered the appropriate attributes for the current mode are determined. This method
     * returns the resolved attributes.
     *
     * @return the currently active attributes for the polygon's cap.
     */
    protected ShapeAttributes getActiveCapAttributes()
    {
        return this.activeCapAttributes;
    }

    /**
     * Returns the polygon's altitude mode, one of {@link WorldWind#RELATIVE_TO_GROUND}, {@link WorldWind#ABSOLUTE}, or
     * {@link WorldWind#CONSTANT}. The altitude mode [@link WorldWind#CLAMP_TO_GROUND} is not supported. The default
     * altitude mode is {@link WorldWind#CONSTANT}.
     *
     * @return the polygon's altitude mode.
     */
    public int getAltitudeMode()
    {
        return this.altitudeMode;
    }

    /**
     * Specifies the polygon's altitude mode, one of {@link WorldWind#RELATIVE_TO_GROUND}, {@link WorldWind#ABSOLUTE},
     * or {@link WorldWind#CONSTANT}. The altitude mode [@link WorldWind#CLAMP_TO_GROUND} is not supported and {@link
     * WorldWind#ABSOLUTE} is used insted if {@link Position}s have been specified as the polygon's outer boundary.}
     * <p/>
     * The altitude mode is ignored if {@link LatLon}s are specified as the polygon's outer boundary.
     *
     * @param altitudeMode the polygon's altitude mode.
     */
    public void setAltitudeMode(int altitudeMode)
    {
        this.altitudeMode = altitudeMode;
    }

    /** {@inheritDoc} */
    public Sector getSector()
    {
        if (this.sector == null && this.getOuterBoundary() != null)
            this.sector = Sector.boundingSector(this.getOuterBoundary());

        return this.sector;
    }

    /**
     * Indicates the position to use as a reference position for computed geometry.
     *
     * @return the reference location, or null if no reference location has been specified.
     */
    public LatLon getReferenceLocation()
    {
        return this.getReferencePosition();
    }

    /**
     * Specifies the location to use as a reference position for computed geometry. This value should typically left to
     * the default value of the first location in the extruded polygon's outer boundary.
     *
     * @param referenceLocation the reference location. May be null, in which case the first location of the outer
     *                          boundary is the reference position. The altitude of the reference position is set to
     *                          0.s
     */
    public void setReferenceLocation(LatLon referenceLocation)
    {
        if (referenceLocation == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.referencePosition = new Position(referenceLocation, 0);
    }

    /**
     * Returns the extruded polygon's side-texture image sources.
     *
     * @return a collection of lists each identifying the image sources for the associated outer or inner polygon
     *         boundary.
     */
    public List<List<Object>> getImageSources()
    {
        boolean hasTextures = false;
        for (ExtrudedBoundaryInfo boundary : this.boundarySet.boundaries)
        {
            if (boundary.sideTextures != null)
                hasTextures = true;
        }

        if (!hasTextures)
            return null;

        List<List<Object>> imageSources = new ArrayList<List<Object>>(this.boundarySet.boundaries.size());

        for (ExtrudedBoundaryInfo boundary : this.boundarySet.boundaries)
        {
            if (boundary.sideTextures == null)
            {
                imageSources.add(null);
            }
            else
            {
                ArrayList<Object> images = new ArrayList<Object>(boundary.sideTextures.size());
                imageSources.add(images);

                for (WWTexture image : boundary.sideTextures)
                {
                    images.add(image.getImageSource());
                }
            }
        }

        return imageSources;
    }

    /**
     * Indicates whether side textures have been specified for the extruded polygon.
     *
     * @return true if side textures have been specified, otherwise false.
     */
    public boolean hasSideTextures()
    {
        for (ExtrudedBoundaryInfo boundary : this.boundarySet.boundaries)
        {
            if (boundary.sideTextures != null && boundary.sideTextures.size() == boundary.faceCount)
                return true;
        }

        return false;
    }

    /**
     * Returns the texture for a specified face of the outer boundary.
     *
     * @param side the zero-origin position of the desired face.
     *
     * @return the texture specified for the indicated face, or null if no outer boundary textures are specified.
     */
    protected WWTexture getTexture(int side)
    {
        ExtrudedBoundaryInfo ob = this.outerBoundary();

        if (ob == null)
            return null;

        if (ob.sideTextures == null || ob.sideTextures.size() == 0)
            return null;

        return ob.sideTextures.size() > side ? ob.sideTextures.get(side) : null;
    }

    protected boolean mustApplySideTextures(DrawContext dc)
    {
        return !dc.isPickingMode() && this.hasSideTextures();
    }

    /**
     * Indicates whether the interior of either the sides or cap must be drawn.
     *
     * @return true if an interior must be drawn, otherwise false.
     */
    protected boolean mustDrawInterior()
    {
        return this.getActiveCapAttributes().isDrawInterior() || this.getActiveSideAttributes().isDrawInterior();
    }

    /**
     * Indicates whether the polygon's outline should be drawn.
     *
     * @return true if the outline should be drawn, otherwise false.
     */
    protected boolean mustDrawOutline()
    {
        return this.getActiveCapAttributes().isDrawOutline() || this.getActiveSideAttributes().isDrawOutline();
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
        return dc != null && !dc.isPickingMode()
            && (this.getActiveCapAttributes().isEnableLighting() || this.getActiveSideAttributes().isEnableLighting());
    }

    @SuppressWarnings({"UnusedDeclaration", "SimplifiableIfStatement"})
    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        if (this.boundarySet.capVertexBuffer == null || this.boundarySet.sideVertexBuffer == null
            || dc.getVerticalExaggeration() != this.previousExaggeration)
            return true;

        return this.getAltitudeMode() != WorldWind.ABSOLUTE
            && this.frameID - this.visGeomRegenFrame > this.getGeometryRegenerationInterval();
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

    protected Extent computeExtent(ExtrudedBoundaryInfo boundary, Vec4 refPoint)
    {
        if (boundary == null)
            return null;

        Vec4[] topVertices = boundary.capVertices;
        Vec4[] botVertices = boundary.baseVertices;
        ArrayList<Vec4> allVertices = new ArrayList<Vec4>(2 * topVertices.length);

        allVertices.addAll(Arrays.asList(topVertices));
        allVertices.addAll(Arrays.asList(botVertices));

        Box boundingBox = Box.computeBoundingBox(allVertices);

        // The bounding box is computed relative to the polygon's reference point, so it needs to be translated to
        // model coordinates in order to indicate its model-coordinate extent.
        return boundingBox != null ? boundingBox.translate(refPoint) : null;
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
            if (this.getCapHighlightAttributes() != null)
                this.activeCapAttributes.copy(this.getCapHighlightAttributes());
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // to cause highlighting.
                if (this.getCapAttributes() != null)
                    this.activeCapAttributes.copy(this.getCapAttributes());
                else
                    this.activeCapAttributes.copy(defaultAttributes);

                this.activeCapAttributes.setOutlineMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeCapAttributes.setInteriorMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
            }

            if (this.getSideHighlightAttributes() != null)
                this.activeSideAttributes.copy(this.getSideHighlightAttributes());
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // to cause highlighting.
                if (this.getSideAttributes() != null)
                    this.activeSideAttributes.copy(this.getSideAttributes());
                else
                    this.activeSideAttributes.copy(defaultSideAttributes);

                this.activeSideAttributes.setOutlineMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeSideAttributes.setInteriorMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
            }
        }
        else
        {
            if (this.getCapAttributes() != null)
                this.activeCapAttributes.copy(this.getCapAttributes());
            else
                this.activeCapAttributes.copy(defaultAttributes);

            if (this.getSideAttributes() != null)
                this.activeSideAttributes.copy(this.getSideAttributes());
            else
                this.activeSideAttributes.copy(defaultSideAttributes);
        }
    }

    public void drawOutline(DrawContext dc)
    {
        if (this.isEnableSides() && getActiveSideAttributes().isDrawOutline())
            this.drawSideEdges(dc);

        if (this.isEnableCap() && getActiveCapAttributes().isDrawOutline())
            this.drawCapEdges(dc);
    }

    public void drawInterior(DrawContext dc)
    {
        if (this.isEnableSides() && getActiveSideAttributes().isDrawInterior())
            this.drawSideInteriors(dc);

        if (this.isEnableCap() && getActiveCapAttributes().isDrawInterior())
            this.drawCapInterior(dc);
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
                this.totalNumLocations = this.countLocations();
            if (this.totalNumLocations < 3)
                return;

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
     * Draws this ordered renderable. If batch rendering is enabled, draws all subsequently adjacent ExtrudedPolygon
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
                ExtrudedPolygon p = (ExtrudedPolygon) nextItem;
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
                ExtrudedPolygon p = (ExtrudedPolygon) nextItem;
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
     * Draws the shape's filled interior. Assumes the vertex buffer has already been set in the OpenGL context.
     * <p/>
     * This base implementation draws the interior of the basic polygon. Subclasses should override it to draw their
     * interior or an alternate interior of the basic polygon.
     *
     * @param dc the draw context.
     */
    public void drawCapInterior(DrawContext dc)
    {
        ShapeAttributes activeAttrs = this.getActiveCapAttributes();

        if (!activeAttrs.isDrawInterior())
            return;

        GL gl = dc.getGL();

        if (!dc.isPickingMode())
        {
            Material material = activeAttrs.getInteriorMaterial();
            if (material == null)
                material = defaultAttributes.getInteriorMaterial();

            if (activeAttrs.isEnableLighting())
            {
                material.apply(gl, GL.GL_FRONT_AND_BACK, (float) activeAttrs.getInteriorOpacity());
                gl.glNormalPointer(GL.GL_FLOAT, 0, this.boundarySet.capNormalBuffer.rewind());
            }
            else
            {
                Color sc = material.getDiffuse();
                double opacity = activeAttrs.getInteriorOpacity();
                gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));
            }
        }

        WWTexture texture = this.getCapTexture();
        if (!dc.isPickingMode() && texture != null && this.boundarySet.capTextureCoordsBuffer != null)
        {
            texture.bind(dc);
            texture.applyInternalTransform(dc);

            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, this.boundarySet.capTextureCoordsBuffer.rewind());
            dc.getGL().glEnable(GL.GL_TEXTURE_2D);
            gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        }
        else
        {
            dc.getGL().glDisable(GL.GL_TEXTURE_2D);
            gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        }

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, this.boundarySet.capVertexBuffer.rewind());

        for (int i = 0; i < this.primTypes.size(); i++)
        {
            IntBuffer ib = this.capFillIndexBuffers.get(i);
            gl.glDrawElements(this.primTypes.get(i), ib.limit(), GL.GL_UNSIGNED_INT, ib.rewind());
        }
    }

    /**
     * Draws the shape's edges. Assumes the vertex buffer has already been set in the OpenGL context.
     * <p/>
     * This base implementation draws the outline of the basic polygon. Subclasses should override it to draw their
     * outline or an alternate outline of the basic polygon.
     *
     * @param dc the draw context.
     */
    public void drawCapEdges(DrawContext dc)
    {
        ShapeAttributes activeAttrs = this.getActiveCapAttributes();

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
                material.apply(gl, GL.GL_FRONT_AND_BACK, (float) this.getActiveCapAttributes().getOutlineOpacity());
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

        for (ExtrudedBoundaryInfo boundary : this.boundarySet)
        {
            if (this.mustApplyLighting(dc))
                gl.glNormalPointer(GL.GL_FLOAT, 0, boundary.capNormalBuffer.rewind());

            IntBuffer indices = boundary.capIndices;
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, boundary.capVertexBuffer.rewind());
            gl.glDrawElements(GL.GL_LINES, indices.limit(), GL.GL_UNSIGNED_INT, indices.rewind());
        }
//
//        // Diagnostic to show the normal vectors.
//        if (this.mustApplyLighting(dc))
//            dc.drawNormals(1000, this.vertexBuffer, this.normalBuffer);
    }

    /**
     * Draws the shape's sides.
     *
     * @param dc the draw context.
     */
    protected void drawSideInteriors(DrawContext dc)
    {
        GL gl = dc.getGL();

        if (!dc.isPickingMode())
        {
            Material material = this.getActiveSideAttributes().getInteriorMaterial();
            if (material == null)
                material = defaultSideAttributes.getInteriorMaterial();

            if (this.activeSideAttributes.isEnableLighting())
            {
                material.apply(gl, GL.GL_FRONT_AND_BACK, (float) this.getActiveSideAttributes().getInteriorOpacity());
            }
            else
            {
                Color sc = material.getDiffuse();
                double opacity = this.getActiveSideAttributes().getInteriorOpacity();
                gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));
            }
        }

        for (ExtrudedBoundaryInfo boundary : this.boundarySet.boundaries)
        {
            if (this.mustApplyLighting(dc))
                gl.glNormalPointer(GL.GL_FLOAT, 0, boundary.sideNormalBuffer.rewind());

            if (!dc.isPickingMode() && boundary.sideTextureCoords != null)
            {
                dc.getGL().glEnable(GL.GL_TEXTURE_2D);
                gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
                gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, boundary.sideTextureCoords.rewind());
            }
            else
            {
                dc.getGL().glDisable(GL.GL_TEXTURE_2D);
                gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
            }

            gl.glVertexPointer(3, GL.GL_FLOAT, 0, boundary.sideVertexBuffer.rewind());

            boundary.sideIndices.rewind();
            for (int j = 0; j < boundary.faceCount; j++)
            {
                if (boundary.sideTextureCoords != null)
                {
                    boundary.sideTextures.get(j).bind(dc);
                    boundary.sideTextures.get(j).applyInternalTransform(dc);
                }

                boundary.sideIndices.position(4 * j);
                boundary.sideIndices.limit(4 * (j + 1));
                gl.glDrawElements(GL.GL_TRIANGLE_STRIP, 4, GL.GL_UNSIGNED_INT, boundary.sideIndices);
            }
        }
    }

    /**
     * Draws the shape's edges.
     *
     * @param dc the draw context.
     */
    protected void drawSideEdges(DrawContext dc)
    {
        GL gl = dc.getGL();

        ShapeAttributes activeAttrs = this.getActiveSideAttributes();

        if (!dc.isPickingMode())
        {
            Material material = activeAttrs.getOutlineMaterial();
            if (material == null)
                material = defaultSideAttributes.getOutlineMaterial();

            if (this.activeSideAttributes.isEnableLighting())
            {
                material.apply(gl, GL.GL_FRONT_AND_BACK, (float) this.getActiveSideAttributes().getOutlineOpacity());
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

        dc.getGL().glDisable(GL.GL_TEXTURE_2D);

        for (ExtrudedBoundaryInfo boundary : this.boundarySet.boundaries)
        {
            if (this.mustApplyLighting(dc))
                gl.glNormalPointer(GL.GL_FLOAT, 0, boundary.sideNormalBuffer.rewind());

            IntBuffer indices = boundary.sideEdgeIndices;
            indices.rewind();

            // Don't draw the top outline if the cap will draw it.
            if (this.isEnableCap() && this.getActiveCapAttributes().isDrawOutline())
            {
                indices = indices.slice();
                indices.position(2 * boundary.faceCount);
            }

            gl.glVertexPointer(3, GL.GL_FLOAT, 0, boundary.sideVertexBuffer.rewind());
            gl.glDrawElements(GL.GL_LINES, indices.remaining(), GL.GL_UNSIGNED_INT, indices);
        }
    }

    /**
     * Computes the information necessary to determine the extruded polygon's extent.
     *
     * @param dc the current draw context.
     */
    protected void createMinimalGeometry(DrawContext dc)
    {
        this.computeReferencePoint(dc.getTerrain(), this.boundarySet);
        if (this.boundarySet.referencePoint == null)
            return;

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

        for (Vec4 point : this.outerBoundary().capVertices)
        {
            double d = point.add3(this.boundarySet.referencePoint).distanceTo3(eyePoint);
            if (d < minDistance)
                minDistance = d;
        }

        return minDistance;
    }

    protected void computeReferencePoint(Terrain terrain, BoundarySet boundarySet)
    {
        LatLon refPos = this.getReferencePosition();
        if (refPos == null)
            return;

        boundarySet.referencePoint = terrain.getSurfacePoint(refPos.getLatitude(), refPos.getLongitude(), 0);
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
        if (this.isEnableSides() || this.isEnableCap())
            this.createVertices(terrain, boundarySet, skipOuterBoundary);

        if (this.isEnableSides())
        {
            this.createSideGeometry(boundarySet);

            if (this.mustApplyLighting(dc))
                this.createSideNormals(boundarySet);

            this.createSideTextureCoords(boundarySet);
        }

        if (this.isEnableCap())
        {
            this.createCapGeometry(dc, boundarySet);

            if (this.mustApplyLighting(dc))
                this.createCapNormals(boundarySet);
        }
    }

    protected void createVertices(Terrain terrain, BoundarySet boundarySet, boolean skipOuterBoundary)
    {
        for (int i = 0; i < boundarySet.boundaries.size(); i++)
        {
            ExtrudedBoundaryInfo boundary = boundarySet.boundaries.get(i);

            if (i > 0 || !skipOuterBoundary)
                this.computeBoundaryVertices(terrain, boundary, boundarySet.referencePoint);
        }
    }

    protected void computeBoundaryVertices(Terrain terrain, ExtrudedBoundaryInfo boundary, Vec4 refPoint)
    {
        Vec4[] topVertices = boundary.capVertices;
        if (topVertices == null || topVertices.length < boundary.locations.size())
            topVertices = new Vec4[boundary.locations.size()];

        Vec4[] bottomVertices = boundary.baseVertices;
        if (bottomVertices == null || bottomVertices.length < boundary.locations.size())
            bottomVertices = new Vec4[boundary.locations.size()];

        Vec4 vaa = null;
        double vaaLength = 0; // used to compute independent length of each cap vertex
        double vaLength = 0;

        boundary.faceCount = boundary.locations.size() - 1;
        for (int i = 0; i < boundary.faceCount; i++)
        {
            // The order for both top and bottom is CCW as one looks down from space onto the base polygon. For a
            // 4-sided polygon (defined by 5 lat/lon locations) the vertex order is 0-1-2-3-4.

            LatLon location = boundary.locations.get(i);

            // Compute the bottom point, which is on the terrain.
            Vec4 vert = terrain.getSurfacePoint(location.getLatitude(), location.getLongitude(), 0);

            bottomVertices[i] = vert.subtract3(refPoint);

            // Compute the top/cap point.
            if (this.getAltitudeMode() == WorldWind.CONSTANT || !(location instanceof Position))
            {
                if (vaa == null)
                {
                    // Compute the vector lengths of the top and bottom points at the reference position.
                    vaa = refPoint.multiply3(this.getHeight() / refPoint.getLength3());
                    vaaLength = vaa.getLength3();
                    vaLength = refPoint.getLength3();
                }

                double delta = vaLength - vert.dot3(refPoint) / vaLength;
                vert = vert.add3(vaa.multiply3(1d + delta / vaaLength));
            }
            else if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            {
                vert = terrain.getSurfacePoint(location.getLatitude(), location.getLongitude(),
                    ((Position) location).getAltitude());
            }
            else // WorldWind.ABSOLUTE
            {
                vert = terrain.getGlobe().computePointFromPosition(location.getLatitude(), location.getLongitude(),
                    ((Position) location).getAltitude() * terrain.getVerticalExaggeration());
            }

            topVertices[i] = vert.subtract3(refPoint);
        }

        topVertices[boundary.locations.size() - 1] = topVertices[0];
        bottomVertices[boundary.locations.size() - 1] = bottomVertices[0];

        boundary.capVertices = topVertices;
        boundary.baseVertices = bottomVertices;
    }

    protected void createSideGeometry(BoundarySet boundarySet)
    {
        // The side vertex buffer requires 4 vertices of x,y,z for each polygon face.
        int vertexCoordCount = this.totalFaceCount * 4 * 3; // 4 vertices of x,y,z per face

        if (boundarySet.sideVertexBuffer != null && boundarySet.sideVertexBuffer.capacity() >= vertexCoordCount)
            boundarySet.sideVertexBuffer.clear();
        else
            boundarySet.sideVertexBuffer = BufferUtil.newFloatBuffer(vertexCoordCount);

        // Create individual buffer slices for each boundary.
        for (ExtrudedBoundaryInfo boundary : boundarySet)
        {
            boundary.sideVertexBuffer = this.fillSideVertexBuffer(boundary.capVertices, boundary.baseVertices,
                boundarySet.sideVertexBuffer.slice());
            boundarySet.sideVertexBuffer.position(
                boundarySet.sideVertexBuffer.position() + boundary.sideVertexBuffer.limit());
        }
    }

    protected void createSideNormals(BoundarySet boundarySet)
    {
        int vertexCoordCount = this.totalFaceCount * 4 * 3; // 4 vertices of x,y,z per face

        if (boundarySet.sideNormalBuffer != null && boundarySet.sideNormalBuffer.capacity() >= vertexCoordCount)
            boundarySet.sideNormalBuffer.clear();
        else
            boundarySet.sideNormalBuffer = BufferUtil.newFloatBuffer(vertexCoordCount);

        // Create individual buffer slices for each boundary.
        for (ExtrudedBoundaryInfo boundary : boundarySet)
        {
            boundary.sideNormalBuffer = this.fillSideNormalBuffer(boundary.capVertices, boundary.baseVertices,
                boundarySet.sideNormalBuffer.slice());
            boundarySet.sideNormalBuffer.position(
                boundarySet.sideNormalBuffer.position() + boundary.sideNormalBuffer.limit());
        }
    }

    protected void createSideTextureCoords(BoundarySet boundarySet)
    {
        // Create individual buffer slices for each boundary.
        for (ExtrudedBoundaryInfo boundary : boundarySet)
        {
            boolean applyTextureToThisBoundary = this.hasSideTextures()
                && boundary.sideTextures != null && boundary.sideTextures.size() == boundary.faceCount;

            if (applyTextureToThisBoundary)
            {
                int texCoordSize = boundary.faceCount * 4 * 2; // n sides of 4 verts w/s,t
                if (boundary.sideTextureCoords != null && boundary.sideTextureCoords.capacity() >= texCoordSize)
                    boundary.sideTextureCoords.clear();
                else
                    boundary.sideTextureCoords = BufferUtil.newFloatBuffer(texCoordSize);

                this.fillSideTexCoordBuffer(boundary.capVertices, boundary.baseVertices,
                    boundary.sideTextureCoords);
            }
        }
    }

    /**
     * Compute the cap geometry.
     *
     * @param dc          the current draw context.
     * @param boundarySet boundary vertices are calculated during {@link #createMinimalGeometry(DrawContext)}).
     */
    protected void createCapGeometry(DrawContext dc, BoundarySet boundarySet)
    {
        if (boundarySet.capVertexBuffer != null && boundarySet.capVertexBuffer.capacity() >= this.totalNumLocations * 3)
            boundarySet.capVertexBuffer.clear();
        else
            boundarySet.capVertexBuffer = BufferUtil.newFloatBuffer(this.totalNumLocations * 3);

        // Fill the vertex buffer. Simultaneously create individual buffer slices for each boundary. These are used to
        // draw the outline.
        for (ExtrudedBoundaryInfo boundary : boundarySet)
        {
            boundary.capVertexBuffer = this.fillCapVertexBuffer(boundary.capVertices,
                boundarySet.capVertexBuffer.slice());
            boundarySet.capVertexBuffer.position(
                boundarySet.capVertexBuffer.position() + boundary.capVertexBuffer.limit());
        }

        if (this.capFillIndices == null) // need to tessellate only once
            this.createTessllationGeometry(dc, boundarySet);
    }

    protected void createCapNormals(BoundarySet boundarySet)
    {
        if (boundarySet.capNormalBuffer != null
            && boundarySet.capNormalBuffer.capacity() >= this.totalNumLocations * 3)
            boundarySet.capNormalBuffer.clear();
        else
            boundarySet.capNormalBuffer = BufferUtil.newFloatBuffer(boundarySet.capVertexBuffer.capacity());

        for (ExtrudedBoundaryInfo boundary : boundarySet)
        {
            boundary.capNormalBuffer = this.computeCapNormals(boundary, boundarySet.capNormalBuffer.slice());
            boundarySet.capNormalBuffer.position(
                boundarySet.capNormalBuffer.position() + boundary.capNormalBuffer.limit());
        }
    }

    /**
     * Compute normal vectors for an extruded polygon's cap vertices.
     *
     * @param boundary the boundary to compute normals for.
     * @param nBuf     the buffer in which to place the computed normals. Must have enough remaining space to hold the
     *                 normals.
     *
     * @return the buffer specified as input, with its limit incremented by the number of vertices copied, and its
     *         position set to 0.
     */
    protected FloatBuffer computeCapNormals(ExtrudedBoundaryInfo boundary, FloatBuffer nBuf)
    {
        int nVerts = boundary.locations.size();
        Vec4[] verts = boundary.capVertices;
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
     * Copy a boundary's vertex coordinates to a specified vertex buffer.
     *
     * @param vertices the vertices to copy.
     * @param vBuf     the buffer to copy the vertices to. Must have enough remaining space to hold the vertices.
     *
     * @return the buffer specified as input, with its limit incremented by the number of vertices copied, and its
     *         position set to 0.
     */
    protected FloatBuffer fillCapVertexBuffer(Vec4[] vertices, FloatBuffer vBuf)
    {
        for (Vec4 v : vertices)
        {
            vBuf.put((float) v.x).put((float) v.y).put((float) v.z);
        }

        vBuf.flip(); // sets the limit to the position and then the position to 0.

        return vBuf;
    }

    protected FloatBuffer fillSideVertexBuffer(Vec4[] topVerts, Vec4[] bottomVerts, FloatBuffer vBuf)
    {
        // Forms the polygon's faces from its vertices, simultaneously copying the Cartesian coordinates from a Vec4
        // array to a FloatBuffer.

        int faceCount = topVerts.length - 1;
        int size = faceCount * 4 * 3; // n sides of 4 verts of x,y,z
        vBuf.limit(size);

        // Fill the vertex buffer with coordinates for each independent face -- 4 vertices per face. Vertices need to be
        // independent in order to have different texture coordinates and normals per face.
        // For an n-sided polygon the vertex order is b0-b1-t1-t0, b1-b2-t2-t1, ... Note the counter-clockwise ordering.
        for (int i = 0; i < faceCount; i++)
        {
            int v = i;
            vBuf.put((float) bottomVerts[v].x).put((float) bottomVerts[v].y).put((float) bottomVerts[v].z);
            v = i + 1;
            vBuf.put((float) bottomVerts[v].x).put((float) bottomVerts[v].y).put((float) bottomVerts[v].z);
            v = i + 1;
            vBuf.put((float) topVerts[v].x).put((float) topVerts[v].y).put((float) topVerts[v].z);
            v = i;
            vBuf.put((float) topVerts[v].x).put((float) topVerts[v].y).put((float) topVerts[v].z);
        }

        vBuf.flip();

        return vBuf;
    }

    protected FloatBuffer fillSideNormalBuffer(Vec4[] topVerts, Vec4[] bottomVerts, FloatBuffer nBuf)
    {
        // This method parallels fillVertexBuffer. The normals are stored in exactly the same order.

        int faceCount = topVerts.length - 1;
        int size = faceCount * 4 * 3; // n sides of 4 verts of x,y,z
        nBuf.limit(size);

        for (int i = 0; i < faceCount; i++)
        {
            Vec4 va = topVerts[i + 1].subtract3(bottomVerts[i]);
            Vec4 vb = topVerts[i].subtract3(bottomVerts[i + 1]);
            Vec4 normal = va.cross3(vb).normalize3();

            nBuf.put((float) normal.x).put((float) normal.y).put((float) normal.z);
            nBuf.put((float) normal.x).put((float) normal.y).put((float) normal.z);
            nBuf.put((float) normal.x).put((float) normal.y).put((float) normal.z);
            nBuf.put((float) normal.x).put((float) normal.y).put((float) normal.z);
        }

        nBuf.flip();

        return nBuf;
    }

    protected FloatBuffer fillSideTexCoordBuffer(Vec4[] topVerts, Vec4[] bottomVerts, FloatBuffer tBuf)
    {
        int faceCount = topVerts.length - 1;
        double lengths[] = new double[faceCount + 1];

        // Find the top-to-bottom lengths of the corners in order to determine their relative lengths.
        for (int i = 0; i < faceCount; i++)
        {
            lengths[i] = bottomVerts[i].distanceTo3(topVerts[i]);
        }
        lengths[faceCount] = lengths[0]; // duplicate the first length to ease iteration below

        // Fill the vertex buffer with texture coordinates for each independent face in the same order as the vertices
        // in the vertex buffer.
        int b = 0;
        for (int i = 0; i < faceCount; i++)
        {
            // Set the base texture coord to 0 for the longer side and a proportional value for the shorter side.
            if (lengths[i] > lengths[i + 1])
            {
                tBuf.put(b++, 0).put(b++, 0);
                tBuf.put(b++, 1).put(b++, (float) (1d - lengths[i + 1] / lengths[i]));
            }
            else
            {
                tBuf.put(b++, 0).put(b++, (float) (1d - lengths[i] / lengths[i + 1]));
                tBuf.put(b++, 1).put(b++, 0);
            }
            tBuf.put(b++, 1).put(b++, 1);
            tBuf.put(b++, 0).put(b++, 1);
        }

        return tBuf;
    }

    /**
     * Returns the indices defining the cap vertices.
     *
     * @param n the number of positions in the polygon.
     *
     * @return a buffer of indices that can be passed to OpenGL to draw all the shape's edges.
     */
    protected IntBuffer getCapIndices(int n)
    {
        IntBuffer ib = capEdgeIndexBuffers.get(n);
        if (ib != null)
            return ib;

        // The edges are two-point lines connecting vertex pairs.
        ib = BufferUtil.newIntBuffer(2 * (n - 1) * 3);
        for (int i = 0; i < n - 1; i++)
        {
            ib.put(i).put(i + 1);
        }

        capEdgeIndexBuffers.put(n, ib);

        return ib;
    }

    /**
     * Returns the indices defining the vertices of each face of the shape.
     *
     * @param n the number of positions in the polygon.
     *
     * @return a buffer of indices that can be passed to OpenGL to draw all face of the shape.
     */
    protected IntBuffer getSideIndices(int n)
    {
        IntBuffer ib = sideFillIndexBuffers.get(n);
        if (ib != null)
            return ib;

        // Compute them if not already computed. Each side is two triangles defined by one triangle strip. All edges
        // can't be combined into one tri-strip because each side may have its own texture and therefore different
        // texture coordinates.
        ib = BufferUtil.newIntBuffer(n * 4);
        for (int i = 0; i < n; i++)
        {
            ib.put(4 * i + 3).put(4 * i).put(4 * i + 2).put(4 * i + 1);
        }

        sideFillIndexBuffers.put(n, ib);

        return ib;
    }

    /**
     * Returns the indices defining the vertices of a boundary's face edges.
     *
     * @param n the number of positions in the boundary.
     *
     * @return a buffer of indices that can be passed to OpenGL to draw all the boundary's edges.
     */
    protected IntBuffer getSideEdgeIndices(int n)
    {
        IntBuffer ib = sideEdgeIndexBuffers.get(n);
        if (ib != null)
            return ib;

        int nn = n - 1; // the boundary is closed so don't add an edge for the redundant position.

        // The edges are two-point lines connecting vertex pairs.

        ib = BufferUtil.newIntBuffer((2 * nn) * 3); // 2n each for top, bottom and corners

        // Top. Keep this first so that the top edge can be turned off independently.
        for (int i = 0; i < nn; i++)
        {
            ib.put(4 * i + 2).put(4 * i + 3);
        }

        // Bottom
        for (int i = 0; i < nn; i++)
        {
            ib.put(4 * i).put(4 * i + 1);
        }

        // Corners
        for (int i = 0; i < nn; i++)
        {
            ib.put(4 * i).put(4 * i + 3);
        }

        sideEdgeIndexBuffers.put(n, ib);

        return ib;
    }

    /**
     * Tessellates the extruded polygon's cap.
     * <p/>
     * This method catches {@link OutOfMemoryError} exceptions and calls {@link #handleUnsuccessfulCapCreation(BoundarySet)}
     * when they occur.
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
    protected void tessellatePolygon(List<ExtrudedBoundaryInfo> boundaries, Vec4 normal)
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
            for (ExtrudedBoundaryInfo boundary : boundaries)
            {
                glu.gluTessBeginContour(glts.getGLUtessellator());
                FloatBuffer vBuf = boundary.capVertexBuffer;
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
        if (this.capFillIndices == null || this.capFillIndices.capacity() < cb.getNumIndices())
            this.capFillIndices = BufferUtil.newIntBuffer(cb.getNumIndices());
        else
            this.capFillIndices.clear();

        if (this.capFillIndexBuffers == null || this.capFillIndexBuffers.size() < cb.getPrimTypes().size())
            this.capFillIndexBuffers = new ArrayList<IntBuffer>(cb.getPrimTypes().size());
        else
            this.capFillIndexBuffers.clear();

        this.primTypes = cb.getPrimTypes();

        for (ArrayList<Integer> prim : cb.getPrims())
        {
            IntBuffer ib = this.capFillIndices.slice();
            for (Integer i : prim)
            {
                ib.put(i);
            }
            ib.flip();
            this.capFillIndexBuffers.add(ib);
            this.capFillIndices.position(this.capFillIndices.position() + ib.limit());
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
     * Compute the intersections of a specified line with this extruded polygon. If the polygon's altitude mode is other
     * than {@link WorldWind#ABSOLUTE}, the extruded polygon's geometry is created relative to the specified terrain
     * rather than the terrain used during rendering, which may be at lower level of detail than required for accurate
     * intersection determination.
     *
     * @param line    the line to intersect.
     * @param terrain the {@link Terrain} to use when computing the extruded polygon's geometry.
     *
     * @return a list of intersections identifying where the line intersects the extruded polygon, or null if the line
     *         does not intersect the extruded polygon.
     *
     * @throws InterruptedException if the operation is interrupted.
     * @see Terrain
     */
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
    {
        if (!this.isEnableSides() && !this.isEnableCap())
            return null;

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

        for (ExtrudedBoundaryInfo boundary : highResBoundarySet)
        {
            List<Intersection> boundaryIntersections = this.intersectBoundarySides(localLine, boundary);

            if (boundaryIntersections != null && boundaryIntersections.size() > 0)
                intersections.addAll(boundaryIntersections);
        }

        if (this.isEnableCap())
            this.intersectCap(localLine, highResBoundarySet, intersections);

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

        if (this.isEnableSides())
            this.createSideGeometry(boundarySet);

        if (this.isEnableCap())
            this.createCapGeometry(null, boundarySet);

        boundarySet.extent = this.computeExtent(boundarySet.boundaries.get(0), boundarySet.referencePoint);

        return boundarySet;
    }

    /**
     * Intersects a line with the sides of an individual boundary.
     *
     * @param line     the line to intersect.
     * @param boundary the boundary to intersect.
     *
     * @return the computed intersections, or null if there are no intersections.
     *
     * @throws InterruptedException if the operation is interrupted.
     */
    protected List<Intersection> intersectBoundarySides(Line line, ExtrudedBoundaryInfo boundary)
        throws InterruptedException
    {
        List<Intersection> intersections = new ArrayList<Intersection>();
        Vec4[] topVertices = boundary.capVertices;
        Vec4[] bottomVertices = boundary.baseVertices;

        for (int i = 0; i < boundary.baseVertices.length - 1; i++)
        {
            Vec4 va = bottomVertices[i];
            Vec4 vb = topVertices[i + 1];
            Vec4 vc = topVertices[i];

            Intersection intersection = Triangle.intersect(line, va, vb, vc);
            if (intersection != null)
                intersections.add(intersection);

            vc = bottomVertices[i + 1];

            intersection = Triangle.intersect(line, va, vb, vc);
            if (intersection != null)
                intersections.add(intersection);
        }

        return intersections.size() > 0 ? intersections : null;
    }

    protected void intersectCap(Line line, BoundarySet boundarySet, List<Intersection> intersections)
        throws InterruptedException
    {
        if (this.primTypes == null)
            return;

        for (int i = 0; i < this.primTypes.size(); i++)
        {
            IntBuffer ib = this.capFillIndexBuffers.get(i);
//            ib = BufferUtil.newIntBuffer(4);
//            ib.put(4).put(1).put(2).put(3);
            ib.rewind();
            List<Intersection> ti = Triangle.intersectTriangleTypes(line, boundarySet.capVertexBuffer, ib,
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
        xmlWriter.writeCharacters("1");
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
            if (outerBoundary.iterator().hasNext() && outerBoundary.iterator().next() instanceof Position)
                this.exportBoundaryAsLinearRing(xmlWriter, outerBoundary);
            else
                KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, outerBoundary, getHeight());
            xmlWriter.writeEndElement(); // outerBoundaryIs
        }

        // Inner boundaries
        Iterator<ExtrudedBoundaryInfo> boundaryIterator = boundarySet.iterator();
        if (boundaryIterator.hasNext())
            boundaryIterator.next(); // Skip outer boundary, we already dealt with it above

        while (boundaryIterator.hasNext())
        {
            ExtrudedBoundaryInfo boundary = boundaryIterator.next();

            xmlWriter.writeStartElement("innerBoundaryIs");
            if (boundary.locations.iterator().hasNext() && boundary.locations.iterator().next() instanceof Position)
                this.exportBoundaryAsLinearRing(xmlWriter, outerBoundary);
            else
                KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, boundary.locations, getHeight());
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
