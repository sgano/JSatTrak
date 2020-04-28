/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.catalogui.*;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

/**
 * @author dcollins
 * @version $Id: GIDBResultModel.java 13375 2010-05-05 23:43:39Z dcollins $
 */
public class GIDBResultModel extends ResultModel
{
    private Server server;
    private WMSCapabilities capabilities;
    private LayerList layerList;
    private CatalogExceptionList exceptionList;    

    public GIDBResultModel()
    {
    }

    public Server getServer()
    {
        return this.server;
    }

    public void setServer(Server server)
    {
        this.server = server;
    }

    public WMSCapabilities getCapabilities()
    {
        return this.capabilities;
    }

    public void setCapabilities(WMSCapabilities capabilities)
    {
        this.capabilities = capabilities;
    }

    public LayerList getLayerList()
    {
        return this.layerList;
    }

    public void setLayerList(LayerList layerList)
    {
        this.layerList = layerList;
    }

    public void addLayer(Layer layer)
    {
        if (this.layerList == null)
            this.layerList = new LayerListImpl();
        this.layerList.addLayer(layer);
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
