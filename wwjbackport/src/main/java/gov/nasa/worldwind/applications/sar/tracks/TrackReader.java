/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.sar.tracks;

import gov.nasa.worldwind.tracks.Track;

/**
 * @author dcollins
 * @version $Id: TrackReader.java 11482 2009-06-07 20:03:47Z dcollins $
 */
public interface TrackReader
{
    String getDescription();

    boolean canRead(Object source);

    Track[] read(Object source);
}
