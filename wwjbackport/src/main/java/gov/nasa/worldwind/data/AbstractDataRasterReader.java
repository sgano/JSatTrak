/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: AbstractDataRasterReader.java 14014 2010-10-22 20:46:56Z garakl $
 */
public abstract class AbstractDataRasterReader extends AVListImpl implements DataRasterReader
{
    private final String description;
    private final String[] mimeTypes;
    private final String[] suffixes;

    public AbstractDataRasterReader(String description, String[] mimeTypes, String[] suffixes)
    {
        this.description = description;
        this.mimeTypes = copyOf(mimeTypes);
        this.suffixes = copyOf(suffixes);

        this.setValue(AVKey.SERVICE_NAME, AVKey.SERVICE_NAME_OFFLINE);
    }

    public AbstractDataRasterReader(String[] mimeTypes, String[] suffixes)
    {
        this(descriptionFromSuffixes(suffixes), mimeTypes, suffixes);
    }

    protected AbstractDataRasterReader(String description)
    {
        this(description, new String[0], new String[0]);
    }

    public String getDescription()
    {
        return this.description;
    }

    public String[] getMimeTypes()
    {
        String[] copy = new String[mimeTypes.length];
        System.arraycopy(mimeTypes, 0, copy, 0, mimeTypes.length);
        return copy;
    }

    public String[] getSuffixes()
    {
        String[] copy = new String[suffixes.length];
        System.arraycopy(suffixes, 0, copy, 0, suffixes.length);
        return copy;
    }

    public boolean canRead(Object source, AVList params)
    {
        if (source == null)
            return false;

        //noinspection SimplifiableIfStatement
        if (!this.canReadSuffix(source))
            return false;

        return this.doCanRead(source, params);
    }

    protected boolean canReadSuffix(Object source)
    {
        // If the source has no path, we cannot return failure, so return that the test passed.
        String path = WWIO.getSourcePath(source);
        if (path == null)
            return true;

        // If the source has a suffix, then we return success if this reader supports the suffix.
        String pathSuffix = WWIO.getSuffix(path);
        boolean matchesAny = false;
        for (String suffix : suffixes)
        {
            if (suffix.equalsIgnoreCase(pathSuffix))
            {
                matchesAny = true;
                break;
            }
        }
        return matchesAny;
    }

    public DataRaster[] read(Object source, AVList params) throws java.io.IOException
    {
        if (!this.canRead(source, params))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        return this.doRead(source, params);
    }

    public void readMetadata(Object source, AVList params) throws java.io.IOException
    {
        if (!this.canRead(source, params))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doReadMetadata(source, params);

        String message = this.validateMetadata(source, params);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }
    }

    protected String validateMetadata(Object source, AVList params)
    {
        StringBuilder sb = new StringBuilder();

        Object o = (params != null) ? params.getValue(AVKey.WIDTH) : null;
        if (o == null || !(o instanceof Integer))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoSizeSpecified", source));

        o = (params != null) ? params.getValue(AVKey.HEIGHT) : null;
        if (o == null || !(o instanceof Integer))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoSizeSpecified", source));

        o = (params != null) ? params.getValue(AVKey.SECTOR) : null;
        if (o == null || !(o instanceof Sector))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoSectorSpecified", source));

        if (sb.length() == 0)
            return null;

        return sb.toString();
    }

    protected abstract boolean doCanRead(Object source, AVList params);

    protected abstract DataRaster[] doRead(Object source, AVList params) throws java.io.IOException;

    protected abstract void doReadMetadata(Object source, AVList params) throws java.io.IOException;

    //**************************************************************//
    //********************  Utilities  *****************************//
    //**************************************************************//

    private static String[] copyOf(String[] array)
    {
        String[] copy = new String[array.length];
        for (int i = 0; i < array.length; i++)
        {
            // TODO garakl
//            copy[i] = array[i].toLowerCase();
            copy[i] = array[i].toString();
        }
        return copy;
    }

    private static String descriptionFromSuffixes(String[] suffixes)
    {
        StringBuilder sb = new StringBuilder();
        for (String suffix : suffixes)
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append("*.").append(suffix.toLowerCase());
        }
        return sb.toString();
    }
}
