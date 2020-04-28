/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.Logging;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.*;

/**
 * Utilities for working with shapefiles.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileUtils.java 13883 2010-09-28 03:12:07Z dcollins $
 */
public class ShapefileUtils
{
    public static Shapefile openZippedShapefile(File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        InputStream shpStream = null, shxStream = null, dbfStream = null, prjStream = null;

        ZipFile zipFile;
        try
        {
            zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

            while (zipEntries.hasMoreElements())
            {
                ZipEntry entry = zipEntries.nextElement();
                if (entry == null)
                    continue;

                if (entry.getName().toLowerCase().endsWith(Shapefile.SHAPE_FILE_SUFFIX))
                {
                    shpStream = zipFile.getInputStream(entry);
                }
                else if (entry.getName().toLowerCase().endsWith(Shapefile.INDEX_FILE_SUFFIX))
                {
                    shxStream = zipFile.getInputStream(entry);
                }
                else if (entry.getName().toLowerCase().endsWith(Shapefile.ATTRIBUTE_FILE_SUFFIX))
                {
                    dbfStream = zipFile.getInputStream(entry);
                }
                else if (entry.getName().toLowerCase().endsWith(Shapefile.PROJECTION_FILE_SUFFIX))
                {
                    prjStream = zipFile.getInputStream(entry);
                }
            }
        }
        catch (Exception e)
        {
            throw new WWRuntimeException(
                Logging.getMessage("generic.ExceptionAttemptingToReadFrom", file.getPath()), e);
        }

        if (shpStream == null)
        {
            String message = Logging.getMessage("SHP.NotAShapeFile", file.getPath());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new Shapefile(shpStream, shxStream, dbfStream, prjStream);
    }

    /**
     * Reads and returns an array of integers from a byte buffer.
     *
     * @param buffer     the byte buffer to read from.
     * @param numEntries the number of integers to read.
     *
     * @return the integers read.
     *
     * @throws IllegalArgumentException if the specified buffer reference is null.
     */
    public static int[] readIntArray(ByteBuffer buffer, int numEntries)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int[] array = new int[numEntries];
        for (int i = 0; i < numEntries; i++)
        {
            array[i] = buffer.getInt();
        }

        return array;
    }

    /**
     * Reads and returns an array of doubles from a byte buffer.
     *
     * @param buffer     the byte buffer to read from.
     * @param numEntries the number of doubles to read.
     *
     * @return the doubles read.
     *
     * @throws IllegalArgumentException if the specified buffer reference is null.
     */
    public static double[] readDoubleArray(ByteBuffer buffer, int numEntries)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] array = new double[numEntries];
        for (int i = 0; i < numEntries; i++)
        {
            array[i] = buffer.getDouble();
        }

        return array;
    }
}
