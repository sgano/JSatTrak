/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.formats.tiff.GeotiffReader;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: GeotiffRasterReader.java 13386 2010-05-14 02:15:20Z garakl $
 */
public class GeotiffRasterReader extends AbstractDataRasterReader
{
    private static final String[] geotiffMimeTypes = {"image/tiff", "image/geotiff"};
    private static final String[] geotiffSuffixes = {"tif", "tiff", "gtif", "tif.zip", "tiff.zip", "tif.gz", "tiff.gz"};

    public GeotiffRasterReader()
    {
        super(geotiffMimeTypes, geotiffSuffixes);
    }

    protected boolean doCanRead(Object source, AVList params)
    {
        String path = WWIO.getSourcePath(source);
        if (path == null)
        {
            return false;
        }

        GeotiffReader reader = null;
        try
        {
            reader = new GeotiffReader(path);
            boolean isGeoTiff = reader.isGeotiff(0);
            if (!isGeoTiff)
            {
                isGeoTiff = this.canReadWorldFiles(source);
            }
            return isGeoTiff;
        }
        catch (Exception e)
        {
            // Intentionally ignoring exceptions.
            return false;
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    protected DataRaster[] doRead(Object source, AVList params) throws java.io.IOException
    {
        String path = WWIO.getSourcePath(source);
        if (path == null)
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        GeotiffReader reader = null;
        DataRaster[] rasters = null;
        try
        {
            reader = new GeotiffReader(path);
            rasters = reader.readDataRaster();
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
        return rasters;
    }

    protected void doReadMetadata(Object source, AVList params) throws java.io.IOException
    {
        String path = WWIO.getSourcePath(source);
        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        GeotiffReader reader = null;
        try
        {
            reader = new GeotiffReader(path);
            reader.copyMetadataTo(params);

            boolean isGeoTiff = reader.isGeotiff(0);
            if (!isGeoTiff && params.hasKey(AVKey.WIDTH) && params.hasKey(AVKey.HEIGHT))
            {
                int[] size = new int[2];

                size[0] = (Integer) params.getValue(AVKey.WIDTH);
                size[1] = (Integer) params.getValue(AVKey.HEIGHT);

                params.setValue(WorldFile.WORLD_FILE_IMAGE_SIZE, size);

                this.readWorldFiles(source, params);

                Object o = params.getValue(AVKey.SECTOR);
                if (o == null || !(o instanceof Sector))
                {
                    ImageUtil.calcBoundingBoxForUTM(params);
                }
            }
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    private boolean canReadWorldFiles(Object source)
    {
        if (!(source instanceof java.io.File))
        {
            return false;
        }

        try
        {
            java.io.File[] worldFiles = WorldFile.getWorldFiles((java.io.File) source);
            if (worldFiles == null || worldFiles.length == 0)
            {
                return false;
            }
        }
        catch (java.io.IOException e)
        {
            // Not interested in logging the exception, we only want to report the failure to read.
            return false;
        }

        return true;
    }

    private void readWorldFiles(Object source, AVList params) throws java.io.IOException
    {
        if (!(source instanceof java.io.File))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        java.io.File[] worldFiles = WorldFile.getWorldFiles((java.io.File) source);
        WorldFile.decodeWorldFiles(worldFiles, params);

        // Translate the property WORLD_FILE_IMAGE_SIZE to separate properties WIDTH and HEIGHT.
        Object o = params.getValue(WorldFile.WORLD_FILE_IMAGE_SIZE);
        if (o != null && o instanceof int[])
        {
            int[] size = (int[]) o;

            if (!params.hasKey(AVKey.WIDTH))
                params.setValue(AVKey.WIDTH, size[0]);

            if (!params.hasKey(AVKey.HEIGHT))
                params.setValue(AVKey.HEIGHT, size[1]);
        }
    }
}
