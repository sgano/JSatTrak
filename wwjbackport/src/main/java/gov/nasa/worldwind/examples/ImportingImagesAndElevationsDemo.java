/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import javax.swing.*;
import javax.xml.xpath.XPath;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This application provides a demonstration of how to import image data and elevation data into a World Wind Java based
 * application.
 *
 * @author dcollins
 * @version $Id: ImportingImagesAndElevationsDemo.java 14056 2010-10-27 09:11:24Z garakl $
 */
public class ImportingImagesAndElevationsDemo extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected ImportedDataFrame importedDataFrame;

        public AppFrame()
        {
            this.importedDataFrame = new ImportedDataFrame(WorldWind.getDataFileStore(), this.getWwd());
            WWUtil.alignComponent(this, this.importedDataFrame, AVKey.RIGHT);
            this.importedDataFrame.setVisible(true);

            // Setup AVKey.LAYERS property change events to refresh the LayerPanel.
            this.getWwd().getModel().addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent event)
                {
                    if (event.getPropertyName().equals(AVKey.LAYERS))
                    {
                        getLayerPanel().update(getWwd());
                    }
                }
            });

            this.layoutComponents();
        }

        public ImportedDataFrame getImportedDataFrame()
        {
            return this.importedDataFrame;
        }

        protected void layoutComponents()
        {
            JButton button = new JButton("Show Imported Data...");
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    getImportedDataFrame().setVisible(true);
                }
            });

            Box box = Box.createVerticalBox();
            box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // top, left, bottom, right
            box.add(button);
            this.getLayerPanel().add(box, BorderLayout.SOUTH);
            this.validate();
            this.pack();
        }
    }

    public static class ImportedDataFrame extends JFrame
    {
        protected FileStore fileStore;
        protected ImportedDataPanel dataConfigPanel;
        protected JFileChooser importFileChooser;

        public ImportedDataFrame(FileStore fileStore, WorldWindow worldWindow) throws HeadlessException
        {
            if (fileStore == null)
            {
                String msg = Logging.getMessage("nullValue.FileStoreIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            this.fileStore = fileStore;
            this.dataConfigPanel = new ImportedDataPanel("Imported Surface Data", worldWindow);
            this.importFileChooser = new JFileChooser(Configuration.getUserHomeDirectory());
            this.importFileChooser.setAcceptAllFileFilterUsed(true);
            this.importFileChooser.setMultiSelectionEnabled(false);
            this.importFileChooser.addChoosableFileFilter(new ImportableDataFilter());

            this.layoutComponents();
            this.loadPreviouslyImportedData();
        }

        protected void loadPreviouslyImportedData()
        {
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    loadImportedDataFromFileStore(fileStore, dataConfigPanel);
                }
            });
            t.start();
        }

        protected void importFromFile()
        {
            int retVal = this.importFileChooser.showDialog(this, "Import");
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;

            final File file = this.importFileChooser.getSelectedFile();
            if (file == null) // This should never happen, but we check anyway.
                return;

            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    Document dataConfig = null;

                    try
                    {
                        // Import the file into a form usable by World Wind components.
                        dataConfig = importDataFromFile(ImportedDataFrame.this, file, fileStore);
                    }
                    catch (Exception e)
                    {
                        final String message = e.getMessage();

                        // Show a message dialog indicating that the import failed, and why.
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                JOptionPane.showMessageDialog(ImportedDataFrame.this, message, "Import Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }

                    if (dataConfig != null)
                    {
                        addImportedData(dataConfig, null, dataConfigPanel);
                    }
                }
            });
            thread.start();
        }

        protected void layoutComponents()
        {
            this.setTitle("Imported Data");
            this.getContentPane().setLayout(new BorderLayout(0, 0)); // hgap, vgap
            this.getContentPane().add(this.dataConfigPanel, BorderLayout.CENTER);

            JButton importButton = new JButton("Import...");
            importButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    importFromFile();
                }
            });
            Box box = Box.createHorizontalBox();
            box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // top, left, bottom, right
            box.add(importButton);
            this.getContentPane().add(box, BorderLayout.SOUTH);

            this.setPreferredSize(new Dimension(400, 500));
            this.validate();
            this.pack();
        }
    }

    protected static void addImportedData(final Document dataConfig, final AVList params,
        final ImportedDataPanel panel)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    addImportedData(dataConfig, params, panel);
                }
            });
        }
        else
        {
            panel.addImportedData(dataConfig.getDocumentElement(), params);
        }
    }

    //**************************************************************//
    //********************  Loading Previously Imported Data  ******//
    //**************************************************************//

    protected static void loadImportedDataFromDirectory(File dir, ImportedDataPanel panel)
    {
        String[] names = WWIO.listDescendantFilenames(dir, new DataConfigurationFilter(), false);
        if (names == null || names.length == 0)
            return;

        for (String filename : names)
        {
            Document doc = null;

            try
            {
                File dataConfigFile = new File(dir, filename);
                doc = WWXML.openDocument(dataConfigFile);
                doc = DataConfigurationUtils.convertToStandardDataConfigDocument(doc);
            }
            catch (WWRuntimeException e)
            {
                e.printStackTrace();
            }

            if (doc == null)
                continue;

            // This data configuration came from an existing file from disk, therefore we cannot guarantee that the
            // current version of World Wind's data importers produced it. This data configuration file may have been
            // created by a previous version of World Wind, or by another program. Set fallback values for any missing
            // parameters that World Wind needs to construct a Layer or ElevationModel from this data configuration.
            AVList params = new AVListImpl();
            setFallbackParams(doc, filename, params);

            // Add the data configuraiton to the ImportedDataPanel.
            addImportedData(doc, params, panel);
        }
    }

    protected static void loadImportedDataFromFileStore(FileStore fileStore, ImportedDataPanel panel)
    {
        for (File file : fileStore.getLocations())
        {
            if (!file.exists())
                continue;

            if (!fileStore.isInstallLocation(file.getPath()))
                continue;

            loadImportedDataFromDirectory(file, panel);
        }
    }

    protected static void setFallbackParams(Document dataConfig, String filename, AVList params)
    {
        XPath xpath = WWXML.makeXPath();
        Element domElement = dataConfig.getDocumentElement();

        // If the data configuration document doesn't define a cache name, then compute one using the file's path
        // relative to its file cache directory.
        String s = WWXML.getText(domElement, "DataCacheName", xpath);
        if (s == null || s.length() == 0)
            DataConfigurationUtils.getDataConfigCacheName(filename, params);

        // If the data configuration document doesn't define the data's extreme elevations, provide default values using
        // the minimum and maximum elevations of Earth.
        String type = DataConfigurationUtils.getDataConfigType(domElement);
        if (type.equalsIgnoreCase("ElevationModel"))
        {
            if (WWXML.getDouble(domElement, "ExtremeElevations/@min", xpath) == null)
                params.setValue(AVKey.ELEVATION_MIN, -11000d); // Depth of Mariana trench.
            if (WWXML.getDouble(domElement, "ExtremeElevations/@max", xpath) == null)
                params.setValue(AVKey.ELEVATION_MAX, 8500d); // Height of Mt. Everest.
        }
    }

    //**************************************************************//
    //********************  Importing Data From File  **************//
    //**************************************************************//

    protected static Document importDataFromFile(Component parentComponent, File file, FileStore fileStore)
        throws Exception
    {
        // Create a DataStoreProducer which is capable of processing the file.
        final DataStoreProducer producer = createDataStoreProducerFromFile(file);
        if (producer == null)
        {
            throw new IllegalArgumentException("Unrecognized file type");
        }

        // Create a ProgressMonitor that will provide feedback on how
        final ProgressMonitor progressMonitor = new ProgressMonitor(parentComponent,
            "Importing " + file.getName(), null, 0, 100);

        final AtomicInteger progress = new AtomicInteger(0);

        // Configure the ProgressMonitor to receive progress events from the DataStoreProducer. This stops sending
        // progress events when the user clicks the "Cancel" button, ensuring that the ProgressMonitor does not
        PropertyChangeListener progressListener = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (progressMonitor.isCanceled())
                    return;

                if (evt.getPropertyName().equals(AVKey.PROGRESS))
                    progress.set((int) (100 * (Double) evt.getNewValue()));
            }
        };
        producer.addPropertyChangeListener(progressListener);
        progressMonitor.setProgress(0);

        // Configure a timer to check if the user has clicked the ProgressMonitor's "Cancel" button. If so, stop
        // production as soon as possible. This just stops the production from completing; it doesn't clean up any state
        // changes made during production,
        java.util.Timer progressTimer = new java.util.Timer();
        progressTimer.schedule(new TimerTask()
        {
            public void run()
            {
                progressMonitor.setProgress(progress.get());

                if (progressMonitor.isCanceled())
                {
                    producer.stopProduction();
                    this.cancel();
                }
            }
        }, progressMonitor.getMillisToDecideToPopup(), 100L);

        Document doc = null;
        try
        {
            // Import the file into the specified FileStore.
            doc = createDataStoreFromFile(file, fileStore, producer);

            // The user clicked the ProgressMonitor's "Cancel" button. Revert any change made during production, and
            // discard the returned DataConfiguration reference.
            if (progressMonitor.isCanceled())
            {
                doc = null;
                producer.removeProductionState();
            }
        }
        finally
        {
            // Remove the progress event listener from the DataStoreProducer. stop the progress timer, and signify to the
            // ProgressMonitor that we're done.
            producer.removePropertyChangeListener(progressListener);
            progressMonitor.close();
            progressTimer.cancel();
        }

        return doc;
    }

    protected static Document createDataStoreFromFile(File file, FileStore fileStore,
        DataStoreProducer producer) throws Exception
    {
        File importLocation = DataImportUtil.getDefaultImportLocation(fileStore);
        if (importLocation == null)
        {
            String message = Logging.getMessage("generic.NoDefaultImportLocation");
            Logging.logger().severe(message);
            return null;
        }

        // Create the production parameters. These parameters instruct the DataStoreProducer where to import the cached
        // data, and what name to put in the data configuration document.
        AVList params = new AVListImpl();
        params.setValue(AVKey.DATASET_NAME, file.getName());
        params.setValue(AVKey.DATA_CACHE_NAME, file.getName());
        params.setValue(AVKey.FILE_STORE_LOCATION, importLocation.getAbsolutePath());
        producer.setStoreParameters(params);

        // Use the specified file as the the production data source.
        producer.offerDataSource(file, null);

        try
        {
            // Convert the file to a form usable by World Wind components, according to the specified DataStoreProducer.
            // This throws an exception if production fails for any reason.
            producer.startProduction();
        }
        catch (Exception e)
        {
            // Exception attempting to convert the file. Revert any change made during production.
            producer.removeProductionState();
            throw e;
        }

        // Return the DataConfiguration from the production results. Since production sucessfully completed, the
        // DataStoreProducer should contain a DataConfiguration in the production results. We test the production
        // results anyway.
        Iterable results = producer.getProductionResults();
        if (results != null && results.iterator() != null && results.iterator().hasNext())
        {
            Object o = results.iterator().next();
            if (o != null && o instanceof Document)
            {
                return (Document) o;
            }
        }

        return null;
    }

    //**************************************************************//
    //********************  Utility Methods  ***********************//
    //**************************************************************//

    protected static DataStoreProducer createDataStoreProducerFromFile(File file)
    {
        if (file == null)
            return null;

        DataStoreProducer producer = null;

        if (DataImportUtil.isElevationData(file))
            producer = new TiledElevationProducer();
        else if (DataImportUtil.isImageData(file))
            producer = new TiledImageProducer();
        else if (DataImportUtil.isWWDotNetLayerSet(file))
            producer = new WWDotNetLayerSetConverter();

        return producer;
    }

    protected static class ImportableDataFilter extends javax.swing.filechooser.FileFilter
    {
        public ImportableDataFilter()
        {
        }

        public boolean accept(File file)
        {
            if (file == null || file.isDirectory())
                return true;

            if (DataImportUtil.isElevationData(file))
                return true;

            if (DataImportUtil.isImageData(file))
                return true;

            //noinspection RedundantIfStatement
            if (DataImportUtil.isWWDotNetLayerSet(file))
                return true;

            return false;
        }

        public String getDescription()
        {
            return "Supported Images/Elevations";
        }
    }

    //**************************************************************//
    //********************  Main Method  ***************************//
    //**************************************************************//

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Image and Elevation Import", AppFrame.class);
    }
}
