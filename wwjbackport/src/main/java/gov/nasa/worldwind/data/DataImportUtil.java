/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.util.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;

/**
 * DataImportUtil is a collection of utility methods for common data import tasks.
 *
 * @author dcollins
 * @version $Id: DataImportUtil.java 14056 2010-10-27 09:11:24Z garakl $
 */
public class DataImportUtil
{
    /**
     * Returns true if the specified input source is non-null and represents elevation data, and false otherwise. The
     * input source may be one of the following: <ul> <li>{@link String}</li> <li>{@link java.io.File}</li> <li>{@link
     * java.net.URL}</li> <li>{@link java.net.URI}</li> <li>{@link java.io.InputStream}</li> </ul> Supported input
     * source formats are: <ul> <li>BIL (Band Interleaved by Line)</li> </ul>
     *
     * @param source the input source reference to test as a elevation data.
     *
     * @return true if the input source is elevation data, and false otherwise.
     *
     * @throws IllegalArgumentException if the input source is null.
     */
    public static boolean isElevationData(Object source)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean isElevation = false;

        AVList params = new AVListImpl();
        DataRasterReader reader = DataRasterReaderFactory.findReaderFor(source, params);
        if (reader != null)
        {
            if (!params.hasKey(AVKey.RASTER_TYPE) && !params.hasKey(AVKey.PIXEL_FORMAT))
            {
                try
                {
                    reader.readMetadata(source, params);
                }
                catch (IOException e)
                {
                    // Reading the input source's metadata caused an exception. This exception does not prevent us from
                    // determining if the source represents elevation data, but we want to make a note of it. Therefore we
                    // log the exception with level FINE.
                    String message = Logging.getMessage("generic.ExceptionWhileReading", source);
                    Logging.logger().log(java.util.logging.Level.FINE, message, e);
                }
            }

            isElevation = AVKey.ELEVATION.equals(params.getValue(AVKey.PIXEL_FORMAT));
            if (isElevation == false)
                isElevation = AVKey.RASTER_TYPE_ELEVATION.equals(params.getValue(AVKey.RASTER_TYPE));
        }

        return isElevation;
    }

    /**
     * Returns true if the specified input source is non-null and represents image data, and false otherwise. The input
     * source may be one of the following: <ul> <li>{@link String}</li> <li>{@link java.io.File}</li> <li>{@link
     * java.net.URL}</li> <li>{@link java.net.URI}</li> <li>{@link java.io.InputStream}</li> </ul> Supported input
     * source formats are: <ul> <li>BMP (with georeferencing file)</li> <li>GeoTIFF</li> <li>GIF (with georeferencing
     * file)</li> <li>JPEG (with georeferencing file)</li> <li>PNG (with georeferencing file)</li> <li>RPF (Raster
     * Product Format)</li> <li>TIFF (with georeferencing file)</li> <li>WBMP (with georeferencing file)</li> <li>X-PNG
     * (with georeferencing file)</li> </ul>
     *
     * @param source the input source reference to test as image data.
     *
     * @return true if the input source is image data, and false otherwise.
     *
     * @throws IllegalArgumentException if the input source is null.
     */
    public static boolean isImageData(Object source)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean isImage = false;

        AVList params = new AVListImpl();
        DataRasterReader reader = DataRasterReaderFactory.findReaderFor(source, params);

        if (reader != null)
        {
            if (!params.hasKey(AVKey.RASTER_TYPE) && !params.hasKey(AVKey.PIXEL_FORMAT))
            {
                try
                {
                    reader.readMetadata(source, params);
                }
                catch (IOException e)
                {
                    // Reading the input source's metadata caused an exception. This exception does not prevent us from
                    // determining if the source represents elevation data, but we want to make a note of it. Therefore we
                    // log the exception with level FINE.
                    String message = Logging.getMessage("generic.ExceptionWhileReading", source);
                    Logging.logger().log(java.util.logging.Level.FINE, message, e);
                }
            }

            isImage = AVKey.IMAGE.equals(params.getValue(AVKey.PIXEL_FORMAT));

            if (isImage == false)
            {
                Object type = params.getValue(AVKey.RASTER_TYPE);
                isImage = (AVKey.RASTER_TYPE_COLOR_IMAGE.equals(type) || AVKey.RASTER_TYPE_MONOCHROME_IMAGE.equals(
                    type));
            }
        }

        return isImage;
    }

    /**
     * Returns true if the specified input source is non-null and represents a reference to a World Wind .NET LayerSet
     * XML document, and false otherwise. The input source may be one of the following: <ul> <li>{@link String}</li>
     * <li>{@link java.io.File}</li> <li>{@link java.net.URL}</li> <li>{@link java.net.URI}</li> <li>{@link
     * java.io.InputStream}</li> </ul>
     *
     * @param source the input source reference to test as a World Wind .NET LayerSet document.
     *
     * @return true if the input source is a World Wind .NET LayerSet document, and false otherwise.
     *
     * @throws IllegalArgumentException if the input source is null.
     */
    public static boolean isWWDotNetLayerSet(Object source)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String path = WWIO.getSourcePath(source);
        if (path != null)
        {
            String suffix = WWIO.getSuffix(path);
            if (suffix != null && !suffix.toLowerCase().endsWith("xml"))
                return false;
        }

        // Open the document in question as an XML event stream. Since we're only interested in testing the document
        // element, we avoiding any unecessary overhead incurred from parsing the entire document as a DOM.
        XMLEventReader eventReader = null;
        try
        {
            eventReader = WWXML.openEventReader(source);

            // Get the first start element event, if any exists, then determine if it represents a LayerSet
            // configuration document.
            XMLEvent event = WWXML.nextStartElementEvent(eventReader);
            return event != null && DataConfigurationUtils.isWWDotNetLayerSetConfigEvent(event);
        }
        catch (Exception e)
        {
            Logging.logger().fine(Logging.getMessage("generic.ExceptionAttemptingToParseXml", source));
            return false;
        }
        finally
        {
            WWXML.closeEventReader(eventReader, source.toString());
        }
    }

    /**
     * Returns a location in the specified {@link gov.nasa.worldwind.cache.FileStore} which should be used as the
     * default location for importing data. This attempts to use the first FileStore location marked as an "install"
     * location. If no install location exists, this falls back to the FileStore's default write location, the same
     * location where downloaded data is cached.
     * <p/>
     * The returned {@link java.io.File} represents an abstract path, and therefore may not exist. In this case, the
     * caller must create the missing directories composing the abstract path.
     *
     * @param fileStore the FileStore to determine the default location for importing data.
     *
     * @return the default location in the specified FileStore to be used for importing data.
     *
     * @throws IllegalArgumentException if the FileStore is null.
     */
    public static File getDefaultImportLocation(FileStore fileStore)
    {
        if (fileStore == null)
        {
            String message = Logging.getMessage("nullValue.FileStoreIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (File location : fileStore.getLocations())
        {
            if (fileStore.isInstallLocation(location.getPath()))
                return location;
        }

        return fileStore.getWriteLocation();
    }
}
