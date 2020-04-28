/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.examples.LayerPanel;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author dcollins
 * @version $Id: GeodataAppFrame.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class GeodataAppFrame extends JFrame
{
    protected WorldWindowGLCanvas wwd;
    protected LayerPanel layerPanel;
    protected StatusBar statusBar;
    protected GeodataPortalFrame portalFrame;

    public GeodataAppFrame() throws HeadlessException
    {
        super(Configuration.getStringValue(GeodataKey.DISPLAY_NAME_LONG));

        // Create the default model as described in the current worldwind properties.
        Model model = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);

        this.wwd = this.createWorldWindow();
        this.wwd.setModel(model);
        this.wwd.addRenderingExceptionListener(new RenderingExceptionListener()
        {
            public void exceptionThrown(Throwable t)
            {
                if (t instanceof WWAbsentRequirementException)
                {
                    String message = "Computer does not meet minimum graphics requirements.\n";
                    message += "Please install up-to-date graphics driver and try again.\n";
                    message += "Reason: " + t.getMessage() + "\n";
                    message += "This program will end when you press OK.";

                    JOptionPane.showMessageDialog(GeodataAppFrame.this, message, "Unable to Start Program",
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
            }
        });

        this.layerPanel = new LayerPanel(this.wwd);
        this.statusBar = new StatusBar();
        this.statusBar.setEventSource(this.wwd);

        this.portalFrame = this.createGeodataPortalFrame();
        this.portalFrame.setWorldWindow(this.wwd);

        this.layoutComponents();
    }

    public GeodataPortalFrame getGeodataPortalFrame()
    {
        return this.portalFrame;
    }

    protected WorldWindowGLCanvas createWorldWindow()
    {
        return new WorldWindowGLCanvas();
    }

    protected GeodataPortalFrame createGeodataPortalFrame()
    {
        return new GeodataPortalFrame();
    }

    protected void layoutComponents()
    {
        this.getContentPane().setLayout(new BorderLayout(0, 0)); // hgap, vgap

        JPanel panel = new JPanel(new BorderLayout(0, 0)); // hgap, vgap
        {
            Box box = Box.createHorizontalBox();
            box.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20)); // top, left, bottom, right
            box.add(new JButton(
                new AbstractAction("Search " + Configuration.getStringValue(GeodataKey.DISPLAY_NAME_SHORT))
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        getGeodataPortalFrame().setVisible(true);
                    }
                }));

            panel.add(this.layerPanel, BorderLayout.CENTER);
            panel.add(box, BorderLayout.SOUTH);
        }
        this.getContentPane().add(panel, BorderLayout.WEST);

        panel = new JPanel(new BorderLayout(0, 0));
        {
            this.wwd.setPreferredSize(new Dimension(800, 600));
            panel.add(this.wwd, BorderLayout.CENTER);
            panel.add(this.statusBar, BorderLayout.SOUTH);
        }
        this.getContentPane().add(panel, BorderLayout.CENTER);

        this.pack();
        this.setResizable(true);
        // Center the application on the screen.
        WWUtil.alignComponent(null, this, AVKey.CENTER);
        WWUtil.alignComponent(this, this.getGeodataPortalFrame(), AVKey.LEFT);
    }
}
