/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.shapefile;

import java.nio.ByteBuffer;

/**
 * Holds the information for a single record of a Polygon shape.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileRecordPolygon.java 13392 2010-05-21 02:10:42Z dcollins $
 */
public class ShapefileRecordPolygon extends ShapefileRecordPolyline
{
    /** {@inheritDoc} */
    public ShapefileRecordPolygon(Shapefile shapeFile, ByteBuffer buffer)
    {
        super(shapeFile, buffer);
    }
}
