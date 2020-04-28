/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogException;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogExceptionList;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.avlist.AVListImpl;

/**
 * @author dcollins
 * @version $Id: Layer.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class Layer extends AVListImpl
{
    private String name;
    private String style;
    private Server server;
    private CatalogExceptionList exceptionList;

    public Layer()
    {
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStyle()
    {
        return this.style;
    }

    public void setStyle(String style)
    {
        this.style = style;
    }

    public Server getServer()
    {
        return this.server;
    }

    public void setServer(Server server)
    {
        this.server = server;
    }

    public CatalogExceptionList getExceptionList()
    {
        return this.exceptionList;
    }

    public void setExceptionList(CatalogExceptionList exceptionList)
    {
        this.exceptionList = exceptionList;
    }

    public void addException(CatalogException e)
    {
        if (this.exceptionList == null)
        {
            this.exceptionList = new CatalogExceptionList();
            if (getValue(CatalogKey.EXCEPTIONS) == null)
                setValue(CatalogKey.EXCEPTIONS, this.exceptionList);
        }
        this.exceptionList.addException(e);
    }
}
