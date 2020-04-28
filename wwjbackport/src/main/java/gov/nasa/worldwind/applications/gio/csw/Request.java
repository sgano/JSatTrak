/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.xml.Element;
import gov.nasa.worldwind.applications.gio.xml.xmlns;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id: Request.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class Request extends Element
{
    private static final xmlns[] GIS_NAMESPACES = {
            xmlns.ows, xmlns.ogc, xmlns.rim, xmlns.csw, xmlns.gml, xmlns.dc, xmlns.dct, xmlns.xlink, xmlns.xsi
    };

    public Request(xmlns ns, String name)
    {
        super(ns, name);

        setService("CSW");
        setVersion("2.0.2");

        for (xmlns namespace : GIS_NAMESPACES)
        {
            setAttribute("xmlns:" + namespace.getPrefix(), namespace.getUrl());
        }
        // set defualt namespace
        setDefaultNamespace(ns);
    }

    public void setService(String service)
    {
        if (service == null)
        {
            String message = "nullValue.ServiceIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("service", service);
    }

    public void setVersion(String version)
    {
        if (version == null)
        {
            String message = "nullValue.VersionIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("version", version);
    }

    public void setDefaultNamespace(String url)
    {
        if (url == null)
        {
            String message = "nullValue.URLIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("xmlns", url);
    }

    public void setDefaultNamespace(xmlns ns)
    {
        if (ns == null)
        {
            String message = "nullValue.xmlnsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("xmlns", ns.getUrl());
    }
}
