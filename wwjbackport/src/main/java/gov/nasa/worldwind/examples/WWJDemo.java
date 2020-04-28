/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.examples.util.ShapefileLoader;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.layers.Mercator.examples.OSMMapnikLayer;
import gov.nasa.worldwind.util.measure.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Hashtable;

/**
 * @author Patrick Murris
 * @version $Id: WWJDemo.java 13613 2010-08-10 17:43:04Z dcollins $
 */
public class WWJDemo extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private final JTabbedPane tabbedPane = new JTabbedPane();
        private JFileChooser fc = new JFileChooser(Configuration.getUserHomeDirectory());

        public AppFrame()
        {
            // Add some layers
            this.setupLayers();

            // Add vertical exaggeration slider panel
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
            controlPanel.add(makeVEControlPanel());

            // Add shapefile loading panel
            controlPanel.add(makeShapefileControlPanel());
            this.getLayerPanel().add(controlPanel, BorderLayout.SOUTH);

            // Setup shapefile file chooser
            this.fc = new JFileChooser(Configuration.getUserHomeDirectory());
            this.fc.addChoosableFileFilter(new Shapefiles.SHPFileFilter());

            // Add WMS layer manager frame
            this.setupWMSLayerManager();

            // Setup tabbed pane
            LayerPanel layerPanel = getLayerPanel();
            this.getContentPane().remove(layerPanel);
            // Add layer panel
            this.tabbedPane.add("Layers", layerPanel);
            // Add measure tool control panel
            MeasureTool measureTool = new MeasureTool(this.getWwd());
            measureTool.setController(new MeasureToolController());
            tabbedPane.add("Measure", new MeasureToolPanel(this.getWwd(), measureTool));

            this.getContentPane().add(this.tabbedPane, BorderLayout.WEST);
            this.pack();
        }

        // *** Layers ***
        // *** Layers ***

        private void setupLayers()
        {
            // Add some imagery layers - layer class, enabled, opacity
            this.setupLayer(USGSDigitalOrtho.class, false, .6);
            this.setupLayer(USGSUrbanAreaOrtho.class, false, 1);
            this.setupLayer(USGSTopographicMaps.class, false, .7);
            this.setupLayer(OpenStreetMapLayer.class, false, 1);
            this.setupLayer(OSMMapnikLayer.class, false, 1);
            this.setupLayer(LatLonGraticuleLayer.class, false, .7);
            this.setupLayer(UTMGraticuleLayer.class, false, .7);
            this.setupLayer(MGRSGraticuleLayer.class, false, .7);

            // Add terrain profile layer
            Layer layer = this.setupLayer(TerrainProfileLayer.class, false, 1);
            if (layer != null)
            {
                TerrainProfileLayer tpl = (TerrainProfileLayer) layer;
                tpl.setEventSource(this.getWwd());
                tpl.setZeroBased(false);
            }

            // Add view controls
            layer = this.setupLayer(ViewControlsLayer.class, true, 1);
            if (layer != null)
            {
                ViewControlsLayer vcl = (ViewControlsLayer) layer;
                this.getWwd().addSelectListener(new ViewControlsSelectListener(this.getWwd(), vcl));
                vcl.setPosition(AVKey.NORTHEAST);
                vcl.setLocationOffset(new Vec4(0, -70, 0));
                vcl.setLayout(AVKey.VERTICAL);
                vcl.setShowVeControls(false);
            }

            // Make compass smaller
            layer = this.findLayer(CompassLayer.class);
            if (layer != null)
                ((CompassLayer) layer).setIconScale(.25);

            // Add some shapefiles - will delay application startup
            //addShapefileLayer("http://worldwind28.arc.nasa.gov/haiti/VOLUNTEER/minustah_shp/hti_polbnda_adm2_minustah.shp", "Haiti adm2");

            // Update layer panel    
            this.getLayerPanel().update(this.getWwd());
        }

        private Layer setupLayer(Class layerClass, boolean enabled, double opacity)
        {
            Layer layer = this.findLayer(layerClass);
            if (layer == null)
            {
                // Add layer to the layer list if not already in
                try
                {
                    layer = (Layer) layerClass.newInstance();
                    insertBeforePlacenames(this.getWwd(), layer);
                }
                catch (Exception e)
                {
                    return null;
                }
            }
            layer.setEnabled(enabled);
            layer.setOpacity(opacity);
            return layer;
        }

        private Layer findLayer(Class layerClass)
        {
            for (Layer layer : this.getWwd().getModel().getLayers())
            {
                if (layer.getClass().equals(layerClass))
                    return layer;
            }

            return null;
        }

        // *** Vertical exaggeration slider ***

        private JPanel makeVEControlPanel()
        {
            JPanel controlPanel = new JPanel(new BorderLayout(0, 10));
            controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Vertical Exaggeration")));

            JPanel vePanel = new JPanel(new BorderLayout(0, 5));
            {
                int MIN_VE = 1;
                int MAX_VE = 8;
                int curVe = (int) this.getWwd().getSceneController().getVerticalExaggeration();
                curVe = curVe < MIN_VE ? MIN_VE : (curVe > MAX_VE ? MAX_VE : curVe);
                JSlider slider = new JSlider(MIN_VE, MAX_VE, curVe);
                slider.setMajorTickSpacing(1);
                slider.setPaintTicks(true);
                slider.setSnapToTicks(true);
                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
                labelTable.put(1, new JLabel("1x"));
                labelTable.put(2, new JLabel("2x"));
                labelTable.put(4, new JLabel("4x"));
                labelTable.put(8, new JLabel("8x"));
                slider.setLabelTable(labelTable);
                slider.setPaintLabels(true);
                slider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        double ve = ((JSlider) e.getSource()).getValue();
                        getWwd().getSceneController().setVerticalExaggeration(ve);
                    }
                });
                vePanel.add(slider, BorderLayout.SOUTH);
            }
            controlPanel.add(vePanel, BorderLayout.SOUTH);
            return controlPanel;
        }

        // *** WMS Layer manager ***
        // *** WMS Layer manager ***

        private static final String[] servers = new String[]
            {
                "http://hypercube.telascience.org/cgi-bin/mapserv?map=/geo/haiti/mapfiles/4326.map&"
//            "http://neowms.sci.gsfc.nasa.gov/wms/wms",
//            "http://mapserver.flightgear.org/cgi-bin/landcover",
//            "http://wms.jpl.nasa.gov/wms.cgi",
//            "http://worldwind46.arc.nasa.gov:8087/wms"
            };

        private final Dimension wmsPanelSize = new Dimension(400, 600);
        private JTabbedPane wmsTabbedPane;
        private int previousTabIndex;

        public void setupWMSLayerManager()
        {
            this.wmsTabbedPane = new JTabbedPane();

            this.wmsTabbedPane.add(new JPanel());
            this.wmsTabbedPane.setTitleAt(0, "+");
            this.wmsTabbedPane.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent changeEvent)
                {
                    if (wmsTabbedPane.getSelectedIndex() != 0)
                    {
                        previousTabIndex = wmsTabbedPane.getSelectedIndex();
                        return;
                    }

                    String server = JOptionPane.showInputDialog("Enter wms server URL");
                    if (server == null || server.length() < 1)
                    {
                        wmsTabbedPane.setSelectedIndex(previousTabIndex);
                        return;
                    }

                    // Respond by adding a new WMSLayerPanel to the tabbed pane.
                    if (addWmsServerTab(wmsTabbedPane.getTabCount(), server.trim()) != null)
                        wmsTabbedPane.setSelectedIndex(wmsTabbedPane.getTabCount() - 1);
                }
            });

            // Create a tab for each server and add it to the tabbed panel.
            for (int i = 0; i < servers.length; i++)
            {
                this.addWmsServerTab(i + 1,
                    servers[i]); // i+1 to place all server tabs to the right of the Add Server tab
            }

            // Display the first server pane by default.
            this.wmsTabbedPane.setSelectedIndex(this.wmsTabbedPane.getTabCount() > 0 ? 1 : 0);
            this.previousTabIndex = this.wmsTabbedPane.getSelectedIndex();

            // Add the tabbed pane to a frame separate from the world window.
            JFrame controlFrame = new JFrame();
            controlFrame.setTitle("WMS Layers");
            controlFrame.getContentPane().add(wmsTabbedPane);
            controlFrame.pack();
            controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            controlFrame.setVisible(true);
        }

        private WMSLayersPanel addWmsServerTab(int position, String server)
        {
            // Add a server to the tabbed dialog.
            try
            {
                WMSLayersPanel layersPanel = new WMSLayersPanel(AppFrame.this.getWwd(), server, wmsPanelSize);
                this.wmsTabbedPane.add(layersPanel, BorderLayout.CENTER);
                String title = layersPanel.getServerDisplayString();
                this.wmsTabbedPane.setTitleAt(position, title != null && title.length() > 0 ? title : server);

                // Add a listener to notice wms layer selections and tell the layer panel to reflect the new state.
                layersPanel.addPropertyChangeListener("LayersPanelUpdated", new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
                    {
                        AppFrame.this.getLayerPanel().update(AppFrame.this.getWwd());
                    }
                });

                return layersPanel;
            }
            catch (URISyntaxException e)
            {
                JOptionPane.showMessageDialog(null, "Server URL is invalid", "Invalid Server URL",
                    JOptionPane.ERROR_MESSAGE);
                wmsTabbedPane.setSelectedIndex(previousTabIndex);
                return null;
            }
        }

        // *** Shapefiles ***
        // *** Shapefiles ***

        protected JPanel makeShapefileControlPanel()
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
                new TitledBorder("Shapefiles")));

            // Open shapefile button
            JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 0, 0)); // nrows, ncols, hgap, vgap
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right
            JButton button = new JButton("Open Shapefile");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    showShapefileOpenDialog();
                }
            });
            buttonPanel.add(button);
            panel.add(buttonPanel);

            return panel;
        }

        public void showShapefileOpenDialog()
        {
            int retVal = this.fc.showOpenDialog(this);
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;

            File file = this.fc.getSelectedFile();
            this.addShapefileLayer(file);
        }

        public void addShapefileLayer(File file)
        {
            try
            {
                ShapefileLoader loader = new ShapefileLoader();
                Layer layer = loader.createLayerFromSource(file);
                if (layer != null)
                {
                    layer.setPickEnabled(false);
                    layer.setName(file.getName());
                    insertBeforePlacenames(this.getWwd(), layer);
                    this.getLayerPanel().update(this.getWwd());
                }
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null, "Error while loading shapefile from file " + file.getPath(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public void addShapefileLayer(String urlString, String displayName)
        {
            try
            {
                ShapefileLoader loader = new ShapefileLoader();
                Layer layer = loader.createLayerFromSource(urlString);
                if (layer != null)
                {
                    layer.setPickEnabled(false);
                    layer.setName(displayName != null ? displayName : urlString);
                    insertBeforePlacenames(this.getWwd(), layer);
                    this.getLayerPanel().update(this.getWwd());
                }
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null, "Error while loading shapefile from url " + urlString, "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("NASA World Wind", AppFrame.class);
    }
}
