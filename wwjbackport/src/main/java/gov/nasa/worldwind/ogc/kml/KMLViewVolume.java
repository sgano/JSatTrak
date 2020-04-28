/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>ViewVolume</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLViewVolume.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public class KMLViewVolume extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLViewVolume(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getNear()
    {
        return (Double) this.getField("near");
    }

    public Double getLeftFov()
    {
        return (Double) this.getField("leftFov");
    }

    public Double getRightFov()
    {
        return (Double) this.getField("rightFov");
    }

    public Double getTopFov()
    {
        return (Double) this.getField("topFov");
    }

    public Double getBottomFov()
    {
        return (Double) this.getField("bottomFov");
    }
}
