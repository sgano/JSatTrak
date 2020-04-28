/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;

/**
 * @author tag
 * @version $Id: BasicPointOfInterest.java 8478 2009-01-16 19:17:42Z jparsons $
 */
public class BasicPointOfInterest extends WWObjectImpl implements PointOfInterest
{
    public BasicPointOfInterest(LatLon latlon)
    {
        this.latlon = latlon;
    }

    protected final LatLon latlon;

    public LatLon getLatlon()
    {
        return latlon;
    }

    public String toString()
    {
        String str = this.getStringValue(AVKey.DISPLAY_NAME);
        if (str != null)
            return str;
        else
            return latlon.toString();
    }
}