/*
 * J2DEarthPanelSave.java
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
 * Created on August 20, 2007, 12:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jsattrak.utilities;

import jsattrak.gui.J2DEarthPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JInternalFrame;
import jsattrak.coverage.JSatTrakRenderable;

/**
 *
 * @author sgano
 */
public class J2DEarthPanelSave implements Serializable
{
    private int xPos; // location of window
    private int yPos;
    
    private int width;
    private int height;
    
    // panel options
    private Color backgroundColor;
    //private int imageMapNum;
    private int imageScalingOption;
    private String backgroundImagePath;
    
    //private JulianDay currentTime;
    // label options
    private boolean showDateTime;
    private int xDateTimeOffset;
    private int yDateTimeOffset;
    private Color dateTimeColor;
    private boolean showLatLong;
    private double centerLat; // center latitude
    private double centerLong; // center longitude
    private double zoomFactor; // current zoom factor (>= 1.0)
    private double zoomIncrementMultiplier;
    
    // sun drawing options
    private boolean drawSun;
    private Color sunColor;//Color.BLACK;
    private int numPtsSunFootPrint;
    private float sunAlpha;
    private boolean showEarthLightsMask;
    
    // region drawing
    private Color landOutlineColor;
    private String dataFileName;
    private boolean showLandMassOutlines;

    // renderable objects
    Vector<JSatTrakRenderable> renderableObjects; //added 21 March 2009 -- bug to save coverage anaylsis and other renderables

    
    /** Creates a new instance of J2DEarthPanelSave */
    public J2DEarthPanelSave(J2DEarthPanel panel, int x, int y, Dimension d)
    {
        xPos = x;
        yPos = y;
        
        width = d.width;
        height = d.height;
        
        //panel options
        backgroundColor = panel.getBackgroundColor();
        //imageMapNum = panel.getImageMapNum();
        backgroundImagePath = panel.getTwoDMap(); // map path
        imageScalingOption = panel.getImageScalingOption();
        //currentTime = ;
        // label options
        showDateTime = panel.isShowDateTime();
        xDateTimeOffset = panel.getXDateTimeOffset();
        yDateTimeOffset = panel.getYDateTimeOffset();
        dateTimeColor = panel.getDateTimeColor();
        showLatLong = panel.getShowLatLonLines();
        centerLat = panel.getCenterLat();
        centerLong = panel.getCenterLong();
        zoomFactor = panel.getZoomFactor();
        zoomIncrementMultiplier = panel.getZoomIncrementMultiplier();
        
        // darkness
        drawSun = panel.isDrawSun();
        sunColor = panel.getSunColor();//Color.BLACK;
        numPtsSunFootPrint = panel.getNumPtsSunFootPrint();
        sunAlpha = panel.getSunAlpha();
        showEarthLightsMask = panel.isShowEarthLightsMask();
        
        // region drawing
        landOutlineColor = panel.getRegionLineColor();
        dataFileName = panel.getRegionFileName();
        showLandMassOutlines = panel.isRegionDrawOn();

        // added 21 March 2009 -- bug to save coverage anaylsis and other renderables
        renderableObjects = panel.getImageMap().getRenderableObjects();
         
    } // J2DEarthPanelSave constructor
    
    // OUTPUT function, save data from this object back to a new j2dearthpanel and internal frame
    public void copySettings2PanelAndFrame(J2DEarthPanel newPanel, JInternalFrame iframe)
    {
        // set size and location
        iframe.setSize(width,height);
        iframe.setLocation(xPos,yPos);
        
        // set panel options
        newPanel.setBackgroundColor(backgroundColor);
        //newPanel.setImageMapNum(imageMapNum); 
        newPanel.setTwoDMap(backgroundImagePath);
        newPanel.setImageScalingOption(imageScalingOption);
        
        // set label options
        newPanel.setShowDateTime(showDateTime);
        newPanel.setXDateTimeOffset(xDateTimeOffset);
        newPanel.setYDateTimeOffset(yDateTimeOffset);
        newPanel.setDateTimeColor(dateTimeColor);
        newPanel.setShowLatLonLines(showLatLong);
        newPanel.setCenterLat(centerLat);
        newPanel.setCenterLong(centerLong);
        newPanel.setZoomFactor(zoomFactor);
        newPanel.setZoomIncrementMultiplier(zoomIncrementMultiplier);
        
        // darkness
        newPanel.setDrawSun(drawSun);
        newPanel.setSunColor(sunColor);
        newPanel.setNumPtsSunFootPrint(numPtsSunFootPrint);
        newPanel.setSunAlpha(sunAlpha);
        newPanel.setShowEarthLightsMask(showEarthLightsMask);
        
         // region drawing
        newPanel.setRegionDrawingOptions(showLandMassOutlines, dataFileName, landOutlineColor);

        //added 21 March 2009 -- bug to save coverage anaylsis and other renderables
        try
        {
            for (JSatTrakRenderable v : renderableObjects)
            {
                newPanel.addRenderableObject(v);
            }
        } catch (Exception e)
        {
            System.out.println("Saved File didn't contain any 2D renderable objects");
        }

    }  
} //J2DEarthPanelSave class
