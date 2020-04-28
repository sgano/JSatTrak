/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.*;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

/**
 * @author dcollins
 * @version $Id: ESGResultModel.java 13375 2010-05-05 23:43:39Z dcollins $
 */
public class ESGResultModel extends ResultModel
{
    private ServicePackage servicePackage;
    private WMSCapabilities capabilities;
    private CatalogExceptionList exceptionList;

    public ESGResultModel()
    {
    }

    public ServicePackage getServicePackage()
    {
        return this.servicePackage;
    }

    public void setServicePackage(ServicePackage servicePackage)
    {
        this.servicePackage = servicePackage;
    }

    public WMSCapabilities getCapabilities()
    {
        return this.capabilities;
    }

    public void setCapabilities(WMSCapabilities capabilities)
    {
        this.capabilities = capabilities;
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
