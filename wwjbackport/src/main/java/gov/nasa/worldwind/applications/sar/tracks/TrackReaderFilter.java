/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.sar.tracks;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: TrackReaderFilter.java 11482 2009-06-07 20:03:47Z dcollins $
 */
public class TrackReaderFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter
{
    protected final TrackReader trackReader;

    public TrackReaderFilter(TrackReader trackReader)
    {
        this.trackReader = trackReader;
    }

    public final TrackReader getTrackReader()
    {
        return this.trackReader;
    }

    public String getDescription()
    {
        return this.trackReader.getDescription();
    }

    public boolean accept(java.io.File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return file.isDirectory() || this.trackReader.canRead(file);
    }
}
