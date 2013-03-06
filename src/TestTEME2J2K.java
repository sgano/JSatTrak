/*
 * TestTEME2J2K.java
 * 
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
 *
 * method from http://www.mpe-garching.mpg.de/gamma/instruments/swift/software/headas_psi/attitude/tasks/prefilter/tle.c
 * was not accrate -- used vallado instead!
 */

/**
 *
 * @author Shawn
 */
public class TestTEME2J2K
{

    public static void main(String[] args)
    {
        double[][] rm = new double[3][3];
         double jd = 2454994.0;//2455197.5;

         teme_to_mean_of_j2000_correction (jd, rm);

         for(int i = 0; i<3 ;i++)
         {
             for(int j = 0; j<3;j++)
             {
                 System.out.print(rm [i][j] + "   ");
             }
             System.out.print("\n");
         }


    }


    public static void teme_to_mean_of_j2000_correction (double jd, double[][] rm)
{
  double DTOR = Math.PI / 180;
  double ASTOR = DTOR / 3600;
  int i, j, k;
  double om, ls, lm, deps;
  double zeta, z, theta;
  double[][] precmat = new double[3][3];
  double[][] nutmat = new double[3][3];

  /* Find the time T, measured in Julian centures from the epoch J2000.0 */
  double T = (jd - 2451545) / 36525;
  double T2 = T * T;
  double T3 = T2 * T;

  /*
  * Meeus Ch. 21
  * Compute the longitude of the ascending node of the Moon's mean
  * orbit on the ecliptic, measured from the mean equinox of date.
  */
  om = 125.04452 - 1934.136261 * T ;   /* [deg] */

  /* Mean longitude of the sun and moon */
  ls = 280.4665 +  36000.7698 * T ;    /* [deg] */
  lm = 218.3165 + 481267.8813 * T ;    /* [deg] */

  /* Convert arguments to radians */
  om = om * DTOR;
  ls = ls * DTOR;
  lm = lm * DTOR;  /* [radian] */

  /* Nutation in obliquity  [arcsec] */
  deps = +9.20 * Math.cos(om) + 0.57 * Math.cos(2.0 * ls)
      + 0.10 * Math.cos(2.0 * lm) - 0.09 * Math.cos(2.0 * om);
  /* Note that nutation in longitude is not required since the TLE's
  * are already referred to the mean equinox of date. */

  /* Precession angles (Meeus Ch. 20; ESAA Sec. 3.2) */
  zeta  = 2306.2181 * T + 0.30188 * T2 + 0.017998 * T3;   /* [arcsec] */
  z     = 2306.2181 * T + 1.09468 * T2 + 0.018203 * T3;   /* [arcsec] */
  theta = 2004.3109 * T - 0.42665 * T2 - 0.041833 * T3;   /* [arcsec] */

  /* Convert arcsec to radians */
  deps  = deps  * ASTOR ;   /* [radian] */
  zeta  = zeta  * ASTOR ;   /* [radian] */
  z     = z     * ASTOR ;   /* [radian] */
  theta = theta * ASTOR ;   /* [radian] */

  /*
  * 3x3 precession matrix, three Euler angles
  * PRECMAT = R_z(-zeta) R_y(+theta) R_z(-z)
  */
  precmat[0][0] = Math.cos(zeta) * Math.cos(theta) * Math.cos(z) - Math.sin(zeta) * Math.sin(z);
  precmat[0][1] = -Math.sin(zeta) * Math.cos(theta) * Math.cos(z) - Math.cos(zeta) * Math.sin(z);
  precmat[0][2] = -Math.sin(theta) * Math.cos(z);
  precmat[1][0] = Math.cos(zeta) * Math.cos(theta) * Math.sin(z) + Math.sin(zeta) * Math.cos(z);
  precmat[1][1] = -Math.sin(zeta) * Math.cos(theta) * Math.sin(z) + Math.cos(zeta) * Math.cos(z);
  precmat[1][2] = -Math.sin(theta) * Math.sin(z);
  precmat[2][0] = Math.cos(zeta) * Math.sin(theta);
  precmat[2][1] = -Math.sin(zeta) * Math.sin(theta);
  precmat[2][2] = Math.cos(theta);

  /* 3x3 nutation matrix = R_x(-deps) */
  nutmat[0][0] = 1;
  nutmat[0][1] = 0;
  nutmat[0][2] = 0;
  nutmat[1][0] = 0;
  nutmat[1][1] = Math.cos(-deps);
  nutmat[1][2] = Math.sin(-deps);
  nutmat[2][0] = 0;
  nutmat[2][1] = -Math.sin(-deps);
  nutmat[2][2] = Math.cos(-deps);

  /* Form matrix product of precession and nutation matrices */
  for (i = 0; i < 3; ++i)
    for (j = 0; j < 3; ++j) {
      double total = 0;
      for (k = 0; k < 3; ++k)
        total += precmat[k][j] * nutmat[i][k];
      rm[i][j] = total;
    }

}

}
