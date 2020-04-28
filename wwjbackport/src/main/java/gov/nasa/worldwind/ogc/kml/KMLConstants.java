/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Defines constants used by the KML parser classes.
 *
 * @author tag
 * @version $Id: KMLConstants.java 13866 2010-09-23 19:56:15Z pabercrombie $
 */
public interface KMLConstants
{
    /** The KML 2.2 namespace URI. */
    final String KML_2dot0_NAMESPACE = "http://earth.google.com/kml/2.0";
    final String KML_2dot1_NAMESPACE = "http://earth.google.com/kml/2.1";
    final String KML_2dot2_NAMESPACE = "http://www.opengis.net/kml/2.2";
    final String KML_GOOGLE_2dot2_NAMESPACE = "http://earth.google.com/kml/2.2";
    final String KML_NAMESPACE = KML_2dot2_NAMESPACE;

    /**
     * List of the versioned KML namespaces.
     */
    final String[] KML_NAMESPACES = {
        KML_2dot2_NAMESPACE,
        KML_GOOGLE_2dot2_NAMESPACE,
        KML_2dot1_NAMESPACE,
        KML_2dot0_NAMESPACE
    };

    /** The mime type for KML documents. */
    final String KML_MIME_TYPE = "application/vnd.google-earth.kml+xml";
    /** The mime type for KMZ documents. */
    final String KMZ_MIME_TYPE = "application/vnd.google-earth.kmz";

    /** Most recent version of KML that WorldWind supports. */
    final String KML_VERSION = "2.2";

    // Style state enums
    final String NORMAL = "normal";
    final String HIGHLIGHT = "highlight";
    final String STYLE_STATE = "styleState"; // a key for a style state field

    // The key that identifies resolved styles in a parser's field map.
    final String BALOON_STYLE_FIELD = "BaloonStyle";
    final String ICON_STYLE_FIELD = "IconStyle";
    final String LABEL_STYLE_FIELD = "LabelStyle";
    final String LINE_STYLE_FIELD = "LineStyle";
    final String LIST_STYLE_FIELD = "ListStyle";
    final String POLY_STYLE_FIELD = "PolyStyle";
    final String STYLE_FIELD = "Style";
    final String STYLE_MAP_FIELD = "StyleMap";
    final String STYLE_URL_FIELD = "styleUrl";
}
