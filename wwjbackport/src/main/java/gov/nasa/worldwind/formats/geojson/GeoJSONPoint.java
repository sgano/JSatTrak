/* Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;

/**
 * @author dcollins
 * @version $Id: GeoJSONPoint.java 13792 2010-09-13 20:43:15Z dcollins $
 */
public class GeoJSONPoint extends GeoJSONGeometry
{
    public GeoJSONPoint(AVList fields)
    {
        super(fields);
    }

    @Override
    public boolean isPoint()
    {
        return true;
    }

    public Position getPosition()
    {
        GeoJSONPositionArray array = this.getCoordinates();
        return array != null ? array.getPosition(0) : null;
    }

    protected GeoJSONPositionArray getCoordinates()
    {
        return (GeoJSONPositionArray) this.getValue(GeoJSONConstants.FIELD_COORDINATES);
    }
}
