/*
 * Shawn E. Gano - methods to perform Sidereal time calculations
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

package name.gano.astro;

/**
 *
 * @author Shawn
 */
public class Sidereal 
{
    
    /**
     * Calculates the Greenwich mean sidereal time (GMST) on julDate (doesn't have to be 0h).
     * Used calculations from Meesus 2nd ed. 
     * @param mjd Modified Julian Date
     * @return Greenwich mean sidereal time in degrees (0-360)
     */
    public static double Greenwich_Mean_Sidereal_Deg(double mjd)
    {
        // calculate T
        double T = (mjd-51544.5)/36525.0;  
        
        // do calculation
        double gmst = ( (280.46061837 + 360.98564736629*(mjd-51544.5)) + 0.000387933*T*T - T*T*T/38710000.0) % 360.0;
        
        // make positive
        if(gmst < 0)
        {
            gmst += 360.0;
        }
        
        return gmst;
    } //Greenwich_Mean_Sidereal_Deg
    
    /**
     * Calculates the mean sidereal time (MST) on julDate (doesn't have to be 0h) for a given longitiude.
     * @param mjd Modified Julian Date
     * @param longitude longitude in degrees
     * @return mean sidereal time in degrees (0-360)
     */
    public static double Mean_Sidereal_Deg(double mjd, double longitudeDeg)
    {
        return (Greenwich_Mean_Sidereal_Deg(mjd) + longitudeDeg) % 360.0;
    } // Mean_Sidereal_Deg

    
    // main testing function
    public static void main(String[] args)
    {
        double mjd = 2449991.875 - AstroConst.JDminusMJD;
        double longitude = -75; // degrees
        double latitude = 40;
        
        double mst = Sidereal.Mean_Sidereal_Deg(mjd, longitude);
        
        System.out.println("For Phily MST: " + mst);
        
        // assuming a spherical Earth - calculate ECI vector
        double [] eciVec = new double[3];
        eciVec[2] = AstroConst.R_Earth * Math.sin( latitude*Math.PI/180.0 );
        double r = AstroConst.R_Earth * Math.cos( latitude*Math.PI/180.0 );
        eciVec[0] = r * Math.cos(mst*Math.PI/180.0);
        eciVec[1] = r * Math.sin(mst*Math.PI/180.0);
        
        System.out.println("ECI: " + eciVec[0] + ", " + eciVec[1] + ", " + eciVec[2]);
    }
    
}
