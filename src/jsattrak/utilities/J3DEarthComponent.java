/*
 *  Interface for 3D view windows (both internal and dialogs)
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
 */

package jsattrak.utilities;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LayerList;
import java.awt.Point;
import jsattrak.gui.JSatTrak;
import name.gano.worldwind.geom.ECIRadialGrid;
import name.gano.worldwind.layers.Earth.EcefTimeDepRenderableLayer;

/**
 *
 * @author sgano
 */
public interface  J3DEarthComponent 
{
    public boolean getTerrainProfileEnabled();
    public String getTerrainProfileSat();
    public double getTerrainProfileLongSpan();
    public void setTerrainProfileSat(String terrainProfileSat);
    public void setTerrainProfileLongSpan(double terrainProfileLongSpan);
    public void setTerrainProfileEnabled(boolean enabled);
    public boolean isViewModeECI();
    public void setViewModeECI(boolean viewModeECI);
    public JSatTrak getApp();
    public WorldWindow getWwd();
    public String getDialogTitle();
    public int getWwdWidth();
    public int getWwdHeight();
    public Point getWwdLocationOnScreen();
    public LayerList getLayerList();
    public void setOrbitFarClipDistance(double clipDist);
    public void setOrbitNearClipDistance(double clipDist);
    public double getOrbitFarClipDistance();
    public double getOrbitNearClipDistance();
    public boolean isModelViewMode();
    public void setModelViewMode(boolean modelViewMode);
    public String getModelViewString();
    public void setModelViewString(String modelViewString);
    public double getModelViewNearClip();
    public void setModelViewNearClip(double modelViewNearClip);
    public double getModelViewFarClip();
    public void setModelViewFarClip(double modelViewFarClip);
    public void resetWWJdisplay();
    public boolean isSmoothViewChanges();
    public void setSmoothViewChanges(boolean smoothViewChanges);
    // sun shading
    public void setSunShadingOn(boolean useSunShading);
    public boolean isSunShadingOn();
    public void setAmbientLightLevel(int level);
    public int getAmbientLightLevel();
    public boolean isLensFlareEnabled();
    public void setLensFlare(boolean enabled);
    public EcefTimeDepRenderableLayer getEcefTimeDepRenderableLayer();
    // grid
    public ECIRadialGrid getEciRadialGrid();

}
