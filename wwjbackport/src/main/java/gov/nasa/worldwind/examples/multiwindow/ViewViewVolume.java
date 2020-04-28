/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples.multiwindow;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: ViewViewVolume.java 12997 2010-01-09 09:56:35Z tgaskins $
 */
public class ViewViewVolume extends JFrame
{
    static
    {
        if (gov.nasa.worldwind.Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Multi-Window Analysis");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    protected WWPanel wwp;

    public ViewViewVolume()
    {
        this.getContentPane().setLayout(new BorderLayout(5, 5));

        this.wwp = new WWPanel(new Dimension(650, 500));
        this.getContentPane().add(wwp);

        this.pack();
        this.setResizable(true);

        WWUtil.alignComponent(null, this, AVKey.CENTER);

        this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }

    protected static class WWPanel extends JPanel
    {
        WorldWindowGLCanvas wwd;

        public WWPanel(Dimension size)
        {
            this.wwd = new WorldWindowGLCanvas();
            this.wwd.setSize(size);

            this.wwd.setModel((Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME));

            this.setLayout(new BorderLayout(5, 5));
            this.add(this.wwd, BorderLayout.CENTER);

            StatusBar statusBar = new StatusBar();
            statusBar.setEventSource(wwd);
            this.add(statusBar, BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                // Make a World Window to observe
                ViewViewVolume vvv = new ViewViewVolume();
                vvv.setVisible(true);

                // Make the observer
                ViewVolumeViewer vvViewer = new ViewVolumeViewer(vvv.wwp.wwd, new Dimension(500, 500));
                Point p = vvv.getLocation();
                vvViewer.setLocation(p.x + vvv.getWidth() + 20, p.y);
                vvViewer.setVisible(true);
            }
        });
    }
}
