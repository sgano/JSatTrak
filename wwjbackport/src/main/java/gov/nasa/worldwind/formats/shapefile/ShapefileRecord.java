/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Represents a single record of a shapefile.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileRecord.java 13937 2010-10-05 23:49:13Z dcollins $
 */
public abstract class ShapefileRecord
{
    protected Shapefile shapeFile;
    protected int recordNumber;
    protected int contentLengthInBytes;
    protected String shapeType;
    protected DBaseRecord attributes;
    protected int numberOfParts;
    protected int numberOfPoints;
    protected int firstPartNumber;
    /** Indicates if the record's point coordinates should be normalized. Defaults to false. */
    protected boolean normalizePoints;

    protected static final int RECORD_HEADER_LENGTH = 8;
    protected static List<String> measureTypes = new ArrayList<String>(Arrays.asList(
        Shapefile.SHAPE_POINT_M, Shapefile.SHAPE_POINT_Z,
        Shapefile.SHAPE_MULTI_POINT_M, Shapefile.SHAPE_MULTI_POINT_Z,
        Shapefile.SHAPE_POLYLINE_M, Shapefile.SHAPE_POLYLINE_Z,
        Shapefile.SHAPE_POLYGON_M, Shapefile.SHAPE_POLYGON_Z
    ));

    /**
     * Constructs a record instance from the given {@link java.nio.ByteBuffer}. The buffer's current position must be
     * the start of the record, and will be the start of the next record when the constructor returns.
     *
     * @param shapeFile the parent {@link Shapefile}.
     * @param buffer    the shapefile record {@link java.nio.ByteBuffer} to read from.
     *
     * @throws IllegalArgumentException if any argument is null or otherwise invalid.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if the record's shape type does not match that of the shapefile.
     */
    public ShapefileRecord(Shapefile shapeFile, ByteBuffer buffer)
    {
        if (shapeFile == null)
        {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Save the buffer's current position.
        int pos = buffer.position();
        try
        {
            this.readFromBuffer(shapeFile, buffer);
        }
        finally
        {
            // Move to the end of the record.
            buffer.position(pos + this.contentLengthInBytes + RECORD_HEADER_LENGTH);
        }
    }

    /**
     * Returns the shapefile containing this record.
     *
     * @return the shapefile containing this record.
     */
    public Shapefile getShapeFile()
    {
        return this.shapeFile;
    }

    /**
     * Returns the zero-orgin ordinal position of the record in the shapefile.
     *
     * @return the record's ordinal position in the shapefile.
     */
    public int getRecordNumber()
    {
        return this.recordNumber;
    }

    /**
     * Returns the record's shape type.
     *
     * @return the record' shape type. See {@link Shapefile} for a list of the defined shape types.
     */
    public String getShapeType()
    {
        return this.shapeType;
    }

    /**
     * Returns the record's attributes.
     *
     * @return the record's attributes.
     */
    public DBaseRecord getAttributes()
    {
        return this.attributes;
    }

    /**
     * Returns the number of parts in the record.
     *
     * @return the number of parts in the record.
     */
    public int getNumberOfParts()
    {
        return this.numberOfParts;
    }

    /**
     * Returns the first part number in the record.
     *
     * @return the first part number in the record.
     */
    public int getFirstPartNumber()
    {
        return this.firstPartNumber;
    }

    /**
     * Returns the last part number in the record.
     *
     * @return the last part number in the record.
     */
    public int getLastPartNumber()
    {
        return this.firstPartNumber + this.numberOfParts - 1;
    }

    /**
     * Returns the number of points in the record.
     *
     * @return the number of points in the record.
     */
    public int getNumberOfPoints()
    {
        return this.numberOfPoints;
    }

    /**
     * Returns the number of points in a specified part of the record.
     *
     * @param partNumber the part number for which to return the number of points.
     *
     * @return the number of points in the specified part.
     */
    public int getNumberOfPoints(int partNumber)
    {
        if (partNumber < 0 || partNumber >= this.getNumberOfParts())
        {
            String message = Logging.getMessage("generic.indexOutOfRange", partNumber);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int shapefilePartNumber = this.getFirstPartNumber() + partNumber;
        return this.getShapeFile().getPointBuffer().subBufferSize(shapefilePartNumber);
    }

    /**
     * Returns the {@link gov.nasa.worldwind.util.VecBuffer} holding the X and Y points of a specified part.
     *
     * @param partNumber the part for which to return the point buffer.
     *
     * @return the buffer holding the part's points. The points are ordered X0,Y0,X1,Y1,...Xn-1,Yn-1, where "n" is the
     *         number of points in the part.
     */
    public VecBuffer getPointBuffer(int partNumber)
    {
        if (partNumber < 0 || partNumber >= this.getNumberOfParts())
        {
            String message = Logging.getMessage("generic.indexOutOfRange", partNumber);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int shapefilePartNumber = this.getFirstPartNumber() + partNumber;
        return this.getShapeFile().getPointBuffer().subBuffer(shapefilePartNumber);
    }

    /**
     * Returns the {@link gov.nasa.worldwind.util.CompoundVecBuffer} holding all the X and Y points for this record. The
     * returned buffer contains one sub-buffer for each of this record's parts. The coordinates for each part are
     * referenced by invoking {@link gov.nasa.worldwind.util.CompoundVecBuffer#subBuffer(int)}, where the index is one
     * of this record's part IDs, starting with 0 and ending with <code>{@link #getNumberOfParts()} - 1</code>
     * (inclusive).
     *
     * @return a CompoundVecBuffer that holds this record's coordinate data.
     */
    public CompoundVecBuffer getCompoundPointBuffer()
    {
        return this.getShapeFile().getPointBuffer().slice(this.getFirstPartNumber(), this.getLastPartNumber());
    }

    /**
     * Reads and parses the contents of a shapefile record from a specified buffer. The buffer's current position must
     * be the start of the record and will be the start of the next record when the constructor returns.
     *
     * @param shapefile the containing {@link Shapefile}.
     * @param buffer    the shapefile record {@link java.nio.ByteBuffer} to read from.
     */
    protected abstract void readFromBuffer(Shapefile shapefile, ByteBuffer buffer);

    /**
     * Verifies that the record's shape type matches the expected one, typically that of the shapefile. Throws an
     * exception if the types do not match and the shape type is not {@link Shapefile#SHAPE_NULL}.
     *
     * @param shapefile the shapefile.
     * @param shapeType the record's shape type.
     *
     * @throws WWRuntimeException       if the shape types do not match.
     * @throws IllegalArgumentException if the specified shape type is null.
     */
    protected void validateShapeType(Shapefile shapefile, String shapeType)
    {
        if (shapeType == null)
        {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!(shapeType.equals(shapefile.getShapeType()) || shapeType.equals(Shapefile.SHAPE_NULL)))
        {
            String message = Logging.getMessage("SHP.UnsupportedShapeType", shapeType);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
    }

    /**
     * Indicates whether the record is a shape type capable of containing optional measure values. Does not indicate
     * whether the record actually contains measure values.
     *
     * @return true if the record may contain measure values.
     */
    protected boolean isMeasureType()
    {
        return Shapefile.isMeasureType(this.getShapeType());
    }

    /**
     * Indicates whether the record is a shape type containing Z values.
     *
     * @return true if the record is a type containing Z values.
     */
    protected boolean isZType()
    {
        return Shapefile.isZType(this.getShapeType());
    }

    /**
     * Returns whether the record's point coordinates should be normalized.
     *
     * @return <code>true</code> if the record's points should be normalized; <code>false</code> otherwise.
     */
    public boolean isNormalizePoints()
    {
        return this.normalizePoints;
    }

    /**
     * Specifies if the record's point coordinates should be normalized. Defaults to <code>false</code>.
     *
     * @param normalizePoints <code>true</code> if the record's points should be normalized; <code>false</code>
     *                        otherwise.
     */
    public void setNormalizePoints(boolean normalizePoints)
    {
        this.normalizePoints = normalizePoints;
    }
}
