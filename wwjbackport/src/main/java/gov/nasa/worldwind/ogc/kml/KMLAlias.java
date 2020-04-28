/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>Alias</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLAlias.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public class KMLAlias extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLAlias(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getTargetHref()
    {
        return (String) this.getField("targetHref");
    }

    public String getSourceRef()
    {
        return (String) this.getField("sourceHref");
    }
}
