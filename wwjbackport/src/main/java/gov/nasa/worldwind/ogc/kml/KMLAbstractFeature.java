/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;
import gov.nasa.worldwind.util.xml.atom.*;
import gov.nasa.worldwind.util.xml.xal.XALAddressDetails;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the KML <i>Feature</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLAbstractFeature.java 13686 2010-08-31 22:11:06Z dcollins $
 */
public abstract class KMLAbstractFeature extends KMLAbstractObject implements KMLRenderable
{
    protected ArrayList<KMLAbstractStyleSelector> styleSelectors = new ArrayList<KMLAbstractStyleSelector>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractFeature(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLAbstractView)
            this.setView((KMLAbstractView) o);
        else if (o instanceof KMLAbstractTimePrimitive)
            this.setTimePrimitive((KMLAbstractTimePrimitive) o);
        else if (o instanceof KMLAbstractStyleSelector)
            this.addStyleSelector((KMLAbstractStyleSelector) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public String getName()
    {
        return (String) this.getField("name");
    }

    public Boolean getVisibility()
    {
        return (Boolean) this.getField("visibility");
    }

    public Boolean getOpen()
    {
        return (Boolean) this.getField("open");
    }

    public AtomPerson getAuthor()
    {
        return (AtomPerson) this.getField("author");
    }

    public AtomLink getLink()
    {
        return (AtomLink) this.getField("link");
    }

    public String getAddress()
    {
        return (String) this.getField("address");
    }

    public XALAddressDetails getAddressDetails()
    {
        return (XALAddressDetails) this.getField("AddressDetails");
    }

    public String getPhoneNumber()
    {
        return (String) this.getField("phoneNumber");
    }

    public Object getSnippet()
    {
        Object o = this.getField("snippet");
        if (o != null)
            return o;

        return this.getField("Snippet");
    }

    public String getSnippetText()
    {
        Object o = this.getField("snippet");
        if (o != null)
            return (String) o;

        KMLSnippet snippet = (KMLSnippet) this.getField("Snippet");
        if (snippet != null)
            return snippet.getCharacters();

        return null;
    }

    public String getDescription()
    {
        return (String) this.getField("description");
    }

    protected void setView(KMLAbstractView o)
    {
        this.setField("AbstractView", o);
    }

    public KMLAbstractView getView()
    {
        return (KMLAbstractView) this.getField("AbstractView");
    }

    protected void setTimePrimitive(KMLAbstractTimePrimitive o)
    {
        this.setField("AbstractTimePrimitive", o);
    }

    public KMLAbstractTimePrimitive getTimePrimitive()
    {
        return (KMLAbstractTimePrimitive) this.getField("AbstractTimePrimitive");
    }

    public KMLStyleUrl getStyleUrl()
    {
        return (KMLStyleUrl) this.getField("styleUrl");
    }

    protected void addStyleSelector(KMLAbstractStyleSelector o)
    {
        this.styleSelectors.add(o);
    }

    public List<KMLAbstractStyleSelector> getStyleSelectors()
    {
        return this.styleSelectors;
    }

    public KMLRegion getRegion()
    {
        return (KMLRegion) this.getField("Region");
    }

    public KMLExtendedData getExtendedData()
    {
        return (KMLExtendedData) this.getField("ExtendedData");
    }

    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        // Subclasses override to implement render behavior.
    }

    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        // Subclasses override to implement render behavior.
    }

    /**
     * Obtains the effective values for a specified sub-style (<i>IconStyle</i>, <i>ListStyle</i>, etc.) and state
     * (<i>normal</i> or <i>highlight</i>). The returned style is the result of merging values from this feature
     * instance's style selectors and its styleUrl, if any, with precedence given to style selectors.
     * <p/>
     * A remote <i>styleUrl</i> that has not yet been resolved is not included in the result. In this case the returned
     * sub-style is marked with the value {@link gov.nasa.worldwind.avlist.AVKey#UNRESOLVED}. The same is true when a
     * StyleMap style selector contains a reference to an external Style and that reference has not been resolved.
     *
     * @param styleState the style mode, either \"normal\" or \"highlight\".
     * @param subStyle   an instance of the sub-style desired, such as {@link gov.nasa.worldwind.ogc.kml.KMLIconStyle}.
     *                   The effective sub-style values are accumulated and merged into this instance. The instance
     *                   should not be one from within the KML document because its values are overridden and augmented;
     *                   it's just an independent variable in which to return the merged attribute values. For
     *                   convenience, the instance specified is returned as the return value of this method.
     *
     * @return the sub-style values for the specified type and state. The reference returned is the one passed in as the
     *         <code>subStyle</code> argument.
     */
    public KMLAbstractSubStyle getSubStyle(KMLAbstractSubStyle subStyle, String styleState)
    {
        return KMLAbstractStyleSelector.mergeSubStyles(this.getStyleUrl(), this.getStyleSelectors(), styleState,
            subStyle);
    }
}
