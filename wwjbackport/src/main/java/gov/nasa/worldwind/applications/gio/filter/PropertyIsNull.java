/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.filter;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: PropertyIsNull.java 5466 2008-06-24 02:17:32Z dcollins $
 */
public class PropertyIsNull extends ComparisonOperator
{
    public PropertyIsNull()
    {
        super("PropertyIsNull");
    }

    public PropertyName addPropertyName(String propertyName) throws Exception
    {
        if (propertyName == null)
        {
            String message = "nullValue.PropertyNameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        PropertyName pn = new PropertyName(propertyName);
        addElement(pn);
        return pn;
    }
}
