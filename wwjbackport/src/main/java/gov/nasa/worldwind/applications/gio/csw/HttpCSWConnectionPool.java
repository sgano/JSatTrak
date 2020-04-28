/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.util.Logging;

import java.net.URL;

/**
 * @author dcollins
 * @version $Id: HttpCSWConnectionPool.java 5472 2008-06-26 20:11:53Z dcollins $
 */
public class HttpCSWConnectionPool implements CSWConnectionPool
{
    private URL serviceURL;

    public HttpCSWConnectionPool(URL serviceURL)
    {
        if (serviceURL == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.serviceURL = serviceURL;
    }

    public URL getServiceURL()
    {
        return this.serviceURL;
    }

    public CSWConnection getConnection() throws Exception
    {
        return new HttpCSWConnection(this.serviceURL);
    }
}
