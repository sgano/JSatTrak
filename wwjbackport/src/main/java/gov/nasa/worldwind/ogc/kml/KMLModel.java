/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>Model</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLModel.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public class KMLModel extends KMLAbstractGeometry
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLModel(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getAltitudeMode()
    {
        return (String) this.getField("altitudeMode");
    }

    public KMLLocation getLocation()
    {
        return (KMLLocation) this.getField("Location");
    }

    public KMLOrientation getOrientation()
    {
        return (KMLOrientation) this.getField("Orientation");
    }

    public KMLScale getScale()
    {
        return (KMLScale) this.getField("Scale");
    }

    public KMLLink getLink()
    {
        return (KMLLink) this.getField("Link");
    }

    public KMLResourceMap getResourceMap()
    {
        return (KMLResourceMap) this.getField("ResourceMap");
    }
}
