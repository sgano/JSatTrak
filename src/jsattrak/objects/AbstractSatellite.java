/*
 * Abstract Class for Satellite Types
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

package jsattrak.objects;

import java.awt.Color;
import java.io.Serializable;
import jsattrak.utilities.TLE;
import name.gano.worldwind.modelloader.WWModel3D_new;

/**
 *
 * @author sgano
 */
public abstract class AbstractSatellite implements Serializable 
{

    /**
     * Calculate J2K position of this sat at a given JulDateTime (doesn't save the time) - can be useful for event searches or optimization
     * @param julDate - julian date
     * @return j2k position of satellite in meters
     */
    public abstract double[] calculateJ2KPositionFromUT(double julDate);

    /**
     * Calculate TEME of date position of this sat at a given JulDateTime (doesn't save the time) - can be useful for event searches or optimization
     * @param julDate - julian date
     * @return j2k position of satellite in meters
     */
    public abstract double[] calculateTemePositionFromUT(double julDate);

    public abstract double getAltitude();

    public abstract double getCurrentJulDate();

    public abstract int getGrnTrkPointsPerPeriod();

    public abstract boolean getGroundTrackIni();

    public abstract double getGroundTrackLagPeriodMultiplier();

    public abstract double getGroundTrackLeadPeriodMultiplier();

    public abstract double[] getGroundTrackLlaLagPt(int index);

    public abstract double[] getGroundTrackLlaLeadPt(int index);

    public abstract double[] getGroundTrackXyzLagPt(int index);

    public abstract double[] getGroundTrackXyzLeadPt(int index);

    public abstract double[] getJ2000Position();

    public abstract double[] getJ2000Velocity();

    public abstract double[] getKeplarianElements();

    public abstract double[] getLLA();

    public abstract double getLatitude();

    public abstract double getLongitude();

    public abstract double[][] getTemePosLag();

    public abstract double[][] getTemePosLead();

    public abstract String getName();

    public abstract int getNumGroundTrackLagPts();

    public abstract int getNumGroundTrackLeadPts();

    public abstract int getNumPtsFootPrint();

    public abstract double getPeriod();

    public abstract boolean getPlot2D();

    public abstract boolean getPlot2DFootPrint();

    public abstract double[] getTEMEPos();

    public abstract Color getSatColor();

    public abstract double getSatTleEpochJulDate();

    public abstract boolean getShowGroundTrack();

    public abstract double[] getTimeLag();

    public abstract double[] getTimeLead();

    public abstract double getTleAgeDays();

    public abstract double getTleEpochJD();

    public abstract boolean isFillFootPrint();

    public abstract boolean isShow3D();

    public abstract boolean isShow3DFootprint();

    public abstract boolean isShow3DName();

    public abstract boolean isShow3DOrbitTrace();

    public abstract boolean isShow3DOrbitTraceECI();

    public abstract boolean isShowGroundTrack3d();

    public abstract boolean isShowName2D();

    public abstract void propogate2JulDate(double julDate);

    public abstract void setFillFootPrint(boolean fillFootPrint);

    public abstract void setGrnTrkPointsPerPeriod(int grnTrkPointsPerPeriod);

    public abstract void setGroundTrackIni2False();

    public abstract void setGroundTrackLagPeriodMultiplier(double groundTrackLagPeriodMultiplier);

    public abstract void setGroundTrackLeadPeriodMultiplier(double groundTrackLeadPeriodMultiplier);

    public abstract void setNumPtsFootPrint(int numPtsFootPrint);

    public abstract void setPlot2DFootPrint(boolean plot2DFootPrint);

    public abstract void setPlot2d(boolean plot2d);

    public abstract void setSatColor(Color satColor);

    public abstract void setShow3D(boolean show3D);

    public abstract void setShow3DFootprint(boolean show3DFootprint);

    public abstract void setShow3DName(boolean show3DName);

    public abstract void setShow3DOrbitTrace(boolean show3DOrbitTrace);

    public abstract void setShow3DOrbitTraceECI(boolean show3DOrbitTraceECI);

    public abstract void setShowGroundTrack(boolean showGrndTrk);

    public abstract void setShowGroundTrack3d(boolean showGroundTrack3d);

    public abstract void setShowName2D(boolean showName2D);

    public abstract void updateTleData(TLE newTLE);
    
    public abstract boolean isUse3dModel();
    
    public abstract void setUse3dModel(boolean use3dModel);
    
    public abstract String getThreeDModelPath();
    
    public abstract void setThreeDModelPath(String path);
    
    public abstract WWModel3D_new getThreeDModel();
    
    public abstract double[] getTEMEVelocity();
    
    public abstract double getThreeDModelSizeFactor();
    
    public abstract void setThreeDModelSizeFactor(double modelSizeFactor);
    

}
