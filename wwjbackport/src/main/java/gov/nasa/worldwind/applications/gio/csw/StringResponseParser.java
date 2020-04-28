/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.util.Logging;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author dcollins
 * @version $Id: StringResponseParser.java 5472 2008-06-26 20:11:53Z dcollins $
 */
public class StringResponseParser implements ResponseParser
{
    private StringBuilder sb;

    public StringResponseParser()
    {
    }

    public void parseResponse(InputStream is) throws Exception
    {
        if (is == null)
        {
            String message = "nullValue.InputStreamIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        doParseResponse(is);
    }

    protected void doParseResponse(InputStream is) throws Exception
    {
        this.sb = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null)
        {
            this.sb.append(line);
        }
    }

    public String getString()
    {
        return this.sb != null ? this.sb.toString() : null;
    }
}
