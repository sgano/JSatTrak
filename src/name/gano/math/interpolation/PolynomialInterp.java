/**
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
package name.gano.math.interpolation;


/**
 * Polynomial Interpolation Methods
 * 
 * Given arrays xa[1..n] and ya[1..n], and given a value x, this routine returns a value y, and
 *    an error estimate dy. If P(x) is the polynomial of degree N - 1 such that P(xai) = yai, i =
 *    1, . . . , n, then the returned value y = P(x).
 *    adaptive from Numerical Recipes
 *    output should be y, dy (y is new value, dy is error estimate evaluated at x)
 * 
 * @author Shawn E. Gano
 */
public class PolynomialInterp
{
    //	Given arrays xa[1..n] and ya[1..n], and given a value x, this routine returns a value y, and
    //	an error estimate dy. If P(x) is the polynomial of degree N - 1 such that P(xai) = yai, i =
    //	1, . . . , n, then the returned value y = P(x).
    // adaptive from Numerical Recipes
    // output should be y, dy (y is new value, dy is error estimate evaluated at x)
    
    /**
     * Static method to perform polynomial interpolation of degree N-1, where N is the number of points given.
     * @param xa Array of x values
     * @param ya Array of f(x) values
     * @param x  x location where value is to be interpolated at.
     * @return interpolated value f(x)
     */
    public static double polint(double[] xa, double[] ya, double x)
    {
        // number of samples ( polyomial degree will be n-1)
        int n = xa.length;

        int ns = 1;
        double den, dif, dift, ho, hp, w;
        double[] c = new double[n];
        double[] d = new double[n];

        double y, dy;

        dif = Math.abs(x - xa[1]);

        for (int i = 1; i <= n; i++) //Here we find the index ns of the closest table entry, 
        {
            if ((dift = Math.abs(x - xa[i - 1])) < dif)
            {
                ns = i;
                dif = dift;
            }
            c[i - 1] = ya[i - 1]; //and initialize the tableau of c’s and d’s.
            d[i - 1] = ya[i - 1];
        }


        y = ya[ns-- - 1]; //This is the initial approximation to y.
        for (int m = 1; m < n; m++)
        { //For each column of the tableau,
            for (int i = 1; i <= n - m; i++)
            { //we loop over the current c’s and d’s and update them. 
                ho = xa[i - 1] - x;
                hp = xa[i + m - 1] - x;
                w = c[i + 1 - 1] - d[i - 1];

                // This error can occur only if two input xa’s are (to within roundoff) identical.
                if ((den = ho - hp) == 0.0)
                {
                    System.out.println("Error in routine PolynomialInterp.polint");
                    //nrerror("Error in routine polint");
                    return ya[0]; // return the first value
                }

                den = w / den;
                d[i - 1] = hp * den;// Here the c’s and d’s are updated.
                c[i - 1] = ho * den;
            }

            dy = (2 * ns < (n - m) ? c[ns + 1 - 1] : d[ns-- - 1]);
            y += dy;

        /*After each column in the tableau is completed, we decide which correction, c or d,
        we want to add to our accumulating value of y, i.e., which path to take through the
        tableau—forking up or down. We do this in such a way as to take the most “straight
        line” route through the tableau to its apex, updating ns accordingly to keep track of
        where we are. This route keeps the partial approximations centered (insofar as possible)
        on the target x. The last dy added is thus the error indication.*/
        }// for

        return y; // dy is the error estimate
    } // polint

    // test driving function
    public static void main(String[] args)
    {
        double[] xx = new double[]{0., 1, 2, 3, 4, 5, 6, 7};
        double[] yy = new double[]{0,
            0.38941834230865,
            0.71735609089952,
            0.93203908596723,
            0.99957360304151,
            0.90929742682568,
            0.67546318055115,
            0.33498815015590
        };

        double x = 5.5;

        double y = 0;
        int runs = 1000000;
        long simStartTime = System.currentTimeMillis();
        for (int i = 0; i < runs; i++)
        {
            y = polint(xx, yy, x);
        }
        long simEndTime = System.currentTimeMillis();

        System.out.println("x = " + x + ", y = " + y + ", average exe time [ms] = " + (1.0 * simEndTime - 1.0 * simStartTime) / runs);

    } // main
} // PolynomialInterp, class
