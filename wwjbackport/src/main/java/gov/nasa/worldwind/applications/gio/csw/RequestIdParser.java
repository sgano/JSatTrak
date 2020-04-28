/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.xml.TextElementParser;

/**
 * @author dcollins
 * @version $Id: RequestIdParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class RequestIdParser extends TextElementParser implements RequestId
{
    public static final String ELEMENT_NAME = "RequestId";

    public RequestIdParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
    }

    public String getURI()
    {
        return getValue();
    }

    public void setURI(String uri)
    {
        setValue(uri);
    }
}
