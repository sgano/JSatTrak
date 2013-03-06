/*
 * Static methods that aid in conversion between different coordinate systems
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
 * ref: Astronomy on the Personal Computer by Oliver Montenbruck, Thomas Pfleger, R.M. West, and S. Dunlop
 */

package name.gano.astro.coordinates;

import name.gano.astro.AstroConst;
import name.gano.astro.MathUtils;


/**
 *
 * @author Shawn E. Gano
 */
public class CoordinateConversion
{
    // updated functions to convert between J2k <-> TEME

    /**
     * Coverts a vector in J2000.0 coordinates to TEME (true equator, mean equinox) of Date
     * If you have more than one vector to transform you might want to do this manually to save calculating rotation matrix many times
     * @param mjd modified julian date of the desired coordinate transformation
     * @param vecJ2k
     * @return teme vector
     */
    public static double[] J2000toTEME(double mjd, double[] vecJ2k)
    {
        //double mjd = julDate - AstroConst.JDminusMJD;
        double ttt = (mjd - AstroConst.MJD_J2000) / 36525.0;
        double[][] A = J2kCoordinateConversion.teme_j2k(J2kCoordinateConversion.Direction.from, ttt, 24, 2, 'a'); // 24 = order(about what STK uses) 2 = all terms, 'a' full nutation matrix
        // rotate vector
        return J2kCoordinateConversion.matvecmult( A, vecJ2k);
    }

    /**
     * Coverts a vector in TEME (true equator, mean equinox) of Date coordinates to J2000.0
     * @param mjd modified julian date of the desired coordinate transformation
     * @param vecTEME
     * @return J2000.0 vector
     */
    public static double[] TEMEtoJ2000(double mjd, double[] vecTEME)
    {
        //double mjd = julDate - AstroConst.JDminusMJD;
        double ttt = (mjd - AstroConst.MJD_J2000) / 36525.0;
        double[][] A = J2kCoordinateConversion.teme_j2k(J2kCoordinateConversion.Direction.to, ttt, 24, 2, 'a');
        // rotate vector
        return J2kCoordinateConversion.matvecmult( A, vecTEME);
    }

    /**
     * Converts up to two J2000 vectors to MOD vectors (second vector set can be null)
     * @param mjd modified julian date of the desired coordinate transformation
     * @param j2kVec1 typically position
     * @param modVec1
     * @param j2kVec2 typically velocity
     * @param modVec2
     */
    public static void j2000toMOD(double mjd, double[] j2kVec1, double[] modVec1, double[]j2kVec2, double[] modVec2)
    {
        double ttt = (mjd - AstroConst.MJD_J2000) / 36525.0;
        double[][] A = J2kCoordinateConversion.mod_j2000(J2kCoordinateConversion.Direction.from, ttt);

        // carfeful on pass by reference, must copy values back to orginal array not assign a new object to it
        double[] temp;

        // perform transformation (if vars not null)
        if(j2kVec1 != null)
        {
            temp = J2kCoordinateConversion.matvecmult( A, j2kVec1);
            modVec1[0] = temp[0];
            modVec1[1] = temp[1];
            modVec1[2] = temp[2];
        }
        if(j2kVec2 != null)
        {
            temp = J2kCoordinateConversion.matvecmult( A, j2kVec2);
            modVec2[0] = temp[0];
            modVec2[1] = temp[1];
            modVec2[2] = temp[2];
        }
    } // j2000toMOD

    /**
     * Converts up to two J2000 vectors to TOD vectors (second vector set can be null)
     * delta psi/eps correction to J2000 are set to 0, uses 40 terms of nutation
     * @param mjd modified julian date of the desired coordinate transformation
     * @param j2kVec1 typically position
     * @param todVec1
     * @param j2kVec2 typically velocity
     * @param todVec2
     */
    public static void j2000toTOD(double mjd, double[] j2kVec1, double[] todVec1, double[]j2kVec2, double[] todVec2)
    {
        double ttt = (mjd - AstroConst.MJD_J2000) / 36525.0;
        double ddpsi = 0.0;
        double ddeps = 0.0;
        int nutTerms = 40; // 4, 40, 106 popular values
        double[][] A = J2kCoordinateConversion.tod_j2000(J2kCoordinateConversion.Direction.from, ttt, ddpsi,ddeps, nutTerms);

        // carfeful on pass by reference, must copy values back to orginal array not assign a new object to it
        double[] temp;

        // perform transformation (if vars not null)
        if(j2kVec1 != null)
        {
            temp = J2kCoordinateConversion.matvecmult( A, j2kVec1);
            todVec1[0] = temp[0];
            todVec1[1] = temp[1];
            todVec1[2] = temp[2];
        }
        if(j2kVec2 != null)
        {
            temp = J2kCoordinateConversion.matvecmult( A, j2kVec2);
            todVec2[0] = temp[0];
            todVec2[1] = temp[1];
            todVec2[2] = temp[2];
        }
    } // j2000toTOD

    /**
     * General function to perform a equinox basis transformation for a vector in Earth equatorial coordinates (e.g., from 2000.0 to 1950.0)
     *
     * @param mjdCurrentEquinox Current Modified Julian Date Equinox
     * @param currentVec Current vector in Earth equatorial coordinates using mjdCurrentEquinox equinox date (any units of length)
     * @param mjdNewEquinox Modified Julian Date  of equinox to transform to
     * @return vector with equatorial coordinates with equinox of mjdNewEquinox
     */
    public static double[] EquatorialEquinoxChange(double mjdCurrentEquinox, final double[] currentVec, double mjdNewEquinox)
    {
        // first get precession matrix between the two dates
        double[][] prec =  PrecMatrix_Equ( (mjdCurrentEquinox-AstroConst.MJD_J2000)/36525, (mjdNewEquinox-AstroConst.MJD_J2000)/36525);
        return MathUtils.mult(prec,currentVec); // transformed to new Equinox
    }
    
    /**
     * Converts J2000.0 Coordinates to a new equinox date (such as J2000.0 to Mean of Date, mjdNewEquinox = current date)
     * @param mjdNewEquinox new equinox date (MJD)
     * @param j2kPos current J2000.0 vector
     * @return transformed coordintes
     */
    public static double[] EquatorialEquinoxFromJ2K(double mjdNewEquinox, final double[] j2kPos)
    {
        return EquatorialEquinoxChange(AstroConst.MJD_J2000, j2kPos, mjdNewEquinox);
    }
    
    // specialty equinox conversion other->J2000 (can be from MOD or other)
    /**
     * Converts Equatorial Coordinates from any equinox to J2000.0 (such as Mean of Date to J2000.0, mjdCurrentEquinox = current date)
     * NOTE: See J2kCoordinateConversion.java - for possibly better ways to do this
     * @param mjdCurrentEquinox current Modified Julian Date of equinox
     * @param currentPos current vector
     * @return J2000.0 equatorial position vector
     */
    public static double[] EquatorialEquinoxToJ2K(double mjdCurrentEquinox, final double[] currentPos)
    {
        return EquatorialEquinoxChange(mjdCurrentEquinox, currentPos, AstroConst.MJD_J2000);
    }
    
    
    /**
     * Precession of equatorial coordinates
     *
     * @param T1 Epoch given (current) in Julian centuries since J2000
     * @param T2 Epoch to precess to in Julian centuries since J2000
     * @return Precession transformation matrix
     */
        public static double[][] PrecMatrix_Equ(double T1, double T2)
        {
            //
            // Constants
            //
            double dT = T2-T1;
            
            
            //
            // Variables
            //
            double zeta,z,theta;
            
            
            zeta  =  ( (2306.2181+(1.39656-0.000139*T1)*T1)+
                    ((0.30188-0.000344*T1)+0.017998*dT)*dT )*dT/AstroConst.Arcs;
            z     =  zeta + ( (0.79280+0.000411*T1)+0.000205*dT)*dT*dT/AstroConst.Arcs;
            theta =  ( (2004.3109-(0.85330+0.000217*T1)*T1)-
                    ((0.42665+0.000217*T1)+0.041833*dT)*dT )*dT/AstroConst.Arcs;
            
            return MathUtils.mult(MathUtils.mult(MathUtils.R_z(-z) , MathUtils.R_y(theta)) , MathUtils.R_z(-zeta) );
        }
        
    /**
     * Precession of equatorial coordinates, similar to PrecMatrix_Equ, except inputs are Modified Julian Dates
     *
     * @param Mjd_1  Epoch given (Modified Julian Date TT)
     * @param MjD_2  Epoch to precess to (Modified Julian Date TT)
     * @return Precession transformation matrix
     */
        public static double[][] PrecMatrix_Equ_Mjd(double Mjd_1, double Mjd_2)
	{
             // Constants

	  final double T  = (Mjd_1-AstroConst.MJD_J2000)/36525.0;
	  final double dT = (Mjd_2-Mjd_1)/36525.0;
	  
	  // Variables

	  double zeta,z,theta;

	  // Precession angles
	  
	  zeta  =  ( (2306.2181+(1.39656-0.000139*T)*T)+
	                ((0.30188-0.000344*T)+0.017998*dT)*dT )*dT/AstroConst.Arcs;
	  z     =  zeta + ( (0.79280+0.000411*T)+0.000205*dT)*dT*dT/AstroConst.Arcs;
	  theta =  ( (2004.3109-(0.85330+0.000217*T)*T)-
	                ((0.42665+0.000217*T)+0.041833*dT)*dT )*dT/AstroConst.Arcs;

	  // Precession matrix
	  
	  return MathUtils.mult(MathUtils.mult(MathUtils.R_z(-z), MathUtils.R_y(theta)), MathUtils.R_z(-zeta));
          
        }
        

        /**
         * Transformation from mean to true equator and equinox
         * 
         * @param Mjd_TT Modified Julian Date (Terrestrial Time)
         * @return Nutation matrix
         */
        public static double[][] NutMatrix(double Mjd_TT)
	{

	  double eps;
	  
	  // Mean obliquity of the ecliptic

	  eps = MeanObliquity(Mjd_TT);

	  // Nutation in longitude and obliquity
	  double[] deps_dpsi = new double[2];
	  deps_dpsi =NutAngles(Mjd_TT);

	  // Transformation from mean to true equator and equinox
	  
	  // where are dpsi and deps changed?
	  return  MathUtils.mult(MathUtils.mult(MathUtils.R_x(-eps-deps_dpsi[0]),MathUtils.R_z(-deps_dpsi[1])),MathUtils.R_x(+eps));

	} // NutMatrix
        

      /**
     *  Computes the mean obliquity of the ecliptic
     * @param Mjd_TT Modified Julian Date (Terrestrial Time)
     * @return Mean obliquity of the ecliptic
     */
    public static double MeanObliquity(double Mjd_TT)
    {
        final double T = (Mjd_TT - AstroConst.MJD_J2000) / 36525.0;

        return AstroConst.Rad * (23.43929111 - (46.8150 + (0.00059 - 0.001813 * T) * T) * T / 3600.0);
    }
        

        /**
         * Nutation in longitude and obliquity
         * @param Mjd_TT Modified Julian Date (Terrestrial Time)
         * @return Nutation matrix
         */
        public static double[] NutAngles(double Mjd_TT) // returns {dpsi,deps}
	{

	  // Constants
	  final double T  = (Mjd_TT-AstroConst.MJD_J2000)/36525.0;
	  final double T2 = T*T;
	  final double T3 = T2*T;
	  final double rev = 360.0*3600.0;  // arcsec/revolution

	  final int  N_coeff = 106;
	  // C[N_coeff][9]
	  final long C[][] =
	  {
	   //
	   // l  l' F  D Om    dpsi    *T     deps     *T       #
	   //
	    {  0, 0, 0, 0, 1,-1719960,-1742,  920250,   89 },   //   1
	    {  0, 0, 0, 0, 2,   20620,    2,   -8950,    5 },   //   2
	    { -2, 0, 2, 0, 1,     460,    0,    -240,    0 },   //   3
	    {  2, 0,-2, 0, 0,     110,    0,       0,    0 },   //   4
	    { -2, 0, 2, 0, 2,     -30,    0,      10,    0 },   //   5
	    {  1,-1, 0,-1, 0,     -30,    0,       0,    0 },   //   6
	    {  0,-2, 2,-2, 1,     -20,    0,      10,    0 },   //   7
	    {  2, 0,-2, 0, 1,      10,    0,       0,    0 },   //   8
	    {  0, 0, 2,-2, 2, -131870,  -16,   57360,  -31 },   //   9
	    {  0, 1, 0, 0, 0,   14260,  -34,     540,   -1 },   //  10
	    {  0, 1, 2,-2, 2,   -5170,   12,    2240,   -6 },   //  11
	    {  0,-1, 2,-2, 2,    2170,   -5,    -950,    3 },   //  12
	    {  0, 0, 2,-2, 1,    1290,    1,    -700,    0 },   //  13
	    {  2, 0, 0,-2, 0,     480,    0,      10,    0 },   //  14
	    {  0, 0, 2,-2, 0,    -220,    0,       0,    0 },   //  15
	    {  0, 2, 0, 0, 0,     170,   -1,       0,    0 },   //  16
	    {  0, 1, 0, 0, 1,    -150,    0,      90,    0 },   //  17
	    {  0, 2, 2,-2, 2,    -160,    1,      70,    0 },   //  18
	    {  0,-1, 0, 0, 1,    -120,    0,      60,    0 },   //  19
	    { -2, 0, 0, 2, 1,     -60,    0,      30,    0 },   //  20
	    {  0,-1, 2,-2, 1,     -50,    0,      30,    0 },   //  21
	    {  2, 0, 0,-2, 1,      40,    0,     -20,    0 },   //  22
	    {  0, 1, 2,-2, 1,      40,    0,     -20,    0 },   //  23
	    {  1, 0, 0,-1, 0,     -40,    0,       0,    0 },   //  24
	    {  2, 1, 0,-2, 0,      10,    0,       0,    0 },   //  25
	    {  0, 0,-2, 2, 1,      10,    0,       0,    0 },   //  26
	    {  0, 1,-2, 2, 0,     -10,    0,       0,    0 },   //  27
	    {  0, 1, 0, 0, 2,      10,    0,       0,    0 },   //  28
	    { -1, 0, 0, 1, 1,      10,    0,       0,    0 },   //  29
	    {  0, 1, 2,-2, 0,     -10,    0,       0,    0 },   //  30
	    {  0, 0, 2, 0, 2,  -22740,   -2,    9770,   -5 },   //  31
	    {  1, 0, 0, 0, 0,    7120,    1,     -70,    0 },   //  32
	    {  0, 0, 2, 0, 1,   -3860,   -4,    2000,    0 },   //  33
	    {  1, 0, 2, 0, 2,   -3010,    0,    1290,   -1 },   //  34
	    {  1, 0, 0,-2, 0,   -1580,    0,     -10,    0 },   //  35
	    { -1, 0, 2, 0, 2,    1230,    0,    -530,    0 },   //  36
	    {  0, 0, 0, 2, 0,     630,    0,     -20,    0 },   //  37
	    {  1, 0, 0, 0, 1,     630,    1,    -330,    0 },   //  38
	    { -1, 0, 0, 0, 1,    -580,   -1,     320,    0 },   //  39
	    { -1, 0, 2, 2, 2,    -590,    0,     260,    0 },   //  40
	    {  1, 0, 2, 0, 1,    -510,    0,     270,    0 },   //  41
	    {  0, 0, 2, 2, 2,    -380,    0,     160,    0 },   //  42
	    {  2, 0, 0, 0, 0,     290,    0,     -10,    0 },   //  43
	    {  1, 0, 2,-2, 2,     290,    0,    -120,    0 },   //  44
	    {  2, 0, 2, 0, 2,    -310,    0,     130,    0 },   //  45
	    {  0, 0, 2, 0, 0,     260,    0,     -10,    0 },   //  46
	    { -1, 0, 2, 0, 1,     210,    0,    -100,    0 },   //  47
	    { -1, 0, 0, 2, 1,     160,    0,     -80,    0 },   //  48
	    {  1, 0, 0,-2, 1,    -130,    0,      70,    0 },   //  49
	    { -1, 0, 2, 2, 1,    -100,    0,      50,    0 },   //  50
	    {  1, 1, 0,-2, 0,     -70,    0,       0,    0 },   //  51
	    {  0, 1, 2, 0, 2,      70,    0,     -30,    0 },   //  52
	    {  0,-1, 2, 0, 2,     -70,    0,      30,    0 },   //  53
	    {  1, 0, 2, 2, 2,     -80,    0,      30,    0 },   //  54
	    {  1, 0, 0, 2, 0,      60,    0,       0,    0 },   //  55
	    {  2, 0, 2,-2, 2,      60,    0,     -30,    0 },   //  56
	    {  0, 0, 0, 2, 1,     -60,    0,      30,    0 },   //  57
	    {  0, 0, 2, 2, 1,     -70,    0,      30,    0 },   //  58
	    {  1, 0, 2,-2, 1,      60,    0,     -30,    0 },   //  59
	    {  0, 0, 0,-2, 1,     -50,    0,      30,    0 },   //  60
	    {  1,-1, 0, 0, 0,      50,    0,       0,    0 },   //  61
	    {  2, 0, 2, 0, 1,     -50,    0,      30,    0 },   //  62
	    {  0, 1, 0,-2, 0,     -40,    0,       0,    0 },   //  63
	    {  1, 0,-2, 0, 0,      40,    0,       0,    0 },   //  64
	    {  0, 0, 0, 1, 0,     -40,    0,       0,    0 },   //  65
	    {  1, 1, 0, 0, 0,     -30,    0,       0,    0 },   //  66
	    {  1, 0, 2, 0, 0,      30,    0,       0,    0 },   //  67
	    {  1,-1, 2, 0, 2,     -30,    0,      10,    0 },   //  68
	    { -1,-1, 2, 2, 2,     -30,    0,      10,    0 },   //  69
	    { -2, 0, 0, 0, 1,     -20,    0,      10,    0 },   //  70
	    {  3, 0, 2, 0, 2,     -30,    0,      10,    0 },   //  71
	    {  0,-1, 2, 2, 2,     -30,    0,      10,    0 },   //  72
	    {  1, 1, 2, 0, 2,      20,    0,     -10,    0 },   //  73
	    { -1, 0, 2,-2, 1,     -20,    0,      10,    0 },   //  74
	    {  2, 0, 0, 0, 1,      20,    0,     -10,    0 },   //  75
	    {  1, 0, 0, 0, 2,     -20,    0,      10,    0 },   //  76
	    {  3, 0, 0, 0, 0,      20,    0,       0,    0 },   //  77
	    {  0, 0, 2, 1, 2,      20,    0,     -10,    0 },   //  78
	    { -1, 0, 0, 0, 2,      10,    0,     -10,    0 },   //  79
	    {  1, 0, 0,-4, 0,     -10,    0,       0,    0 },   //  80
	    { -2, 0, 2, 2, 2,      10,    0,     -10,    0 },   //  81
	    { -1, 0, 2, 4, 2,     -20,    0,      10,    0 },   //  82
	    {  2, 0, 0,-4, 0,     -10,    0,       0,    0 },   //  83
	    {  1, 1, 2,-2, 2,      10,    0,     -10,    0 },   //  84
	    {  1, 0, 2, 2, 1,     -10,    0,      10,    0 },   //  85
	    { -2, 0, 2, 4, 2,     -10,    0,      10,    0 },   //  86
	    { -1, 0, 4, 0, 2,      10,    0,       0,    0 },   //  87
	    {  1,-1, 0,-2, 0,      10,    0,       0,    0 },   //  88
	    {  2, 0, 2,-2, 1,      10,    0,     -10,    0 },   //  89
	    {  2, 0, 2, 2, 2,     -10,    0,       0,    0 },   //  90
	    {  1, 0, 0, 2, 1,     -10,    0,       0,    0 },   //  91
	    {  0, 0, 4,-2, 2,      10,    0,       0,    0 },   //  92
	    {  3, 0, 2,-2, 2,      10,    0,       0,    0 },   //  93
	    {  1, 0, 2,-2, 0,     -10,    0,       0,    0 },   //  94
	    {  0, 1, 2, 0, 1,      10,    0,       0,    0 },   //  95
	    { -1,-1, 0, 2, 1,      10,    0,       0,    0 },   //  96
	    {  0, 0,-2, 0, 1,     -10,    0,       0,    0 },   //  97
	    {  0, 0, 2,-1, 2,     -10,    0,       0,    0 },   //  98
	    {  0, 1, 0, 2, 0,     -10,    0,       0,    0 },   //  99
	    {  1, 0,-2,-2, 0,     -10,    0,       0,    0 },   // 100
	    {  0,-1, 2, 0, 1,     -10,    0,       0,    0 },   // 101
	    {  1, 1, 0,-2, 1,     -10,    0,       0,    0 },   // 102
	    {  1, 0,-2, 2, 0,     -10,    0,       0,    0 },   // 103
	    {  2, 0, 0, 2, 0,      10,    0,       0,    0 },   // 104
	    {  0, 0, 2, 4, 2,     -10,    0,       0,    0 },   // 105
	    {  0, 1, 0, 1, 0,      10,    0,       0,    0 }    // 106
	   };

	  // Variables

	  double  l, lp, F, D, Om;
	  double  arg;
	  

	  // Mean arguments of luni-solar motion
	  //
	  //   l   mean anomaly of the Moon
	  //   l'  mean anomaly of the Sun
	  //   F   mean argument of latitude
	  //   D   mean longitude elongation of the Moon from the Sun 
	  //   Om  mean longitude of the ascending node
	  
	  l  = MathUtils.Modulo(  485866.733 + (1325.0*rev +  715922.633)*T+ 31.310*T2 + 0.064*T3, rev );
	  lp = MathUtils.Modulo ( 1287099.804 + (  99.0*rev + 1292581.224)*T -  0.577*T2 - 0.012*T3, rev );
	  F  = MathUtils.Modulo (  335778.877 + (1342.0*rev +  295263.137)*T - 13.257*T2 + 0.011*T3, rev );
	  D  = MathUtils.Modulo ( 1072261.307 + (1236.0*rev + 1105601.328)*T -  6.891*T2 + 0.019*T3, rev );
	  Om = MathUtils.Modulo (  450160.280 - (   5.0*rev +  482890.539)*T +  7.455*T2 + 0.008*T3, rev );

	  // Nutation in longitude and obliquity [rad]
	  
	  double[] deps_dpsi = {0.0,0.0}; // deps = dpsi = 0.0;
	  
	  for (int i=0; i<N_coeff; i++) 
	  {
	    arg  =  ( C[i][0]*l+C[i][1]*lp+C[i][2]*F+C[i][3]*D+C[i][4]*Om ) / AstroConst.Arcs;
	    deps_dpsi[1] += ( C[i][5]+C[i][6]*T ) * Math.sin(arg);
	    deps_dpsi[0] += ( C[i][7]+C[i][8]*T ) * Math.cos(arg);
	  }
	      
	  deps_dpsi[1] = 1.0E-5 * deps_dpsi[1]/AstroConst.Arcs;
	  deps_dpsi[0] = 1.0E-5 * deps_dpsi[0]/AstroConst.Arcs;
	  
	  return deps_dpsi;

	} // NutAngles
        
        
        /**
         * Greenwich Apparent Sidereal Time
         * @param Mjd_UT1 Modified Julian Date UT1
         * @return GMST in [rad]
         */
        public static double GAST(double Mjd_UT1)
	{
	  return MathUtils.Modulo( GMST(Mjd_UT1) + EqnEquinox(Mjd_UT1), 2.0*Math.PI );
	}


        /**
         * Transformation from true equator and equinox to Earth equator and Greenwich meridian system
         * @param Mjd_UT1 Modified Julian Date UT1
         * @return Greenwich Hour Angle matrix
         */
        public static double[][] GHAMatrix(double Mjd_UT1)
	{
	  return  MathUtils.R_z( GAST(Mjd_UT1) );
	}

        
        /**
         * Greenwich Mean Sidereal Time
         * @param Mjd_UT1 Modified Julian Date UT1
         * @return GMST in [rad]
         */
        public static double GMST(double Mjd_UT1)
	{

	  // Constants

	  final double Secs = 86400.0;        // Seconds per day

	  // Variables

	  double Mjd_0,UT1,T_0,T,gmst;

	  // Mean Sidereal Time
	  
	  Mjd_0 = Math.floor(Mjd_UT1);
	  UT1   = Secs*(Mjd_UT1-Mjd_0);          // [s]
	  T_0   = (Mjd_0  -AstroConst.MJD_J2000)/36525.0; 
	  T     = (Mjd_UT1-AstroConst.MJD_J2000)/36525.0; 

	  gmst  = 24110.54841 + 8640184.812866*T_0 + 1.002737909350795*UT1
	          + (0.093104-6.2e-6*T)*T*T; // [s]

	  return  2.0*Math.PI*MathUtils.Frac(gmst/Secs);       // [rad], 0..2pi

	} // GMST
        
        
        /**
         * Computation of the equation of the equinoxes
         * 
         * Notes: The equation of the equinoxes dpsi*cos(eps) is the right ascension of the 
         *        mean equinox referred to the true equator and equinox and is equal to the 
         *        difference between apparent and mean sidereal time.
         * 
         * @param Mjd_TT Modified Julian Date (Terrestrial Time)
         * @return  Equation of the equinoxes
         */
        public static double EqnEquinox(double Mjd_TT)
	{           
	  // Nutation in longitude and obliquity 
	  double[] deps_dpsi = new double[2];    // Nutation angles 
	  deps_dpsi = NutAngles( Mjd_TT );

	  // Equation of the equinoxes

	  return  deps_dpsi[1] * Math.cos( MeanObliquity(Mjd_TT) );

	}


      /**
     * Convert an angle in degrees (such as azimuth) to compass points (N, NNE, NE...etc)
     * @param degrees  (will be converted to [0,360) )
     * @return compass direction -- if you only want N, NE, E... then check for a length of 3 and drop first char
     */
    public static String degrees2CompassPoints(double degrees)
    {
        double deg = degrees;

        // make sure it is in the range 0-360
        while(deg <= 0)
        {
            deg += 360.0; // add 360 until it is positive
        }

        deg = deg % 360.0;  // insure it is less than 360

        if(deg < 11.25)
        {
            return "N";
        }
        else if(deg < 33.75)
        {
            return "NNE";
        }
        else if(deg < 56.25)
        {
            return "NE";
        }
        else if(deg < 78.75)
        {
            return "ENE";
        }
        else if(deg < 101.25)
        {
            return "E";
        }
        else if(deg < 123.75)
        {
            return "ESE";
        }
        else if(deg < 146.25)
        {
            return "SE";
        }
        else if(deg < 168.75)
        {
            return "SSE";
        }
        else if(deg < 191.25)
        {
            return "S";
        }
        else if(deg < 213.75)
        {
            return "SSW";
        }
        else if(deg < 236.25)
        {
            return "SW";
        }
        else if(deg < 258.75)
        {
            return "WSW";
        }
        else if(deg < 281.25)
        {
            return "W";
        }
        else if(deg < 303.75)
        {
            return "WNW";
        }
        else if(deg < 326.25)
        {
            return "NW";
        }
        else if(deg < 348.75)
        {
            return "NNW";
        }
        else //if(deg => 348.75)
        {
            return"N";
        }

    } //degrees2CompassPoints
        
}
