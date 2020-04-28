/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml.gx;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLAbstractObject;

/**
 * @author tag
 * @version $Id: GXLatLongQuad.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class GXLatLongQuad extends KMLAbstractObject
{
    public GXLatLongQuad(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Position.PositionList getCoordinates()
    {
        return (Position.PositionList) this.getField("coordinates");
    }
}
