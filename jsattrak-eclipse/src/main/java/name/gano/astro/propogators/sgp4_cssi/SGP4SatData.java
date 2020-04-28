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
// based off of the "typedef struct elsetrec" in the CSSI's sgp4unit.h file
// conatins all the data needed for a SGP4 propogated satellite
// holds all initialization info, etc.
package name.gano.astro.propogators.sgp4_cssi;

import java.io.Serializable;

/**
 * 19 June 2009
 * converted to Java by:
 * @author Shawn E. Gano, shawn@gano.name
 */
public class SGP4SatData implements Serializable
{
  public int   satnum; // changed to int SEG
  public int    epochyr, epochtynumrev;
  public int    error; // 0 = ok, 1= eccentricity (sgp4),   6 = satellite decay, 7 = tle data
  public char   operationmode;
  public char   init, method;

  public SGP4unit.Gravconsttype gravconsttype; // gravity constants to use - SEG

  /* Near Earth */
  public int    isimp;
  public double aycof  , con41  , cc1    , cc4      , cc5    , d2      , d3   , d4    ,
                delmo  , eta    , argpdot, omgcof   , sinmao , t       , t2cof, t3cof ,
                t4cof  , t5cof  , x1mth2 , x7thm1   , mdot   , nodedot, xlcof , xmcof ,
                nodecf;

  /* Deep Space */
  public int    irez;
  public double d2201  , d2211  , d3210  , d3222    , d4410  , d4422   , d5220 , d5232 ,
                d5421  , d5433  , dedt   , del1     , del2   , del3    , didt  , dmdt  ,
                dnodt  , domdt  , e3     , ee2      , peo    , pgho    , pho   , pinco ,
                plo    , se2    , se3    , sgh2     , sgh3   , sgh4    , sh2   , sh3   ,
                si2    , si3    , sl2    , sl3      , sl4    , gsto    , xfact , xgh2  ,
                xgh3   , xgh4   , xh2    , xh3      , xi2    , xi3     , xl2   , xl3   ,
                xl4    , xlamo  , zmol   , zmos     , atime  , xli     , xni;

  public double a      , altp   , alta   , epochdays, jdsatepoch       , nddot , ndot  ,
                bstar  , rcse   , inclo  , nodeo    , ecco             , argpo , mo    ,
                no;

  // Extra Data added by SEG - from TLE and a name variable (and save the lines for future use)
  public String name="", line1="", line2="";
  public boolean tleDataOk;
  public String classification, intldesg;
  public int nexp, ibexp, numb; // numb is the second number on line 1
  public long elnum,revnum; 

}
