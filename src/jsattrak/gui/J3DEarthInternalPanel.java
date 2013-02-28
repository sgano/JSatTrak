/*
 * J3DEarthPanel.java
 * =====================================================================
 *   This file is part of JSatTrak.
 *
 *   Copyright 2007-2013 Shawn E. Gano
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =====================================================================
 *
 * Created on October 21, 2007, 8:15 PM
 */
package jsattrak.gui;

import gov.nasa.worldwind.Configuration;
import jsattrak.objects.GroundStation;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.examples.WMSLayersPanel;
import gov.nasa.worldwind.examples.sunlight.AtmosphereLayer;
import gov.nasa.worldwind.examples.sunlight.LensFlareLayer;
import gov.nasa.worldwind.examples.sunlight.RectangularNormalTessellator;
import gov.nasa.worldwind.examples.sunlight.SunPositionProvider;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Earth.CountryBoundariesLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3;
import gov.nasa.worldwind.layers.Earth.USGSTopographicMaps;
import gov.nasa.worldwind.layers.Earth.USGSUrbanAreaOrtho;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.Mercator.examples.OSMCycleMapLayer;
import gov.nasa.worldwind.layers.Mercator.examples.OSMMapnikLayer;
import gov.nasa.worldwind.layers.Mercator.examples.OSMMapnikTransparentLayer;
import gov.nasa.worldwind.layers.Mercator.examples.VirtualEarthLayer;
import gov.nasa.worldwind.layers.Mercator.examples.YahooMapsLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.BasicOrbitView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jsattrak.coverage.CoverageAnalyzer;
import jsattrak.objects.AbstractSatellite;
import jsattrak.utilities.ECEFModelRenderable;
import jsattrak.utilities.J3DEarthComponent;
import jsattrak.utilities.OrbitModelRenderable;
import name.gano.file.SaveImageFile;
import name.gano.swingx.fullscreen.ToggleFullscreen;
import name.gano.worldwind.WwjUtils;
import name.gano.worldwind.geom.ECIRadialGrid;
import name.gano.worldwind.layers.Earth.CoverageRenderableLayer;
import name.gano.worldwind.layers.Earth.ECEFRenderableLayer;
import name.gano.worldwind.layers.Earth.ECIRenderableLayer;
import name.gano.worldwind.layers.Earth.EcefTimeDepRenderableLayer;
import name.gano.worldwind.sunshader.CustomSunPositionProvider;
import name.gano.worldwind.view.AutoClipBasicOrbitView;
import name.gano.worldwind.view.BasicModelView3;
import name.gano.worldwind.view.BasicModelViewInputHandler3;

/**
 *
 * @author  Shawn
 */
public class J3DEarthInternalPanel extends javax.swing.JPanel implements J3DEarthComponent
{
    
    private WorldWindowGLJPanel wwd;
    StatusBar statusBar;
    JInternalFrame parent; // parent dialog
    ECIRenderableLayer eciLayer; // ECI layer for plotting in ECI coordinates
    ECEFRenderableLayer ecefLayer; // ECEF layer for plotting in ECEF coordinates
    EcefTimeDepRenderableLayer timeDepLayer;
    OrbitModelRenderable orbitModel; // renderable object for plotting
    ECEFModelRenderable ecefModel;
    // terrain profile layer
    TerrainProfileLayer terrainProfileLayer;
    
    CoverageRenderableLayer cel;
    
    private boolean viewModeECI = true; // view mode - ECI (true) or ECEF (false)
    
    // Web Map Servers
    private static final String[] servers = new String[]{
        "http://neowms.sci.gsfc.nasa.gov/wms/wms",
        "http://mapserver.flightgear.org/cgi-bin/landcover",
        "http://wms.jpl.nasa.gov/wms.cgi",
        "http://labs.metacarta.com/wms/vmap0",
        
    };
    
    
    // parent app
    private JSatTrak app; // used to force repaints
    // Star layer - for rotation if in ECI
    StarsLayer starsLayer;
    Hashtable<String, AbstractSatellite> satHash;
    Hashtable<String, GroundStation> gsHash;
    
    // options
    private String terrainProfileSat = "";
    private double terrainProfileLongSpan = 10.0;
    
    // view mode options
    private boolean modelViewMode = false; // default false
    private String modelViewString = ""; // to hold name of satellite to view when modelViewMode=true
    private double modelViewNearClip = 10000; // clipping pland for when in Model View mode
    private double modelViewFarClip = 5.0E7;
    private boolean smoothViewChanges = true; // for 3D view smoothing (only is set after model/earth view has been changed -needs to be fixed)
    // near/far clipping plane distances for 3d windows (can effect render speed and if full orbit is shown)
    private double farClippingPlaneDistOrbit = -1;//200000000d; // good out to geo, but slow for LEO, using AutoClipping plane view I made works better
    private double nearClippingPlaneDistOrbit = -1; // -1 value Means auto adjusting

    ViewControlsLayer viewControlsLayer;

    // sun shader
    private RectangularNormalTessellator tessellator;
    private LensFlareLayer lensFlareLayer;
    private AtmosphereLayer atmosphereLayer;
    private SunPositionProvider spp;
    private boolean sunShadingOn = false; // controls if sun shading is used

     // ECI grid
    private ECIRadialGrid eciRadialGrid = new ECIRadialGrid();
    
    /** Creates new form J3DEarthPanel
     * @param parent
     * @param satHash
     * @param gsHash
     * @param currentMJD
     * @param app 
     */
    public J3DEarthInternalPanel(JInternalFrame parent, Hashtable<String, AbstractSatellite> satHash, Hashtable<String, GroundStation> gsHash, double currentMJD, JSatTrak app)
    {
        this.parent = parent;
        this.app = app;
        this.satHash = satHash;
        this.gsHash = gsHash;
        
        initComponents();
        
         // set default initial view
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 38.0);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -90.0);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 1.913445320360136E7);
        Configuration.setValue(AVKey.INITIAL_HEADING, 0.0);
        Configuration.setValue(AVKey.INITIAL_PITCH, 0.0);

        // Use normal/shading tessellator
        // sun shading needs this
        Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, RectangularNormalTessellator.class.getName());

        // make a new instace from the shared wwj resource!
        wwd = new WorldWindowGLJPanel(app.getWwd());

        // add WWJ to panel
        //wwd = new WorldWindowGLJPanel(); // lightweight component
        wwd.setPreferredSize(new java.awt.Dimension(600, 400));
        this.add(wwd, java.awt.BorderLayout.CENTER);
        //wwd.setModel(new BasicModel());

        // turn network loading off -- this is a global switch
        //gov.nasa.worldwind.WorldWind.getNetworkStatus().setOfflineMode(true);
        
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        // m.setLayers(layerList);
        m.setShowWireframeExterior(false);
        m.setShowWireframeInterior(false);
        m.setShowTessellationBoundingVolumes(false);
        
        // add political boundary layer
        m.getLayers().add(new CountryBoundariesLayer());
        // MS  Virtual Earth imagery
        VirtualEarthLayer ve = new VirtualEarthLayer();
        ve.setEnabled(false);  // off by default
        m.getLayers().add(ve);
        // yahoo imagergy
        YahooMapsLayer ya = new YahooMapsLayer();
        ya.setEnabled(false);  // off by default
        m.getLayers().add(ya);
        // open maps
        OSMMapnikLayer ol = new OSMMapnikLayer();
        ol.setEnabled(false);  // off by default
        m.getLayers().add(ol);
        OSMCycleMapLayer  ol2 = new OSMCycleMapLayer();
        ol2.setEnabled(false);  // off by default
        m.getLayers().add(ol2);
        OSMMapnikTransparentLayer  ol3 = new OSMMapnikTransparentLayer();
        ol3.setEnabled(false);  // off by default
        m.getLayers().add(ol3);

        // Add view controls layer and select listener - New in WWJ V0.6
        viewControlsLayer = new ViewControlsLayer();
        viewControlsLayer.setLayout(AVKey.VERTICAL); // VOTD change from LAYOUT_VERTICAL (9/june/09)
        viewControlsLayer.setScale(6/10d);
        viewControlsLayer.setPosition(AVKey.SOUTHEAST); // put it on the right side
        viewControlsLayer.setLocationOffset( new Vec4(15,35,0,0));
        viewControlsLayer.setEnabled(true); // turn off by default
        m.getLayers().add(viewControlsLayer);
        //insertBeforeCompass(wwd, viewControlsLayer);
        //getLayerPanel().update(wwd);
        this.getWwd().addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
       
        // set default layer visabiliy
        for (Layer layer : m.getLayers())
        {
            if (layer instanceof TiledImageLayer)
            {
                ((TiledImageLayer) layer).setShowImageTileOutlines(false);
            }
            if (layer instanceof LandsatI3)
            {
                ((TiledImageLayer) layer).setDrawBoundingVolumes(false);
                ((TiledImageLayer) layer).setEnabled(false);
            }
            if (layer instanceof CompassLayer)
            {
                ((CompassLayer) layer).setShowTilt(true);
                ((CompassLayer) layer).setEnabled(false);
            }
            if (layer instanceof PlaceNameLayer)
            {
                ((PlaceNameLayer) layer).setEnabled(false); // off
            }
            if (layer instanceof WorldMapLayer)
            {
                ((WorldMapLayer) layer).setEnabled(false); // off
            }
            if (layer instanceof USGSUrbanAreaOrtho)
            {
                ((USGSUrbanAreaOrtho) layer).setEnabled(false); // off
            }
            // save star layer
            if (layer instanceof StarsLayer)
            {
                starsLayer = (StarsLayer) layer;
                
                // for now just enlarge radius by a factor of 10
                starsLayer.setRadius(starsLayer.getRadius()*10.0);
            }
            if(layer instanceof CountryBoundariesLayer)
            {
                ((CountryBoundariesLayer) layer).setEnabled(false); // off by default
            }
        } // for layers
        
        
        wwd.setModel(m);

        // add USGS topo layer
        USGSTopographicMaps topo = new USGSTopographicMaps();
        topo.setEnabled(false);
        WwjUtils.insertBeforePlacenames(getWwd(), topo);
        
        // Coverage Data Layer
        cel = new CoverageRenderableLayer(app.getCoverageAnalyzer());
        //cel.setEnabled(false); // off by default
        m.getLayers().add(cel); // add Layer

        // add EcefTimeDepRenderableLayer layer
        timeDepLayer = new EcefTimeDepRenderableLayer(currentMJD,app);
        m.getLayers().add(timeDepLayer);
        
        // add ECI Layer
        eciLayer = new ECIRenderableLayer(currentMJD); // create ECI layer
        orbitModel = new OrbitModelRenderable(satHash, wwd.getModel().getGlobe());
        eciLayer.addRenderable(orbitModel); // add renderable object
        eciLayer.setCurrentMJD(currentMJD); // update time again after adding renderable
        m.getLayers().add(eciLayer); // add ECI Layer
        eciLayer.addRenderable(eciRadialGrid); // add grid (optional if it is on or not)
        
        // add ECEF Layer
        ecefLayer = new ECEFRenderableLayer(); // create ECEF layer
        ecefModel = new ECEFModelRenderable(satHash, gsHash, wwd.getModel().getGlobe());
        ecefLayer.addRenderable(ecefModel); // add renderable object
        m.getLayers().add(ecefLayer); // add ECI Layer
        
        // add terrain profile layer
        terrainProfileLayer = new TerrainProfileLayer();
        m.getLayers().add(terrainProfileLayer); // add ECI Layer
        terrainProfileLayer.setEventSource(this.getWwd());

        // ini start and end - to avoid null calculations
        terrainProfileLayer.setStartLatLon(LatLon.fromDegrees(0.0, 0.0));
        terrainProfileLayer.setEndLatLon(LatLon.fromDegrees(50.0, 50.0));
        
        terrainProfileLayer.setFollow( TerrainProfileLayer.FOLLOW_NONE );
        terrainProfileLayer.setEnabled( false ); // off by default
        
        RenderableLayer latLongLinesLayer = createLatLongLinesLayer();
        latLongLinesLayer.setName("Lat/Long Lines");
        latLongLinesLayer.setEnabled(false);
        //insertBeforeCompass(this.getWwd(), latLongLinesLayer);
        m.getLayers().add(latLongLinesLayer); // add ECI Layer
        
        // add the WWJ status bar at the bottom
        statusBar = new StatusBar();
        this.add(statusBar, java.awt.BorderLayout.PAGE_END);
        statusBar.setEventSource(wwd);
        
        // if ECI update star field rotation
        // update star field based on date (any mode)
        //        if(viewModeECI)
        //        {
        starsLayer.setLongitudeOffset(Angle.fromDegrees(-eciLayer.getRotateECIdeg()));
        //        }

         // SUN SHADING -------------

        // set the sun provider to the shader
        spp = new CustomSunPositionProvider(app.getSun());

        // Replace sky gradient with this atmosphere layer when using sun shading
        this.atmosphereLayer = new AtmosphereLayer();

        // Add lens flare layer
        this.lensFlareLayer = LensFlareLayer.getPresetInstance(LensFlareLayer.PRESET_BOLD);
        this.getWwd().getModel().getLayers().add(this.lensFlareLayer);

        // Get tessellator
        this.tessellator = (RectangularNormalTessellator)getWwd().getModel().getGlobe().getTessellator();
        // set default colors for shading
        //this.tessellator.setLightColor(Color.WHITE); //this.colorButton.getBackground());
        //this.tessellator.setAmbientColor(Color.BLACK); //this.ambientButton.getBackground());
        this.tessellator.setAmbientColor(new Color(0.50f, 0.50f, 0.50f));

        // Add position listener to update light direction relative to the eye
        getWwd().addPositionListener(new PositionListener()
        {
            Vec4 eyePoint;

            public void moved(PositionEvent event)
            {
                if(eyePoint == null || eyePoint.distanceTo3(getWwd().getView().getEyePoint()) > 1000)
                {
                    update(true);
                    eyePoint = getWwd().getView().getEyePoint();
                }
            }
        });

        setSunShadingOn(true); // enable sun shading by default
        // END Sun Shading -------------

        // correct clipping plane -- so entire orbits are shown - maybe make variable?
        setupView(); // setup needed viewing specs and use of AutoClipBasicOrbitView
  
    } // constructor

    // Update worldwind wun shading
    private void update(boolean redraw)
    {
        if(sunShadingOn) //this.enableCheckBox.isSelected())
        {
            // Compute Sun position according to current date and time
            LatLon sunPos = spp.getPosition();
            Vec4 sun = getWwd().getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();

            Vec4 light = sun.getNegative3();
            this.tessellator.setLightDirection(light);
            this.lensFlareLayer.setSunDirection(sun);
            this.atmosphereLayer.setSunDirection(sun);

            // Redraw if needed
            if(redraw)
            {
                this.getWwd().redraw();
            }
        } // if sun Shading

    } // update - for sun shading

    public void setSunShadingOn(boolean useSunShading)
    {
        if(useSunShading == sunShadingOn)
        {
            return; // nothing to do
        }

        sunShadingOn = useSunShading;

        if(sunShadingOn)
        {
            // enable shading - use special atmosphere
            for(int i = 0; i < this.getWwd().getModel().getLayers().size(); i++)
            {
                Layer l = this.getWwd().getModel().getLayers().get(i);
                if(l instanceof SkyGradientLayer)
                {
                    this.getWwd().getModel().getLayers().set(i, this.atmosphereLayer);
                }
            }
        }
        else
        {
            // disable shading
            // Turn off lighting
            this.tessellator.setLightDirection(null);
            this.lensFlareLayer.setSunDirection(null);
            this.atmosphereLayer.setSunDirection(null);

            // use standard atmosphere
            for(int i = 0; i < this.getWwd().getModel().getLayers().size(); i++)
            {
                Layer l = this.getWwd().getModel().getLayers().get(i);
                if(l instanceof AtmosphereLayer)
                {
                    this.getWwd().getModel().getLayers().set(i, new SkyGradientLayer());
                }
            }

        } // if/else shading

        this.update(true); // redraw
    } // setSunShadingOn

    public boolean isSunShadingOn()
    {
        return sunShadingOn;
    }

    /**
     * set the ambient light level for sun shading
     * @param level 0-100
     */
    public void setAmbientLightLevel(int level)
    {
        this.tessellator.setAmbientColor(new Color(level/100.0f,level/100.0f,level/100.0f) );

        this.update(true);
    }

    /**
     *
     * @return ambient light level 0-100
     */
    public int getAmbientLightLevel()
    {
        return (int) Math.round( 100.0*this.tessellator.getAmbientColor().getRed()/255.0 );
    }

    /**
     * if lens flair rendering is enabeled
     * @return
     */
    public boolean isLensFlareEnabled()
    {
        return lensFlareLayer.isEnabled();
    }

    /**
     * Set if the lens flair and sun are rendered if sun shading is enabeled
     * @param enabled
     */
    public void setLensFlare(boolean enabled)
    {
        lensFlareLayer.setEnabled(enabled);
    }
    
    private RenderableLayer createLatLongLinesLayer()
    {
        RenderableLayer shapeLayer = new RenderableLayer();

            // Generate meridians
            ArrayList<Position> positions = new ArrayList<Position>(3);
            double height = 30e3; // 10e3 default
            for (double lon = -180; lon < 180; lon += 10)
            {
                Angle longitude = Angle.fromDegrees(lon);
                positions.clear();
                positions.add(new Position(Angle.NEG90, longitude, height));
                positions.add(new Position(Angle.ZERO, longitude, height));
                positions.add(new Position(Angle.POS90, longitude, height));
                Polyline polyline = new Polyline(positions);
                polyline.setFollowTerrain(false);
                polyline.setNumSubsegments(30);
                
                if(lon == -180 || lon == 0)
                {
                    polyline.setColor(new Color(1f, 1f, 0f, 0.5f)); // yellow
                }
                else
                {
                    polyline.setColor(new Color(1f, 1f, 1f, 0.5f));
                }
                
                shapeLayer.addRenderable(polyline);
            }

            // Generate parallels
            for (double lat = -80; lat < 90; lat += 10)
            {
                Angle latitude = Angle.fromDegrees(lat);
                positions.clear();
                positions.add(new Position(latitude, Angle.NEG180, height));
                positions.add(new Position(latitude, Angle.ZERO, height));
                positions.add(new Position(latitude, Angle.POS180, height));
                Polyline polyline = new Polyline(positions);
                polyline.setPathType(Polyline.LINEAR);
                polyline.setFollowTerrain(false);
                polyline.setNumSubsegments(30);
                
                if(lat == 0)
                {
                    polyline.setColor(new Color(1f, 1f, 0f, 0.5f));
                }
                else
                {
                    polyline.setColor(new Color(1f, 1f, 1f, 0.5f));
                }
                
                shapeLayer.addRenderable(polyline);
            }

            return shapeLayer;
    }
    
    public void setFocusWWJ()
    {
        // MUST USE SO THAT KEYBOARD COMMANDS WORK FOR WWJ
        // MUST BE CALLED AFTER ALL OF GUI IS MADE TOO (it seems)
        wwd.requestFocusInWindow();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        viewPropButton = new javax.swing.JButton();
        globeLayersButton = new javax.swing.JButton();
        wmsButton = new javax.swing.JButton();
        terrainProfileButton = new javax.swing.JButton();
        screenCaptureButton = new javax.swing.JButton();
        genMovieButton = new javax.swing.JButton();
        fullScreenButton = new javax.swing.JButton();
        wwjPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        viewPropButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/eye24.png"))); // NOI18N
        viewPropButton.setToolTipText("View Properties");
        viewPropButton.setFocusable(false);
        viewPropButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewPropButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        viewPropButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewPropButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(viewPropButton);

        globeLayersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/applications-internet.png"))); // NOI18N
        globeLayersButton.setToolTipText("Globe Layers");
        globeLayersButton.setFocusable(false);
        globeLayersButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        globeLayersButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        globeLayersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                globeLayersButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(globeLayersButton);

        wmsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/folder-remote.png"))); // NOI18N
        wmsButton.setToolTipText("Manage Web Map Services");
        wmsButton.setFocusable(false);
        wmsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        wmsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        wmsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wmsButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(wmsButton);

        terrainProfileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/stock_chart-autoformat.png"))); // NOI18N
        terrainProfileButton.setToolTipText("Terrain Profiler");
        terrainProfileButton.setFocusable(false);
        terrainProfileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        terrainProfileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        terrainProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                terrainProfileButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(terrainProfileButton);

        screenCaptureButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/applets-screenshooter22.png"))); // NOI18N
        screenCaptureButton.setToolTipText("Screenshot");
        screenCaptureButton.setFocusable(false);
        screenCaptureButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        screenCaptureButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        screenCaptureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenCaptureButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(screenCaptureButton);

        genMovieButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/applications-multimedia.png"))); // NOI18N
        genMovieButton.setToolTipText("Create Movie");
        genMovieButton.setFocusable(false);
        genMovieButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        genMovieButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        genMovieButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genMovieButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(genMovieButton);

        fullScreenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/view-fullscreen.png"))); // NOI18N
        fullScreenButton.setToolTipText("Fullscreen Mode - press Esc to exit");
        fullScreenButton.setFocusable(false);
        fullScreenButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        fullScreenButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        fullScreenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullScreenButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(fullScreenButton);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        wwjPanel.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout wwjPanelLayout = new javax.swing.GroupLayout(wwjPanel);
        wwjPanel.setLayout(wwjPanelLayout);
        wwjPanelLayout.setHorizontalGroup(
            wwjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        wwjPanelLayout.setVerticalGroup(
            wwjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 252, Short.MAX_VALUE)
        );

        add(wwjPanel, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(100, 15));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        add(jPanel2, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    private void globeLayersButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_globeLayersButtonActionPerformed
    {//GEN-HEADEREND:event_globeLayersButtonActionPerformed
        // create globe layers dialog
        String windowName = "Globe Layers";
        
        //JDialog iframe = new JDialog(parent, windowName, false); // parent, title, modal
        JDialog iframe = new JDialog(app, windowName, false); // parent, title, modal
        
        //iframe.setContentPane(newPanel);
        Container cp = iframe.getContentPane();
        
        // get layers on Globe
        LayerList layerList = wwd.getModel().getLayers();
        
        // create panel of layers check boxes
        JPanel westContainer = new JPanel(new BorderLayout());
        {
            JPanel westPanel = new JPanel(new GridLayout(0, 1, 0, 10));
            westPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
            {
                JPanel layersPanel = new JPanel(new GridLayout(0, 1, 0, layerList.size()));
                layersPanel.setBorder(new TitledBorder("Layers"));
                for (Layer currentLayer : layerList)
                {
                    LayerAction la = new LayerAction(currentLayer, currentLayer.isEnabled(), wwd);
                    JCheckBox jcb = new JCheckBox(la);
                    jcb.setSelected(la.selected);
                    layersPanel.add(jcb);
                }
                westPanel.add(layersPanel);
                westContainer.add(westPanel, BorderLayout.NORTH);
            }
        }
        
        // add layer list to model (for listening)
        wwd.getModel().setLayers(layerList);
        
        // make scroll pane
        JScrollPane jsp = new JScrollPane(westContainer);
        //jsp.add(westContainer);
        // add to dialog
        cp.add(jsp);
        
        iframe.setSize(200+50, 350+40);
        
        Point p = this.getWwdLocationOnScreen();
        iframe.setLocation(p.x + 15, p.y + 15);

        setLookandFeel(iframe);
        
        iframe.setVisible(true);
        
    }//GEN-LAST:event_globeLayersButtonActionPerformed
    private int previousTabIndex = 0;
    
    private void wmsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_wmsButtonActionPerformed
    {//GEN-HEADEREND:event_wmsButtonActionPerformed
        // create a dialog for all the web map services
        
        // create tabbed pane to add to Dialog
        //JTabbedPane tabbedPane;
        
        final JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.add(new JPanel());
        tabbedPane.setTitleAt(0, "+");
        tabbedPane.addChangeListener(new ChangeListener()
        {
            
            public void stateChanged(ChangeEvent changeEvent)
            {
                if (tabbedPane.getSelectedIndex() != 0)
                {
                    previousTabIndex = tabbedPane.getSelectedIndex();
                    return;
                }
                
                String server = JOptionPane.showInputDialog("Enter wms server URL");
                if (server == null || server.length() < 1)
                {
                    tabbedPane.setSelectedIndex(previousTabIndex);
                    return;
                }
                
                // Respond by adding a new WMSLayerPanel to the tabbed pane.
                if (addWMSTab(tabbedPane.getTabCount(), server.trim(), tabbedPane) != null)
                {
                    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                }
            }
        });
        
        // Create a tab for each server and add it to the tabbed panel.
        for (int i = 0; i < servers.length; i++)
        {
            addWMSTab(i + 1, servers[i], tabbedPane); // i+1 to place all server tabs to the right of the Add Server tab
        }
        
        // Display the first server pane by default.
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() > 0 ? 1 : 0);
        previousTabIndex = tabbedPane.getSelectedIndex();
        
        // create and open dialog
        String windowName = "Web Map Services";
        
        JDialog iframe = new JDialog(app, windowName, false); // parent, title, modal
        
        //iframe.setContentPane(newPanel);
        Container cp = iframe.getContentPane();
        
        // add to dialog
        cp.add(tabbedPane);
        
        iframe.setSize(480, 350);
        
        Point p = parent.getLocation();
        iframe.setLocation(p.x + 15, p.y + 15);

        setLookandFeel(iframe);
        
        iframe.setVisible(true);
        
        
    }//GEN-LAST:event_wmsButtonActionPerformed
    
    private void screenCaptureButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_screenCaptureButtonActionPerformed
    {//GEN-HEADEREND:event_screenCaptureButtonActionPerformed
        createScreenCapture();
    }//GEN-LAST:event_screenCaptureButtonActionPerformed
    
    private void viewPropButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewPropButtonActionPerformed
    {//GEN-HEADEREND:event_viewPropButtonActionPerformed
        // create create Sat Settings panel
        JThreeDViewPropPanel newPanel = new JThreeDViewPropPanel(app, this, wwd);
        
        //String windowName = prop.getName().trim() + " - Settings"; // set name - trim excess spaces
        String windowName = "3D View Settings"; // set name - trim excess spaces
        
        // create new internal frame window
        JDialog iframe = new JDialog(app, windowName, false);
        
        iframe.setContentPane(newPanel); // set contents pane
        iframe.setSize(220+330, 260+65+185); // set size w,h
        
        Point p = this.getLocationOnScreen();
        iframe.setLocation(p.x + 15, p.y + 55);
        
        newPanel.setParentDialog(iframe); // save parent for closing
        
        setLookandFeel(iframe);

        iframe.setVisible(true);
        
        
    }//GEN-LAST:event_viewPropButtonActionPerformed
    
    private void setLookandFeel(JDialog iframe)
    {
        // set icon
        iframe.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));
        ////// SEG 23 March 2009 ///////////////////////////////
        // Check look and feel to see if this JDialog should have a deocrated window
        boolean canBeDecoratedByLAF = UIManager.getLookAndFeel().getSupportsWindowDecorations();
        if(canBeDecoratedByLAF != iframe.isUndecorated())
        {
            //boolean wasVisible = iframe.isVisible();
            //iframe.setVisible(false);
            iframe.dispose();
            if(!canBeDecoratedByLAF) //|| wasOriginallyDecoratedByOS
            {
                // see the java docs under the method
                // JFrame.setDefaultLookAndFeelDecorated(boolean
                // value) for description of these 2 lines:
                iframe.setUndecorated(false);
                iframe.getRootPane().setWindowDecorationStyle(JRootPane.NONE);

            }
            else
            {
                iframe.setUndecorated(true);
                iframe.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
            }
        }
        ///////////////////////////////// window decoration check
    } // setLookandFeel

    private void genMovieButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_genMovieButtonActionPerformed
    {//GEN-HEADEREND:event_genMovieButtonActionPerformed
        JCreateMovieDialog panel = new JCreateMovieDialog(app, false, this, app);
        Point p = this.getLocationOnScreen();
        panel.setLocation(p.x + 15, p.y + 55);
        setLookandFeel(panel);
        panel.setVisible(true);
    }//GEN-LAST:event_genMovieButtonActionPerformed
    
    private void terrainProfileButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_terrainProfileButtonActionPerformed
    {//GEN-HEADEREND:event_terrainProfileButtonActionPerformed
        JTerrainProfileDialog panel = new JTerrainProfileDialog(app, false, app, this);
        Point p = this.getLocationOnScreen();
        panel.setLocation(p.x + 15, p.y + 55);
        setLookandFeel(panel);
        panel.setVisible(true);
    }//GEN-LAST:event_terrainProfileButtonActionPerformed

private void fullScreenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullScreenButtonActionPerformed
    //Get the default graphics configuration from the graphics environment
    new ToggleFullscreen(GraphicsEnvironment.getLocalGraphicsEnvironment()
	.getDefaultScreenDevice(),wwd, this);
}//GEN-LAST:event_fullScreenButtonActionPerformed
    
    private WMSLayersPanel addWMSTab(int position, String server, JTabbedPane tabbedPane)
    {
        // Add a server to the tabbed dialog.
        try
        {
            WMSLayersPanel layersPanel = new WMSLayersPanel(wwd, server, new Dimension(200, 200)); // min size
            tabbedPane.add(layersPanel, BorderLayout.CENTER);
            String title = layersPanel.getServerDisplayString();
            tabbedPane.setTitleAt(position, title != null && title.length() > 0 ? title : server);
            
            // Add a listener to notice wms layer selections and tell the layer panel to reflect the new state.
            // this should only run when layer dialog is open?
            layersPanel.addPropertyChangeListener("LayersPanelUpdated", new PropertyChangeListener()
            {
                
                public void propertyChange(PropertyChangeEvent propertyChangeEvent)
                {
                    //AppFrame.this.getLayerPanel().update(AppFrame.this.getWwd());
                }
            });
            
            return layersPanel;
        }
        catch (URISyntaxException e)
        {
            JOptionPane.showMessageDialog(null, "Server URL is invalid", "Invalid Server URL",
                    JOptionPane.ERROR_MESSAGE);
            tabbedPane.setSelectedIndex(previousTabIndex);
            return null;
        }
    } // addWMStab
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton fullScreenButton;
    private javax.swing.JButton genMovieButton;
    private javax.swing.JButton globeLayersButton;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton screenCaptureButton;
    private javax.swing.JButton terrainProfileButton;
    private javax.swing.JButton viewPropButton;
    private javax.swing.JButton wmsButton;
    private javax.swing.JPanel wwjPanel;
    // End of variables declaration//GEN-END:variables
    
    public boolean isViewModeECI()
    {
        return viewModeECI;
    }
    
    public void setViewModeECI(boolean viewModeECI)
    {
        this.viewModeECI = viewModeECI;
        
        // take care of which view mode to use
        if(viewModeECI)
        {
            // update stars
            starsLayer.setLongitudeOffset(Angle.fromDegrees(-eciLayer.getRotateECIdeg()));
        }
        else
        {
            starsLayer.setLongitudeOffset(Angle.fromDegrees(0.0)); // reset to normal
        }
        
    }
    
    // parent app
    public JSatTrak getApp()
    {
        return app;
    }
    
    public WorldWindow getWwd()
    {
        return wwd;
    }
    
    public int getWwdWidth()
    {
        return wwd.getWidth();
    }
    
    public int getWwdHeight()
    {
        return wwd.getHeight();
    }
    
    public Point getWwdLocationOnScreen()
    {
        return wwd.getLocationOnScreen();
    }
    
    public String getTerrainProfileSat()
    {
        return terrainProfileSat;
    }
    
    public void setTerrainProfileSat(String terrainProfileSat)
    {
        this.terrainProfileSat = terrainProfileSat;
    }
    
    public double getTerrainProfileLongSpan()
    {
        return terrainProfileLongSpan;
    }
    
    public void setTerrainProfileLongSpan(double terrainProfileLongSpan)
    {
        this.terrainProfileLongSpan = terrainProfileLongSpan;
    }
    
    public boolean isModelViewMode()
    {
        return modelViewMode;
    }

    public void setModelViewMode(boolean viewMode)
    {
        // see if it is changed?
        if(viewMode == modelViewMode)
        {
            // no change, then do nothing
            return;
        }
        
        // save state
        this.modelViewMode = viewMode;
        
        // setup correct view
        setupView();
        
    } // setModelViewMode

    public String getModelViewString()
    {
        return modelViewString;
    }

    public void setModelViewString(String modelString)
    {
        // if changed model name and mode view active need to update model
        if(!modelViewString.equalsIgnoreCase(modelString) && modelViewMode)
        {
            this.modelViewString = modelString; // save first
            setupView();
        }
        
        this.modelViewString = modelString;
    }

    public double getModelViewNearClip()
    {
        return modelViewNearClip;
    }

    public void setModelViewNearClip(double modelViewNearClip)
    {
        this.modelViewNearClip = modelViewNearClip;
        
        if(this.isModelViewMode())
        {
            wwd.getView().setNearClipDistance(modelViewNearClip);
        }
    }

    public double getModelViewFarClip()
    {
        return modelViewFarClip;
    }

    public void setModelViewFarClip(double modelViewFarClip)
    {
        this.modelViewFarClip = modelViewFarClip;
        
        if(this.isModelViewMode())
        {
            wwd.getView().setFarClipDistance(modelViewFarClip);
        }
    }
    
    private void setupView()
    {
        if(modelViewMode == false)
        { // Earth View mode
            AutoClipBasicOrbitView bov = new AutoClipBasicOrbitView();
            wwd.setView(bov);
            
            // remove the rest of the old input handler  (does this need a remove of hover listener? - maybe it is now completely removed?)
            wwd.getInputHandler().setEventSource(null);
            
            AWTInputHandler awth = new AWTInputHandler();
            awth.setEventSource(wwd);
            wwd.setInputHandler(awth);
            awth.setSmoothViewChanges(smoothViewChanges); // FALSE MAKES THE VIEW FAST!! -- MIGHT WANT TO MAKE IT GUI Chooseable
                        
            // IF EARTH VIEW -- RESET CLIPPING PLANES BACK TO NORMAL SETTINGS!!!
            wwd.getView().setNearClipDistance(this.nearClippingPlaneDistOrbit);
            wwd.getView().setFarClipDistance(this.farClippingPlaneDistOrbit);
            
            // change class for inputHandler
            Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, 
                        AWTInputHandler.class.getName());

            // re-setup control layer handler
            this.getWwd().addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
            
        } // Earth View mode
        else
        { // Model View mode
            
            // TEST NEW VIEW -- TO MAKE WORK MUST TURN OFF ECI!
            this.setViewModeECI(false);

            if(!satHash.containsKey(modelViewString))
            {
                System.out.println("NO Current Satellite Selected, can't switch to Model Mode: " + modelViewString);
                return;
            }

            AbstractSatellite sat = satHash.get(modelViewString);

            BasicModelView3 bmv;
            if(wwd.getView() instanceof BasicOrbitView)
            {
                bmv = new BasicModelView3(((BasicOrbitView)wwd.getView()).getOrbitViewModel(), sat);
                //bmv = new BasicModelView3(sat);
            }
            else
            {
                bmv = new BasicModelView3(((BasicModelView3)wwd.getView()).getOrbitViewModel(), sat);
            }
            
            // remove the old hover listener -- depending on this instance of the input handler class type
            if( wwd.getInputHandler() instanceof AWTInputHandler)
            {
                ((AWTInputHandler) wwd.getInputHandler()).removeHoverSelectListener();
            }
            else if( wwd.getInputHandler() instanceof BasicModelViewInputHandler3)
            {
                ((BasicModelViewInputHandler3) wwd.getInputHandler()).removeHoverSelectListener();
            }
            
            // set view
            wwd.setView(bmv);

            // remove the rest of the old input handler
            wwd.getInputHandler().setEventSource(null);
             
            // add new input handler
            BasicModelViewInputHandler3 mih = new BasicModelViewInputHandler3();
            mih.setEventSource(wwd);
            wwd.setInputHandler(mih);
            
            // view smooth?
            mih.setSmoothViewChanges(smoothViewChanges); // FALSE MAKES THE VIEW FAST!!

            // settings for great closeups!
            wwd.getView().setNearClipDistance(modelViewNearClip);
            wwd.getView().setFarClipDistance(modelViewFarClip);
            bmv.setZoom(900000);
            bmv.setPitch(Angle.fromDegrees(45));
            
            // change class for inputHandler
            Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, 
                        BasicModelViewInputHandler3.class.getName());

            // re-setup control layer handler
            this.getWwd().addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
            
        } // model view mode
        
    } // setupView

    /**
     * @return the smoothViewChanges
     */
    public boolean isSmoothViewChanges()
    {
        return smoothViewChanges;
    }

    /**
     * @param smoothViewChanges the smoothViewChanges to set
     */
    @Override
    public void setSmoothViewChanges(boolean smoothViewChanges)
    {
        if(smoothViewChanges == this.smoothViewChanges)
        {
            return; //nothing to do
        }

        this.smoothViewChanges = smoothViewChanges;
        setupView();
    }

    /**
     * @return the eciRadialGrid
     */
    public ECIRadialGrid getEciRadialGrid()
    {
        return eciRadialGrid;
    }
    //    public void setWwd(WorldWindowGLCanvas wwd)
    //    {
    //        this.wwd = wwd;
    //    }
    
    // End of variables declaration
    // inner class for layers list
    private static class LayerAction extends AbstractAction
    {
        
        private Layer layer;
        private boolean selected;
        private WorldWindowGLJPanel wwd;
        
        public LayerAction(Layer layer, boolean selected, WorldWindowGLJPanel wwd)
        {
            super(layer.getName());
            this.layer = layer;
            this.selected = selected;
            this.layer.setEnabled(this.selected);
            this.wwd = wwd;
        }
        
        public void actionPerformed(ActionEvent actionEvent)
        {
            if (((JCheckBox) actionEvent.getSource()).isSelected())
            {
                this.layer.setEnabled(true);
            }
            else
            {
                this.layer.setEnabled(false);
            }
            
            wwd.repaint();
        }
    }  // LayerAction
    
    public void setMJD(double mjd)
    {
        
        if(viewModeECI)
        {
            // Hmm need to do something to keet the ECI view moving even after user interaction
            // seems to work after you click off globe after messing with it
            // this fixes the problem:
            wwd.getView().stopStateIterators();
            wwd.getView().stopMovement(); //seems to fix prop in v0.5
            
            // update rotation of view and Stars
            double theta0 = eciLayer.getRotateECIdeg();
            
            // UPDATE TIME
            eciLayer.setCurrentMJD(mjd);
            
            double thetaf = eciLayer.getRotateECIdeg();
            
            // move view
            
            //Quaternion q0 = ((BasicOrbitView) wwd.getView()).getRotation();
            //Vec4 vec = ((BasicOrbitView) wwd.getView()).getEyePoint();
            //Position pos = ((BasicOrbitView) wwd.getView()).getCurrentEyePosition();
            Position pos = ((BasicOrbitView) wwd.getView()).getCenterPosition(); // WORKS
            
            // amount to rotate the globe (degrees) around poles axis
            double rotateEarthDelta = thetaf - theta0; // deg

            //Quaternion q = Quaternion.fromRotationYPR(Angle.fromDegrees(0), Angle.fromDegrees(rotateEarthDelta), Angle.fromDegrees(0.0));
            // rotate the earth around z axis by rotateEarthDelta
            //double[][] rz = MathUtils.R_z(rotateEarthDelta*Math.PI/180);
            //double[] newEyePos = MathUtils.mult(rz, new double[] {vec.x,vec.y,vec.z});
//            Angle newLon = pos.getLongitude().addDegrees(-rotateEarthDelta);
//            Position newPos = new Position(pos.getLatitude(),newLon,pos.getElevation());
            //Position newPos = pos.add(new Position(Angle.fromDegrees(0),Angle.fromDegrees(-rotateEarthDelta),0.0));
            Position newPos = pos.add(new Position(Angle.fromDegrees(0),Angle.fromDegrees(-rotateEarthDelta),0.0)); // WORKS
            
            // rotation in 3D space is "added" to the quaternion by quaternion multiplication
//            try // try around it to prevent problems when running the simulation and then opening a new 3D window (this is called before the wwj is initalized)
//            {
                //((BasicOrbitView) wwd.getView()).setRotation(q0.multiply(q));
                //((BasicOrbitView) wwd.getView()).setEyePosition(newPos);
                ((BasicOrbitView) wwd.getView()).setCenterPosition(newPos); // WORKS  -- fixed 15  Jul 2008 SEG
//            }
//            catch(Exception e)
//            {
//                // do nothing, it will catch up next update
//            }
            
            // star layer
            starsLayer.setLongitudeOffset(Angle.fromDegrees(-eciLayer.getRotateECIdeg()));
            
        } // if ECI
        else
        {
            // EFEC - just update time
            eciLayer.setCurrentMJD(mjd);
            
            // star layer
            starsLayer.setLongitudeOffset(Angle.fromDegrees(-eciLayer.getRotateECIdeg()));
        }
        
        
        // if needed update terrain profile layer
        if (terrainProfileLayer.isEnabled())
        {
            try
            {
                AbstractSatellite sat = satHash.get(terrainProfileSat);
                double[] lla = sat.getLLA();
                terrainProfileLayer.setStartLatLon(LatLon.fromRadians(lla[0], lla[1] - terrainProfileLongSpan * Math.PI / 180.0));
                terrainProfileLayer.setEndLatLon(LatLon.fromRadians(lla[0], lla[1] + terrainProfileLongSpan * Math.PI / 180.0));
            }
            catch (Exception e)
            {
            }
        } // terrain profil layer

        // update layer that needs time updates
        timeDepLayer.setCurrentMJD(mjd);
        
    } // set MJD
    
    public void repaintWWJ()
    {
        //wwd.redraw(); // may not force repaint when it is slow to repaint (thus skiped)
        wwd.redrawNow(); //force it to happen now -- needed when plotting coverage data
    }
    
    // screen capture
    // routine to do the screen capture:
    public void createScreenCapture()
    {
        try
        {
            //capture the whole screen
            //BufferedImage screencapture = new Robot().createScreenCapture(
            //      new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()) );
            
            // just the framePanel	 // viewsTabbedPane frame3d
            Point pt = new Point();
            int width = 0;
            int height = 0;
            
            // Get location on screen / width / height
            pt = wwd.getLocationOnScreen();
            width = wwd.getWidth();
            height = wwd.getHeight();
            
            // not a possible size
            if (height <= 0 || width <= 0)
            {
                // no screen shot
                JOptionPane.showInternalMessageDialog(this, "A Screenshot was not possible - too small of size", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // full app screen shot
            //BufferedImage screencapture = new Robot().createScreenCapture(
            //			   new Rectangle( mainFrame.getX()+viewsTabbedPane.getX(), mainFrame.getY(),
            //					   viewsTabbedPane.getWidth(), mainFrame.getHeight() ) );
            // scree shot of just window
            BufferedImage screencapture = new Robot().createScreenCapture(
                    new Rectangle(pt.x, pt.y, width, height));
            
            
            //    	Create a file chooser
            final JFileChooser fc = new JFileChooser();
            jsattrak.utilities.CustomFileFilter pngFilter = new jsattrak.utilities.CustomFileFilter("png", "*.png");
            fc.addChoosableFileFilter(pngFilter);
            jsattrak.utilities.CustomFileFilter jpgFilter = new jsattrak.utilities.CustomFileFilter("jpg", "*.jpg");
            fc.addChoosableFileFilter(jpgFilter);
            
            fc.setDialogTitle("Save Screenshot");
            int returnVal = fc.showSaveDialog(this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File file = fc.getSelectedFile();
                
                String fileExtension = "png"; // default
                if (fc.getFileFilter() == pngFilter)
                {
                    fileExtension = "png";
                }
                if (fc.getFileFilter() == jpgFilter)
                {
                    fileExtension = "jpg";
                }
                
                String extension = getExtension(file);
                if (extension != null)
                {
                    fileExtension = extension;
                }
                else
                {
                    // append the extension
                    file = new File(file.getAbsolutePath() + "." + fileExtension);
                    //System.out.println("path="+file.getAbsolutePath());
                }
                
                //addMessagetoLog("Screenshot saved: " + file.getAbsolutePath());
                // save file
                //File file = new File("screencapture.png");
                //ImageIO.write(screencapture, fileExtension, file);
                //System.out.println("Saved!" + fileExtension );
                // new way---
                Exception e = SaveImageFile.saveImage(fileExtension, file, screencapture, 0.9f); // the last one is the compression quality (1=best)
                if(e != null)
                {
                    System.out.println("ERROR SCREEN CAPTURE:" + e.toString());
                    return;
                }
                
            }
            else
            {
                //log.append("Open command cancelled by user." + newline);
            }
            
            
        }
        catch (Exception e4)
        {
            System.out.println("ERROR SCREEN CAPTURE:" + e4.toString());
        }
    } // createScreenCapture
    
    public static String getExtension(File f)
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 && i < s.length() - 1)
        {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    } // getExtension
    
    public void closeWindow()
    {
        try
        {
            parent.dispose(); // could setClosed(true)
        }
        catch(Exception e)
        {}
    }
    
    public JInternalFrame getParentDialog()
    {
        return parent;
    }
    
    public String getDialogTitle()
    {
        return parent.getTitle();
    }
    
    public void setTerrainProfileEnabled(boolean enabled)
    {
        terrainProfileLayer.setEnabled(enabled);
        
        if (enabled) // try to update data
        {
            try
            {
                AbstractSatellite sat = satHash.get(terrainProfileSat);
                double[] lla = sat.getLLA();
                terrainProfileLayer.setStartLatLon(LatLon.fromRadians(lla[0], lla[1] - terrainProfileLongSpan * Math.PI / 180.0));
                terrainProfileLayer.setEndLatLon(LatLon.fromRadians(lla[0], lla[1] + terrainProfileLongSpan * Math.PI / 180.0));
            }
            catch (Exception e)
            {
            }
        } // terrain profil layer
    }
    
    public boolean getTerrainProfileEnabled()
    {
        return terrainProfileLayer.isEnabled();
    }
    
    public LayerList getLayerList()
    {
        return wwd.getModel().getLayers();
    }
    
    public void setOrbitFarClipDistance(double clipDist)
    {
        farClippingPlaneDistOrbit = clipDist;
        if(!this.isModelViewMode())
        {
            wwd.getView().setFarClipDistance(farClippingPlaneDistOrbit);
        }
    }

    public double getOrbitFarClipDistance()
    {
        return farClippingPlaneDistOrbit;
    }

    public void setOrbitNearClipDistance(double clipDist)
    {
        nearClippingPlaneDistOrbit = clipDist;
        if(!this.isModelViewMode())
        {
            wwd.getView().setNearClipDistance(nearClippingPlaneDistOrbit);
        }
    }

    public double getOrbitNearClipDistance()
    {
        return nearClippingPlaneDistOrbit;
    }
    
    public void updateCoverageLayerObject(CoverageAnalyzer ca)
    {
        cel.updateNewCoverageObject(ca);
    }
    
    public void resetWWJdisplay()
    {
        
        wwd.setSize(100, 100);
        this.add(wwd, java.awt.BorderLayout.CENTER);

        //  BUG if resume from Full screen mouse not over window, wwj takes up whole frame
  
        // repaint!
        super.repaint();
    }

    public EcefTimeDepRenderableLayer getEcefTimeDepRenderableLayer()
    {
        return timeDepLayer;
    } //getEcefTimeDepRenderableLayer
    
}
