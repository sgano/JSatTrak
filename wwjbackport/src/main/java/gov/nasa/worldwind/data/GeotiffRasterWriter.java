/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.formats.tiff.GeotiffWriter;
import gov.nasa.worldwind.util.Logging;

import java.io.*;

/**
 * @author Lado Garakanidze
 * @version $Id$
 */
public class GeotiffRasterWriter extends AbstractDataRasterWriter
{
    private static final String[] geotiffMimeTypes = {"image/tiff", "image/geotiff"};
    private static final String[] geotiffSuffixes = {"tif", "tiff", "gtif"};

    public GeotiffRasterWriter()
    {
        super( geotiffMimeTypes, geotiffSuffixes );
    }

    protected boolean doCanWrite(DataRaster raster, String formatSuffix, File file)
    {
        return (raster != null) && (raster instanceof BufferedImageRaster || raster instanceof ByteBufferRaster );
    }

    protected void doWrite(DataRaster raster, String formatSuffix, File file) throws IOException
    {
        if( null == file )
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if( null == raster )
        {
            String message = Logging.getMessage("nullValue.RasterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

//        String ext = file.getName().substring( file.getName().lastIndexOf('.') + 1 );
//        for( String suffix : geotiffSuffixes )
//        {
//            // TODO validate file's extension vs geotiffSuffixes and formatSuffix(?!)
//        }

        GeotiffWriter writer = null;
        try
        {
            writer = new GeotiffWriter( file );
            writer.write( raster );
        }
        finally
        {
            if( null != writer )
                writer.close();
        }
    }
}
