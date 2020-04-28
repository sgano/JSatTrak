/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLStreamException;
import java.util.*;

/**
 * Represents the KML <i>SchemaData</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLSchemaData.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public class KMLSchemaData extends KMLAbstractObject
{
    protected List<KMLSimpleData> simpleData = new ArrayList<KMLSimpleData>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLSchemaData(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLSimpleData)
            this.addSimpleData((KMLSimpleData) o);
    }

    public String getSchemaUrl()
    {
        return (String) this.getField("schemaUrl");
    }

    protected void addSimpleData(KMLSimpleData o)
    {
        this.simpleData.add(o);
    }

    public List<KMLSimpleData> getSimpleData()
    {
        return this.simpleData;
    }
}
