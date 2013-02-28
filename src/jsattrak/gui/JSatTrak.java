/* 
 * ======= JSatTrak's main GUI interface================================
 * JSatTrak.java  - Shawn E. Gano,  shawn@gano.name
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
 * Created Using the NetBeans IDE
 * 
 * when building for distrbution include these directories:
 *  data/
 *  plugins/
 *  and for windows use launch4j to make .exe and include in the root: gluegen-rt.dll, jogl.dll, jogl_awt.dll, jogl_cg.dll
 * 
 * Using the license feature in netbeans: (use template file: license-JSatTrak.txt)
 * http://bigallan.blogspot.com/2008/02/using-license-functionality-in-netbeans.html
 * 
 * Similar Apps:
 * Nasa online tracker for shuttle/ISS:
 * http://spaceflight.nasa.gov/realdata/tracking/
 * 
 * J-Track:
 * http://science.nasa.gov/realtime/jtrack/Spacecraft.html
 * 
 * good site that has STS TLE for alll mission (past/preset/future planned)
 * http://spaceflight.nasa.gov/realdata/sightings/SSapplications/Post/JavaSSOP/orbit/SHUTTLE/SVPOST.html
 * 
 * real time data: (add this main link to website)
 * http://spaceflight.nasa.gov/realdata/
 * 
 * Created on July 23, 2007, 4:24 PM
 *
 *  Version 1.1 -- Added low precision position of sun (day/night) 26 Aug 2007
 *  Version 1.2 -- bug fix, uses MOD positions for lat/long calculations instead of J2000 (for satellites and sun)
 *  Version 1.3 -- Abilitiy to draw regions on 2d Map, fixed time rounding errors in time (new Time Classes), code refactored some.
 *  Version 2.0 -- Added 3D Window using WorldWind Java, movie creating tool (new icons)
 *  Version 2.1 -- Added Ground stations and tracking tool, tracking polar plot, pass predictions
 *              4Jan08 - added visibility - Visible/Radar-Night/Sun  -- Elevation constraints working in tracking tool
 *              5Jan  - connected dots in polar plot, go to pass in pass predictor, lat/long lines layer in 3D added
 *                    - scenario epoch setting, 
 *  Version 3.0 25Jan - Custom Sats, Mission Designer, Solver Loops   
 *              28Jan - Popogator stopping conditions (apogee/perigee)  
 *      (final) 9Feb - XML saving, general bug fixes updates, export of custom sat  
 *       3.0.1 - 13 Feb2008 - fixed decimal seperator issue, in SDP4  
 *       3.0.2 - 13 Feb2008 - fixed problem when missing only some of TLE data files (program freezes)
 *       3.0.3 - 14 Feb2008 - fixed issues of custom sat dialog not closing when unpropogated
 *
 *  Version 3.1.0 - 22 Mar2008 - added bean shell - plugins and command line interface and remote command server / client
 *                               from the command you can access jsattrak object to have access to all of its methods to change things on the fly
 *          3.1.1 - 20 April2008 - updated to swingx 0.9.2 compatibility -removed deleted features (table highlighters). Added Ground Station browser button to toolbar
 *          3.1.2 - 21 April08 - added polical boundaries layer for 3D globe
 *          3.2   - 9 May 2008 - Earth Coverage anylsis, added ability to create movies of any window or entire app, update to WWJ 0.5 (removed WWJ source), set custom clipping plane distances (sim properties)
 *          3.2.1 - (22 May 2008) - unsucessful try to remove dependency on JMF
 *                  ? view following sat (very preliminary) and 3D models?
 *             // bug - does CoverageAnalyzer save and open in 2d window correctly?? use dialog maybe to fix this?
 *  Version 3.5.0 - 7 August 2008 -- Added 3D models, added model view mode - fixed bug: 3d view in ECI reverted back to North up when time advanced (J3DEarthPanel.java, and internal)
 *                                  added full screen exclusive mode, GUI updates - double click on object opens properties, Custom Sat icons in new locations.
 *                                  added nimbus look and feel choice, if java6u10 or greater! (updated" jgoodies, swingx, xstream)
 *                                  Added ability to choose any 2D map image.
 *                                  Added 2D Earth Lights night image effect  
 *                                  Added FPS info (press f when a 2D window is active), added memory status bar (+plugin)  
 *                                  -- bug? 3d model only stays in full screen mode if two 3d windows are open before? - some quirks with the model view
 *          3.5.1  26 Sept 2008 -- Bug fix - repaint groundtrack when jumping to a time in the mission designer for a custom satellite
 *                                           fixed 3d cone, added get/set slices and stacks parameters, improved 2d window performance by>30% (removed most drawline calls), improved land mass drawing performance
 *                                           added tle-new.txt (new sats), sphere rednering still a bottleneck but reduced divions from 20 to 8, gained 25% performance
 *          3.6 13 Nov 2008    --  Added capibility to run JSatTrak script without starting GUI via passing a command line argument (plus script ability to save images of 2D plot and polar plots)
 *                                 Added app look and feel chooser to the help menu
 *                                  "C:\Documents and Settings\sgano\Desktop\JSatTrak\JSatTrak\noGUIscript.bsh"
 *                              KNOWN ISSUE:  Stored satellites reference Name this may not be unique (and many cases it isn't)! Need to store it by NORAD ID or if a custom sat some other ID
 *          3.6.1 22 Dec 2008  - Marvin joined team - started in on UI improvements drag of 2D zoomed in map, mouse wheel zoom (18 Dec 2008)
 *                    Better compression settings for JPG screenshots and movie creation (22 Dec 2008 - SEG)
 *          3.6.2 11 Jan 2009 -- added a menu to the satellite browser - to load custom satellite TLE data, and create a custom sat
 *          3.7    16 Jan 2009 (unreleased) -- Updates for helping oberservers (based on feed back from Dave Ortiz) - added TLE_user directory for custom tle files that are automatically loaded (with options for category specification)
 *                                                              - Updates to tracking form: polar plot-print,invert colors,limit to horizon, compass points, pass prediction-rize/set Az degrees/compass points, save as csv
 *          3.7.5 23 Mar 2009 -- Change 2D sun terminator resolution to 61 - 51 was reported by a user to cause some unwanted jumps as to which side was filled in, still has issues at times (even for higher res)
 *                               Integrated NASA World Wind Java V0.6 - plus a few new layers (like controls), redid some swing worker routines for thread safteyness
 *                               added substance look and feel (default Raven until Nimbus can work again),
 *                               fixed: saving of 2D coverage windows, 2D night light effects, and main app window location and size saved.
 *                               - 3D external windows now decorated to match look and feel (if avaiblilble)
 *                                   known issue: 3D internal window doesn't work with v0.6 (nimubs Look and feel only)
 *                                   known issue: webstart cannot save movies (no fix)
 *          3.7.6 24 Mar 2009 -- resize fix on 3D external window (in 3.7.5 they could only be made bigger) - set min size on globe panel (0,0) preferred size(50,50)
 *          3.7.7 2 April 2009 -- added Microsfot virtual earth layer/yahoo/open maps from WWJ Experimental code
 *          3.7.8 26 May 2009 - bug fix, close app by file->exit doesn't close process - fixed (bug found by Horst Meyerdierks - SGP4 author)
 *          4.0  21 June 2009 - added sun shading effects in latest WWJ verion of the day. See posts:
 *                                    http://forum.worldwindcentral.com/showthread.php?t=21021&highlight=sun+shading
 *                                    http://patmurris.blogspot.com/2009/04/sunlight-package-for-worldwind-java.html
 *                                  (VOTD - WWJ Broke COLOR OF 3D ORBIT TRACE - fixed - disabled 2D textures in OrbitModelRenderable and EFEFModelRenderable)
 *                                  - Increased precision of GeoFunctions.GeodeticLLA
 *                                  - CORRECTED error in treating SGP4 data as MOD not TEME of date! fixed (and transformation to J2000.0 - found stk prob uses 24 terms in nutation calc)
 *                                  - CORRECTED error in using MOD position for LLA instead of TEME of date (corrected)
 *                                  - added name.gano.astro.coordinates.J2kCoordinateConversion for better coordinate transformations! (phase out old transformations)
 *                                  - now SGP4 prop matches STK very closely (~1m for ISS) and HPROP was compared as well and preformed just as well.
 *                                  - added smooth changes to the 3D view options
 *                                  - coordinate system selection for sat info dialog (J2k, TEME, MOD, TOD)
 *                                  - REPLACED SDP4 (GPL license, see references.txt) with my own coversion of CSSI's SGP4 propagator (name.gano.astro.propogators.sgp4_cssi) (ver: 3 Nov 2008) (same one used in STK 9.0)
 *                                     Note: the above change of SGP4 props and other changes in this release make the saved files from older versions not compatible! Files should now be smaller! (why it gets promoted to v4)
 *                                  - RELICENCED JSatTrak as LGPL!
 *                                  - FILE FORMAT: changes to use zip compression! files are now much smaller!
 *          4.1  10 July 2009    - added AutoClipBasicOrbit view for dynamic clipping plane calculations (23 June)
 *                               - ECI grid, and gui options for grid and clipping planes (moved from sim properties to each 3D window)
 *                               - fixed bug (that surfaced in v4.0) for using the autorun in the coverage tool - needed to make SGP4SatData.java serializable (9 July 2009)
 *                               - Added read ephemeris from file to Custom Satellite Builder (also ability to delete "ini conditions" and add ini conditions nodes (always to top)) (10 Junly 2009)
 *                               - fixed bug where null values were no cloned properly in CustomSatellite - creating exceptions in OrbitModelRenderable/ECI rendering layer
 *          4.1.1 21 Aug 2009    -  Added the current look and feel to the save file / and open file
 *                               - 31 Aug - fixed addSeconds in Time to avoid max integer problems
 *          4.1.2 26 Sep 2009    - special apache license version released for NASA / GSFC called ILIADS (Integrated Lunar Information Architecture for Decision Support)
 *          4.1.3 30 Mar 2010    - now can use startup.bsh script if found in root directory on startup (run locally) to config the opening scenario (for Pat M. of WWJ)
 *          *******
 *          4.1.4 27 Feb 2013    - UPDATED TO Apache License, Version 2.0 (and started to manage src from GitHub)
 *          *******
 * 
 *               Need to update to latest WWJ build - new view architecture
 *
 *          4.2   - in progress  TODO - fixes and ideas from: Christopher Suski
 *                                      - in the 3D window the sun shading terminator line doesn't wrap around the earth properly when the camera is set to follow an object rather than the earth
 *                                      - a viewport from the ground station to the sat
 *                                      - if you can make room on the Ground station browser button should probably be one the object list window
 *                                      Master Sargent:
 *                                      - add launch vechile
 *                                      - add missile
 *                                      - Radar "fence" for ground stations
 *                                      Jason Liu:
 *                                      - FIX:  handle case when SGP4 Propogator fails with an error code!!!!  use this TLE past Sept 14 2009 as an example to test with -- logg error in console
 *                                          H-2B R/B
                                            1 35818U 09048B   09257.12951375  .35140387  12869-4  28262-3 0   201
                                            2 35818 051.5903 210.3110 0015017 310.1142 050.3875 16.51204315   549
 *
 *                              Ideas for next versions: (no particular order)
 *                                  - Vectors tool (make them seperate objects) axis, grids, lines, arrows (data providers)
 *                                  - DATA out! - Reports and graphs and exporting of data out of program
 *                                  - 3D "Earth Night Lights" mask / 1/2 sphere transparent night shell
 *                                  - Add Objects (Abstract Satellites) like Moon, Sun, Panets maybe (can set view to center on them as well)?
 *                                  - Satellite Orbit Lifetime Estimator
 *                                  - Launch Vehicle "Simple Ascent" (or missile - Ballistic) 
 *                                  - Orbit Dertermination tool
 *                                  - track aircraft? (does this mean I need to rename the app?)
 *                                  - swath planning
 *                                  - dyanmic changing of clipping planes when in different viewing modes -- and Exponential zooming! (see my jogl 3d soloar system)
 *        
 */
// notes: not good to use rk78 in a solver loop because direvatives inaccurate, because solution changes slightly near end.?
//
// NOTE: in beanshell jar (i.e. bsh-2.0b4.jar) need to replace desktop.bsh, with one included with this source -- it allows bean shell desktop to be reopened
//       THIS BEANSHELL problem may be fixed in a newer version as I wrote the authore about this and gave him my solution

// 
// run from linux: add to VM Options
// -Djava.library.path="/home/sgano/Desktop/sgano/Java/Libraries/"
//
// To make jogl display correctly in Ubuntu Linux:
// sudo apt-get install driconf
// run driconf and set "Enable S3TC"
//
// =====================
// changes to WWJ to make internal windows work:
// http://forum.worldwindcentral.com/showthread.php?t=12360&highlight=WorldWindowGLJPanel
//  in (WorldWindowGLAutoDrawable.initDrawable()) comment line: this.drawable.setAutoSwapBufferMode(fals e); 
// TerrainProfileLayer:  -- works now my bug reports and fixes are in the release!
// - Fix clipping plane problem:
// in AbstractView.java (gov.nasa.worldwind.view)  --  change the code to this:
// private static final double MIN_FAR_CLIP_DISTANCE = 10000000000d;
// THIS SHOULD BE FIXED in WWJ >v0.4.1 (see: http://issues.worldwind.arc.nasa.gov/browse/WWJ-9)
//  I STILL WANT LIGHTING ADDED TO WWJ - SO I CAN SIMULATE sun/dark side of earth!!
//  - and also views for model centric views... but I think that will work in the next release too.
//
// Jar signing commands:
// NOTE: to unsign a jar (such as worldwind.jar) - open jar in winzip and delete everything in the meta-inf directory
//        then resign -- fixed issue with "web start complaining about jars signed with multiple keys)
// /usr/java/jdk1.6.0_01/bin/keytool -genkey -keystore myKeys -alias jdc
// /usr/java/jdk1.6.0_01/bin/jarsigner -keystore myKeys JSatTrak.jar jdc

package jsattrak.gui;

import bsh.Interpreter;
import bsh.util.JConsole;
import bsh.util.NameCompletionTable;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import jsattrak.objects.GroundStation;
import jsattrak.about.AboutDialog;
import com.thoughtworks.xstream.XStream;
import commandclient.CommandClientGUI;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import jguiserver.GuiServer;
import jsattrak.coverage.CoverageAnalyzer;
import jsattrak.coverage.JSatTrakTimeDependent;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.CustomSatellite;
import jsattrak.objects.SatelliteTleSGP4;
import jsattrak.utilities.ConsoleDialog;
import jsattrak.utilities.CustomFileFilter;
import jsattrak.utilities.J2DEarthPanelSave;
import jsattrak.utilities.J3DEarthComponent;
import jsattrak.utilities.J3DEarthlPanelSave;
import jsattrak.utilities.JstSaveClass;
import jsattrak.utilities.LafChanger;
import jsattrak.utilities.SatPropertyPanelSave;
import jsattrak.utilities.TLE;
import name.gano.astro.bodies.Sun;
import name.gano.astro.time.Time;
import jsattrak.utilities.TLEDownloader;
import name.gano.file.FileTypeFilter;
import name.gano.file.SaveImageFile;

/**
 *
 * @author  Shawn E. Gano, shawn@gano.name
 */
public class JSatTrak extends javax.swing.JFrame implements InternalFrameListener, WindowListener, Serializable
{
    private String versionString = "Version 4.1.4  (7 Feb 2013)"; // Version of app
    
    // hastable to store all the statelites currently being processed
    private Hashtable<String,AbstractSatellite> satHash = new Hashtable<String,AbstractSatellite>();
    
    // hastable to store all the Ground Stations
    private Hashtable<String,GroundStation> gsHash = new Hashtable<String,GroundStation>();
    
    // Vector to store all the 2D windows -- so they can all be updated
    //Vector<J2DEarthPanel> twoDWindowVec = new Vector();
    Vector<J2DEarthPanel> twoDWindowVec = new Vector<J2DEarthPanel>();
    int twoDWindowCount = 0; // running count (for naming)
    // Satellite property vector to store all the windows (so they can be updated)
    Vector<SatPropertyPanel> satPropWindowVec = new Vector<SatPropertyPanel>(); 
    // 3D windows
    private Vector<J3DEarthInternalPanel> threeDInternalWindowVec = new Vector<J3DEarthInternalPanel>();
    int threeDInternalWindowCount = 0; // running count (for naming)
    private Vector<J3DEarthPanel> threeDWindowVec = new Vector<J3DEarthPanel>();
    int threeDWindowCount = 0; // running count (for naming)
    
    // Tracking Tool Dialogs
    private Vector<JTrackingPanel> trackingWindowVec = new Vector<JTrackingPanel>();
    
    // sat panel - show list of satellites
    JObjectListPanel objListPanel;
    
    // Sun object
    private Sun sun;
    
    // ----- TIME parameters --------------------
    // UTCG 
    //JulianDay currentJulianDate = new JulianDay(); // current sim or real time (Julian Date) 
    Time currentJulianDate = new Time(); // current sim or real time (Julian Date) 
    
    // animation parameters
    private Timer playTimer;
    private boolean stopHit = false;
    private int realTimeAnimationRefreshRateMs = 1000; // refresh rate for real time animation
    private int nonRealTimeAnimationRefreshRateMs = 50; // refresh rate for non-real time animation
    private int animationRefreshRateMs = nonRealTimeAnimationRefreshRateMs; // (current)Milliseconds ** this should be an option somewhere!! - determines CPU used in animation
    private double animationSimStepSeconds = 1.0; // dt in Days per animation step/time update
    int currentPlayDirection = 0; // 1= forward, -1=backward, =0-no animation step, but can update time (esentially a graphic ini or refresh)
    
    // these are listed in seconds
    double[] timeStepSpeeds = new double[] {0.0001,0.001,0.01,0.1,0.25,0.5,1.0,2.0,5.0,10.0,30.0,60.0,300.0,600.0,1800.0,3600.0,43200.0,86400.0,604800.0,2419200.0,31556926.0};
    private int currentTimeStepSpeedIndex = 11;
    
    // date formats for displaying and reading in
    private SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS z");
    private SimpleDateFormat dateformatShort1 = new SimpleDateFormat("dd MMM y H:m:s.S z");
    private SimpleDateFormat dateformatShort2 = new SimpleDateFormat("dd MMM y H:m:s z"); // no Milliseconds
        
    // store local time zone for printing
    TimeZone localTZ = TimeZone.getDefault();
    
    // save file
    private String fileSaveAs = null; // starts out null
    
    // boolean to see if 3D is working
    boolean threeDWorking = true; // assume it is true?
    
    // scenario epoch time settings
    private boolean epochTimeEqualsCurrentTime = true; // uses current time for scenario epoch (reset button)
    private Time scenarioEpochDate = new Time(); // scenario epoch if epochTimeisCurrentTime = false
    
    
    // icons for animations
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    Timer messageTimer;
    Timer busyIconTimer;
    
    // WWJ online/offline mode
    private boolean wwjOfflineMode = false;
    
    // console for displaying things written to system.out
    ConsoleDialog console;
    
    // interpreter for bean shell
    JConsole commandConsole = new JConsole();
    public Interpreter beanShellInterp = new Interpreter(commandConsole);
    
    // time dependent objects that should be update when time is updated -- NEED TO BE SAVED?
    Vector<JSatTrakTimeDependent> timeDependentObjects = new Vector<JSatTrakTimeDependent>();
    
     // coverage anaylzer tool (default null, until tool opened)
     private CoverageAnalyzer coverageAnalyzer;
     
     // WorldWindGLCanvas so all 3D windows can share resources like 3D models
     private WorldWindowGLCanvas wwd; // intially null - only created when needed
     
     // FPS variables
     private long lastFPSms;
     private double fpsAnimation;
     
     
    /** Creates new form JSatTrak */
    public JSatTrak()
    {
        boolean usingNimbus = false; // flag for updating nimbus

        // setup look and feel first        
        // if User has java 6u10 or greater that means NimbusLookAndFeel is supported!
        try
        {
            // TEMP UNTIL NIMBUS WORKS WITH INTERNAL WINDOWS
            String laf = "org.jvnet.substance.skin.Substance" + "Raven" + "LookAndFeel";
            LafChanger.changeLaf(this, laf);
//            for (J2DEarthPanel w : twoDWindowVec) //strange hack to make the map repaint correctly with the LAF (needed if done at the end)
//            {
//                w.setSize(w.getWidth() + 1, w.getHeight());
//                w.setSize(w.getWidth() - 1, w.getHeight());
//            }
        }
        catch(Exception ex2)
        {
            try
            {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); //
                usingNimbus = true;
            } catch (Exception ex1) // default using jgoodies looks plastic theme
            {
                PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                PlasticLookAndFeel.setTabStyle("Metal"); // makes tabes look much better

                try
                {
                    UIManager.setLookAndFeel(new PlasticLookAndFeel());
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
 
        
        
        // set locale for decimal seperator issue?
        //Locale.setDefault(Locale.CANADA_FRENCH); // reproduces TLE parsing error
        //Locale.setDefault(Locale.US); // forces it to work... but maybe not the best way (okay, fixed in SDP4 - set decimal seperator there)
        
        // setup window position and icon
        //super.setLocation(new java.awt.Point(50,50));  // set inital location
        //setLocationByPlatform(true); // better placement algorithms
        
        // set icon
        super.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/logo/JSatTrakLogo_32.png")));
        
        // ini GUI
        initComponents();
        
        // show shadow behind menus
        //Options.setPopupDropShadowEnabled(true);
        
        // setup look for toolbars:
       //jToolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        
        // make it scroll.. need to be smarter about this though!
        //mainDesktopPane.setPreferredSize(new Dimension(600,200));
        
        // create the console
        try
        {
            console = new ConsoleDialog(this,false);
        }
        catch (Exception e)
        {
            System.out.println("ERROR CREATING CONSOLE:" + e.toString());
        }
        
        
        // create Sun object
        sun = new Sun(currentJulianDate.getMJD());
        
        
        // create Satelite List Internal Frame (that can never be closed)
        objListPanel = new JObjectListPanel(satHash, gsHash, this);   
        
        // add it to the internal Frame
        satListInternalFrame.setContentPane(objListPanel);

//        // TEST
//        JObjectListInternalFrame t = new JObjectListInternalFrame(satHash,gsHash,this);
//        this.addInternalFrame(t);
        
        // by default open a new 2D window to start
        createNew2dWindow();
        
        // first call to update time to current time:
        currentJulianDate.update2CurrentTime(); //update();// = getCurrentJulianDate(); // ini time
        
        // just a little touch up -- remove the milliseconds from the time
        int mil = currentJulianDate.get(Time.MILLISECOND);
        currentJulianDate.add(Time.MILLISECOND,1000-mil); // remove the milliseconds (so it shows an even second)
        
        // set time string format
        currentJulianDate.setDateFormat(dateformat);
        scenarioEpochDate.setDateFormat(dateformat);
        
        updateTime(); // update plots
                
        // update gui with timestep
        updateTimeStepsDataGUI(); 
        
        // auto select local timezon button 
        localTimeZoneCheckBox.doClick();

        //this.setVisible(true); // only needed to debug if there is a problem below this point in constructor
        
        
                
        
        // TEST NOT WORKING!!!!!
        // test if 3d JOGL is working
//        try
//        {
//            GLCanvas canvas = new GLCanvas();
//        }
//        catch(Exception e)
//        {
//            System.out.println("3D ERROR!");
//            JOptionPane.showInternalMessageDialog(mainDesktopPane, "3D Error (Check your JOGL Installation):\n " + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
//
//            threeDWorking = false;
//            update3Dconfig();
//        }
        
        
        // create timers for animations and status messages
        int messageTimeout = 10000; // 10 sec
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        
        // set idle busy icons
        idleIcon = new javax.swing.ImageIcon(getClass().getResource("/icons/busyicons/idle-icon.png"));
        
        //busy-icon0.png
        for(int i=0;i<15;i++)
        {
           busyIcons[i] = new javax.swing.ImageIcon(getClass().getResource("/icons/busyicons/busy-icon"+i+".png")); 
        }
        
        // create timer for anumation, animation_rate is the first value
        busyIconTimer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        
        // try it out status messages
        setStatusMessage("Welcome to JSatTrak " + versionString + ", by Shawn E. Gano");
        
        
        // save root app to Bean Shell interperator
        try
        {
                        
            // name completion 
            final NameCompletionTable nct = new NameCompletionTable();
            nct.add(beanShellInterp.getNameSpace());
            commandConsole.setNameCompletion(nct);
        
            // start the interperator in its own thread
            final Thread thread = new Thread(beanShellInterp, "BeanShell");
            thread.setDaemon(true);
            thread.start();
            
            //new Thread( beanShellInterp ).start();
            beanShellInterp.set("jsattrak", this);
            
            // set the scope so it is in this object (no need to use jsattrak. all the time)
            // doesn't work this way?
            //beanShellInterp.eval("setNameSpace(jsattrak.namespace)");
            
            
            // set this so closing the bean shell Desktop doesn't close the whole app
            beanShellInterp.eval("bsh.system.shutdownOnExit = false;");
            
            // so bean shell returns values upon evaluation
            // doesn't seem to work
            //beanShellInterp.eval("show();");
            
        }
        catch(Exception e)
        {
            System.out.println("Error saving main object to bean shell:" + e.toString());
        }


        // Default sats in list: -- load if TLE data is local
        try
        {
            TLEDownloader tleDownloader = new TLEDownloader();
            if((new File(tleDownloader.getLocalPath()).exists()) && (new File(tleDownloader.getTleFilePath(0)).exists()))
            {
                // check for a startup script file and run it if it exists
                if( (new File("startup.bsh")).exists())
                {
                    // run startup script
                    runScript("startup.bsh");
                }
                else // default scene - ISS
                {
                    // default startup scene
                    addSat2ListByName("ISS (ZARYA)             ");
                    satHash.get("ISS (ZARYA)             ").setThreeDModelPath("isscomplete/iss_complete.3ds");// set default 3d model
                }
            }
        }
        catch(Exception e)
        {
            System.err.println("Error loading Local Default TLE Data");
        }
        
                
        // make the GUI large by default -- Nimbus look and feel needs it!
        this.setSize(this.getWidth(), 650);
        
        // display window in the center of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        Point p = this.getLocation();
        int x= (dim.width - this.getWidth())/2;
        int y = (dim.height- this.getHeight())/2;
        //System.out.println("h" + this.getHeight());
        this.setBounds(x, y, this.getWidth(), this.getHeight()+1);
        
        // check for plugin scripts
        checkAndInstallPlugins();
               
        
        // Debug for coverage module - for now create it and add it to 2D window
        if (false)
        {
            coverageAnalyzer = new CoverageAnalyzer(currentJulianDate); // start after first step
            //CoverageAnalyzer coverageAnalyzer = new CoverageAnalyzer(currentJulianDate,timeStepSpeeds[currentTimeStepSpeedIndex]/(24.0*60*60),satHash);
            twoDWindowVec.get(0).addRenderableObject(coverageAnalyzer); // add it to the panel
            coverageAnalyzer.addSatToCoverageAnaylsis("ISS (ZARYA)             ");
            //coverageAnalyzer.addSatToCoverageAnaylsis("test");
            timeDependentObjects.add(coverageAnalyzer); // add object to time updates
            twoDWindowVec.get(0).setShowLatLonLines(false);
            twoDWindowVec.get(0).setDrawSun(false);
            twoDWindowVec.get(0).setShowDateTime(false);
        }
        
        // fire resize function - to correct "dissapearing or transparent windows" issues
        int sizeJump = 1;
        this.setSize(this.getSize().width+sizeJump, this.getSize().height+sizeJump);
        this.setSize(this.getSize().width-sizeJump, this.getSize().height-sizeJump);
        
        // DEBUG - testing earht lights
        //this.twoDWindowVec.get(0).setShowEarthLightsMask(true);
        
        // for some reason nimbus has to be reapplied to work correctly
        if (usingNimbus)
        {
            try
            {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                SwingUtilities.updateComponentTreeUI(this); // apply look and feel over current L&F (otherwise nimbus shows up in correctly)
            } catch (Exception ex)
            {
                System.out.println("Sorry no Nimbus LookAndFeel needs java 6u10 or higher!");
            //ex.printStackTrace();
            }
        } // if using nimbus
                   
    } // constructor
        
    public void checkAndInstallPlugins()
    {
        // first clean out all current plugins displayed (in case it is refreshed)
        pluginMenu.removeAll();
        
        // see if plugin directory exisits
        File rootFile = new File("plugins");
        
        // make sure plugins exist and is a dirctory
        if (rootFile.exists() && rootFile.isDirectory())
        {
            System.out.println("Plugin Directory Exists:");

            // create action for plugin menu items
            AbstractAction pluginAction = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    runPluginAction(e);
                }
            };

            // for each file in plugin dir with extension .bsh
            for (String fileName : rootFile.list(new FileTypeFilter("bsh")))
            {
                String menuName = fileName.substring(0, fileName.length()-4);
                
                JMenuItem newPluginMenuItem = new JMenuItem(menuName);
                pluginMenu.add(newPluginMenuItem);
                System.out.println("Loading plugin: " + fileName);

                newPluginMenuItem.setAction(pluginAction); // add action
                newPluginMenuItem.setText(menuName); // reset name on menu item (since action over writes it)                
            }

        } // plugin loading
        
        // add seperator
        pluginMenu.add(new JSeparator());
        
        AbstractAction refreshPluginAction = new AbstractAction("Refresh Plugins List")
            {
                public void actionPerformed(ActionEvent e)
                {
                    checkAndInstallPlugins();
                }
            };
        pluginMenu.add(new JMenuItem(refreshPluginAction));
        
        // load in all plugins with right file extension
        
    } // checkAndInstallPlugins
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jToolBar = new javax.swing.JToolBar();
        saveButton = new javax.swing.JButton();
        openSatBrowserButton = new javax.swing.JButton();
        openGSbrowser = new javax.swing.JButton();
        resetTimeButton = new javax.swing.JButton();
        playBackButton = new javax.swing.JButton();
        stepBackButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        stepForwardTimeButton = new javax.swing.JButton();
        playButton = new javax.swing.JButton();
        incrementTimeStepButton = new javax.swing.JButton();
        timeStepLabel = new javax.swing.JLabel();
        decementTimeStepButton = new javax.swing.JButton();
        dateTextField = new javax.swing.JTextField();
        realTimeModeCheckBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        localTimeZoneCheckBox = new javax.swing.JCheckBox();
        zoomIntoggleButton = new javax.swing.JToggleButton();
        zoomOuttoggleButton = new javax.swing.JToggleButton();
        recenterToggleButton = new javax.swing.JToggleButton();
        screenShotButton = new javax.swing.JButton();
        createMovieButton = new javax.swing.JButton();
        toolbar3DWindowButton = new javax.swing.JButton();
        desktopPanel = new javax.swing.JPanel();
        desktopScrollPane = new javax.swing.JScrollPane();
        mainDesktopPane = new javax.swing.JDesktopPane();
        satListInternalFrame = new javax.swing.JInternalFrame();
        statusPanel = new javax.swing.JPanel();
        jSeparator7 = new javax.swing.JSeparator();
        statusLabel = new javax.swing.JLabel();
        statusProgressBar = new javax.swing.JProgressBar();
        statusAnimationLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        simMenu = new javax.swing.JMenu();
        simPropMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        realTimeMenuItem = new javax.swing.JMenuItem();
        nonRealTimeMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        utcTimeMenuItem = new javax.swing.JMenuItem();
        localTimeMenuItem = new javax.swing.JMenuItem();
        utilitiesMenu = new javax.swing.JMenu();
        tleLoaderMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        trackToolMenuItem = new javax.swing.JMenuItem();
        coverageMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        movieWholeAppMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        commandServerMenuItem = new javax.swing.JMenuItem();
        commandClientMenuItem = new javax.swing.JMenuItem();
        new3DwindowMenu = new javax.swing.JMenu();
        showSatBrowser = new javax.swing.JMenuItem();
        showGSBrowserMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        new2DWindowMenuItem = new javax.swing.JMenuItem();
        newInternal3DWindowMenuItem = new javax.swing.JMenuItem();
        newExternal3DWindowMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        twoDwindowPropMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        consoleMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        commandShellMenuItem = new javax.swing.JMenuItem();
        commandShellDesktopMenuItem = new javax.swing.JMenuItem();
        pluginMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        sysPropsMenuItem = new javax.swing.JMenuItem();
        lookFeelMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JSatTrak"); // NOI18N

        jPanel3.setLayout(new java.awt.BorderLayout());

        jToolBar.setRollover(true);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-save.png"))); // NOI18N
        saveButton.setToolTipText("Save"); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        jToolBar.add(saveButton);

        openSatBrowserButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/custom/sat_icon_tle.png"))); // NOI18N
        openSatBrowserButton.setToolTipText("Open Satellite Browser"); // NOI18N
        openSatBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openSatBrowserButtonActionPerformed(evt);
            }
        });
        jToolBar.add(openSatBrowserButton);

        openGSbrowser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/custom/groundStation_obj.png"))); // NOI18N
        openGSbrowser.setToolTipText("Open Ground Station Browser");
        openGSbrowser.setFocusable(false);
        openGSbrowser.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openGSbrowser.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openGSbrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openGSbrowserActionPerformed(evt);
            }
        });
        jToolBar.add(openGSbrowser);

        resetTimeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/media-seek-backward.png"))); // NOI18N
        resetTimeButton.setToolTipText("Reset To Current Time or Epoch"); // NOI18N
        resetTimeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetTimeButtonActionPerformed(evt);
            }
        });
        jToolBar.add(resetTimeButton);

        playBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/media-playback-playbackwards.png"))); // NOI18N
        playBackButton.setToolTipText("Play Backwards"); // NOI18N
        playBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playBackButtonActionPerformed(evt);
            }
        });
        jToolBar.add(playBackButton);

        stepBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/media-skip-backward.png"))); // NOI18N
        stepBackButton.setToolTipText("Step Backwards"); // NOI18N
        stepBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepBackButtonActionPerformed(evt);
            }
        });
        jToolBar.add(stepBackButton);

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/media-playback-stop.png"))); // NOI18N
        stopButton.setToolTipText("Stop Animation"); // NOI18N
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        jToolBar.add(stopButton);

        stepForwardTimeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/media-skip-forward.png"))); // NOI18N
        stepForwardTimeButton.setToolTipText("Step Forwards"); // NOI18N
        stepForwardTimeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepForwardTimeButtonActionPerformed(evt);
            }
        });
        jToolBar.add(stepForwardTimeButton);

        playButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/media-playback-start.png"))); // NOI18N
        playButton.setToolTipText("Play Forwards"); // NOI18N
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playButtonActionPerformed(evt);
            }
        });
        jToolBar.add(playButton);

        incrementTimeStepButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/view-sort-ascending.png"))); // NOI18N
        incrementTimeStepButton.setToolTipText("Faster"); // NOI18N
        incrementTimeStepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incrementTimeStepButtonActionPerformed(evt);
            }
        });
        jToolBar.add(incrementTimeStepButton);

        timeStepLabel.setForeground(new java.awt.Color(102, 102, 102));
        timeStepLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timeStepLabel.setText("10s"); // NOI18N
        timeStepLabel.setToolTipText("Time Step [sec]"); // NOI18N
        timeStepLabel.setMaximumSize(new java.awt.Dimension(40, 14));
        timeStepLabel.setMinimumSize(new java.awt.Dimension(40, 14));
        jToolBar.add(timeStepLabel);

        decementTimeStepButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/view-sort-descending.png"))); // NOI18N
        decementTimeStepButton.setToolTipText("Slower"); // NOI18N
        decementTimeStepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decementTimeStepButtonActionPerformed(evt);
            }
        });
        jToolBar.add(decementTimeStepButton);

        dateTextField.setText("Date/Time"); // NOI18N
        dateTextField.setToolTipText("UTC Date/Time"); // NOI18N
        dateTextField.setPreferredSize(new java.awt.Dimension(200, 27));
        dateTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateTextFieldActionPerformed(evt);
            }
        });
        jToolBar.add(dateTextField);

        realTimeModeCheckBox.setToolTipText("Real Time Mode"); // NOI18N
        realTimeModeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        realTimeModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                realTimeModeCheckBoxActionPerformed(evt);
            }
        });
        jToolBar.add(realTimeModeCheckBox);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/xclock.png"))); // NOI18N
        jLabel2.setToolTipText("Real Time Mode"); // NOI18N
        jToolBar.add(jLabel2);

        localTimeZoneCheckBox.setText("Local TZ"); // NOI18N
        localTimeZoneCheckBox.setToolTipText("Display Local Time Zone? (Otherwise UTC)"); // NOI18N
        localTimeZoneCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        localTimeZoneCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localTimeZoneCheckBoxActionPerformed(evt);
            }
        });
        jToolBar.add(localTimeZoneCheckBox);

        zoomIntoggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/zoom-in.png"))); // NOI18N
        zoomIntoggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomIntoggleButtonActionPerformed(evt);
            }
        });
        jToolBar.add(zoomIntoggleButton);

        zoomOuttoggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/zoom-out.png"))); // NOI18N
        zoomOuttoggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOuttoggleButtonActionPerformed(evt);
            }
        });
        jToolBar.add(zoomOuttoggleButton);

        recenterToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/zoom-best-fit.png"))); // NOI18N
        recenterToggleButton.setToolTipText("2D Recenter"); // NOI18N
        recenterToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recenterToggleButtonActionPerformed(evt);
            }
        });
        jToolBar.add(recenterToggleButton);

        screenShotButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/applets-screenshooter.png"))); // NOI18N
        screenShotButton.setToolTipText("Take Screen Shot of Current Window"); // NOI18N
        screenShotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenShotButtonActionPerformed(evt);
            }
        });
        jToolBar.add(screenShotButton);

        createMovieButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/applications-multimedia.png"))); // NOI18N
        createMovieButton.setToolTipText("Create Movie of Selected Window");
        createMovieButton.setFocusable(false);
        createMovieButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        createMovieButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        createMovieButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMovieButtonActionPerformed(evt);
            }
        });
        jToolBar.add(createMovieButton);

        toolbar3DWindowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/emblem-web.png"))); // NOI18N
        toolbar3DWindowButton.setToolTipText("New 3D Window (External)");
        toolbar3DWindowButton.setFocusable(false);
        toolbar3DWindowButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbar3DWindowButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar3DWindowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbar3DWindowButtonActionPerformed(evt);
            }
        });
        jToolBar.add(toolbar3DWindowButton);

        jPanel3.add(jToolBar, java.awt.BorderLayout.NORTH);

        desktopPanel.setAutoscrolls(true);

        desktopScrollPane.setAutoscrolls(true);

        mainDesktopPane.setDoubleBuffered(true);

        satListInternalFrame.setIconifiable(true);
        satListInternalFrame.setMaximizable(true);
        satListInternalFrame.setResizable(true);
        satListInternalFrame.setTitle("Object List"); // NOI18N
        satListInternalFrame.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png"))); // NOI18N
        satListInternalFrame.setVisible(true);

        javax.swing.GroupLayout satListInternalFrameLayout = new javax.swing.GroupLayout(satListInternalFrame.getContentPane());
        satListInternalFrame.getContentPane().setLayout(satListInternalFrameLayout);
        satListInternalFrameLayout.setHorizontalGroup(
            satListInternalFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 174, Short.MAX_VALUE)
        );
        satListInternalFrameLayout.setVerticalGroup(
            satListInternalFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 323, Short.MAX_VALUE)
        );

        satListInternalFrame.setBounds(610, 5, 190, 350);
        mainDesktopPane.add(satListInternalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);

        desktopScrollPane.setViewportView(mainDesktopPane);

        javax.swing.GroupLayout desktopPanelLayout = new javax.swing.GroupLayout(desktopPanel);
        desktopPanel.setLayout(desktopPanelLayout);
        desktopPanelLayout.setHorizontalGroup(
            desktopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktopScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
        );
        desktopPanelLayout.setVerticalGroup(
            desktopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktopScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
        );

        jPanel3.add(desktopPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        statusPanel.setPreferredSize(new java.awt.Dimension(0, 26));

        statusLabel.setForeground(new java.awt.Color(102, 102, 102));

        statusProgressBar.setVisible(false); // start out invisible
        statusProgressBar.setStringPainted(true);

        statusAnimationLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/busyicons/idle-icon.png"))); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
                    .addComponent(statusAnimationLabel)
                    .addComponent(statusProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);

        fileMenu.setText("File"); // NOI18N

        newMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/window-new.png"))); // NOI18N
        newMenuItem.setText("New Scenario"); // NOI18N
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newMenuItem);

        openMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/folder-open.png"))); // NOI18N
        openMenuItem.setText("Open..."); // NOI18N
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        closeMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/window-close.png"))); // NOI18N
        closeMenuItem.setText("Close"); // NOI18N
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-save.png"))); // NOI18N
        saveMenuItem.setText("Save"); // NOI18N
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-save-as.png"))); // NOI18N
        saveAsMenuItem.setText("Save As..."); // NOI18N
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(jSeparator2);

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/process-stop.png"))); // NOI18N
        exitMenuItem.setText("Exit"); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        simMenu.setText("Simulation"); // NOI18N

        simPropMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/control-center2.png"))); // NOI18N
        simPropMenuItem.setText("Properties"); // NOI18N
        simPropMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simPropMenuItemActionPerformed(evt);
            }
        });
        simMenu.add(simPropMenuItem);
        simMenu.add(jSeparator3);

        realTimeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        realTimeMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/xclock.png"))); // NOI18N
        realTimeMenuItem.setText("Real Time Mode"); // NOI18N
        realTimeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                realTimeMenuItemActionPerformed(evt);
            }
        });
        simMenu.add(realTimeMenuItem);

        nonRealTimeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        nonRealTimeMenuItem.setText("Non-Real Time Mode"); // NOI18N
        nonRealTimeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nonRealTimeMenuItemActionPerformed(evt);
            }
        });
        simMenu.add(nonRealTimeMenuItem);
        simMenu.add(jSeparator4);

        utcTimeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_MASK));
        utcTimeMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/appointment-soon.png"))); // NOI18N
        utcTimeMenuItem.setText("UTC Time Zone"); // NOI18N
        utcTimeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utcTimeMenuItemActionPerformed(evt);
            }
        });
        simMenu.add(utcTimeMenuItem);

        localTimeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        localTimeMenuItem.setText("Local Time Zone"); // NOI18N
        localTimeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localTimeMenuItemActionPerformed(evt);
            }
        });
        simMenu.add(localTimeMenuItem);

        jMenuBar1.add(simMenu);

        utilitiesMenu.setText("Utilities"); // NOI18N
        utilitiesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesMenuActionPerformed(evt);
            }
        });

        tleLoaderMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tango_blue_materia/network-transmit-receive.png"))); // NOI18N
        tleLoaderMenuItem.setText("Update Satellite TLE Data"); // NOI18N
        tleLoaderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tleLoaderMenuItemActionPerformed(evt);
            }
        });
        utilitiesMenu.add(tleLoaderMenuItem);
        utilitiesMenu.add(jSeparator6);

        trackToolMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        trackToolMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/network-wireless.png"))); // NOI18N
        trackToolMenuItem.setText("Tracking Tool"); // NOI18N
        trackToolMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackToolMenuItemActionPerformed(evt);
            }
        });
        utilitiesMenu.add(trackToolMenuItem);

        coverageMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/preferences-system-network-proxy.png"))); // NOI18N
        coverageMenuItem.setText("Coverage Analysis");
        coverageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                coverageMenuItemActionPerformed(evt);
            }
        });
        utilitiesMenu.add(coverageMenuItem);
        utilitiesMenu.add(jSeparator10);

        movieWholeAppMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/applications-multimedia.png"))); // NOI18N
        movieWholeAppMenuItem.setText("Create Movie of Application");
        movieWholeAppMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movieWholeAppMenuItemActionPerformed(evt);
            }
        });
        utilitiesMenu.add(movieWholeAppMenuItem);
        utilitiesMenu.add(jSeparator11);

        commandServerMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tango_blue_materia/network-receive.png"))); // NOI18N
        commandServerMenuItem.setText("Command Server");
        commandServerMenuItem.setToolTipText("TCP/IP command server");
        commandServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commandServerMenuItemActionPerformed(evt);
            }
        });
        utilitiesMenu.add(commandServerMenuItem);

        commandClientMenuItem.setText("Command Client");
        commandClientMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commandClientMenuItemActionPerformed(evt);
            }
        });
        utilitiesMenu.add(commandClientMenuItem);

        jMenuBar1.add(utilitiesMenu);

        new3DwindowMenu.setText("Windows"); // NOI18N
        new3DwindowMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new3DwindowMenuActionPerformed(evt);
            }
        });

        showSatBrowser.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        showSatBrowser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/custom/folder-open_sat.png"))); // NOI18N
        showSatBrowser.setText("Satellite Browser"); // NOI18N
        showSatBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSatBrowserActionPerformed(evt);
            }
        });
        new3DwindowMenu.add(showSatBrowser);

        showGSBrowserMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        showGSBrowserMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/custom/folder-open_gs.png"))); // NOI18N
        showGSBrowserMenuItem.setText("Ground Station Browser"); // NOI18N
        showGSBrowserMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showGSBrowserMenuItemActionPerformed(evt);
            }
        });
        new3DwindowMenu.add(showGSBrowserMenuItem);
        new3DwindowMenu.add(jSeparator5);

        new2DWindowMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/gnome-mime-image.png"))); // NOI18N
        new2DWindowMenuItem.setText("New 2D Window"); // NOI18N
        new2DWindowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new2DWindowMenuItemActionPerformed(evt);
            }
        });
        new3DwindowMenu.add(new2DWindowMenuItem);

        newInternal3DWindowMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/emblem-web.png"))); // NOI18N
        newInternal3DWindowMenuItem.setText("New 3D Window (Internal)"); // NOI18N
        newInternal3DWindowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newInternal3DWindowMenuItemActionPerformed(evt);
            }
        });
        new3DwindowMenu.add(newInternal3DWindowMenuItem);

        newExternal3DWindowMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/emblem-web.png"))); // NOI18N
        newExternal3DWindowMenuItem.setText("New 3D Window (External)"); // NOI18N
        newExternal3DWindowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newExternal3DWindowMenuItemActionPerformed(evt);
            }
        });
        new3DwindowMenu.add(newExternal3DWindowMenuItem);
        new3DwindowMenu.add(jSeparator1);

        twoDwindowPropMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/preferences-system.png"))); // NOI18N
        twoDwindowPropMenuItem.setText("2D Window Properties"); // NOI18N
        twoDwindowPropMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                twoDwindowPropMenuItemActionPerformed(evt);
            }
        });
        new3DwindowMenu.add(twoDwindowPropMenuItem);
        new3DwindowMenu.add(jSeparator8);

        consoleMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/logviewer.png"))); // NOI18N
        consoleMenuItem.setText("Log Console");
        consoleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consoleMenuItemActionPerformed(evt);
            }
        });
        new3DwindowMenu.add(consoleMenuItem);
        new3DwindowMenu.add(jSeparator9);

        commandShellMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tango_blue_materia/utilities-terminal.png"))); // NOI18N
        commandShellMenuItem.setText("Command Shell");
        commandShellMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commandShellMenuItemActionPerformed(evt);
            }
        });
        new3DwindowMenu.add(commandShellMenuItem);

        commandShellDesktopMenuItem.setText("Command Shell Desktop");
        commandShellDesktopMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commandShellDesktopMenuItemActionPerformed(evt);
            }
        });
        new3DwindowMenu.add(commandShellDesktopMenuItem);

        jMenuBar1.add(new3DwindowMenu);

        pluginMenu.setText("Plugins");
        jMenuBar1.add(pluginMenu);

        helpMenu.setText("Help"); // NOI18N

        sysPropsMenuItem.setText("System Properties..."); // NOI18N
        sysPropsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sysPropsMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(sysPropsMenuItem);

        lookFeelMenuItem.setText("App Look and Feel...");
        lookFeelMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lookFeelMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(lookFeelMenuItem);

        aboutMenuItem.setText("About..."); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sysPropsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sysPropsMenuItemActionPerformed
    {//GEN-HEADEREND:event_sysPropsMenuItemActionPerformed
        SystemPropertiesDialog spd = new SystemPropertiesDialog(this,false);
        setLookandFeel(spd);
        spd.setVisible(true);
    }//GEN-LAST:event_sysPropsMenuItemActionPerformed

    private void localTimeMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_localTimeMenuItemActionPerformed
    {//GEN-HEADEREND:event_localTimeMenuItemActionPerformed
        if(!localTimeZoneCheckBox.isSelected())
        {
            localTimeZoneCheckBox.doClick();
        }
    }//GEN-LAST:event_localTimeMenuItemActionPerformed

    private void utcTimeMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_utcTimeMenuItemActionPerformed
    {//GEN-HEADEREND:event_utcTimeMenuItemActionPerformed
        
        if(localTimeZoneCheckBox.isSelected())
        {
            localTimeZoneCheckBox.doClick();
        }
    }//GEN-LAST:event_utcTimeMenuItemActionPerformed

    private void nonRealTimeMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nonRealTimeMenuItemActionPerformed
    {//GEN-HEADEREND:event_nonRealTimeMenuItemActionPerformed
        if(realTimeModeCheckBox.isSelected())
        {
            realTimeModeCheckBox.doClick();
        }
    }//GEN-LAST:event_nonRealTimeMenuItemActionPerformed

    private void realTimeMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_realTimeMenuItemActionPerformed
    {//GEN-HEADEREND:event_realTimeMenuItemActionPerformed
        if(!realTimeModeCheckBox.isSelected())
        {
            realTimeModeCheckBox.doClick();
        }
    }//GEN-LAST:event_realTimeMenuItemActionPerformed

    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newMenuItemActionPerformed
    {//GEN-HEADEREND:event_newMenuItemActionPerformed
        
        // make sure you can close the current senario
        boolean closed = closeScenario(true); // make sure it is okay to close current scenario?
        
        if(!closed)
        {
            return;
        }
        
        // okay now create default layout:
        // by default open a new 2D window to start
        createNew2dWindow();
        
        // first call to update time to current time:
        currentJulianDate.update2CurrentTime();//.update();// = getCurrentJulianDate(); // ini time
        // set time string format
        currentJulianDate.setDateFormat(dateformat);
        
        updateTime(); // update plots
                
        // update gui with timestep
        updateTimeStepsDataGUI(); 
                
        // Default sats in list: -- load if TLE data is local
        TLEDownloader tleDownloader = new TLEDownloader();
        if( (new File(tleDownloader.getLocalPath()).exists()) && (new File(tleDownloader.getTleFilePath(0)).exists()) )
        {
            addSat2ListByName("ISS (ZARYA)             ");
        }
        
        // set default epoch time to be current time
        epochTimeEqualsCurrentTime = true;
        // other default settings
        realTimeAnimationRefreshRateMs = 1000; // refresh rate for real time animation
        nonRealTimeAnimationRefreshRateMs = 50;
    
    }//GEN-LAST:event_newMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitMenuItemActionPerformed
    {//GEN-HEADEREND:event_exitMenuItemActionPerformed
        // how to exit:
        this.dispose(); // close app
        System.exit(0); // SEG added 26 May 2009 bug fix
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveAsMenuItemActionPerformed
    {//GEN-HEADEREND:event_saveAsMenuItemActionPerformed
        saveAppAs();
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveMenuItemActionPerformed
    {//GEN-HEADEREND:event_saveMenuItemActionPerformed
        if(getFileSaveAs() == null || getFileSaveAs().length() == 0)
        {
            // then we need to save as
            saveAppAs();
        }
        else
        {
            saveApp(getFileSaveAs());
        }  
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openMenuItemActionPerformed
    {//GEN-HEADEREND:event_openMenuItemActionPerformed
        openFile();
    }//GEN-LAST:event_openMenuItemActionPerformed
    
    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeMenuItemActionPerformed
    {//GEN-HEADEREND:event_closeMenuItemActionPerformed
        closeScenario(true);
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveButtonActionPerformed
    {//GEN-HEADEREND:event_saveButtonActionPerformed
        
        if(getFileSaveAs() == null || getFileSaveAs().length() == 0)
        {
            // then we need to save as
            saveAppAs();
        }
        else
        {
            saveApp(getFileSaveAs());
        }  
        
    }//GEN-LAST:event_saveButtonActionPerformed

    private void new3DwindowMenuActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_new3DwindowMenuActionPerformed
    {//GEN-HEADEREND:event_new3DwindowMenuActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_new3DwindowMenuActionPerformed

    private void simPropMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_simPropMenuItemActionPerformed
    {//GEN-HEADEREND:event_simPropMenuItemActionPerformed
        SimulationPropPanel propPanel = new SimulationPropPanel(this);
        
        // create new internal frame window
        JInternalFrame iframe = new JInternalFrame("Simulation Properties",true,true,true,true);
        
        iframe.setContentPane( propPanel );
        iframe.setSize(305+35,335+115); // w, h
        iframe.setLocation(15, 20);

        iframe.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));
        
        // save frame
        propPanel.setInternalFrame(iframe);
        
        // add close action listener -- to remove window from hash
        iframe.addInternalFrameListener(this);
        
        iframe.setVisible(true);
        mainDesktopPane.add(iframe);
        try
        {
            iframe.setSelected(true);
        }
        catch (java.beans.PropertyVetoException e)
        {}
    }//GEN-LAST:event_simPropMenuItemActionPerformed

    private void twoDwindowPropMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_twoDwindowPropMenuItemActionPerformed
    {//GEN-HEADEREND:event_twoDwindowPropMenuItemActionPerformed
       
        // create a new internal window by passing in infor of current panel
        open2dWindowOptions();
        
    }//GEN-LAST:event_twoDwindowPropMenuItemActionPerformed

    private void screenShotButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_screenShotButtonActionPerformed
    {//GEN-HEADEREND:event_screenShotButtonActionPerformed
       createScreenCapture();
    }//GEN-LAST:event_screenShotButtonActionPerformed

    private void openSatBrowserButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openSatBrowserButtonActionPerformed
    {//GEN-HEADEREND:event_openSatBrowserButtonActionPerformed
        showSatBrowserInternalFrame();
    }//GEN-LAST:event_openSatBrowserButtonActionPerformed

    private void recenterToggleButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_recenterToggleButtonActionPerformed
    {//GEN-HEADEREND:event_recenterToggleButtonActionPerformed
        zoomIntoggleButton.setSelected(false);
        zoomOuttoggleButton.setSelected(false);
    }//GEN-LAST:event_recenterToggleButtonActionPerformed

    private void zoomOuttoggleButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zoomOuttoggleButtonActionPerformed
    {//GEN-HEADEREND:event_zoomOuttoggleButtonActionPerformed
        // make other zoom button unselected
        zoomIntoggleButton.setSelected(false);
        recenterToggleButton.setSelected(false);
    }//GEN-LAST:event_zoomOuttoggleButtonActionPerformed

    private void zoomIntoggleButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zoomIntoggleButtonActionPerformed
    {//GEN-HEADEREND:event_zoomIntoggleButtonActionPerformed
        // make other zoom button unselected
        zoomOuttoggleButton.setSelected(false); // does this trigger an event on other?, nope
        recenterToggleButton.setSelected(false);
    }//GEN-LAST:event_zoomIntoggleButtonActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_aboutMenuItemActionPerformed
    {//GEN-HEADEREND:event_aboutMenuItemActionPerformed
        // Show about menu
        AboutDialog ad = new AboutDialog(versionString, this, gov.nasa.worldwind.Version.getVersion());
        //System.out.println("V:" + gov.nasa.worldwind.Version.getVersion() );

        setLookandFeel(ad);
        
        ad.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void localTimeZoneCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localTimeZoneCheckBoxActionPerformed
        
        // Set the formatting timezone
        if(localTimeZoneCheckBox.isSelected())
        {
            // local time zone
            currentJulianDate.setTzStringFormat(localTZ);
            // epoch
            scenarioEpochDate.setTzStringFormat(localTZ);
        }
        else
        {
            // UTC
            currentJulianDate.setTzStringFormat( TimeZone.getTimeZone("UTC") );
            // epoch
            scenarioEpochDate.setTzStringFormat( TimeZone.getTimeZone("UTC") );
        }
        
        // update date box:
        dateTextField.setText( currentJulianDate.getDateTimeStr() );
        
    }//GEN-LAST:event_localTimeZoneCheckBoxActionPerformed

    private void dateTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dateTextFieldActionPerformed
    {//GEN-HEADEREND:event_dateTextFieldActionPerformed
        // save old time
        double prevJulDate = currentJulianDate.getJulianDate();
        
        // enter hit in date/time box...
        System.out.println("Date Time Changed");
        
        GregorianCalendar currentTimeDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        //or
        //GregorianCalendar currentTimeDate = new GregorianCalendar();
        
        boolean dateAccepted = true; // assume date valid at first
        try
        {
            currentTimeDate.setTime( dateformatShort1.parse(dateTextField.getText()) );
            dateTextField.setText(dateformat.format(currentTimeDate.getTime()));
        }
        catch(Exception e2)
        {
            try
            {
                // try reading without the milliseconds
                currentTimeDate.setTime( dateformatShort2.parse(dateTextField.getText()) );
                dateTextField.setText(dateformat.format(currentTimeDate.getTime()));
            }
            catch(Exception e3)
            {
                // bad date input put back the old date string
                dateTextField.setText(dateformat.format(currentTimeDate.getTime()));
                dateAccepted = false;
                System.out.println(" -- Rejected");
            } // catch 2
                        
        } // catch 1
        
        if(dateAccepted)
        {
            // date entered was good...
            System.out.println(" -- Accepted");
            
            // save
            currentJulianDate.set( currentTimeDate.getTimeInMillis() );
//            currentJulianDate.set(currentTimeDate.get(Calendar.YEAR),
//                                  currentTimeDate.get(Calendar.MONTH),
//                                  currentTimeDate.get(Calendar.DATE),
//                                  currentTimeDate.get(Calendar.HOUR_OF_DAY),
//                                  currentTimeDate.get(Calendar.MINUTE),
//                                  currentTimeDate.get(Calendar.SECOND));
                    
            // check that time change wasn't too great for resetting ground track
            double timeDiffDays = Math.abs(currentJulianDate.getJulianDate()-prevJulDate); // in days
            checkTimeDiffResetGroundTracks(timeDiffDays);
                        
            // update maps ----------------
            // set animation direction = 0
            currentPlayDirection = 0;
            // update graphics
            updateTime();
            
        } // if date accepted
        
        
    }//GEN-LAST:event_dateTextFieldActionPerformed

    private void decementTimeStepButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_decementTimeStepButtonActionPerformed
    {//GEN-HEADEREND:event_decementTimeStepButtonActionPerformed
        currentTimeStepSpeedIndex--;
        updateTimeStepsDataGUI();
    }//GEN-LAST:event_decementTimeStepButtonActionPerformed

    private void incrementTimeStepButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_incrementTimeStepButtonActionPerformed
    {//GEN-HEADEREND:event_incrementTimeStepButtonActionPerformed
        
        currentTimeStepSpeedIndex++;
        updateTimeStepsDataGUI();       
                
    }//GEN-LAST:event_incrementTimeStepButtonActionPerformed

    // updates timestep data and GUI based on current currentTimeStepSpeedIndex
    private void updateTimeStepsDataGUI()
    {
        // bounds checking
        if(currentTimeStepSpeedIndex > (timeStepSpeeds.length-1))
        {
            currentTimeStepSpeedIndex = (timeStepSpeeds.length-1);
        }
        else if(currentTimeStepSpeedIndex < 0)
        {
            currentTimeStepSpeedIndex = 0;
        }
        
        double speedInSec = timeStepSpeeds[currentTimeStepSpeedIndex];
                
        timeStepLabel.setText("" + speedInSec);
        
        animationSimStepSeconds = speedInSec; // seconds to days 
    }
    
    private void resetTimeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_resetTimeButtonActionPerformed
    {//GEN-HEADEREND:event_resetTimeButtonActionPerformed
        // save old time
        double prevJulDate = currentJulianDate.getJulianDate();

        // set/update current time to current time
        if(epochTimeEqualsCurrentTime)
        {
            // update to current time
            currentJulianDate.update2CurrentTime(); //update();// = getCurrentJulianDate(); // ini time
        }
        else
        {
            // update to epoch
            currentJulianDate.set(  scenarioEpochDate.getCurrentGregorianCalendar().getTimeInMillis() );
        }
        // check to see if large time jump occured
        double timeDiffDays = Math.abs(currentJulianDate.getJulianDate()-prevJulDate); // in days
        checkTimeDiffResetGroundTracks(timeDiffDays);
        
        // set animation direction = 0
        currentPlayDirection = 0;
        // update graphics
        updateTime();
    }//GEN-LAST:event_resetTimeButtonActionPerformed

    private void playBackButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_playBackButtonActionPerformed
    {//GEN-HEADEREND:event_playBackButtonActionPerformed
        currentPlayDirection = -1; // backwards
        runAnimation();
    }//GEN-LAST:event_playBackButtonActionPerformed

    private void stepBackButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_stepBackButtonActionPerformed
    {//GEN-HEADEREND:event_stepBackButtonActionPerformed
        currentPlayDirection = -1; // backward in time
        updateTime();
    }//GEN-LAST:event_stepBackButtonActionPerformed

    private void playButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_playButtonActionPerformed
    {//GEN-HEADEREND:event_playButtonActionPerformed
        //currentPlayDirection = 1; // forwards
        //runAnimation(); // perform animation
        playScenario();
    }//GEN-LAST:event_playButtonActionPerformed

    /**
     * play the scenario forward in time.
     */
    public void playScenario()
    {
        currentPlayDirection = 1; // forwards
        runAnimation(); // perform animation
    } // playScenario

    private void runAnimation()
    {
        // useses globally set animation direction and starts animation
        playButton.setEnabled(false);
        stepForwardTimeButton.setEnabled(false);
        stepBackButton.setEnabled(false);
        playBackButton.setEnabled(false);
        resetTimeButton.setEnabled(false);
        
        stopHit = false;
        //Create a timer.
        lastFPSms = System.currentTimeMillis();
        playTimer = new Timer(animationRefreshRateMs, new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                // take one time step in the aimation
                updateTime(); // animate
                long stopTime = System.currentTimeMillis();
                
                fpsAnimation = 1.0 / ((stopTime-lastFPSms)/1000.0); // fps calculation
                lastFPSms = stopTime;
                // goal FPS:
                //fpsAnimation = 1.0 / (animationRefreshRateMs/1000.0);
                
                if (stopHit)
                {
                    playTimer.stop();
                    resetAnimationIcons();                    
                }
                
                // SEG - if update took a long time reduce the timers repeat interval
                                
            }
        });
        playTimer.setRepeats(true);
        playTimer.start();
    } // runAnimation
    
    public void resetAnimationIcons()
    {
        if( realTimeModeCheckBox.isSelected() )
        {
            // real time mode
            playButton.setEnabled(true);
            stepForwardTimeButton.setEnabled(true);
            stepBackButton.setEnabled(false);
            playBackButton.setEnabled(false);
            resetTimeButton.setEnabled(false);
        }
        else
        {   // non-real time mode
            playButton.setEnabled(true);
            stepForwardTimeButton.setEnabled(true);
            stepBackButton.setEnabled(true);
            playBackButton.setEnabled(true);
            resetTimeButton.setEnabled(true);
        }
        
        
    } // resetAnimationIcons
    
    // input change in time in days, checks to see if ground tracks need to be updated
    public void checkTimeDiffResetGroundTracks(double timeDiffDays)
    {
        if( timeDiffDays > 91.0/1440.0)
        {
            // big time jump
            for (AbstractSatellite sat : satHash.values() )
            {
                if(sat.getShowGroundTrack() && (sat.getPeriod() <= (timeDiffDays*24.0*60.0) ) )
                {
                    sat.setGroundTrackIni2False();
                    //System.out.println(sat.getName() +" - Groundtrack Iniated");
                }
            }
        }
    } // checkTimeDiffResetGroundTracks
    
    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_stopButtonActionPerformed
    {//GEN-HEADEREND:event_stopButtonActionPerformed
        // TODO add your handling code here:
        stopAnimation();
    }//GEN-LAST:event_stopButtonActionPerformed

    public void stopAnimation()
    {
        stopHit = true; // set flag for next animation step
    }
    
    private void realTimeModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_realTimeModeCheckBoxActionPerformed
    {//GEN-HEADEREND:event_realTimeModeCheckBoxActionPerformed
        
        // first stop animating if animation is in progress
        stopAnimation();
        
        // turn on/off needed icons
        if( realTimeModeCheckBox.isSelected() )
        {
            // real time is on
            resetTimeButton.setEnabled(false);
            playBackButton.setEnabled(false);
            stepBackButton.setEnabled(false);
            
            animationRefreshRateMs = realTimeAnimationRefreshRateMs; // set real time refresh rate
        }
        else
        {
            // real time turned off
            resetTimeButton.setEnabled(true);
            playBackButton.setEnabled(true);
            stepBackButton.setEnabled(true);
            
            animationRefreshRateMs = nonRealTimeAnimationRefreshRateMs; // non real time refresh rate
        }
        
    }//GEN-LAST:event_realTimeModeCheckBoxActionPerformed

    private void stepForwardTimeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_stepForwardTimeButtonActionPerformed
    {//GEN-HEADEREND:event_stepForwardTimeButtonActionPerformed
        currentPlayDirection = 1; // forward in time
        updateTime();
    }//GEN-LAST:event_stepForwardTimeButtonActionPerformed

    private void new2DWindowMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_new2DWindowMenuItemActionPerformed
    {//GEN-HEADEREND:event_new2DWindowMenuItemActionPerformed
        
        createNew2dWindow();
        
    }//GEN-LAST:event_new2DWindowMenuItemActionPerformed

    private JInternalFrame createNew2dWindow()
    {
        
        // create 2D Earth Panel:
        //J2DEarthPanel newPanel = new J2DEarthPanel(satHash);
        J2DEarthPanel newPanel = new J2DEarthPanel(satHash, gsHash, zoomIntoggleButton, zoomOuttoggleButton, recenterToggleButton, currentJulianDate, sun, this);

        twoDWindowCount++;
        String windowName = "2D Earth Window - " + twoDWindowCount;
        newPanel.setName(windowName);
        twoDWindowVec.add(newPanel);

        // create new internal frame window
        JInternalFrame iframe = new JInternalFrame(windowName, true, true, true, true);

        iframe.setContentPane(newPanel);
        iframe.setSize(600, 350);
        iframe.setLocation(5, 5);

        iframe.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));

        // add close action listener -- to remove window from hash
        iframe.addInternalFrameListener(this);

        iframe.setVisible(true);
        mainDesktopPane.add(iframe);
        try
        {
            iframe.setSelected(true);
        } 
        catch (java.beans.PropertyVetoException e)
        {
        }

        return iframe; // return it if the user wants to set properties
    }
    
    private JInternalFrame createNew3DInternalWindow()
    {
        threeDInternalWindowCount++;
        String windowName = "3D Earth Window - " + threeDInternalWindowCount;
        

        // create new internal frame window
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);

        // create 3D Earth Panel:
        J3DEarthInternalPanel newPanel = new J3DEarthInternalPanel(iframe,satHash, gsHash,currentJulianDate.getMJD(), this);
        threeDInternalWindowVec.add( newPanel ); // add to vector
        
        // frame size       
        iframe.setContentPane(newPanel);
        iframe.setSize(400, 350);
        Point p = this.getLocation();
        iframe.setLocation(p.x + 15, p.y + 95);

        iframe.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));

        //iframe.pack(); // makes wwj slow
        
        iframe.setVisible(true);
        
        

        threeDWorking = true;
        update3Dconfig();
        
        addInternalFrame(iframe); // add to window-- need to add coordinates!!

        //return iframe; // return it if the user wants to set properties
        
        //satBrowser.setVisible(true); // show window
        
        newPanel.setFocusWWJ(); // make wwj respond to keyboard clicks
        
        return iframe;
    }

    // private JInternalFrame createNew3dWindow()
    public JDialog createNew3dWindow()
    {
        threeDWindowCount++;
        String windowName = "3D Earth Window - " + threeDWindowCount;
        

        // create new internal frame window
        //JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        //JFrame iframe = new JFrame(windowName);
        JDialog iframe = new JDialog(this, windowName, false); // parent, title, modal

        // create 3D Earth Panel:
        J3DEarthPanel newPanel = new J3DEarthPanel(iframe,satHash,gsHash,currentJulianDate.getMJD(), this);
        threeDWindowVec.add( newPanel ); // add to vector
        
        iframe.setContentPane(newPanel);
        iframe.setSize(400, 350);
        Point p = this.getLocation();
        iframe.setLocation(p.x + 15, p.y + 95);

        // add close action listener -- to remove window from hash
        iframe.addWindowListener(this);

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

        iframe.setVisible(true);

        threeDWorking = true;
        update3Dconfig();

        return iframe; // return it if the user wants to set properties

    } // new 3d window

    public void update3Dconfig()
    {
        // update the GUI based on info about if 3D is working
        if (threeDWorking)
        {
            new3DwindowMenu.setEnabled(true);
        } 
        else // not working
        {
            new3DwindowMenu.setEnabled(false);
        }
    } // update3Dconfig
    
    // update time and take an anmiation step   
    public void updateTime()
    {
        // save old time
        double prevJulDate = currentJulianDate.getJulianDate();
        
        // Get current simulation time!             
        if(realTimeModeCheckBox.isSelected())
        {
            // real time mode -- just use real time
            
            // Get current time in GMT
            // calculate current Juilian Date, update to current time
            currentJulianDate.update2CurrentTime(); //update();// = getCurrentJulianDate();

        }
        else
        {
            // non-real time mode add fraction of time to current jul date
            //currentJulianDate += currentPlayDirection*animationSimStepDays;
            currentJulianDate.addSeconds( currentPlayDirection*animationSimStepSeconds );
        }
        
        // update sun position
        sun.setCurrentMJD(currentJulianDate.getMJD());
        // DEBUG:
//        double [] sunPos = sun.getCurrentPositionJ2K();
//        System.out.println("Sun Pos(J2K) - Date (MJD): " + currentJulianDate.getMJD() + ", <x,y,z> < " +sunPos[0] +", " +sunPos[1] +", "+sunPos[2] +" >");
//        sunPos = sun.getCurrentPositionMOD();
//        System.out.println("Sun Pos(MOD) - Date (MJD): " + currentJulianDate.getMJD() + ", <x,y,z> < " +sunPos[0] +", " +sunPos[1] +", "+sunPos[2] +" >");
//        double[] llaTemp = GeoFunctions.GeodeticJulDate( sun.getCurrentPositionMOD() ,currentJulianDate.getJulianDate());
//        // sun.getCurrentPositionMOD()
//        //double[] llaTemp = GeoFunctions.GeodeticJulDate( new double[] {-1.400954880970050E+08,  5.168955443226393E+07 , 2.240975286312218E+07} ,currentJulianDate.getJDN());
//        // 01 Sep 2007 00:00:00.000 UTC
//        System.out.println("Sun lat/long :  " + llaTemp[0]*180.0/Math.PI + " , " + llaTemp[1]*180.0/Math.PI);
                
        // if time jumps by more than 91 minutes check period of sat to see if
        // ground tracks need to be updated
        double timeDiffDays = Math.abs(currentJulianDate.getJulianDate()-prevJulDate); // in days
        checkTimeDiffResetGroundTracks(timeDiffDays);        
                
        // update date box:
        dateTextField.setText( currentJulianDate.getDateTimeStr() );//String.format("%tc",cal) );
        
        // now propogate all satellites to the current time  
        for (AbstractSatellite sat : satHash.values() )
        {
            sat.propogate2JulDate( currentJulianDate.getJulianDate() );
        } // propgate each sat
        
        // update ground stations to the current time  
        for (GroundStation gs : gsHash.values() )
        {
            gs.setCurrentJulianDate( currentJulianDate.getJulianDate() );
            
            // test look angles
//            SatelliteProps sp= satHash.get("ISS (ZARYA)             ");
//            double[] aer = gs.calculate_AER(sp.getJ2000Position());
//            
//            System.out.println("AER: " + aer[0] + ", " + aer[1] + ", " + aer[2]);
            
        } // propgate each sat
        
        
        // update times in 3D windows
        for(J3DEarthPanel threeDPanel : threeDWindowVec )
        {
            threeDPanel.setMJD(currentJulianDate.getMJD());
        }
        for(J3DEarthInternalPanel threeDPanel : threeDInternalWindowVec )
        {
            threeDPanel.setMJD(currentJulianDate.getMJD());
        }
        
        // update any other time dependant objects
        for(JSatTrakTimeDependent tdo : timeDependentObjects)
        {
            if(tdo != null)
            {
                tdo.updateTime(currentJulianDate, satHash, gsHash);
            }
        }
                
        forceRepainting(); // repaint 2d/3d earth
        
        // update any satellite property window that is open
        for(SatPropertyPanel satP : satPropWindowVec)
        {
            satP.updateProperties(); 
        }
        
        // update Tracking windows
        for(JTrackingPanel tp : trackingWindowVec)
        {
            tp.updateTime( currentJulianDate.getDateTimeStr() );
        }
        
        
    } // update time
        
    public Time getCurrentJulianDay()
    {
        return currentJulianDate;
    }
    
    public void forceRepainting()
    {
        // force repainting of all 2D windows
        for(J2DEarthPanel twoDPanel : twoDWindowVec )
        {
            twoDPanel.repaint();
        }
        
        // repaint 3D windows
        for(J3DEarthPanel threeDPanel : threeDWindowVec )
        {
            threeDPanel.repaintWWJ();
        }
        for(J3DEarthInternalPanel threeDPanel : threeDInternalWindowVec )
        {
            threeDPanel.repaintWWJ();         
        }
        
    }// forceRepainting
    
    public void forceRepainting(boolean updateMapsData)
    {
        if(updateMapsData)
        {
            // update maps ----------------
            // set animation direction = 0
            currentPlayDirection = 0;
            // update graphics
            updateTime();
        }
        else
        {
            // just do a normal update -- don't regenerate data
            forceRepainting();
        }
    } // forceRepainting
    
    public double getCurrentJulTime()
    {
        return currentJulianDate.getJulianDate();
    }
    
    
    private void showSatBrowserActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showSatBrowserActionPerformed
    {//GEN-HEADEREND:event_showSatBrowserActionPerformed
        showSatBrowserInternalFrame();
    }//GEN-LAST:event_showSatBrowserActionPerformed
    
    public void showSatBrowserInternalFrame()
    {
//        // show satellite browser window
//        JSatBrowser satBrowser = new JSatBrowser(this, false, this); // non-modal version
//
//        // create new internal frame window
//        String windowName = "Satellite Browser";
//        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
//
//        iframe.setContentPane( satBrowser.getContentPane() );
//
//        if(satBrowser.getJMenuBar() != null)
//        {
//            iframe.setJMenuBar(satBrowser.getJMenuBar()); // get menu bar too! (if there is one)
//        }

        // NEW WAY
       JInternalFrame iframe = new JSatBrowserInternalFrame(this, this);

        iframe.setSize(261,450); // w,h
        iframe.setLocation(5,5);
        
        // add close action listener -- to remove window from hash
        iframe.addInternalFrameListener(this);
                
        iframe.setVisible(true);
        mainDesktopPane.add(iframe);
        try
        {
            iframe.setSelected(true);
        }
        catch (java.beans.PropertyVetoException e)
        {}
        
        
    } // showsatBrowserInternalFrame
    
    private void tleLoaderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tleLoaderMenuItemActionPerformed
        
                
        String windowName = "TLE Downloader";
        
        // create new internal frame window
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        TleDownloaderPanel  newPanel = new TleDownloaderPanel(this,iframe);
        
        iframe.setContentPane( newPanel ); // set contents pane
        iframe.setSize(375,230); // set size w,h

        iframe.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));
        
        iframe.setVisible(true);
        addInternalFrame(iframe);
        
    }//GEN-LAST:event_tleLoaderMenuItemActionPerformed

    private void newInternal3DWindowMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newInternal3DWindowMenuItemActionPerformed
    {//GEN-HEADEREND:event_newInternal3DWindowMenuItemActionPerformed
        
        // Easter Egg Sorta -- holding the control key while opening a 3D window opens the window in a dialog - though window is not saved
        // the dialog version uses a faster WWJ rendering class!!!
        if( evt.getModifiers() == 18) // 18 = control key held down (for win at least!) this could be problematic
        {
            createNew3dWindow();
            return;
        }
        
        // create new window
        createNew3DInternalWindow();
        
        
    }//GEN-LAST:event_newInternal3DWindowMenuItemActionPerformed

    private void showGSBrowserMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showGSBrowserMenuItemActionPerformed
    {//GEN-HEADEREND:event_showGSBrowserMenuItemActionPerformed
        showGSBrowserInternalFrame();
    }//GEN-LAST:event_showGSBrowserMenuItemActionPerformed

    private void trackToolMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_trackToolMenuItemActionPerformed
    {//GEN-HEADEREND:event_trackToolMenuItemActionPerformed
        openTrackingToolSelectorWindow();
    }//GEN-LAST:event_trackToolMenuItemActionPerformed

    private void newExternal3DWindowMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newExternal3DWindowMenuItemActionPerformed
    {//GEN-HEADEREND:event_newExternal3DWindowMenuItemActionPerformed
        createNew3dWindow();
    }//GEN-LAST:event_newExternal3DWindowMenuItemActionPerformed

    private void consoleMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_consoleMenuItemActionPerformed
    {//GEN-HEADEREND:event_consoleMenuItemActionPerformed
         console.setVisible(true);
    }//GEN-LAST:event_consoleMenuItemActionPerformed

    private void commandShellMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_commandShellMenuItemActionPerformed
    {//GEN-HEADEREND:event_commandShellMenuItemActionPerformed
   
        JDialog iframe = new JDialog(this, "Command Shell", false); // parent, title, modal

        // create 3D Earth Panel:
        
        iframe.setContentPane(commandConsole);
        
        iframe.setSize(450, 250);
        Point p = this.getLocation();
        iframe.setLocation(p.x + 15, p.y + 95);
        iframe.addWindowListener(this);


        iframe.setVisible(true);
        
        // set menu item to disabled
        commandShellMenuItem.setEnabled(false);
        
    }//GEN-LAST:event_commandShellMenuItemActionPerformed

    private void commandShellDesktopMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_commandShellDesktopMenuItemActionPerformed
    {//GEN-HEADEREND:event_commandShellDesktopMenuItemActionPerformed
        // open the command desktop - all kinds of freatures for editing/testing scripts 
        // and a class browser
        try
        {
            beanShellInterp.eval("desktop();");
        }catch(Exception e){}
    }//GEN-LAST:event_commandShellDesktopMenuItemActionPerformed

    private void commandServerMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_commandServerMenuItemActionPerformed
    {//GEN-HEADEREND:event_commandServerMenuItemActionPerformed
        // open a command server
        GuiServer guiServer = new GuiServer(beanShellInterp);
        //setLookandFeel(guiServer);
        guiServer.setVisible(true);
    }//GEN-LAST:event_commandServerMenuItemActionPerformed

    private void commandClientMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_commandClientMenuItemActionPerformed
    {//GEN-HEADEREND:event_commandClientMenuItemActionPerformed
        CommandClientGUI client = new CommandClientGUI();
        //setLookandFeel(client);
        client.setVisible(true);   
    }//GEN-LAST:event_commandClientMenuItemActionPerformed

    private void openGSbrowserActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openGSbrowserActionPerformed
    {//GEN-HEADEREND:event_openGSbrowserActionPerformed
        showGSBrowserInternalFrame();
    }//GEN-LAST:event_openGSbrowserActionPerformed

private void createMovieButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMovieButtonActionPerformed
    JCreateMovieDialog panel;
    
    // figure out what window is selected (if any), get location on screen / width / height

    // if no frames are selected - use entire application as component
    if (mainDesktopPane.getSelectedFrame() == null) {
        panel = new JCreateMovieDialog(this, false, this.getContentPane(), this, "JSatTrak App");
    }
    else if (mainDesktopPane.getSelectedFrame().getContentPane() instanceof J3DEarthComponent) { // 3d window

        panel = new JCreateMovieDialog(this, false, (J3DEarthComponent) mainDesktopPane.getSelectedFrame().getContentPane(), this);
    }
    else if (mainDesktopPane.getSelectedFrame().getContentPane() instanceof J2DEarthPanel) { // if a 2d window

        panel = new JCreateMovieDialog(this, false, (J2DEarthPanel) mainDesktopPane.getSelectedFrame().getContentPane(), this);
    }
    else // default container
    {
        panel = new JCreateMovieDialog(this, false, mainDesktopPane.getSelectedFrame().getContentPane(), this, mainDesktopPane.getSelectedFrame().getName());
    }

    Point p = this.getLocationOnScreen();
    panel.setLocation(p.x + 15, p.y + 55);
    setLookandFeel(panel);
    panel.setVisible(true);
}//GEN-LAST:event_createMovieButtonActionPerformed

private void utilitiesMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesMenuActionPerformed

}//GEN-LAST:event_utilitiesMenuActionPerformed

private void movieWholeAppMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_movieWholeAppMenuItemActionPerformed
    JCreateMovieDialog panel = new JCreateMovieDialog(this, false, this.getContentPane(), this, "JSatTrak App");


    Point p = this.getLocationOnScreen();
    panel.setLocation(p.x + 15, p.y + 55);
    setLookandFeel(panel);
    panel.setVisible(true);
}//GEN-LAST:event_movieWholeAppMenuItemActionPerformed

private void coverageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_coverageMenuItemActionPerformed
        
        // check if coverage anaylzer class is null -
        if(coverageAnalyzer == null)
        {
            coverageAnalyzer = new CoverageAnalyzer(currentJulianDate); // setup new analyzer
            // add coverage analyzer to time update objects
            timeDependentObjects.add(coverageAnalyzer); // add object to time updates
            
            // update the CA object in any 3D window currently opened
            for(J3DEarthInternalPanel panel: threeDInternalWindowVec)
            {
                panel.updateCoverageLayerObject(coverageAnalyzer);
            }
            for(J3DEarthPanel panel: threeDWindowVec)
            {
                panel.updateCoverageLayerObject(coverageAnalyzer);
            }
        }
        
        // show satellite browser window
        //JTrackingToolSelector trackingBrowser = new JTrackingToolSelector(satHash, gsHash, this); // non-modal version
        JCoverageDialog coverageBrowser = new JCoverageDialog(coverageAnalyzer, currentJulianDate, this, satHash, twoDWindowVec);
        
        // create new internal frame window
        String windowName = "Coverage Analysis Options";
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        iframe.setContentPane(coverageBrowser );
        iframe.setSize(530+35,330+85); // w,h
        iframe.setLocation(10,10);

        iframe.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));
        
        coverageBrowser.setIframe(iframe);
        
        // add close action listener -- to remove window from hash
        iframe.addInternalFrameListener(this);
                
        iframe.setVisible(true);
        mainDesktopPane.add(iframe);
        try
        {
            iframe.setSelected(true);
        }
        catch (java.beans.PropertyVetoException e)
        {}
}//GEN-LAST:event_coverageMenuItemActionPerformed

private void toolbar3DWindowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbar3DWindowButtonActionPerformed
    createNew3dWindow();
}//GEN-LAST:event_toolbar3DWindowButtonActionPerformed

private void lookFeelMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lookFeelMenuItemActionPerformed
     LookAndFeelJDialog diag = new LookAndFeelJDialog(this, true);
     diag.setLocationRelativeTo(mainDesktopPane);
     setLookandFeel(diag);
     diag.setVisible(true);
}//GEN-LAST:event_lookFeelMenuItemActionPerformed
    
    public JTrackingPanel openTrackingToolSelectorWindow()
    {
        // show satellite browser window
        //JTrackingToolSelector trackingBrowser = new JTrackingToolSelector(satHash, gsHash, this); // non-modal version
        JTrackingPanel trackingBrowser = new JTrackingPanel(satHash,gsHash,currentJulianDate.getDateTimeStr(),currentJulianDate, this);
        
        // add panel to vector for updates
        trackingWindowVec.add(trackingBrowser);
        

        // create new internal frame window
        String windowName = "Tracking Tool";
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        iframe.setContentPane(trackingBrowser );
        iframe.setSize(400+105,320+55); // w,h
        iframe.setLocation(10,10);

        iframe.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));
        
        // add close action listener -- to remove window from hash
        iframe.addInternalFrameListener(this);
                
        iframe.setVisible(true);
        mainDesktopPane.add(iframe);
        try
        {
            iframe.setSelected(true);
        }
        catch (java.beans.PropertyVetoException e)
        {}
        
        return trackingBrowser;
        
    } // openTrackingToolSelectorWindow
    
   public void showGSBrowserInternalFrame()
   {
        
        
        //satBrowser.setVisible(true); // show window
        
        // create new internal frame window
        String windowName = "Ground Station Browser";
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        // show satellite browser window
        JGroundStationBrowser gsBrowser = new JGroundStationBrowser(this); // non-modal version
        
        iframe.setContentPane( gsBrowser );
        iframe.setSize(261,380); // w,h
        iframe.setLocation(5,5);

        iframe.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));
        
        // add close action listener -- to remove window from hash
        iframe.addInternalFrameListener(this);
                
        iframe.setVisible(true);
        mainDesktopPane.add(iframe);
        try
        {
            iframe.setSelected(true);
        }
        catch (java.beans.PropertyVetoException e)
        {}
        
   } // showGSBrowserInternalFrame
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem commandClientMenuItem;
    private javax.swing.JMenuItem commandServerMenuItem;
    private javax.swing.JMenuItem commandShellDesktopMenuItem;
    private javax.swing.JMenuItem commandShellMenuItem;
    private javax.swing.JMenuItem consoleMenuItem;
    private javax.swing.JMenuItem coverageMenuItem;
    private javax.swing.JButton createMovieButton;
    private javax.swing.JTextField dateTextField;
    private javax.swing.JButton decementTimeStepButton;
    private javax.swing.JPanel desktopPanel;
    private javax.swing.JScrollPane desktopScrollPane;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton incrementTimeStepButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JToolBar jToolBar;
    private javax.swing.JMenuItem localTimeMenuItem;
    private javax.swing.JCheckBox localTimeZoneCheckBox;
    private javax.swing.JMenuItem lookFeelMenuItem;
    public javax.swing.JDesktopPane mainDesktopPane;
    private javax.swing.JMenuItem movieWholeAppMenuItem;
    private javax.swing.JMenuItem new2DWindowMenuItem;
    private javax.swing.JMenu new3DwindowMenu;
    private javax.swing.JMenuItem newExternal3DWindowMenuItem;
    private javax.swing.JMenuItem newInternal3DWindowMenuItem;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem nonRealTimeMenuItem;
    private javax.swing.JButton openGSbrowser;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JButton openSatBrowserButton;
    private javax.swing.JButton playBackButton;
    private javax.swing.JButton playButton;
    private javax.swing.JMenu pluginMenu;
    private javax.swing.JMenuItem realTimeMenuItem;
    private javax.swing.JCheckBox realTimeModeCheckBox;
    private javax.swing.JToggleButton recenterToggleButton;
    private javax.swing.JButton resetTimeButton;
    private javax.swing.JInternalFrame satListInternalFrame;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton screenShotButton;
    private javax.swing.JMenuItem showGSBrowserMenuItem;
    private javax.swing.JMenuItem showSatBrowser;
    private javax.swing.JMenu simMenu;
    private javax.swing.JMenuItem simPropMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JProgressBar statusProgressBar;
    private javax.swing.JButton stepBackButton;
    private javax.swing.JButton stepForwardTimeButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JMenuItem sysPropsMenuItem;
    private javax.swing.JLabel timeStepLabel;
    private javax.swing.JMenuItem tleLoaderMenuItem;
    private javax.swing.JButton toolbar3DWindowButton;
    private javax.swing.JMenuItem trackToolMenuItem;
    private javax.swing.JMenuItem twoDwindowPropMenuItem;
    private javax.swing.JMenuItem utcTimeMenuItem;
    private javax.swing.JMenu utilitiesMenu;
    private javax.swing.JToggleButton zoomIntoggleButton;
    private javax.swing.JToggleButton zoomOuttoggleButton;
    // End of variables declaration//GEN-END:variables
    
    
    // Internal Window Frame Closing Listner
    public void internalFrameClosing(InternalFrameEvent e) {}
    
    public void internalFrameClosed(InternalFrameEvent e) 
    {
        try  // try to cast frame contents as a J2DEarthPanel -- to see if it is that type
        {
            J2DEarthPanel closedEarthPanel = (J2DEarthPanel)(((JInternalFrame)e.getSource()).getContentPane());
            twoDWindowVec.remove( closedEarthPanel );
        }
        catch(Exception ee)
        {
            try
            {
                // if not see if it was a SatPropertyPanel
                SatPropertyPanel closedPropPanel = (SatPropertyPanel)(((JInternalFrame)e.getSource()).getContentPane());
                satPropWindowVec.remove( closedPropPanel );
                //System.out.println("Prop window Closed");
            }
            catch(Exception eee)
            {
                //SatSettingsPanel
                try
                {
                    // if not see if it was a SatPropertyPanel
                    SatSettingsPanel closedPropPanel = (SatSettingsPanel)(((JInternalFrame)e.getSource()).getContentPane());
                    
                    //System.out.println("Settings window Closed");
                }
                catch(Exception eeee)
                {
                    
                    
                    try
                    {
                        // if not see if it was a J3DEarthInternalPanel
                        J3DEarthInternalPanel closedPropPanel = (J3DEarthInternalPanel) (((JInternalFrame) e.getSource()).getContentPane());
                        threeDInternalWindowVec.remove( closedPropPanel );
                        System.gc(); // clean up
                        
                    //System.out.println("Settings window Closed");
                    }
                    catch (Exception eeeee)
                    {
                        // something else trackingWindowVec
                        try
                        {
                            // if not see if it was a J3DEarthInternalPanel
                            JTrackingPanel closedPropPanel = (JTrackingPanel) (((JInternalFrame) e.getSource()).getContentPane());
                            trackingWindowVec.remove(closedPropPanel);
                            System.gc(); // clean up

                        //System.out.println("Settings window Closed");
                        }
                        catch (Exception eeeeee)
                        {
                        // something else trackingWindowVec
                        }
                    }
                }
            }

        }// 2D Earth window
        
        //System.out.println("2D Windows Open:" + twoDWindowVec.size() );
        
    } // internalFrameClosed

    public void internalFrameOpened(InternalFrameEvent e) {}

    public void internalFrameIconified(InternalFrameEvent e) {}

    public void internalFrameDeiconified(InternalFrameEvent e) {}

    public void internalFrameActivated(InternalFrameEvent e) {}

    public void internalFrameDeactivated(InternalFrameEvent e) {}

    void displayMessage(String prefix, InternalFrameEvent e) {}
    
    // overloaded addInternalFrame to default location
    public void addInternalFrame(JInternalFrame iframe)
    {
        addInternalFrame(iframe, 15, 15);
    }
    
    
    // add internal frame to desktop
    public void addInternalFrame(JInternalFrame iframe, int xloc, int yloc)
    {
        // add action listener
        iframe.addInternalFrameListener(this);
        
        iframe.setLocation(xloc,yloc); // set starting loc

        // add default icon
        iframe.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));
        
        iframe.setVisible(true);
        
        // add frame
        mainDesktopPane.add(iframe);
        
        // select new frame
        try
        {
            iframe.setSelected(true);
        }
        catch (java.beans.PropertyVetoException e)
        {}
        
        // Figure out which kind of internal frame was added, and add it to Vector
        try
        {
            // is it a satellite Propery window?
            SatPropertyPanel closedPropPanel = (SatPropertyPanel)( iframe.getContentPane() );
            satPropWindowVec.add( closedPropPanel ); // add it to the vector
            // init it
            closedPropPanel.updateProperties();
        }
        catch(Exception e)
        {
            // else somthing else
        }
        
    } // add InternalFrame
    
    public void updateTleDataInCurrentList()
    {
        // update TLE data in current list  - used mostly after updating TLEs
        JSatBrowser newBrowswer = new JSatBrowser(this, false, this);
        Hashtable<String,TLE> tleHash = newBrowswer.getTleHash();
        
        for (AbstractSatellite sat : satHash.values() )
        {
            if (sat instanceof SatelliteTleSGP4) // if sat is a TLE/SGP4 sat
            {
                String name = sat.getName();
                TLE newTLE = tleHash.get(name);

                if (newTLE != null)
                {
                    sat.updateTleData(newTLE);
                }
            }
            
        } // propgate each sat
        
        forceRepainting(true); // fore repaint with update to all data
                
    } //updateTleDataInCurrentList
    
    public void addSat2ListByName(String satName)
    {
        // add sat to list if TLE exsits by name
        JSatBrowser newBrowswer = new JSatBrowser(this, false, this);
        
        Hashtable<String,TLE> tleHash = newBrowswer.getTleHash();
        
         TLE newTLE = tleHash.get(satName); 
         
         if(newTLE != null)
         {
            // make sat prop object and add it to the list
             try
             {
                SatelliteTleSGP4 prop = new SatelliteTleSGP4(newTLE.getSatName(), newTLE.getLine1(), newTLE.getLine2());
            
                // add sat to list
                objListPanel.addSat2List(prop);
             }
             catch(Exception e)
             {
                // do nothing just ignore the bad satellite trying to be added (and don't add it)
             }
         }
         
    } // addSat2ListByName
    
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
            
            // figure out what window is selected (if any), get location on screen / width / height
            if( mainDesktopPane.getSelectedFrame() == null)
            {
                JOptionPane.showInternalMessageDialog(mainDesktopPane,"A window must be selected to perform a screenshot","ERROR",JOptionPane.ERROR_MESSAGE);
                return;
            }
            pt = mainDesktopPane.getSelectedFrame().getContentPane().getLocationOnScreen();
            width = mainDesktopPane.getSelectedFrame().getContentPane().getWidth();
            height = mainDesktopPane.getSelectedFrame().getContentPane().getHeight();
            
            // if a 2d window was selected do a little more math to just get to map
            if(mainDesktopPane.getSelectedFrame().getContentPane() instanceof J2DEarthPanel)
            {
                //System.out.println("2D WINDOW");
                double aspectRatio = 2.0; // width/height
                int newWidth=1, newHeight=1;
                if(height != 0)
                {
                    if(width / height > aspectRatio)
                    {
                        // label has larger aspect ratio, constraint by height
                        newHeight = height;
                        newWidth = (int) (height*aspectRatio);
                    }
                    else
                    {
                        // label has lower aspect ratio
                        newWidth = width;
                        newHeight = (int) (width*1.0/aspectRatio);
                    }
                    
                    // changes in size
                    int deltaW = width - newWidth;
                    int deltaH = height - newHeight;
                    
                    pt.y = pt.y + (int)(deltaH/2.0);
                    pt.x = pt.x + (int)(deltaW/2.0);
                    
                    width = newWidth;
                    height = newHeight;
                }// find scale
            } // if 2D panel
            
            // not a possible size
            if(height<=0 || width<=0)
            {
                // no screen shot
                JOptionPane.showInternalMessageDialog(mainDesktopPane,"A Screenshot was not possible - too small of size","ERROR",JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // full app screen shot
            //BufferedImage screencapture = new Robot().createScreenCapture(
            //			   new Rectangle( mainFrame.getX()+viewsTabbedPane.getX(), mainFrame.getY(),
            //					   viewsTabbedPane.getWidth(), mainFrame.getHeight() ) );
            // scree shot of just window
            BufferedImage screencapture = new Robot().createScreenCapture(
                    new Rectangle( pt.x, pt.y,width, height) );
            
            
            //    	Create a file chooser
            final JFileChooser fc = new JFileChooser();
            jsattrak.utilities.CustomFileFilter pngFilter = new jsattrak.utilities.CustomFileFilter("png","*.png");
            fc.addChoosableFileFilter(pngFilter);
            jsattrak.utilities.CustomFileFilter jpgFilter = new jsattrak.utilities.CustomFileFilter("jpg","*.jpg");
            fc.addChoosableFileFilter(jpgFilter);
            int returnVal = fc.showSaveDialog(this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File file = fc.getSelectedFile();
                
                String fileExtension = "png"; // default
                if(fc.getFileFilter() == pngFilter)
                {
                    fileExtension = "png";
                }
                if(fc.getFileFilter() == jpgFilter)
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
                //ImageIO.write(screencapture, fileExtension, file); // old way
                //System.out.println("Saved!" + fileExtension );

                // new way to save file -- better compression for jpg
                Exception e = SaveImageFile.saveImage(fileExtension, file, screencapture, 0.9f); // the last one is the compression quality (1=best)
                if(e != null)
                {
                    System.out.println("ERROR SCREEN CAPTURE:" + e.toString());
                    JOptionPane.showInternalMessageDialog(mainDesktopPane,"ERROR SCREEN CAPTURE:" + e.toString(),"ERROR",JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
            }
            else
            {
                //log.append("Open command cancelled by user." + newline);
            }
            
            
        }
    	catch(Exception e4)
    	{
    		System.out.println("ERROR SCREEN CAPTURE:" + e4.toString());
            JOptionPane.showInternalMessageDialog(mainDesktopPane,"ERROR SCREEN CAPTURE:" + e4.toString(),"ERROR",JOptionPane.ERROR_MESSAGE);
    	}
    } // createScreenCapture
    
    public static String getExtension(File f) 
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
        {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    } // getExtension
    
    public void open2dWindowOptions()
    {
        if( mainDesktopPane.getSelectedFrame() == null || !(mainDesktopPane.getSelectedFrame().getContentPane() instanceof J2DEarthPanel))
        {
            JOptionPane.showInternalMessageDialog(mainDesktopPane,"A 2D window must be selected.","ERROR",JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // first check to make sure a 2D window is selected
        // if a 2d window was selected do a little more math to just get to map
        
        
        J2DEarthPanel panel =  (J2DEarthPanel)mainDesktopPane.getSelectedFrame().getContentPane();
        
        Point pt = mainDesktopPane.getSelectedFrame().getLocation();
        
        TwoDViewPropertiesPanel propPanel = new TwoDViewPropertiesPanel(panel, this);
        
        // create new internal frame window
        JInternalFrame iframe = new JInternalFrame("2D Earth Window Prop.",true,true,true,true);
        
        iframe.setContentPane( propPanel );
        iframe.setSize(605,377+30); // w, h
        iframe.setLocation(pt.x + 15, pt.y + 20);

        iframe.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png")));
        
        // save frame
        propPanel.setInternalFrame(iframe);
        
        // add close action listener -- to remove window from hash
        iframe.addInternalFrameListener(this);
        
        iframe.setVisible(true);
        mainDesktopPane.add(iframe);
        try
        {
            iframe.setSelected(true);
        }
        catch (java.beans.PropertyVetoException e)
        {}
        
        
        
    } // open2dWindowOptions

    public int getRealTimeAnimationRefreshRateMs()
    {
        return realTimeAnimationRefreshRateMs;
    }

    public void setRealTimeAnimationRefreshRateMs(int realTimeAnimationRefreshRateMs)
    {
        this.realTimeAnimationRefreshRateMs = realTimeAnimationRefreshRateMs;
        
        //  set refresh rate
        if( realTimeModeCheckBox.isSelected() )
        {            
            animationRefreshRateMs = realTimeAnimationRefreshRateMs; // set real time refresh rate
        }
        else
        {            
            animationRefreshRateMs = nonRealTimeAnimationRefreshRateMs; // non real time refresh rate
        }
        
    }

    public int getNonRealTimeAnimationRefreshRateMs()
    {
        return nonRealTimeAnimationRefreshRateMs;
    }

    public void setNonRealTimeAnimationRefreshRateMs(int nonRealTimeAnimationRefreshRateMs)
    {
        this.nonRealTimeAnimationRefreshRateMs = nonRealTimeAnimationRefreshRateMs;
        
        //  set refresh rate
        if( realTimeModeCheckBox.isSelected() )
        {            
            animationRefreshRateMs = realTimeAnimationRefreshRateMs; // set real time refresh rate
        }
        else
        {            
            animationRefreshRateMs = nonRealTimeAnimationRefreshRateMs; // non real time refresh rate
        }
    }
    
    public void saveApp(String fileName)
    {
        
        // testing for now, save the entire application that is running
        // Write to disk with FileOutputStream
        try
        {
            // Writing UTF-8 Encoded Data
            //BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
//            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF8"));
//            XStream xstream = new XStream();
//            out.write(xstream.toXML(new JstSaveClass(this)));
//            out.close();

            // SEG - added in version 4.0--- using zip format to save files, files are MUCH SMALLER!
            // try writing to a zipped file
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(fileName));
            XStream xstream = new XStream();

            // Add ZIP entry to output stream.
            zipOut.putNextEntry(new ZipEntry("JSatTrak.scenario"));

            xstream.toXML(new JstSaveClass(this),zipOut);

            zipOut.closeEntry();
            zipOut.close();
            
            setStatusMessage("Saved Scenario: " + fileName);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error Saving File: " + e.toString(), "SAVE ERROR", JOptionPane.ERROR_MESSAGE);
            setStatusMessage("Error Saving File: " + e.toString());
        }
        
    } // saveApp
    
    // returns if the scenario was actually closed
    public boolean closeScenario(boolean askIfSure)
    {
        // close scenario
        
        // make sure you want to close
        //JOptionPane.showMessageDialog(app, message, "ERROR", JOptionPane.ERROR_MESSAGE);
        if(askIfSure)
        {
            int answer = JOptionPane.showConfirmDialog(this,"Are you sure you want to close the current scenario?","Close?",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
            
            if(answer != JOptionPane.YES_OPTION)
            {
                return false; // don't close
            }
        }
        
        // clear satHash
        satHash.clear();
        gsHash.clear();
        
        // clear console
        console.clearMessages();
        
        // close all internal windows besides the sat list
        for(JInternalFrame jif : mainDesktopPane.getAllFrames())
        {
            if( !(jif.getContentPane() instanceof JObjectListPanel ) )
            {
                jif.dispose(); // don't close sat list
            }
            else
            {
                ((JObjectListPanel)jif.getContentPane()).refreshObjectList(); // refresh the list (clear it)
                jif.setLocation(610, 5); // ini location
            }
        }
        
        // close all 3D views
        for(J3DEarthPanel panel : threeDWindowVec)
        {
            panel.closeWindow();
        }

        
        // clear other window Vectors in case they were not cleared correctly
        twoDWindowVec.clear();
        satPropWindowVec.clear();
        threeDWindowVec.clear();
        threeDInternalWindowVec.clear();
        
        // reset save file name
        fileSaveAs = null;
        
        // window count reset
        twoDWindowCount = 0;
        threeDWindowCount = 0;
        threeDInternalWindowCount = 0;
        
        // clear time dependant objects
        timeDependentObjects.clear();
        
        // reset coverage analysis
        coverageAnalyzer = null;
        
        System.gc(); // clean up
        
        return true;
        
    } //closeScenario
    
    // open file populate scenario
    public void openFile()
    {
        
        
//        boolean closed = closeScenario(true); // first close scenario
//        
//        if(!closed)
//        {
//            return;
//        }
        
    	final JFileChooser fc = new JFileChooser(getFileSaveAs());
    	CustomFileFilter xmlFilter = new CustomFileFilter("jst","*.jst");
    	fc.addChoosableFileFilter(xmlFilter);

    	int returnVal = fc.showOpenDialog(this);
    	
    	if (returnVal == JFileChooser.APPROVE_OPTION) 
    	{
            File file = fc.getSelectedFile();
            
            boolean closed = closeScenario(true); // make sure it is okay to close current scenario?
            
            if(!closed)
            {
                return;
            }
            
            // save file name
            fileSaveAs = file.getAbsolutePath();
            
            // Open and load file into scenario
            try
            {
//                // Read from disk using FileInputStream
//                FileInputStream f_in = new FileInputStream(fileSaveAs);
//                
//                // Read object using ObjectInputStream
//                ObjectInputStream obj_in = new JSX.ObjectReader(f_in);// new ObjectInputStream(f_in);  // JSX
//                
//                // Read an object
//                Object obj = obj_in.readObject();
//                
//                f_in.close(); // close file
                
                 // before version 4.0 way
//                 BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(getFileSaveAs()), "UTF8"));
//
//                XStream xstream = new XStream();
//                Object obj = xstream.fromXML(in);

                // v4.0 - use zip file to get data out of the file
                ZipInputStream in = new ZipInputStream(new FileInputStream(getFileSaveAs()));
                ZipEntry entry = in.getNextEntry();
                XStream xstream = new XStream();
                Object obj = xstream.fromXML(in);

                if (obj instanceof JstSaveClass) // it better be
                {
                    
                    // Cast object to a Vector
                    JstSaveClass openClass = (JstSaveClass) obj;
                                       
                    // load satHash carefully
                    satHash.clear();
                    Hashtable<String,AbstractSatellite> tempHash = openClass.getSatHash();
                    for(String key : tempHash.keySet() )
                    {
                        satHash.put(key, tempHash.get(key)); // copy manually
                        satHash.get(key).setUse3dModel( satHash.get(key).isUse3dModel() ); // auto-loads 3D models if they are used
                    }
                    
                    // populate ground station hash
                    gsHash.clear();
                    Hashtable<String, GroundStation> tempHash2 = openClass.getGsHash();
                    for (String key : tempHash2.keySet())
                    {
                        gsHash.put(key, tempHash2.get(key)); // copy manually
                    }

                    // update sat List
                    objListPanel.refreshObjectList();
                   
                    
                    
                    // load other data
                    this.currentJulianDate = openClass.getCurrentJulianDate();
                    this.realTimeAnimationRefreshRateMs = openClass.getRealTimeAnimationRefreshRateMs();
                    this.nonRealTimeAnimationRefreshRateMs = openClass.getNonRealTimeAnimationRefreshRateMs();
                    this.currentTimeStepSpeedIndex = openClass.getCurrentTimeStepSpeedIndex();
                    this.setLocalTimeModeSelected( openClass.isLocalTimeZoneSelected() );
                    this.setRealTimeMode(openClass.isRealTimeMode() );
                    
                    this.epochTimeEqualsCurrentTime = openClass.isEpochTimeEqualsCurrentTime();
                    if(!epochTimeEqualsCurrentTime)
                    {
                        this.scenarioEpochDate = openClass.getScenarioEpochDate();
                    }
                    
                    // update parameters based on these new parameters
                    updateTimeStepsDataGUI();
                    setRealTimeAnimationRefreshRateMs(realTimeAnimationRefreshRateMs); // will reset all internal values correctly for refresh
                    
                    // location of Object list
                    satListInternalFrame.setBounds(openClass.getSatListX(),openClass.getSatListY(),openClass.getSatListWidth(),openClass.getSatListHeight());
                    satListInternalFrame.setSelected(true);
                    
                    // wwj offline mode
                    this.wwjOfflineMode = openClass.isWwjOfflineMode();
                    // set ofline mode
                    try{
                        gov.nasa.worldwind.WorldWind.getNetworkStatus().setOfflineMode(wwjOfflineMode);
                    }catch(Exception ee){}
                    
                    // coverage analyzer
                    this.coverageAnalyzer = openClass.getCa();
                    if(coverageAnalyzer != null)
                    {
                        timeDependentObjects.add(coverageAnalyzer); // add object to time updates
                    }
                    
                    // create all the needed 2D windows:
                    for( J2DEarthPanelSave j2dp : openClass.getTwoDWindowSaveVec() )
                    {
                        JInternalFrame iframe = createNew2dWindow(); // create new window
                        J2DEarthPanel newPanel = twoDWindowVec.lastElement(); // get window just created
                        
                        j2dp.copySettings2PanelAndFrame(newPanel, iframe); // copy settings to this window
                    }
                    
                    // create all the needed sat prop windows:                    
                    for( SatPropertyPanelSave propPan : openClass.getSatPropWindowSaveVec() )
                    {
                        // addInternalFrame(JInternalFrame iframe)
                        AbstractSatellite prop = satHash.get(propPan.getName());
                        
                        // create property Panel:
                        SatPropertyPanel newPanel = new SatPropertyPanel(prop);
                        
                        String windowName = prop.getName().trim(); // set name - trim excess spaces
                        
                        // create new internal frame window
                        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
                        
                        iframe.setContentPane( newPanel ); // set contents pane
                        iframe.setSize(propPan.getWidth(),propPan.getHeight()); // set size
                                
                        iframe.setVisible(true);
                        addInternalFrame(iframe, propPan.getXPos(), propPan.getYPos() );
                    }
                    
                    // PUT BEFORE 3D windows so they load correctly though data in them might be funny?
                    // update GUI -- this is messing up the 3D views.. they need some time to render
                    forceRepainting(true); // force repaint and regeneration of data
                                      
                    // create all needed 3D windows:
                     
                    // create all 3D external windows needed
                    for( J3DEarthlPanelSave j3dp : openClass.getThreeDExtWindowSaveVec() )
                    {
                        Container iframe = createNew3dWindow();
                        J3DEarthPanel newPanel = threeDWindowVec.lastElement(); // get window just created
                        
                        j3dp.copySettings2PanelAndFrame(newPanel,iframe); // copy settings to this window
                    }
                    // create all 3D internal windows needed
                    for( J3DEarthlPanelSave j3dp : openClass.getThreeDWindowSaveVec() )
                    {
                        JInternalFrame iframe = createNew3DInternalWindow(); // create new window
                        J3DEarthInternalPanel newPanel = threeDInternalWindowVec.lastElement(); // get window just created
                        
                        j3dp.copySettings2PanelAndFrame(newPanel, iframe); // copy settings to this window
                    }

                    // New 20 March 2009  -- app size and onscreen location
                    try
                    {
                        this.setLocation(openClass.getScreenLoc());
                        this.setSize(openClass.getAppWidth(),openClass.getAppHeight());
                        this.repaint();
                    }
                    catch(Exception e)
                    {
                        System.out.println("Saved File didn't have any information on app size or location.");
                    }

                    // New 21 August 2009  -- saved look and feel - apply
                    try
                    {
                        String laf = openClass.getLookFeelString();
                        if(laf != null)
                        {
                            LafChanger.changeLaf(this, laf);
                        }
                        else
                        {
                            System.out.println("Saved File didn't have any look and feel data.");
                        }
                    }
                    catch(Exception e)
                    {
                        System.out.println("Saved File didn't have any or has invalid look and feel data.");
                    }

           
                    setStatusMessage("Opened file: " + file.getAbsolutePath());

                    in.close(); //c lose file
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "Error Opening File: Incorrect file format, may be corrupt or an old version", "OPEN ERROR", JOptionPane.ERROR_MESSAGE);
                    setStatusMessage("Error Opening File: Incorrect file format");
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error Opening File: " + e.toString(), "OPEN ERROR", JOptionPane.ERROR_MESSAGE);
                setStatusMessage("Error Opening File: " + e.toString());
            }
        } // file selected

    } // openFile
    
    // returns if a file was selected (true) or canceled (false)
    public void saveAppAs()
    {
        final JFileChooser fc = new JFileChooser(getFileSaveAs());
        CustomFileFilter xmlFilter = new CustomFileFilter("jst","*.jst");
        fc.addChoosableFileFilter(xmlFilter);
        
        int returnVal = fc.showSaveDialog(this);
        
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            
            String extension = getExtension(file);
            if (extension != null)
            {
                fileSaveAs = file.getAbsolutePath();
            }
            else
            {
                // append the extension
                file = new File(file.getAbsolutePath() + ".jst");
                fileSaveAs = file.getAbsolutePath();
            }
            
            fileSaveAs = file.getAbsolutePath();
            
            // run save app:
            saveApp(getFileSaveAs());
            
        } // if approve
        
    }// saveAppAs

    public String getVersionString()
    {
        return versionString;
    }

    public void setVersionString(String versionString)
    {
        this.versionString = versionString;
    }

    public int getCurrentTimeStepSpeedIndex()
    {
        return currentTimeStepSpeedIndex;
    }

    public void setCurrentTimeStepSpeedIndex(int currentTimeStepSpeedIndex)
    {
        this.currentTimeStepSpeedIndex = currentTimeStepSpeedIndex;
    }
    
    public JDesktopPane getJDesktopPane()
    {
        return mainDesktopPane;
    }

    public Hashtable<String, AbstractSatellite> getSatHash()
    {
        return satHash;
    }
    
    public int[] getSatListWHXY()
    {
        return new int[] {satListInternalFrame.getWidth(),satListInternalFrame.getHeight(),satListInternalFrame.getX(),satListInternalFrame.getY()};
    }
    
    public boolean isRealTimeMode()
    {
        return realTimeModeCheckBox.isSelected();
    }
    
    public void setRealTimeMode(boolean bool)
    {
        if(bool == realTimeModeCheckBox.isSelected())
        {
            // already the same
        }
        else
        {
            realTimeModeCheckBox.doClick();
        }
    }
    
    public boolean isLoalTimeModeSelected()
    {
        return localTimeZoneCheckBox.isSelected();
    }
    
    public void setLocalTimeModeSelected(boolean bool)
    {
        if(bool == localTimeZoneCheckBox.isSelected())
        {
            // already the same
        }
        else
        {
            localTimeZoneCheckBox.doClick();
        }
    }
    
    // Window Listner Override methods
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) 
    {
                
        try  // try to cast frame contents as a J3DEarthPanel -- to see if it is that type
        {
            J3DEarthPanel closedEarthPanel = (J3DEarthPanel)(((JDialog)e.getSource()).getContentPane());
            threeDWindowVec.remove( closedEarthPanel ); // remove from Vector
            
            //System.out.println("3D Window Count = " + threeDWindowVec.size());
            
            System.gc(); // force garbage collection after getting rid of that window
            
        }
        catch(Exception ee)
        {
              
            try // close Command Console Window
            {                
                if( ((JDialog)e.getSource()).getContentPane() instanceof JConsole)
                {
                    // enable menu item again
                    commandShellMenuItem.setEnabled(true);
                }
                
                // something else closed
                
            }
            catch(Exception eee)
            {
                // something else closed
                // note the last one wouldn't cause a problem potentially...!
            }
  
        }

    } // windowClosed
    
    
    public double getCurrentTimeStep()
    {
        return timeStepSpeeds[currentTimeStepSpeedIndex];
                
    }
    
    
    public void setTime(long millisecs)
    {
        currentJulianDate.set(millisecs);
        
        // update maps ----------------
        // set animation direction = 0
        currentPlayDirection = 0;
        // update graphics
        updateTime();
    }
    
    /**
     * Set the current time of the app.
     * @param julianDate Julian Date
     */
    public void setTime(double julianDate)
    {
        GregorianCalendar gc = Time.convertJD2Calendar(julianDate);
        setTime(gc.getTimeInMillis());        
    }
    
    public Time getCurrentJulianDate()
    {
        return currentJulianDate;
    }

    public boolean isEpochTimeEqualsCurrentTime()
    {
        return epochTimeEqualsCurrentTime;
    }

    public void setEpochTimeEqualsCurrentTime(boolean epochTimeEqualsCurrentTime)
    {
        this.epochTimeEqualsCurrentTime = epochTimeEqualsCurrentTime;
    }

    public Time getScenarioEpochDate()
    {
        return scenarioEpochDate;
    }

    public void setScenarioEpochDate(Time scenarioEpochDate)
    {
        this.scenarioEpochDate = scenarioEpochDate;
    }
    
    
    /**
     * Sets the text of the status bar and resets the timer for the message to be erased
     * @param msg message to be displayed in the status bar
     */
    public void setStatusMessage(String msg)
    {
        statusLabel.setText(msg);
        messageTimer.restart();
        
        // so this shows up in console
        System.out.println(msg);
    }
    
    /**
     * Starts Statusbar Animation
     */
    public void startStatusAnimation()
    {
        statusAnimationLabel.setIcon(busyIcons[0]);
        busyIconIndex = 0;
        busyIconTimer.start();
    }
    
    /**
     * Stops Statusbar Animation
     */
    public void stopStatusAnimation()
    {
        busyIconTimer.stop();
        statusAnimationLabel.setIcon(idleIcon);
    }
    
    public void setStatusProgressBarVisible(boolean visiable)
    {
        statusProgressBar.setVisible(visiable);
    }
    public void setStatusProgressBarText(String txt)
    {
        statusProgressBar.setString(txt);
    }
    
    public void setStatusProgressBarValue(int value)
    {
        statusProgressBar.setValue(value);
    }

    public Hashtable<String, GroundStation> getGsHash()
    {
        return gsHash;
    }

    public // running count (for naming)
    Vector<J3DEarthPanel> getThreeDWindowVec()
    {
        return threeDWindowVec;
    }

    public void setThreeDWindowVec(Vector<J3DEarthPanel> threeDWindowVec)
    {
        this.threeDWindowVec = threeDWindowVec;
    }

    public

    // WWJ online/offline mode
    boolean isWwjOfflineMode() {
        return wwjOfflineMode;
    }

    public void setWwjOfflineMode(boolean wwjOfflineMode)
    {
        this.wwjOfflineMode = wwjOfflineMode;
    }

//    public void setGsHash(Hashtable<String, GroundStation> gsHash)
//    {
//        this.gsHash = gsHash;
//    }
    
    /**
     * Display the console window
     * @param show
     */
    public void showConsoleWindow(boolean show)
    {
        console.setVisible(show);
    }
    
    public ConsoleDialog getConsole()
    {
        return console;
    }

    public String getFileSaveAs()
    {
        return fileSaveAs;
    }
    
    // action event for plugins run this method
    protected void runPluginAction(ActionEvent e) 
    {
        String fileName = ((JMenuItem)e.getSource()).getText();
        // new way
        runScript("plugins/" + fileName + ".bsh");

//        // old way
//        System.out.println("Running Plugin: " + fileName);
//
//        // runnning bash script
//        try
//        {
//            beanShellInterp.source("plugins/" + fileName + ".bsh");
//        }
//        catch(Exception ee)
//        {
//            System.out.println("Error running plugin script: " + ee.toString());
//            JOptionPane.showMessageDialog(this, "Error running plugin script: \n" + ee.toString(), "Pluging Error", JOptionPane.ERROR_MESSAGE);
//        }
    } // runPluginAction

    /**
     *
     * @param scriptFilePath full or relative path to the script file to be executed
     */
    public void runScript(String scriptFilePath)
    {
        System.out.println("Running Plugin: " + scriptFilePath);

        // runnning bash script
        try
        {
            beanShellInterp.source( scriptFilePath );
        }
        catch(Exception ee)
        {
            System.out.println("Error running plugin script (" + scriptFilePath + "): " + ee.toString());
            JOptionPane.showMessageDialog(this, "Error running plugin script (" + scriptFilePath + "): \n" + ee.toString(), "Pluging Error", JOptionPane.ERROR_MESSAGE);
        }
    } // runScript

//    // moved to 3D views
//    public double getFarClippingPlaneDist()
//    {
//        return farClippingPlaneDist;
//    }
//
//    public void setFarClippingPlaneDist(double farClippingPlaneDist)
//    {
//        this.farClippingPlaneDist = farClippingPlaneDist;
//        // now set all 3d windows:
//        for(J3DEarthInternalPanel panel : threeDInternalWindowVec)
//        {
//            panel.setFarClipDistance(farClippingPlaneDist);
//        }
//        for(J3DEarthPanel panel : threeDWindowVec)
//        {
//            panel.setFarClipDistance(farClippingPlaneDist);
//        }
//    }

// moved to the indivulde 3d views
//    public double getNearClippingPlaneDist()
//    {
//        return nearClippingPlaneDist;
//    }
//
//    public void setNearClippingPlaneDist(double nearClippingPlaneDist)
//    {
//        this.nearClippingPlaneDist = nearClippingPlaneDist;
//        // now set all 3d windows:
//        for(J3DEarthInternalPanel panel : threeDInternalWindowVec)
//        {
//            panel.setNearClipDistance(nearClippingPlaneDist);
//        }
//        for(J3DEarthPanel panel : threeDWindowVec)
//        {
//            panel.setNearClipDistance(nearClippingPlaneDist);
//        }
//    }

    public CoverageAnalyzer getCoverageAnalyzer()
    {
        return coverageAnalyzer;
    }
    
    
    public void addCustomSat()
    {
        // start status bar animation
        startStatusAnimation();
        
        // get a name from the user:
        String name = JOptionPane.showInputDialog(this,"Custom Satellite Name");
        
         stopStatusAnimation();
         
         // call overloaded function with name
         addCustomSat(name);
    }
    
    public void addCustomSat(String name)
    {
        // add new custom sat to the list
        // is not already in list
        // add to hashTable
        
        
        
        // if nothing given:
        if(name == null || name.equalsIgnoreCase(""))
        {
            //System.out.println("returned");
            this.setStatusMessage("Custom Satellite Canceled: Either by user or not supplying a name.");
            return;
        }
        
        CustomSatellite prop = new CustomSatellite(name,this.getScenarioEpochDate());
        
        satHash.put(name, prop);

        // set satellite time to current date
        prop.propogate2JulDate(this.getCurrentJulTime());

        // add item to the Object list tree
        objListPanel.addSat2List(prop);
        
//        //topSatTreeNode.add( new IconTreeNode(name) );
//        IconTreeNode newNode = new IconTreeNode(name);
//        // assign icon to node
//        newNode.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/custom/sat_icon_cst.png"))));
//
//        treeModel.insertNodeInto(newNode, topSatTreeNode, topSatTreeNode.getChildCount());
//
//
//        //System.out.println("node added: " + name);                   
//        objectTree.scrollPathToVisible(getPath(newNode));
        
        this.setStatusMessage("Custom Satellite Added: " + name );
        // open properties panel
        objListPanel.openCurrentOptions(prop);
    }

    public // 3D windows
    Vector<J3DEarthInternalPanel> getThreeDInternalWindowVec()
    {
        return threeDInternalWindowVec;
    }

    public // -1 value Means auto adjusting
    // WorldWindGLCanvas so all 3D windows can share resources like 3D models
    WorldWindowGLCanvas getWwd()
    {
        // if current wwd is null - create one!
        if(wwd == null)
        {
            wwd = new WorldWindowGLCanvas(); // make first one
        }
        
        return wwd;
    } // get wwd

    public double getFpsAnimation()
    {
        return fpsAnimation;
    }
    
    public Vector<J2DEarthPanel> getTwoDWindowVec()
    {
        return twoDWindowVec;
        
    }
    
    public J2DEarthPanel getNonDisplayed2DEarthPanel(int height)
    {
        int width = 2*height;
        // create panel
        J2DEarthPanel earthPanel = new J2DEarthPanel(satHash, gsHash, zoomIntoggleButton, zoomOuttoggleButton, recenterToggleButton, currentJulianDate, sun, this);  
        // insure that the panel is the correct size and all background painting has been done
        earthPanel.getImageMap().setSize(width,height);
        earthPanel.setSize(width, height);
        earthPanel.rescaleAndSetBackgroundImage();
        // return
        return earthPanel;
        
    }


    /**
     * @param args the command line arguments
     *
     * if run with no command line parameters the GUI is displayed
     * if run with a command line argument with the path of a script it will be executed
     * example : java JSatTrak runme.bsh  > satLog.txt  (will run the script runme.bsh and save output to the satLog.txt file)
     */
    public static void main(final String args[])
    {
        
        // no command line arguments
        if(args.length == 0)
        {


           java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {

                    new JSatTrak().setVisible(true);

                } // run
            });
        }
        else // no GUI
        {
            // see if command line is a exisiting file (assume it is a .bsh file and try to run it)
            File inScriptFile = new File(args[0]);
            if(inScriptFile.exists())
            {
                long t1 = System.currentTimeMillis();
                JSatTrak app = new JSatTrak();
                // runnning bash script
                try
                {
                    app.beanShellInterp.source(args[0]);
                }
                catch(Exception ee)
                {
                    System.err.println("Error running script: " + ee.toString());
                }

                // close JSatTrakApp
                app.dispose();

                long dt = System.currentTimeMillis() - t1;
                System.err.println("Time to Execute (sec): " + dt / 1000.0);

            } // file exsists
            else
            {
                System.err.println("File does not exist: " + inScriptFile.getAbsolutePath());
            }

            System.exit(0); // exit

        }// program ran with line args

    } // main

    /**
     * @return the sun
     */
    public Sun getSun()
    {
        return sun;
    }

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

}
