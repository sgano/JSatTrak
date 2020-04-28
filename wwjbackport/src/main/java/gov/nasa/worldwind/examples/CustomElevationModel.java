/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

/**
 * @author tag
 * @version $Id: CustomElevationModel.java 10307 2009-04-17 22:07:11Z tgaskins $
 */
public class CustomElevationModel extends ApplicationTemplate
{
    public static void main(String[] args)
    {
        // Specify the configuration file for the elevation model prior to starting World Wind:
        Configuration.setValue(AVKey.EARTH_ELEVATION_MODEL_CONFIG_FILE,
            "gov/nasa/worldwind/examples/CustomElevationModel.xml");

        ApplicationTemplate.start("World Wind Custom Elevation Model", AppFrame.class);
    }
}
