/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>LookAt</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLLookAt.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public class KMLLookAt extends KMLAbstractView
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLLookAt(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getLongitude()
    {
        return (Double) this.getField("longitude");
    }

    public Double getLatitude()
    {
        return (Double) this.getField("latitude");
    }

    public Double getAltitude()
    {
        return (Double) this.getField("altitude");
    }

    public Double getHeading()
    {
        return (Double) this.getField("heading");
    }

    public Double getTilt()
    {
        return (Double) this.getField("tilt");
    }

    public Double getRange()
    {
        return (Double) this.getField("range");
    }

    public String getAltitudeMode()
    {
        return (String) this.getField("altitudeMode");
    }
}
