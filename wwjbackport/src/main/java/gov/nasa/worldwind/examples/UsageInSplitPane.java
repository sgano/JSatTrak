/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.awt.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: UsageInSplitPane.java 11955 2009-06-27 21:10:38Z tgaskins $
 */
public class UsageInSplitPane
{
    public static class AppPanel extends JPanel
    {
        private WorldWindowGLCanvas wwd;

        public AppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            super(new BorderLayout());

            this.wwd = new WorldWindowGLCanvas();
            this.wwd.setPreferredSize(canvasSize);
            
            // THIS IS THE TRICK: Set the panel's minimum size to (0,0);
            this.setMinimumSize(new Dimension(0, 0));

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            // Setup a select listener for the worldmap click-and-go feature
            this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));

            this.add(this.wwd, BorderLayout.CENTER);
            if (includeStatusBar)
            {
                StatusBar statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                statusBar.setEventSource(wwd);
            }
        }
    }

    private static class AppFrame extends JFrame
    {
        private Dimension canvasSize = new Dimension(800, 600);

        public AppFrame()
        {
            // Create the WorldWindow.
            AppPanel wwjPanel = new AppPanel(this.canvasSize, false);
            LayerPanel layerPanel = new LayerPanel(wwjPanel.wwd);

            JSplitPane splitPane = new JSplitPane();
            splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setLeftComponent(layerPanel);
            splitPane.setRightComponent(wwjPanel);
            splitPane.setOneTouchExpandable(true);
            splitPane.setContinuousLayout(true); // prevents the pane's being obscured when expanding right

            this.getContentPane().add(splitPane, BorderLayout.CENTER);
            this.pack();

            // Center the application on the screen.
            Dimension prefSize = this.getPreferredSize();
            Dimension parentSize;
            java.awt.Point parentLocation = new java.awt.Point(0, 0);
            parentSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
            int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
            this.setLocation(x, y);
            this.setResizable(true);
        }
    }

    public static void main(String[] args)
    {
        start("World Wind Split Pane Usage", AppFrame.class);
    }

    public static void start(String appName, Class appFrameClass)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final AppFrame frame = new AppFrame();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
