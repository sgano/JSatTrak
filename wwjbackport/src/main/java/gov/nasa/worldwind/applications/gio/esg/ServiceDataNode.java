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
 * @version $Id: ServiceDataNode.java 5551 2008-07-18 23:50:47Z dcollins $
 */
public class ServiceDataNode extends AVListNode<ServiceData>
{
    public ServiceDataNode(ServiceData serviceData)
    {
        super(serviceData);

        update();
        setSortChildren(true);
        setSortKey(CatalogKey.TITLE);
    }

    public void update()
    {
        setLinks(getObject().getLinkIterator());
    }

    protected void setLinks(Iterator<ServiceDataLink> iter)
    {
        List<AVListNode> children = null;
        while (iter.hasNext())
        {
            ServiceDataLink link = iter.next();
            if (link != null)
            {
                if (children == null)
                    children = new ArrayList<AVListNode>();
                children.add(new ServiceDataLinkNode(link));
            }
        }

        boolean hasChildren = children != null && children.size() > 0;
        doSetChildren(children);
        setAllowsChildren(hasChildren);
        setLeaf(!hasChildren);
    }
}
