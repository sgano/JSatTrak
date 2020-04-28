/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import java.io.InputStream;

/**
 * @author dcollins
 * @version $Id: ResponseParser.java 5456 2008-06-18 17:18:25Z dcollins $
 */
public interface ResponseParser
{
    void parseResponse(InputStream is) throws Exception;
}
