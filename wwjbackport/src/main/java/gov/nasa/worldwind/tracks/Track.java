/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.tracks;

/**
 * @author tag
 * @version $Id: Track.java 2422 2007-07-25 23:07:49Z tgaskins $
 */
public interface Track
{
    java.util.List<TrackSegment> getSegments();

    String getName();

    int getNumPoints();
}
