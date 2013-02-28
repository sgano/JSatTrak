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
 */
package name.gano.astro.propogators.solvers;

import java.text.DecimalFormat;
import java.util.Vector;
import jsattrak.utilities.StateVector;

/**
 *
 * @author Shawn
 */
public class RungeKutta4
{

    private double t;
    private double dt;
    private double[] var;
    private double[] vel;
    private OrbitProblem func;
    private int nSteps;
    // output dataformat
    private DecimalFormat d12 = new DecimalFormat("0.00000000000E0"); // display format
    // vector of stopping conditions
    Vector<StoppingCondition> stopConditionsVec = new Vector<StoppingCondition>();

    // create a constructor and a solver build in to this class
    // t0 should be TT (not UTC) (Mjd_TT)
    public RungeKutta4(double t0, double dt0, double[] var0, double[] vel0, int nSteps, OrbitProblem func)
    {

        // initialize integrator parameters
        t = t0;
        dt = dt0;

        if (var0.length != vel0.length)
        {
            System.out.println("ERROR [RK4]: X and dX not the same length");
            return;
        }

        var = new double[var0.length];
        vel = new double[vel0.length];
        for (int i = 0; i < var0.length; i++)
        {
            var[i] = var0[i];
            vel[i] = vel0[i];
        }

        this.nSteps = nSteps;
        this.func = func;

    } // constructor

    // solve returns solve time in milliseconds
    public long solve()
    {
        double t0 = t; // store inital time

        long startTime = System.currentTimeMillis();

        // initialize stopping conditions
        for (StoppingCondition sc : stopConditionsVec)
        {
            sc.iniStoppingCondition(t, var, vel);
        }


        checkOutputOptions();

        TIME_STEPPING:
        for (int n = 0; n < nSteps; n++)
        {
            step();
            t = t0 + dt * (n + 1);

            checkOutputOptions();

            // check stopping conditions - if met - fix last epehermis point and break
            for (StoppingCondition sc : stopConditionsVec)
            {
                if (sc.checkStoppingCondition(t, var, vel))
                {
                    // if stopped automatically fixes state all we need to to is break the propogation
                    break TIME_STEPPING;
                }
            }

        } // integration steps


        long endTime = System.currentTimeMillis();

        // only for GUI
        //sssApp.setProgressBar(0); // reset bar

        return endTime - startTime;
    }

    public void checkOutputOptions()
    {
        // verbosity to the console
        if (func.getVerbose())
        {
            System.out.println(d12.format(t) + " " + d12.format(var[0]) + " " + d12.format(var[1]) + " " + d12.format(var[2]) + " " + d12.format(vel[0]) + " " + d12.format(vel[1]) + " " + d12.format(vel[2]));
        }


        // Do we need to save the ephemeris to memory?
//		if(func.getStoreEphemeris())
//		{
        func.addState2Ephemeris(new StateVector(t, var[0], var[1], var[2], vel[0], vel[1], vel[2]));
//		} // store ephemeris



    }// output params
    /**
     * Calculated a step of the integration of an ODE with 4th order RK.
     * 
     * @param t
     *            independent variable
     * @param dt
     *            step in the independent variable
     * @param var
     *            array holding the dependent variable
     * @param vel
     *            array holding the first derivative
     * @param func
     *            Object that contains the equations of motion            
     * 
     */
    public void step()
    {
        double[] k1_var = new double[var.length];
        double[] k1_vel = new double[var.length];
        double[] k2_var = new double[var.length];
        double[] k2_vel = new double[var.length];
        double[] k3_var = new double[var.length];
        double[] k3_vel = new double[var.length];
        double[] k4_var = new double[var.length];
        double[] k4_vel = new double[var.length];

        double[] tmp1 = new double[var.length];
        double[] tmp2 = new double[var.length];

        // K1
        for (int i = 0; i < var.length; i++)
        {
            k1_var[i] = vel[i] * dt;
        }

        k1_vel = func.deriv(var, vel, t);
        for (int i = 0; i < var.length; i++)
        {
            k1_vel[i] = k1_vel[i] * dt;
        }

        // K2
        for (int i = 0; i < var.length; i++)
        {
            k2_var[i] = (vel[i] + 0.5 * k1_vel[i]) * dt;

            tmp1[i] = var[i] + 0.5 * k1_var[i];
            tmp2[i] = vel[i] + 0.5 * k1_vel[i];
        }

        k2_vel = func.deriv(tmp1, tmp2, t + 0.5 * dt);
        for (int i = 0; i < var.length; i++)
        {
            k2_vel[i] = k2_vel[i] * dt;
        }

        // K3
        for (int i = 0; i < var.length; i++)
        {
            k3_var[i] = (vel[i] + 0.5 * k2_vel[i]) * dt;

            tmp1[i] = var[i] + 0.5 * k2_var[i];
            tmp2[i] = vel[i] + 0.5 * k2_vel[i];
        }

        k3_vel = func.deriv(tmp1, tmp2, t + 0.5 * dt);
        for (int i = 0; i < var.length; i++)
        {
            k3_vel[i] = k3_vel[i] * dt;
        }

        // K4
        for (int i = 0; i < var.length; i++)
        {
            k4_var[i] = (vel[i] + k3_vel[i]) * dt;

            tmp1[i] = var[i] + k3_var[i];
            tmp2[i] = vel[i] + k3_vel[i];
        }

        k4_vel = func.deriv(tmp1, tmp2, t + dt);
        for (int i = 0; i < var.length; i++)
        {
            k4_vel[i] = k4_vel[i] * dt;
        }

        // take next step
        for (int i = 0; i < var.length; i++)
        {
            var[i] = var[i] + (k1_var[i] + 2.0 * k2_var[i] + 2.0 * k3_var[i] + k4_var[i]) / 6.0;
            vel[i] = vel[i] + (k1_vel[i] + 2.0 * k2_vel[i] + 2.0 * k3_vel[i] + k4_vel[i]) / 6.0;
        }

        // next time step
        t = t + dt;

    } // step

    /**
     * Add a stopping condition to this propogator
     * @param sc stopping condition.
     */
    public void addStoppingCondition(StoppingCondition sc)
    {
        stopConditionsVec.add(sc);
    }
} // RungeKutta4th

