/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.Configuration;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: GeodataApp.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class GeodataApp
{
    static
    {
        Configuration.insertConfigurationDocument("gov/nasa/worldwind/applications/gos/gosapp.config.xml");
        
        if (Configuration.isMacOS())
        {
            String appName = Configuration.getStringValue(GeodataKey.DISPLAY_NAME_LONG);
            if (appName != null)
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);

            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    public static void main(String[] args)
    {
        try
        {
            final GeodataAppFrame frame = new GeodataAppFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                    frame.getGeodataPortalFrame().setVisible(true);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
