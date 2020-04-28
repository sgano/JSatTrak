/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.util;

import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.util.Logging;

import java.io.File;
import java.util.Arrays;

/**
 * @author dcollins
 * @version $Id: ImageIOFileFilter.java 9168 2009-03-05 00:40:03Z dcollins $
 */
public class ImageIOFileFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter
{
    private ImageIOReader reader;
    private String description;

    public ImageIOFileFilter(ImageIOReader reader)
    {
        if (reader == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.reader = reader;
        this.description = makeDescription(reader);
    }

    public boolean accept(File file)
    {
        if (file == null)
            return false;

        //noinspection SimplifiableIfStatement
        if (file.isDirectory())
            return true;

        return this.reader.canRead(file);
    }

    public String getDescription()
    {
        return this.description;
    }

    protected static String makeDescription(ImageIOReader reader)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Raster Imagery (");
        sb.append(BasicDataIODescriptor.createCombinedDescription(Arrays.asList(reader)));
        sb.append(")");

        return sb.toString();
    }
}
