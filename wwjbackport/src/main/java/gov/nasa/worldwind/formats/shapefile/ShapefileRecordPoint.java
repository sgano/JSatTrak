/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.util.VecBuffer;

import java.nio.*;

/**
 * Holds the information for a single record of a Point shape.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileRecordPoint.java 13937 2010-10-05 23:49:13Z dcollins $
 */
public class ShapefileRecordPoint extends ShapefileRecord
{
    protected Double z; // non-null only for Z types
    protected Double m; // non-null only for Measure types with measures specified

    /** {@inheritDoc} */
    public ShapefileRecordPoint(Shapefile shapeFile, ByteBuffer buffer)
    {
        super(shapeFile, buffer);
    }

    /**
     * Get the point X and Y coordinates.
     *
     * @return the point X and Y coordinates.
     */
    public double[] getPoint()
    {
        VecBuffer vb = this.getPointBuffer(0);
        return vb.get(0, new double[vb.getCoordsPerVec()]);
    }

    /**
     * Returns the shape's Z value.
     *
     * @return the shape's Z value.
     */
    public Double getZ()
    {
        return this.z;
    }

    /**
     * Return the shape's optional measure value.
     *
     * @return the shape's measure, or null if no measure is in the record.
     */
    public Double getM()
    {
        return this.m;
    }

    /** {@inheritDoc} */
    protected void readFromBuffer(Shapefile shapefile, ByteBuffer buffer)
    {
        // Read record number and record length - big endian.
        buffer.order(ByteOrder.BIG_ENDIAN);
        this.recordNumber = buffer.getInt();
        this.contentLengthInBytes = buffer.getInt() * 2;

        // Read shape type - little endian
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int type = buffer.getInt();
        String shapeType = shapefile.getShapeType(type);
        this.validateShapeType(shapefile, shapeType);

        this.shapeType = shapeType;
        this.shapeFile = shapefile;

        if (shapeType.equals(Shapefile.SHAPE_NULL))
            return;

        // Specify that the record's points should be normalized if the shapefile itself is marked as needing
        // normalization.
        if (shapefile.isNormalizePoints())
            this.setNormalizePoints(true);

        // Store the number of parts and the number of points (always 1).
        this.numberOfParts = 1;
        this.numberOfPoints = 1;

        // Add the record's points to the Shapefile's point buffer, and record this record's part offset in the
        // Shapefile's point buffer.
        this.firstPartNumber = shapefile.addPoints(this, buffer, 1);

        // Read the optional Z value.
        if (this.isZType())
            this.readZ(buffer);

        // Read the optional measure value.
        if (this.isMeasureType())
            this.readOptionalMeasure(buffer);
    }

    /**
     * Read the record's Z value from the record buffer.
     *
     * @param buffer the record to read from.
     */
    protected void readZ(ByteBuffer buffer)
    {
        double[] zArray = ShapefileUtils.readDoubleArray(buffer, 1);
        this.z = zArray[0];
    }

    /**
     * Read any optional measure values from the record.
     *
     * @param buffer the record buffer to read from.
     */
    protected void readOptionalMeasure(ByteBuffer buffer)
    {
        // Measure values are optional.
        if (buffer.hasRemaining() && (buffer.limit() - buffer.position()) >= 8)
        {
            double[] mArray = ShapefileUtils.readDoubleArray(buffer, 1);
            this.m = mArray[0];
        }
    }
}
