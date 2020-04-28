/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwindow.core.*;
import gov.nasa.worldwindow.util.Util;

import java.awt.*;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: WorldWindow.java 13266 2010-04-10 02:47:09Z tgaskins $
 */
public class WorldWindow
{
    static
    {
        System.setProperty("gov.nasa.worldwind.app.config.document",
            "gov/nasa/worldwindow/config/worldwindow.worldwind.xml");
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            String s = Configuration.getStringValue(Constants.APPLICATION_DISPLAY_NAME);
            System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                Configuration.getStringValue(Constants.APPLICATION_DISPLAY_NAME));//"World Window");
        }
        else if (Configuration.isWindowsOS())
        {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    private static final String APP_CONFIGURATION = "gov/nasa/worldwindow/config/AppConfiguration.xml";

    public static void main(String[] args)
    {
        Controller controller = new Controller();

        Dimension appSize = null;
        if (args.length >= 2) // The first two arguments are the application width and height.
            appSize = new Dimension(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        try
        {
            controller.start(APP_CONFIGURATION, appSize);
        }
        catch (Exception e)
        {
            String msg = "Fatal application error";
            controller.showErrorDialog(null, "Cannot Start Application", msg);
            Util.getLogger().log(Level.SEVERE, msg);
        }
    }
}
