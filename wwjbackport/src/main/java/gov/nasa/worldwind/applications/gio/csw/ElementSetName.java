/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.xml.TextElement;
import gov.nasa.worldwind.applications.gio.xml.xmlns;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id: ElementSetName.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ElementSetName extends TextElement
{
    public ElementSetName(String elementSetType)
    {
        super(xmlns.csw, "ElementSetName", elementSetType);
    }

    public void setTypeNames(String typeNames)
    {
        if (typeNames == null)
        {
            String message = "csw.TypeNamesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        setAttribute("typeNames", typeNames);        
    }
}
