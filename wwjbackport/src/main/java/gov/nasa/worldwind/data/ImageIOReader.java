/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.formats.tiff.GeotiffImageReaderSpi;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.avlist.*;

/**
 * @author dcollins
 * @version $Id: ImageIOReader.java 9716 2009-03-27 16:55:14Z dcollins $
 */
public class ImageIOReader extends BasicDataIODescriptor
{
    static
    {
        javax.imageio.spi.IIORegistry.getDefaultInstance().registerServiceProvider(GeotiffImageReaderSpi.inst());
    }

    public ImageIOReader()
    {
        super(javax.imageio.ImageIO.getReaderMIMETypes(), getImageIOReaderSuffixes());
    }

    public boolean canRead(Object input)
    {
        if (input == null)
            return false;

        // Use format suffix as an early exit criteria. If the input has a format suffix, and it does not match, then
        // immediately return false.

        String suffix = getSuffixFor(input);
        //noinspection SimplifiableIfStatement
        if (suffix != null && !this.matchesFormatSuffix(suffix))
            return false;

        return this.doCanRead(input);
    }

    public java.awt.image.BufferedImage read(Object input) throws java.io.IOException
    {
        if (!this.canRead(input))
        {
            String message = Logging.getMessage("generic.InvalidDataSource", input);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        return this.doRead(input);
    }

    public void readMetadata(Object input, AVList values) throws java.io.IOException
    {
        if (!this.canRead(input))
        {
            String message = Logging.getMessage("generic.InvalidDataSource", input);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }
        if (values == null)
        {
            String message = Logging.getMessage("nullValue.AVListIsNull");
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        this.doReadMetadata(input, values);
    }

    protected boolean doCanRead(Object input)
    {
        javax.imageio.stream.ImageInputStream iis = null;
        javax.imageio.ImageReader reader = null;
        try
        {
            iis = createInputStream(input);
            if (iis != null)
                reader = readerFor(iis);
        }
        catch (Exception ignored)
        {
        }
        finally
        {
            if (reader != null)
            {
                reader.dispose();
            }

            try
            {
                if (iis != null)
                {
                    iis.close();
                }
            }
            catch (Exception ignored)
            {
            }
        }

        return reader != null;
    }

    protected java.awt.image.BufferedImage doRead(Object input) throws java.io.IOException
    {
        javax.imageio.stream.ImageInputStream iis = createInputStream(input);
        return javax.imageio.ImageIO.read(iis);
    }

    protected void doReadMetadata(Object input, AVList values) throws java.io.IOException
    {
        javax.imageio.stream.ImageInputStream iis = createInputStream(input);
        javax.imageio.ImageReader reader = readerFor(iis);
        try
        {
            if (reader == null)
            {
                String message = Logging.getMessage("generic.UnrecognizedImageSourceType", input);
                Logging.logger().severe(message);
                throw new java.io.IOException(message);
            }

            reader.setInput(iis, true, true);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            values.setValue(AVKey.WIDTH, width);
            values.setValue(AVKey.HEIGHT, height);
        }
        finally
        {
            if (reader != null)
                reader.dispose();
            iis.close();
        }
    }

    protected static javax.imageio.stream.ImageInputStream createInputStream(Object input) throws java.io.IOException
    {
        // ImageIO can create an ImageInputStream automatically from a File references or a standard I/O InputStream
        // reference. If the data source is a URL, or a string file path, then we must open an input stream ourselves.

        if (input instanceof java.net.URL)
            input = ((java.net.URL) input).openStream();
        else if (input instanceof CharSequence)
            input = openInputStream(input.toString());

        return javax.imageio.ImageIO.createImageInputStream(input);
    }

    protected static java.io.InputStream openInputStream(String path) throws java.io.IOException
    {
        Object streamOrException = WWIO.getFileOrResourceAsStream(path, null);
        if (streamOrException == null)
        {
            return null;
        }
        else if (streamOrException instanceof java.io.IOException)
        {
            throw (java.io.IOException) streamOrException;
        }
        else if (streamOrException instanceof Exception)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadImageFile", path);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, streamOrException);
            throw new java.io.IOException(message);
        }

        return (java.io.InputStream) streamOrException;
    }

    protected static javax.imageio.ImageReader readerFor(javax.imageio.stream.ImageInputStream iis)
    {
        java.util.Iterator<javax.imageio.ImageReader> readers = javax.imageio.ImageIO.getImageReaders(iis);
        if (!readers.hasNext())
            return null;

        return readers.next();
    }

    protected static String[] getImageIOReaderSuffixes()
    {
        java.util.Iterator<javax.imageio.spi.ImageReaderSpi> iter;
        try
        {
            iter = javax.imageio.spi.IIORegistry.getDefaultInstance().getServiceProviders(
                javax.imageio.spi.ImageReaderSpi.class, true);
        }
        catch (Exception e)
        {
            return new String[0];
        }

        java.util.Set<String> set = new java.util.HashSet<String>();
        while (iter.hasNext())
        {
            javax.imageio.spi.ImageReaderSpi spi = iter.next();
            String[] names = spi.getFileSuffixes();
            set.addAll(java.util.Arrays.asList(names));
        }

        String[] array = new String[set.size()];
        set.toArray(array);
        return array;
    }
}
