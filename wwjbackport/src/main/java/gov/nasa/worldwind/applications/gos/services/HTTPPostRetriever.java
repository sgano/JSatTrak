/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * @author dcollins
 * @version $Id: HTTPPostRetriever.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class HTTPPostRetriever extends HTTPRetriever
{
    protected String postContent;

    public HTTPPostRetriever(URL url, String postContent, RetrievalPostProcessor postProcessor)
    {
        super(url, postProcessor);
        this.postContent = postContent;
    }

    protected ByteBuffer doRead(URLConnection connection) throws Exception
    {
        if (connection == null)
        {
            String msg = Logging.getMessage("nullValue.ConnectionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        connection.setDoOutput(true);

        if (connection instanceof HttpURLConnection)
        {
            ((HttpURLConnection) connection).setRequestMethod("POST");
        }

        Writer writer = null;
        try
        {
            OutputStream outputStream = connection.getOutputStream();
            if (outputStream == null)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE, "URLRetriever.OutputStreamFromConnectionNull", connection.getURL());
                return null;
            }

            writer = new OutputStreamWriter(outputStream);

            if (!WWUtil.isEmpty(this.postContent))
            {
                writer.write(this.postContent);
            }
        }
        finally
        {
            WWIO.closeStream(writer, connection.getURL().toString());
        }

        return super.doRead(connection);
    }
}
