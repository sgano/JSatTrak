/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.view;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.view.firstperson.*;

/**
 * @author jym
 * @version $Id$
 */
public class FlyViewControls extends ApplicationTemplate
{

    public static void main(String[] args)
    {
        // Call the static start method like this from the main method of your derived class.
        // Substitute your application's name for the first argument.
        AppFrame appFrame = ApplicationTemplate.start("World Wind Fly View Controls", AppFrame.class);

        WorldWindow wwd = appFrame.getWwd();

        // Force the view to be a FlyView
        BasicFlyView flyView = new BasicFlyView();
        wwd.setView(flyView);

        // Get access to the FlyView's limits.
        FlyViewLimits flyViewLimits = (FlyViewLimits) flyView.getViewPropertyLimits();
        // Set the elevation limits to between 1000 and 5000 meters.
        flyViewLimits.setEyeElevationLimits(1000, 5000);

        // Set the pitch limts to 45, 135.  90 is straight ahead, 0 is straight down.
        flyViewLimits.setPitchLimits(Angle.fromDegrees(45), Angle.fromDegrees(135));
    }
}
