/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.formats.dds.*;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: DDSRasterWriter.java 8941 2009-02-21 00:33:27Z dcollins $
 */
public class DDSRasterWriter extends AbstractDataRasterWriter
{
    private static final String[] ddsMimeTypes = {"image/dds"};
    private static final String[] ddsSuffixes = {"dds"};

    public DDSRasterWriter()
    {
        super(ddsMimeTypes, ddsSuffixes);
    }

    protected boolean doCanWrite(DataRaster raster, String formatSuffix, File file)
    {
        return (raster != null) && (raster instanceof BufferedImageRaster);
    }

    protected void doWrite(DataRaster raster, String formatSuffix, File file) throws IOException
    {
        BufferedImageRaster bufferedImageRaster = (BufferedImageRaster) raster;
        java.awt.image.BufferedImage image = bufferedImageRaster.getBufferedImage();
        
        java.nio.ByteBuffer byteBuffer = DDSCompressor.compressImage(image);
        // Do not force changes to the underlying filesystem. This drastically improves write performance.
        boolean forceFilesystemWrite = false;
        WWIO.saveBuffer(byteBuffer, file, forceFilesystemWrite);
    }
}
