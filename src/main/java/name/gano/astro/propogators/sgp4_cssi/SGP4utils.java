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
/**     ----------------------------------------------------------------
 * Contains functions to read TLE data files and initalize the SGP4 propogator
 * as well as other routines from sgp4ext.cpp
 * 
 * <p>
 * sgp4ext.cpp header information:
 * <p>
 *
 *                               sgp4ext.cpp
 *
 *    this file contains extra routines needed for the main test program for sgp4.
 *    these routines are derived from the astro libraries.
 *
 *                            companion code for
 *               fundamentals of astrodynamics and applications
 *                                    2007
 *                              by david vallado
 *
 *       (w) 719-573-2600, email dvallado@agi.com
 *
 *    current :
 *               7 may 08  david vallado
 *                           fix sgn
 *    changes :
 *               2 apr 07  david vallado
 *                           fix jday floor and str lengths
 *                           updates for constants
 *              14 aug 06  david vallado
 *                           original baseline
 *       ----------------------------------------------------------------      */
package name.gano.astro.propogators.sgp4_cssi;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;

/**
 * 19 June 2009
 * @author Shawn E. Gano, shawn@gano.name
 */
public class SGP4utils
{
    public static double pi = SGP4unit.pi;
    public static char OPSMODE_AFSPC = 'a';
    public static char OPSMODE_IMPROVED = 'i';

    /**
     * Reads the data from the TLE and initializes the SGP4 propogator variables and stores them in the SGP4unit.Gravconsttype object
     * DOES NOT PERFORM ANY INTERNAL CHECK BEYOND BASICS OF THE TLE DATA use other methods to do that if desired.
     *
     * @param satName
     * @param line1  TLE line 1
     * @param line2  TLE line 2
     * @param opsmode 
     * @param whichconst which constants to use in propogation
     * @param satrec  object to store the SGP4 data
     * @return if the sgp4 propogator was initialized properly
     */
    public static boolean readTLEandIniSGP4(String satName, String line1, String line2, char opsmode, SGP4unit.Gravconsttype whichconst, SGP4SatData satrec)
    {
        final double deg2rad = pi / 180.0;         //   0.0174532925199433
        final double xpdotp = 1440.0 / (2.0 * pi);  // 229.1831180523293

        double sec, tumin;//, mu, radiusearthkm, xke, j2, j3, j4, j3oj2;
//        double startsec, stopsec, startdayofyr, stopdayofyr, jdstart, jdstop;
//        int startyear, stopyear, startmon, stopmon, startday, stopday,
//                starthr, stophr, startmin, stopmin;
//        int cardnumb, j; // numb,
        //long revnum = 0, elnum = 0;
        //char classification, intldesg[11], tmpstr[80];
        int year = 0;
        int mon, day, hr, minute;//, nexp, ibexp;

        double[] temp = SGP4unit.getgravconst(whichconst);
        tumin = temp[0];
//        mu = temp[1];
//        radiusearthkm = temp[2];
//        xke = temp[3];
//        j2 = temp[4];
//        j3 = temp[5];
//        j4 = temp[6];
//        j3oj2 = temp[7];

        satrec.error = 0;

        satrec.name = satName;

        // SEG -- save gravity  - moved to SGP4unit.sgp4init fo consistancy
        //satrec.gravconsttype = whichconst;

        // get variables from the two lines
        satrec.line1 = line1;
        try
        {
            readLine1(line1, satrec);
        }
        catch(Exception e)
        {
            System.out.println("Error Reading TLE line 1: " + e.toString());
            satrec.tleDataOk = false;
            satrec.error = 7;
            return false;
        }

        satrec.line2 = line2;
        try
        {
            readLine2(line2, satrec);
        }
        catch(Exception e)
        {
            System.out.println("Error Reading TLE line 2: " + e.toString());
            satrec.tleDataOk = false;
            satrec.error = 7;
            return false;
        }


        // ---- find no, ndot, nddot ----
        satrec.no = satrec.no / xpdotp; //* rad/min
        satrec.nddot = satrec.nddot * Math.pow(10.0, satrec.nexp);
        satrec.bstar = satrec.bstar * Math.pow(10.0, satrec.ibexp);

        // ---- convert to sgp4 units ----
        satrec.a = Math.pow(satrec.no * tumin, (-2.0 / 3.0));
        satrec.ndot = satrec.ndot / (xpdotp * 1440.0);  //* ? * minperday
        satrec.nddot = satrec.nddot / (xpdotp * 1440.0 * 1440);

        // ---- find standard orbital elements ----
        satrec.inclo = satrec.inclo * deg2rad;
        satrec.nodeo = satrec.nodeo * deg2rad;
        satrec.argpo = satrec.argpo * deg2rad;
        satrec.mo = satrec.mo * deg2rad;

        satrec.alta = satrec.a * (1.0 + satrec.ecco) - 1.0;
        satrec.altp = satrec.a * (1.0 - satrec.ecco) - 1.0;

        // ----------------------------------------------------------------
        // find sgp4epoch time of element set
        // remember that sgp4 uses units of days from 0 jan 1950 (sgp4epoch)
        // and minutes from the epoch (time)
        // ----------------------------------------------------------------

        // ---------------- temp fix for years from 1957-2056 -------------------
        // --------- correct fix will occur when year is 4-digit in tle ---------
        if(satrec.epochyr < 57)
        {
            year = satrec.epochyr + 2000;
        }
        else
        {
            year = satrec.epochyr + 1900;
        }

        // computes the m/d/hr/min/sec from year and epoch days
        MDHMS mdhms = days2mdhms(year, satrec.epochdays);
        mon = mdhms.mon;
        day = mdhms.day;
        hr = mdhms.hr;
        minute = mdhms.minute;
        sec = mdhms.sec;
        // computes the jd from  m/d/...
        satrec.jdsatepoch = jday(year, mon, day, hr, minute, sec);

        // ---------------- initialize the orbit at sgp4epoch -------------------
        boolean result = SGP4unit.sgp4init(whichconst, opsmode, satrec.satnum,
                satrec.jdsatepoch - 2433281.5, satrec.bstar,
                satrec.ecco, satrec.argpo, satrec.inclo, satrec.mo, satrec.no,
                satrec.nodeo, satrec);

        return result;

    } // readTLEandIniSGP4

    private static boolean readLine1(String line1, SGP4SatData satrec) throws Exception
    {
        String tleLine1 = line1; // first line
        if(!tleLine1.startsWith("1 "))
        {
            throw new Exception("TLE line 1 not valid first line");
        }

        // satnum
        satrec.satnum = (int)readFloatFromString(tleLine1.substring(2, 7));
        // classification
        satrec.classification = tleLine1.substring(7, 8); // 1 char
        // intln designator
        satrec.intldesg = tleLine1.substring(9, 17); // should be within 8

        // epochyr
        satrec.epochyr = (int)readFloatFromString(tleLine1.substring(18, 20));

        // epoch days
        satrec.epochdays = readFloatFromString(tleLine1.substring(20, 32));

        // ndot
        satrec.ndot = readFloatFromString(tleLine1.substring(33, 43));

        // nddot
        //nexp
        if((tleLine1.substring(44, 52)).equals("        "))
        {
            satrec.nddot = 0;
            satrec.nexp = 0;
        }
        else
        {
            satrec.nddot = readFloatFromString(tleLine1.substring(44, 50)) / 1.0E5;
            //nexp
            satrec.nexp = (int)readFloatFromString(tleLine1.substring(50, 52));
        }
        //bstar
        satrec.bstar = readFloatFromString(tleLine1.substring(53, 59)) / 1.0E5;
        //ibex
        satrec.ibexp = (int)readFloatFromString(tleLine1.substring(59, 61));

        // these last things are not essential so just try to read them, and give a warning - but no error
        try
        {
            // num b.
            satrec.numb = (int)readFloatFromString(tleLine1.substring(62, 63));

            //  elnum
            satrec.elnum = (long)readFloatFromString(tleLine1.substring(64, 68));
        }
        catch(Exception e)
        {
            System.out.println("Warning: Error Reading numb or elnum from TLE line 1 sat#:" + satrec.satnum);
        }

        // checksum
        //int checksum1 = (int) readFloatFromString(tleLine1.substring(68));

        // if no errors yet everything went ok
        return true;
    } // readLine1

    private static boolean readLine2(String line2, SGP4SatData satrec) throws Exception
    {
        /* Read the second line of elements. */

        //theLine = aFile.readLine();
        String tleLine2 = line2; // second line
        if(!tleLine2.startsWith("2 "))
        {
            throw new Exception("TLE line 2 not valid second line");
        }

        // satnum
        int satnum = (int)readFloatFromString(tleLine2.substring(2, 7));
        if(satnum != satrec.satnum)
        {
            System.out.println("Warning TLE line 2 Sat Num doesn't match line1 for sat: " + satrec.name);
        }

        // inclination
        satrec.inclo = readFloatFromString(tleLine2.substring(8, 17));

        // nodeo
        satrec.nodeo = readFloatFromString(tleLine2.substring(17, 26));

        //satrec.ecco
        satrec.ecco = readFloatFromString(tleLine2.substring(26, 34)) / 1.0E7;

        // satrec.argpo
        satrec.argpo = readFloatFromString(tleLine2.substring(34, 43));

        // satrec.mo
        satrec.mo = readFloatFromString(tleLine2.substring(43, 52));

        // no
        satrec.no = readFloatFromString(tleLine2.substring(52, 63));

        // try to read other data
        try
        {
            // revnum
            satrec.revnum = (long)readFloatFromString(tleLine2.substring(63, 68));
        }
        catch(Exception e)
        {
            System.out.println("Warning: Error Reading revnum from TLE line 2 sat#:" + satrec.satnum + "\n" + e.toString());
            satrec.revnum = -1;
        }

//        // checksum
//        int checksum2 = (int) readFloatFromString(tleLine2.substring(68));

        return true;
    } // readLine1

    /**
     * Read float data from a string
     * @param inStr
     * @return
     * @throws Exception 
     */
    protected static double readFloatFromString(String inStr) throws Exception
    {
        // make sure decimal sparator is '.' so it works in other countries
        // because of this can't use Double.parse
        DecimalFormat dformat = new DecimalFormat("#");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        dformat.setDecimalFormatSymbols(dfs);

        // trim white space and if there is a + at the start
        String trimStr = inStr.trim();
        if(trimStr.startsWith("+"))
        {
            trimStr = trimStr.substring(1);
        }

        // parse until we hit the end or invalid char
        ParsePosition pp = new ParsePosition(0);
        Number num = dformat.parse(trimStr, pp);
        if(null == num)
        {
            throw new Exception("Invalid Float In TLE");
        }

        return num.doubleValue();
    } // readFloatFromString

    /** -----------------------------------------------------------------------------
     *
     *                           procedure jday
     *
     *  this procedure finds the julian date given the year, month, day, and time.
     *    the julian date is defined by each elapsed day since noon, jan 1, 4713 bc.
     *
     *  algorithm     : calculate the answer in one step for efficiency
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    year        - year                           1900 .. 2100
     *    mon         - month                          1 .. 12
     *    day         - day                            1 .. 28,29,30,31
     *    hr          - universal time hour            0 .. 23
     *    min         - universal time min             0 .. 59
     *    sec         - universal time sec             0.0 .. 59.999
     *
     *  outputs       :
     *    jd          - julian date                    days from 4713 bc
     *
     *  locals        :
     *    none.
     *
     *  coupling      :
     *    none.
     *
     *  references    :
     *    vallado       2007, 189, alg 14, ex 3-14
     *
     * ---------------------------------------------------------------------------
     * @param year
     * @param mon
     * @param day
     * @param hr
     * @param minute
     * @param sec
     * @return
     */
    public static double jday(
            int year, int mon, int day, int hr, int minute, double sec//,
            //double& jd
            )
    {
        double jd;
        jd = 367.0 * year -
                Math.floor((7 * (year + Math.floor((mon + 9) / 12.0))) * 0.25) +
                Math.floor(275 * mon / 9.0) +
                day + 1721013.5 +
                ((sec / 60.0 + minute) / 60.0 + hr) / 24.0;  // ut in days
        // - 0.5*sgn(100.0*year + mon - 190002.5) + 0.5;

        return jd;
    }  // end jday

    /* -----------------------------------------------------------------------------
     *
     *                           procedure days2mdhms
     *
     *  this procedure converts the day of the year, days, to the equivalent month
     *    day, hour, minute and second.
     *
     *
     *
     *  algorithm     : set up array for the number of days per month
     *                  find leap year - use 1900 because 2000 is a leap year
     *                  loop through a temp value while the value is < the days
     *                  perform int conversions to the correct day and month
     *                  convert remainder into h m s using type conversions
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    year        - year                           1900 .. 2100
     *    days        - julian day of the year         0.0  .. 366.0
     *
     *  outputs       :
     *    mon         - month                          1 .. 12
     *    day         - day                            1 .. 28,29,30,31
     *    hr          - hour                           0 .. 23
     *    min         - minute                         0 .. 59
     *    sec         - second                         0.0 .. 59.999
     *
     *  locals        :
     *    dayofyr     - day of year
     *    temp        - temporary extended values
     *    inttemp     - temporary int value
     *    i           - index
     *    lmonth[12]  - int array containing the number of days per month
     *
     *  coupling      :
     *    none.
     * --------------------------------------------------------------------------- */
// returns MDHMS object with the mdhms variables
    private static MDHMS days2mdhms(
            int year, double days//,
            //int& mon, int& day, int& hr, int& minute, double& sec
            )
    {
        // return variables
        //int mon, day, hr, minute, sec
        MDHMS mdhms = new MDHMS();

        int i, inttemp, dayofyr;
        double temp;
        int lmonth[] =
        {
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        };

        dayofyr = (int)Math.floor(days);
        /* ----------------- find month and day of month ---------------- */
        if((year % 4) == 0) // doesn't work for dates starting 2100 and beyond
        {
            lmonth[1] = 29;
        }

        i = 1;
        inttemp = 0;
        while((dayofyr > inttemp + lmonth[i - 1]) && (i < 12))
        {
            inttemp = inttemp + lmonth[i - 1];
            i++;
        }
        mdhms.mon = i;
        mdhms.day = dayofyr - inttemp;

        /* ----------------- find hours minutes and seconds ------------- */
        temp = (days - dayofyr) * 24.0;
        mdhms.hr = (int)Math.floor(temp);
        temp = (temp - mdhms.hr) * 60.0;
        mdhms.minute = (int)Math.floor(temp);
        mdhms.sec = (temp - mdhms.minute) * 60.0;

        return mdhms;
    }  // end days2mdhms

    // Month Day Hours Min Sec
    private static class MDHMS
    {
        int mon = 0;
        ;
        int day = 0;
        ;
        int hr = 0;
        ;
        int minute = 0;
        ;
        double sec = 0;
    }

    /** -----------------------------------------------------------------------------
     *
     *                           procedure invjday
     *
     *  this procedure finds the year, month, day, hour, minute and second
     *  given the julian date. tu can be ut1, tdt, tdb, etc.
     *
     *  algorithm     : set up starting values
     *                  find leap year - use 1900 because 2000 is a leap year
     *                  find the elapsed days through the year in a loop
     *                  call routine to find each individual value
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    jd          - julian date                    days from 4713 bc
     *
     *  outputs       :
     *    year        - year                           1900 .. 2100
     *    mon         - month                          1 .. 12
     *    day         - day                            1 .. 28,29,30,31
     *    hr          - hour                           0 .. 23
     *    min         - minute                         0 .. 59
     *    sec         - second                         0.0 .. 59.999
     *
     *  locals        :
     *    days        - day of year plus fractional
     *                  portion of a day               days
     *    tu          - julian centuries from 0 h
     *                  jan 0, 1900
     *    temp        - temporary double values
     *    leapyrs     - number of leap years from 1900
     *
     *  coupling      :
     *    days2mdhms  - finds month, day, hour, minute and second given days and year
     *
     *  references    :
     *    vallado       2007, 208, alg 22, ex 3-13
     * ---------------------------------------------------------------------------
     *
     * @param jd
     * @return [year,mon,day,hr,minute,sec]
     */
    public static double[] invjday(double jd)
    {
        // return vars
        double year, mon, day, hr, minute, sec;

        int leapyrs;
        double days, tu, temp;

        /* --------------- find year and days of the year --------------- */
        temp = jd - 2415019.5;
        tu = temp / 365.25;
        year = 1900 + (int)Math.floor(tu);
        leapyrs = (int)Math.floor((year - 1901) * 0.25);

        // optional nudge by 8.64x10-7 sec to get even outputs
        days = temp - ((year - 1900) * 365.0 + leapyrs) + 0.00000000001;

        /* ------------ check for case of beginning of a year ----------- */
        if(days < 1.0)
        {
            year = year - 1;
            leapyrs = (int)Math.floor((year - 1901) * 0.25);
            days = temp - ((year - 1900) * 365.0 + leapyrs);
        }

        /* ----------------- find remaing data  ------------------------- */
        //days2mdhms(year, days, mon, day, hr, minute, sec);
        MDHMS mdhms = days2mdhms((int)year, days);
        mon = mdhms.mon;
        day = mdhms.day;
        hr = mdhms.hr;
        minute = mdhms.minute;
        sec = mdhms.sec;

        sec = sec - 0.00000086400; // ?

        return new double[]
                {
                    year, mon, day, hr, minute, sec
                };
    }  // end invjday

    /** -----------------------------------------------------------------------------
     *
     *                           function rv2coe
     *
     *  this function finds the classical orbital elements given the geocentric
     *    equatorial position and velocity vectors.
     *
     *  author        : david vallado                  719-573-2600   21 jun 2002
     *
     *  revisions
     *    vallado     - fix special cases                              5 sep 2002
     *    vallado     - delete extra check in inclination code        16 oct 2002
     *    vallado     - add constant file use                         29 jun 2003
     *    vallado     - add mu                                         2 apr 2007
     *
     *  inputs          description                    range / units
     *    r           - ijk position vector            km
     *    v           - ijk velocity vector            km / s
     *    mu          - gravitational parameter        km3 / s2
     *
     *  outputs       :
     *    p           - semilatus rectum               km
     *    a           - semimajor axis                 km
     *    ecc         - eccentricity
     *    incl        - inclination                    0.0  to pi rad
     *    omega       - longitude of ascending node    0.0  to 2pi rad
     *    argp        - argument of perigee            0.0  to 2pi rad
     *    nu          - true anomaly                   0.0  to 2pi rad
     *    m           - mean anomaly                   0.0  to 2pi rad
     *    arglat      - argument of latitude      (ci) 0.0  to 2pi rad
     *    truelon     - true longitude            (ce) 0.0  to 2pi rad
     *    lonper      - longitude of periapsis    (ee) 0.0  to 2pi rad
     *
     *  locals        :
     *    hbar        - angular momentum h vector      km2 / s
     *    ebar        - eccentricity     e vector
     *    nbar        - line of nodes    n vector
     *    c1          - v**2 - u/r
     *    rdotv       - r dot v
     *    hk          - hk unit vector
     *    sme         - specfic mechanical energy      km2 / s2
     *    i           - index
     *    e           - eccentric, parabolic,
     *                  hyperbolic anomaly             rad
     *    temp        - temporary variable
     *    typeorbit   - type of orbit                  ee, ei, ce, ci
     *
     *  coupling      :
     *    mag         - magnitude of a vector
     *    cross       - cross product of two vectors
     *    angle       - find the angle between two vectors
     *    newtonnu    - find the mean anomaly
     *
     *  references    :
     *    vallado       2007, 126, alg 9, ex 2-5
     * ---------------------------------------------------------------------------
     *
     * @param r
     * @param v
     * @param mu
     * @return [p, a, ecc, incl, omega, argp, nu, m, arglat, truelon, lonper]
     */
    public static double[] rv2coe(
            double[] r, double[] v, double mu//,
            //       double& p, double& a, double& ecc, double& incl, double& omega, double& argp,
            //       double& nu, double& m, double& arglat, double& truelon, double& lonper
            )
    {

        // return variables
        double p, a, ecc, incl, omega, argp, nu, m, arglat, truelon, lonper;

        // internal

        double undefined, small, magr, magv, magn, sme,
                rdotv, infinite, temp, c1, hk, twopi, magh, halfpi, e;
        double[] hbar = new double[3];
        double[] nbar = new double[3];
        double[] ebar = new double[3];

        int i;
        String typeorbit;

        twopi = 2.0 * Math.PI;
        halfpi = 0.5 * Math.PI;
        small = 0.00000001;
        undefined = 999999.1;
        infinite = 999999.9;

        // SEG m needs to be ini
        m = undefined;

        // -------------------------  implementation   -----------------
        magr = mag(r);
        magv = mag(v);

        // ------------------  find h n and e vectors   ----------------
        cross(r, v, hbar);
        magh = mag(hbar);
        if(magh > small)
        {
            nbar[0] = -hbar[1];
            nbar[1] = hbar[0];
            nbar[2] = 0.0;
            magn = mag(nbar);
            c1 = magv * magv - mu / magr;
            rdotv = dot(r, v);
            for(i = 0; i <= 2; i++)
            {
                ebar[i] = (c1 * r[i] - rdotv * v[i]) / mu;
            }
            ecc = mag(ebar);

            // ------------  find a e and semi-latus rectum   ----------
            sme = (magv * magv * 0.5) - (mu / magr);
            if(Math.abs(sme) > small)
            {
                a = -mu / (2.0 * sme);
            }
            else
            {
                a = infinite;
            }
            p = magh * magh / mu;

            // -----------------  find inclination   -------------------
            hk = hbar[2] / magh;
            incl = Math.acos(hk);

            // --------  determine type of orbit for later use  --------
            // ------ elliptical, parabolic, hyperbolic inclined -------
            typeorbit = "ei";
            if(ecc < small)
            {
                // ----------------  circular equatorial ---------------
                if((incl < small) | (Math.abs(incl - Math.PI) < small))
                {
                    typeorbit = "ce";
                }
                else
                // --------------  circular inclined ---------------
                {
                    typeorbit = "ci";
                }
            }
            else
            {
                // - elliptical, parabolic, hyperbolic equatorial --
                if((incl < small) | (Math.abs(incl - Math.PI) < small))
                {
                    typeorbit = "ee";
                }
            }

            // ----------  find longitude of ascending node ------------
            if(magn > small)
            {
                temp = nbar[0] / magn;
                if(Math.abs(temp) > 1.0)
                {
                    temp = Math.signum(temp);
                }
                omega = Math.acos(temp);
                if(nbar[1] < 0.0)
                {
                    omega = twopi - omega;
                }
            }
            else
            {
                omega = undefined;
            }

            // ---------------- find argument of perigee ---------------
            if(typeorbit.equalsIgnoreCase("ei") == true) // 0 = true for cpp strcmp
            {
                argp = angle(nbar, ebar);
                if(ebar[2] < 0.0)
                {
                    argp = twopi - argp;
                }
            }
            else
            {
                argp = undefined;
            }

            // ------------  find true anomaly at epoch    -------------
            if(typeorbit.startsWith("e"))//typeorbit[0] == 'e' )
            {
                nu = angle(ebar, r);
                if(rdotv < 0.0)
                {
                    nu = twopi - nu;
                }
            }
            else
            {
                nu = undefined;
            }

            // ----  find argument of latitude - circular inclined -----
            if(typeorbit.equalsIgnoreCase("ci") == true)
            {
                arglat = angle(nbar, r);
                if(r[2] < 0.0)
                {
                    arglat = twopi - arglat;
                }
                m = arglat;
            }
            else
            {
                arglat = undefined;
            }

            // -- find longitude of perigee - elliptical equatorial ----
            if((ecc > small) && (typeorbit.equalsIgnoreCase("ee") == true))
            {
                temp = ebar[0] / ecc;
                if(Math.abs(temp) > 1.0)
                {
                    temp = Math.signum(temp);
                }
                lonper = Math.acos(temp);
                if(ebar[1] < 0.0)
                {
                    lonper = twopi - lonper;
                }
                if(incl > halfpi)
                {
                    lonper = twopi - lonper;
                }
            }
            else
            {
                lonper = undefined;
            }

            // -------- find true longitude - circular equatorial ------
            if((magr > small) && (typeorbit.equalsIgnoreCase("ce") == true))
            {
                temp = r[0] / magr;
                if(Math.abs(temp) > 1.0)
                {
                    temp = Math.signum(temp);
                }
                truelon = Math.acos(temp);
                if(r[1] < 0.0)
                {
                    truelon = twopi - truelon;
                }
                if(incl > halfpi)
                {
                    truelon = twopi - truelon;
                }
                m = truelon;
            }
            else
            {
                truelon = undefined;
            }

            // ------------ find mean anomaly for all orbits -----------
            if(typeorbit.startsWith("e"))//typeorbit[0] == 'e' )
            {
                double[] tt = newtonnu(ecc, nu);
                e = tt[0];
                m = tt[1];
            }
        }
        else
        {
            p = undefined;
            a = undefined;
            ecc = undefined;
            incl = undefined;
            omega = undefined;
            argp = undefined;
            nu = undefined;
            m = undefined;
            arglat = undefined;
            truelon = undefined;
            lonper = undefined;
        }

        return new double[]
                {
                    p, a, ecc, incl, omega, argp, nu, m, arglat, truelon, lonper
                };
    }  // end rv2coe

    /** -----------------------------------------------------------------------------
     *
     *                           function newtonnu
     *
     *  this function solves keplers equation when the true anomaly is known.
     *    the mean and eccentric, parabolic, or hyperbolic anomaly is also found.
     *    the parabolic limit at 168ø is arbitrary. the hyperbolic anomaly is also
     *    limited. the hyperbolic sine is used because it's not double valued.
     *
     *  author        : david vallado                  719-573-2600   27 may 2002
     *
     *  revisions
     *    vallado     - fix small                                     24 sep 2002
     *
     *  inputs          description                    range / units
     *    ecc         - eccentricity                   0.0  to
     *    nu          - true anomaly                   -2pi to 2pi rad
     *
     *  outputs       :
     *    e0          - eccentric anomaly              0.0  to 2pi rad       153.02 ø
     *    m           - mean anomaly                   0.0  to 2pi rad       151.7425 ø
     *
     *  locals        :
     *    e1          - eccentric anomaly, next value  rad
     *    sine        - sine of e
     *    cose        - cosine of e
     *    ktr         - index
     *
     *  coupling      :
     *    asinh       - arc hyperbolic sine
     *
     *  references    :
     *    vallado       2007, 85, alg 5
     * ---------------------------------------------------------------------------
     *
     * @param ecc
     * @param nu
     * @return [e0, m]
     */
    public static double[] newtonnu(double ecc, double nu)
    {
        // return vars
        double e0, m;

        // internal

        double small, sine, cose;

        // ---------------------  implementation   ---------------------
        e0 = 999999.9;
        m = 999999.9;
        small = 0.00000001;

        // --------------------------- circular ------------------------
        if(Math.abs(ecc) < small)
        {
            m = nu;
            e0 = nu;
        }
        else // ---------------------- elliptical -----------------------
        if(ecc < 1.0 - small)
        {
            sine = (Math.sqrt(1.0 - ecc * ecc) * Math.sin(nu)) / (1.0 + ecc * Math.cos(nu));
            cose = (ecc + Math.cos(nu)) / (1.0 + ecc * Math.cos(nu));
            e0 = Math.atan2(sine, cose);
            m = e0 - ecc * Math.sin(e0);
        }
        else // -------------------- hyperbolic  --------------------
        if(ecc > 1.0 + small)
        {
            if((ecc > 1.0) && (Math.abs(nu) + 0.00001 < Math.PI - Math.acos(1.0 / ecc)))
            {
                sine = (Math.sqrt(ecc * ecc - 1.0) * Math.sin(nu)) / (1.0 + ecc * Math.cos(nu));
                e0 = asinh(sine);
                m = ecc * Math.sinh(e0) - e0;
            }
        }
        else // ----------------- parabolic ---------------------
        if(Math.abs(nu) < 168.0 * Math.PI / 180.0)
        {
            e0 = Math.tan(nu * 0.5);
            m = e0 + (e0 * e0 * e0) / 3.0;
        }

        if(ecc < 1.0)
        {
            m = (m % (2.0 * Math.PI));
            if(m < 0.0)
            {
                m = m + 2.0 * Math.PI;
            }
            e0 = e0 % (2.0 * Math.PI);
        }

        return new double[]
                {
                    e0, m
                };
    }  // end newtonnu


    /* -----------------------------------------------------------------------------
     *
     *                           function asinh
     *
     *  this function evaluates the inverse hyperbolic sine function.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    xval        - angle value                                  any real
     *
     *  outputs       :
     *    arcsinh     - result                                       any real
     *
     *  locals        :
     *    none.
     *
     *  coupling      :
     *    none.
     *
     * --------------------------------------------------------------------------- */
    public static double asinh(double xval)
    {
        return Math.log(xval + Math.sqrt(xval * xval + 1.0));
    }  // end asinh

    /* -----------------------------------------------------------------------------
     *
     *                           function mag
     *
     *  this procedure finds the magnitude of a vector.  the tolerance is set to
     *    0.000001, thus the 1.0e-12 for the squared test of underflows.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    vec         - vector
     *
     *  outputs       :
     *    vec         - answer stored in fourth component
     *
     *  locals        :
     *    none.
     *
     *  coupling      :
     *    none.
     * --------------------------------------------------------------------------- */
    public static double mag(double[] x)
    {
        return Math.sqrt(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
    }  // end mag

    /* -----------------------------------------------------------------------------
     *
     *                           procedure cross
     *
     *  this procedure crosses two vectors.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    vec1        - vector number 1
     *    vec2        - vector number 2
     *
     *  outputs       :
     *    outvec      - vector result of a x b
     *
     *  locals        :
     *    none.
     *
     *  coupling      :
     *    mag           magnitude of a vector
    ---------------------------------------------------------------------------- */
    public static void cross(double[] vec1, double[] vec2, double[] outvec)
    {
        outvec[0] = vec1[1] * vec2[2] - vec1[2] * vec2[1];
        outvec[1] = vec1[2] * vec2[0] - vec1[0] * vec2[2];
        outvec[2] = vec1[0] * vec2[1] - vec1[1] * vec2[0];
    }  // end cross

    /* -----------------------------------------------------------------------------
     *
     *                           function dot
     *
     *  this function finds the dot product of two vectors.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    vec1        - vector number 1
     *    vec2        - vector number 2
     *
     *  outputs       :
     *    dot         - result
     *
     *  locals        :
     *    none.
     *
     *  coupling      :
     *    none.
     *
     * --------------------------------------------------------------------------- */
    public static double dot(double[] x, double[] y)
    {
        return (x[0] * y[0] + x[1] * y[1] + x[2] * y[2]);
    }  // end dot

    /* -----------------------------------------------------------------------------
     *
     *                           procedure angle
     *
     *  this procedure calculates the angle between two vectors.  the output is
     *    set to 999999.1 to indicate an undefined value.  be sure to check for
     *    this at the output phase.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    vec1        - vector number 1
     *    vec2        - vector number 2
     *
     *  outputs       :
     *    theta       - angle between the two vectors  -pi to pi
     *
     *  locals        :
     *    temp        - temporary real variable
     *
     *  coupling      :
     *    dot           dot product of two vectors
     * --------------------------------------------------------------------------- */
    public static double angle(double[] vec1, double[] vec2)
    {
        double small, undefined, magv1, magv2, temp;
        small = 0.00000001;
        undefined = 999999.1;

        magv1 = mag(vec1);
        magv2 = mag(vec2);

        if(magv1 * magv2 > small * small)
        {
            temp = dot(vec1, vec2) / (magv1 * magv2);
            if(Math.abs(temp) > 1.0)
            {
                temp = Math.signum(temp) * 1.0;
            }
            return Math.acos(temp);
        }
        else
        {
            return undefined;
        }
    }  // end angle
}
