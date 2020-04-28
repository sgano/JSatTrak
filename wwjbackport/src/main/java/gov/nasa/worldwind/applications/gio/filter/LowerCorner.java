/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.filter;

import gov.nasa.worldwind.applications.gio.xml.TextElement;
import gov.nasa.worldwind.applications.gio.xml.xmlns;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: LowerCorner.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class LowerCorner extends TextElement
{
    public LowerCorner(Angle latitude, Angle longitude)
    {
        super(xmlns.gml, "lowerCorner", latLonToLowerCorner(latitude, longitude));
    }

    protected static String latLonToLowerCorner(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(latitude.degrees);
        sb.append(" ");
        sb.append(longitude.degrees);
        return sb.toString();
    }
}