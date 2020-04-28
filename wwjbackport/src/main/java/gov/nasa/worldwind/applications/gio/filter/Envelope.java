/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.filter;

import gov.nasa.worldwind.applications.gio.xml.Element;
import gov.nasa.worldwind.applications.gio.xml.xmlns;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: Envelope.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class Envelope extends Element
{
    public Envelope()
    {
        super(xmlns.gml, "Envelope");
    }

    public LowerCorner addLowerCorner(Angle latitude, Angle longitude) throws Exception
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LowerCorner lc = new LowerCorner(latitude, longitude);
        addElement(lc);
        return lc;
    }

    public UpperCorner addUpperCorner(Angle latitude, Angle longitude) throws Exception
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        UpperCorner uc = new UpperCorner(latitude, longitude);
        addElement(uc);
        return uc;
    }
}
