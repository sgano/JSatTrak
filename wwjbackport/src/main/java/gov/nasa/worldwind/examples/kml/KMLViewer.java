/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples.kml;

import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.examples.util.BalloonController;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;

/**
 * KML Viewer. Currently under initial development.
 *
 * @author tag
 * @version $Id: KMLViewer.java 14087 2010-11-05 19:57:10Z pabercrombie $
 */
public class KMLViewer extends ApplicationTemplate
{
    public static class KMLAppPanel extends ApplicationTemplate.AppPanel
    {
        protected BalloonController balloonController;

        public KMLAppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            super(canvasSize, includeStatusBar);

            // Add a controller to display balloons when placemarks are clicked
            this.balloonController = new BalloonController(this.getWwd());
        }
    }

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);
        }
        
        @Override
        protected AppPanel createAppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            return new KMLAppPanel(canvasSize, includeStatusBar);
        }
    }

    public static class WorkerThread extends Thread
    {
        protected Object kmlSource;
        protected AppFrame appFrame;
        protected KMLController kmlController;

        public WorkerThread(Object kmlSource, AppFrame appFrame)
        {
            this.kmlSource = kmlSource;
            this.appFrame = appFrame;
        }

        public void run()
        {
            try
            {
                KMLRoot kmlRoot = KMLRoot.create(kmlSource);
                if (kmlRoot == null)
                {
                    String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
                        kmlSource.toString());
                    throw new IllegalArgumentException(message);
                }

                kmlRoot.parse();
                this.kmlController = new KMLController(kmlRoot);
                final RenderableLayer layer = new RenderableLayer();
                layer.addRenderable(this.kmlController);
                layer.setName(formName(kmlSource, kmlRoot));

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        insertBeforePlacenames(appFrame.getWwd(), layer);
                        appFrame.getLayerPanel().update(appFrame.getWwd());
                    }
                });
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (XMLStreamException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String formName(Object kmlSource, KMLRoot kmlRoot)
    {
        KMLAbstractFeature rootFeature = kmlRoot.getFeature();

        if (rootFeature != null && !WWUtil.isEmpty(rootFeature.getName()))
            return rootFeature.getName();

        if (kmlSource instanceof File)
            return ((File) kmlSource).getName();

        if (kmlSource instanceof URL)
            return ((URL) kmlSource).getPath();

        if (kmlSource instanceof String && WWIO.makeURL((String) kmlSource) != null)
            return WWIO.makeURL((String) kmlSource).getPath();

        return "KML Layer";
    }

    protected static void makeMenu(final AppFrame appFrame)
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("KML/KMZ File", "kml", "kmz"));

        JMenuBar menuBar = new JMenuBar();
        appFrame.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openFileMenuItem = new JMenuItem(new AbstractAction("Open File...")
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    int status = fileChooser.showOpenDialog(appFrame);
                    if (status == JFileChooser.APPROVE_OPTION)
                    {
                        new WorkerThread(fileChooser.getSelectedFile(), appFrame).start();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        fileMenu.add(openFileMenuItem);

        JMenuItem openURLMenuItem = new JMenuItem(new AbstractAction("Open URL...")
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    String status = JOptionPane.showInputDialog(appFrame, "URL");
                    if (!WWUtil.isEmpty(status))
                    {
                        new WorkerThread(status, appFrame).start();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        fileMenu.add(openURLMenuItem);
    }

//    final static String DEFAULT_FILE = "testData/KML/DatelineSpanners.kml";
//    final static String DEFAULT_FILE = "testData/KML/LineStringPlacemark.kml";
//    final static String DEFAULT_FILE = "testData/KML/LinearRingPlacemark.kml";
//    final static String DEFAULT_FILE = "testData/KML/GoogleTutorialExample01.kml";
//    final static String DEFAULT_FILE = "testData/KML/PointPlacemark.kml";
//    final static String DEFAULT_FILE = "testData/KML/MultiGeometryPlacemark.kml";
//    final static String DEFAULT_FILE = "demodata/KML/PolygonPlacemark.kml";
//    final static String DEFAULT_FILE = "http://code.google.com/apis/kml/documentation/KML_Samples.kml";

    public static void main(String[] args)
    {
        final AppFrame af = (AppFrame) ApplicationTemplate.start("World Wind KML Viewer", AppFrame.class);
        makeMenu(af);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
//                new WorkerThread(DEFAULT_FILE, af).start();
            }
        });
    }
}
