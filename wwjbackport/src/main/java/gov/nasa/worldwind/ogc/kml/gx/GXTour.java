/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml.gx;

import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;

/**
 * @author tag
 * @version $Id: GXTour.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class GXTour extends KMLAbstractFeature
{
    public GXTour(String namespaceURI)
    {
        super(namespaceURI);
    }

    public GXPlaylist getPlaylist()
    {
        return (GXPlaylist) this.getField("Playlist");
    }
}
