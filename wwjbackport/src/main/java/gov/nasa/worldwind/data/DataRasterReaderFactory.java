/*
Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id: DataRasterReaderFactory.java 14056 2010-10-27 09:11:24Z garakl $
 */

public class DataRasterReaderFactory
{
    protected static DataRasterReader[] readers = new DataRasterReader[]
        {
            new GDALDataRasterReader(),
            new ImageIORasterReader(),
            new GeotiffRasterReader(),
            new RPFRasterReader(),
            new BILRasterReader()
//            TODO garakl make DTEDRasterReader
        };

    public static DataRasterReader findReaderFor(Object source, AVList params)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return findReaderFor(source, params, readers);
    }

    public static DataRasterReader findReaderFor(Object source, AVList params, DataRasterReader[] readers)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (readers == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (DataRasterReader reader : readers)
        {
            if (reader != null)
            {
                if (reader.canRead(source, params))
                    return reader;
            }
        }

        return null;
    }
}
