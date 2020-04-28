/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.AVListNode;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: ESGResultNode.java 5551 2008-07-18 23:50:47Z dcollins $
 */
public class ESGResultNode extends AVListNode<ESGResultModel>
{
    public ESGResultNode(ESGResultModel resultModel)
    {
        super(resultModel);

        setAllowsChildren(true);
        setLeaf(false);
        setSortChildren(true);
        setSortKey(CatalogKey.TITLE);
        update();
    }

    public void update()
    {
        ServicePackage servicePackage = getObject().getServicePackage();
        if (servicePackage != null)
        {
            setServiceData(servicePackage.getServiceDataIterator());
        }
    }

    protected void setServiceData(Iterator<ServiceData> iter)
    {
        List<AVListNode> children = null;
        while (iter.hasNext())
        {
            ServiceData serviceData = iter.next();
            if (serviceData != null)
            {
                if (children == null)
                    children = new ArrayList<AVListNode>();
                children.add(new ServiceDataNode(serviceData));
            }
        }
        doSetChildren(children);
    }
}
