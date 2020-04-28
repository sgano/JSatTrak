/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>ItemIcon</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLItemIcon.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public class KMLItemIcon extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLItemIcon(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getHref()
    {
        return (String) this.getField("href");
    }

    public String getState()
    {
        return (String) this.getField("state");
    }
}
