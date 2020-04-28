/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.avlist.AVKey;

/**
 * @author Patrick Murris
 * @version $Id: Mars.java 12579 2009-09-12 01:07:05Z tgaskins $
 */

public class Mars extends EllipsoidalGlobe
{
    // From http://en.wikipedia.org/wiki/Mars
    public static final double EQUATORIAL_RADIUS = 3396200.0; // ellipsoid equatorial getRadius, in meters
    public static final double POLAR_RADIUS = 3376200.0; // ellipsoid polar getRadius, in meters
    public static final double ES = 0.00589; // eccentricity squared, semi-major axis

    public Mars()
    {
        super(EQUATORIAL_RADIUS, POLAR_RADIUS, ES,
            EllipsoidalGlobe.makeElevationModel(AVKey.MARS_ELEVATION_MODEL_CONFIG_FILE,
                "config/Mars/MarsElevationModel.xml"));
    }
}