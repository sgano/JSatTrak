/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Sector;

/**
 * An interface for objects that can provide an extent in latitude and longitude.
 *
 * @author tag
 * @version $Id: GeographicExtent.java 13173 2010-03-03 18:00:47Z tgaskins $
 */
public interface GeographicExtent extends Renderable//, AVList
{
    /**
     * Returns the object's geographic extent.
     *
     * @return the object's geographic extent.
     */
    Sector getSector();
}
