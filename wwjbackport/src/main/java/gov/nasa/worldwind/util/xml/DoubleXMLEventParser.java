/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.util.WWUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Parse a Double from an XML event.
 *
 * @author tag
 * @version $Id: DoubleXMLEventParser.java 13107 2010-02-06 08:25:24Z tgaskins $
 */
public class DoubleXMLEventParser extends AbstractXMLEventParser
{
    public Object parse(XMLEventParserContext ctx, XMLEvent doubleEvent, Object... args) throws XMLStreamException
    {
        String s = this.parseCharacterContent(ctx, doubleEvent);
        return s != null ? WWUtil.convertStringToDouble(s) : null;
    }

    public Double parseDouble(XMLEventParserContext ctx, XMLEvent doubleEvent, Object... args) throws XMLStreamException
    {
        return (Double) this.parse(ctx, doubleEvent, args);
    }
}
