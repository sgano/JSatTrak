/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render.markers;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: BasicMarker.java 12576 2009-09-10 16:17:20Z jterhorst $
 */
public class BasicMarker implements Marker
{
    private Position position; // may be null
    private Angle heading; // may be null

    // To avoid the memory overhead of creating an attibutes object for every new marker, attributes are
    // required to be specified at construction.
    private MarkerAttributes attributes;

    public BasicMarker(Position position, MarkerAttributes attrs)
    {
        if (attrs == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;
        this.attributes = attrs;
    }

    public BasicMarker(Position position, MarkerAttributes attrs, Angle heading)
    {
        if (attrs == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;
        this.heading = heading;
        this.attributes = attrs;
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    public Angle getHeading()
    {
        return this.heading;
    }

    public void setHeading(Angle heading)
    {
        this.heading = heading;
    }

    public MarkerAttributes getAttributes()
    {
        return attributes;
    }

    public void setAttributes(MarkerAttributes attributes)
    {
        this.attributes = attributes;
    }

    public void render(DrawContext dc, Vec4 point, double radius, boolean isRelative)
    {
        this.attributes.getShape(dc).render(dc, this, point, radius, isRelative);
    }

    public void render(DrawContext dc, Vec4 point, double radius)
    {
        this.attributes.getShape(dc).render(dc, this, point, radius, false);
    }
}
