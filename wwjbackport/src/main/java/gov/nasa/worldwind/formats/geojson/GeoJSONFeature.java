/* Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: GeoJSONFeature.java 13567 2010-07-21 04:15:15Z dcollins $
 */
public class GeoJSONFeature extends GeoJSONObject
{
    public GeoJSONFeature(AVList fields)
    {
        super(fields);
    }

    @Override
    public boolean isFeature()
    {
        return true;
    }

    public GeoJSONGeometry getGeometry()
    {
        return (GeoJSONGeometry) this.getValue(GeoJSONConstants.FIELD_GEOMETRY);
    }

    public AVList getProperties()
    {
        return (AVList) this.getValue(GeoJSONConstants.FIELD_PROPERTIES);
    }
}
