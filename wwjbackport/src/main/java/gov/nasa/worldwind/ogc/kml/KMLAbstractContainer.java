/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the KML <i>Container</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLAbstractContainer.java 13681 2010-08-31 14:44:03Z dcollins $
 */
public class KMLAbstractContainer extends KMLAbstractFeature
{
    protected ArrayList<KMLAbstractFeature> features = new ArrayList<KMLAbstractFeature>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractContainer(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLAbstractFeature)
            this.addFeature((KMLAbstractFeature) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public List<KMLAbstractFeature> getFeatures()
    {
        return this.features;
    }

    protected void addFeature(KMLAbstractFeature feature)
    {
        this.features.add(feature);
    }

    @Override
    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        for (KMLAbstractFeature feature : this.getFeatures())
        {
            feature.preRender(tc, dc);
        }
    }

    @Override
    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        for (KMLAbstractFeature feature : this.getFeatures())
        {
            feature.render(tc, dc);
        }
    }
}
