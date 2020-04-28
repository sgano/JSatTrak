/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.tracks;

/**
 * @author tag
 * @version $Id: TrackSegment.java 2422 2007-07-25 23:07:49Z tgaskins $
 */
public interface TrackSegment
{
    java.util.List<TrackPoint> getPoints();
}
