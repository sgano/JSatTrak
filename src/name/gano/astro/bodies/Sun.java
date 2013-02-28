/*
 * Class used to model the sun (in Earth Equatorial Coordinates)
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

package name.gano.astro.bodies;

import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.MathUtils;
import name.gano.astro.coordinates.CoordinateConversion;

/**
 *
 * @author Shawn E. Gano
 */
public class Sun
{
    
    private double[] currentPosition; // current J2000 position of the Sun
    private double currentMJD; // current Modified Julian Date - UT
    //private double[] lla; // lat, lon, alt (in radians,radians,meters)
    private double[] currentPositionTEME; // TEME of date position
    //private double[] lla; // lat and long
    private double[] darkCenterLLA; // center Lat/Long of darkness
    private double[] sunCenterLLA;
    
    /**
     * Creates a new instance of Sun
     *
     * @param iniMJD initial Modified Julian Date
     */
    public Sun(double iniMJD)
    {
        currentMJD = iniMJD;
        setJ2KPosition(SunPositionLowUT(currentMJD));
    }
    
    /**
     * sets J2K position as well as calculates MOD position 
     * @param newJ2kPos j2K position
     */
    private void setJ2KPosition(double[] newJ2kPos)
    {
        currentPosition = newJ2kPos;
        // convert to MOD, J2000-> MOD
        //double[][] prec =  CoordinateConversion.PrecMatrix_Equ(0.0, (currentMJD-AstroConst.MJD_J2000)/36525)
        //currentPositionMOD = MathUtils.mult(prec,currentPosition); // currentPosition
        // ERROR above in conversion - convert to TEME (not MOD)
        currentPositionTEME = CoordinateConversion.J2000toTEME(currentMJD, currentPosition);
        
        
        // LLA of sun center and darkness center
        sunCenterLLA = GeoFunctions.GeodeticLLA( currentPositionTEME , currentMJD );
        darkCenterLLA = GeoFunctions.GeodeticLLA( getOpositeSunPositionTEME() , currentMJD );
        
        //System.out.println("Sun LLA:" + sunCenterLLA[0]*180/Math.PI + ", " + sunCenterLLA[1]*180/Math.PI);
    }
     
    
    /**
     * Computes the Sun's geocentric position using a low precision analytical series
     *
     * @param mjd modified Julian date (UT)
     * @return Solar position vector [m] with respect to the mean equator and equinox of J2000 (EME2000, ICRF)
     */
    public double[] SunPositionLowUT(double mjd)
    {
        double mjdTT = mjd + name.gano.astro.time.Time.deltaT(mjd); // corrected time to TT from UT
        
        return calculateSunPositionLowTT(mjdTT);
    }
    
   
    /**
     * Computes the Sun's geocentric position using a low precision analytical series
     *
     * @param Mjd_TT Terrestrial Time (Modified Julian Date)
     * @return Solar position vector [m] with respect to the mean equator and equinox of J2000 (EME2000, ICRF)
     */
	public static double[] calculateSunPositionLowTT(double Mjd_TT)
	{
	  // Constants

	  final double eps = 23.43929111*AstroConst.Rad;             // Obliquity of J2000 ecliptic 
	  final double T   = (Mjd_TT-AstroConst.MJD_J2000)/36525.0;  // Julian cent. since J2000

	  // Variables

	  double L, M, r;
	  double[] r_Sun = new double[3];

	  // Mean anomaly, ecliptic longitude and radius

	  M = 2.0*Math.PI * MathUtils.Frac( 0.9931267 + 99.9973583*T);                    // [rad]
	  L = 2.0*Math.PI * MathUtils.Frac ( 0.7859444 + M/(2.0*Math.PI) + 
	                    (6892.0*Math.sin(M)+72.0*Math.sin(2.0*M)) / 1296.0e3); // [rad]
	  r = 149.619e9 - 2.499e9*Math.cos(M) - 0.021e9*Math.cos(2*M);             // [m]
	  
	  // Equatorial position vector
	  double[] temp = {r*Math.cos(L),r*Math.sin(L),0.0};
	  r_Sun = MathUtils.mult(MathUtils.R_x(-eps) , temp);
	  
	  return r_Sun;
	} // SunPositionLow

        

    /**
     * get current j2000.0 position of the sun
     *
     * @return current j2000.0 position
     */
    public double[] getCurrentPositionJ2K()
    {
        return currentPosition;
    }

    /**
     * get current modified julian date
     *
     * @return MJD
     */
    public double getCurrentMJD()
    {
        return currentMJD;
    }

    /**
     * Set current time - a new position is calculated
     *
     * @param currentMJD modified julian date
     */
    public void setCurrentMJD(double currentMJD)
    {
        this.currentMJD = currentMJD;
        // update position
        setJ2KPosition(SunPositionLowUT(currentMJD));
    }
    

    /**
     * returns oposite position from sun (dark side origin)
     *
     * @return J2k position [m]
     */
    public double[] getOpositeSunPositionJ2K()
    {
        return new double[] {-currentPosition[0],-currentPosition[1],-currentPosition[2]};
    }
    

    /**
     * returns opposite position from the sun TEME
     * @return TEME of date position [m]
     */
    public double[] getOpositeSunPositionTEME()
    {
        return new double[] {-currentPositionTEME[0],-currentPositionTEME[1],-currentPositionTEME[2]};
    }

    /**
     *  Return current sun position in Earth Equatorial coordinates with mean of date Equinox
     *  (ECI) 
     * @return TEME of date position [m]
     */
    public double[] getCurrentPositionTEME()
    {
        return currentPositionTEME;
    }
    
    /**
     * Latitude/Longitude/Altitude of position opposite sun  
     *
     * @return Latitude/Longitude/Altitude [rad]
     */
    public double[] getCurrentDarkLLA()
    {
        return darkCenterLLA;
    }
    
    /**
     * Latitude/Longitude/Altitude of Sun  
     *
     * @return Latitude/Longitude/Altitude [rad]
     */
    public double[] getCurrentLLA()
    {
        return sunCenterLLA;
    }

    
    /**
     * Computes the acceleration due to solar radiation pressure assuming the spacecraft surface normal to the Sun direction
     * Note: r, r_sun, Area, mass, P0 and AU must be given in consistent units,  e.g. m, m^2, kg and N/m^2. 
     * @param r Spacecraft position vector 
     * @param r_Sun Sun position vector 
     * @param Area Cross-section 
     * @param mass Spacecraft mass
     * @param CR Solar radiation pressure coefficient
     * @param P0 Solar radiation pressure at 1 AU 
     * @param AU Length of one Astronomical Unit 
     * @return Acceleration (a=d^2r/dt^2)
     */
    public static double[] AccelSolrad(final double[] r, final double[] r_Sun, double Area, double mass, double CR, double P0, double AU)
    {
        double[] d = new double[3];

        // Relative position vector of spacecraft w.r.t. Sun
        d = MathUtils.sub(r, r_Sun);

        // Acceleration 
        return MathUtils.scale(d, CR * (Area / mass) * P0 * (AU * AU) / Math.pow(MathUtils.norm(d), 3));
    } // AccelSolrad
    
    

    /**
     * Computes the fractional illumination of a spacecraft in the vicinity of 
     * the Earth assuming a cylindrical shadow model
     * @param r Spacecraft position vector [m]
     * @param r_Sun Sun position vector [m]
     * @return Illumination factor: nu=0   Spacecraft in Earth shadow, 
     *                              nu=1   Spacecraft fully illuminated by the Sun
     */
    public static double Illumination(final double[] r, final double[] r_Sun)
    {

        double[] e_Sun = MathUtils.scale(r_Sun, 1.0 / MathUtils.norm(r_Sun));   // Sun direction unit vector
        double s = MathUtils.dot(r, e_Sun);      // Projection of s/c position 

        double illuminated = 0.0;

        if (s > 0 || MathUtils.norm(MathUtils.sub(r, MathUtils.scale(e_Sun, s))) > AstroConst.R_Earth)
        {
            illuminated = 1.0;
        }
        else
        {
            illuminated = 0.0;
        }

        return illuminated;
    } // illumination
    
    
} // Sun
