/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.filter.Filter;
import gov.nasa.worldwind.applications.gio.xml.Element;
import gov.nasa.worldwind.applications.gio.xml.xmlns;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id: Constraint.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class Constraint extends Element
{
    public Constraint()
    {
        super(xmlns.csw, "Constraint");
    }

    public void setVersion(String version)
    {
        if (version == null)
        {
            String message = "nullValue.VersionIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("version", version);
    }

    public Filter addFilter() throws Exception
    {
        Filter filter = new Filter();
        this.addElement(filter);
        return filter;
    }
}
