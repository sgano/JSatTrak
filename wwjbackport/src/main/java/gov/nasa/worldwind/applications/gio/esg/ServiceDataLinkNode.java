/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.AVListNode;

/**
 * @author dcollins
 * @version $Id: ServiceDataLinkNode.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ServiceDataLinkNode extends AVListNode<ServiceDataLink>
{
    public ServiceDataLinkNode(ServiceDataLink serviceDataLink)
    {
        super(serviceDataLink);

        setAllowsChildren(false);
        setLeaf(true);
    }
}
