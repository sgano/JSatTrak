/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import java.net.*;

/**
 * @author dcollins
 * @version $Id: CSWRequest.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class CSWRequest extends Request
{
    protected CSWRequest()
    {
        super();
    }

    protected CSWRequest(URI uri) throws URISyntaxException
    {
        super(uri);
    }

    protected CSWRequest(Request sourceRequest) throws URISyntaxException
    {
        super(sourceRequest);
    }

    protected void initialize()
    {
        super.initialize();

        this.setParam("SERVICE", "CSW");
        this.setParam("EXCEPTIONS", "application/vnd.ogc.se_xml");
    }
}
