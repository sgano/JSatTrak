/*
 * Class used to model the Moon
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
import name.gano.astro.Kepler;
import name.gano.astro.MathUtils;

/**
 *
 * @author sgano
 */
public class Moon 
{
    
    /**
     * Computes the Moon's geocentric position using a low precision analytical series
     * @param Mjd_TT Terrestrial Time (Modified Julian Date)
     * @return Lunar position vector [m] with respect to the mean equator and equinox of J2000 (EME2000, ICRF)
     */
    public static double[] MoonPosition(double Mjd_TT)
    {
        // Constants

        final double eps = 23.43929111 * AstroConst.Rad;  // Obliquity of J2000 ecliptic
        final double T = (Mjd_TT - AstroConst.MJD_J2000) / 36525.0;  // Julian cent. since J2000

        // Variables

        double L_0, l, lp, F, D, dL, S, h, N;
        double L, B, R, cosB;
        double[] r_Moon = new double[3];


        // Mean elements of lunar orbit

        L_0 = MathUtils.Frac(0.606433 + 1336.851344 * T);     // Mean longitude [rev]
        // w.r.t. J2000 equinox
        l = 2.0 * Math.PI * MathUtils.Frac(0.374897 + 1325.552410 * T);     // Moon's mean anomaly [rad]
        lp = 2.0 * Math.PI * MathUtils.Frac(0.993133 + 99.997361 * T);     // Sun's mean anomaly [rad]
        D = 2.0 * Math.PI * MathUtils.Frac(0.827361 + 1236.853086 * T);     // Diff. long. Moon-Sun [rad]
        F = 2.0 * Math.PI * MathUtils.Frac(0.259086 + 1342.227825 * T);     // Argument of latitude 


        // Ecliptic longitude (w.r.t. equinox of J2000)

        dL = +22640 * Math.sin(l) - 4586 * Math.sin(l - 2 * D) + 2370 * Math.sin(2 * D) + 769 * Math.sin(2 * l) - 668 * Math.sin(lp) - 412 * Math.sin(2 * F) - 212 * Math.sin(2 * l - 2 * D) - 206 * Math.sin(l + lp - 2 * D) + 192 * Math.sin(l + 2 * D) - 165 * Math.sin(lp - 2 * D) - 125 * Math.sin(D) - 110 * Math.sin(l + lp) + 148 * Math.sin(l - lp) - 55 * Math.sin(2 * F - 2 * D);

        L = 2.0 * Math.PI * MathUtils.Frac(L_0 + dL / 1296.0e3);  // [rad]

        // Ecliptic latitude

        S = F + (dL + 412 * Math.sin(2 * F) + 541 * Math.sin(lp)) / AstroConst.Arcs;
        h = F - 2 * D;
        N = -526 * Math.sin(h) + 44 * Math.sin(l + h) - 31 * Math.sin(-l + h) - 23 * Math.sin(lp + h) + 11 * Math.sin(-lp + h) - 25 * Math.sin(-2 * l + F) + 21 * Math.sin(-l + F);

        B = (18520.0 * Math.sin(S) + N) / AstroConst.Arcs;   // [rad]

        cosB = Math.cos(B);

        // Distance [m]

        R = 385000e3 - 20905e3 * Math.cos(l) - 3699e3 * Math.cos(2 * D - l) - 2956e3 * Math.cos(2 * D) - 570e3 * Math.cos(2 * l) + 246e3 * Math.cos(2 * l - 2 * D) - 205e3 * Math.cos(lp - 2 * D) - 171e3 * Math.cos(l + 2 * D) - 152e3 * Math.cos(l + lp - 2 * D);

        // Equatorial coordinates
        double[] temp = {R * Math.cos(L) * cosB, R * Math.sin(L) * cosB, R * Math.sin(B)};
        r_Moon = MathUtils.mult(MathUtils.R_x(-eps), temp);

        return r_Moon;

    } // MoonPosition
    
    
}
