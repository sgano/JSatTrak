/*
 * Stopping Condition for any apsis (cloesest/furtherest approach) - dR/dt crosses a 0 from decreasing to increasing (peri) or increasing to decreasing (apo)
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

package name.gano.astro.propogators.solvers;

import java.util.Vector;
import jsattrak.utilities.StateVector;
import name.gano.astro.MathUtils;
import name.gano.math.interpolation.PolynomialInterp;

/**
 *
 * @author sgano
 */
public class ApsisStopCond implements StoppingCondition
{
    public static int APOAPSIS = -1; // apoapsis stopping condition
    public static int PERIAPSIS = 1; // perapsis stopping condition
    
    boolean stopCondMet = false;
    
    double lastT=0;
    double lastRdot;    
    
    private int apoOrPeriCondition = ApsisStopCond.PERIAPSIS; // default
    
    Vector<StateVector> ephemeris; // holds pointer to ephemeris for cleaning up after condition met
    
    long updateCount = 0; // keeps track of how many times this function is called (for interpolation rutines)
    
    /**
     * Creates a new Apsis Stopping condition, use ApsisStopCond.APOAPSIS for apoapsis, or ApsisStopCond.PERIAPSIS for periapsis
     * @param apoOrPeriCondition
     */
    public ApsisStopCond(int apoOrPeriCondition, Vector<StateVector> ephemeris)
    {
        this.apoOrPeriCondition = apoOrPeriCondition;
        this.ephemeris = ephemeris;
    }
    
    // initializes stopping condition
    public void iniStoppingCondition(double t, double[] x, double[] dx)
    {
        stopCondMet = false;
        lastRdot = calculateMagRdot(x, dx);
        updateCount = 1; // updated once
        
    } //iniStoppingCondition
    
    // check the new step to see if it meets the stopping condition
    public boolean checkStoppingCondition(double t, double[] x, double[] dx)
    {
        stopCondMet = false;
        updateCount++; // update count
        
        double currentRdot =  calculateMagRdot(x, dx);
        
        if( apoOrPeriCondition*lastRdot <=0 && apoOrPeriCondition*currentRdot >=0)
        {
            stopCondMet = true;
            replaceStoppedLastState();
        }
        
        lastRdot = currentRdot; // save for next update
        
        return stopCondMet;
        
    } //checkStoppingCondition
    
    // function used to calculate last state, if the stopping condition forced the iterator to stop
    // use interpolation methods of other methods to determine this
    // only should replace a state if this condition caused the stop
    private void replaceStoppedLastState()
    {
      
        // replace last state with interpolated state to the apsis point (use up to 8th order)
        // ephemeris
        //System.out.println("STOPPED");

        int interpolPoints = 9; // interpolation points for finding 0 point (degree = -1)

        if (updateCount < interpolPoints) // assumes epehermis is at least this long (which it better be)
        {
            interpolPoints = (int) updateCount; // take what we can get (the last few)
        }

        // okay get data needed for interp and root finding
        double[] t = new double[interpolPoints];  // times
        double[][] x = new double[3][interpolPoints]; // j2k pos vectors
        double[][] dx = new double[3][interpolPoints]; // vel vectors
        double[] rDot = new double[interpolPoints]; // rDot values

        double t0 = ephemeris.lastElement().state[0]; // get inital time for scaling purposes
        
        for (int i = 0; i < interpolPoints; i++)
        {
            int ephemVal = ephemeris.size() - 1 - i;
            double[] sv = ephemeris.get(ephemVal).state;
            t[i] = sv[0] - t0;  // time minus t0
            x[0][i] = sv[1];
            x[1][i] = sv[2];
            x[2][i] = sv[3];
            dx[0][i] = sv[4];
            dx[1][i] = sv[5];
            dx[2][i] = sv[6];
            rDot[i] = calculateMagRdot(new double[] {sv[1],sv[2],sv[3]}, new double[] {sv[4],sv[5],sv[6]});
        }
        
        // find root of rDot using polynomial interpolation
        // we know root is in the range of the last two times
        // use secant method to find solution
        double finalTScaled = secantMethod(t[0], t[1], 1E-12, 50, t, rDot);
        
//        System.out.println("Secant [" + rDot[0] + "," + rDot[1] + "] ");
//        System.out.println("Secant [" + t[0] + "," + t[1] + "]: " + finalTScaled);
//        double error = Math.abs( PolynomialInterp.polint(t, rDot, finalTScaled) );
//        System.out.println("Error:" + error);
        
        // save new values in final ephemeris location
        StateVector sv = ephemeris.lastElement();
        
        sv.state[0] = finalTScaled + t0; // time
        
        for(int i=0; i<3;i++)
        {
            sv.state[1+i] = PolynomialInterp.polint(t, x[i], finalTScaled); // x,y,z
            sv.state[4+i] = PolynomialInterp.polint(t, dx[i], finalTScaled); // dx,dy,dz
        }
        
//        System.out.println("sv:" + sv.toString());
//        System.out.println("em:" + ephemeris.lastElement().toString());
                

    } //replaceStoppedLastState
    
    
    private double calculateMagRdot(double[] x, double[] dx)
    {
        double[] R = new double[]{x[0], x[1], x[2]};
        double normR = MathUtils.norm(R);
        R = MathUtils.scale(R, 1.0 / normR); // unit vector

        double[] v = new double[]{dx[0],dx[1],dx[2]};
        double rDot = MathUtils.dot(v, R);
        return rDot;
    } // calculateMagRdot
    
    // ---- SECANT METHOD --- really need to make this a solver class.....
    //---------------------------------------
    //  SECANT Routines to find zeros of rDot from a polynomial  -- if this fails maybe resort to bisection method
    // xn_1 = time guess 1
    // xn = time guess 2
    // tol = convergence tolerance
    // maxIter = maximum iterations allowed
    // RETURNS: double = time of apsis
    private double secantMethod(double xn_1, double xn, double tol, int maxIter, double[] t, double[] rDot)
    {

        double d;
        
        // calculate functional values at guesses
        double fn_1 = PolynomialInterp.polint(t, rDot, xn_1);
        double fn = PolynomialInterp.polint(t, rDot, xn);
        
        for (int n = 1; n <= maxIter; n++)
        {
            d = (xn - xn_1) / (fn - fn_1) * fn;
            if (Math.abs(d) < tol ) // convergence check
            {
                // System.out.println("Iters:"+n);
                return xn;
            }
            
            // save past point
            xn_1 = xn;
            fn_1 = fn;
            
            // new point
            xn = xn - d;
            fn = PolynomialInterp.polint(t, rDot, xn);
            
            // check if new point is within tol/1000; too small of step
            if(Math.abs(xn -  xn_1 ) < tol/1000 )
            {
                return xn;
            }
        }
        
        System.out.println("Warning: Secant Method - Max Iteration limit reached finding Apsis Stopping Condition.");
        
        return xn;
    } // secantMethod
    
}
