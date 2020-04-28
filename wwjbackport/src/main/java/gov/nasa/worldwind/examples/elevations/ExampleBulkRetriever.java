/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples.elevations;

import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.retrieve.BulkRetrievalThread;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author Lado Garakanidze
 * @version $Id: ExampleBulkRetriever.java 13950 2010-10-07 17:40:47Z dcollins $
 */

public class ExampleBulkRetriever extends ApplicationTemplate
{
    public static final String ACTION_COMMAND_BUTTON_START_BULK_RETRIEVER = "ActionCommand_Button_StartBulkRetriever";
    public static final String ACTION_COMMAND_BUTTON_START_GET_ELEVATIONS = "ActionCommand_Button_StartGetElevations";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected ElevationsDemoController controller;
        protected LayerPanel layerPanel;

        public AppFrame()
        {
            // We add our own LayerPanel, but keep the StatusBar from ApplicationTemplate.
            super(true, false, false);
            this.controller = new ElevationsDemoController(this.getWwd());
            this.controller.frame = this;
            this.makeComponents();
            this.getLayerPanel().update(this.getWwd());
            this.pack();
        }

        public LayerPanel getLayerPanel()
        {
            return this.layerPanel;
        }

        protected void makeComponents()
        {
            this.getWwd().setPreferredSize(new Dimension(1024, 768));

            JPanel panel = new JPanel(new BorderLayout());
            {
                panel.setBorder(new EmptyBorder(10, 0, 10, 0));

                JPanel controlPanel = new JPanel(new BorderLayout(0, 10));
                controlPanel.setBorder(new EmptyBorder(20, 10, 20, 10));

                JPanel btnPanel = new JPanel(new GridLayout(5, 1, 0, 5));
                {
                    JButton btn = new JButton("Start bulk retriever");
                    btn.setActionCommand(ACTION_COMMAND_BUTTON_START_BULK_RETRIEVER);
                    btn.addActionListener(this.controller);
                    btnPanel.add(btn);

                    btn = new JButton("Start getElevations()");
                    btn.setActionCommand(ACTION_COMMAND_BUTTON_START_GET_ELEVATIONS);
                    btn.addActionListener(this.controller);
                    btnPanel.add(btn);
                }
                controlPanel.add(btnPanel, BorderLayout.NORTH);

                panel.add(controlPanel, BorderLayout.SOUTH);

                this.layerPanel = new LayerPanel(this.getWwd(), null);
                panel.add(this.layerPanel, BorderLayout.CENTER);
            }
            getContentPane().add(panel, BorderLayout.WEST);
        }
    }

    public static class ElevationsDemoController implements ActionListener
    {
        protected ExampleBulkRetriever.AppFrame frame;
        // World Wind stuff.
        protected WorldWindowGLCanvas wwd;

        public ElevationsDemoController(WorldWindowGLCanvas wwd)
        {
            this.wwd = wwd;
        }

        public void actionPerformed(ActionEvent e)
        {
            if (ACTION_COMMAND_BUTTON_START_BULK_RETRIEVER.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doStartBulkRetriever();
            }
            else if (ACTION_COMMAND_BUTTON_START_GET_ELEVATIONS.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doStartGetElevations();
            }
        }

        public void doStartBulkRetriever()
        {
            // Now, let's find WMSBasicElevationModel
            WMSBasicElevationModel wmsbem = this.findWMSBasicElevationModel();
            if (null != wmsbem)
            {
                Sector s = Sector.fromDegrees(45, 47, -124, -123);
                double maxResolution = (Angle.fromDegrees(1)).getRadians() / 3600; // DTED 2 (1/3600)

                BulkRetrievalThread bulkRetrievalThread = wmsbem.makeLocal(s, maxResolution, null);
                new ActionUpdateBulkRetrieverStatus(bulkRetrievalThread).start(1000 /* update every 1 second */);
            }
            else
            {
                String message = Logging.getMessage("ElevationModel.ExceptionRequestingElevations",
                    "No instance of WMSBasicElevationModel was found");
                Logging.logger().severe(message);
            }
        }

        protected class ActionUpdateBulkRetrieverStatus extends AbstractAction
        {
            protected BulkRetrievalThread bulkRetrievalThread;
            protected Timer timer;

            public ActionUpdateBulkRetrieverStatus(BulkRetrievalThread bulkRetrievalThread)
            {
                this.bulkRetrievalThread = bulkRetrievalThread;
                this.timer = null;
            }

            public void start(int intervalInMillis)
            {
                this.timer = new Timer(intervalInMillis, this);
                this.timer.start();
            }

            public void stop()
            {
                if (null != this.timer)
                {
                    this.timer.stop();
                    this.timer = null;
                }

                if (null != this.bulkRetrievalThread && this.bulkRetrievalThread.isAlive())
                {
                    try
                    {
                        this.bulkRetrievalThread.join();
                    }
                    catch (Exception e)
                    {
                        Logging.logger().finest(e.getMessage());
                    }
                }
            }

            public void actionPerformed(ActionEvent e)
            {
                if (null == this.bulkRetrievalThread)
                    return;

                boolean isAlive = this.bulkRetrievalThread.isAlive();

                Logging.logger().info("BulkRetrievalThread [" + this.bulkRetrievalThread.getId() + "] :"
                    + " isAlive=" + isAlive
                    + " state=" + this.bulkRetrievalThread.getState().name()
                    + " current=" + this.bulkRetrievalThread.getProgress().getCurrentCount()
                    + " total=" + this.bulkRetrievalThread.getProgress().getTotalCount()
                );
                if (!isAlive && null != this.timer)
                    this.timer.stop();
            }
        }

        public void doStartGetElevations()
        {
            Logging.logger().info("Button `Start getElevations()` pressed");

            // request resolution of DTED2 (1degree / 3600 )
            double targetResolution = Angle.fromDegrees(1d).radians / 3600;

            ElevationModel model = this.wwd.getModel().getGlobe().getElevationModel();

            ArrayList<LatLon> latlons = new ArrayList<LatLon>();

            for (double lat = 45d; lat < 46d; lat += 1d / 3600d)
            {
                latlons.add(LatLon.fromDegrees(lat, -123.3d));
            }

            ArrayList<Position> elevations = new ArrayList<Position>();

            this.readElevations(model, latlons, elevations, targetResolution, 30 * 1000 /* timeout in millis */);
        }

        public void doUpdateElevations(final ArrayList<Position> elevations)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    Logging.logger().info("Success: end of getElevations(); number of results=" + elevations.size());

                    for (Position p : elevations)
                    {
                        Logging.logger().finest(p.toString());
                    }
                }
            });
        }

        public void readElevations(final ElevationModel model,
            final List<? extends LatLon> locations,
            final ArrayList<Position> elevations,
            final double targetResolution,
            final long timeOutInMillis)
        {
            Thread thread = new Thread()
            {
                protected Double readElevation(LatLon ll)
                {
                    double[] results = new double[1];
                    ArrayList<LatLon> pos = new ArrayList<LatLon>();
                    pos.add(ll);

                    Sector s = Sector.fromDegrees(ll.getLatitude().degrees, ll.getLatitude().degrees,
                        ll.getLongitude().degrees, ll.getLongitude().degrees);

                    double achievedResolution = model.getElevations(s, pos, targetResolution, results);
                    return (achievedResolution <= targetResolution) ? results[0] : null;
                }

                public void run()
                {
                    try
                    {
                        long startTime = System.currentTimeMillis();
                        long timeOut = startTime + timeOutInMillis;

                        // clone list of locations, becuase we will start remove processed locations
                        List<LatLon> latlons = new ArrayList<LatLon>(locations);

                        execute:
                        {
                            while (latlons.size() > 0)
                            {
                                Iterator<LatLon> iterator = latlons.iterator();
                                while (iterator.hasNext())
                                {
                                    if (System.currentTimeMillis() > timeOut)
                                        break execute;

                                    LatLon ll = iterator.next();
                                    Double elev = readElevation(ll);
                                    if (elev != null)
                                    {
                                        iterator.remove();
                                        elevations.add(new Position(ll, elev));

                                        // tile IS in the memory cache, but it is a common practice to sleep(0)
                                        // to let other higher priority threads to run, if there are any
                                        Thread.sleep(0);
                                    }
                                    else
                                    {   // tile is NOT in the memory cache, let it chance to be loaded
                                        Thread.sleep(5);
                                    }
                                }

                                if (latlons.size() > 0)
                                    Thread.sleep(50);
                            }
                            Logging.logger().info("Total time = " + (System.currentTimeMillis() - startTime));
                        }

                        doUpdateElevations(elevations);
                    }
                    catch (InterruptedException e)
                    {
                        Logging.logger().severe(e.getMessage());
                    }
                }
            };

            thread.start();
        }

        protected WMSBasicElevationModel findWMSBasicElevationModel()
        {
            WMSBasicElevationModel wmsbem = null;

            ElevationModel model = this.wwd.getModel().getGlobe().getElevationModel();
            if (model instanceof CompoundElevationModel)
            {
                CompoundElevationModel cbem = (CompoundElevationModel) model;
                for (ElevationModel em : cbem.getElevationModels())
                {
                    // you can have additional checks if you know specific model name, etc.
                    if (em instanceof WMSBasicElevationModel)
                    {
                        wmsbem = (WMSBasicElevationModel) em;
                        break;
                    }
                }
            }
            else if (model instanceof WMSBasicElevationModel)
            {
                wmsbem = (WMSBasicElevationModel) model;
            }

            return wmsbem;
        }
    }

    public static void main(String[] args)
    {
        start("World Wind - bulk elevations demo", AppFrame.class);
    }
}
