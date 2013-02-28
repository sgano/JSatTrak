/*
 * JstSaveClass.java
 *=====================================================================
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
 * Created on August 20, 2007, 12:11 PM
 *
 * Main saving class - stores all the info from app to this class for serializable saving / loading
 * //
 * // - saves windows in reverse order
 */

package jsattrak.utilities;

import java.awt.Point;
import jsattrak.gui.J2DEarthPanel;
import jsattrak.gui.JSatTrak;
import jsattrak.gui.SatPropertyPanel;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import jsattrak.coverage.CoverageAnalyzer;
import jsattrak.gui.J3DEarthInternalPanel;
import jsattrak.gui.J3DEarthPanel;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import name.gano.astro.time.Time;

/**
 *
 * @author sgano
 */
public class JstSaveClass implements Serializable
{
    private // app loacation and size
    Point screenLoc;
    private int appWidth;
    private int appHeight;

    // main satHash to save
    private Hashtable<String,AbstractSatellite> satHash;
    private Hashtable<String,GroundStation> gsHash;
    
    // vectors for the windows to save:
    private Vector<J2DEarthPanelSave> twoDWindowSaveVec = new Vector<J2DEarthPanelSave>(); // 2D Panels
    private Vector<SatPropertyPanelSave> satPropWindowSaveVec = new Vector<SatPropertyPanelSave>(); // Property Panels
    private Vector<J3DEarthlPanelSave> threeDWindowSaveVec = new Vector<J3DEarthlPanelSave>(); // 3D Windows (internal)
    private Vector<J3DEarthlPanelSave> threeDExtWindowSaveVec = new Vector<J3DEarthlPanelSave>(); // 3d Windows (external)
    
    // other data to save and options to save
    private Time currentJulianDate;
    private String versionString;
    private int realTimeAnimationRefreshRateMs; // refresh rate for real time animation
    private int nonRealTimeAnimationRefreshRateMs; // refresh rate for non-real time animation
    private int currentTimeStepSpeedIndex;
    private boolean realTimeMode;
    private boolean localTimeZoneSelected;
    
    private int satListWidth;
    private int satListHeight;
    private int satListX;
    private int satListY;
    
    // scenario epoch
    private boolean epochTimeEqualsCurrentTime; // uses current time for scenario epoch (reset button)
    private Time scenarioEpochDate;
    
    // wwj offline mode
    private boolean wwjOfflineMode;
    
    // coverage data
    private CoverageAnalyzer ca;

    // string for the look and feel used
    private String lookFeelString = "";
            
    /** Creates a new instance of JstSaveClass */
    public JstSaveClass(JSatTrak app)
    {
        // save hash  -- LARGE SAVE FILE due to saving of SDP4 classes, only TLE text really needed!
        satHash = app.getSatHash();
        gsHash = app.getGsHash();
        
        // get and save app data
        currentJulianDate = app.getCurrentJulianDay();
        versionString = app.getVersionString();
        realTimeAnimationRefreshRateMs = app.getRealTimeAnimationRefreshRateMs();
        nonRealTimeAnimationRefreshRateMs = app.getNonRealTimeAnimationRefreshRateMs();
        currentTimeStepSpeedIndex = app.getCurrentTimeStepSpeedIndex(); 
        realTimeMode = app.isRealTimeMode();
        localTimeZoneSelected = app.isLoalTimeModeSelected();
        
        // save data for sat list location
        int whxy[] = app.getSatListWHXY();
        satListWidth = whxy[0];
        satListHeight = whxy[1];
        satListX = whxy[2];
        satListY = whxy[3];
        
        // save scenario epoch data
        epochTimeEqualsCurrentTime = app.isEpochTimeEqualsCurrentTime();
        scenarioEpochDate = app.getScenarioEpochDate();
        
        // offline mode
        wwjOfflineMode = app.isWwjOfflineMode();
        
        
        // save data for window minimize state?

        // get and save data about each window that is open
        JDesktopPane jdp = app.getJDesktopPane(); 
        
        for(JInternalFrame jif : jdp.getAllFrames())
        {
            if( jif.getContentPane() instanceof J2DEarthPanel  )
            {
                // 2d panel
                twoDWindowSaveVec.add( new J2DEarthPanelSave( (J2DEarthPanel)jif.getContentPane(), jif.getX(), jif.getY(), jif.getSize() ));
            }
            else if( jif.getContentPane() instanceof SatPropertyPanel)
            {
                // property panel
                satPropWindowSaveVec.add( new SatPropertyPanelSave((SatPropertyPanel)jif.getContentPane(), jif.getX(), jif.getY(), jif.getSize() ));
            }
            else if( jif.getContentPane() instanceof J3DEarthInternalPanel)
            {
                // property panel
                threeDWindowSaveVec.add( new J3DEarthlPanelSave((J3DEarthInternalPanel)jif.getContentPane(), jif.getX(), jif.getY(), jif.getSize() ));
            }
            
        } // all frames
        
        // all 3D external frames
        for(J3DEarthPanel pan : app.getThreeDWindowVec())
        {
            Point p = pan.getParentDialog().getLocationOnScreen(); // get size/shape of entire dialog
            threeDExtWindowSaveVec.add( new J3DEarthlPanelSave(pan, p.x, p.y, pan.getParentDialog().getSize() )  );
        }
        
        
        // coverage anaylzer
        ca = app.getCoverageAnalyzer();

        // screen location and size of entire app  -- NEW 20 March 2009
        screenLoc = app.getLocationOnScreen();
        appWidth = app.getWidth();
        appHeight = app.getHeight();

        // save look and feel
        //LookAndFeel lf = UIManager.getLookAndFeel();
        lookFeelString = UIManager.getLookAndFeel().getClass().toString().substring(6); // remove the 'class ' at begining of string

    } // JstSaveClass  constructor

    public Hashtable<String, AbstractSatellite> getSatHash()
    {
        return satHash;
    }

    public void setSatHash(Hashtable<String, AbstractSatellite> satHash)
    {
        this.satHash = satHash;
    }

    public Vector<J2DEarthPanelSave> getTwoDWindowSaveVec()
    {
        return twoDWindowSaveVec;
    }

    public void setTwoDWindowSaveVec(Vector<J2DEarthPanelSave> twoDWindowSaveVec)
    {
        this.twoDWindowSaveVec = twoDWindowSaveVec;
    }

    public Vector<SatPropertyPanelSave> getSatPropWindowSaveVec()
    {
        return satPropWindowSaveVec;
    }

    public void setSatPropWindowSaveVec(Vector<SatPropertyPanelSave> satPropWindowSaveVec)
    {
        this.satPropWindowSaveVec = satPropWindowSaveVec;
    }

    public Time getCurrentJulianDate()
    {
        return currentJulianDate;
    }

    public void setCurrentJulianDate(Time currentJulianDate)
    {
        this.currentJulianDate = currentJulianDate;
    }

    public String getVersionString()
    {
        return versionString;
    }

    public void setVersionString(String versionString)
    {
        this.versionString = versionString;
    }

    public int getRealTimeAnimationRefreshRateMs()
    {
        return realTimeAnimationRefreshRateMs;
    }

    public void setRealTimeAnimationRefreshRateMs(int realTimeAnimationRefreshRateMs)
    {
        this.realTimeAnimationRefreshRateMs = realTimeAnimationRefreshRateMs;
    }

    public int getNonRealTimeAnimationRefreshRateMs()
    {
        return nonRealTimeAnimationRefreshRateMs;
    }

    public void setNonRealTimeAnimationRefreshRateMs(int nonRealTimeAnimationRefreshRateMs)
    {
        this.nonRealTimeAnimationRefreshRateMs = nonRealTimeAnimationRefreshRateMs;
    }

    public int getCurrentTimeStepSpeedIndex()
    {
        return currentTimeStepSpeedIndex;
    }

    public void setCurrentTimeStepSpeedIndex(int currentTimeStepSpeedIndex)
    {
        this.currentTimeStepSpeedIndex = currentTimeStepSpeedIndex;
    }

    public int getSatListWidth()
    {
        return satListWidth;
    }

    public void setSatListWidth(int satListWidth)
    {
        this.satListWidth = satListWidth;
    }

    public int getSatListHeight()
    {
        return satListHeight;
    }

    public void setSatListHeight(int satListHeight)
    {
        this.satListHeight = satListHeight;
    }

    public int getSatListX()
    {
        return satListX;
    }

    public void setSatListX(int satListX)
    {
        this.satListX = satListX;
    }

    public int getSatListY()
    {
        return satListY;
    }

    public void setSatListY(int satListY)
    {
        this.satListY = satListY;
    }

    public boolean isRealTimeMode()
    {
        return realTimeMode;
    }

    public void setRealTimeMode(boolean realTimeMode)
    {
        this.realTimeMode = realTimeMode;
    }

    public boolean isLocalTimeZoneSelected()
    {
        return localTimeZoneSelected;
    }

    public void setLocalTimeZoneSelected(boolean localTimeZoneSelected)
    {
        this.localTimeZoneSelected = localTimeZoneSelected;
    }

    public Vector<J3DEarthlPanelSave> getThreeDWindowSaveVec()
    {
        return threeDWindowSaveVec;
    }

    public void setThreeDWindowSaveVec(Vector<J3DEarthlPanelSave> threeDWindowSaveVec)
    {
        this.threeDWindowSaveVec = threeDWindowSaveVec;
    }

    public Hashtable<String, GroundStation> getGsHash()
    {
        return gsHash;
    }

    public void setGsHash(Hashtable<String, GroundStation> gsHash)
    {
        this.gsHash = gsHash;
    }

    public Vector<J3DEarthlPanelSave> getThreeDExtWindowSaveVec()
    {
        return threeDExtWindowSaveVec;
    }

    public void setThreeDExtWindowSaveVec(Vector<J3DEarthlPanelSave> threeDExtWindowSaveVec)
    {
        this.threeDExtWindowSaveVec = threeDExtWindowSaveVec;
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

    public boolean isWwjOfflineMode() {
        return wwjOfflineMode;
    }

    public void setWwjOfflineMode(boolean wwjOfflineMode) {
        this.wwjOfflineMode = wwjOfflineMode;
    }

    public CoverageAnalyzer getCa()
    {
        return ca;
    }

    public void setCa(CoverageAnalyzer ca)
    {
        this.ca = ca;
    }

    /**
     * @return the screenLoc
     */
    public Point getScreenLoc()
    {
        return screenLoc;
    }

    /**
     * @return the appWidth
     */
    public int getAppWidth()
    {
        return appWidth;
    }

    /**
     * @return the appHeight
     */
    public int getAppHeight()
    {
        return appHeight;
    }

    /**
     * @return the lookFeelString
     */
    public String getLookFeelString()
    {
        return lookFeelString;
    }
    
} // JstSaveClass
