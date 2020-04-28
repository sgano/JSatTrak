/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.util.WWUtil;

import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLStreamException;

/**
 * @author tag
 * @version $Id: IntegerXMLEventParser.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class IntegerXMLEventParser extends AbstractXMLEventParser
{
    public Object parse(XMLEventParserContext ctx, XMLEvent integerEvent, Object... args) throws XMLStreamException
    {
        String s = this.parseCharacterContent(ctx, integerEvent);
        return s != null ? WWUtil.convertStringToInteger(s) : null;
    }

    public Integer parseDouble(XMLEventParserContext ctx, XMLEvent doubleEvent, Object... args)
        throws XMLStreamException
    {
        return (Integer) this.parse(ctx, doubleEvent, args);
    }
}
