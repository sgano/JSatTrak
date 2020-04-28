/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Angle;

/**
 * @author jparsons
 * @version $Id$
 */
public class StatusDescription
{
    //copied from StatusBar...todo refactor to remove duplicate code
    public final static String UNIT_METRIC = "gov.nasa.worldwind.StatusBar.Metric";
    public final static String UNIT_IMPERIAL = "gov.nasa.worldwind.StatusBar.Imperial";
    private final static double METER_TO_FEET = 3.280839895;
    private final static double METER_TO_MILE = 0.000621371192;
    
    public static String makeAngleDescription(String label, Angle angle)
    {
        return makeAngleDescription(label, angle, 4);
    }

    public static String makeAngleDescription(String label, Angle angle, int places)
    {
        String s;
        s = String.format("%s %7." + places+ "f\u00B0", label, angle.degrees);
        return s;
    }

    public static String makeEyeAltitudeDescription(double metersAltitude)
    {
        return makeEyeAltitudeDescription(metersAltitude, UNIT_METRIC);
    }

    public static String makeEyeAltitudeDescription(double metersAltitude, String elevationUnit)
    {
        String s;
        String altitude = Logging.getMessage("term.Altitude");
        if (UNIT_IMPERIAL.equals(elevationUnit))
            s = String.format(altitude + " %,d mi", (int) Math.round(metersAltitude * METER_TO_MILE));
        else // Default to metric units.
            s = String.format(altitude + " %,d km", (int) Math.round(metersAltitude / 1e3));
        return s;
    }

    public static String makeCursorElevationDescription(double metersElevation)
    {
        return makeCursorElevationDescription(metersElevation, UNIT_METRIC);
    }

    public static String makeCursorElevationDescription(double metersElevation, String elevationUnit)
    {
        String s;
        String elev = Logging.getMessage("term.Elev");
        if (UNIT_IMPERIAL.equals(elevationUnit))
            s = String.format(elev + " %,d feet", (int) (metersElevation * METER_TO_FEET));
        else // Default to metric units.
            s = String.format(elev + " %,d meters", (int) metersElevation);
        return s;
    }


}
