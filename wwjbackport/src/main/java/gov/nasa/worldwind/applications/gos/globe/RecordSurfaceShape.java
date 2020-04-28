/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.globe;

import gov.nasa.worldwind.applications.gos.Record;
import gov.nasa.worldwind.render.*;

/**
 * @author dcollins
 * @version $Id: RecordSurfaceShape.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class RecordSurfaceShape extends SurfacePolygon
{
    public RecordSurfaceShape(Record record)
    {
        this.setLocations(record.getLocations());

        ShapeAttributes attr = record.getShapeAttributes();
        if (attr != null)
            this.setAttributes(attr);
    }
}
