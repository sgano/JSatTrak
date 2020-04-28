/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import java.net.*;

/**
 * @author dcollins
 * @version $Id: CSWGetCapabilitiesRequest.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class CSWGetCapabilitiesRequest extends CSWRequest
{
    public CSWGetCapabilitiesRequest()
    {
        super();
    }

    public CSWGetCapabilitiesRequest(Request sourceRequest) throws URISyntaxException
    {
        super(sourceRequest);
    }

    public CSWGetCapabilitiesRequest(URI uri) throws URISyntaxException
    {
        super(uri);
    }

    protected void initialize()
    {
        super.initialize();

        this.setParam("REQUEST", "GetCapabilities");
    }
}
