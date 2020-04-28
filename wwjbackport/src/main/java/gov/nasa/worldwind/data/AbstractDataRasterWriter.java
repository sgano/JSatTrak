/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: AbstractDataRasterWriter.java 8321 2009-01-05 17:06:14Z dcollins $
 */
public abstract class AbstractDataRasterWriter implements DataRasterWriter
{
    private final String[] mimeTypes;
    private final String[] suffixes;

    public AbstractDataRasterWriter(String[] mimeTypes, String[] suffixes)
    {
        this.mimeTypes = copyOf(mimeTypes);
        this.suffixes = copyOf(suffixes);
    }

    public String[] getMimeTypes()
    {
        String[] copy = new String[this.mimeTypes.length];
        System.arraycopy(this.mimeTypes, 0, copy, 0, this.mimeTypes.length);
        return copy;
    }

    public String[] getSuffixes()
    {
        String[] copy = new String[this.suffixes.length];
        System.arraycopy(this.suffixes, 0, copy, 0, this.suffixes.length);
        return copy;
    }

    public boolean canWrite(DataRaster raster, String formatSuffix, java.io.File file)
    {
        if (formatSuffix == null)
            return false;

        formatSuffix = stripLeadingPeriod(formatSuffix);

        boolean matchesAny = false;
        for (String suffix : this.suffixes)
        {
            if (suffix.equalsIgnoreCase(formatSuffix))
            {
                matchesAny = true;
                break;
            }
        }

        //noinspection SimplifiableIfStatement
        if (!matchesAny)
            return false;

        return this.doCanWrite(raster, formatSuffix, file);
    }

    public void write(DataRaster raster, String formatSuffix, java.io.File file) throws java.io.IOException
    {
        if (raster == null)
        {
            String message = Logging.getMessage("nullValue.RasterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (formatSuffix == null)
        {
            String message = Logging.getMessage("nullValue.FormatSuffixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        formatSuffix = stripLeadingPeriod(formatSuffix);        
        if (!this.canWrite(raster, formatSuffix, file))
        {
            String message = Logging.getMessage("DataRaster.CannotWrite", raster, formatSuffix, file);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doWrite(raster, formatSuffix, file);
    }

    protected abstract boolean doCanWrite(DataRaster raster, String formatSuffix, java.io.File file);

    protected abstract void doWrite(DataRaster raster, String formatSuffix, java.io.File file) throws java.io.IOException;

    //**************************************************************//
    //********************  Utilities  *****************************//
    //**************************************************************//

    private static String[] copyOf(String[] array)
    {
        String[] copy = new String[array.length];
        for (int i = 0; i < array.length; i++)
            copy[i] = array[i].toLowerCase();
        return copy;
    }

    private static String stripLeadingPeriod(String s)
    {
        if (s.startsWith("."))
            return s.substring(Math.min(1, s.length()), s.length());
        return s;
    }
}
