/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.examples.util.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * @author Patrick Murris
 * @version $Id: Shapefiles.java 13665 2010-08-27 19:36:33Z dcollins $
 */
public class Shapefiles extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        JFileChooser fc = new JFileChooser(Configuration.getUserHomeDirectory());

        protected List<Layer> layers = new ArrayList<Layer>();
        protected BasicDragger dragger;
        private JCheckBox pickCheck, dragCheck;

        public AppFrame()
        {
            // Add our control panel
            this.getLayerPanel().add(makeControlPanel(), BorderLayout.SOUTH);

            // Setup file chooser
            this.fc = new JFileChooser(Configuration.getUserHomeDirectory());
            this.fc.addChoosableFileFilter(new SHPFileFilter());

            // Create a select listener for shape dragging but do not add it yet. Dragging can be enabled via the user
            // interface.
            this.dragger = new BasicDragger(this.getWwd());
        }

        protected JPanel makeControlPanel()
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 9, 9, 9),
                new TitledBorder("Shapefiles")));

            // Open shapefile buttons.
            JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 5)); // nrows, ncols, hgap, vgap
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right
            panel.add(buttonPanel);
            // Open shapefile from File button.
            JButton openFileButton = new JButton("Open File...");
            openFileButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    showOpenFileDialog();
                }
            });
            buttonPanel.add(openFileButton);
            // Open shapefile from URL button.
            JButton openURLButton = new JButton("Open URL...");
            openURLButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    showOpenURLDialog();
                }
            });
            buttonPanel.add(openURLButton);

            // Picking and dragging checkboxes
            JPanel pickPanel = new JPanel(new GridLayout(1, 1, 10, 10)); // nrows, ncols, hgap, vgap
            pickPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right
            this.pickCheck = new JCheckBox("Allow picking");
            this.pickCheck.setSelected(true);
            this.pickCheck.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    enablePicking(((JCheckBox) actionEvent.getSource()).isSelected());
                }
            });
            pickPanel.add(this.pickCheck);

            this.dragCheck = new JCheckBox("Allow dragging");
            this.dragCheck.setSelected(false);
            this.dragCheck.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    enableDragging(((JCheckBox) actionEvent.getSource()).isSelected());
                }
            });
            pickPanel.add(this.dragCheck);

            panel.add(pickPanel);

            return panel;
        }

        protected void enablePicking(boolean enabled)
        {
            for (Layer layer : this.layers)
            {
                layer.setPickEnabled(enabled);
            }

            // Disable the drag check box. Dragging is implicitly disabled since the objects cannot be picked.
            this.dragCheck.setEnabled(enabled);
        }

        protected void enableDragging(boolean enabled)
        {
            if (enabled)
                this.getWwd().addSelectListener(this.dragger);
            else
                this.getWwd().removeSelectListener(this.dragger);
        }

        public void showOpenFileDialog()
        {
            int retVal = AppFrame.this.fc.showOpenDialog(this);
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;

            Thread t = new WorkerThread(this.fc.getSelectedFile(), this);
            t.start();
        }

        public void showOpenURLDialog()
        {
            String retVal = JOptionPane.showInputDialog(this, "Enter Shapefile URL", "Open",
                JOptionPane.INFORMATION_MESSAGE);
            if (WWUtil.isEmpty(retVal)) // User cancelled the operation entered an empty URL.
                return;

            URL url = WWIO.makeURL(retVal);
            if (url == null)
            {
                JOptionPane.showMessageDialog(this, retVal + " is not a valid URL.", "Open", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Thread t = new WorkerThread(url, this);
            t.start();
        }
    }

    public static class WorkerThread extends Thread
    {
        private Object source;
        private AppFrame appFrame;

        public WorkerThread(Object source, AppFrame appFrame)
        {
            this.source = source;
            this.appFrame = appFrame;
        }

        public void run()
        {
            try
            {
                final Layer layer = this.makeShapefileLayer(this.source);
                layer.setName(this.makeDisplayName(this.source));
                layer.setPickEnabled(this.appFrame.pickCheck.isSelected());

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        insertBeforePlacenames(appFrame.getWwd(), layer);
                        appFrame.getLayerPanel().update(appFrame.getWwd());
                        appFrame.layers.add(layer);
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        protected Layer makeShapefileLayer(Object source)
        {
            if (OpenStreetMapShapefileLoader.isOSMPlacesSource(source))
            {
                return OpenStreetMapShapefileLoader.makeLayerFromOSMPlacesSource(source);
            }
            else
            {
                ShapefileLoader loader = new ShapefileLoader();
                return loader.createLayerFromSource(source);
            }
        }

        protected String makeDisplayName(Object source)
        {
            String name = WWIO.getSourcePath(source);
            if (name != null)
                name = WWIO.getFilename(name);
            if (name == null)
                name = "Shapefile";

            return name;
        }
    }

    public static class SHPFileFilter extends FileFilter
    {
        public boolean accept(File file)
        {
            if (file == null)
            {
                String message = Logging.getMessage("nullValue.FileIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            return file.isDirectory() || file.getName().toLowerCase().endsWith(".shp");
        }

        public String getDescription()
        {
            return "ESRI Shapefiles (shp)";
        }
    }

    public static void main(String[] args)
    {
        start("World Wind Shapefiles", AppFrame.class);
    }
}
