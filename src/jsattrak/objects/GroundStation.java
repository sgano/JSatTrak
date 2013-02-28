/*
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
import java.util.Random;
import name.gano.astro.AstroConst;
import name.gano.astro.MathUtils;
import name.gano.astro.Sidereal;

/**
 *
 * @author sgano
 */
public class GroundStation implements Serializable
{
    // basic atributes
    private String stationName;
    private double[] lla_deg_m; // lat long and alt of station in deg/deg/meters (Geodetic)
    
    private double elevationConst = 10; // 360 deg averaged elevation constraint (in future might want to make this an array around site)
    
    // current time - julian date
    private double currentJulianDate = -1;
    
    // display settings
    private Color stationColor = Color.RED; //
    private boolean show2D = true;
    private boolean show2DName = true;
    private int groundStation2DPixelSize = 6;
    
    private boolean show3D = true;
    private boolean show3DName = true;
    
    
    
    // constructor
    public GroundStation(String name, double[] lla_deg_m, double currentJulianDate)
    {
        this.stationName = name;
        this.lla_deg_m = lla_deg_m;
        this.currentJulianDate = currentJulianDate;
        
        // pick random color
        Random generator = new Random();
        switch( generator.nextInt(6) )
        {
            case 0: stationColor = Color.red; break;
            case 1: stationColor = Color.blue; break;
            case 2: stationColor = Color.green; break;
            case 3: stationColor = Color.white; break;
            case 4: stationColor = Color.yellow; break;
            case 5: stationColor = Color.orange; break;
            default: stationColor = Color.red; break;
        } // random color switch
    } // constructor
    
    // ECI in meters - Uses Earth Flattening; WGS-84
    // theta is pass in as Degrees!! (it is the local mean sidereal time)
    private double[] calculateECIposition(double theta)
    {
        // calculate the ECI j2k position vector of the ground station at the current time
        double [] eciVec = new double[3];
        
//        // calculate geocentric latitude - using non spherical earth (in radians)
//        // http://celestrak.com/columns/v02n03/
//        double  geocentricLat = Math.atan( Math.pow(1.0-AstroConst.f_Earth, 2.0) * Math.tan( lla_deg_m[0]*Math.PI/180.0 )  ); // (1-f)^2 tan(?).
//        
//        eciVec[2] = AstroConst.R_Earth * Math.sin( geocentricLat ); //lla_deg_m[0]*Math.PI/180.0 );
//        double r = AstroConst.R_Earth * Math.cos( geocentricLat ); //lla_deg_m[0]*Math.PI/180.0 );
//        eciVec[0] = r * Math.cos(theta*Math.PI/180.0);
//        eciVec[1] = r * Math.sin(theta*Math.PI/180.0);
        
        // alternate way to calcuate ECI position - using earth flattening
        // http://celestrak.com/columns/v02n03/
        double C = 1.0 / Math.sqrt( 1.0+AstroConst.f_Earth*(AstroConst.f_Earth-2.0)*Math.pow(Math.sin(lla_deg_m[0]*Math.PI/180.0 ),2.0) );
        double S = Math.pow(1.0-AstroConst.f_Earth, 2.0) * C;
        
        eciVec[0] = AstroConst.R_Earth * C * Math.cos(lla_deg_m[0]*Math.PI/180.0)*Math.cos(theta*Math.PI/180.0);
        eciVec[1] = AstroConst.R_Earth * C * Math.cos(lla_deg_m[0]*Math.PI/180.0)*Math.sin(theta*Math.PI/180.0);
        eciVec[2] = AstroConst.R_Earth * S * Math.sin(lla_deg_m[0]*Math.PI/180.0);
        
        return eciVec;
        
    } //calculateECIposition
    
    // overloaded with no inputs -- calculates sidereal time for you
    private double[] calculateECIposition()
    {
        // calculate the ECI j2k position vector of the ground station at the current time
        
        // first get mean sidereal time for this station
        double theta = Sidereal.Mean_Sidereal_Deg(currentJulianDate-AstroConst.JDminusMJD, lla_deg_m[1]);
                     
        return calculateECIposition(theta);
        
    } //calculateECIposition
    
    // transform ECI to topocentric-horizon system (SEZ) (south-East-Zenith)
    private double[] eci2sez(double[] rECI,double thetaDeg,double latDeg)
    {
        double[] rSEZ = new double[3]; // new postion in SEZ coorinates
        
        //? (the local sidereal time) -> (thetaDeg*Math.PI)
        //? (the observer's latitude) - > (latDeg*Math.PI)
        rSEZ[0] = Math.sin(latDeg*Math.PI/180.0) * Math.cos(thetaDeg*Math.PI/180.0) * rECI[0] + Math.sin(latDeg*Math.PI/180.0) * Math.sin(thetaDeg*Math.PI/180.0) * rECI[1] - Math.cos(latDeg*Math.PI/180.0) * rECI[2];
        rSEZ[1] = -Math.sin(thetaDeg*Math.PI/180.0) * rECI[0] + Math.cos(thetaDeg*Math.PI/180.0) * rECI[1];
        rSEZ[2] = Math.cos(latDeg*Math.PI/180.0) * Math.cos(thetaDeg*Math.PI/180.0) * rECI[0] + Math.cos(latDeg*Math.PI/180.0) * Math.sin(thetaDeg*Math.PI/180.0) * rECI[1] + Math.sin(latDeg*Math.PI/180.0) * rECI[2];
        
        return rSEZ;
    }
    
    /**
     * Calculates the Azumuth, Elevation, and Range from Ground Station to another position
     * @param eci_pos ECI position of object in meters
     * @return Azumuth [deg], Elevation [deg], and Range vector [m]
     */
    public double[] calculate_AER(double[] eci_pos)
    {
        double[] aer = new double[3];
        
        // 0th step get local mean Sidereal time
        // first get mean sidereal time for this station - since we use it twice
        double thetaDeg = Sidereal.Mean_Sidereal_Deg(currentJulianDate-AstroConst.JDminusMJD, lla_deg_m[1]);
        
        // first calculate ECI position of Station
        double[] eciGS = calculateECIposition(thetaDeg);
        
        // find the vector between pos and GS
        double[] rECI = MathUtils.sub(eci_pos, eciGS);
        
        // calculate range
        aer[2] = MathUtils.norm(rECI);
        
        // now transform ECI to topocentric-horizon system (SEZ)  (use Geodetic Lat, not geocentric)
        double[] rSEZ = eci2sez(rECI,thetaDeg,lla_deg_m[0]); // ECI vec, sidereal in Deg, latitude in deg
        
        // compute azimuth [radians] -> Deg
        //aer[0] = Math.atan(-rSEZ[1]/rSEZ[0]) * 180.0/Math.PI;
        aer[0] = Math.atan2(-rSEZ[0], rSEZ[1]) * 180.0/Math.PI;
        
        //System.out.println("aer[0]_0=" + aer[0] + ", rSEZ[-0,1]=" + (-rSEZ[0]) + ", " +rSEZ[1] );
        
        // do conversions so N=0, S=180, NW=270
        if(aer[0] <= 0)
        {
            aer[0] = Math.abs(aer[0]) + 90;
        }
        else
        {
            if(aer[0]<= 90)  //(between 0 and 90)
            {
                aer[0] = -1.0*aer[0] + 90.0;
            }
            else // between 90 and 180
            {
                aer[0] = -1.0*aer[0] + 450.0; 
            }
        }
        
        // compute elevation [radians]
        aer[1] = Math.asin(rSEZ[2] / aer[2]) * 180.0/Math.PI; 
        
        //System.out.println("SEZ: " + rSEZ[0] + ", " + rSEZ[1] + ", " + rSEZ[2]);
        
        return aer;
    }
    
    /// get set methods ========================================

    public String getStationName()
    {
        return stationName;
    }

    public void setStationName(String stationName)
    {
        this.stationName = stationName;
    }

    public double[] getLla_deg_m()
    {
        return lla_deg_m;
    }

    public void setLla_deg_m(double[] lla_deg_m)
    {
        this.lla_deg_m = lla_deg_m;
    }
    
    public double getLatitude()
    {
        return lla_deg_m[0];
    }
    
    public double getLongitude()
    {
        return lla_deg_m[1];
    }
    
    public double getAltitude()
    {
        return lla_deg_m[2];
    }

    public double getElevationConst()
    {
        return elevationConst;
    }

    public void setElevationConst(double elevationConst)
    {
        this.elevationConst = elevationConst;
    }

    public Color getStationColor()
    {
        return stationColor;
    }

    public void setStationColor(Color satColor)
    {
        this.stationColor = satColor;
    }

    public boolean isShow2D()
    {
        return show2D;
    }

    public void setShow2D(boolean show2D)
    {
        this.show2D = show2D;
    }

    public boolean isShow2DName()
    {
        return show2DName;
    }

    public void setShow2DName(boolean show2DName)
    {
        this.show2DName = show2DName;
    }

    public int getGroundStation2DPixelSize()
    {
        return groundStation2DPixelSize;
    }

    public void setGroundStation2DPixelSize(int groundStation2DPixelSize)
    {
        this.groundStation2DPixelSize = groundStation2DPixelSize;
    }

    public boolean isShow3D()
    {
        return show3D;
    }

    public void setShow3D(boolean show3D)
    {
        this.show3D = show3D;
    }

    public boolean isShow3DName()
    {
        return show3DName;
    }

    public void setShow3DName(boolean show3DName)
    {
        this.show3DName = show3DName;
    }

    // current time - julian date
    public double getCurrentJulianDate()
    {
        return currentJulianDate;
    }

    public void setCurrentJulianDate(double currentJulianDate)
    {
        this.currentJulianDate = currentJulianDate;
    }
    
    @Override
    public String toString()
    {
        return this.stationName;
    }
    
}
