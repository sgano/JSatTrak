/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: ElementSetType.java 5465 2008-06-24 00:17:03Z dcollins $
 */
public class ElementSetType
{
    private String type;
    public static final ElementSetType BRIEF = new ElementSetType("brief");
    public static final ElementSetType SUMMARY = new ElementSetType("summary");
    public static final ElementSetType FULL = new ElementSetType("full");

    protected ElementSetType(String type)
    {
        if (type == null)
        {
            String message = "csw.TypeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }

    public String toString()
    {
        return getType();
    }
}
