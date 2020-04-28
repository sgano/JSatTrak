/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

/**
 * @author dcollins
 * @version $Id: CSWConnectionPool.java 5472 2008-06-26 20:11:53Z dcollins $
 */
public interface CSWConnectionPool
{
    CSWConnection getConnection() throws Exception;
}
