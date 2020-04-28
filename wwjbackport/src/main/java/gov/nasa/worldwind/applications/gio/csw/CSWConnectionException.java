/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: CSWConnectionException.java 5479 2008-06-30 09:16:17Z dcollins $
 */
public class CSWConnectionException extends IOException
{
    public CSWConnectionException()
    {
    }

    public CSWConnectionException(String s)
    {
        super(s);
    }
}
