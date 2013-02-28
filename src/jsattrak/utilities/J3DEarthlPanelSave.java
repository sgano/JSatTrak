/*
 * J3DEarthPanelSave.java
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
 * Created on 15 August 2007
 *
 */

package jsattrak.utilities;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.view.OrbitView;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.io.Serializable;
import java.util.Hashtable;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;

/**
 *
 * @author sgano
 */
public class J3DEarthlPanelSave implements Serializable
{
    private int xPos; // location of window
    private int yPos;
    private int width;
    private int height;
    // panel options
    private boolean viewModeECI; // view mode - ECI (true) or ECEF (false)
    private double fovDeg; // field of view  in degrees
    // terrain profiler
    private boolean showTerrainProfiler;
    private String terrainProfileSat;
    private double terrainProfileLongSpan;
    // 3D view point
    String viewStateInXml;
    // title of frame
    String frameTitle = "";
    // layer list and if they are visible
    Hashtable<String, Boolean> layerEnabledHT;
    // web map services
    // view mode options
    private boolean modelViewMode; // default false
    private String modelViewString; // to hold name of satellite to view when modelViewMode=true
    private double modelViewNearClip; // clipping pland for when in Model View mode
    private double modelViewFarClip;
    private boolean smoothViewChanges; // for 3D view smoothing
    // orbit clipping plans
    private double farClippingPlaneDistOrbit;
    private double nearClippingPlaneDistOrbit;

    // sun shading
    private boolean sunShadingOn;
    private int ambientLightLevel;
    private boolean lensFlareEnabled;
    private boolean showTermintorLine;
    private Color terminatorColor;

    // ECI raidal grid
    private boolean showEciRadialGrid;
    private Color eciRadialGridColor;

    
    /** Creates a new instance of J2DEarthPanelSave */
    public J3DEarthlPanelSave(J3DEarthComponent panel, int x, int y, Dimension d)
    {
        xPos = x;
        yPos = y;

        width = d.width;
        height = d.height;

        //panel options
        viewModeECI = panel.isViewModeECI();
        fovDeg = panel.getWwd().getView().getFieldOfView().getDegrees();

        showTerrainProfiler = panel.getTerrainProfileEnabled();
        terrainProfileSat = panel.getTerrainProfileSat();
        terrainProfileLongSpan = panel.getTerrainProfileLongSpan();

        // gets a hashtable with all the layers names and if they are enabled - but no other properties
        // this won't allow opening of user added layers unfortunaly but not sure how to do that.
        // saving LayerList doesn't work.
        layerEnabledHT = new Hashtable<String, Boolean>();
        for(Layer l : panel.getLayerList())
        {
            layerEnabledHT.put(l.getName(), l.isEnabled());
        }

        // 3D view camera location and 
        // UPDATE using v0.5
        viewStateInXml = ((OrbitView)panel.getWwd().getView()).getRestorableState();

        // save view type (model) options
        modelViewMode = panel.isModelViewMode(); // default false
        modelViewString = panel.getModelViewString(); // to hold name of satellite to view when modelViewMode=true
        modelViewNearClip = panel.getModelViewNearClip(); // clipping pland for when in Model View mode
        modelViewFarClip = panel.getModelViewFarClip();
        //orbit view clipping planes
        farClippingPlaneDistOrbit = panel.getOrbitFarClipDistance();
        nearClippingPlaneDistOrbit = panel.getOrbitNearClipDistance();

        // save title
        frameTitle = panel.getDialogTitle();

        // smooth
        smoothViewChanges = panel.isSmoothViewChanges();

        // sun shading
        sunShadingOn = panel.isSunShadingOn();
        ambientLightLevel = panel.getAmbientLightLevel();
        lensFlareEnabled = panel.isLensFlareEnabled();

        // terminator
        showTermintorLine = panel.getEcefTimeDepRenderableLayer().isShowTerminatorLine();
        terminatorColor = panel.getEcefTimeDepRenderableLayer().getTerminator().getColor();

        // grid
        showEciRadialGrid = panel.getEciRadialGrid().isShowGrid();
        eciRadialGridColor = panel.getEciRadialGrid().getColor();
        
    } // J2DEarthPanelSave constructor
    
    // OUTPUT function, save data from this object back to a new j2dearthpanel and internal frame
    public void copySettings2PanelAndFrame(J3DEarthComponent newPanel, Container iframe)
    {
        // set size and location
        iframe.setSize(width, height);
        iframe.setLocation(xPos, yPos);

        // set propties and options - like FOV and terrain
        newPanel.setViewModeECI(viewModeECI);
        newPanel.setTerrainProfileEnabled(showTerrainProfiler);
        newPanel.setTerrainProfileSat(terrainProfileSat);
        newPanel.setTerrainProfileLongSpan(terrainProfileLongSpan);

        // smooth view
        try
        {
            newPanel.setSmoothViewChanges(smoothViewChanges);
        }
        catch(Exception e)
        {
            System.out.println("Error loading smooth view changes");
        }


        // set FOV to WWJ model
        newPanel.getWwd().getView().setFieldOfView(Angle.fromDegrees(fovDeg));


        // set layer list enabled/disabled (using current layers in the object)
        for(Layer l : newPanel.getLayerList())
        {
            if(layerEnabledHT.containsKey(l.getName())) // if the saved has contains the name of this layer then set its properties
            {
                l.setEnabled(layerEnabledHT.get(l.getName()));
            }
        }

        // sun shading
        try
        {
            newPanel.setSunShadingOn(sunShadingOn);
            newPanel.setAmbientLightLevel(ambientLightLevel);
            newPanel.setLensFlare(lensFlareEnabled);
        }
        catch(Exception e)
        {
            System.out.println("Error loading sun shading options");
        }

        // view mode options
        newPanel.setModelViewString(modelViewString); // to hold name of satellite to view when modelViewMode=true
        newPanel.setModelViewNearClip(modelViewNearClip); // clipping pland for when in Model View mode
        newPanel.setModelViewFarClip(modelViewFarClip);
        newPanel.setModelViewMode(modelViewMode);  // set last! (in model view mode section)
        try
        {
            //orbit view clipping planes
            newPanel.setOrbitFarClipDistance(farClippingPlaneDistOrbit);
            newPanel.setOrbitNearClipDistance(nearClippingPlaneDistOrbit);
        }
        catch(Exception e)
        {
            System.out.println("Error loading Orbit Clipping Planes");
        }

        // set panel options

        // restor view using xml state
        try
        {
            ((OrbitView)newPanel.getWwd().getView()).restoreState(viewStateInXml);
        }
        catch(Exception e)
        {
            System.out.println("Error loading 3D view State: " + e.toString());
        }


        newPanel.getWwd().redraw();

        // set TITLE
        if(iframe instanceof JInternalFrame) // internal
        {
            ((JInternalFrame)iframe).setTitle(frameTitle);
        }
        else if(iframe instanceof JDialog) // external
        {
            ((JDialog)iframe).setTitle(frameTitle);
        }

        // terminator
        newPanel.getEcefTimeDepRenderableLayer().getTerminator().setColor(terminatorColor);
        newPanel.getEcefTimeDepRenderableLayer().setShowTerminatorLine(showTermintorLine);

        // grid
        try
        {
            newPanel.getEciRadialGrid().setShowGrid(showEciRadialGrid);
            newPanel.getEciRadialGrid().setColor(eciRadialGridColor);
        }
        catch(Exception e)
        {
            System.out.println("Error loading ECI Grid options");
        }

    } // copy settings
    
} //J3DEarthInternalPanelSave class
