/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render.markers;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;

/**
 * @author tag
 * @version $Id: Marker.java 12576 2009-09-10 16:17:20Z jterhorst $
 */
public interface Marker
{
    void render(DrawContext dc, Vec4 point, double radius, boolean isRelative);

    void render(DrawContext dc, Vec4 point, double radius);

    Position getPosition();

    void setPosition(Position position);

    MarkerAttributes getAttributes();

    void setAttributes(MarkerAttributes attributes);

    Angle getHeading();

    void setHeading(Angle heading);

}
