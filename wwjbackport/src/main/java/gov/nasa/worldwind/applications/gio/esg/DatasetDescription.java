/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.ebrim.ExtrinsicObject;

/**
 * @author dcollins
 * @version $Id: DatasetDescription.java 5472 2008-06-26 20:11:53Z dcollins $
 */
public class DatasetDescription
{
    private ExtrinsicObject extrinsicObject;

    public DatasetDescription()
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
}
