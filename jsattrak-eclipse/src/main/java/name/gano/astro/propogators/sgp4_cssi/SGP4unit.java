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
 * Created June 2009
 */
/**
 *   Vallado/CSSI SGP4 propogator
 *   Converted to Java from C++ by: Shawn E. Gano, 19 June 2009
 *   The goal of this conversion was to stick as closely to the orginal code
 *   as possible and not trying to convert it to OO design.
 *   This code has been compared using the verification TLEs and "SGP4verification.java"
 *   and the results were virtually identical to the c++ version.
 *   Only rare differences were found in the results on the order of 0.001 cm.
 * ----------------------------------------------------------------
 *
 *                               sgp4unit.cpp
 *
 *    this file contains the sgp4 procedures for analytical propagation
 *    of a satellite. the code was originally released in the 1980 and 1986
 *    spacetrack papers. a detailed discussion of the theory and history
 *    may be found in the 2006 aiaa paper by vallado, crawford, hujsak,
 *    and kelso.
 *
 *                            companion code for
 *               fundamentals of astrodynamics and applications
 *                                    2007
 *                              by david vallado
 *
 *       (w) 719-573-2600, email dvallado@agi.com
 *
 *    current :
 *               3 Nov 08  david vallado
 *                           put returns in for error codes
 *    changes :
 *              29 sep 08  david vallado
 *                           fix atime for faster operation in dspace
 *                           add operationmode for afspc (a) or improved (i)
 *                           performance mode
 *              16 jun 08  david vallado
 *                           update small eccentricity check
 *              16 nov 07  david vallado
 *                           misc fixes for better compliance
 *              20 apr 07  david vallado
 *                           misc fixes for constants
 *              11 aug 06  david vallado
 *                           chg lyddane choice back to strn3, constants, misc doc
 *              15 dec 05  david vallado
 *                           misc fixes
 *              26 jul 05  david vallado
 *                           fixes for paper
 *                           note that each fix is preceded by a
 *                           comment with "sgp4fix" and an explanation of
 *                           what was changed
 *              10 aug 04  david vallado
 *                           2nd printing baseline working
 *              14 may 01  david vallado
 *                           2nd edition baseline
 *                     80  norad
 *                           original baseline
 *       ----------------------------------------------------------------      */
package name.gano.astro.propogators.sgp4_cssi;

/**
 *
 * @author Shawn E. Gano, shawn@gano.name
 */
public class SGP4unit
{
    public static double pi = 3.14159265358979323846;

    public static enum Gravconsttype
    {
        wgs72old,
        wgs72,
        wgs84
    }

    /** -----------------------------------------------------------------------------
     * Java version outputs double array: [ep,inclp,nodep,argpp,mp]
     * -------------------------------------------------------------------------------
     *
     *                           procedure dpper
     *
     *  this procedure provides deep space long period periodic contributions
     *    to the mean elements.  by design, these periodics are zero at epoch.
     *    this used to be dscom which included initialization, but it's really a
     *    recurring function.
     *
     *  author        : david vallado                  719-573-2600   28 jun 2005
     *
     *  inputs        :
     *    e3          -
     *    ee2         -
     *    peo         -
     *    pgho        -
     *    pho         -
     *    pinco       -
     *    plo         -
     *    se2 , se3 , sgh2, sgh3, sgh4, sh2, sh3, si2, si3, sl2, sl3, sl4 -
     *    t           -
     *    xh2, xh3, xi2, xi3, xl2, xl3, xl4 -
     *    zmol        -
     *    zmos        -
     *    ep          - eccentricity                           0.0 - 1.0
     *    inclo       - inclination - needed for lyddane modification
     *    nodep       - right ascension of ascending node
     *    argpp       - argument of perigee
     *    mp          - mean anomaly
     *
     *  outputs       :
     *    ep          - eccentricity                           0.0 - 1.0
     *    inclp       - inclination
     *    nodep        - right ascension of ascending node
     *    argpp       - argument of perigee
     *    mp          - mean anomaly
     *
     *  locals        :
     *    alfdp       -
     *    betdp       -
     *    cosip  , sinip  , cosop  , sinop  ,
     *    dalf        -
     *    dbet        -
     *    dls         -
     *    f2, f3      -
     *    pe          -
     *    pgh         -
     *    ph          -
     *    pinc        -
     *    pl          -
     *    sel   , ses   , sghl  , sghs  , shl   , shs   , sil   , sinzf , sis   ,
     *    sll   , sls
     *    xls         -
     *    xnoh        -
     *    zf          -
     *    zm          -
     *
     *  coupling      :
     *    none.
     *
     *  references    :
     *    hoots, roehrich, norad spacetrack report #3 1980
     *    hoots, norad spacetrack report #6 1986
     *    hoots, schumacher and glover 2004
     *    vallado, crawford, hujsak, kelso  2006
    ----------------------------------------------------------------------------*/
// outputs an array with values for, all outputs are also inputs
// [ep,inclp,nodep,argpp,mp]
    private static double[] dpper(
            double e3, double ee2, double peo, double pgho, double pho,
            double pinco, double plo, double se2, double se3, double sgh2,
            double sgh3, double sgh4, double sh2, double sh3, double si2,
            double si3, double sl2, double sl3, double sl4, double t,
            double xgh2, double xgh3, double xgh4, double xh2, double xh3,
            double xi2, double xi3, double xl2, double xl3, double xl4,
            double zmol, double zmos, double inclo,
            char init,
            double ep, double inclp, double nodep, double argpp, double mp,
            char opsmode)
    {
        // return variables -- all also inputs
        //double inclp,nodep,argpp,mp; // ep -- input and output

        /* --------------------- local variables ------------------------ */
        final double twopi = 2.0 * pi;
        double alfdp, betdp, cosip, cosop, dalf, dbet, dls,
                f2, f3, pe, pgh, ph, pinc, pl,
                sel, ses, sghl, sghs, shll, shs, sil,
                sinip, sinop, sinzf, sis, sll, sls, xls,
                xnoh, zf, zm, zel, zes, znl, zns;

        /* ---------------------- constants ----------------------------- */
        zns = 1.19459e-5;
        zes = 0.01675;
        znl = 1.5835218e-4;
        zel = 0.05490;

        /* --------------- calculate time varying periodics ----------- */
        zm = zmos + zns * t;
        // be sure that the initial call has time set to zero
        if(init == 'y')
        {
            zm = zmos;
        }
        zf = zm + 2.0 * zes * Math.sin(zm);
        sinzf = Math.sin(zf);
        f2 = 0.5 * sinzf * sinzf - 0.25;
        f3 = -0.5 * sinzf * Math.cos(zf);
        ses = se2 * f2 + se3 * f3;
        sis = si2 * f2 + si3 * f3;
        sls = sl2 * f2 + sl3 * f3 + sl4 * sinzf;
        sghs = sgh2 * f2 + sgh3 * f3 + sgh4 * sinzf;
        shs = sh2 * f2 + sh3 * f3;
        zm = zmol + znl * t;
        if(init == 'y')
        {
            zm = zmol;
        }
        zf = zm + 2.0 * zel * Math.sin(zm);
        sinzf = Math.sin(zf);
        f2 = 0.5 * sinzf * sinzf - 0.25;
        f3 = -0.5 * sinzf * Math.cos(zf);
        sel = ee2 * f2 + e3 * f3;
        sil = xi2 * f2 + xi3 * f3;
        sll = xl2 * f2 + xl3 * f3 + xl4 * sinzf;
        sghl = xgh2 * f2 + xgh3 * f3 + xgh4 * sinzf;
        shll = xh2 * f2 + xh3 * f3;
        pe = ses + sel;
        pinc = sis + sil;
        pl = sls + sll;
        pgh = sghs + sghl;
        ph = shs + shll;

        if(init == 'n')
        {
            pe = pe - peo;
            pinc = pinc - pinco;
            pl = pl - plo;
            pgh = pgh - pgho;
            ph = ph - pho;
            inclp = inclp + pinc;
            ep = ep + pe;
            sinip = Math.sin(inclp);
            cosip = Math.cos(inclp);

            /* ----------------- apply periodics directly ------------ */
            //  sgp4fix for lyddane choice
            //  strn3 used original inclination - this is technically feasible
            //  gsfc used perturbed inclination - also technically feasible
            //  probably best to readjust the 0.2 limit value and limit discontinuity
            //  0.2 rad = 11.45916 deg
            //  use next line for original strn3 approach and original inclination
            //  if (inclo >= 0.2)
            //  use next line for gsfc version and perturbed inclination
            if(inclp >= 0.2)
            {
                ph = ph / sinip;
                pgh = pgh - cosip * ph;
                argpp = argpp + pgh;
                nodep = nodep + ph;
                mp = mp + pl;
            }
            else
            {
                /* ---- apply periodics with lyddane modification ---- */
                sinop = Math.sin(nodep);
                cosop = Math.cos(nodep);
                alfdp = sinip * sinop;
                betdp = sinip * cosop;
                dalf = ph * cosop + pinc * cosip * sinop;
                dbet = -ph * sinop + pinc * cosip * cosop;
                alfdp = alfdp + dalf;
                betdp = betdp + dbet;
                nodep = (nodep % twopi);
                //  sgp4fix for afspc written intrinsic functions
                // nodep used without a trigonometric function ahead
                if((nodep < 0.0) && (opsmode == 'a'))
                {
                    nodep = nodep + twopi;
                }
                xls = mp + argpp + cosip * nodep;
                dls = pl + pgh - pinc * nodep * sinip;
                xls = xls + dls;
                xnoh = nodep;
                nodep = Math.atan2(alfdp, betdp);
                //  sgp4fix for afspc written intrinsic functions
                // nodep used without a trigonometric function ahead
                if((nodep < 0.0) && (opsmode == 'a'))
                {
                    nodep = nodep + twopi;
                }
                if(Math.abs(xnoh - nodep) > pi)
                {
                    if(nodep < xnoh)
                    {
                        nodep = nodep + twopi;
                    }
                    else
                    {
                        nodep = nodep - twopi;
                    }
                }
                mp = mp + pl;
                argpp = xls - mp - cosip * nodep;
            }
        }   // if init == 'n'

        return new double[]
                {
                    ep, inclp, nodep, argpp, mp
                };
//#include "debug1.cpp"
    }  // end dpper

    /*-----------------------------------------------------------------------------
     *
     *                           procedure dscom
     *
     *  this procedure provides deep space common items used by both the secular
     *    and periodics subroutines.  input is provided as shown. this routine
     *    used to be called dpper, but the functions inside weren't well organized.
     *
     *  author        : david vallado                  719-573-2600   28 jun 2005
     *
     *  inputs        :
     *    epoch       -
     *    ep          - eccentricity
     *    argpp       - argument of perigee
     *    tc          -
     *    inclp       - inclination
     *    nodep       - right ascension of ascending node
     *    np          - mean motion
     *
     *  outputs       :
     *    sinim  , cosim  , sinomm , cosomm , snodm  , cnodm
     *    day         -
     *    e3          -
     *    ee2         -
     *    em          - eccentricity
     *    emsq        - eccentricity squared
     *    gam         -
     *    peo         -
     *    pgho        -
     *    pho         -
     *    pinco       -
     *    plo         -
     *    rtemsq      -
     *    se2, se3         -
     *    sgh2, sgh3, sgh4        -
     *    sh2, sh3, si2, si3, sl2, sl3, sl4         -
     *    s1, s2, s3, s4, s5, s6, s7          -
     *    ss1, ss2, ss3, ss4, ss5, ss6, ss7, sz1, sz2, sz3         -
     *    sz11, sz12, sz13, sz21, sz22, sz23, sz31, sz32, sz33        -
     *    xgh2, xgh3, xgh4, xh2, xh3, xi2, xi3, xl2, xl3, xl4         -
     *    nm          - mean motion
     *    z1, z2, z3, z11, z12, z13, z21, z22, z23, z31, z32, z33         -
     *    zmol        -
     *    zmos        -
     *
     *  locals        :
     *    a1, a2, a3, a4, a5, a6, a7, a8, a9, a10         -
     *    betasq      -
     *    cc          -
     *    ctem, stem        -
     *    x1, x2, x3, x4, x5, x6, x7, x8          -
     *    xnodce      -
     *    xnoi        -
     *    zcosg  , zsing  , zcosgl , zsingl , zcosh  , zsinh  , zcoshl , zsinhl ,
     *    zcosi  , zsini  , zcosil , zsinil ,
     *    zx          -
     *    zy          -
     *
     *  coupling      :
     *    none.
     *
     *  references    :
     *    hoots, roehrich, norad spacetrack report #3 1980
     *    hoots, norad spacetrack report #6 1986
     *    hoots, schumacher and glover 2004
     *    vallado, crawford, hujsak, kelso  2006
     *  ----------------------------------------------------------------------------
     * constructor modified to return an array with all the values that are not contained in the SGP4SatData object
     * Array returned with these values:
     * [snodm, cnodm, sinim, cosim, sinomm, cosomm, day, em, emsq, gam, rtemsq, s1, s2, s3,
     *  s4, s5, s6, s7, ss1, ss2, ss3, ss4, ss5, ss6, ss7, sz1, sz2, sz3, sz11, sz12, sz13,
     * sz21, sz22, sz23, sz31, sz32, sz33, nm, z1, z2, z3, z11, z12, z13, z21, z22, z23, z31, z32, z33 ]
     * --------------------------------------------------------------*/
    private static double[] dscom(
            // inputs
            double epoch, double ep, double argpp, double tc, double inclp,
            double nodep, double np,
            SGP4SatData satrec //       // not
            //       double& snodm, double& cnodm, double& sinim,  double& cosim, double& sinomm,
            //       double& cosomm,double& day,
            //       //SGP4SatData
            //       double& e3,     double& ee2,
            //       // not
            //       double& em, double& emsq,  double& gam,
            //       // SGP4SatData
            //       double& peo,   double& pgho,  double& pho,
            //       double& pinco, double& plo,
            //       // not
            //       double& rtemsq,
            //       // SGP4SatData
            //       double& se2,   double& se3,
            //       double& sgh2,  double& sgh3,  double& sgh4,   double& sh2,   double& sh3,
            //       double& si2,   double& si3,   double& sl2,    double& sl3,   double& sl4,
            //       // not
            //       double& s1,    double& s2,    double& s3,     double& s4,    double& s5,
            //       double& s6,    double& s7,    double& ss1,    double& ss2,   double& ss3,
            //       double& ss4,   double& ss5,   double& ss6,    double& ss7,   double& sz1,
            //       double& sz2,   double& sz3,   double& sz11,   double& sz12,  double& sz13,
            //       double& sz21,  double& sz22,  double& sz23,   double& sz31,  double& sz32,
            //       double& sz33,
            //       // SGP4SatData
            //       double& xgh2,  double& xgh3,   double& xgh4,  double& xh2,
            //       double& xh3,   double& xi2,   double& xi3,    double& xl2,   double& xl3,
            //       double& xl4,
            //       // not
            //       double& nm,    double& z1,     double& z2,    double& z3,
            //       double& z11,   double& z12,   double& z13,    double& z21,   double& z22,
            //       double& z23,   double& z31,   double& z32,    double& z33,
            //       // SGP4SatData
            //       double& zmol, double& zmos
            )
    {
        // Return variables not in SGP4SatData ----------------------------
        double snodm, cnodm, sinim, cosim, sinomm, cosomm, day, em, emsq, gam, rtemsq,
                s1, s2, s3, s4, s5, s6, s7, ss1, ss2, ss3, ss4, ss5, ss6, ss7,
                sz1, sz2, sz3, sz11, sz12, sz13, sz21, sz22, sz23, sz31, sz32, sz33,
                nm, z1, z2, z3, z11, z12, z13, z21, z22, z23, z31, z32, z33;

        // SEG : it is okay to initalize these here as they are not assigned before this function is called
        s1 = 0;
        s2 = 0;
        s3 = 0;
        s4 = 0;
        s5 = 0;
        s6 = 0;
        s7 = 0;
        ss1 = 0;
        ss2 = 0;
        ss3 = 0;
        ss4 = 0;
        ss5 = 0;
        ss6 = 0;
        ss7 = 0;
        sz1 = 0;
        sz2 = 0;
        sz3 = 0;
        sz11 = 0;
        sz12 = 0;
        sz13 = 0;
        sz21 = 0;
        sz22 = 0;
        sz23 = 0;
        sz31 = 0;
        sz32 = 0;
        sz33 = 0;
        z1 = 0;
        z2 = 0;
        z3 = 0;
        z11 = 0;
        z12 = 0;
        z13 = 0;
        z21 = 0;
        z22 = 0;
        z23 = 0;
        z31 = 0;
        z32 = 0;
        z33 = 0;

        /* -------------------------- constants ------------------------- */
        final double zes = 0.01675;
        final double zel = 0.05490;
        final double c1ss = 2.9864797e-6;
        final double c1l = 4.7968065e-7;
        final double zsinis = 0.39785416;
        final double zcosis = 0.91744867;
        final double zcosgs = 0.1945905;
        final double zsings = -0.98088458;
        final double twopi = 2.0 * pi;

        /* --------------------- local variables ------------------------ */
        int lsflg;
        double a1, a2, a3, a4, a5, a6, a7,
                a8, a9, a10, betasq, cc, ctem, stem,
                x1, x2, x3, x4, x5, x6, x7,
                x8, xnodce, xnoi, zcosg, zcosgl, zcosh, zcoshl,
                zcosi, zcosil, zsing, zsingl, zsinh, zsinhl, zsini,
                zsinil, zx, zy;

        nm = np;
        em = ep;
        snodm = Math.sin(nodep);
        cnodm = Math.cos(nodep);
        sinomm = Math.sin(argpp);
        cosomm = Math.cos(argpp);
        sinim = Math.sin(inclp);
        cosim = Math.cos(inclp);
        emsq = em * em;
        betasq = 1.0 - emsq;
        rtemsq = Math.sqrt(betasq);

        /* ----------------- initialize lunar solar terms --------------- */
        satrec.peo = 0.0;
        satrec.pinco = 0.0;
        satrec.plo = 0.0;
        satrec.pgho = 0.0;
        satrec.pho = 0.0;
        day = epoch + 18261.5 + tc / 1440.0;
        xnodce = ((4.5236020 - 9.2422029e-4 * day) % twopi);
        stem = Math.sin(xnodce);
        ctem = Math.cos(xnodce);
        zcosil = 0.91375164 - 0.03568096 * ctem;
        zsinil = Math.sqrt(1.0 - zcosil * zcosil);
        zsinhl = 0.089683511 * stem / zsinil;
        zcoshl = Math.sqrt(1.0 - zsinhl * zsinhl);
        gam = 5.8351514 + 0.0019443680 * day;
        zx = 0.39785416 * stem / zsinil;
        zy = zcoshl * ctem + 0.91744867 * zsinhl * stem;
        zx = Math.atan2(zx, zy);
        zx = gam + zx - xnodce;
        zcosgl = Math.cos(zx);
        zsingl = Math.sin(zx);

        /* ------------------------- do solar terms --------------------- */
        zcosg = zcosgs;
        zsing = zsings;
        zcosi = zcosis;
        zsini = zsinis;
        zcosh = cnodm;
        zsinh = snodm;
        cc = c1ss;
        xnoi = 1.0 / nm;

        for(lsflg = 1; lsflg <= 2; lsflg++)
        {
            a1 = zcosg * zcosh + zsing * zcosi * zsinh;
            a3 = -zsing * zcosh + zcosg * zcosi * zsinh;
            a7 = -zcosg * zsinh + zsing * zcosi * zcosh;
            a8 = zsing * zsini;
            a9 = zsing * zsinh + zcosg * zcosi * zcosh;
            a10 = zcosg * zsini;
            a2 = cosim * a7 + sinim * a8;
            a4 = cosim * a9 + sinim * a10;
            a5 = -sinim * a7 + cosim * a8;
            a6 = -sinim * a9 + cosim * a10;

            x1 = a1 * cosomm + a2 * sinomm;
            x2 = a3 * cosomm + a4 * sinomm;
            x3 = -a1 * sinomm + a2 * cosomm;
            x4 = -a3 * sinomm + a4 * cosomm;
            x5 = a5 * sinomm;
            x6 = a6 * sinomm;
            x7 = a5 * cosomm;
            x8 = a6 * cosomm;

            z31 = 12.0 * x1 * x1 - 3.0 * x3 * x3;
            z32 = 24.0 * x1 * x2 - 6.0 * x3 * x4;
            z33 = 12.0 * x2 * x2 - 3.0 * x4 * x4;
            z1 = 3.0 * (a1 * a1 + a2 * a2) + z31 * emsq;
            z2 = 6.0 * (a1 * a3 + a2 * a4) + z32 * emsq;
            z3 = 3.0 * (a3 * a3 + a4 * a4) + z33 * emsq;
            z11 = -6.0 * a1 * a5 + emsq * (-24.0 * x1 * x7 - 6.0 * x3 * x5);
            z12 = -6.0 * (a1 * a6 + a3 * a5) + emsq *
                    (-24.0 * (x2 * x7 + x1 * x8) - 6.0 * (x3 * x6 + x4 * x5));
            z13 = -6.0 * a3 * a6 + emsq * (-24.0 * x2 * x8 - 6.0 * x4 * x6);
            z21 = 6.0 * a2 * a5 + emsq * (24.0 * x1 * x5 - 6.0 * x3 * x7);
            z22 = 6.0 * (a4 * a5 + a2 * a6) + emsq *
                    (24.0 * (x2 * x5 + x1 * x6) - 6.0 * (x4 * x7 + x3 * x8));
            z23 = 6.0 * a4 * a6 + emsq * (24.0 * x2 * x6 - 6.0 * x4 * x8);
            z1 = z1 + z1 + betasq * z31;
            z2 = z2 + z2 + betasq * z32;
            z3 = z3 + z3 + betasq * z33;
            s3 = cc * xnoi;
            s2 = -0.5 * s3 / rtemsq;
            s4 = s3 * rtemsq;
            s1 = -15.0 * em * s4;
            s5 = x1 * x3 + x2 * x4;
            s6 = x2 * x3 + x1 * x4;
            s7 = x2 * x4 - x1 * x3;

            /* ----------------------- do lunar terms ------------------- */
            if(lsflg == 1)
            {
                ss1 = s1;
                ss2 = s2;
                ss3 = s3;
                ss4 = s4;
                ss5 = s5;
                ss6 = s6;
                ss7 = s7;
                sz1 = z1;
                sz2 = z2;
                sz3 = z3;
                sz11 = z11;
                sz12 = z12;
                sz13 = z13;
                sz21 = z21;
                sz22 = z22;
                sz23 = z23;
                sz31 = z31;
                sz32 = z32;
                sz33 = z33;
                zcosg = zcosgl;
                zsing = zsingl;
                zcosi = zcosil;
                zsini = zsinil;
                zcosh = zcoshl * cnodm + zsinhl * snodm;
                zsinh = snodm * zcoshl - cnodm * zsinhl;
                cc = c1l;
            }
        }

        satrec.zmol = ((4.7199672 + 0.22997150 * day - gam) % twopi);
        satrec.zmos = ((6.2565837 + 0.017201977 * day) % twopi);

        /* ------------------------ do solar terms ---------------------- */
        satrec.se2 = 2.0 * ss1 * ss6;
        satrec.se3 = 2.0 * ss1 * ss7;
        satrec.si2 = 2.0 * ss2 * sz12;
        satrec.si3 = 2.0 * ss2 * (sz13 - sz11);
        satrec.sl2 = -2.0 * ss3 * sz2;
        satrec.sl3 = -2.0 * ss3 * (sz3 - sz1);
        satrec.sl4 = -2.0 * ss3 * (-21.0 - 9.0 * emsq) * zes;
        satrec.sgh2 = 2.0 * ss4 * sz32;
        satrec.sgh3 = 2.0 * ss4 * (sz33 - sz31);
        satrec.sgh4 = -18.0 * ss4 * zes;
        satrec.sh2 = -2.0 * ss2 * sz22;
        satrec.sh3 = -2.0 * ss2 * (sz23 - sz21);

        /* ------------------------ do lunar terms ---------------------- */
        satrec.ee2 = 2.0 * s1 * s6;
        satrec.e3 = 2.0 * s1 * s7;
        satrec.xi2 = 2.0 * s2 * z12;
        satrec.xi3 = 2.0 * s2 * (z13 - z11);
        satrec.xl2 = -2.0 * s3 * z2;
        satrec.xl3 = -2.0 * s3 * (z3 - z1);
        satrec.xl4 = -2.0 * s3 * (-21.0 - 9.0 * emsq) * zel;
        satrec.xgh2 = 2.0 * s4 * z32;
        satrec.xgh3 = 2.0 * s4 * (z33 - z31);
        satrec.xgh4 = -18.0 * s4 * zel;
        satrec.xh2 = -2.0 * s2 * z22;
        satrec.xh3 = -2.0 * s2 * (z23 - z21);

        return new double[]
                {
                    snodm, cnodm, sinim, cosim, sinomm, cosomm, day, em, emsq, gam, rtemsq, s1, s2, s3,
                    s4, s5, s6, s7, ss1, ss2, ss3, ss4, ss5, ss6, ss7, sz1, sz2, sz3, sz11, sz12, sz13,
                    sz21, sz22, sz23, sz31, sz32, sz33, nm, z1, z2, z3, z11, z12, z13, z21, z22, z23, z31, z32, z33
                };

//#include "debug2.cpp"
    }  // end dscom

    /*-----------------------------------------------------------------------------
     * Java version returns a double array with these values: [ em, argpm, inclm, mm, nm, nodem, dndt]
     * -----------------------------------------------------------------------------
     *                           procedure dsinit
     *
     *  this procedure provides deep space contributions to mean motion dot due
     *    to geopotential resonance with half day and one day orbits.
     *
     *  author        : david vallado                  719-573-2600   28 jun 2005
     *
     *  inputs        :
     *    cosim, sinim-
     *    emsq        - eccentricity squared
     *    argpo       - argument of perigee
     *    s1, s2, s3, s4, s5      -
     *    ss1, ss2, ss3, ss4, ss5 -
     *    sz1, sz3, sz11, sz13, sz21, sz23, sz31, sz33 -
     *    t           - time
     *    tc          -
     *    gsto        - greenwich sidereal time                   rad
     *    mo          - mean anomaly
     *    mdot        - mean anomaly dot (rate)
     *    no          - mean motion
     *    nodeo       - right ascension of ascending node
     *    nodedot     - right ascension of ascending node dot (rate)
     *    xpidot      -
     *    z1, z3, z11, z13, z21, z23, z31, z33 -
     *    eccm        - eccentricity
     *    argpm       - argument of perigee
     *    inclm       - inclination
     *    mm          - mean anomaly
     *    xn          - mean motion
     *    nodem       - right ascension of ascending node
     *
     *  outputs       :
     *    em          - eccentricity
     *    argpm       - argument of perigee
     *    inclm       - inclination
     *    mm          - mean anomaly
     *    nm          - mean motion
     *    nodem       - right ascension of ascending node
     *    irez        - flag for resonance           0-none, 1-one day, 2-half day
     *    atime       -
     *    d2201, d2211, d3210, d3222, d4410, d4422, d5220, d5232, d5421, d5433    -
     *    dedt        -
     *    didt        -
     *    dmdt        -
     *    dndt        -
     *    dnodt       -
     *    domdt       -
     *    del1, del2, del3        -
     *    ses  , sghl , sghs , sgs  , shl  , shs  , sis  , sls
     *    theta       -
     *    xfact       -
     *    xlamo       -
     *    xli         -
     *    xni
     *
     *  locals        :
     *    ainv2       -
     *    aonv        -
     *    cosisq      -
     *    eoc         -
     *    f220, f221, f311, f321, f322, f330, f441, f442, f522, f523, f542, f543  -
     *    g200, g201, g211, g300, g310, g322, g410, g422, g520, g521, g532, g533  -
     *    sini2       -
     *    temp        -
     *    temp1       -
     *    theta       -
     *    xno2        -
     *
     *  coupling      :
     *    getgravconst
     *
     *  references    :
     *    hoots, roehrich, norad spacetrack report #3 1980
     *    hoots, norad spacetrack report #6 1986
     *    hoots, schumacher and glover 2004
     *    vallado, crawford, hujsak, kelso  2006
    ----------------------------------------------------------------------------*/
// returns array containing:
// [ em, argpm, inclm, mm, nm, nodem, dndt]
    private static double[] dsinit(
            Gravconsttype whichconst,
            double cosim, double emsq, double argpo, double s1, double s2,
            double s3, double s4, double s5, double sinim, double ss1,
            double ss2, double ss3, double ss4, double ss5, double sz1,
            double sz3, double sz11, double sz13, double sz21, double sz23,
            double sz31, double sz33, double t, double tc, double gsto,
            double mo, double mdot, double no, double nodeo, double nodedot,
            double xpidot, double z1, double z3, double z11, double z13,
            double z21, double z23, double z31, double z33, double ecco,
            double eccsq,
            // return
            SGP4SatData satrec,
            double em, // variable is also an INPUT and output - SEG!!
            double argpm,
            double inclm,
            double mm,
            double nm, // variable is also an INPUT and output - SEG!!
            double nodem //       // not in SGP4SatData
            //       double& em,    double& argpm,  double& inclm, double& mm,
            //       double& nm,    double& nodem,
            //       // in
            //       int& irez,
            //       double& atime, double& d2201, double& d2211,  double& d3210, double& d3222,
            //       double& d4410, double& d4422, double& d5220,  double& d5232, double& d5421,
            //       double& d5433, double& dedt,  double& didt,   double& dmdt,
            //       // not
            //       double& dndt,
            //       // in
            //       double& dnodt, double& domdt, double& del1,   double& del2,  double& del3,
            //       double& xfact, double& xlamo, double& xli,    double& xni
            )
    {
        // return values ------------------
        double dndt; // em,nm, inclm, argpm, nodem, mm are also inputs

        /* --------------------- local variables ------------------------ */
        final double twopi = 2.0 * pi;

        double ainv2, aonv = 0.0, cosisq, eoc, f220, f221, f311,
                f321, f322, f330, f441, f442, f522, f523,
                f542, f543, g200, g201, g211, g300, g310,
                g322, g410, g422, g520, g521, g532, g533,
                ses, sgs, sghl, sghs, shs, shll, sis,
                sini2, sls, temp, temp1, theta, xno2, q22,
                q31, q33, root22, root44, root54, rptim, root32,
                root52, x2o3, xke, znl, emo, zns, emsqo,
                tumin, mu, radiusearthkm, j2, j3, j4, j3oj2;

        q22 = 1.7891679e-6;
        q31 = 2.1460748e-6;
        q33 = 2.2123015e-7;
        root22 = 1.7891679e-6;
        root44 = 7.3636953e-9;
        root54 = 2.1765803e-9;
        rptim = 4.37526908801129966e-3; // this equates to 7.29211514668855e-5 rad/sec
        root32 = 3.7393792e-7;
        root52 = 1.1428639e-7;
        x2o3 = 2.0 / 3.0;
        znl = 1.5835218e-4;
        zns = 1.19459e-5;

        // sgp4fix identify constants and allow alternate values
        double[] temp2 = getgravconst(whichconst);
        tumin = temp2[0];
        mu = temp2[1];
        radiusearthkm = temp2[2];
        xke = temp2[3];
        j2 = temp2[4];
        j3 = temp2[5];
        j4 = temp2[6];
        j3oj2 = temp2[7];

        /* -------------------- deep space initialization ------------ */
        satrec.irez = 0;
        if((nm < 0.0052359877) && (nm > 0.0034906585))
        {
            satrec.irez = 1;
        }
        if((nm >= 8.26e-3) && (nm <= 9.24e-3) && (em >= 0.5))
        {
            satrec.irez = 2;
        }

        /* ------------------------ do solar terms ------------------- */
        ses = ss1 * zns * ss5;
        sis = ss2 * zns * (sz11 + sz13);
        sls = -zns * ss3 * (sz1 + sz3 - 14.0 - 6.0 * emsq);
        sghs = ss4 * zns * (sz31 + sz33 - 6.0);
        shs = -zns * ss2 * (sz21 + sz23);
        // sgp4fix for 180 deg incl
        if((inclm < 5.2359877e-2) || (inclm > pi - 5.2359877e-2))
        {
            shs = 0.0;
        }
        if(sinim != 0.0)
        {
            shs = shs / sinim;
        }
        sgs = sghs - cosim * shs;

        /* ------------------------- do lunar terms ------------------ */
        satrec.dedt = ses + s1 * znl * s5;
        satrec.didt = sis + s2 * znl * (z11 + z13);
        satrec.dmdt = sls - znl * s3 * (z1 + z3 - 14.0 - 6.0 * emsq);
        sghl = s4 * znl * (z31 + z33 - 6.0);
        shll = -znl * s2 * (z21 + z23);
        // sgp4fix for 180 deg incl
        if((inclm < 5.2359877e-2) || (inclm > pi - 5.2359877e-2))
        {
            shll = 0.0;
        }
        satrec.domdt = sgs + sghl;
        satrec.dnodt = shs;
        if(sinim != 0.0)
        {
            satrec.domdt = satrec.domdt - cosim / sinim * shll;
            satrec.dnodt = satrec.dnodt + shll / sinim;
        }

        /* ----------- calculate deep space resonance effects -------- */
        dndt = 0.0;
        theta = ((gsto + tc * rptim) % twopi);
        em = em + satrec.dedt * t;
        inclm = inclm + satrec.didt * t;
        argpm = argpm + satrec.domdt * t;
        nodem = nodem + satrec.dnodt * t;
        mm = mm + satrec.dmdt * t;
        //   sgp4fix for negative inclinations
        //   the following if statement should be commented out
        //if (inclm < 0.0)
        //  {
        //    inclm  = -inclm;
        //    argpm  = argpm - pi;
        //    nodem = nodem + pi;
        //  }

        /* -------------- initialize the resonance terms ------------- */
        if(satrec.irez != 0)
        {
            aonv = Math.pow(nm / xke, x2o3);

            /* ---------- geopotential resonance for 12 hour orbits ------ */
            if(satrec.irez == 2)
            {
                cosisq = cosim * cosim;
                emo = em;
                em = ecco;
                emsqo = emsq;
                emsq = eccsq;
                eoc = em * emsq;
                g201 = -0.306 - (em - 0.64) * 0.440;

                if(em <= 0.65)
                {
                    g211 = 3.616 - 13.2470 * em + 16.2900 * emsq;
                    g310 = -19.302 + 117.3900 * em - 228.4190 * emsq + 156.5910 * eoc;
                    g322 = -18.9068 + 109.7927 * em - 214.6334 * emsq + 146.5816 * eoc;
                    g410 = -41.122 + 242.6940 * em - 471.0940 * emsq + 313.9530 * eoc;
                    g422 = -146.407 + 841.8800 * em - 1629.014 * emsq + 1083.4350 * eoc;
                    g520 = -532.114 + 3017.977 * em - 5740.032 * emsq + 3708.2760 * eoc;
                }
                else
                {
                    g211 = -72.099 + 331.819 * em - 508.738 * emsq + 266.724 * eoc;
                    g310 = -346.844 + 1582.851 * em - 2415.925 * emsq + 1246.113 * eoc;
                    g322 = -342.585 + 1554.908 * em - 2366.899 * emsq + 1215.972 * eoc;
                    g410 = -1052.797 + 4758.686 * em - 7193.992 * emsq + 3651.957 * eoc;
                    g422 = -3581.690 + 16178.110 * em - 24462.770 * emsq + 12422.520 * eoc;
                    if(em > 0.715)
                    {
                        g520 = -5149.66 + 29936.92 * em - 54087.36 * emsq + 31324.56 * eoc;
                    }
                    else
                    {
                        g520 = 1464.74 - 4664.75 * em + 3763.64 * emsq;
                    }
                }
                if(em < 0.7)
                {
                    g533 = -919.22770 + 4988.6100 * em - 9064.7700 * emsq + 5542.21 * eoc;
                    g521 = -822.71072 + 4568.6173 * em - 8491.4146 * emsq + 5337.524 * eoc;
                    g532 = -853.66600 + 4690.2500 * em - 8624.7700 * emsq + 5341.4 * eoc;
                }
                else
                {
                    g533 = -37995.780 + 161616.52 * em - 229838.20 * emsq + 109377.94 * eoc;
                    g521 = -51752.104 + 218913.95 * em - 309468.16 * emsq + 146349.42 * eoc;
                    g532 = -40023.880 + 170470.89 * em - 242699.48 * emsq + 115605.82 * eoc;
                }

                sini2 = sinim * sinim;
                f220 = 0.75 * (1.0 + 2.0 * cosim + cosisq);
                f221 = 1.5 * sini2;
                f321 = 1.875 * sinim * (1.0 - 2.0 * cosim - 3.0 * cosisq);
                f322 = -1.875 * sinim * (1.0 + 2.0 * cosim - 3.0 * cosisq);
                f441 = 35.0 * sini2 * f220;
                f442 = 39.3750 * sini2 * sini2;
                f522 = 9.84375 * sinim * (sini2 * (1.0 - 2.0 * cosim - 5.0 * cosisq) +
                        0.33333333 * (-2.0 + 4.0 * cosim + 6.0 * cosisq));
                f523 = sinim * (4.92187512 * sini2 * (-2.0 - 4.0 * cosim +
                        10.0 * cosisq) + 6.56250012 * (1.0 + 2.0 * cosim - 3.0 * cosisq));
                f542 = 29.53125 * sinim * (2.0 - 8.0 * cosim + cosisq *
                        (-12.0 + 8.0 * cosim + 10.0 * cosisq));
                f543 = 29.53125 * sinim * (-2.0 - 8.0 * cosim + cosisq *
                        (12.0 + 8.0 * cosim - 10.0 * cosisq));
                xno2 = nm * nm;
                ainv2 = aonv * aonv;
                temp1 = 3.0 * xno2 * ainv2;
                temp = temp1 * root22;
                satrec.d2201 = temp * f220 * g201;
                satrec.d2211 = temp * f221 * g211;
                temp1 = temp1 * aonv;
                temp = temp1 * root32;
                satrec.d3210 = temp * f321 * g310;
                satrec.d3222 = temp * f322 * g322;
                temp1 = temp1 * aonv;
                temp = 2.0 * temp1 * root44;
                satrec.d4410 = temp * f441 * g410;
                satrec.d4422 = temp * f442 * g422;
                temp1 = temp1 * aonv;
                temp = temp1 * root52;
                satrec.d5220 = temp * f522 * g520;
                satrec.d5232 = temp * f523 * g532;
                temp = 2.0 * temp1 * root54;
                satrec.d5421 = temp * f542 * g521;
                satrec.d5433 = temp * f543 * g533;
                satrec.xlamo = ((mo + nodeo + nodeo - theta - theta) % twopi);
                satrec.xfact = mdot + satrec.dmdt + 2.0 * (nodedot + satrec.dnodt - rptim) - no;
                em = emo;
                emsq = emsqo;
            }

            /* ---------------- synchronous resonance terms -------------- */
            if(satrec.irez == 1)
            {
                g200 = 1.0 + emsq * (-2.5 + 0.8125 * emsq);
                g310 = 1.0 + 2.0 * emsq;
                g300 = 1.0 + emsq * (-6.0 + 6.60937 * emsq);
                f220 = 0.75 * (1.0 + cosim) * (1.0 + cosim);
                f311 = 0.9375 * sinim * sinim * (1.0 + 3.0 * cosim) - 0.75 * (1.0 + cosim);
                f330 = 1.0 + cosim;
                f330 = 1.875 * f330 * f330 * f330;
                satrec.del1 = 3.0 * nm * nm * aonv * aonv;
                satrec.del2 = 2.0 * satrec.del1 * f220 * g200 * q22;
                satrec.del3 = 3.0 * satrec.del1 * f330 * g300 * q33 * aonv;
                satrec.del1 = satrec.del1 * f311 * g310 * q31 * aonv;
                satrec.xlamo = ((mo + nodeo + argpo - theta) % twopi);
                satrec.xfact = mdot + xpidot - rptim + satrec.dmdt + satrec.domdt + satrec.dnodt - no;
            }

            /* ------------ for sgp4, initialize the integrator ---------- */
            satrec.xli = satrec.xlamo;
            satrec.xni = no;
            satrec.atime = 0.0;
            nm = no + dndt;
        }

        return new double[]
                {
                    em, argpm, inclm, mm, nm, nodem, dndt
                };

//#include "debug3.cpp"
    }  // end dsinit

    /*-----------------------------------------------------------------------------
     *
     *                           procedure dspace
     *
     *  this procedure provides deep space contributions to mean elements for
     *    perturbing third body.  these effects have been averaged over one
     *    revolution of the sun and moon.  for earth resonance effects, the
     *    effects have been averaged over no revolutions of the satellite.
     *    (mean motion)
     *
     *  author        : david vallado                  719-573-2600   28 jun 2005
     *
     *  inputs        :
     *    d2201, d2211, d3210, d3222, d4410, d4422, d5220, d5232, d5421, d5433 -
     *    dedt        -
     *    del1, del2, del3  -
     *    didt        -
     *    dmdt        -
     *    dnodt       -
     *    domdt       -
     *    irez        - flag for resonance           0-none, 1-one day, 2-half day
     *    argpo       - argument of perigee
     *    argpdot     - argument of perigee dot (rate)
     *    t           - time
     *    tc          -
     *    gsto        - gst
     *    xfact       -
     *    xlamo       -
     *    no          - mean motion
     *    atime       -
     *    em          - eccentricity
     *    ft          -
     *    argpm       - argument of perigee
     *    inclm       - inclination
     *    xli         -
     *    mm          - mean anomaly
     *    xni         - mean motion
     *    nodem       - right ascension of ascending node
     *
     *  outputs       :
     *    atime       -
     *    em          - eccentricity
     *    argpm       - argument of perigee
     *    inclm       - inclination
     *    xli         -
     *    mm          - mean anomaly
     *    xni         -
     *    nodem       - right ascension of ascending node
     *    dndt        -
     *    nm          - mean motion
     *
     *  locals        :
     *    delt        -
     *    ft          -
     *    theta       -
     *    x2li        -
     *    x2omi       -
     *    xl          -
     *    xldot       -
     *    xnddt       -
     *    xndt        -
     *    xomi        -
     *
     *  coupling      :
     *    none        -
     *
     *  references    :
     *    hoots, roehrich, norad spacetrack report #3 1980
     *    hoots, norad spacetrack report #6 1986
     *    hoots, schumacher and glover 2004
     *    vallado, crawford, hujsak, kelso  2006
    ----------------------------------------------------------------------------*/
// nm - also added as an input since it may not be changed and it needs to retain its old value
// returns array with these values:
// [em, argpm, inclm, mm,nodem, dndt, nm]
    private static double[] dspace(
            int irez,
            double d2201, double d2211, double d3210, double d3222, double d4410,
            double d4422, double d5220, double d5232, double d5421, double d5433,
            double dedt, double del1, double del2, double del3, double didt,
            double dmdt, double dnodt, double domdt, double argpo, double argpdot,
            double t, double tc, double gsto, double xfact, double xlamo,
            double no,//
            // These variables are both inputs and returned as outputs
            double em,
            double argpm,
            double inclm,
            double mm,
            double nodem,
            // data that is retained with the SGP4SatData object
            SGP4SatData satrec,
            //       double& atime, double& em,    double& argpm,  double& inclm, double& xli,
            //       double& mm,    double& xni,   double& nodem,  double& dndt,  double& nm
            double nm // input and output
            )
    {
        // variables that are both inputs and outputs! included in SGP4SatData
        // atime, em, argpm, inclm, xli, mm, xni, nodem

        // Return variables (that are not also inputs)---------------------
        double dndt;

        final double twopi = 2.0 * pi;
        int iretn, iret;
        double delt, ft, theta, x2li, x2omi, xl, xldot, xnddt, xndt, xomi, g22, g32,
                g44, g52, g54, fasx2, fasx4, fasx6, rptim, step2, stepn, stepp;

        // SEG -- it is okay to initalize these values here as they are assigned values
        xndt = 0;
        xnddt = 0;
        xldot = 0;

        fasx2 = 0.13130908;
        fasx4 = 2.8843198;
        fasx6 = 0.37448087;
        g22 = 5.7686396;
        g32 = 0.95240898;
        g44 = 1.8014998;
        g52 = 1.0508330;
        g54 = 4.4108898;
        rptim = 4.37526908801129966e-3; // this equates to 7.29211514668855e-5 rad/sec
        stepp = 720.0;
        stepn = -720.0;
        step2 = 259200.0;

        /* ----------- calculate deep space resonance effects ----------- */
        dndt = 0.0;
        theta = ((gsto + tc * rptim) % twopi);
        em = em + dedt * t;

        inclm = inclm + didt * t;
        argpm = argpm + domdt * t;
        nodem = nodem + dnodt * t;
        mm = mm + dmdt * t;

        //   sgp4fix for negative inclinations
        //   the following if statement should be commented out
        //  if (inclm < 0.0)
        // {
        //    inclm = -inclm;
        //    argpm = argpm - pi;
        //    nodem = nodem + pi;
        //  }

        /* - update resonances : numerical (euler-maclaurin) integration - */
        /* ------------------------- epoch restart ----------------------  */
        //   sgp4fix for propagator problems
        //   the following integration works for negative time steps and periods
        //   the specific changes are unknown because the original code was so convoluted

        // sgp4fix take out atime = 0.0 and fix for faster operation
        ft = 0.0;
        if(irez != 0)
        {
            // sgp4fix streamline check
            if((satrec.atime == 0.0) || (t * satrec.atime <= 0.0) || (Math.abs(t) < Math.abs(satrec.atime)))
            {
                satrec.atime = 0.0;
                satrec.xni = no;
                satrec.xli = xlamo;
            }
            // sgp4fix move check outside loop
            if(t > 0.0)
            {
                delt = stepp;
            }
            else
            {
                delt = stepn;
            }

            iretn = 381; // added for do loop
            iret = 0; // added for loop
            while(iretn == 381)
            {
                /* ------------------- dot terms calculated ------------- */
                /* ----------- near - synchronous resonance terms ------- */
                if(irez != 2)
                {
                    xndt = del1 * Math.sin(satrec.xli - fasx2) + del2 * Math.sin(2.0 * (satrec.xli - fasx4)) +
                            del3 * Math.sin(3.0 * (satrec.xli - fasx6));
                    xldot = satrec.xni + xfact;
                    xnddt = del1 * Math.cos(satrec.xli - fasx2) +
                            2.0 * del2 * Math.cos(2.0 * (satrec.xli - fasx4)) +
                            3.0 * del3 * Math.cos(3.0 * (satrec.xli - fasx6));
                    xnddt = xnddt * xldot;
                }
                else
                {
                    /* --------- near - half-day resonance terms -------- */
                    xomi = argpo + argpdot * satrec.atime;
                    x2omi = xomi + xomi;
                    x2li = satrec.xli + satrec.xli;
                    xndt = d2201 * Math.sin(x2omi + satrec.xli - g22) + d2211 * Math.sin(satrec.xli - g22) +
                            d3210 * Math.sin(xomi + satrec.xli - g32) + d3222 * Math.sin(-xomi + satrec.xli - g32) +
                            d4410 * Math.sin(x2omi + x2li - g44) + d4422 * Math.sin(x2li - g44) +
                            d5220 * Math.sin(xomi + satrec.xli - g52) + d5232 * Math.sin(-xomi + satrec.xli - g52) +
                            d5421 * Math.sin(xomi + x2li - g54) + d5433 * Math.sin(-xomi + x2li - g54);
                    xldot = satrec.xni + xfact;
                    xnddt = d2201 * Math.cos(x2omi + satrec.xli - g22) + d2211 * Math.cos(satrec.xli - g22) +
                            d3210 * Math.cos(xomi + satrec.xli - g32) + d3222 * Math.cos(-xomi + satrec.xli - g32) +
                            d5220 * Math.cos(xomi + satrec.xli - g52) + d5232 * Math.cos(-xomi + satrec.xli - g52) +
                            2.0 * (d4410 * Math.cos(x2omi + x2li - g44) +
                            d4422 * Math.cos(x2li - g44) + d5421 * Math.cos(xomi + x2li - g54) +
                            d5433 * Math.cos(-xomi + x2li - g54));
                    xnddt = xnddt * xldot;
                }

                /* ----------------------- integrator ------------------- */
                // sgp4fix move end checks to end of routine
                if(Math.abs(t - satrec.atime) >= stepp)
                {
                    iret = 0;
                    iretn = 381;
                }
                else // exit here
                {
                    ft = t - satrec.atime;
                    iretn = 0;
                }

                if(iretn == 381)
                {
                    satrec.xli = satrec.xli + xldot * delt + xndt * step2;
                    satrec.xni = satrec.xni + xndt * delt + xnddt * step2;
                    satrec.atime = satrec.atime + delt;
                }
            }  // while iretn = 381

            nm = satrec.xni + xndt * ft + xnddt * ft * ft * 0.5;
            xl = satrec.xli + xldot * ft + xndt * ft * ft * 0.5;
            if(irez != 1)
            {
                mm = xl - 2.0 * nodem + 2.0 * theta;
                dndt = nm - no;
            }
            else
            {
                mm = xl - nodem - argpm + theta;
                dndt = nm - no;
            }
            nm = no + dndt;
        }

        return new double[]
                {
                    em, argpm, inclm, mm, nodem, dndt, nm
                };

//#include "debug4.cpp"
    }  // end dsspace

    /** -----------------------------------------------------------------------------
     *
     *                           procedure initl
     *
     *  this procedure initializes the spg4 propagator. all the initialization is
     *    consolidated here instead of having multiple loops inside other routines.
     *
     *  author        : david vallado                  719-573-2600   28 jun 2005
     *
     *  inputs        :
     *    ecco        - eccentricity                           0.0 - 1.0
     *    epoch       - epoch time in days from jan 0, 1950. 0 hr
     *    inclo       - inclination of satellite
     *    no          - mean motion of satellite
     *    satn        - satellite number
     *
     *  outputs       :
     *    ainv        - 1.0 / a
     *    ao          - semi major axis
     *    con41       -
     *    con42       - 1.0 - 5.0 cos(i)
     *    cosio       - cosine of inclination
     *    cosio2      - cosio squared
     *    eccsq       - eccentricity squared
     *    method      - flag for deep space                    'd', 'n'
     *    omeosq      - 1.0 - ecco * ecco
     *    posq        - semi-parameter squared
     *    rp          - radius of perigee
     *    rteosq      - square root of (1.0 - ecco*ecco)
     *    sinio       - sine of inclination
     *    gsto        - gst at time of observation               rad
     *    no          - mean motion of satellite
     *
     *  locals        :
     *    ak          -
     *    d1          -
     *    del         -
     *    adel        -
     *    po          -
     *
     *  coupling      :
     *    getgravconst
     *    gstime      - find greenwich sidereal time from the julian date
     *
     *  references    :
     *    hoots, roehrich, norad spacetrack report #3 1980
     *    hoots, norad spacetrack report #6 1986
     *    hoots, schumacher and glover 2004
     *    vallado, crawford, hujsak, kelso  2006
     * ----------------------------------------------------------------------------
     * @param satn satellite number
     * @param whichconst which constants set to use
     * @param ecco eccentricity (0,1)
     * @param epoch epoch time in days from jan 0, 1950. 0 hr
     * @param inclo inclination of satellite
     * @param satrec satellite object that stores needed SGP4 data
     * @return double array with these values: [ainv, ao, con42, cosio, cosio2, eccsq, omeosq, posq, rp, rteosq, sinio]
     */
// outputs not stored in SGP4SatData and are returned by this function:
// [ainv, ao, con42, cosio, cosio2, eccsq, omeosq, posq, rp, rteosq, sinio]
    public static double[] initl(
            int satn, Gravconsttype whichconst,
            double ecco, double epoch, double inclo,
            //
            SGP4SatData satrec //       //double& no,
            //       //char& method,
            //       double& ainv, double& ao,
            //       //double& con41,
            //       double& con42, double& cosio,
            //       double& cosio2,double& eccsq, double& omeosq, double& posq,
            //       double& rp,    double& rteosq,double& sinio ,
            //       //double& gsto, char opsmode
            )
    {
        // no is an input and an output
        // return variables ----------------
        double ainv, ao, con42, cosio, cosio2, eccsq, omeosq, posq, rp, rteosq, sinio;

        /* --------------------- local variables ------------------------ */
        double ak, d1, del, adel, po, x2o3, j2, xke,
                tumin, mu, radiusearthkm, j3, j4, j3oj2;

        // sgp4fix use old way of finding gst
        double ds70;
        double ts70, tfrac, c1, thgr70, fk5r, c1p2p, thgr, thgro;
        final double twopi = 2.0 * pi;

        /* ----------------------- earth constants ---------------------- */
        // sgp4fix identify constants and allow alternate values
        double[] temp2 = getgravconst(whichconst);
        tumin = temp2[0];
        mu = temp2[1];
        radiusearthkm = temp2[2];
        xke = temp2[3];
        j2 = temp2[4];
        j3 = temp2[5];
        j4 = temp2[6];
        j3oj2 = temp2[7];

        x2o3 = 2.0 / 3.0;

        /* ------------- calculate auxillary epoch quantities ---------- */
        eccsq = ecco * ecco;
        omeosq = 1.0 - eccsq;
        rteosq = Math.sqrt(omeosq);
        cosio = Math.cos(inclo);
        cosio2 = cosio * cosio;

        /* ------------------ un-kozai the mean motion ----------------- */
        ak = Math.pow(xke / satrec.no, x2o3);
        d1 = 0.75 * j2 * (3.0 * cosio2 - 1.0) / (rteosq * omeosq);
        del = d1 / (ak * ak);
        adel = ak * (1.0 - del * del - del *
                (1.0 / 3.0 + 134.0 * del * del / 81.0));
        del = d1 / (adel * adel);
        satrec.no = satrec.no / (1.0 + del);

        ao = Math.pow(xke / satrec.no, x2o3);
        sinio = Math.sin(inclo);
        po = ao * omeosq;
        con42 = 1.0 - 5.0 * cosio2;
        satrec.con41 = -con42 - cosio2 - cosio2;
        ainv = 1.0 / ao;
        posq = po * po;
        rp = ao * (1.0 - ecco);
        satrec.method = 'n';

        // sgp4fix modern approach to finding sidereal time
        if(satrec.operationmode == 'a')
        {
            // sgp4fix use old way of finding gst
            // count integer number of days from 0 jan 1970
            ts70 = epoch - 7305.0;
            ds70 = Math.floor(ts70 + 1.0e-8);
            tfrac = ts70 - ds70;
            // find greenwich location at epoch
            c1 = 1.72027916940703639e-2;
            thgr70 = 1.7321343856509374;
            fk5r = 5.07551419432269442e-15;
            c1p2p = c1 + twopi;
            satrec.gsto = ((thgr70 + c1 * ds70 + c1p2p * tfrac + ts70 * ts70 * fk5r) % twopi);
            if(satrec.gsto < 0.0)
            {
                satrec.gsto = satrec.gsto + twopi;
            }
        }
        else
        {
            satrec.gsto = gstime(epoch + 2433281.5);
        }

        return new double[]
                {
                    ainv, ao, con42, cosio, cosio2, eccsq, omeosq, posq, rp, rteosq, sinio
                };

//#include "debug5.cpp"
    }  // end initl

    /**-----------------------------------------------------------------------------
     * This method is called from within SGP4utils.readTLEandIniSGP4 and therefore not
     * typically called elsewhere.
     * -----------------------------------------------------------------------------
     *
     *                             procedure sgp4init
     *
     *  this procedure initializes variables for sgp4.
     *
     *  author        : david vallado                  719-573-2600   28 jun 2005
     *
     *  inputs        :
     *    opsmode     - mode of operation afspc or improved 'a', 'i'
     *    whichconst  - which set of constants to use  72, 84
     *    satn        - satellite number
     *    bstar       - sgp4 type drag coefficient              kg/m2er
     *    ecco        - eccentricity
     *    epoch       - epoch time in days from jan 0, 1950. 0 hr
     *    argpo       - argument of perigee (output if ds)
     *    inclo       - inclination
     *    mo          - mean anomaly (output if ds)
     *    no          - mean motion
     *    nodeo       - right ascension of ascending node
     *
     *  outputs       :
     *    satrec      - common values for subsequent calls
     *    return code - non-zero on error.
     *                   1 - mean elements, ecc >= 1.0 or ecc < -0.001 or a < 0.95 er
     *                   2 - mean motion less than 0.0
     *                   3 - pert elements, ecc < 0.0  or  ecc > 1.0
     *                   4 - semi-latus rectum < 0.0
     *                   5 - epoch elements are sub-orbital
     *                   6 - satellite has decayed
     *
     *  locals        :
     *    cnodm  , snodm  , cosim  , sinim  , cosomm , sinomm
     *    cc1sq  , cc2    , cc3
     *    coef   , coef1
     *    cosio4      -
     *    day         -
     *    dndt        -
     *    em          - eccentricity
     *    emsq        - eccentricity squared
     *    eeta        -
     *    etasq       -
     *    gam         -
     *    argpm       - argument of perigee
     *    nodem       -
     *    inclm       - inclination
     *    mm          - mean anomaly
     *    nm          - mean motion
     *    perige      - perigee
     *    pinvsq      -
     *    psisq       -
     *    qzms24      -
     *    rtemsq      -
     *    s1, s2, s3, s4, s5, s6, s7          -
     *    sfour       -
     *    ss1, ss2, ss3, ss4, ss5, ss6, ss7         -
     *    sz1, sz2, sz3
     *    sz11, sz12, sz13, sz21, sz22, sz23, sz31, sz32, sz33        -
     *    tc          -
     *    temp        -
     *    temp1, temp2, temp3       -
     *    tsi         -
     *    xpidot      -
     *    xhdot1      -
     *    z1, z2, z3          -
     *    z11, z12, z13, z21, z22, z23, z31, z32, z33         -
     *
     *  coupling      :
     *    getgravconst-
     *    initl       -
     *    dscom       -
     *    dpper       -
     *    dsinit      -
     *    sgp4        -
     *
     *  references    :
     *    hoots, roehrich, norad spacetrack report #3 1980
     *    hoots, norad spacetrack report #6 1986
     *    hoots, schumacher and glover 2004
     *    vallado, crawford, hujsak, kelso  2006
     * ----------------------------------------------------------------------------
     * @param whichconst
     * @param opsmode
     * @param satn
     * @param epoch
     * @param xbstar
     * @param xecco
     * @param xargpo
     * @param xinclo
     * @param xmo
     * @param xno
     * @param xnodeo
     * @param satrec
     * @return if initialization was successful
     */
    public static boolean sgp4init(
            Gravconsttype whichconst, char opsmode, final int satn, final double epoch,
            final double xbstar, final double xecco, final double xargpo,
            final double xinclo, final double xmo, final double xno,
            final double xnodeo, SGP4SatData satrec)
    {
        /* --------------------- local variables ------------------------ */
        double ao, ainv, con42, cosio, sinio, cosio2, eccsq,
                omeosq, posq, rp, rteosq,
                cnodm, snodm, cosim, sinim, cosomm, sinomm, cc1sq,
                cc2, cc3, coef, coef1, cosio4, day, dndt,
                em, emsq, eeta, etasq, gam, argpm, nodem,
                inclm, mm, nm, perige, pinvsq, psisq, qzms24,
                rtemsq, s1, s2, s3, s4, s5, s6,
                s7, sfour, ss1, ss2, ss3, ss4, ss5,
                ss6, ss7, sz1, sz2, sz3, sz11, sz12,
                sz13, sz21, sz22, sz23, sz31, sz32, sz33,
                tc, temp, temp1, temp2, temp3, tsi, xpidot,
                xhdot1, z1, z2, z3, z11, z12, z13,
                z21, z22, z23, z31, z32, z33,
                qzms2t, ss, j2, j3oj2, j4, x2o3, //r[3], v[3],
                tumin, mu, radiusearthkm, xke, j3;
        double[] r = new double[3];
        double[] v = new double[3];

        /* ------------------------ initialization --------------------- */
        // sgp4fix divisor for divide by zero check on inclination
        // the old check used 1.0 + cos(pi-1.0e-9), but then compared it to
        // 1.5 e-12, so the threshold was changed to 1.5e-12 for consistency
        final double temp4 = 1.5e-12;

        /* ----------- set all near earth variables to zero ------------ */
        satrec.isimp = 0;
        satrec.method = 'n';
        satrec.aycof = 0.0;
        satrec.con41 = 0.0;
        satrec.cc1 = 0.0;
        satrec.cc4 = 0.0;
        satrec.cc5 = 0.0;
        satrec.d2 = 0.0;
        satrec.d3 = 0.0;
        satrec.d4 = 0.0;
        satrec.delmo = 0.0;
        satrec.eta = 0.0;
        satrec.argpdot = 0.0;
        satrec.omgcof = 0.0;
        satrec.sinmao = 0.0;
        satrec.t = 0.0;
        satrec.t2cof = 0.0;
        satrec.t3cof = 0.0;
        satrec.t4cof = 0.0;
        satrec.t5cof = 0.0;
        satrec.x1mth2 = 0.0;
        satrec.x7thm1 = 0.0;
        satrec.mdot = 0.0;
        satrec.nodedot = 0.0;
        satrec.xlcof = 0.0;
        satrec.xmcof = 0.0;
        satrec.nodecf = 0.0;

        /* ----------- set all deep space variables to zero ------------ */
        satrec.irez = 0;
        satrec.d2201 = 0.0;
        satrec.d2211 = 0.0;
        satrec.d3210 = 0.0;
        satrec.d3222 = 0.0;
        satrec.d4410 = 0.0;
        satrec.d4422 = 0.0;
        satrec.d5220 = 0.0;
        satrec.d5232 = 0.0;
        satrec.d5421 = 0.0;
        satrec.d5433 = 0.0;
        satrec.dedt = 0.0;
        satrec.del1 = 0.0;
        satrec.del2 = 0.0;
        satrec.del3 = 0.0;
        satrec.didt = 0.0;
        satrec.dmdt = 0.0;
        satrec.dnodt = 0.0;
        satrec.domdt = 0.0;
        satrec.e3 = 0.0;
        satrec.ee2 = 0.0;
        satrec.peo = 0.0;
        satrec.pgho = 0.0;
        satrec.pho = 0.0;
        satrec.pinco = 0.0;
        satrec.plo = 0.0;
        satrec.se2 = 0.0;
        satrec.se3 = 0.0;
        satrec.sgh2 = 0.0;
        satrec.sgh3 = 0.0;
        satrec.sgh4 = 0.0;
        satrec.sh2 = 0.0;
        satrec.sh3 = 0.0;
        satrec.si2 = 0.0;
        satrec.si3 = 0.0;
        satrec.sl2 = 0.0;
        satrec.sl3 = 0.0;
        satrec.sl4 = 0.0;
        satrec.gsto = 0.0;
        satrec.xfact = 0.0;
        satrec.xgh2 = 0.0;
        satrec.xgh3 = 0.0;
        satrec.xgh4 = 0.0;
        satrec.xh2 = 0.0;
        satrec.xh3 = 0.0;
        satrec.xi2 = 0.0;
        satrec.xi3 = 0.0;
        satrec.xl2 = 0.0;
        satrec.xl3 = 0.0;
        satrec.xl4 = 0.0;
        satrec.xlamo = 0.0;
        satrec.zmol = 0.0;
        satrec.zmos = 0.0;
        satrec.atime = 0.0;
        satrec.xli = 0.0;
        satrec.xni = 0.0;

        // sgp4fix - note the following variables are also passed directly via satrec.
        // it is possible to streamline the sgp4init call by deleting the "x"
        // variables, but the user would need to set the satrec.* values first. we
        // include the additional assignments in case twoline2rv is not used.
        satrec.bstar = xbstar;
        satrec.ecco = xecco;
        satrec.argpo = xargpo;
        satrec.inclo = xinclo;
        satrec.mo = xmo;
        satrec.no = xno;
        satrec.nodeo = xnodeo;

        // sgp4fix add opsmode
        satrec.operationmode = opsmode;

        // SEG -- also save gravity constant type
        satrec.gravconsttype = whichconst;

        /* ------------------------ earth constants ----------------------- */
        // sgp4fix identify constants and allow alternate values
        double[] temp5 = getgravconst(whichconst);//, tumin, mu, radiusearthkm, xke, j2, j3, j4, j3oj2 );
        tumin = temp5[0];
        mu = temp5[1];
        radiusearthkm = temp5[2];
        xke = temp5[3];
        j2 = temp5[4];
        j3 = temp5[5];
        j4 = temp5[6];
        j3oj2 = temp5[7];

        ss = 78.0 / radiusearthkm + 1.0;
        qzms2t = Math.pow(((120.0 - 78.0) / radiusearthkm), 4);
        x2o3 = 2.0 / 3.0;

        satrec.init = 'y';
        satrec.t = 0.0;

        double[] ttemp = initl(satn, whichconst, satrec.ecco, epoch, satrec.inclo, satrec);
        ainv = ttemp[0];
        ao = ttemp[1];
        con42 = ttemp[2];
        cosio = ttemp[3];
        cosio2 = ttemp[4];
        eccsq = ttemp[5];
        omeosq = ttemp[6];
        posq = ttemp[7];
        rp = ttemp[8];
        rteosq = ttemp[9];
        sinio = ttemp[10];

        satrec.error = 0;

        // sgp4fix remove this check as it is unnecessary
        // the mrt check in sgp4 handles decaying satellite cases even if the starting
        // condition is below the surface of te earth
//     if (rp < 1.0)
//       {
//         printf("# *** satn%d epoch elts sub-orbital ***\n", satn);
//         satrec.error = 5;
//       }

        if((omeosq >= 0.0) || (satrec.no >= 0.0))
        {
            satrec.isimp = 0;
            if(rp < (220.0 / radiusearthkm + 1.0))
            {
                satrec.isimp = 1;
            }
            sfour = ss;
            qzms24 = qzms2t;
            perige = (rp - 1.0) * radiusearthkm;

            /* - for perigees below 156 km, s and qoms2t are altered - */
            if(perige < 156.0)
            {
                sfour = perige - 78.0;
                if(perige < 98.0)
                {
                    sfour = 20.0;
                }
                qzms24 = Math.pow(((120.0 - sfour) / radiusearthkm), 4.0);
                sfour = sfour / radiusearthkm + 1.0;
            }
            pinvsq = 1.0 / posq;

            tsi = 1.0 / (ao - sfour);
            satrec.eta = ao * satrec.ecco * tsi;
            etasq = satrec.eta * satrec.eta;
            eeta = satrec.ecco * satrec.eta;
            psisq = Math.abs(1.0 - etasq);
            coef = qzms24 * Math.pow(tsi, 4.0);
            coef1 = coef / Math.pow(psisq, 3.5);
            cc2 = coef1 * satrec.no * (ao * (1.0 + 1.5 * etasq + eeta *
                    (4.0 + etasq)) + 0.375 * j2 * tsi / psisq * satrec.con41 *
                    (8.0 + 3.0 * etasq * (8.0 + etasq)));
            satrec.cc1 = satrec.bstar * cc2;
            cc3 = 0.0;
            if(satrec.ecco > 1.0e-4)
            {
                cc3 = -2.0 * coef * tsi * j3oj2 * satrec.no * sinio / satrec.ecco;
            }
            satrec.x1mth2 = 1.0 - cosio2;
            satrec.cc4 = 2.0 * satrec.no * coef1 * ao * omeosq *
                    (satrec.eta * (2.0 + 0.5 * etasq) + satrec.ecco *
                    (0.5 + 2.0 * etasq) - j2 * tsi / (ao * psisq) *
                    (-3.0 * satrec.con41 * (1.0 - 2.0 * eeta + etasq *
                    (1.5 - 0.5 * eeta)) + 0.75 * satrec.x1mth2 *
                    (2.0 * etasq - eeta * (1.0 + etasq)) * Math.cos(2.0 * satrec.argpo)));
            satrec.cc5 = 2.0 * coef1 * ao * omeosq * (1.0 + 2.75 *
                    (etasq + eeta) + eeta * etasq);
            cosio4 = cosio2 * cosio2;
            temp1 = 1.5 * j2 * pinvsq * satrec.no;
            temp2 = 0.5 * temp1 * j2 * pinvsq;
            temp3 = -0.46875 * j4 * pinvsq * pinvsq * satrec.no;
            satrec.mdot = satrec.no + 0.5 * temp1 * rteosq * satrec.con41 + 0.0625 *
                    temp2 * rteosq * (13.0 - 78.0 * cosio2 + 137.0 * cosio4);
            satrec.argpdot = -0.5 * temp1 * con42 + 0.0625 * temp2 *
                    (7.0 - 114.0 * cosio2 + 395.0 * cosio4) +
                    temp3 * (3.0 - 36.0 * cosio2 + 49.0 * cosio4);
            xhdot1 = -temp1 * cosio;
            satrec.nodedot = xhdot1 + (0.5 * temp2 * (4.0 - 19.0 * cosio2) +
                    2.0 * temp3 * (3.0 - 7.0 * cosio2)) * cosio;
            xpidot = satrec.argpdot + satrec.nodedot;
            satrec.omgcof = satrec.bstar * cc3 * Math.cos(satrec.argpo);
            satrec.xmcof = 0.0;
            if(satrec.ecco > 1.0e-4)
            {
                satrec.xmcof = -x2o3 * coef * satrec.bstar / eeta;
            }
            satrec.nodecf = 3.5 * omeosq * xhdot1 * satrec.cc1;
            satrec.t2cof = 1.5 * satrec.cc1;
            // sgp4fix for divide by zero with xinco = 180 deg
            if(Math.abs(cosio + 1.0) > 1.5e-12)
            {
                satrec.xlcof = -0.25 * j3oj2 * sinio * (3.0 + 5.0 * cosio) / (1.0 + cosio);
            }
            else
            {
                satrec.xlcof = -0.25 * j3oj2 * sinio * (3.0 + 5.0 * cosio) / temp4;
            }
            satrec.aycof = -0.5 * j3oj2 * sinio;
            satrec.delmo = Math.pow((1.0 + satrec.eta * Math.cos(satrec.mo)), 3);
            satrec.sinmao = Math.sin(satrec.mo);
            satrec.x7thm1 = 7.0 * cosio2 - 1.0;

            /* --------------- deep space initialization ------------- */
            if((2 * pi / satrec.no) >= 225.0)
            {
                satrec.method = 'd';
                satrec.isimp = 1;
                tc = 0.0;
                inclm = satrec.inclo;

                double[] ttemp2 = dscom(epoch, satrec.ecco, satrec.argpo, tc, satrec.inclo, satrec.nodeo, satrec.no, satrec);
                // save ouput vars
                snodm = ttemp2[0];
                cnodm = ttemp2[1];
                sinim = ttemp2[2];
                cosim = ttemp2[3];
                sinomm = ttemp2[4];
                cosomm = ttemp2[5];
                day = ttemp2[6];
                em = ttemp2[7];
                emsq = ttemp2[8];
                gam = ttemp2[9];
                rtemsq = ttemp2[10];
                s1 = ttemp2[11];
                s2 = ttemp2[12];
                s3 = ttemp2[13];
                s4 = ttemp2[14];
                s5 = ttemp2[15];
                s6 = ttemp2[16];
                s7 = ttemp2[17];
                ss1 = ttemp2[18];
                ss2 = ttemp2[19];
                ss3 = ttemp2[20];
                ss4 = ttemp2[21];
                ss5 = ttemp2[22];
                ss6 = ttemp2[23];
                ss7 = ttemp2[24];
                sz1 = ttemp2[25];
                sz2 = ttemp2[26];
                sz3 = ttemp2[27];
                sz11 = ttemp2[28];
                sz12 = ttemp2[29];
                sz13 = ttemp2[30];
                sz21 = ttemp2[31];
                sz22 = ttemp2[32];
                sz23 = ttemp2[33];
                sz31 = ttemp2[34];
                sz32 = ttemp2[35];
                sz33 = ttemp2[36];
                nm = ttemp2[37];
                z1 = ttemp2[38];
                z2 = ttemp2[39];
                z3 = ttemp2[40];
                z11 = ttemp2[41];
                z12 = ttemp2[42];
                z13 = ttemp2[43];
                z21 = ttemp2[44];
                z22 = ttemp2[45];
                z23 = ttemp2[46];
                z31 = ttemp2[47];
                z32 = ttemp2[48];
                z33 = ttemp2[49];

                //dpper(satrec);
                ttemp2 = dpper(
                        satrec.e3, satrec.ee2, satrec.peo, satrec.pgho,
                        satrec.pho, satrec.pinco, satrec.plo, satrec.se2,
                        satrec.se3, satrec.sgh2, satrec.sgh3, satrec.sgh4,
                        satrec.sh2, satrec.sh3, satrec.si2, satrec.si3,
                        satrec.sl2, satrec.sl3, satrec.sl4, satrec.t,
                        satrec.xgh2, satrec.xgh3, satrec.xgh4, satrec.xh2,
                        satrec.xh3, satrec.xi2, satrec.xi3, satrec.xl2,
                        satrec.xl3, satrec.xl4, satrec.zmol, satrec.zmos, inclm, satrec.init,
                        satrec.ecco, satrec.inclo, satrec.nodeo, satrec.argpo, satrec.mo,
                        satrec.operationmode);
                satrec.ecco = ttemp2[0];
                satrec.inclo = ttemp2[1];
                satrec.nodeo = ttemp2[2];
                satrec.argpo = ttemp2[3];
                satrec.mo = ttemp2[4];

                argpm = 0.0;
                nodem = 0.0;
                mm = 0.0;

                double[] ttemp3 = dsinit(whichconst,
                        cosim, emsq, satrec.argpo, s1, s2, s3, s4, s5, sinim, ss1, ss2, ss3, ss4,
                        ss5, sz1, sz3, sz11, sz13, sz21, sz23, sz31, sz33, satrec.t, tc,
                        satrec.gsto, satrec.mo, satrec.mdot, satrec.no, satrec.nodeo,
                        satrec.nodedot, xpidot, z1, z3, z11, z13, z21, z23, z31, z33,
                        satrec.ecco, eccsq,
                        satrec,
                        em, argpm, inclm, mm, nm, nodem);
                em = ttemp3[0];
                argpm = ttemp3[1];
                inclm = ttemp3[2];
                mm = ttemp3[3];
                nm = ttemp3[4];
                nodem = ttemp3[5];
                dndt = ttemp3[6];
            }

            /* ----------- set variables if not deep space ----------- */
            if(satrec.isimp != 1)
            {
                cc1sq = satrec.cc1 * satrec.cc1;
                satrec.d2 = 4.0 * ao * tsi * cc1sq;
                temp = satrec.d2 * tsi * satrec.cc1 / 3.0;
                satrec.d3 = (17.0 * ao + sfour) * temp;
                satrec.d4 = 0.5 * temp * ao * tsi * (221.0 * ao + 31.0 * sfour) *
                        satrec.cc1;
                satrec.t3cof = satrec.d2 + 2.0 * cc1sq;
                satrec.t4cof = 0.25 * (3.0 * satrec.d3 + satrec.cc1 *
                        (12.0 * satrec.d2 + 10.0 * cc1sq));
                satrec.t5cof = 0.2 * (3.0 * satrec.d4 +
                        12.0 * satrec.cc1 * satrec.d3 +
                        6.0 * satrec.d2 * satrec.d2 +
                        15.0 * cc1sq * (2.0 * satrec.d2 + cc1sq));
            }
        } // if omeosq = 0 ...

        /* finally propogate to zero epoch to initialize all others. */
        // sgp4fix take out check to let satellites process until they are actually below earth surface
//       if(satrec.error == 0)
        boolean sgp4Error = sgp4(satrec, 0.0, r, v);  // SEG removed gravity constant passing

        satrec.init = 'n';

//#include "debug6.cpp"
        //sgp4fix return boolean. satrec.error contains any error codes
        return sgp4Error;
    }  // end sgp4init



    /**
     * Similar to sgp4(..) but time parameter is the Julian Date to the propagated to.
     * This method was not orgiinally in the CSSI C++ version.
     * @param satrec satellite SGP4 data object
     * @param jd Julian Date
     * @param r position vector [km] return array (needs to be of size 3)
     * @param v velocity [km/sec] return array (needs to be of size 3)
     * @return
     */
    public static boolean sgp4Prop2JD(SGP4SatData satrec, double jd, double[] r, double[] v)
    {
        double tminSinceEpoch = (jd - satrec.jdsatepoch)*24.0*60.0;
        return sgp4(satrec, tminSinceEpoch,r, v);
    }

    /*-----------------------------------------------------------------------------
     *
     *                             procedure sgp4
     *
     *  this procedure is the sgp4 prediction model from space command. this is an
     *    updated and combined version of sgp4 and sdp4, which were originally
     *    published separately in spacetrack report #3. this version follows the
     *    methodology from the aiaa paper (2006) describing the history and
     *    development of the code.
     *
     *  author        : david vallado                  719-573-2600   28 jun 2005
     *
     *  inputs        :
     *    satrec	 - initialised structure from sgp4init() call.
     *    tsince	 - time eince epoch (minutes)
     *
     *  outputs       :
     *    r           - position vector                     km
     *    v           - velocity                            km/sec
     *  return code - non-zero on error.
     *                   1 - mean elements, ecc >= 1.0 or ecc < -0.001 or a < 0.95 er
     *                   2 - mean motion less than 0.0
     *                   3 - pert elements, ecc < 0.0  or  ecc > 1.0
     *                   4 - semi-latus rectum < 0.0
     *                   5 - epoch elements are sub-orbital
     *                   6 - satellite has decayed
     *
     *  locals        :
     *    am          -
     *    axnl, aynl        -
     *    betal       -
     *    cosim   , sinim   , cosomm  , sinomm  , cnod    , snod    , cos2u   ,
     *    sin2u   , coseo1  , sineo1  , cosi    , sini    , cosip   , sinip   ,
     *    cosisq  , cossu   , sinsu   , cosu    , sinu
     *    delm        -
     *    delomg      -
     *    dndt        -
     *    eccm        -
     *    emsq        -
     *    ecose       -
     *    el2         -
     *    eo1         -
     *    eccp        -
     *    esine       -
     *    argpm       -
     *    argpp       -
     *    omgadf      -
     *    pl          -
     *    r           -
     *    rtemsq      -
     *    rdotl       -
     *    rl          -
     *    rvdot       -
     *    rvdotl      -
     *    su          -
     *    t2  , t3   , t4    , tc
     *    tem5, temp , temp1 , temp2  , tempa  , tempe  , templ
     *    u   , ux   , uy    , uz     , vx     , vy     , vz
     *    inclm       - inclination
     *    mm          - mean anomaly
     *    nm          - mean motion
     *    nodem       - right asc of ascending node
     *    xinc        -
     *    xincp       -
     *    xl          -
     *    xlm         -
     *    mp          -
     *    xmdf        -
     *    xmx         -
     *    xmy         -
     *    nodedf      -
     *    xnode       -
     *    nodep       -
     *    np          -
     *
     *  coupling      :
     *    getgravconst-
     *    dpper
     *    dpspace
     *
     *  references    :
     *    hoots, roehrich, norad spacetrack report #3 1980
     *    hoots, norad spacetrack report #6 1986
     *    hoots, schumacher and glover 2004
     *    vallado, crawford, hujsak, kelso  2006
     *  ----------------------------------------------------------------------------
     *
     * @param satrec satelite sgp4 data object
     * @param tsince time eince epoch (minutes)
     * @param r position vector [km] return array (needs to be of size 3)
     * @param v velocity [km/sec] return array (needs to be of size 3)
     * @return true if there were no errors, see satrec.error for error code
     */
    public static boolean sgp4(
            //Gravconsttype whichconst, // SEG removed as it should already be saved into satrec
            SGP4SatData satrec, double tsince,
            double[] r, double[] v)
    {
        double am, axnl, aynl, betal, cosim, cnod,
                cos2u, coseo1 = 0, cosi, cosip, cosisq, cossu, cosu,
                delm, delomg, em, emsq, ecose, el2, eo1,
                ep, esine, argpm, argpp, argpdf, pl, mrt = 0.0,
                mvt, rdotl, rl, rvdot, rvdotl, sinim,
                sin2u, sineo1 = 0, sini, sinip, sinsu, sinu,
                snod, su, t2, t3, t4, tem5, temp,
                temp1, temp2, tempa, tempe, templ, u, ux,
                uy, uz, vx, vy, vz, inclm, mm,
                nm, nodem, xinc, xincp, xl, xlm, mp,
                xmdf, xmx, xmy, nodedf, xnode, nodep, tc, dndt,
                twopi, x2o3, j2, j3, tumin, j4, xke, j3oj2, radiusearthkm,
                mu, vkmpersec;
        int ktr;

        /* ------------------ set mathematical constants --------------- */
        // sgp4fix divisor for divide by zero check on inclination
        // the old check used 1.0 + cos(pi-1.0e-9), but then compared it to
        // 1.5 e-12, so the threshold was changed to 1.5e-12 for consistency
        final double temp4 = 1.5e-12;
        twopi = 2.0 * pi;
        x2o3 = 2.0 / 3.0;
        // sgp4fix identify constants and allow alternate values
        double[] temp5 = getgravconst(satrec.gravconsttype);//, tumin, mu, radiusearthkm, xke, j2, j3, j4, j3oj2 );
        tumin = temp5[0];
        mu = temp5[1];
        radiusearthkm = temp5[2];
        xke = temp5[3];
        j2 = temp5[4];
        j3 = temp5[5];
        j4 = temp5[6];
        j3oj2 = temp5[7];

        vkmpersec = radiusearthkm * xke / 60.0;

        /* --------------------- clear sgp4 error flag ----------------- */
        satrec.t = tsince;
        satrec.error = 0;

        /* ------- update for secular gravity and atmospheric drag ----- */
        xmdf = satrec.mo + satrec.mdot * satrec.t;
        argpdf = satrec.argpo + satrec.argpdot * satrec.t;
        nodedf = satrec.nodeo + satrec.nodedot * satrec.t;
        argpm = argpdf;
        mm = xmdf;
        t2 = satrec.t * satrec.t;
        nodem = nodedf + satrec.nodecf * t2;
        tempa = 1.0 - satrec.cc1 * satrec.t;
        tempe = satrec.bstar * satrec.cc4 * satrec.t;
        templ = satrec.t2cof * t2;

        if(satrec.isimp != 1)
        {
            delomg = satrec.omgcof * satrec.t;
            delm = satrec.xmcof *
                    (Math.pow((1.0 + satrec.eta * Math.cos(xmdf)), 3) -
                    satrec.delmo);
            temp = delomg + delm;
            mm = xmdf + temp;
            argpm = argpdf - temp;
            t3 = t2 * satrec.t;
            t4 = t3 * satrec.t;
            tempa = tempa - satrec.d2 * t2 - satrec.d3 * t3 -
                    satrec.d4 * t4;
            tempe = tempe + satrec.bstar * satrec.cc5 * (Math.sin(mm) -
                    satrec.sinmao);
            templ = templ + satrec.t3cof * t3 + t4 * (satrec.t4cof +
                    satrec.t * satrec.t5cof);
        }

        nm = satrec.no;
        em = satrec.ecco;
        inclm = satrec.inclo;
        if(satrec.method == 'd')
        {
            tc = satrec.t;
            double[] ttemp = dspace(
                    satrec.irez,
                    satrec.d2201, satrec.d2211, satrec.d3210,
                    satrec.d3222, satrec.d4410, satrec.d4422,
                    satrec.d5220, satrec.d5232, satrec.d5421,
                    satrec.d5433, satrec.dedt, satrec.del1,
                    satrec.del2, satrec.del3, satrec.didt,
                    satrec.dmdt, satrec.dnodt, satrec.domdt,
                    satrec.argpo, satrec.argpdot, satrec.t, tc,
                    satrec.gsto, satrec.xfact, satrec.xlamo,
                    satrec.no,
                    em, argpm, inclm, mm, nodem,
                    satrec,
                    nm);
            // copy variables back
            em = ttemp[0];
            argpm = ttemp[1];
            inclm = ttemp[2];
            mm = ttemp[3];
            nodem = ttemp[4];
            dndt = ttemp[5];
            nm = ttemp[6];

        } // if method = d

        if(nm <= 0.0)
        {
//         printf("# error nm %f\n", nm);
            satrec.error = 2;
            // sgp4fix add return
            return false;
        }
        am = Math.pow((xke / nm), x2o3) * tempa * tempa;
        nm = xke / Math.pow(am, 1.5);
        em = em - tempe;

        // fix tolerance for error recognition
        // sgp4fix am is fixed from the previous nm check
        if((em >= 1.0) || (em < -0.001)/* || (am < 0.95)*/)
        {
//         printf("# error em %f\n", em);
            satrec.error = 1;
            // sgp4fix to return if there is an error in eccentricity
            return false;
        }
        // sgp4fix fix tolerance to avoid a divide by zero
        if(em < 1.0e-6)
        {
            em = 1.0e-6;
        }
        mm = mm + satrec.no * templ;
        xlm = mm + argpm + nodem;
        emsq = em * em;
        temp = 1.0 - emsq;

        nodem = (nodem % twopi);
        argpm = (argpm % twopi);
        xlm = (xlm % twopi);
        mm = ((xlm - argpm - nodem) % twopi);

        /* ----------------- compute extra mean quantities ------------- */
        sinim = Math.sin(inclm);
        cosim = Math.cos(inclm);

        /* -------------------- add lunar-solar periodics -------------- */
        ep = em;
        xincp = inclm;
        argpp = argpm;
        nodep = nodem;
        mp = mm;
        sinip = sinim;
        cosip = cosim;
        if(satrec.method == 'd')
        {
            //dpper(satrec);
            double[] ttemp = dpper(
                    satrec.e3, satrec.ee2, satrec.peo,
                    satrec.pgho, satrec.pho, satrec.pinco,
                    satrec.plo, satrec.se2, satrec.se3,
                    satrec.sgh2, satrec.sgh3, satrec.sgh4,
                    satrec.sh2, satrec.sh3, satrec.si2,
                    satrec.si3, satrec.sl2, satrec.sl3,
                    satrec.sl4, satrec.t, satrec.xgh2,
                    satrec.xgh3, satrec.xgh4, satrec.xh2,
                    satrec.xh3, satrec.xi2, satrec.xi3,
                    satrec.xl2, satrec.xl3, satrec.xl4,
                    satrec.zmol, satrec.zmos, satrec.inclo,
                    'n', ep, xincp, nodep, argpp, mp, satrec.operationmode);
            ep = ttemp[0];
            xincp = ttemp[1];
            nodep = ttemp[2];
            argpp = ttemp[3];
            mp = ttemp[4];

            if(xincp < 0.0)
            {
                xincp = -xincp;
                nodep = nodep + pi;
                argpp = argpp - pi;
            }
            if((ep < 0.0) || (ep > 1.0))
            {
//            printf("# error ep %f\n", ep);
                satrec.error = 3;
                // sgp4fix add return
                return false;
            }
        } // if method = d

        /* -------------------- long period periodics ------------------ */
        if(satrec.method == 'd')
        {
            sinip = Math.sin(xincp);
            cosip = Math.cos(xincp);
            satrec.aycof = -0.5 * j3oj2 * sinip;
            // sgp4fix for divide by zero for xincp = 180 deg
            if(Math.abs(cosip + 1.0) > 1.5e-12)
            {
                satrec.xlcof = -0.25 * j3oj2 * sinip * (3.0 + 5.0 * cosip) / (1.0 + cosip);
            }
            else
            {
                satrec.xlcof = -0.25 * j3oj2 * sinip * (3.0 + 5.0 * cosip) / temp4;
            }
        }
        axnl = ep * Math.cos(argpp);
        temp = 1.0 / (am * (1.0 - ep * ep));
        aynl = ep * Math.sin(argpp) + temp * satrec.aycof;
        xl = mp + argpp + nodep + temp * satrec.xlcof * axnl;

        /* --------------------- solve kepler's equation --------------- */
        u = ((xl - nodep) % twopi);
        eo1 = u;
        tem5 = 9999.9;
        ktr = 1;
        //   sgp4fix for kepler iteration
        //   the following iteration needs better limits on corrections
        while((Math.abs(tem5) >= 1.0e-12) && (ktr <= 10))
        {
            sineo1 = Math.sin(eo1);
            coseo1 = Math.cos(eo1);
            tem5 = 1.0 - coseo1 * axnl - sineo1 * aynl;
            tem5 = (u - aynl * coseo1 + axnl * sineo1 - eo1) / tem5;
            if(Math.abs(tem5) >= 0.95)
            {
                tem5 = tem5 > 0.0 ? 0.95 : -0.95;
            }
            eo1 = eo1 + tem5;
            ktr = ktr + 1;
        }

        /* ------------- short period preliminary quantities ----------- */
        ecose = axnl * coseo1 + aynl * sineo1;
        esine = axnl * sineo1 - aynl * coseo1;
        el2 = axnl * axnl + aynl * aynl;
        pl = am * (1.0 - el2);
        if(pl < 0.0)
        {
//         printf("# error pl %f\n", pl);
            satrec.error = 4;
            // sgp4fix add return
            return false;
        }
        else
        {
            rl = am * (1.0 - ecose);
            rdotl = Math.sqrt(am) * esine / rl;
            rvdotl = Math.sqrt(pl) / rl;
            betal = Math.sqrt(1.0 - el2);
            temp = esine / (1.0 + betal);
            sinu = am / rl * (sineo1 - aynl - axnl * temp);
            cosu = am / rl * (coseo1 - axnl + aynl * temp);
            su = Math.atan2(sinu, cosu);
            sin2u = (cosu + cosu) * sinu;
            cos2u = 1.0 - 2.0 * sinu * sinu;
            temp = 1.0 / pl;
            temp1 = 0.5 * j2 * temp;
            temp2 = temp1 * temp;

            /* -------------- update for short period periodics ------------ */
            if(satrec.method == 'd')
            {
                cosisq = cosip * cosip;
                satrec.con41 = 3.0 * cosisq - 1.0;
                satrec.x1mth2 = 1.0 - cosisq;
                satrec.x7thm1 = 7.0 * cosisq - 1.0;
            }
            mrt = rl * (1.0 - 1.5 * temp2 * betal * satrec.con41) +
                    0.5 * temp1 * satrec.x1mth2 * cos2u;
            su = su - 0.25 * temp2 * satrec.x7thm1 * sin2u;
            xnode = nodep + 1.5 * temp2 * cosip * sin2u;
            xinc = xincp + 1.5 * temp2 * cosip * sinip * cos2u;
            mvt = rdotl - nm * temp1 * satrec.x1mth2 * sin2u / xke;
            rvdot = rvdotl + nm * temp1 * (satrec.x1mth2 * cos2u +
                    1.5 * satrec.con41) / xke;

            /* --------------------- orientation vectors ------------------- */
            sinsu = Math.sin(su);
            cossu = Math.cos(su);
            snod = Math.sin(xnode);
            cnod = Math.cos(xnode);
            sini = Math.sin(xinc);
            cosi = Math.cos(xinc);
            xmx = -snod * cosi;
            xmy = cnod * cosi;
            ux = xmx * sinsu + cnod * cossu;
            uy = xmy * sinsu + snod * cossu;
            uz = sini * sinsu;
            vx = xmx * cossu - cnod * sinsu;
            vy = xmy * cossu - snod * sinsu;
            vz = sini * cossu;

            /* --------- position and velocity (in km and km/sec) ---------- */
            r[0] = (mrt * ux) * radiusearthkm;
            r[1] = (mrt * uy) * radiusearthkm;
            r[2] = (mrt * uz) * radiusearthkm;
            v[0] = (mvt * ux + rvdot * vx) * vkmpersec;
            v[1] = (mvt * uy + rvdot * vy) * vkmpersec;
            v[2] = (mvt * uz + rvdot * vz) * vkmpersec;
        }  // if pl > 0

        // sgp4fix for decaying satellites
        if(mrt < 1.0)
        {
//         printf("# decay condition %11.6f \n",mrt);
            satrec.error = 6;
            return false;
        }

//#include "debug7.cpp"
        return true;
    }  // end sgp4

    /** -----------------------------------------------------------------------------
     *
     *                           function gstime
     *
     *  this function finds the greenwich sidereal time.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    jdut1       - julian date in ut1             days from 4713 bc
     *
     *  outputs       :
     *    gstime      - greenwich sidereal time        0 to 2pi rad
     *
     *  locals        :
     *    temp        - temporary variable for doubles   rad
     *    tut1        - julian centuries from the
     *                  jan 1, 2000 12 h epoch (ut1)
     *
     *  coupling      :
     *    none
     *
     *  references    :
     *    vallado       2004, 191, eq 3-45
     * ---------------------------------------------------------------------------
     * @param jdut1
     * @return
     */
    public static double gstime(double jdut1)
    {
        final double twopi = 2.0 * pi;
        final double deg2rad = pi / 180.0;
        double temp, tut1;

        tut1 = (jdut1 - 2451545.0) / 36525.0;
        temp = -6.2e-6 * tut1 * tut1 * tut1 + 0.093104 * tut1 * tut1 +
                (876600.0 * 3600 + 8640184.812866) * tut1 + 67310.54841;  // sec
        temp = ((temp * deg2rad / 240.0) % twopi); //360/86400 = 1/240, to deg, to rad

        // ------------------------ check quadrants ---------------------
        if(temp < 0.0)
        {
            temp += twopi;
        }

        return temp;
    }  // end gstime

    /* -----------------------------------------------------------------------------
     *
     *                           function getgravconst
     *
     *  this function gets constants for the propagator. note that mu is identified to
     *    facilitiate comparisons with newer models. the common useage is wgs72.
     *
     *  author        : david vallado                  719-573-2600   21 jul 2006
     *
     *  inputs        :
     *    whichconst  - which set of constants to use  wgs72old, wgs72, wgs84
     *
     *  outputs       :
     *    tumin       - minutes in one time unit
     *    mu          - earth gravitational parameter
     *    radiusearthkm - radius of the earth in km
     *    xke         - reciprocal of tumin
     *    j2, j3, j4  - un-normalized zonal harmonic values
     *    j3oj2       - j3 divided by j2
     *
     *  locals        :
     *
     *  coupling      :
     *    none
     *
     *  references    :
     *    norad spacetrack report #3
     *    vallado, crawford, hujsak, kelso  2006
    ---------------------------------------------------------------------------
     * @param whichconst
     * @return [tumin, mu, radiusearthkm, xke, j2, j3, j4, j3oj2]
     */
    public static double[] getgravconst(
            Gravconsttype whichconst //      double& tumin,
            //      double& mu,
            //      double& radiusearthkm,
            //      double& xke,
            //      double& j2,
            //      double& j3,
            //      double& j4,
            //      double& j3oj2
            )
    {

        // return values
        double tumin, mu, radiusearthkm, xke, j2, j3, j4, j3oj2;

        switch(whichconst)
        {
            // -- wgs-72 low precision str#3 constants --
            case wgs72old:
                mu = 398600.79964;        // in km3 / s2
                radiusearthkm = 6378.135;     // km
                xke = 0.0743669161;
                tumin = 1.0 / xke;
                j2 = 0.001082616;
                j3 = -0.00000253881;
                j4 = -0.00000165597;
                j3oj2 = j3 / j2;
                break;
            // ------------ wgs-72 constants ------------
            case wgs72:
                mu = 398600.8;            // in km3 / s2
                radiusearthkm = 6378.135;     // km
                xke = 60.0 / Math.sqrt(radiusearthkm * radiusearthkm * radiusearthkm / mu);
                tumin = 1.0 / xke;
                j2 = 0.001082616;
                j3 = -0.00000253881;
                j4 = -0.00000165597;
                j3oj2 = j3 / j2;
                break;
            case wgs84:
                // ------------ wgs-84 constants ------------
                mu = 398600.5;            // in km3 / s2
                radiusearthkm = 6378.137;     // km
                xke = 60.0 / Math.sqrt(radiusearthkm * radiusearthkm * radiusearthkm / mu);
                tumin = 1.0 / xke;
                j2 = 0.00108262998905;
                j3 = -0.00000253215306;
                j4 = -0.00000161098761;
                j3oj2 = j3 / j2;
                break;
            default:
                System.out.println("unknown gravity option:" + whichconst + ", using wgs84");
                // MODIFIED - SHAWN GANO -- Orginal implementation just returned no values!
                // ------------ wgs-84 constants ------------
                mu = 398600.5;            // in km3 / s2
                radiusearthkm = 6378.137;     // km
                xke = 60.0 / Math.sqrt(radiusearthkm * radiusearthkm * radiusearthkm / mu);
                tumin = 1.0 / xke;
                j2 = 0.00108262998905;
                j3 = -0.00000253215306;
                j4 = -0.00000161098761;
                j3oj2 = j3 / j2;
                break;
        }

        return new double[]
                {
                    tumin, mu, radiusearthkm, xke, j2, j3, j4, j3oj2
                };

    }   // end getgravconst
} // SGP4unit
