/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.avlist.AVListImpl;

/**
 * @author dcollins
 * @version $Id: ResultModel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ResultModel extends AVListImpl
{
    public ResultModel()
    {
    }

    public void firePropertyChange()
    {
        firePropertyChange(CatalogKey.RESULT_MODEL, null, this);
    }
}
