/* Copyright (C) 2001, 2007 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;

/**
 * App for stress testing WWJ functions.  Just start it and let it run.
 *
 * @author jparsons
 * @version $Id$
 */
public class StressWWJIterator implements ActionListener
{
    private WorldWindowGLCanvas wwd;
    private ApplicationTemplate.AppFrame frame;
    private int viewIterations=0;
    private long totIterations=0;
    private int markerIterations=0;
    private int numRounds = 0;
    private int airspaceIterations=0;
    private static ArrayList<Marker> markers = new ArrayList<Marker>();
    private static StressAirspace airspace;
    private int wmsIterations=0;
    private int wmsLayerCount=0;
    private StressWMS stressWMS=null;
    private ArrayList<Layer> addedLayers = new ArrayList<Layer>();
    private long maxMemUsed=0;
    private long currentMemUsed=0;

    //Max values
    private int maxViewIterations = 1;
    private int maxMarkerIterations = 10;
    private int numMarkersPerIteration = 1000;
    private int maxAirspaceIterations = 10;
    private int numAirspacePerIteration = 500;
    private int timePerIteration = 45000; //in ms
    private int maxWMSIterations = 2;
    private final long maxCacheCap = 220000000;
    private final long minCacheCap = 180000000;
    private final long maxCacheLowCap = 180000000;
    private final long minCacheLowCap = 10000000;
    private boolean minimizeWindow = false;
    private String externalWMSURL = "http://neowms.sci.gsfc.nasa.gov/wms/wms";

    //Property Keys
    public static String PROPERTIES_FILE = "config/stresswwj.properties";
    public static String VIEW_ITERATIONS_KEY = "gov.nasa.worldwind.avkey.ViewIterations";
    public static String TIME_PER_ITERTION_KEY = "gov.nasa.worldwind.avkey.TimePerIteration";
    public static String MARKER_ITERATIONS_KEY = "gov.nasa.worldwind.avkey.MarkerIterations";
    public static String AIRSPACE_ITERATIONS_KEY = "gov.nasa.worldwind.avkey.AirspaceIterations";
    public static String MARKERS_PER_ITERATION_KEY = "gov.nasa.worldwind.avkey.MarkersPerIteration";
    public static String AIRSPACES_PER_ITERATION_KEY = "gov.nasa.worldwind.avkey.AirspacesPerIteration";
    public static String WMS_ITERATIONS_KEY = "gov.nasa.worldwind.avkey.WMSIterations";
    public static String MINIMIZE_WINDOW_KEY = "gov.nasa.worldwind.avkey.MinimizeWindow";
    public static String EXTERNAL_WMS_KEY = "gov.nasa.worldwind.avkey.ExternalWMS";

    //label strings
    private final static String FREE_MEM_STR= "Free memory: ";
    private final static String TOTAL_MEM_STR="Total memory: ";
    private final static String TOT_ITERATIONS_STR = "Total iterations: ";
    private final static String NUM_ROUNDS_STR = "Number of completed rounds: ";
    private final static String NUM_ACTIVE_MARKERS_STR = "# Active Markers: ";
    private final static String NUM_VIEW_ITERATIONS_STR = "View iterations: ";
    private final static String NUM_MARKER_ITERATIONS_STR = "Marker iterations: ";

    //Labels
    private JLabel freeMemLbl = new JLabel(FREE_MEM_STR);
    private JLabel totalMemLbl = new JLabel(TOTAL_MEM_STR);
    private JLabel iterationsLbl = new JLabel(TOT_ITERATIONS_STR);
    private JLabel roundsLbl = new JLabel(NUM_ROUNDS_STR);
    private JLabel viewLbl = new JLabel(NUM_VIEW_ITERATIONS_STR + "0 of "+maxViewIterations);
    private JLabel markerLbl = new JLabel(NUM_MARKER_ITERATIONS_STR + "0 of "+maxMarkerIterations);
    private JLabel airspaceLbl = new JLabel("Airspace iterations: " + "0 of "+maxAirspaceIterations);
    private JLabel numObjectsLbl = new JLabel();
    private JLabel statusLbl = new JLabel("status:");
    private JLabel wmsLbl = new JLabel("WMS Iterations " + wmsIterations + " of " + maxWMSIterations);

    private static final MarkerAttributes[] attrs = new BasicMarkerAttributes[]
    {
        new BasicMarkerAttributes(Material.DARK_GRAY,BasicMarkerShape.SPHERE, 1d, 10, 5),
        new BasicMarkerAttributes(Material.RED, BasicMarkerShape.CONE, 0.7),
        new BasicMarkerAttributes(Material.YELLOW, BasicMarkerShape.CYLINDER,
            0.9),
        new BasicMarkerAttributes(Material.CYAN, BasicMarkerShape.SPHERE,
            0.7),
        new BasicMarkerAttributes(Material.GREEN, BasicMarkerShape.CONE, 1d),
        new BasicMarkerAttributes(Material.PINK, BasicMarkerShape.ORIENTED_SPHERE,
            0.8),
        new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.CONE, 0.6),
    };

    public StressWWJIterator(ApplicationTemplate.AppFrame frame0)
    {
        this.frame=frame0;
        this.wwd=frame.getWwd();
        readPropertiesFile();
        airspace = new StressAirspace();

        try{
            if (maxWMSIterations > 0)
                stressWMS=new StressWMS(externalWMSURL);
        }catch(URISyntaxException uriEX)
        {
            Logging.logger().severe("Error initializing WMS component: " + uriEX.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        long timeToMove = timePerIteration / 2L;   //time for "movements"
        nextViewState( timeToMove);
    }

    private void readPropertiesFile()
    {
        StressTestConfiguration readProp = new StressTestConfiguration(PROPERTIES_FILE);

        maxViewIterations = readProp.getIntegerValue(VIEW_ITERATIONS_KEY, maxViewIterations);
        maxMarkerIterations = readProp.getIntegerValue(MARKER_ITERATIONS_KEY, maxMarkerIterations);
        timePerIteration = readProp.getIntegerValue(TIME_PER_ITERTION_KEY, timePerIteration);
        maxAirspaceIterations = readProp.getIntegerValue(AIRSPACE_ITERATIONS_KEY, maxAirspaceIterations);
        numMarkersPerIteration = readProp.getIntegerValue(MARKERS_PER_ITERATION_KEY, numMarkersPerIteration);
        numAirspacePerIteration = readProp.getIntegerValue(AIRSPACES_PER_ITERATION_KEY, numAirspacePerIteration);
        maxWMSIterations = readProp.getIntegerValue(WMS_ITERATIONS_KEY, maxWMSIterations);

        String oms = readProp.getStringValue(MINIMIZE_WINDOW_KEY);
        if (oms != null)
            minimizeWindow = oms.startsWith("t") || oms.startsWith("T");
    }

    public void runWWJIterator()
    {
        javax.swing.Timer timer = new javax.swing.Timer(timePerIteration, this);
        timer.setInitialDelay(5000);
        Logging.logger().info("Starting Stress Iterator");
        timer.start();
    }

    public ArrayList<Marker> getMarkers()
    {
        return markers;
    }

    public AirspaceLayer getAirspaceLayer()
    {
        return airspace.getAirspaceLayer();
    }

    public void updateStressStatsPanel()
    {
        long freeMem  = (java.lang.Runtime.getRuntime().freeMemory()/(1024*1024));
        long totalMem = (java.lang.Runtime.getRuntime().totalMemory()/(1024*1024));
        currentMemUsed = totalMem-freeMem;
        maxMemUsed = Math.max(currentMemUsed, maxMemUsed);

        freeMemLbl.setText(FREE_MEM_STR + freeMem +"mb");
        totalMemLbl.setText(TOTAL_MEM_STR + totalMem +"mb");
        iterationsLbl.setText(TOT_ITERATIONS_STR +  totIterations);
        roundsLbl.setText(NUM_ROUNDS_STR + numRounds);
        viewLbl.setText(NUM_VIEW_ITERATIONS_STR + viewIterations + " of " +maxViewIterations);
        markerLbl.setText(NUM_MARKER_ITERATIONS_STR + markerIterations + " of " + maxMarkerIterations );

        if ( markers.size() > 0)
            numObjectsLbl.setText(NUM_ACTIVE_MARKERS_STR + markers.size());
        else if (airspace.size() > 0)
            numObjectsLbl.setText("Airspace objects: " + airspace.size());
        else
            numObjectsLbl.setText("no surface objects");

        airspaceLbl.setText("Airspace iterations: " + airspaceIterations + " of " + maxAirspaceIterations );
        wmsLbl.setText("WMS iterations: " + wmsIterations + " of " + maxWMSIterations );
    }

    private void nextViewState(long timeToMove)
    {
        String logMsg;
        if (wwd != null
            && wwd.getView() != null
            && wwd.getView() instanceof OrbitView
            && wwd.getModel() != null
            && wwd.getModel().getGlobe() != null)
        {
            Globe globe = wwd.getModel().getGlobe();

            if (viewIterations < maxViewIterations)
            {
                viewIterations++;
                totIterations++;
                logMsg = "Round: " + numRounds + " | Total Iterations: " + totIterations + " | View Iterations: " + viewIterations;
                Position randPos = randomPosition(globe);
                double zoom = randPos.getElevation() + 2000 + (2000 * Math.random());
                Position center = new Position(randPos, zoom);
                moveToandAlter(timeToMove, center, viewIterations);
            }
            else if (markerIterations < maxMarkerIterations)
            {
                markerIterations++;
                totIterations++;
                logMsg = "Round: " + numRounds + " | Total Iterations: " + totIterations + " | Marker Iterations: " + markerIterations;
                Position randPos=null;
                statusLbl.setText("status:  Adding Markers");
                final ArrayList<Marker> newMarkers = new ArrayList<Marker>();
                for (int i=0; i < numMarkersPerIteration; i++)
                {
                    randPos = randomPosition(globe);
                    double lat = randPos.getLatitude().getDegrees();
                    double lon = randPos.getLongitude().getDegrees();
                    Marker marker = new BasicMarker(Position.fromDegrees( lat, lon, 0),
                            attrs[(int) (Math.abs(lat) + Math.abs(lon)) % attrs.length]);
                    marker.setPosition(Position.fromDegrees(lat, lon, 0));
                    marker.setHeading(Angle.fromDegrees(lat * 5));
                    newMarkers.add(marker);
                }

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        markers.addAll(newMarkers);
                    }
                });

                //zoom to last marker created
                Position center = new Position(randPos, 50000);

                moveToandAlter(timeToMove, center, markerIterations);
            }
            else if ( airspaceIterations < maxAirspaceIterations)
            {
                statusLbl.setText("status:  Adding Airspace objects");
                if ( airspaceIterations == 0)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {  
                            if(markers.size() > 0)
                                markers.clear();
                        }
                    });
                }
                //Call GC
                System.gc();
                airspaceIterations++;
                totIterations++;
                logMsg = "Round: " + numRounds + " | Total Iterations: " + totIterations + " | Airspace Iterations: " + airspaceIterations;
                Position randPos=null;

                for (int i=0; i < numAirspacePerIteration; i++)
                {
                    randPos=randomPosition(globe);
                    airspace.addAirspace(randPos);
                }

                statusLbl.setText("status: Moving to new location");
                //zoom to last marker created
                Position center = new Position(randPos, 1000000); //markers are big, don't zoom in close
                moveToandAlter(timeToMove, center, airspaceIterations);
            }
            else if (wmsIterations < maxWMSIterations)
            {

                if(wmsIterations == 0)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            if(markers.size() > 0)
                                markers.clear();

                            if ( airspace.size() > 0)
                                airspace.clearAirspace();
                       }
                    });
                }
                //Call GC
                System.gc();
                wmsIterations++;
                totIterations++;
                logMsg = "Round: " + numRounds + " | Total Iterations: " + totIterations + " | WMS Iterations: " + wmsIterations;
                if (( wmsLayerCount <= (stressWMS.size()-1)) && ((wmsIterations % 3) == 1))
                {

                    statusLbl.setText("status:  Adding WMS Layer");
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            statusLbl.setText("status:  Adding WMS Layer");
                            Layer wmsLayer =  stressWMS.getLayer(wmsLayerCount);
                            addedLayers.add(wmsLayer);
                            wmsLayerCount++;
                            ApplicationTemplate.insertBeforePlacenames(wwd, wmsLayer);
                            frame.getLayerPanel().update(wwd);
                        }
                    });

                    moveToDefaut(timeToMove);
                }
                else
                {
                    Position randPos = randomPosition(globe);
                    double zoom = randPos.getElevation()+ (1000000); //do not need to zoom in for these layers
                    Position center = new Position(randPos, zoom); 
                    moveToandAlter(timeToMove, center, 1);
                }
            }
            else
            {
                statusLbl.setText("status: Reseting for next round");
                moveToDefaut(timeToMove);

                viewIterations=0;
                numRounds++;
                markerIterations=0;
                airspaceIterations=0;
                wmsIterations=0;
                wmsLayerCount=0;
                logMsg = "Round: " + numRounds + " | Total Iterations: " + totIterations;
                if(markers.size() > 0)
                    markers.clear();

                if ( airspace.size() > 0)
                    airspace.clearAirspace();

                //reset to orig layers
                if ( addedLayers.size() > 0)
                {
                    wwd.getModel().getLayers().removeAll(addedLayers);
                    addedLayers.clear();
                }
                
                //Call GC
                System.gc();
                wwd.redraw();
                frame.getLayerPanel().update(wwd);

                //Alter Texture Cache values
                long cap = new Double(wwd.getTextureCache().getCapacity() * .95).longValue();
                if ( cap < minCacheCap)
                    wwd.getTextureCache().setCapacity(maxCacheCap);
                else
                    wwd.getTextureCache().setCapacity(cap);

                cap = new Double(wwd.getTextureCache().getLowWater() * .95).longValue();
                if ( cap < minCacheLowCap)
                    wwd.getTextureCache().setLowWater(maxCacheLowCap);
                else
                    wwd.getTextureCache().setLowWater(cap);


                if(minimizeWindow)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            for (int i =0; i<5; i++)
                            {
                                try{
                                    Thread.sleep(500);
                                    frame.setVisible(false);
                                    Thread.sleep(500);
                                    frame.setVisible(true);
                                }
                                catch(InterruptedException ie)
                                {
                                    Logging.logger().severe("Error minimizing/maximizing WWJ window: " + ie.getMessage());
                                    frame.setVisible(true);
                                    break;
                                }
                            }
                        }
                    });
                }
            }
            Logging.logger().info(logMsg + " | Current Memory Used: " + currentMemUsed + "mb | Max Memory Used: " + maxMemUsed + "mb");
        }
    }

    private void moveToDefaut(long timeToIterate)
    {
        Position center = new Position(Angle.fromDegreesLatitude(22.0), Angle.fromDegreesLongitude(-47.0), 15000000); //randPos.getElevation()); //0);
        nextVSAnimator(timeToIterate, center, 1);
    }

    private void moveToandAlter(long timeToIterate, Position center, int iterations )
    {
        statusLbl.setText("status: Moving to new location");

        nextVSAnimator(timeToIterate, center, iterations);
    }

    private void nextVSAnimator(long timeToIterate, Position center, int num)
    {
        BasicOrbitView view = (BasicOrbitView) wwd.getView();

        switch ( num % 10)
        {
            case 0:
                view.addEyePositionAnimator(
                    timeToIterate,view.getCenterPosition(), center);
                break;
            case 2:
                view.addPanToAnimator(
                    view.getCenterPosition(), center,
                    view.getHeading(), Angle.fromDegrees(360 * Math.random()),
                    view.getPitch(), Angle.fromDegrees(0.0),
                    view.getZoom(), center.getElevation(),
                    timeToIterate, true);
                break;
            case 4:
                view.addPanToAnimator(
                    view.getCenterPosition(), center,
                    view.getHeading(), Angle.fromDegrees(0.0),
                    view.getPitch(), Angle.fromDegrees(70 * Math.random()),
                    view.getZoom(), center.getElevation(),
                    timeToIterate, true);
                break;
            case 6:
                view.addPanToAnimator(
                    view.getCenterPosition(), center,
                    view.getHeading(), Angle.fromDegrees(360 * Math.random()),
                    view.getPitch(), Angle.fromDegrees(70 * Math.random()),
                    view.getZoom(), center.getElevation(),
                    timeToIterate, true);
                break;
            default:
                view.addPanToAnimator(
                    view.getCenterPosition(), center,
                    view.getHeading(), Angle.fromDegrees(0.0),
                    view.getPitch(), Angle.fromDegrees(0.0),
                    view.getZoom(), center.getElevation(),
                    timeToIterate, true);
                break;
        }

    }

    private void alterAnimator(long timeToMove)
    {
        BasicOrbitView view = (BasicOrbitView) wwd.getView();

        double heading = 360 * Math.random();
        double pitch = 80 * Math.random();

        view.addPanToAnimator(
                            view.getCenterPosition(), view.getCenterPosition(),
                            view.getHeading(), Angle.fromDegrees(heading),
                            Angle.fromDegrees(0), Angle.fromDegrees(pitch),
                            view.getZoom(), view.getZoom(),
                            timeToMove, true);
    }

    private static Position randomPosition(Globe globe)
    {
        double lat, lon;
        double elevation;
        double rand;
        do
        {
            rand = Math.random();
            lat = 140 * rand - 70;   
            rand = Math.random();
            lon = 360 * rand - 180;
            elevation = globe.getElevation(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon));
        } while (elevation < 1);

        return Position.fromDegrees(lat, lon, elevation);
    }


    public JPanel makeControlPanel()
    {
        JPanel controlPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        controlPanel.add(new JLabel("Started: " + new Date().toString()));
        controlPanel.add(totalMemLbl);
        controlPanel.add(freeMemLbl);
        controlPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        controlPanel.add(viewLbl);
        controlPanel.add(markerLbl);
        controlPanel.add(airspaceLbl);
        controlPanel.add(wmsLbl);
        controlPanel.add(numObjectsLbl);
        controlPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        controlPanel.add(roundsLbl);
        controlPanel.add(iterationsLbl);

        statusLbl.setForeground(Color.BLUE);
        controlPanel.add(statusLbl);
        controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                                                            new TitledBorder("Stress Test Statistics")));

        return controlPanel;
    }


    //inner class for reading iterator properties file
    //borrowed from gov.nasa.worldwind.Configuration
    private class StressTestConfiguration
    {
        private static final String DEFAULT_LOGGER_NAME = "gov.nasa.worldwind";
        private Properties properties= new Properties();

        public StressTestConfiguration(String propsFile)
        {
            readProperties(propsFile);
        }

        private void readProperties(String configFileName)
        {
            try
            {
                java.io.InputStream propsStream = null;
                File file = new File(configFileName);
                if (file.exists())
                {
                    try
                    {
                        propsStream = new FileInputStream(file);
                    }
                    catch (FileNotFoundException e)
                    {
                        String message = Logging.getMessage("Configuration.LocalConfigFileNotFound", configFileName);
                        Logging.logger().finest(message);
                    }
                }

                if (propsStream == null)
                {
                    propsStream = this.getClass().getResourceAsStream("/" + configFileName);
                }

                if (propsStream == null)
                {
                    Logging.logger().log(Level.WARNING, "Configuration.UnavailablePropsFile", configFileName);
                }

                if (propsStream != null)
                    this.properties.load(propsStream);
            }
            // Use a named logger in all the catch statements below to prevent Logger from calling back into
            // Configuration when this Configuration instance is not yet fully instantiated.
            catch (FileNotFoundException e)
            {
                Logging.logger(DEFAULT_LOGGER_NAME).log(Level.WARNING, "Configuration.UnavailablePropsFile",
                    configFileName);
            }
            catch (IOException e)
            {
                Logging.logger(DEFAULT_LOGGER_NAME).log(Level.SEVERE, "Configuration.ExceptionReadingPropsFile", e);
            }
            catch (Exception e)
            {
                Logging.logger(DEFAULT_LOGGER_NAME).log(Level.SEVERE, "Configuration.ExceptionReadingPropsFile", e);
            }
        }


        public synchronized Integer getIntegerValue(String key, Integer defaultValue)
        {
            Integer v = getIntegerValue(key);
            return v != null ? v : defaultValue;
        }


        public synchronized Integer getIntegerValue(String key)
        {
            String v = getStringValue(key);
            if (v == null)
                return null;

            try
            {
                return Integer.parseInt(v);
            }
            catch (NumberFormatException e)
            {
                Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
                return null;
            }
        }

        public String getStringValue(String key, String defaultValue)
        {
            String v = getStringValue(key);
            return v != null ? v : defaultValue;
        }


        public synchronized String getStringValue(String key)
        {
            return properties.getProperty(key);
        }

    }
}
