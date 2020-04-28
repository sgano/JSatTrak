/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.ebrim.ExternalLink;
import gov.nasa.worldwind.avlist.AVListImpl;

/**
 * @author dcollins
 * @version $Id: ServiceDataLink.java 5472 2008-06-26 20:11:53Z dcollins $
 */
public class ServiceDataLink extends AVListImpl
{
    private ExternalLink externalLink;

    public ServiceDataLink()
    {
    }

    public ExternalLink getExternalLink()
    {
        return this.externalLink;
    }

    public void setExternalLink(ExternalLink externalLink)
    {
        this.externalLink = externalLink;
    }
}
