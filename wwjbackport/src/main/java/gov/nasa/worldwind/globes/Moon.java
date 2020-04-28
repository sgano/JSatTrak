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
 * @version $Id: Moon.java 12579 2009-09-12 01:07:05Z tgaskins $
 */

public class Moon extends EllipsoidalGlobe
{
    // From http://en.wikipedia.org/wiki/Moon
    public static final double EQUATORIAL_RADIUS = 1738140.0; // ellipsoid equatorial getRadius, in meters
    public static final double POLAR_RADIUS = 1735970.0; // ellipsoid polar getRadius, in meters
    public static final double ES = 0.00125; // eccentricity squared, semi-major axis

    public Moon()
    {
        super(EQUATORIAL_RADIUS, POLAR_RADIUS, ES,
            EllipsoidalGlobe.makeElevationModel(AVKey.MOON_ELEVATION_MODEL_CONFIG_FILE,
                "config/Moon/MoonElevationModel.xml"));
    }
}