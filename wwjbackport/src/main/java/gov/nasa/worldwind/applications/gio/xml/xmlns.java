/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.xml;

/**
 * @author Lado Garakanidze
 * @version $Id: xmlns.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class xmlns
{
    private final String prefix;
    private final String url;

    private xmlns(String prefix, String url)
    {
        this.prefix = prefix;
        this.url = url;
    }

    public String getPrefix()
    {
        return this.prefix;
    }

    public String getUrl()
    {
        return this.url;
    }

    public String getNamespace()
    {
        StringBuilder sb = new StringBuilder("xmlns:");
        sb.append(prefix).append('=').append('\"').append(this.url).append('\"');
        return sb.toString();
    }

    public static final xmlns ows = new xmlns("ows", "http://www.opengis.net/ows");
    public static final xmlns ogc = new xmlns("ogc", "http://www.opengis.net/ogc");
    public static final xmlns rim = new xmlns("rim", "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5");
    public static final xmlns csw = new xmlns("csw", "http://www.opengis.net/csw");
    public static final xmlns gml = new xmlns("gml", "http://www.opengis.net/gml");
    public static final xmlns dc = new xmlns("dc", "http://purl.org/dc/elements/1.1/");
    public static final xmlns dct = new xmlns("dct", "http://purl.org/dc/terms/");
    public static final xmlns xlink = new xmlns("xlink", "http://www.w3.org/1999/xlink");
    public static final xmlns xsi = new xmlns("xsi", "http://www.w3.org/2001/XMLSchema-instance");
}
