package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.*;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

/**
 * Renders a {@link GlobeAnnotation} with a casted shadow over the terrain.
 *
 * @author Patrick Murris
 * @version $Id: AnnotationShadow.java 13585 2010-07-27 03:18:16Z dcollins $
 */
public class AnnotationShadow extends GlobeAnnotation implements PreRenderable
{
    protected SurfacePolygon shadowShape;
    private double shadowOpacity = .3;

    public AnnotationShadow(String text, Position position)
    {
        super(text, position);
    }

    public double getShadowOpacity()
    {
        return this.shadowOpacity;
    }

    public void setShadowOpacity(double opacity)
    {
        this.shadowOpacity = opacity < 0 ? 0 : opacity > 1 ? 1 : opacity; // clamp to 0..1
        if (this.shadowShape != null)
        {
            ShapeAttributes attr = this.shadowShape.getAttributes();
            attr.setInteriorOpacity(this.shadowOpacity);
            attr.setOutlineOpacity(this.shadowOpacity / 2);
            this.shadowShape.setAttributes(attr);
        }
    }

    // Note that at the pre render stage, the annotation has not been drawn and we don't know yet it's
    // callout dimensions for this frame. The shadow is based on the previous frame.

    public void preRender(DrawContext dc)
    {
        // Pre render surface polygon
        if (this.shadowShape != null)
            this.shadowShape.preRender(dc);
    }

    public void render(DrawContext dc)
    {
        // TODO: determine whether the annotation will draw so as to hide the shadow if not
        // Render surface polygon
        if (this.shadowShape != null)
            this.shadowShape.render(dc);

        // Render annotation
        super.render(dc);
    }

    protected void applyScreenTransform(DrawContext dc, int x, int y, int width, int height, double scale)
    {
        super.applyScreenTransform(dc, x, y, width, height, scale);

        // Determine call out shape in model coordinates
        // Project shadow on the ground to determine surface polygon path
        // TODO: move shadow computation to pre render stage
        ArrayList<LatLon> locations = computeShadowLocations(dc, width, height, scale);
        if (locations == null)
            return;

        // Update surface polygon
        if (this.shadowShape == null)
        {
            this.shadowShape = new SurfacePolygon(locations);
            ShapeAttributes attr = new BasicShapeAttributes();
            attr.setInteriorMaterial(Material.BLACK);
            attr.setOutlineMaterial(Material.BLACK);
            attr.setInteriorOpacity(shadowOpacity);
            attr.setOutlineOpacity(shadowOpacity / 2);
            this.shadowShape.setAttributes(attr);
        }
        else
            this.shadowShape.setLocations(locations);
    }

    protected ArrayList<LatLon> computeShadowLocations(DrawContext dc, int width, int height, double scale)
    {
        Globe globe = dc.getGlobe();
        DoubleBuffer vertices = getCalloutVertices(width, height);
        Matrix transform = computeCalloutTransform(dc, width, height, scale, this.getPosition());

        ArrayList<LatLon> locations = new ArrayList<LatLon>();
        int numVertices = vertices.limit() / 2;
        int idx = 0;
        for (int i = 0; i < numVertices; i++)
        {
            // The transform will make the callout 'flat' or parallel to the ground, we just determine the
            // LatLon locations of it's vertices.
            Vec4 vert = new Vec4(vertices.get(idx++), vertices.get(idx++), 0);
            vert = vert.transformBy4(transform);
            // Determine vertice LatLon
            Position pos = globe.computePositionFromPoint(vert);
            locations.add(pos);
        }

        return locations;
    }

    protected DoubleBuffer getCalloutVertices(int width, int height)
    {
        Point offset = this.getAttributes().getDrawOffset();
        String shape = this.getAttributes().getFrameShape();
        String leader = this.getAttributes().getLeader();
        int leaderGapWidth = this.getAttributes().getLeaderGapWidth();
        int cornerRadius = this.getAttributes().getCornerRadius();

        // Compute callout dimensions and leader offset
        Point shapeLeaderOffset = new Point(width / 2 - offset.x, -offset.y);

        // The callout vertices are drawn in pixels in the x/y plane, the lower left corner at the origin.
        // The leader is pointing toward negative y.
        return leader.equals(FrameFactory.LEADER_TRIANGLE) ?
            FrameFactory.createShapeWithLeaderBuffer(shape, width, height, shapeLeaderOffset, leaderGapWidth,
                cornerRadius, null) :
            FrameFactory.createShapeBuffer(shape, width, height, cornerRadius, null);
    }

    protected Matrix computeCalloutTransform(DrawContext dc, int width, int height, double scale, Position position)
    {
        Point offset = this.getAttributes().getDrawOffset();
        Point shapeLeaderOffset = new Point(width / 2 - offset.x, -offset.y);

        double pixelSize = getPixelSizeAtLocation(dc, position);
        Matrix transform = Matrix.IDENTITY;
        // This will scale the callout to the apparent dimensions of the annotation and move it to
        // the given position. The callout ends up 'flat' over the ground, behind the annotation, it's leader
        // pointing to the position.
        transform = transform.multiply(dc.getGlobe().computeModelCoordinateOriginTransform(position));
        transform = transform.multiply(Matrix.fromScale(pixelSize * scale));  // scale from pixels to meter
        transform = transform.multiply(Matrix.fromRotationZ(getDrawHeading(dc).multiply(-1)));
        transform = transform.multiply(Matrix.fromTranslation(-shapeLeaderOffset.x, -shapeLeaderOffset.y, 0));

        return transform;
    }

    protected double getPixelSizeAtLocation(DrawContext dc, LatLon location)
    {
        Globe globe = dc.getGlobe();
        Vec4 locationPoint = globe.computePointFromPosition(location.getLatitude(), location.getLongitude(),
            globe.getElevation(location.getLatitude(), location.getLongitude()));
        double distance = dc.getView().getEyePoint().distanceTo3(locationPoint);
        return dc.getView().computePixelSizeAtDistance(distance);
    }

    protected Angle getDrawHeading(DrawContext dc)
    {
        Angle heading = Angle.ZERO;
        if (dc.getView() instanceof OrbitView)
            heading = dc.getView().getHeading();
        return heading;
    }
}
