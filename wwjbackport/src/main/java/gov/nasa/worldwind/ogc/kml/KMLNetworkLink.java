/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>NetworkLink</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLNetworkLink.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public class KMLNetworkLink extends KMLAbstractFeature
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLNetworkLink(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Boolean getRefreshVisibility()
    {
        return (Boolean) this.getField("refreshVisibility");
    }

    public Boolean getFlyToView()
    {
        return (Boolean) this.getField("flyToView");
    }

    public KMLLink getNetworkLink()
    {
        return (KMLLink) this.getField("Link");
    }

    public KMLLink getUrl()
    {
        return (KMLLink) this.getField("Url");
    }
}
