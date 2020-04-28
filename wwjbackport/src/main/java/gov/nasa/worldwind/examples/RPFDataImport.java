/*
Copyright (C) 2001, 2007 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.rpf.RPFTiledImageLayer;
import gov.nasa.worldwind.layers.rpf.wizard.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.wizard.Wizard;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * Application demonstrating how to import and view local imagery in WWJ.
 * Currently only RPF (CADRG and CIB) data is supported.
 * 
 * <h5>How to view RPF imagery</h5>
 * <ol>
 *   <li>Click "Import CADRG/CIB".</li>
 *   <li>A dialog will appear.</li>
 *   <li>Select a folder to search for data. This should be a folder you know contains RPF data.</li>
 *   <li>Click "Next".</li>
 *   <li>Select the data series you want to import.
 *       Note: A new RPF layer will be created for each data series selected.</li>
 *   <li>Click "Next".</li>
 *   <li>Wait for preprocessing to complete for each data series you selected.</li>
 *   <li>Click "Finish".</li>
 * </ol>
 *
 * <h5>Key RPF features</h5>
 * <ul>
 *   <li>Wizard UI walks user through process of selecting, preprocessing, and importing RPF imagery.</li>
 *   <li>Preprocessing enables the layer to create images spanning thousands of RPF files very quickly.</li>
 *   <li>Impact of preprocessed data on users hard drive is constant - all selected files are preprocessed.
 *       These files are roughly equivalent in size to the original data.</li>
 *   <li>Imagery is created only when user views it, and then it is stored in the WWJ file cache.</li>
 *   <li>Impact of actual imagery on users hard drive is commensurate to areas the user has viewed.</li>
 * </ul>
 *
 * @author dcollins
 * @version $Id: RPFDataImport.java 13225 2010-03-18 21:18:41Z dcollins $
 */
public class RPFDataImport extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        @SuppressWarnings({"FieldCanBeLocal"})
        private RPFPanel rpfPanel;

        public AppFrame()
        {
            super(true, false, false);
            initComponents();
        }

        protected void initComponents()
        {
            this.rpfPanel = new RPFPanel(getWwd());
            getContentPane().add(this.rpfPanel, BorderLayout.WEST);
        }
    }

    protected static class RPFPanel extends JPanel
    {
        private LayerPanel layerPanel;
        private WorldWindow wwd;
        private JFileChooser loadSaveChooser;

        public RPFPanel(WorldWindow wwd)
        {
            this.wwd = wwd;
            initComponents();
        }

        protected void onImportRPFPressed()
        {
            runRPFImportWizard(this.wwd);
            this.layerPanel.update(this.wwd);

        }

        protected void onSaveLayersPressed()
        {
            File dir = this.showLoadSaveDialog(this, "Save");
            if (dir == null)
                return;

            int count = 0;

            LayerList layers = this.wwd.getModel().getLayers();
            for (Layer layer : layers)
            {
                if (layer instanceof RPFTiledImageLayer)
                {
                    String stateXml = layer.getRestorableState();
                    if (stateXml != null)
                    {
                        StringBuilder filename = new StringBuilder();
                        filename.append(layer.getClass().getName()).append("-").append(++count).append(".xml");

                        WWIO.writeTextFile(stateXml, new File(dir, filename.toString()));
                    }
                }
            }
        }

        protected void onOpenLayersPressed()
        {
            File dir = this.showLoadSaveDialog(this, "Open");
            if (dir == null)
                return;
            
            String[] names = dir.list();
            for (String fileName : names)
            {
                if (fileName.endsWith(".xml"))
                {
                    String[] tokens = fileName.split("-");
                    if (tokens.length >= 2)
                    {
                        try
                        {
                            String stateInXml = WWIO.readTextFile(new File(dir, fileName));
                            Layer layer = new RPFTiledImageLayer(stateInXml);
                            ApplicationTemplate.insertBeforePlacenames(wwd, layer);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        protected File showLoadSaveDialog(Component parent, String approveButtonText)
        {
            if (this.loadSaveChooser == null)
            {
                this.loadSaveChooser = new JFileChooser(Configuration.getUserHomeDirectory());
                this.loadSaveChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                this.loadSaveChooser.setMultiSelectionEnabled(false);
            }

            int returnVal = this.loadSaveChooser.showDialog(parent, approveButtonText);
            if (returnVal != JFileChooser.APPROVE_OPTION)
                return null;

            return this.loadSaveChooser.getSelectedFile();
        }

        protected void initComponents()
        {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 0, 10, 0));

            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new GridLayout(0, 1, 0, 5));
            btnPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
            JButton importBtn = new JButton("Import RPF Files...");
            importBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onImportRPFPressed();
                }
            });
            btnPanel.add(importBtn);
            JButton saveLayersBtn = new JButton("Save RPF Layers...");
            saveLayersBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSaveLayersPressed();
                }
            });
            btnPanel.add(saveLayersBtn);
            JButton openLayersBtn = new JButton("Open RPF Layers...");
            openLayersBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onOpenLayersPressed();
                }
            });
            btnPanel.add(openLayersBtn);
            add(btnPanel, BorderLayout.SOUTH);

            this.layerPanel = new LayerPanel(this.wwd, null);
            add(this.layerPanel, BorderLayout.CENTER);
        }
    }

    public static void runRPFImportWizard(WorldWindow wwd)
    {
        RPFImportWizard wizard = new RPFImportWizard();
        wizard.setTitle("Import RPF Files");
        wizard.getDialog().setPreferredSize(new Dimension(500, 400));
        WWUtil.alignComponent(null, wizard.getDialog(), AVKey.CENTER);

        int returnCode = wizard.showModalDialog();
        if (returnCode == Wizard.FINISH_RETURN_CODE)
        {
            List<Layer> layerList = RPFWizardUtil.getLayerList(wizard.getModel());
            if (layerList != null)
            {
                for (Layer layer : layerList)
                {
                    ApplicationTemplate.insertBeforePlacenames(wwd, layer);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind RPF Import", AppFrame.class);
    }
}
