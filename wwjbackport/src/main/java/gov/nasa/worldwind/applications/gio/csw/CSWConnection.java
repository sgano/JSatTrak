/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

/**
 * @author dcollins
 * @version $Id$
 */
public interface CSWConnection
{
    public void openConnection() throws Exception;

    public void closeConnection();

    public void sendRequest(Request request, ResponseParser responseParser) throws Exception;
}
