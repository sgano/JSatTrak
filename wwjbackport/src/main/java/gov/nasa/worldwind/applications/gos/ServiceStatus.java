/* Copyright (C) 2001, 2009 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.applications.gos;

/**
 * @author dcollins
 * @version $Id: ServiceStatus.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public interface ServiceStatus
{
    final double SCORE_UNKNOWN = -99.99;
    final double SPEED_UNKNOWN = 0d;

    double getScore();

    double getSpeed();

    OnlineResource getScoreImageResource();
}
