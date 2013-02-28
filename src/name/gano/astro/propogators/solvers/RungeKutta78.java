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
package name.gano.astro.propogators.solvers;

import java.text.DecimalFormat;
import java.util.Vector;
import jsattrak.utilities.StateVector;

/**
 *
 * @author Shawn E. Gano
 */
/** Implements a Runge-Kutta-Fehlberg adaptive step size integrator
 * from Numerical Recipes. Modified to RK78 from the original RK45 in NR.
 * RK78 values from Erwin Fehlberg, NASA TR R-287
 * 
 */
// modified to work with SSS - and integrating a second order ODE
public class RungeKutta78
{

    // inital default parameters
    private double minStepSize = 1.0; // 1 second
    private double maxStepSize = 600.0; //10 minutes
    private double stepSize = 60.0;
    private double currentStepSize = 60.0;
    private double accuracy = 1.00e-013;
    private boolean verbose = false;
    private boolean adaptive = true; // adapt step size
    private static final int MAXSTP = 1000000; // max number of steps allowed
    private static final double TINY = 1.0e-30;
    // problem to integrate
    private OrbitProblem func;
    // position and velocity variables, start and end time
    private double[] var;
    private double[] vel;
    private double startTime;
    private double endTime;
    // internal parameters - stats
    private int[] nok = new int[1];  // number of good steps
    private int[] nbad = new int[1]; // number of bad steps
    // time global value
    private double time = 0.0;
    // output dataformat
    private DecimalFormat d12 = new DecimalFormat("0.00000000000E0"); // display format
    // debug flag
    private boolean debug = false; // don't check options
    // step stats
    private double maxStep = 0,  minStep = 0;
    // Adaptive parameters - recomended by numerical recipes
    private static final double SAFETY = 0.9;
    private static final double PGROW = -1.0 / 8.0;
    private static final double PSHRNK = -1.0 / 7.0;
    private static final double ERRCON = 2.56578451395034701E-8;
    
    // vector of stopping conditions
    Vector<StoppingCondition> stopConditionsVec = new Vector<StoppingCondition>();

    /** Default constructor.
     */
    public RungeKutta78(double t0, double tEnd, double[] var0, double[] vel0, OrbitProblem func)
    {
        // all default parameters

        // inital state and times
        var = new double[var0.length];
        vel = new double[vel0.length];
        for (int i = 0; i < var0.length; i++)
        {
            var[i] = var0[i];
            vel[i] = vel0[i];
        }
        startTime = t0;
        endTime = tEnd;

        // problem
        this.func = func;
    }

    /** Explicit constructor.
     */
    public RungeKutta78(double t0, double tEnd, double[] var0, double[] vel0, OrbitProblem func, double minStepSize, double maxStepSize, double iniStepSize, double accuracy, boolean adaptive)
    {
        // problem
        this.func = func;

        // inital state and times
        var = new double[var0.length];
        vel = new double[vel0.length];
        for (int i = 0; i < var0.length; i++)
        {
            var[i] = var0[i];
            vel[i] = vel0[i];
        }
        startTime = t0;
        endTime = tEnd;

        // parameters
        this.minStepSize = minStepSize;
        this.maxStepSize = maxStepSize;
        this.stepSize = iniStepSize;
        this.currentStepSize = iniStepSize;
        this.accuracy = accuracy;
        this.adaptive = adaptive;
    }

    /** Solve the proble / Integrate the equations of motion. 
     * @param start Initial time. , t0
     * @param y Initial state values, var
     * @param end Final time., endTime
     * @param dv Equations of Motion.
     * @param pr Printable.
     * @param print_switch Print flag. True = call the print method.
     * @return the final state.
     */
    public long solve()
    {
        double h = stepSize;
        double hmin = minStepSize;
        nok[0] = nbad[0] = 0;
        maxStep = stepSize;
        minStep = stepSize;

        // first update to global time
        time = startTime;

        // timer for execution time
        long simStartTime = System.currentTimeMillis();

        // open output file if needed
        if (debug)
        {
            System.out.println(0 + " " + var[0] + " " + var[1] + " " + var[2] + " " + vel[0] + " " + vel[1] + " " + vel[2]);
        }
        else
        {
            checkOutputOptions();
        }
        
        // initialize stopping conditions
        for (StoppingCondition sc : stopConditionsVec)
        {
            sc.iniStoppingCondition(time, var, vel);
        }

        if (adaptive)
        {
            odeint(var, vel, startTime, endTime, accuracy, h, hmin, nok, nbad);
            if (verbose)
            {
                System.out.println("nok = " + nok[0] + "\tnbad = " + nbad[0]);
            }
        }
        else // not adaptive
        {
            rkdumb(var, startTime, endTime, h);
        }

        // close output file if needed
        if (!debug)
        {
//        	if(func.getSTKOutput())
//        	{
//        		func.exportEphemeris(); // save ephemeris out to file
//        		// must do this afterwards because for the adaptive case, we don't know number of steps a priori
//        	} 
        }

        // end timer
        long simEndTime = System.currentTimeMillis();

        return simEndTime - simStartTime;
    }

    // Runge-Kutta fixed size 8th order integrator
    //------------------------------------------------
    // ystart - starting position vector
    // start - starting time (time var is x)
    // end - ending time
    // h - step size
    private void rkdumb(double[] ystart, double start, double end, double h)
    {
        int nvar = ystart.length;

        int nSteps = (int) Math.abs((end - start) / h);
        if (nSteps < 1)
        {
            nSteps = 1;
        }
        h = (end - start) / nSteps;

        //double[] dydx = new double[nvar];
        double[] yend = new double[nvar];
        double[] yerr = new double[nvar];
        double[] velEnd = new double[nvar];
        double[] velErr = new double[nvar];

        // debug
        //System.out.println("nSteps: " + nSteps + ", h: "+h+", end: " + end + ", start: " + start);  

        double x = start + 0 * h; // update time
        time = x; // update global time

        TIME_STEPPING: for (int step = 0; step < nSteps; step++)
        {
            //dydx = func.derivs(x, ystart); // should have these [no call nessesary)
            rkck(ystart, vel, x, h, yend, yerr, velEnd, velErr);
            for (int n = 0; n < nvar; n++)
            {
                ystart[n] = yend[n];  // start next iteration at end of this one
                vel[n] = velEnd[n];
            }
            // update time
            x = start + (step + 1.0) * h; // update time
            time = x; // update global time

            // print out results (debug)
            if (debug)
            {
                System.out.println((x + h) + " " + var[0] + " " + var[1] + " " + var[2] + " " + vel[0] + " " + vel[1] + " " + vel[2]);
            }
            else
            {
                checkOutputOptions(); // check output options every time step
            //System.out.println((x+h) +" " + var[0] +" " + var[1]+" " + var[2] +" " + vel[0]+" " + vel[1]+" " + vel[2]);
            }
            
            // check stopping conditions - if met - fix last epehermis point and break
            for (StoppingCondition sc : stopConditionsVec)
            {
                if (sc.checkStoppingCondition(time, var, vel))
                {
                    // if stopped automatically fixes state all we need to to is break the propogation
                    break TIME_STEPPING;
                }
            }

        }//for each step

    }// rkdumb
    private void odeint(double[] ystart, double[] velStart, double x1, double x2,
            double eps, double h1, double hmin, int[] nok,
            int[] nbad)
    {
        int nvar = ystart.length;

        double[] x = new double[1]; // time
        double[] hnext = new double[1]; // next step size
        double[] hdid = new double[1];  // current step??
        double[] yscal = new double[nvar];
        double[] velScal = new double[nvar];
        double[] y = new double[nvar];
        double[] dydx = new double[nvar];   // need intial velocity here!!!
        x[0] = x1;                         // this is the time
        double h = Math.abs(h1);

        if (x2 < x1) // step backwards
        {
            h = -h;
        }

        nok[0] = nbad[0] = 0;  // ini step counts
        y = ystart;
        dydx = velStart;


        for (int nstp = 1; nstp <= MAXSTP; nstp++)
        {
            //dydx = func.derivs(x[0], y);  // this should already be known
            // do we need to scale vel????
            for (int i = 0; i < nvar; i++)
            {
                yscal[i] = Math.abs(y[i]) + Math.abs(dydx[i] * h) + TINY;
                velScal[i] = Math.abs(dydx[i]) + TINY; // do I need second derivative to estimate this?
            }


            if ((x[0] + h - x2) * (x[0] + h - x1) > 0.0)
            {
                h = x2 - x[0]; // only one step needed to reach end
            }

            rkqs(y, dydx, x, h, eps, yscal, velScal, hdid, hnext);

            if (hdid[0] == h)
            {
                ++nok[0];

            } // good step
            else  // bad step meaning, the step size was reduced and solution found at smaller step
            {
                ++nbad[0];
            //System.out.println("Bad Step");
            }

            // update time
            time = x[0]; // update global time

            //  save results after a step
            if (debug)
            {
                // print out results (debug)
                System.out.println((x[0]) + " " + var[0] + " " + var[1] + " " + var[2] + " " + vel[0] + " " + vel[1] + " " + vel[2]);
            }
            else
            {
                time = x[0];
                checkOutputOptions(); // check output options every time step
            }

            // statistics on step sizes
            if (nstp == 1)
            {
                maxStep = hdid[0];
                minStep = hdid[0];
            }
            else
            {
                if (hdid[0] > maxStep)
                {
                    maxStep = hdid[0];
                }
                if (hdid[0] < minStep)
                {
                    minStep = hdid[0];
                }
            }

            // check stopping conditions - if met - fix last epehermis point and break
            for (StoppingCondition sc : stopConditionsVec)
            {
                if (sc.checkStoppingCondition(time, var, vel))
                {
                    // if stopped automatically fixes state all we need to to is break the propogation
                    return; // just return from function
                }
            }


            // are we done?
            if ((x[0] - x2) * (x2 - x1) >= 0.0)
            {
                return;
            }


            if (Math.abs(hnext[0]) < hmin)
            {
            //error("Step size too small in odeint");
            //System.out.println("h = "+hnext[0]);
            // NEED TO INFORM USER IN GUI when THIS HAPPENS
              hnext[0] = minStepSize; // set step to min step size
            }
            if (Math.abs(hnext[0]) > maxStepSize)
            {
                //error("Step size too large in odeint");
                //System.out.println("too large h = "+hnext[0]);
                // just reduce step size
                if (hnext[0] > 0)
                {
                    hnext[0] = maxStepSize;
                }
                else
                {
                    hnext[0] = -maxStepSize;
                }
            }

            h = hnext[0];
            currentStepSize = h;            // added for comphys
        //            System.out.println("Current Step Size = "+h);
        }
        error("Too many steps in routine odeint");
        System.out.println("step size = " + currentStepSize);
    } // odeint

    public double getMinStep()
    {
        return minStep;
    }

    public double getMaxStep()
    {
        return maxStep;
    }

    public void checkOutputOptions()
    {
        // verbosity to the console
        if (func.getVerbose())
        {
            System.out.println(d12.format(time) + " " + d12.format(var[0]) + " " + d12.format(var[1]) + " " + d12.format(var[2]) + " " + d12.format(vel[0]) + " " + d12.format(vel[1]) + " " + d12.format(vel[2]));
        }


        // Do we need to save the ephemeris to memory?
//		if(func.getStoreEphemeris())
//		{
        func.addState2Ephemeris(new StateVector(time, var[0], var[1], var[2], vel[0], vel[1], vel[2]));
//		} // store ephemeris



    }// output params
    // quality controlled runge-kutta step
    protected void rkqs(double[] y, double[] dydx, double[] x,
            double htry, double eps, double[] yscal, double[] velScale,
            double[] hdid, double[] hnext)
    {
        int n = y.length;
        double errmax = 0;
        double[] yerr = new double[n];
        double[] ytemp = new double[n];
        double[] velErr = new double[n];
        double[] velTemp = new double[n];
        double h = htry;

        for (;;) // repeat until we have a succesfull step
        {
            rkck(y, dydx, x[0], h, ytemp, yerr, velTemp, velErr);
            //rkck(ystart, vel, x, h, yend, yerr,velEnd,velErr); // lost more fixes needed here
            errmax = 0;
            for (int i = 0; i < n; i++)
            {
                errmax = Math.max(errmax, Math.abs(yerr[i] / yscal[i]));
                errmax = Math.max(errmax, Math.abs(velErr[i] / velScale[i]));
            }
            errmax /= eps;
            if (errmax <= 1.0)
            {
                break;
            }
            double htemp = SAFETY * h * Math.pow(errmax, PSHRNK);
            h = (h >= 0.0 ? Math.max(htemp, 0.1 * h) : Math.min(htemp, 0.1 * h));
            double xnew = x[0] + h;
            if (xnew == x[0])
            {
                error("stepsize underflow in rkqs");
            }
        } // for ;;

        if (errmax > ERRCON)
        {
            hnext[0] = SAFETY * h * Math.pow(errmax, PGROW);
        }
        else
        {
            hnext[0] = 5.0 * h;
        }

        x[0] += (hdid[0] = h);
        for (int i = 0; i < n; i++)
        {
            y[i] = ytemp[i];  // next step location
            dydx[i] = velTemp[i]; // advance derivatives here too
        }

    } // rkqs
    private static final double[] a = {0.0, 2.0 / 27.0, 1.0 / 9.0, 1.0 / 6.0, 5.0 / 12.0, 0.5,
        5.0 / 6.0, 1.0 / 6.0, 2.0 / 3.0, 1.0 / 3.0, 1.0, 0.0, 1.0
    };
    private static final double[][] b = new double[13][12];

    static
    {
        for (int i = 0; i < 13; i++)
        {
            for (int j = 0; j < 12; j++)
            {
                b[i][j] = 0.0;
            }
        }

        b[1][0] = 2.0 / 27.0;
        b[2][0] = 1.0 / 36.0;
        b[2][1] = 1.0 / 12.0;
        b[3][0] = 1.0 / 24.0;
        b[3][2] = 1.0 / 8.0;
        b[4][0] = 5.0 / 12.0;
        b[4][2] = -25.0 / 16.0;
        b[4][3] = 25.0 / 16.0;
        b[5][0] = 1.0 / 20.0;
        b[5][3] = 0.25;
        b[5][4] = 0.2;
        b[6][0] = -25.0 / 108.0;
        b[6][3] = 125.0 / 108.0;
        b[6][4] = -65.0 / 27.0;
        b[6][5] = 125.0 / 54.0;
        b[7][0] = 31.0 / 300.0;
        b[7][4] = 61.0 / 225.0;
        b[7][5] = -2.0 / 9.0;
        b[7][6] = 13.0 / 900.0;
        b[8][0] = 2.0;
        b[8][3] = -53.0 / 6.0;
        b[8][4] = 704.0 / 45.0;
        b[8][5] = -107.0 / 9.0;
        b[8][6] = 67.0 / 90.0;
        b[8][7] = 3.0;
        b[9][0] = -91.0 / 108.0;
        b[9][3] = 23.0 / 108.0;
        b[9][4] = -976.0 / 135.0;
        b[9][5] = 311.0 / 54.0;
        b[9][6] = -19.0 / 60.0;
        b[9][7] = 17.0 / 6.0;
        b[9][8] = -1.0 / 12.0;
        b[10][0] = 2383.0 / 4100.0;
        b[10][3] = -341.0 / 164.0;
        b[10][4] = 4496.0 / 1025.0;
        b[10][5] = -301.0 / 82.0;
        b[10][6] = 2133.0 / 4100.0;
        b[10][7] = 45.0 / 82.0;
        b[10][8] = 45.0 / 164.0;
        b[10][9] = 18.0 / 41.0;
        b[11][0] = 3.0 / 205.0;
        b[11][5] = -6.0 / 41.0;
        b[11][6] = -3.0 / 205.0;
        b[11][7] = -3.0 / 41.0;
        b[11][8] = 3.0 / 41.0;
        b[11][9] = 6.0 / 41.0;
        b[12][0] = -1777.0 / 4100.0;
        b[12][3] = -341.0 / 164.0;
        b[12][4] = 4496.0 / 1025.0;
        b[12][5] = -289.0 / 82.0;
        b[12][6] = 2193.0 / 4100.0;
        b[12][7] = 51.0 / 82.0;
        b[12][8] = 33.0 / 164.0;
        b[12][9] = 12.0 / 41.0;
        b[12][11] = 1.0;
    }
    private static final double[] c = {41.0 / 840.0, 0.0, 0.0, 0.0, 0.0, 34.0 / 105.0, 9.0 / 35.0,
        9.0 / 35.0, 9.0 / 280.0, 9.0 / 280.0, 41.0 / 840.0, 0.0, 0.0
    };
    private static final double[] chat = {0.0, 0.0, 0.0, 0.0, 0.0, 34.0 / 105.0, 9.0 / 35.0,
        9.0 / 35.0, 9.0 / 280.0, 9.0 / 280.0, 0.0, 41.0 / 840.0, 41.0 / 840.0
    };

    // Cash-Karp Runge-Kutta step
    protected void rkck(double[] y, double[] dydx, double x, double h,
            double[] yout, double[] yerr, double[] velOut, double[] velErr)
    {

        int n = y.length;

        double f[][] = new double[13][n];
        double f2[][] = new double[13][n];
        //double yt[][] = new double[13][n];

        //	   double y7th[] = new double[n];
        double ytmp[] = new double[n];
        double velTmp[] = new double[n];
        //double sum[] = new double[n];

        //	   System.out.println("step size = "+h);

        double xeval[] = new double[13];
        for (int i = 0; i < 13; i++)  // find times for function evals
        {
            xeval[i] = x + a[i] * h;
        }

        // build f matrix
        //	   f[0] = derivs.derivs(x, y);
        // K0 
        for (int i = 0; i < n; i++)
        {
            f[0][i] = dydx[i];
        }

        f2[0] = func.deriv(y, dydx, xeval[0]);

        // K1
        for (int i = 0; i < n; i++)
        {
            f[1][i] = dydx[i] + h * b[1][0] * f2[0][i];

            ytmp[i] = y[i] + h * b[1][0] * f[0][i];
            velTmp[i] = f[1][i];
        }
        f2[1] = func.deriv(ytmp, velTmp, xeval[1]);


        // K2
        for (int i = 0; i < n; i++)
        {
            f[2][i] = dydx[i] + h * (b[2][0] * f2[0][i] + b[2][1] * f2[1][i]);

            ytmp[i] = y[i] + h * (b[2][0] * f[0][i] + b[2][1] * f[1][i]);
            velTmp[i] = f[2][i];
        }
        f2[2] = func.deriv(ytmp, velTmp, xeval[2]);

        // K3
        for (int i = 0; i < n; i++)
        {
            f[3][i] = dydx[i] + h * (b[3][0] * f2[0][i] + b[3][2] * f2[2][i]);

            ytmp[i] = y[i] + h * (b[3][0] * f[0][i] + b[3][2] * f[2][i]);
            velTmp[i] = f[3][i];
        }
        f2[3] = func.deriv(ytmp, velTmp, xeval[3]);

        // K4
        for (int i = 0; i < n; i++)
        {
            f[4][i] = dydx[i] + h * (b[4][0] * f2[0][i] + b[4][2] * f2[2][i] + b[4][3] * f2[3][i]);

            ytmp[i] = y[i] + h * (b[4][0] * f[0][i] + b[4][2] * f[2][i] + b[4][3] * f[3][i]);
            velTmp[i] = f[4][i];
        }
        f2[4] = func.deriv(ytmp, velTmp, xeval[4]);

        // K5     
        for (int i = 0; i < n; i++)
        {
            f[5][i] = dydx[i] + h * (b[5][0] * f2[0][i] + b[5][3] * f2[3][i] + b[5][4] * f2[4][i]);

            ytmp[i] = y[i] + h * (b[5][0] * f[0][i] + b[5][3] * f[3][i] + b[5][4] * f[4][i]);
            velTmp[i] = f[5][i];
        }
        f2[5] = func.deriv(ytmp, velTmp, xeval[5]);

        // K6
        for (int i = 0; i < n; i++)
        {
            f[6][i] = dydx[i] + h * (b[6][0] * f2[0][i] + b[6][3] * f2[3][i] + b[6][4] * f2[4][i] + b[6][5] * f2[5][i]);

            ytmp[i] = y[i] + h * (b[6][0] * f[0][i] + b[6][3] * f[3][i] + b[6][4] * f[4][i] + b[6][5] * f[5][i]);
            velTmp[i] = f[6][i];
        }
        f2[6] = func.deriv(ytmp, velTmp, xeval[6]);

        // K7
        for (int i = 0; i < n; i++)
        {
            f[7][i] = dydx[i] + h * (b[7][0] * f2[0][i] + b[7][4] * f2[4][i] + b[7][5] * f2[5][i] + b[7][6] * f2[6][i]);

            ytmp[i] = y[i] + h * (b[7][0] * f[0][i] + b[7][4] * f[4][i] + b[7][5] * f[5][i] + b[7][6] * f[6][i]);
            velTmp[i] = f[7][i];
        }
        f2[7] = func.deriv(ytmp, velTmp, xeval[7]);

        // K8
        for (int i = 0; i < n; i++)
        {
            f[8][i] = dydx[i] + h * (b[8][0] * f2[0][i] + b[8][3] * f2[3][i] + b[8][4] * f2[4][i] + b[8][5] * f2[5][i] + b[8][6] * f2[6][i] + b[8][7] * f2[7][i]);

            ytmp[i] = y[i] + h * (b[8][0] * f[0][i] + b[8][3] * f[3][i] + b[8][4] * f[4][i] + b[8][5] * f[5][i] + b[8][6] * f[6][i] + b[8][7] * f[7][i]);
            velTmp[i] = f[8][i];
        }
        f2[8] = func.deriv(ytmp, velTmp, xeval[8]);

        // K9
        for (int i = 0; i < n; i++)
        {
            f[9][i] = dydx[i] + h * (b[9][0] * f2[0][i] + b[9][3] * f2[3][i] + b[9][4] * f2[4][i] + b[9][5] * f2[5][i] + b[9][6] * f2[6][i] + b[9][7] * f2[7][i] + b[9][8] * f2[8][i]);

            ytmp[i] = y[i] + h * (b[9][0] * f[0][i] + b[9][3] * f[3][i] + b[9][4] * f[4][i] + b[9][5] * f[5][i] + b[9][6] * f[6][i] + b[9][7] * f[7][i] + b[9][8] * f[8][i]);
            velTmp[i] = f[9][i];
        }
        f2[9] = func.deriv(ytmp, velTmp, xeval[9]);

        // K10
        for (int i = 0; i < n; i++)
        {
            f[10][i] = dydx[i] + h * (b[10][0] * f2[0][i] + b[10][3] * f2[3][i] + b[10][4] * f2[4][i] + b[10][5] * f2[5][i] + b[10][6] * f2[6][i] + b[10][7] * f2[7][i] + b[10][8] * f2[8][i] + b[10][9] * f2[9][i]);

            ytmp[i] = y[i] + h * (b[10][0] * f[0][i] + b[10][3] * f[3][i] + b[10][4] * f[4][i] + b[10][5] * f[5][i] + b[10][6] * f[6][i] + b[10][7] * f[7][i] + b[10][8] * f[8][i] + b[10][9] * f[9][i]);
            velTmp[i] = f[10][i];
        }
        f2[10] = func.deriv(ytmp, velTmp, xeval[10]);

        // K11
        for (int i = 0; i < n; i++)
        {
            f[11][i] = dydx[i] + h * (b[11][0] * f2[0][i] + b[11][5] * f2[5][i] + b[11][6] * f2[6][i] + b[11][7] * f2[7][i] + b[11][8] * f2[8][i] + b[11][9] * f2[9][i]);

            ytmp[i] = y[i] + h * (b[11][0] * f[0][i] + b[11][5] * f[5][i] + b[11][6] * f[6][i] + b[11][7] * f[7][i] + b[11][8] * f[8][i] + b[11][9] * f[9][i]);
            velTmp[i] = f[11][i];
        }
        f2[11] = func.deriv(ytmp, velTmp, xeval[11]);

        // K12
        for (int i = 0; i < n; i++)
        {
            f[12][i] = dydx[i] + h * (b[12][0] * f2[0][i] + b[12][3] * f2[3][i] + b[12][4] * f2[4][i] + b[12][5] * f2[5][i] + b[12][6] * f2[6][i] + b[12][7] * f2[7][i] + b[12][8] * f2[8][i] + b[12][9] * f2[9][i] + f2[11][i]);

            ytmp[i] = y[i] + h * (b[12][0] * f[0][i] + b[12][3] * f[3][i] + b[12][4] * f[4][i] + b[12][5] * f[5][i] + b[12][6] * f[6][i] + b[12][7] * f[7][i] + b[12][8] * f[8][i] + b[12][9] * f[9][i] + f[11][i]);
            velTmp[i] = f[12][i];
        }
        f2[12] = func.deriv(ytmp, velTmp, xeval[12]);

        // construct solutions
        // yout is the 8th order solution

        for (int i = 0; i < n; i++)
        {
            //		  y7th[i] = y[i] + h*(c[0]*f[0][i] +c[5]*f[5][i] + c[6]*f[6][i] + c[7]*f[7][i] + c[8]*f[8][i] + c[9]*f[9][i] + c[10]*f[10][i]);
            yout[i] = y[i] + h * (chat[5] * f[5][i] + chat[6] * f[6][i] + chat[7] * f[7][i] + chat[8] * f[8][i] + chat[9] * f[9][i] + chat[11] * f[11][i] + chat[12] * f[12][i]);
            yerr[i] = h * c[0] * (f[11][i] + f[12][i] - f[0][i] - f[10][i]);

            velOut[i] = dydx[i] + h * (chat[5] * f2[5][i] + chat[6] * f2[6][i] + chat[7] * f2[7][i] + chat[8] * f2[8][i] + chat[9] * f2[9][i] + chat[11] * f2[11][i] + chat[12] * f2[12][i]);
            velErr[i] = h * c[0] * (f2[11][i] + f2[12][i] - f2[0][i] - f2[10][i]);
        }
    } // rkck

    public void print(double t, double[] y)
    {
    // do nothing
    }

    private void error(String msg)
    {
        System.err.println("ERROR RungeKuttaFehlberg78: " + msg);
    }

    //  Get Stats -----------------------------
    public int getNumGoodSteps()
    {
        return nok[0];
    }

    public int getNumBadSteps()
    {
        return nbad[0];
    }

    public int getNumSteps()
    {
        return nok[0] + nbad[0];
    }

    public double getStepSize()
    {
        return currentStepSize;
    }
    
    /**
     * Add a stopping condition to this propogator
     * @param sc stopping condition.
     */
    public void addStoppingCondition(StoppingCondition sc)
    {
        stopConditionsVec.add(sc);
    }
    
}

