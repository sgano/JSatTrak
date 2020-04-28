/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.geom.Position;

/**
 * Represents the KML <i>LinearRing</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLLinearRing.java 13633 2010-08-13 04:10:21Z tgaskins $
 */
public class KMLLinearRing extends KMLLineString
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLLinearRing(String namespaceURI)
    {
        super(namespaceURI);
    }
}
