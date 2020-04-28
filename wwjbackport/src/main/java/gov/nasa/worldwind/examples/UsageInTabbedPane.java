/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: UsageInTabbedPane.java 4331 2008-02-01 03:38:37Z tgaskins $
 */
public class UsageInTabbedPane
{
    static
    {
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Tabbed Pane Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    public static class WWJPanel extends JPanel
    {
        protected WorldWindowGLCanvas wwd;
        protected StatusBar statusBar;

        public WWJPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            super(new BorderLayout());

            this.wwd = new WorldWindowGLCanvas();
            this.wwd.setPreferredSize(canvasSize);

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            this.add(this.wwd, BorderLayout.CENTER);
            if (includeStatusBar)
            {
                this.statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                this.statusBar.setEventSource(wwd);
            }
        }
    }

    public static void main(String[] args)
    {
        try
        {
            JFrame mainFrame = new JFrame();

            mainFrame.setTitle("World Wind Tabbed Pane");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final JTabbedPane tabbedPane = new JTabbedPane();
            final WWJPanel wwjPanel = new WWJPanel(new Dimension(800, 600), true);
            final JPanel controlPanel = new JPanel(new BorderLayout());

            JButton detachButton = new JButton("Detach");
            detachButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    System.out.println("Detaching wwj");
//                    wwjPanel.wwd.detachFromParent();
                    System.out.println("Removing tab");
                    tabbedPane.removeTabAt(0);
                    System.out.println("Tab removed");
                }
            });

            JButton attachButton = new JButton("Attach");
            attachButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    System.out.println("Adding tab");
                    tabbedPane.insertTab("WWJ Pane 1", null, wwjPanel, "Reattach", 0);
                    System.out.println("Tab added");
                }
            });

            controlPanel.add(detachButton, BorderLayout.NORTH);
            controlPanel.add(attachButton, BorderLayout.SOUTH);

            tabbedPane.add("WWJ Pane 1", wwjPanel);
            tabbedPane.add("Dummy Pane", controlPanel);

            mainFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
            mainFrame.pack();
            mainFrame.setVisible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
