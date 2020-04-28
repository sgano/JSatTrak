/* Copyright (C) 2001, 2009 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.ShapeAttributes;

/**
 * @author dcollins
 * @version $Id: Record.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public interface Record extends AVList
{
    String getIdentifier();

    String getTitle();

    String getType();

    Sector getSector();

    long getModifiedTime();

    String getAbstract();

    OnlineResource getResource(String key);

    Iterable<OnlineResource> getResources();

    Iterable<LatLon> getLocations();

    ShapeAttributes getShapeAttributes();
}
