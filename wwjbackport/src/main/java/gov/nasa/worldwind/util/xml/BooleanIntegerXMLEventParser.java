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
 * @author tag
 * @version $Id: BooleanIntegerXMLEventParser.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class BooleanIntegerXMLEventParser extends AbstractXMLEventParser
{
    public Object parse(XMLEventParserContext ctx, XMLEvent booleanEvent, Object... args) throws XMLStreamException
    {
        String s = this.parseCharacterContent(ctx, booleanEvent);
        return s != null ? WWUtil.convertNumericStringToBoolean(s) : null;
    }

    public Boolean parseBoolean(XMLEventParserContext ctx, XMLEvent booleanEvent, Object... args)
        throws XMLStreamException
    {
        return (Boolean) this.parse(ctx, booleanEvent, args);
    }
}
