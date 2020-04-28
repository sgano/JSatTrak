/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>LatLonAltBox</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLLatLonAltBox.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public class KMLLatLonAltBox extends KMLAbstractLatLonBoxType
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLLatLonAltBox(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getMinAltitude()
    {
        return (Double) this.getField("minAltitude");
    }

    public Double getMaxAltitude()
    {
        return (Double) this.getField("maxAltitude");
    }

    public String getAltitudeMode()
    {
        return (String) this.getField("altitudeMode");
    }
}
