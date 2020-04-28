/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.util.logging.Level;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALDataRasterReader.java 14056 2010-10-27 09:11:24Z garakl $
 */

public class GDALDataRasterReader extends AbstractDataRasterReader
{
    // Extract list of mime types supported by GDAL
    protected static final String[] mimeTypes = new String[] {
        "image/jp2", "image/jpeg2000", "image/jpeg2000-image", "image/x-jpeg2000-image",
        "image/x-mrsid-image",
        "image/jpeg", "image/png", "image/bmp", "image/tif"
    };

    // TODO Extract list of extensions supported by GDAL
    protected static final String[] suffixes = new String[] {
        "jp2", "sid", "ntf", "nitf",
        "JP2", "SID", "NTF", "NITF",

        "jpg", "jpe", "jpeg",   /* "image/jpeg" */
        "png",                  /* "image/png" */
        "bmp",                  /* "image/bmp" */
        "TIF", "TIFF", "GTIF", "GTIFF", "tif", "tiff", "gtif", "gtiff",     /* "image/tif" */

        // Elevations

        // DTED
        "dt0", "dt1", "dt2",
        "asc", "adf", "dem"
    };

    public GDALDataRasterReader()
    {
        super("GDAL-based Data Raster Reader", mimeTypes, suffixes);
    }

    @Override
    public boolean canRead(Object source, AVList params)
    {
        if (!GDALUtils.isGDALAvailable())
            return false;

        // RPF imagery cannot be identified by a small set of suffixes or mime types, so we override the standard
        // suffix comparison behavior here.
        return this.doCanRead(source, params);
    }

    @Override
    protected boolean doCanRead(Object source, AVList params)
    {
        if (!GDALUtils.isGDALAvailable())
            return false;

        if (null == source)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            return false;
        }

        if (!(source instanceof File))
        {
            String message = Logging.getMessage("generic.UnexpectedObjectType", source.getClass().getName());
            Logging.logger().severe(message);
            return false;
        }

        return GDALUtils.canOpen((File) source);
    }

    @Override
    protected DataRaster[] doRead(Object source, AVList params) throws IOException
    {
        GDALDataRaster raster = this.readDataRaster(source, false);
        if (null != raster && null != params)
            params.setValues(raster.getMetadata());

        return (null == raster) ? null : new DataRaster[] {raster};
    }

    @Override
    protected void doReadMetadata(Object source, AVList params) throws IOException
    {
        if (!GDALUtils.isGDALAvailable())
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        GDALDataRaster raster = this.readDataRaster(source, true);
        if (null != raster && null != params)
        {
            params.setValues(raster.getMetadata());
            raster.dispose();
        }
    }

    protected GDALDataRaster readDataRaster(Object source, boolean quickReadingMode) throws IOException
    {
        if (!GDALUtils.isGDALAvailable())
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        if (null == source)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!(source instanceof File))
        {
            String message = Logging.getMessage("generic.UnexpectedObjectType", source.getClass().getName());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GDALDataRaster raster = null;

        File file = (File) source;

        try
        {
            raster = new GDALDataRaster(file, quickReadingMode);
            if (null == raster)
            {
                String message = Logging.getMessage("generic.CannotOpenFile", GDALUtils.getErrorMessage());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }
        }
        catch (WWRuntimeException wwre)
        {
            throw wwre;
        }
        catch (Throwable t)
        {
            String message = Logging.getMessage("generic.CannotOpenFile", GDALUtils.getErrorMessage());
            Logging.logger().log(Level.SEVERE, message, t);
            throw new WWRuntimeException(t);
        }

        return raster;
    }
}
