/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.filter;

import gov.nasa.worldwind.applications.gio.xml.Element;
import gov.nasa.worldwind.applications.gio.xml.xmlns;

/**
 * @author dcollins
 * @version $Id: LogicalOperator.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public abstract class LogicalOperator extends Element
{
    protected LogicalOperator(String name)
    {
        super(xmlns.ogc, name);
    }
}
