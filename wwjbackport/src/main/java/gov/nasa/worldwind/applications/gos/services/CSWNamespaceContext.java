/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import gov.nasa.worldwind.util.BasicNamespaceContext;

/**
 * @author dcollins
 * @version $Id: CSWNamespaceContext.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class CSWNamespaceContext extends BasicNamespaceContext
{
    public static final String CSW_NS_PREFIX = "csw";
    public static final String CSW_NS_URI = "http://www.opengis.net/cat/csw/2.0.2";

    public static final String DC_NS_PREFIX = "dc";
    public static final String DC_NS_URI = "http://purl.org/dc/elements/1.1/";

    public static final String DCMIBOX_NS_PREFIX = "dcmiBox";
    public static final String DCMIBOX_NS_URI = "http://dublincore.org/documents/2000/07/11/dcmi-box/";

    public static final String DCT_NS_PREFIX = "dct";
    public static final String DCT_NS_URI = "http://purl.org/dc/terms/";

    public static final String GML_NS_PREFIX = "gml";
    public static final String GML_NS_URI = "http://www.opengis.net/gml";

    public static final String OWS_NS_PREFIX = "ows";
    public static final String OWS_NS_URI = "http://www.opengis.net/ows";

    public CSWNamespaceContext()
    {
        this.addNamespace(CSW_NS_PREFIX, CSW_NS_URI);
        this.addNamespace(DC_NS_PREFIX, DC_NS_URI);
        this.addNamespace(DCMIBOX_NS_PREFIX, DCMIBOX_NS_URI);
        this.addNamespace(DCT_NS_PREFIX, DCT_NS_URI);
        this.addNamespace(GML_NS_PREFIX, GML_NS_URI);
        this.addNamespace(OWS_NS_PREFIX, OWS_NS_URI);
    }
}
