/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.geom.*;

/**
 * @author tag
 * @version $Id: PointOfInterest.java 8393 2009-01-10 05:36:05Z tgaskins $
 */
public interface PointOfInterest extends WWObject
{
    LatLon getLatlon();
}
