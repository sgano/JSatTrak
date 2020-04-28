/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.shapefile;

import java.nio.*;

/**
 * Holds the information for a single record of a Polyline shape.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileRecordPolyline.java 13937 2010-10-05 23:49:13Z dcollins $
 */
public class ShapefileRecordPolyline extends ShapefileRecord
{
    protected double[] boundingRectangle;
    protected double[] zRange; // non-null only for Z types
    protected double[] zValues; // non-null only for Z types
    protected double[] mRange; // will be null if no measures
    protected double[] mValues; // will be null if no measures

    /** {@inheritDoc} */
    public ShapefileRecordPolyline(Shapefile shapeFile, ByteBuffer buffer)
    {
        super(shapeFile, buffer);
    }

    /**
     * Returns a four-element array containing the record's bounding rectangle, or null if record parsing failed. The
     * returned array is ordered as follows: minimum Y, maximum Y, minimum X, and maximum X. If the Shapefile's
     * coordinate system is geographic, the elements can be interpreted as angular degrees in the order minimum
     * latitude, maximum latitude, minimum longitude, and maximum longitude.
     *
     * @return the record's bounding rectangle, or null to denote the record parsing failed.
     */
    public double[] getBoundingRectangle()
    {
        return this.boundingRectangle != null ? this.boundingRectangle : null;
    }

    /**
     * Get all the points X and Y coordinates for the given part of this record. Part numbers start at zero.
     *
     * @param partNumber the number of the part of this record - zero based.
     *
     * @return an {@link Iterable} over the points X and Y coordinates.
     */
    public Iterable<double[]> getPoints(int partNumber)
    {
        return this.getPointBuffer(partNumber).getCoords();
    }

    /**
     * Returns the shape's Z range.
     *
     * @return the shape's Z range. The range minimum is at index 0, the maximum at index 1.
     */
    public double[] getZRange()
    {
        return this.zRange;
    }

    /**
     * Returns the shape's Z values.
     *
     * @return the shape's Z values.
     */
    public double[] getZValues()
    {
        return this.zValues;
    }

    /**
     * Returns the shape's optional measure range.
     *
     * @return the shape's measure range, or null if no measures are in the record. The range minimum is at index 0, the
     *         maximum at index 1.
     */
    public double[] getMRange()
    {
        return this.mRange;
    }

    /**
     * Returns the shape's optional measure values.
     *
     * @return the shape's measure values, or null if no measures are in the record.
     */
    public double[] getMValues()
    {
        return this.mValues;
    }

    /** {@inheritDoc} */
    protected void readFromBuffer(Shapefile shapefile, ByteBuffer buffer)
    {
        // Read record number and record length - big endian.
        buffer.order(ByteOrder.BIG_ENDIAN);
        this.recordNumber = buffer.getInt();
        this.contentLengthInBytes = buffer.getInt() * 2;

        // Read shape type - little endian.
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int type = buffer.getInt();
        String shapeType = shapefile.getShapeType(type);
        this.validateShapeType(shapefile, shapeType);

        this.shapeType = shapeType;
        this.shapeFile = shapefile;

        if (shapeType.equals(Shapefile.SHAPE_NULL))
            return;

        // Read the bounding rectangle.
        Shapefile.BoundingRectangle rect = shapefile.readBoundingRectangle(buffer);
        this.boundingRectangle = rect.coords;

        // Specify that the record's points should be normalized if the bounding rectangle is normalized. Ignore the
        // shapefile's normalizePoints property to avoid normalizing records that don't need it.
        if (rect.isNormalized)
            this.setNormalizePoints(true);

        // Read the number of parts and the number of points.
        this.numberOfParts = buffer.getInt();
        this.numberOfPoints = buffer.getInt();
        this.firstPartNumber = -1;

        if (this.numberOfParts > 0 && this.numberOfPoints > 0)
        {
            // Read the part positions.
            int[] partPositions = ShapefileUtils.readIntArray(buffer, this.numberOfParts);

            for (int i = 0; i < this.numberOfParts; i++)
            {
                int length = (i == this.numberOfParts - 1) ? this.numberOfPoints - partPositions[i]
                    : partPositions[i + 1] - partPositions[i];

                // Add the record's points to the Shapefile's point buffer, and record this record's part offset in the
                // Shapefile's point buffer.
                int offset = shapefile.addPoints(this, buffer, length);

                if (this.firstPartNumber < 0)
                    this.firstPartNumber = offset;
            }
        }

        // Read the optional Z value.
        if (this.isZType())
            this.readZ(buffer);

        // Read the optional measure value.
        if (this.isMeasureType())
            this.readOptionalMeasures(buffer);
    }

    /**
     * Read's the shape's Z values from the record buffer.
     *
     * @param buffer the record buffer to read from.
     */
    protected void readZ(ByteBuffer buffer)
    {
        this.zRange = ShapefileUtils.readDoubleArray(buffer, 2);
        this.zValues = ShapefileUtils.readDoubleArray(buffer, this.getNumberOfPoints());
    }

    /**
     * Reads any optional measure values from the record buffer.
     *
     * @param buffer the record buffer to read from.
     */
    protected void readOptionalMeasures(ByteBuffer buffer)
    {
        // Measure values are optional.
        if (buffer.hasRemaining() && (buffer.limit() - buffer.position()) >= (this.getNumberOfPoints() * 8))
        {
            this.mRange = ShapefileUtils.readDoubleArray(buffer, 2);
            this.mValues = ShapefileUtils.readDoubleArray(buffer, this.getNumberOfPoints());
        }
    }
}
