/* Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: GeoJSONGeometry.java 13792 2010-09-13 20:43:15Z dcollins $
 */
public abstract class GeoJSONGeometry extends GeoJSONObject
{
    protected GeoJSONGeometry(AVList fields)
    {
        super(fields);
    }

    @Override
    public boolean isGeometry()
    {
        return true;
    }
}
