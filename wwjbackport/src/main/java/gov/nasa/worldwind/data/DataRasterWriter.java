/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

/**
 * @author dcollins
 * @version $Id: DataRasterWriter.java 8321 2009-01-05 17:06:14Z dcollins $
 */
public interface DataRasterWriter
{
    String[] getMimeTypes();

    String[] getSuffixes();

    boolean canWrite(DataRaster raster, String formatSuffix, java.io.File file);

    void write(DataRaster raster, String formatSuffix, java.io.File file) throws java.io.IOException;
}
