/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.ebrim.ExtrinsicObject;

/**
 * @author dcollins
 * @version $Id: Geometry.java 5472 2008-06-26 20:11:53Z dcollins $
 */
public class Geometry
{
    private ExtrinsicObject extrinsicObject;
    private Object extents;

    public Geometry()
    {
    }

    public ExtrinsicObject getExtrinsicObject()
    {
        return this.extrinsicObject;
    }

    public void setExtrinsicObject(ExtrinsicObject extrinsicObject)
    {
        this.extrinsicObject = extrinsicObject;
    }

    public Object getExtents()
    {
        return this.extents;
    }

    public void setExtents(Object extents)
    {
        this.extents = extents;
    }
}
