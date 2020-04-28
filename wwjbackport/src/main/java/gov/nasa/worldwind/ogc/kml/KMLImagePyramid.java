/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>ImagePyramid</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLImagePyramid.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public class KMLImagePyramid extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLImagePyramid(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Integer getTileSize()
    {
        return (Integer) this.getField("tileSize");
    }

    public Integer getMaxWidth()
    {
        return (Integer) this.getField("maxWidth");
    }

    public Integer getMaxHeight()
    {
        return (Integer) this.getField("maxHeight");
    }

    public String getGridOrigin()
    {
        return (String) this.getField("gridOrigin");
    }
}
