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
// simple test of the SGP4 propagator
package name.gano.astro.propogators.sgp4_cssi;

/**
 * 19 June 2009
 * @author Shawn E. Gano, shawn@gano.name
 */
public class SGP4test
{
    public static void main(String[] args)
    {
        // new sat data object
        SGP4SatData data = new SGP4SatData();

        // tle data
        String name = "ISS (ZARYA)";
        String line1 = "1 25544U 98067A   09161.51089941  .00015706  00000-0  11388-3 0   112";
        String line2 = "2 25544  51.6406 341.1646 0009228  98.8703 312.6668 15.73580432604904";

        // options
        char opsmode = SGP4utils.OPSMODE_IMPROVED; // OPSMODE_IMPROVED
        SGP4unit.Gravconsttype gravconsttype = SGP4unit.Gravconsttype.wgs72;

        // read in data and ini SGP4 data
        boolean result1 = SGP4utils.readTLEandIniSGP4(name, line1, line2, opsmode, gravconsttype, data);
        if(!result1)
        {
            System.out.println("Error Reading / Ini Data, error code: " + data.error);
            return;
        }

        // prop to a given date
        double propJD = 2454994.0; // JD to prop to
        double minutesSinceEpoch = (propJD-data.jdsatepoch)*24.0*60.0;
        double[] pos = new double[3];
        double[] vel = new double[3];

        boolean result = SGP4unit.sgp4(data, minutesSinceEpoch, pos, vel);
        if(!result)
        {
            System.out.println("Error in Sat Prop");
            return;
        }

        // output
        System.out.println("Epoch of TLE (JD): " + data.jdsatepoch);
        System.out.println(minutesSinceEpoch + ", " + pos[0]+ ", " + pos[1]+ ", " + pos[2]+ ", " + vel[0]+ ", " + vel[1]+ ", " + vel[2]);

        double[] stk8Results = new double[] {-2881017.428533447,-3207508.188455666,-5176685.907342243};
        double[] stk9Results = new double[] {-2881017.432281017,-3207508.189681858,-5176685.904856035};
        
        double dX = norm( sub( scale(pos,1000.0), stk8Results) );
        System.out.println("Error from STk8 (m) : " + dX);
        double dX2 = norm( sub( scale(pos,1000.0), stk9Results) );
        System.out.println("Error from STk9 (m) : " + dX2);

    }

     /**
     * vector subtraction
     *
     * @param a vector of length 3
     * @param b vector of length 3
     * @return a-b
     */
	public static double[] sub(double[] a, double[] b)
	{
		double[] c = new double[3];
		for(int i=0;i<3;i++)
		{
			c[i] = a[i] - b[i];
		}

		return c;
	}

    //	vector 2-norm
    /**
     * vector 2-norm
     *
     * @param a vector of length 3
     * @return norm(a)
     */
	public static double norm(double[] a)
	{
		double c = 0.0;

		for(int i=0;i<a.length;i++)
		{
			c += a[i]*a[i];
		}

		return Math.sqrt(c);
	}

    //	multiply a vector times a scalar
    /**
     * multiply a vector times a scalar
     *
     * @param a a vector of length 3
     * @param b scalar
     * @return a * b
     */
	public static double[] scale(double[] a, double b)
	{
		double[] c = new double[3];

		for(int i=0;i<3;i++)
		{
			c[i] = a[i]*b;
		}

		return c;
	}
}
