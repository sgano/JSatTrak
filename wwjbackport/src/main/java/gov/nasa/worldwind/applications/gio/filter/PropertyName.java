/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.filter;

import gov.nasa.worldwind.applications.gio.xml.TextElement;
import gov.nasa.worldwind.applications.gio.xml.xmlns;

/**
 * @author dcollins
 * @version $Id: PropertyName.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class PropertyName extends TextElement implements Expression
{
    public PropertyName(String propertyName)
    {
        super(xmlns.ogc, "PropertyName", propertyName);
    }
}
