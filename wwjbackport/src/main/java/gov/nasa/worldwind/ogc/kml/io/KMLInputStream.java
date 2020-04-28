/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml.io;

import gov.nasa.worldwind.util.Logging;

import java.io.*;

/**
 * Implements the {@link KMLDoc} interface for KML files read directly from input streams.
 *
 * @author tag
 * @version $Id: KMLInputStream.java 13466 2010-06-17 20:44:29Z tgaskins $
 */
public class KMLInputStream implements KMLDoc
{
    /** The {@link InputStream} specified to the constructor. */
    protected InputStream inputStream;

    /**
     * Construct a <code>KMLInputStream</code> instance.
     *
     * @param sourceStream the KML stream.
     *
     * @throws IllegalArgumentException if the specified input stream is null.
     * @throws IOException              if an error occurs while attempting to read from the stream.
     */
    public KMLInputStream(InputStream sourceStream) throws IOException
    {
        if (sourceStream == null)
        {
            String message = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.inputStream = sourceStream;
    }

    /**
     * Returns the input stream refrence passed to the constructor.
     *
     * @return the input stream refrence passed to the constructor.
     */
    public InputStream getKMLStream() throws IOException
    {
        return this.inputStream;
    }

    /**
     * Since the input stream has no location in the file system, relative paths to support files are not meaningful.
     * Therefore this class always returns null from this method.
     *
     * @param path the path of the requested file.
     *
     * @return null (always)
     */
    public InputStream getSupportFileStream(String path) throws IOException
    {
        return null; // there are no relative-path source files in this case
    }

    /**
     * Since the input stream has no location in the file system, relative paths to support files are not meaningful.
     * Therefore this class always returns null from this method.
     *
     * @param path the path of the requested file.
     *
     * @return null (always)
     */
    public String getSupportFilePath(String path)
    {
        return null; // there are no relative-path source files in this case
    }
}
