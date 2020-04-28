/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.xml.xal;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id: XALAbstractObject.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class XALAbstractObject extends AbstractXMLEventParser
{
    public XALAbstractObject(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getType()
    {
        return (String) this.getField("Type");
    }

    public String getCode()
    {
        return (String) this.getField("Code");
    }
}
