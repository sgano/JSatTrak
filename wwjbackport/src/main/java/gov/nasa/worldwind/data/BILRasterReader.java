/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

import java.io.File;

/**
 * @author dcollins
 * @version $Id: BILRasterReader.java 14145 2010-11-22 20:27:06Z garakl $
 */
public class BILRasterReader extends AbstractDataRasterReader
{
    private static final String[] bilMimeTypes = new String[]
        {"image/bil", "application/bil", "application/bil16", "application/bil32"};

    private static final String[] bilSuffixes = new String[]
        {"bil", "bil16", "bil32", "bil.gz", "bil16.gz", "bil32.gz"};

    private boolean mapLargeFiles = false;
    private long largeFileThreshold = 16777216L; // 16 megabytes

    public BILRasterReader()
    {
        super(bilMimeTypes, bilSuffixes);
    }

    public boolean isMapLargeFiles()
    {
        return this.mapLargeFiles;
    }

    public void setMapLargeFiles(boolean mapLargeFiles)
    {
        this.mapLargeFiles = mapLargeFiles;
    }

    public long getLargeFileThreshold()
    {
        return this.largeFileThreshold;
    }

    public void setLargeFileThreshold(long largeFileThreshold)
    {
        if (largeFileThreshold < 0L)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "largeFileThreshold < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.largeFileThreshold = largeFileThreshold;
    }

    protected boolean doCanRead(Object source, AVList params)
    {
        if (!(source instanceof java.io.File) && !(source instanceof java.net.URL))
            return false;

        // If the data source doesn't already have all the necessary metadata, then we determine whether or not
        // the missing metadata can be read.
        if (this.validateMetadata(source, params) != null)
        {
            if (!this.canReadWorldFiles(source))
                return false;
        }

        if (!params.hasKey(AVKey.PIXEL_FORMAT))
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);

        if (!params.hasKey(AVKey.RASTER_TYPE))
            params.setValue(AVKey.RASTER_TYPE, AVKey.RASTER_TYPE_ELEVATION);

        return true;
    }

    protected DataRaster[] doRead(Object source, AVList params) throws java.io.IOException
    {
        java.nio.ByteBuffer byteBuffer = this.readElevations(source);

        // If the parameter list is null, or doesn't already have all the necessary metadata, then we copy the parameter
        // list and attempt to populate the copy with any missing metadata.        
        if (this.validateMetadata(source, params) != null)
        {
            // Copy the parameter list to insulate changes from the caller.
            params = (params != null) ? params.copy() : new AVListImpl();
            params.setValue(AVKey.FILE_SIZE, byteBuffer.capacity());
            this.readWorldFiles(source, params);
        }

        int width = (Integer) params.getValue(AVKey.WIDTH);
        int height = (Integer) params.getValue(AVKey.HEIGHT);
        Sector sector = (Sector) params.getValue(AVKey.SECTOR);

        // Translate the property PIXEL_TYPE to the property DATA_TYPE.
        if (params.getValue(AVKey.DATA_TYPE) == null)
            params.setValue(AVKey.DATA_TYPE, params.getValue(AVKey.PIXEL_TYPE));

        if (!params.hasKey(AVKey.PIXEL_FORMAT))
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);

        if (!params.hasKey(AVKey.RASTER_TYPE))
            params.setValue(AVKey.RASTER_TYPE, AVKey.RASTER_TYPE_ELEVATION);

        ByteBufferRaster raster = new ByteBufferRaster(width, height, sector, byteBuffer, params);

        // This code expects the string "gov.nasa.worldwind.avkey.MissingDataValue", which now corresponds to the
        // key MISSING_DATA_REPLACEMENT.
        Double missingDataValue = (Double) params.getValue(AVKey.MISSING_DATA_REPLACEMENT);
        if (missingDataValue != null)
            raster.setTransparentValue(missingDataValue);

        return new DataRaster[] {raster};
    }

    protected void doReadMetadata(Object source, AVList params) throws java.io.IOException
    {
        if (this.validateMetadata(source, params) != null)
        {
            this.readWorldFiles(source, params);
        }
    }

    protected String validateMetadata(Object source, AVList params)
    {
        StringBuilder sb = new StringBuilder();

        String message = super.validateMetadata(source, params);
        if (message != null)
            sb.append(message);

        Object o = (params != null) ? params.getValue(AVKey.BYTE_ORDER) : null;
        if (o == null || !(o instanceof String))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoByteOrderSpecified", source));

        o = (params != null) ? params.getValue(AVKey.PIXEL_FORMAT) : null;
        if (o == null)
        {
            sb.append(sb.length() > 0 ? ", " : "").append(
                Logging.getMessage("WorldFile.NoPixelFormatSpecified", source));
        }
        else if (!AVKey.ELEVATION.equals(o))
        {
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.InvalidPixelFormat", source));
        }

        o = (params != null) ? params.getValue(AVKey.PIXEL_TYPE) : null;
        if (o == null)
        {
            o = (params != null) ? params.getValue(AVKey.DATA_TYPE) : null;
            if (o == null)
            {
                sb.append(sb.length() > 0 ? ", " : "").append(
                    Logging.getMessage("WorldFile.NoPixelTypeSpecified", source));
            }
        }

        if (sb.length() == 0)
            return null;

        return sb.toString();
    }

    private boolean canReadWorldFiles(Object source)
    {
        if (!(source instanceof java.io.File))
            return false;

        try
        {
            java.io.File[] worldFiles = WorldFile.getWorldFiles((java.io.File) source);
            if (worldFiles == null || worldFiles.length == 0)
                return false;
        }
        catch (java.io.IOException e)
        {
            // Not interested in logging the exception, we only want to report the failure to read.
            return false;
        }

        return true;
    }

    private java.nio.ByteBuffer readElevations(Object source) throws java.io.IOException
    {
        if (!(source instanceof java.io.File) && !(source instanceof java.net.URL))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        File file = (source instanceof java.io.File) ? (File) source : null;
        java.net.URL url = (source instanceof java.net.URL) ? (java.net.URL) source : null;

        if (null == file && "file".equalsIgnoreCase(url.getProtocol()))
            file = new File(url.getFile());

        if (null != file)
        {
            // handle .bil.zip, .bil16.zip, and .bil32.gz files
            if (file.getName().toLowerCase().endsWith(".zip"))
            {
                return WWIO.readZipEntryToBuffer(file, null);
            }
            // handle bil.gz, bil16.gz, and bil32.gz files
            else if (file.getName().toLowerCase().endsWith(".gz"))
            {
                return WWIO.readGZipFileToBuffer(file);
            }
            else if (!this.isMapLargeFiles() || (this.getLargeFileThreshold() > file.length()))
            {
                return WWIO.readFileToBuffer(file);
            }
            else
            {
                return WWIO.mapFile(file);
            }
        }
        else // (source instanceof java.net.URL)
        {
            return WWIO.readURLContentToBuffer(url);
        }
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
            params.setValue(AVKey.WIDTH, size[0]);
            params.setValue(AVKey.HEIGHT, size[1]);
        }
    }
}
