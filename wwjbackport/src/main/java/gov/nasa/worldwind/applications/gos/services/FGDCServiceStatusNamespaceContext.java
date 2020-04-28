/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import gov.nasa.worldwind.util.BasicNamespaceContext;

import javax.xml.XMLConstants;

/**
 * @author dcollins
 * @version $Id: FGDCServiceStatusNamespaceContext.java 13557 2010-07-17 20:56:32Z dcollins $
 */
public class FGDCServiceStatusNamespaceContext extends BasicNamespaceContext
{
    public static final String REST_NS_PREFIX = "rest";
    public static final String REST_NS_URI = "http://registry.gsdi.org/statuschecker/services/rest/";

    public FGDCServiceStatusNamespaceContext()
    {
        this.addNamespace(REST_NS_PREFIX, REST_NS_URI);
        this.addNamespace(XMLConstants.DEFAULT_NS_PREFIX, REST_NS_URI);
    }
}
