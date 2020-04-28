/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.util.*;

import java.io.*;
import java.net.*;

/**
 * @author dcollins
 * @version $Id: HttpCSWConnection.java 9306 2009-03-11 20:05:32Z tgaskins $
 */
public class HttpCSWConnection implements CSWConnection
{
    private URL serviceURL;
    private URLConnection connection;
    private String contentType;
    private String contentEncoding;
    private static final String DEFAULT_CONTENT_TYPE = "text/xml";
    private static final String DEFAULT_CONTENT_ENCODING = "utf-8";

    public HttpCSWConnection(URL serviceURL, String contentType, String contentEncoding)
    {
        if (serviceURL == null)
        {
            String message = "nullValue.ServiceURLIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (contentType == null)
        {
            String message = "nullValue.ContentTypeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (contentEncoding == null)
        {
            String message = "nullValue.ContentEncodingIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceURL = serviceURL;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
    }

    public HttpCSWConnection(URL serviceURL)
    {
        this(serviceURL, DEFAULT_CONTENT_TYPE, DEFAULT_CONTENT_ENCODING);
    }

    public URL getServiceURL()
    {
        return this.serviceURL;
    }

    public String getContentType()
    {
        return this.contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public String getContentEncoding()
    {
        return this.contentEncoding;
    }

    public void setContentEncoding(String contentEncoding)
    {
        this.contentEncoding = contentEncoding;
    }

    public void openConnection() throws Exception
    {
        try
        {
            this.connection = this.serviceURL.openConnection();
        }
        catch (IOException e)
        {
            String message = "csw.ExceptionWhileConnectingtoCSW";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw new CSWConnectionException(e.getMessage());
        }

        if (this.connection == null)
        {
            String message = "csw.ConnectionIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!(this.connection instanceof HttpURLConnection))
        {
            String message = "csw.ConnectionIsNotHttp";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        HttpURLConnection httpConn = (HttpURLConnection) this.connection;
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);

        httpConn.setRequestProperty("Content-Type", this.contentType + "; charset=" + this.contentEncoding);
        httpConn.setRequestMethod("POST");

        String message = String.format("csw.ConnectionOpened [Method=%s, URL=%s]",
            httpConn.getRequestMethod(),
            this.connection.getURL().toExternalForm());
        Logging.logger().fine(message);
    }

    public void closeConnection()
    {
        if (this.connection == null)
        {
            String message = "csw.ConnectionIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!(this.connection instanceof HttpURLConnection))
        {
            String message = "csw.ConnectionIsNotHttp";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String urlString = this.connection.getURL().toExternalForm();

        HttpURLConnection httpConn = (HttpURLConnection) this.connection;
        httpConn.disconnect();
        this.connection = null;

        String message = String.format("csw.ConnectionClosed [URL=%s]", urlString);
        Logging.logger().fine(message);

    }

    public void sendRequest(Request request, ResponseParser responseParser) throws Exception
    {
        if (request == null)
        {
            String message = "nullValue.RequestIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (responseParser == null)
        {
            String message = "nullValue.ResponseParserIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (this.connection == null)
        {
            String message = "csw.ConnectionIsClosed";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);    
        }

        String requestContent = null;
        if (!interrupted())
        {
            StringBuilder sb = new StringBuilder("<?xml version='1.0'?>");
            sb.append(request.toXml());
            requestContent = sb.toString();
        }

        if (!interrupted())
        {
            writeRequest(requestContent);
        }

        if (!interrupted())
        {
            readResponse(responseParser);
        }
    }

    private void writeRequest(String requestContent) throws Exception
    {
        if (requestContent == null)
        {
            String message = "nullValue.RequestContentIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        byte[] requestBytes = requestContent.getBytes("UTF-8");
        this.connection.setRequestProperty("Content-Length", String.valueOf(requestBytes.length));

        OutputStream out = null;
        try
        {
            out = this.connection.getOutputStream();
            if (out != null)
            {
                out.write(requestBytes);
                out.flush();

                String message = String.format(
                    "csw.Request [Content-Length=%s, Content-Type=%s, URL=%s]",
                    this.connection.getContentLength(),
                    this.connection.getContentType(),
                    this.connection.getURL().toExternalForm());
                Logging.logger().fine(message);
            }
        }
        catch (IOException e)
        {
            String message = "csw.ExceptionWhileWritingRequest";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw new CSWConnectionException(e.getMessage());
        }
        finally
        {
            WWIO.closeStream(out, requestContent);
        }
    }

    private void readResponse(ResponseParser responseParser) throws Exception
    {
        InputStream in = null;
        try
        {
            in = this.connection.getInputStream();
            if (in != null)
            {
                responseParser.parseResponse(in);

                String message = String.format(
                    "csw.Response [Response-Code=%d, Content-Length=%s, Content-Type=%s, URL=%s]",
                    this.connection instanceof HttpURLConnection ?
                        ((HttpURLConnection) this.connection).getResponseCode() : -1,
                    this.connection.getContentLength(),
                    this.connection.getContentType(),
                    this.connection.getURL().toExternalForm());
                Logging.logger().fine(message);
            }
        }
        catch (IOException e)
        {
            String message = "csw.ExceptionWhileReadingResponse";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw new CSWConnectionException(e.getMessage());
        }
        finally
        {
            WWIO.closeStream(in, null);
        }
    }

    private boolean interrupted()
    {
        if (Thread.currentThread().isInterrupted())
        {
            String message = "csw.ConnectionInterrupted";
            Logging.logger().fine(message);
            return true;
        }
        return false;
    }
}
