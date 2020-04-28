/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.xml.Element;
import gov.nasa.worldwind.applications.gio.xml.xmlns;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id: Query.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class Query extends Element
{
    public Query(String typeNames)
    {
        super(xmlns.csw, "Query");
        
        if (typeNames == null)
        {
            String message = "csw.TypeNamesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        setTypeNames(typeNames);
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

    public ElementSetName addElementSetName(ElementSetType elementSetType) throws Exception
    {
        if (elementSetType == null)
        {
            String message = "nullValue.ElementSetTypeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ElementSetName el = new ElementSetName(elementSetType.getType());
        addElement(el);
        return el;
    }

    public ElementName addElementName(String name) throws Exception
    {
        if (name == null)
        {
            String message = "nullValue.NameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ElementName el = new ElementName(name);
        addElement(el);
        return el;
    }

    public Constraint addConstraint() throws Exception
    {
        Constraint el = new Constraint();
        addElement(el);
        return el;
    }
}
