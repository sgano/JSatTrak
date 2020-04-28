/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL;
import javax.xml.stream.*;
import java.awt.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.List;

import static gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil.kmlBoolean;

// TODO: Measurement (getLength), Texture, maybe lighting

/**
 * Displays a line or curve between positions. The path is drawn between input positions to achieve a specified path
 * type, e.g., {@link AVKey#GREAT_CIRCLE}. It can also conform to the underlying terrain. A curtain may be formed by
 * extruding the path to the ground.
 * <p/>
 * Altitudes within the path's positions are interpreted according to the path's altitude mode. If the altitude mode is
 * {@link WorldWind#ABSOLUTE}, the altitudes are considered as height above the ellipsoid. If the altitude mode is
 * {@link WorldWind#RELATIVE_TO_GROUND}, the altitudes are added to the elevation of the terrrain at the position. If
 * the altitude mode is {@link WorldWind#CLAMP_TO_GROUND} the altitudes are ignored.
 * <p/>
 * Between the specified positions the path is drawn along a curve specified by the path's path type, either {@link
 * AVKey#GREAT_CIRCLE}, {@link AVKey#RHUMB_LINE} or {@link AVKey#LINEAR}. (See {@link #setPathType(String)}.)
 * <p/>
 * Paths have separate attributes for normal display and highlighted display. If no attributes are specified, default
 * attributes are used. See {@link #DEFAULT_INTERIOR_MATERIAL}, {@link #DEFAULT_OUTLINE_MATERIAL}, and {@link
 * #DEFAULT_HIGHLIGHT_MATERIAL}.
 * <p/>
 * When the path is terrain following -- altitude mode is {@link WorldWind#CLAMP_TO_GROUND}, or {@link
 * WorldWind#RELATIVE_TO_GROUND} -- the terrain conformance is governed by the path's <code>terrain conformance</code>
 * property. See {@link #setTerrainConformance(double)}. When the path is not terrain following, path-type conformance
 * is governed by the number of sub-segments used to tessellate the path between positions. (See {@link
 * #setNumSubsegments(int)}.
 *
 * @author tag
 * @version $Id: Path.java 14177 2010-12-03 00:28:05Z tgaskins $
 */
public class Path extends WWObjectImpl implements Highlightable, OrderedRenderable, Movable, ExtentHolder, Exportable
{
    /** The default interior color. */
    protected static final Material DEFAULT_INTERIOR_MATERIAL = Material.PINK;
    /** The default outline color. */
    protected static final Material DEFAULT_OUTLINE_MATERIAL = Material.RED;
    /** The default highlight color. */
    protected static final Material DEFAULT_HIGHLIGHT_MATERIAL = Material.WHITE;
    /** The default path type. */
    protected static final String DEFAULT_PATH_TYPE = AVKey.LINEAR;
    /** The default altitude mode. */
    protected static final int DEFAULT_ALTITUDE_MODE = WorldWind.ABSOLUTE;
    /** The default geometry regeneration interval. */
    protected static final long DEFAULT_GEOMETRY_GENERATION_INTERVAL = 2000;

    /** The attributes used if attributes are not specified. */
    protected static final ShapeAttributes defaultAttributes;

    static
    {
        defaultAttributes = new BasicShapeAttributes();
        defaultAttributes.setInteriorMaterial(DEFAULT_INTERIOR_MATERIAL);
        defaultAttributes.setOutlineMaterial(DEFAULT_OUTLINE_MATERIAL);
    }

    Iterable<? extends Position> positions; // the positions as provided by the application
    protected int numPositions; // the number of positions in the posistions field.
    protected ShapeAttributes normalAttrs;
    protected ShapeAttributes highlightAttrs;
    protected ShapeAttributes activeAttributes = new BasicShapeAttributes(); // re-determined each frame

    protected boolean highlighted;
    protected boolean visible = true;
    protected String pathType = DEFAULT_PATH_TYPE;
    protected int altitudeMode = DEFAULT_ALTITUDE_MODE;
    protected boolean followTerrain; // true if altitude mode indicates terrain following
    protected boolean extrude;
    protected double terrainConformance = 10;
    protected int numSubsegments = 10;
    protected boolean drawVerticals = true;
    protected boolean enableBatchRendering = true;
    protected boolean enableBatchPicking = true;
    protected long geometryRegenerationInteval = DEFAULT_GEOMETRY_GENERATION_INTERVAL;
    protected int outlinePickWidth = 10;

    // Values computed once per frame and reused during the frame as needed.
    protected long frameNumber = -1; // identifies frame used to calculate these values
    protected double previousExaggeration = -1;
    protected List<Position> tessellatedPositions; // positions formed from path type and terrain conformance
    protected List<Integer> polePositions = new ArrayList<Integer>(); // identifies original positions in rendered path
    protected FloatBuffer renderedPath; // the model-coordinate points to render, relative to reference center
    protected boolean hasExtrusionPoints; // true when the rendered path contains extrusion points
    protected Vec4 referenceCenter;
    protected Extent extent;
    protected double eyeDistance;
    protected Position endPosition;
    protected Layer pickLayer;

    protected PickSupport pickSupport = new PickSupport();

    /** Creates a path with no positions. */
    public Path()
    {
    }

    /**
     * Creates a path with specified positions.
     * <p/>
     * Note: If fewer than two positions is specified, no path is drawn.
     *
     * @param positions the path positions.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public Path(List<? extends Position> positions)
    {
        this.setPositions(positions);
    }

    /**
     * Creates a path with specified positions.
     * <p/>
     * Note: If fewer than two positions is specified, no path is drawn.
     *
     * @param positions the path positions.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public Path(Iterable<? extends Position> positions)
    {
        this.setPositions(positions);
    }

    /**
     * Creates a path with positions specified via a generic list.
     * <p/>
     * Note: If fewer than two positions is specified, the path is not drawn.
     *
     * @param positions the path positions.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public Path(Position.PositionList positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setPositions(positions.list);
    }

    public Path(Position posA, Position posB)
    {
        if (posA == null || posB == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<Position> endPoints = new ArrayList<Position>(2);
        endPoints.add(posA);
        endPoints.add(posB);
        this.setPositions(endPoints);
    }

    /** Invalidates computed values. Called when the path's contents or certain attributes change. */
    protected void reset()
    {
        this.referenceCenter = null;
        this.tessellatedPositions = null;
        if (this.renderedPath != null)
            this.renderedPath.clear();
        this.extent = null;
    }

    /**
     * Returns the path's positions.
     *
     * @return the path's positions. Will be null if no positions have been specified.
     */
    public Iterable<? extends Position> getPositions()
    {
        return positions;
    }

    /**
     * Specifies the path's positions, which replace the path's current positions, if any.
     * <p/>
     * Note: If fewer than two positions is specified, the path is not drawn.
     *
     * @param positions the path's positions.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.reset();
        this.positions = positions;
        this.computePositionCount();
    }

    /**
     * Returns the path's normal (as opposed to highlight) attributes.
     *
     * @return the path's normal attributes. May be null.
     */
    public ShapeAttributes getAttributes()
    {
        return normalAttrs;
    }

    /**
     * Specifies the path's normal (as opposed to highlight) attributes.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    public void setAttributes(ShapeAttributes normalAttrs)
    {
        this.normalAttrs = normalAttrs;
    }

    /**
     * Returns the path's highlight attributes.
     *
     * @return the path's highlight attributes. May be null.
     */
    public ShapeAttributes getHighlightAttributes()
    {
        return highlightAttrs;
    }

    /**
     * Specifies the path's highlight attributes.
     *
     * @param highlightAttrs the highlight attributes. May be null, in which case default attributes are used.
     */
    public void setHighlightAttributes(ShapeAttributes highlightAttrs)
    {
        this.highlightAttrs = highlightAttrs;
    }

    /**
     * Indicates whether to highlight the path.
     *
     * @return true if highlighted, otherwise false.
     *
     * @see #setHighlighted(boolean)
     * @see #setHighlightAttributes(ShapeAttributes)
     */
    public boolean isHighlighted()
    {
        return highlighted;
    }

    /**
     * Specifies whether the path is highlighted.
     *
     * @param highlighted true to highlight the path, otherwise false. The default value is false.
     */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    /**
     * Indicates whether to draw the path.
     *
     * @return true if the path is drawn, otherwise false.
     *
     * @see #setVisible(boolean)
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Specifies whether to draw the path.
     *
     * @param visible true to draw the path, otherwise false. The default value is true.
     *
     * @see #setAttributes(ShapeAttributes)
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * Returns the path's altitude mode.
     *
     * @return the path's altitude mode.
     *
     * @see #setAltitudeMode(int)
     */
    public int getAltitudeMode()
    {
        return altitudeMode;
    }

    /**
     * Specifies the path's altitude mode, one of {@link WorldWind#ABSOLUTE}, {@link WorldWind#RELATIVE_TO_GROUND} or
     * {@link WorldWind#CLAMP_TO_GROUND}.
     * <p/>
     * Note: If the altitude mode is unrecognized, {@link WorldWind#ABSOLUTE} is used.
     *
     * @param altitudeMode the altitude mode. The default value is {@link WorldWind#ABSOLUTE}.
     */
    public void setAltitudeMode(int altitudeMode)
    {
        this.reset();
        this.altitudeMode = altitudeMode;
    }

    /**
     * Indicates whether to extrude the path.
     *
     * @return true to extrude the path, otherwise false.
     *
     * @see #setExtrude(boolean)
     */
    public boolean isExtrude()
    {
        return extrude;
    }

    /**
     * Specifies whether to extrude the path.
     *
     * @param extrude true to extrude the path, otherwise false. The default value is false.
     */
    public void setExtrude(boolean extrude)
    {
        this.extrude = extrude;
    }

    /**
     * Indicates whether the path is terrain following.
     *
     * @return true if terrain following, otherwise false.
     *
     * @see #setFollowTerrain(boolean)
     */
    public boolean isFollowTerrain()
    {
        return followTerrain;
    }

    /**
     * Specifies whether the path is terrain following.
     *
     * @param followTerrain true if terrain following, otherwise false. The default value is false.
     */
    public void setFollowTerrain(boolean followTerrain)
    {
        this.reset();
        this.followTerrain = followTerrain;
    }

    public double getDistanceFromEye()
    {
        return this.eyeDistance;
    }

    /**
     * Indicates the number of segments used between specified positions to achieve the path's path type. Higher values
     * cause the path to conform more closely to the path type but decrease performance.
     * <p/>
     * Note: The sub-segments number is ignored when the path follows terrain or when the path type is {@link
     * AVKey#LINEAR}.
     *
     * @return the number of sub-segments.
     *
     * @see #setNumSubsegments(int)
     */
    public int getNumSubsegments()
    {
        return numSubsegments;
    }

    /**
     * Specifies the number of segments used between specified positions to achieve the path's path type. Higher values
     * cause the path to conform more closely to the path type but decrease performance.
     * <p/>
     * Note: The sub-segments number is ignored when the path follows terrain or when the path type is {@link
     * AVKey#LINEAR}.
     *
     * @param numSubsegments the number of sub-segments. The default is 10.
     */
    public void setNumSubsegments(int numSubsegments)
    {
        this.reset();
        this.numSubsegments = numSubsegments;
    }

    /**
     * Indicates the terrain conformance target when the path follows the terrain.
     *
     * @return the terrain conformance, in pixels.
     *
     * @see #setTerrainConformance(double)
     */
    public double getTerrainConformance()
    {
        return terrainConformance;
    }

    /**
     * Specifies how accurately the path must adhere to the terrain when the path is terrain following. The value
     * specifies the minimum number of pixels between tessellation points. Lower values increase accuracy but decrease
     * performance.
     *
     * @param terrainConformance the number of pixels between tessellation points.
     */
    public void setTerrainConformance(double terrainConformance)
    {
        this.reset();
        this.terrainConformance = terrainConformance;
    }

    /**
     * Indicate's the paths path type.
     *
     * @return the path type.
     *
     * @see #setPathType(String)
     */
    public String getPathType()
    {
        return pathType;
    }

    /**
     * Specifies the path's path type. Recognized values are {@link AVKey#GREAT_CIRCLE}, {@link AVKey#RHUMB_LINE} and
     * {@link AVKey#LINEAR}.
     *
     * @param pathType the current path type. The default value is {@link AVKey#LINEAR}.
     */
    public void setPathType(String pathType)
    {
        this.reset();
        this.pathType = pathType;
    }

    /**
     * Indicates whether to draw vertical lines at each specified path position when the path is extruded.
     *
     * @return true to draw the lines, otherwise false.
     *
     * @see #setDrawVerticals(boolean)
     */
    public boolean isDrawVerticals()
    {
        return drawVerticals;
    }

    /**
     * Specifies whether to draw vertical lines at each specified path position when the path is extruded.
     *
     * @param drawVerticals true to draw the lines, otherwise false. The default value is true.
     */
    public void setDrawVerticals(boolean drawVerticals)
    {
        this.drawVerticals = drawVerticals;
    }

    public boolean isEnableBatchRendering()
    {
        return enableBatchRendering;
    }

    public void setEnableBatchRendering(boolean enableBatchRendering)
    {
        this.enableBatchRendering = enableBatchRendering;
    }

    public boolean isEnableBatchPicking()
    {
        return enableBatchPicking;
    }

    public void setEnableBatchPicking(boolean enableBatchPicking)
    {
        this.enableBatchPicking = enableBatchPicking;
    }

    public int getOutlinePickWidth()
    {
        return outlinePickWidth;
    }

    public void setOutlinePickWidth(int outlinePickWidth)
    {
        this.outlinePickWidth = outlinePickWidth;
    }

    /**
     * Indicates the maximum length of time between geometry regenerations. See {@link
     * #setGeometryRegenerationInteval(long)} for the regeneration-interval's description.
     *
     * @return the geometry regeneration interval, in milliseconds.
     *
     * @see #setGeometryRegenerationInteval(long)
     */
    public long getGeometryRegenerationInteval()
    {
        return geometryRegenerationInteval;
    }

    /**
     * Specifies the maximum length of time between geometry regenerations. The geometry is regenerated when the
     * polygon's altitude mode is {@link WorldWind#RELATIVE_TO_GROUND} in order to capture changes to the terrain. (The
     * terrain changes when its resolution changes or when new elevation data is returned from a server.) Decreasing
     * this value causes the geometry to more quickly track terrain changes, but at the cost of performance. Increasing
     * this value often does not have much effect because there are limiting factors other than geometry regeneration.
     *
     * @param geometryRegenerationInteval the geometry regeneration interval, in milliseconds.
     */
    public void setGeometryRegenerationInteval(long geometryRegenerationInteval)
    {
        this.geometryRegenerationInteval = geometryRegenerationInteval;
    }

    /**
     * Returns the path's extent in model coordinates.
     *
     * @return the path's extent, or null if an extent cannot be computed.
     */
    public Extent getExtent()
    {
        return this.extent;
    }

    protected void setExtent(Extent extent)
    {
        this.extent = extent;
    }

    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        return this.referenceCenter == null || this.renderedPath == null
            || dc.getVerticalExaggeration() != this.previousExaggeration
            || this.frameNumber - this.regenTime > this.getGeometryRegenerationInteval();
    }

    /** Counts the number of positions in the path's specified positions. */
    protected void computePositionCount()
    {
        this.numPositions = 0;

        if (this.positions != null)
        {
            //noinspection UnusedDeclaration
            for (Position pos : this.positions)
            {
                ++this.numPositions;
                this.endPosition = pos;
            }
        }
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
        else if (this.getAttributes() != null)
        {
            this.activeAttributes.copy(this.getAttributes());
        }
        else
        {
            this.activeAttributes.copy(defaultAttributes);
        }
    }

    public ShapeAttributes getActiveAttributes()
    {
        return this.activeAttributes;
    }

    /**
     * Indicates whether blending must be applied, which it must when the polygon has any transparency.
     *
     * @param dc the current draw context.
     *
     * @return true if blending must be applied, otherwise false.
     */
    protected boolean mustApplyBlending(DrawContext dc)
    {
        if (dc.isPickingMode())
            return false;

        ShapeAttributes attrs = this.getActiveAttributes();

        return (attrs.isDrawOutline() && attrs.getOutlineOpacity() < 1)
            || (attrs.isDrawInterior() && attrs.getInteriorOpacity() < 1);
    }

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

    public void render(DrawContext dc)
    {
        // This render method is called three times during frame generation. It's first called as a {@link Renderable}
        // during <code>Renderable</code> picking. It's called again during normal rendering. And it's called a third
        // time as an OrderedRenderable. The first two calls determine whether to add the path to the ordered renderable
        // list during pick and render. The third call just draws the ordered renderable.
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getSurfaceGeometry() == null)
            return;

        if (!this.isVisible())
            return;

        if (this.getExtent() != null)
        {
            if (!this.intersectsFrustum(dc))
                return;

            // If the path is less that a pixel in size, don't render it.
            if (dc.isSmall(this.getExtent(), 1))
                return;
        }

        if (dc.isOrderedRenderingMode())
            this.drawOrderedRenderable(dc);
        else
            this.makeOrderedRenderable(dc);
    }

    protected long regenTime = -1;

    /**
     * Determines whether to add the path to the draw context's ordered renderable list and creates the path points.
     *
     * @param dc the current draw context.
     */
    protected void makeOrderedRenderable(DrawContext dc)
    {
        // Re-use values already calculated this frame.
        if (dc.getFrameTimeStamp() != this.frameNumber)
        {
            this.frameNumber = dc.getFrameTimeStamp();

            this.determineActiveAttributes();
            if (this.getActiveAttributes() == null)
                return;

            // Regenerate the positions and path at a specified frequency.
            if (this.mustRegenerateGeometry(dc)) // TODO: account for multi-window
            {
                this.regenTime = this.frameNumber;
                this.previousExaggeration = dc.getVerticalExaggeration();

                if (this.referenceCenter == null) // ref center never changes unless the path's positions change
                {
                    this.computeReferenceCenter(dc);
                    if (this.referenceCenter == null)
                        return;

                    this.eyeDistance = dc.getView().getEyePoint().distanceTo3(this.referenceCenter);
                }

                // Re-use the tessellation positions if we can.
                if (this.tessellatedPositions == null || this.isFollowTerrain())
                    this.makeTessellatedPositions(dc);
                if (this.tessellatedPositions == null || this.tessellatedPositions.size() < 2)
                    return;

                // Create the rendered Cartesian points.
                this.renderedPath = this.computePath(dc, this.tessellatedPositions, this.tessellatedPositions.size(),
                    this.renderedPath);
                if (this.renderedPath == null || this.renderedPath.limit() < 6)
                    return;

                this.setExtent(this.computeExtent());

                if (this.getExtent() == null)
                    return;

                // If the path is less that a pixel in size, don't render it.
                if (dc.isSmall(this.getExtent(), 1))
                    return;

                if (!this.intersectsFrustum(dc))
                    return;
            }
        }

        if (this.renderedPath == null || this.renderedPath.limit() < 6)
            return;

        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();

        dc.addOrderedRenderable(this);
    }

    /**
     * Determines whether the path intersects the view frustum.
     *
     * @param dc the current draw context.
     *
     * @return true if the path intersects the frustum, otherwise false.
     */
    protected boolean intersectsFrustum(DrawContext dc)
    {
        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(this.getExtent());

        return dc.getView().getFrustumInModelCoordinates().intersects(this.getExtent());
    }

    /**
     * Draws the path as an ordered renderable.
     *
     * @param dc the current draw context.
     */
    protected void drawOrderedRenderable(DrawContext dc)
    {
        if (this.renderedPath == null)
            return;

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
     * Draws this ordered renderable and all subsequent Path ordered renderables in the ordered renderable list.
     *
     * @param dc the current draw context.
     */
    protected void drawBatched(DrawContext dc)
    {
        // Draw as many as we can in a batch to save ogl state switching.
        Object nextItem = dc.peekOrderedRenderables();

        if (!dc.isPickingMode())
        {
            while (nextItem != null && nextItem instanceof Path)
            {
                Path p = (Path) nextItem;
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

            while (nextItem != null && nextItem instanceof Path)
            {
                Path p = (Path) nextItem;

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
        dc.getView().setReferenceCenter(dc, this.referenceCenter);

        if (dc.isPickingMode())
        {
            Color pickColor = dc.getUniquePickColor();
            this.pickSupport.addPickableObject(this.createPickedObject(dc, pickColor));
            dc.getGL().glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
        }

        if (this.getActiveAttributes().isDrawInterior() && this.hasExtrusionPoints)
            this.drawInterior(dc, this.renderedPath);

        if (this.getActiveAttributes().isDrawOutline())
            this.drawOutline(dc, this.renderedPath, this.isDrawVerticals() ? this.polePositions : null);
    }

    /**
     * Create a {@link gov.nasa.worldwind.pick.PickedObject} for this Path. The PickedObject returned by this method
     * will be added to the pick list to represent the current Path.
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

    /**
     * Establish the OpenGL state needed to draw Paths.
     *
     * @param dc the current draw context.
     */
    protected void beginDrawing(DrawContext dc)
    {
        GL gl = dc.getGL();

        int attrMask = GL.GL_CURRENT_BIT
            | GL.GL_LINE_BIT | GL.GL_HINT_BIT // for outline
            | GL.GL_POLYGON_BIT // for interior
            | GL.GL_COLOR_BUFFER_BIT; // for blending

        gl.glPushAttrib(attrMask);
        gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);

        gl.glEnableClientState(GL.GL_VERTEX_ARRAY); // all drawing uses vertex arrays

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_LINE_SMOOTH);

            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);
        }

        dc.getView().pushReferenceCenter(dc, this.referenceCenter);
    }

    /**
     * Pop the state set in beginDrawing.
     *
     * @param dc the current draw context.
     */
    protected void endDrawing(DrawContext dc)
    {
        dc.getView().popReferenceCenter(dc);
        dc.getGL().glPopAttrib();
        dc.getGL().glPopClientAttrib();
    }

    /**
     * Draws the path's outline, or just the path line if the path is not extruded.
     *
     * @param dc    the current draw context.
     * @param path  the path to draw.
     * @param poles a list of indices identifying the location of points in the path corresponding to the original path
     *              positions. Used to draw the vertical lines at these positions for extruded paths. May be null to
     *              indicate that vertical lines should not be drawn.
     */
    protected void drawOutline(DrawContext dc, FloatBuffer path, List<Integer> poles)
    {
        GL gl = dc.getGL();
        ShapeAttributes attrs = this.getActiveAttributes();
        boolean projectionOffsetPushed = false; // keep track for error recovery

        try
        {
            if (!dc.isPickingMode())
            {
                Material material = attrs.getOutlineMaterial();
                if (material == null)
                    material = defaultAttributes.getOutlineMaterial();

                Color sc = material.getDiffuse();
                double opacity = attrs.getOutlineOpacity();
                gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));

                gl.glHint(GL.GL_LINE_SMOOTH_HINT, attrs.isEnableAntialiasing() ? GL.GL_NICEST : GL.GL_DONT_CARE);
            }

            if (attrs.getOutlineStippleFactor() > 0)
            {
                gl.glEnable(GL.GL_LINE_STIPPLE);
                gl.glLineStipple(attrs.getOutlineStippleFactor(), attrs.getOutlineStipplePattern());
            }

            if (dc.isPickingMode() && attrs.getOutlineWidth() < this.getOutlinePickWidth())
                gl.glLineWidth(this.getOutlinePickWidth());
            else
                gl.glLineWidth((float) attrs.getOutlineWidth());

            if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND || this.hasExtrusionPoints)
            {
                dc.pushProjectionOffest(0.99);
                projectionOffsetPushed = true;
            }

            if (this.hasExtrusionPoints)
                path.limit(path.limit() / 2);

            gl.glVertexPointer(3, GL.GL_FLOAT, 0, path.rewind());
            gl.glDrawArrays(GL.GL_LINE_STRIP, 0, path.limit() / 3);

            if (this.hasExtrusionPoints && poles != null)
            {
                path.limit(path.limit() * 2);
                this.drawVerticalOutline(dc, path, poles);
            }
        }
        finally
        {
            if (projectionOffsetPushed)
                dc.popProjectionOffest();
        }
    }

    /**
     * Draws vertical lines at the path's specified positions.
     *
     * @param dc         the current draw context.
     * @param pathToDraw the path points.
     * @param poles      a list of indices identifying the location of the points corresponding to the original path
     *                   positions in the path to draw. If null or its size is less then one, verticals are not drawn.
     */
    protected void drawVerticalOutline(DrawContext dc, FloatBuffer pathToDraw, List<Integer> poles)
    {
        if (poles == null || poles.size() < 1)
            return;

        int N = (pathToDraw.limit() / 3) / 2;
        int numPoints = poles.size() * 2;
        IntBuffer indices = BufferUtil.newIntBuffer(numPoints);

        for (Integer i : poles)
        {
            indices.put(i).put(i + N);
        }

        GL gl = dc.getGL();

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, pathToDraw.rewind());
        gl.glDrawElements(GL.GL_LINES, indices.limit(), GL.GL_UNSIGNED_INT, indices.rewind());
    }

    /**
     * Draws the path's interior when the path is extruded.
     *
     * @param dc   the current draw context.
     * @param path the path to draw, including the extrusion positions.
     */
    protected void drawInterior(DrawContext dc, FloatBuffer path)
    {
        GL gl = dc.getGL();

        if (!dc.isPickingMode())
        {
            Material material = this.getActiveAttributes().getInteriorMaterial();
            if (material == null)
                material = defaultAttributes.getInteriorMaterial();

            Color sc = material.getDiffuse();
            double opacity = this.getActiveAttributes().getInteriorOpacity();
            gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));
        }

        int numPoints = path.limit() / 3;
        int numEdges = numPoints / 2;
        IntBuffer indices = BufferUtil.newIntBuffer(numPoints);

        int N = numPoints / 2;
        indices.put(0).put(N);
        for (int i = 1; i < numEdges; i++)
        {
            indices.put(i).put(N + i);
        }

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, path.rewind());
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, indices.limit(), GL.GL_UNSIGNED_INT, indices.rewind());
    }

    /**
     * Computes a model-coordinate path from a list of positions. Applies the path's terrain-conformance settings. Adds
     * extrusion points -- those on the ground -- when the path is extruded.
     *
     * @param dc           the current draw context.
     * @param positions    the positions to create a path for.
     * @param numPositions the number of positions in the positions list.
     * @param path         a buffer in which to store the computed points. May be null. The buffer is not used if it is
     *                     null or tool small for the required number of points. A new buffer is created in that case
     *                     and returned by this method. This method modifies the buffer,s position and limit fields.
     *
     * @return the buffer in which to place the computed points.
     */
    protected FloatBuffer computePath(DrawContext dc, Iterable<? extends Position> positions, int numPositions,
        FloatBuffer path)
    {
        this.hasExtrusionPoints = false;

        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
            path = this.computePointsRelativeToTerrain(dc, positions, numPositions, 0d, path);
        else if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            path = this.computePointsRelativeToTerrain(dc, positions, numPositions, null, path);
        else
            path = this.computeAbsolutePoints(dc, positions, numPositions, path);

        path.flip(); // since the path is reused the limit might not be the same as the previous usage

        return path;
    }

    /**
     * Computes a terrain-conforming, model-coordinate path from a list of positions, using either a specifed altitude
     * or the altitudes in the specified positions. Adds extrusion points -- those on the ground -- when the path is
     * extruded and the specified single altitude is not 0.
     *
     * @param dc           the current draw context.
     * @param positions    the positions to create a path for.
     * @param numPositions the number of positions in the positions list.
     * @param altitude     if non-null, the height above the terrain to use for all positions. If null, each position's
     *                     altitude is used as the height above the terrain.
     * @param path         a buffer in which to store the computed points. May be null. The buffer is not used if it is
     *                     null or tool small for the required number of points. A new buffer is created in that case
     *                     and returned by this method. This method modifies the buffer,s position and limit fields.
     *
     * @return the buffer in which to place the computed points.
     */
    protected FloatBuffer computePointsRelativeToTerrain(DrawContext dc, Iterable<? extends Position> positions,
        int numPositions, Double altitude, FloatBuffer path)
    {
        boolean extrudeIt = this.isExtrude() && !(altitude != null && altitude == 0);
        int numPoints = extrudeIt ? 2 * numPositions : numPositions;

        if (path == null || path.capacity() < numPoints * 3)
            path = BufferUtil.newFloatBuffer(3 * numPoints);

        path.clear();

        for (Position pos : positions)
        {
            double height = altitude != null ? altitude : pos.getAltitude();
            Vec4 pt = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), height);
            path.put((float) (pt.x - this.referenceCenter.x));
            path.put((float) (pt.y - this.referenceCenter.y));
            path.put((float) (pt.z - this.referenceCenter.z));
        }

        if (extrudeIt)
            this.appendTerrainPoints(dc, positions, path);

        return path;
    }

    /**
     * Computes a model-coordinate path from a list of positions, using the altitudes in the specified positions. Adds
     * extrusion points -- those on the ground -- when the path is extruded and the specified single altitude is not 0.
     *
     * @param dc           the current draw context.
     * @param positions    the positions to create a path for.
     * @param numPositions the number of positions in the positions list.
     * @param path         a buffer in which to store the computed points. May be null. The buffer is not used if it is
     *                     null or tool small for the required number of points. A new buffer is created in that case
     *                     and returned by this method. This method modifies the buffer,s position and limit fields.
     *
     * @return the buffer in which to place the computed points.
     */
    protected FloatBuffer computeAbsolutePoints(DrawContext dc, Iterable<? extends Position> positions,
        int numPositions, FloatBuffer path)
    {
        int numPoints = this.isExtrude() ? 2 * numPositions : numPositions;

        if (path == null || path.capacity() < numPoints * 3)
            path = BufferUtil.newFloatBuffer(3 * numPoints);

        path.clear();

        Globe globe = dc.getGlobe();

        if (dc.getVerticalExaggeration() != 1)
        {
            double ve = dc.getVerticalExaggeration();
            for (Position pos : positions)
            {
                Vec4 pt = globe.computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
                    ve * (pos.getAltitude()));
                path.put((float) (pt.x - this.referenceCenter.x));
                path.put((float) (pt.y - this.referenceCenter.y));
                path.put((float) (pt.z - this.referenceCenter.z));
            }
        }
        else
        {
            for (Position pos : positions)
            {
                Vec4 pt = globe.computePointFromPosition(pos);
                path.put((float) (pt.x - this.referenceCenter.x));
                path.put((float) (pt.y - this.referenceCenter.y));
                path.put((float) (pt.z - this.referenceCenter.z));
            }
        }

        if (this.isExtrude())
            this.appendTerrainPoints(dc, positions, path);

        return path;
    }

    /**
     * Adds to a spcecified path the corresponding points on the terrain. Used to generate extrusion vertices.
     *
     * @param dc        the current draw context.
     * @param positions the path positions.
     * @param path      the path to append to. Assumes that the path has adequate capacity.
     */
    protected void appendTerrainPoints(DrawContext dc, Iterable<? extends Position> positions, FloatBuffer path)
    {
        for (Position pos : positions)
        {
            Vec4 pt = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), 0d);
            path.put((float) (pt.x - this.referenceCenter.x));
            path.put((float) (pt.y - this.referenceCenter.y));
            path.put((float) (pt.z - this.referenceCenter.z));
        }

        this.hasExtrusionPoints = true;
    }

    /**
     * Generates positions defining the path with path type and terrain-conforming properties applied. Builds the path's
     * <code>tessellatedPositions</code> and <code>polePositions</code> fields.
     *
     * @param dc the current draw context.
     */
    protected void makeTessellatedPositions(DrawContext dc)
    {
        if (this.numPositions < 2)
            return;

        if (this.tessellatedPositions == null)
        {
            int size = (this.numSubsegments * (this.numPositions - 1) + 1) * (this.isExtrude() ? 2 : 1);
            this.tessellatedPositions = new ArrayList<Position>(size);
        }
        else
        {
            this.tessellatedPositions.clear();
        }

        this.polePositions.clear();

        Iterator<? extends Position> iter = this.positions.iterator();
        Position posA = iter.next();
        this.addPosition(posA, true); // add the first position of the path

        // If the entire path is very small, just create a two-point path with the first and last path points
        Vec4 ptA = this.computePoint(dc, posA);
        Vec4 ptB = this.endPosition != null && !this.endPosition.equals(posA) // TODO: do better test than this
            ? this.computePoint(dc, this.endPosition) : null;
        if (ptB != null && this.isSmall(dc, ptA, ptB, 8))
        {
            this.addPosition(this.endPosition, true);
            return;
        }

        // Tessellate each segment of the path.
        for (int i = 1; i <= this.numPositions; i++)
        {
            Position posB;
            if (i < this.numPositions)
                posB = iter.next();
            else
                break;

            ptB = this.computePoint(dc, posB);

            // If the segment is very small or not visible, don't tessellate it, just add the segment's end position.
            if (this.isSmall(dc, ptA, ptB, 8) || !this.isSegmentVisible(dc, posA, posB, ptA, ptB))
                this.addPosition(posB, true);
            else
                this.makeSegment(dc, posA, posB, ptA, ptB);

            posA = posB;
            ptA = ptB;
        }
    }

    /**
     * Adds a position to the path's <code>tesselatedPositions</code> list and optionally its <code>polePositions</code>
     * list.
     *
     * @param pos          the position to add.
     * @param polePosition if true, add the positions index to the <code>polePositions</code>.
     */
    protected void addPosition(Position pos, boolean polePosition)
    {
        if (polePosition)
            this.polePositions.add(this.tessellatedPositions.size());
        this.tessellatedPositions.add(pos);
    }

    /**
     * Determines whether the segment between two path positions is visible.
     *
     * @param dc   the current draw context.
     * @param posA the segment's first position.
     * @param posB the segment's second position.
     * @param ptA  the model-coordinate point corresponding to the segment's first position.
     * @param ptB  the model-coordinate point corresponding to the segment's second position.
     *
     * @return true if the segment is visible relative to the current view frustum, otherwise false.
     */
    protected boolean isSegmentVisible(DrawContext dc, Position posA, Position posB, Vec4 ptA, Vec4 ptB)
    {
        Frustum f = dc.getView().getFrustumInModelCoordinates();

        if (f.contains(ptA))
            return true;

        if (f.contains(ptB))
            return true;

        if (ptA.equals(ptB))
            return false;

        Position posC = Position.interpolateRhumb(0.5, posA, posB);
        Vec4 ptC = this.computePoint(dc, posC);
        if (f.contains(ptC))
            return true;

        double r = Line.distanceToSegment(ptA, ptB, ptC);
        Cylinder cyl = new Cylinder(ptA, ptB, r == 0 ? 1 : r);
        return cyl.intersects(dc.getView().getFrustumInModelCoordinates());
    }

    /**
     * Creates the interior segment positions to adhere to the current path type and terrain-following settings.
     *
     * @param dc   the current draw context.
     * @param posA the segment's first position.
     * @param posB the segment's second position.
     * @param ptA  the model-coordinate point corresponding to the segment's first position.
     * @param ptB  the model-coordinate point corresponding to the segment's second position.
     */
    @SuppressWarnings({"StringEquality"})
    protected void makeSegment(DrawContext dc, Position posA, Position posB, Vec4 ptA, Vec4 ptB)
    {
        // This method does not add the first position of the segment to the position list. It adds only the
        // subsequent positions, including the segment's last position.

        double arcLength =
            this.getPathType() == AVKey.LINEAR ? ptA.distanceTo3(ptB) : this.computeSegmentLength(dc, posA, posB);
        if (arcLength <= 0 || (this.getPathType() == AVKey.LINEAR && !this.isFollowTerrain()))
        {
            if (!ptA.equals(ptB))
                this.addPosition(posB, true);
            return;
        }

        // Variables for great circle and rhumb computation.
        Angle segmentAzimuth = null;
        Angle segmentDistance = null;

        for (double s = 0, p = 0; s < 1;)
        {
            if (this.followTerrain)
                p += this.terrainConformance * dc.getView().computePixelSizeAtDistance(
                    ptA.distanceTo3(dc.getView().getEyePoint()));
            else
                p += arcLength / this.numSubsegments;

            Position pos;

            s = p / arcLength;
            if (s >= 1)
            {
                pos = posB;
            }
            else if (this.pathType == AVKey.RHUMB_LINE || this.pathType == AVKey.LINEAR) // or LOXODROME
            {
                if (segmentAzimuth == null)
                {
                    segmentAzimuth = LatLon.rhumbAzimuth(posA, posB);
                    segmentDistance = LatLon.rhumbDistance(posA, posB);
                }
                Angle distance = Angle.fromRadians(s * segmentDistance.radians);
                LatLon latLon = LatLon.rhumbEndPosition(posA, segmentAzimuth, distance);
                pos = new Position(latLon, (1 - s) * posA.getElevation() + s * posB.getElevation());
            }
            else // GREAT_CIRCLE
            {
                if (segmentAzimuth == null)
                {
                    segmentAzimuth = LatLon.greatCircleAzimuth(posA, posB);
                    segmentDistance = LatLon.greatCircleDistance(posA, posB);
                }
                Angle distance = Angle.fromRadians(s * segmentDistance.radians);
                LatLon latLon = LatLon.greatCircleEndPosition(posA, segmentAzimuth, distance);
                pos = new Position(latLon, (1 - s) * posA.getElevation() + s * posB.getElevation());
            }

            this.addPosition(pos, s >= 1);

            ptA = ptB;
        }
    }

    /**
     * Computes the model-coordinate, great-circle length between two positions.
     *
     * @param dc   the current draw context.
     * @param posA the first position.
     * @param posB the second position.
     *
     * @return the distance between the positions.
     */
    protected double computeSegmentLength(DrawContext dc, Position posA, Position posB)
    {
        LatLon llA = new LatLon(posA.getLatitude(), posA.getLongitude());
        LatLon llB = new LatLon(posB.getLatitude(), posB.getLongitude());

        Angle ang = LatLon.greatCircleDistance(llA, llB);

        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
            return ang.radians * (dc.getGlobe().getRadius());

        double height = 0.5 * (posA.getElevation() + posB.getElevation());
        return ang.radians * (dc.getGlobe().getRadius() + height * dc.getVerticalExaggeration());
    }

    /**
     * Computes and assigns the path's reference center.
     *
     * @param dc the current draw context.
     */
    protected void computeReferenceCenter(DrawContext dc)
    {
        if (this.positions == null)
            return;

        Position pos = this.getReferencePosition();
        if (pos == null)
            return;

        this.referenceCenter = dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
            dc.getVerticalExaggeration() * pos.getAltitude());
    }

    /**
     * Computes the path's bounding box from the current rendering path. Assumes the rendering path is up-to-date.
     *
     * @return the computed extent.
     */
    protected Extent computeExtent()
    {
        if (this.renderedPath == null)
            return null;

        this.renderedPath.rewind();
        Box box = Box.computeBoundingBox(new BufferWrapper.FloatBufferWrapper(this.renderedPath));

        // The path points are relative to the reference center, so translate the extent to the reference center.
        box = box.translate(this.referenceCenter);

        return box;
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        // First try to get the one computed from the rendered path.
        Extent ext = this.getExtent() != null ? this.getExtent() : this.computeExtent();
        if (ext != null)
            return ext;

        // Must compute it from positions.

        if (this.getPositions() == null)
            return null;

        Iterable<? extends Position> posits =
            this.tessellatedPositions != null ? this.tessellatedPositions : this.getPositions();
        Sector sector = Sector.boundingSector(posits);
        if (sector == null)
            return null;

        double[] minAndMaxElevations = globe.getMinAndMaxElevations(sector);
        if (this.getAltitudeMode() != WorldWind.CLAMP_TO_GROUND)
        {
            double[] extremes = new double[] {Double.MAX_VALUE, -Double.MAX_VALUE};
            for (Position pos : positions)
            {
                double elevation = pos.getElevation();
                if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
                    elevation += minAndMaxElevations[1];

                if (extremes[0] > elevation)
                    extremes[0] = elevation * verticalExaggeration; // min
                if (extremes[1] < elevation)
                    extremes[1] = elevation * verticalExaggeration; // max
            }
        }

        ext = Sector.computeBoundingBox(globe, verticalExaggeration, sector, minAndMaxElevations[0],
            minAndMaxElevations[1]);
        this.setExtent(ext);

        return ext;
    }

    /**
     * Computes a model-coordinate point from a position, applying the path's altitude mode.
     *
     * @param dc  the current draw context.
     * @param pos the position to compute a point for.
     *
     * @return the model-coordinate point corresponding to the position and the path's path type.
     */
    protected Vec4 computePoint(DrawContext dc, Position pos)
    {
        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
            return dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), 0d);
        else if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            return dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), pos.getElevation());

        double height = pos.getElevation() * dc.getVerticalExaggeration();
        return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), height);
    }

    /**
     * Computes the path's reference position. The position returned is the center-most ordinal position in the path's
     * specified positions.
     *
     * @return the computed reference position.
     */
    public Position getReferencePosition()
    {
        if (this.numPositions < 1)
        {
            return null;
        }
        else if (this.numPositions < 3)
        {
            return this.positions.iterator().next();
        }
        else
        {
            int n = this.numPositions / 2;
            Iterator<? extends Position> iter = this.positions.iterator();
            Position pos = iter.next();
            for (int i = 1; i < n; i++)
            {
                pos = iter.next();
            }

            return pos;
        }
    }

    public void move(Position delta)
    {
        if (delta == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position refPos = this.getReferencePosition();

        // The reference position is null if this Path has no positions. In this case moving the Path by a
        // relative delta is meaningless because the Path has no geographic location. Therefore we fail softly by
        // exiting and doing nothing.
        if (refPos == null)
            return;

        this.moveTo(refPos.add(delta));
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.numPositions == 0)
            return;

        Position oldRef = this.getReferencePosition();

        // The reference position is null if this Path has no positions. In this case moving the Path to a new
        // reference position is meaningless because the Path has no geographic location. Therefore we fail softly
        // by exiting and doing nothing.
        if (oldRef == null)
            return;

        ArrayList<Position> newPositions = new ArrayList<Position>(this.numPositions);

        double elevDelta = position.getElevation() - oldRef.getElevation();

        for (Position pos : this.positions)
        {
            Angle distance = LatLon.greatCircleDistance(oldRef, pos);
            Angle azimuth = LatLon.greatCircleAzimuth(oldRef, pos);
            LatLon newLocation = LatLon.greatCircleEndPosition(position, azimuth, distance);
            double newElev = pos.getElevation() + elevDelta;

            newPositions.add(new Position(newLocation, newElev));
        }

        this.setPositions(newPositions);
    }

    protected boolean isSmall(DrawContext dc, Vec4 ptA, Vec4 ptB, int numPixels)
    {
        return ptA.distanceTo3(ptB) <= numPixels * dc.getView().computePixelSizeAtDistance(
            dc.getView().getEyePoint().distanceTo3(ptA));
    }

    /** {@inheritDoc} */
    public String isExportFormatSupported(String mimeType)
    {
        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(mimeType))
            return Exportable.FORMAT_SUPPORTED;
        else
            return Exportable.FORMAT_NOT_SUPPORTED;
    }

    public void export(String mimeType, Object output) throws IOException, UnsupportedOperationException
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
     * Export the path to KML as a {@code <Placemark>} element. The {@code output} object will receive the data. This
     * object must be one of: java.io.Writer java.io.OutputStream javax.xml.stream.XMLStreamWriter
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
        xmlWriter.writeCharacters(kmlBoolean(this.isVisible()));
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
        xmlWriter.writeStartElement("LineString");

        xmlWriter.writeStartElement("extrude");
        xmlWriter.writeCharacters(kmlBoolean(isExtrude()));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("tessellate");
        xmlWriter.writeCharacters(kmlBoolean(isFollowTerrain()));
        xmlWriter.writeEndElement();

        final String altitudeMode = KMLExportUtil.kmlAltitudeMode(getAltitudeMode());
        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters(altitudeMode);
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("coordinates");
        for (Position position : this.positions)
        {
            xmlWriter.writeCharacters(String.format("%f,%f,%f ",
                position.getLongitude().getDegrees(),
                position.getLatitude().getDegrees(),
                position.getElevation()));
        }
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement(); // LineString
        xmlWriter.writeEndElement(); // Placemark

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    }
}
