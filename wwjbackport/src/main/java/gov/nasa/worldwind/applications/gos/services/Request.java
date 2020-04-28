/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import gov.nasa.worldwind.util.Logging;

import java.net.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: Request.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class Request
{
    private URI uri = null;
    // Use a LinkedHashMap to hold the query params so that they'll always be attached to the
    // URL query string in the same order. This allows a simple string comparison to
    // determine whether two url strings address the same document.
    private LinkedHashMap<String, String> queryParams = new LinkedHashMap<String, String>();

    protected Request()
    {
        this.initialize();
    }

    protected Request(URI uri) throws URISyntaxException
    {
        if (uri != null)
        {
            try
            {
                this.setUri(uri);
            }
            catch (URISyntaxException e)
            {
                Logging.logger().fine(Logging.getMessage("generic.URIInvalid", uri.toString()));
                throw e;
            }
        }

        this.initialize();
    }

    protected Request(Request sourceRequest) throws URISyntaxException
    {
        if (sourceRequest == null)
        {
            String message = Logging.getMessage("nullValue.CopyConstructorSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        sourceRequest.copyParamsTo(this);
        this.setUri(sourceRequest.getUri());
    }

    protected void copyParamsTo(Request destinationRequest)
    {
        if (destinationRequest == null)
        {
            String message = Logging.getMessage("nullValue.CopyTargetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (Map.Entry<String, String> entry : this.queryParams.entrySet())
        {
            destinationRequest.setParam((String) ((Map.Entry) entry).getKey(), (String) ((Map.Entry) entry).getValue());
        }
    }

    protected void initialize()
    {
    }

    protected void setUri(URI uri) throws URISyntaxException
    {
        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            this.uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
                this.buildQueryString(), null);
        }
        catch (URISyntaxException e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri.toString());
            Logging.logger().fine(message);
            throw e;
        }
    }

    public void setParam(String key, String value)
    {
        if (key != null)
            this.queryParams.put(key, value);
    }

    public String getParam(String key)
    {
        return key != null ? this.queryParams.get(key) : null;
    }

    public URI getUri() throws URISyntaxException
    {
        if (this.uri == null)
            return null;

        try
        {
            return new URI(this.uri.getScheme(), this.uri.getUserInfo(), this.uri.getHost(), this.uri.getPort(),
                uri.getPath(), this.buildQueryString(), null);
        }
        catch (URISyntaxException e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri.toString());
            Logging.logger().fine(message);
            throw e;
        }
    }

    private String buildQueryString()
    {
        StringBuffer queryString = new StringBuffer();

        for (Map.Entry<String, String> entry : this.queryParams.entrySet())
        {
            if (((Map.Entry) entry).getKey() != null && ((Map.Entry) entry).getValue() != null)
            {
                queryString.append(((Map.Entry) entry).getKey());
                queryString.append("=");
                queryString.append(((Map.Entry) entry).getValue());
                queryString.append("&");
            }
        }

        return queryString.toString();
    }

    public String toString()
    {
        String errorMessage = "Error converting request URI to string.";
        try
        {
            java.net.URI fullUri = this.getUri();
            return fullUri != null ? fullUri.toString() : errorMessage;
        }
        catch (URISyntaxException e)
        {
            return errorMessage;
        }
    }
}
