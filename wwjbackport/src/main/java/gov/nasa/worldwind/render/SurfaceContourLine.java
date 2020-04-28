/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Renders a contour line on the terrain at a given elevation. The controur line extent can be bounded by a
 * <code>Sector</code>.
 *
 * @author Patrick Murris
 * @version $Id: SurfaceContourLine.java 13585 2010-07-27 03:18:16Z dcollins $
 */
public class SurfaceContourLine extends ContourLine implements PreRenderable
{
    protected List<SurfaceShape> surfaceShapes = new ArrayList<SurfaceShape>();
    protected ShapeAttributes attributes = new BasicShapeAttributes();

    public SurfaceContourLine()
    {
        super();

        this.attributes.setDrawInterior(false);
        this.attributes.setOutlineMaterial(new Material(this.getColor()));
        this.attributes.setOutlineWidth(this.getLineWidth());
    }

    public ShapeAttributes getAttributes()
    {
        return this.attributes;
    }

    public void setAttributes(ShapeAttributes attributes)
    {
        this.attributes = attributes;
    }

    public double getLineWidth()
    {
        return this.attributes.getOutlineWidth();
    }

    public void setLineWidth(double width)
    {
        this.attributes.setOutlineWidth(width);
    }

    public Color getColor()
    {
        return this.attributes.getOutlineMaterial().getDiffuse();
    }

    public void setColor(Color color)
    {
        this.attributes.setOutlineMaterial(new Material(color));
    }

    public List<SurfaceShape> getSurfaceShapes(DrawContext dc)
    {
        return this.isValid(dc) ? this.surfaceShapes : null;
    }

    public void preRender(DrawContext dc)
    {
        if (!this.isValid(dc))
            return;

        for (SurfaceShape shape : this.surfaceShapes)
        {
            shape.preRender(dc);
        }
    }

    public void render(DrawContext dc)
    {
        if (!this.isValid(dc))
            return;

        for (SurfaceShape shape : this.surfaceShapes)
        {
            shape.render(dc);
        }
    }

    protected boolean isValid(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isEnabled())
            return false;

        if (!this.getSector().intersects(dc.getVisibleSector()))
            return false;

        if (this.expirySupport.isExpired(dc))
        {
            this.makeContourLine(dc);
            this.expirySupport.updateExpiryCriteria(dc);
        }

        return this.surfaceShapes.size() > 0;
    }

    protected void makeContourLine(DrawContext dc)
    {
        super.makeContourLine(dc);

        // Create surface polylines using the original polylines positions
        this.surfaceShapes.clear();
        for (Renderable r : this.getRenderables())
        {
            if (r instanceof Polyline)
                this.surfaceShapes.add(new SurfacePolyline(this.attributes, ((Polyline) r).getPositions()));
        }
    }
}
