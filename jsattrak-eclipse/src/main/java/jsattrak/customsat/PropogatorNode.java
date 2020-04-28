/*
 * Propogator Node for Custom Sat Class Mission Designer
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
 */

package jsattrak.customsat;

import java.awt.Toolkit;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import jsattrak.customsat.gui.PropogatorPanel;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.StateVector;
import name.gano.astro.AstroConst;
import name.gano.astro.Atmosphere;
import name.gano.astro.GeoFunctions;
import name.gano.astro.GravityField;
import name.gano.astro.Kepler;
import name.gano.astro.MathUtils;
import name.gano.astro.bodies.Moon;
import name.gano.astro.bodies.Sun;
import name.gano.astro.coordinates.CoordinateConversion;
import name.gano.astro.propogators.solvers.ApsisStopCond;
import name.gano.astro.propogators.solvers.OrbitProblem;
import name.gano.astro.propogators.solvers.RungeKutta4;
import name.gano.astro.propogators.solvers.RungeKutta78;
import name.gano.astro.propogators.solvers.StoppingCondition;
import name.gano.astro.time.Time;
import name.gano.swingx.treetable.CustomTreeTableNode;

/**
 *
 * @author sgano
 */
public class PropogatorNode extends CustomTreeTableNode implements OrbitProblem
{  
    // static int to determin which propogator to use
    public static final int HPROP4 = 0;
    public static final int HPROP8 = 1;
    public static final int HPROP78 = 2;
    
    // which prop to use 
    private int propogator = PropogatorNode.HPROP4;
    
    // Hprop settings
    private int n_max = 20;  // degree
    private int m_max = 20;  // order
    private boolean includeLunarPert = true;
    private boolean includeSunPert = true;
    private boolean includeSolRadPress = true;
    private boolean includeAtmosDrag = true;
    private double mass = 1000.0;  // [kg] mass of spacecraft
    private double area = 5.0;     // [m^2]  Cross-section Area
    private double CR = 1.3;     // Solar radiation pressure coefficient
    private double CD = 2.3;     // spacecraft drag coefficient
    private double stepSize = 60.0; // seconds  (ini step for Hprop 7-8)
    // Hprop 7-8 unique
    private double minStepSize = 1.0; // 1 second
    private double maxStepSize = 600.0; //10 minutes
    private double relAccuracy = 1.00e-012; 
    
    private double popogateTimeLen = 86400; // in seconds
    
    // stopping conditions used
    private boolean stopOnApogee = false;
    private boolean stopOnPerigee = false;
    
    // private variables used internally only
    private double JD_TT0; // JD_TT at initial time (Julian Date)
    Vector<StateVector> ephemeris;
    
    // USED FOR GOAL CALCULATIONS
    StateVector lastStateVector = null; // last state -- to calculate goal properties
    
    // variables that can be set data ----------
    String[] varNames = new String[]{"Propogation Time [s]"};
    // -----------------------------------------
    
    // parameters that can be used as GOALS ---
    String[] goalNames = new String[]
    {
        "X Position (J2000) [m]",  // element 0
        "Y Position (J2000) [m]",
        "Z Position (J2000) [m]",
        "X Velocity (J2000) [m/s]",
        "Y Velocity (J2000) [m/s]",
        "Z Velocity (J2000) [m/s]",
        "Semimajor axis (Osculating) [m]",
        "Eccentricity (Osculating)",
        "Inclination (Osculating) [deg]",
        "Longitude of the ascending node (Osculating) [deg]",
        "Argument of pericenter (Osculating) [deg]",
        "Mean anomaly (Osculating) [deg]",
        "Orbital Radius [m]",
        "Derivative of Orbital Radius [m/s]", // can be used to find perigee / apogee
        "Latitude [deg]",
        "Longitude [deg]",
        "Altitude [m]",  // element 16
    };
    // ========================================
    
    
    public PropogatorNode(CustomTreeTableNode parentNode)
    {
        super(new String[] {"Propogate","",""}); // initialize node, default values
        // set icon for this type
        setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/customSatIcons/prop.png")) ) );
        //set Node Type
        setNodeType("Propogator");
        
        // add this node to parent - last thing
        if( parentNode != null)
            parentNode.add(this);
        
    } // PropogatorNode
    
    
     // propogate sat (using starting point as last ephemeris pt)
    public void execute(Vector<StateVector> ephemeris)
    {
        this.ephemeris = ephemeris;
        
         // dummy but should do something based on input ephemeris
        //System.out.println("Executing : " + getValueAt(0) );
        
        // last state before propogation
        StateVector lastState = ephemeris.lastElement();
        
        // save initial time of the node ( TT)
        this.setStartTTjulDate(lastState.state[0]);
        
        double[] pos = new double[] {lastState.state[1],lastState.state[2],lastState.state[3]};
        double[] vel = new double[] {lastState.state[4],lastState.state[5],lastState.state[6]};
        
        boolean propSuccess = false;
        
        // time parameters that are shared
        JD_TT0 = lastState.state[0]; // last time !!!! IS THIS TT 
        double dt = stepSize; // in seconds

        int nSteps = (int) Math.ceil(popogateTimeLen/dt); // number of steps to integrate
        
        // run correct propogator
        if(propogator == PropogatorNode.HPROP4)
        {
            
            // use seconds as integration time (start at 0.0)
            RungeKutta4 integrator = new RungeKutta4(0.0, dt, pos, vel, nSteps, this);
            
            // Add stopping conditions
            if(stopOnApogee)
            {
                StoppingCondition sc = new ApsisStopCond(ApsisStopCond.APOAPSIS,ephemeris);
                integrator.addStoppingCondition(sc);
            }
            if(stopOnPerigee)
            {
                StoppingCondition sc = new ApsisStopCond(ApsisStopCond.PERIAPSIS,ephemeris);
                integrator.addStoppingCondition(sc);
            }
            
            long ms = integrator.solve();

            //	System.out.println("RK4 Solver took: " + ms + " ms");
            //guiApp.addMessagetoLog("RK4 Solver took: " + ms / 1000.0 + " sec (" + name + ")");

            // finish
            propSuccess = true;
        } // RK 4
        else if(propogator == PropogatorNode.HPROP8)
        {
            
            
            // 8th order test
            boolean adaptive = false; // not adaptive
            double iniStep = dt; // real value
            double relErrorTol = relAccuracy;  // hmm not set by user for this method??  set in RK7-8??
            
            RungeKutta78 text = new RungeKutta78(0.0, popogateTimeLen, pos, vel, this, minStepSize, maxStepSize, iniStep, relErrorTol, adaptive);
   
            // Add stopping conditions
            if(stopOnApogee)
            {
                StoppingCondition sc = new ApsisStopCond(ApsisStopCond.APOAPSIS,ephemeris);
                text.addStoppingCondition(sc);
            }
            if(stopOnPerigee)
            {
                StoppingCondition sc = new ApsisStopCond(ApsisStopCond.PERIAPSIS,ephemeris);
                text.addStoppingCondition(sc);
            }
            
            long simt = text.solve();
            
            double minStep = text.getMinStep();
            double maxStep = text.getMaxStep();
            int ns = text.getNumSteps();
            
            //guiApp.addMessagetoLog("RK8 Solver took: " + simt / 1000.0 + " sec (" + name + ")");

            propSuccess = true;
        } // RK8
        else if(propogator == PropogatorNode.HPROP78)
        {
            // 8th order test
            boolean adaptive = true; // is adaptive

            // get parameter values
            double iniStep = dt; // real value
            double relErrorTol = relAccuracy;
            
            RungeKutta78 text = new RungeKutta78(0.0, popogateTimeLen, pos, vel, this, minStepSize, maxStepSize, iniStep, relErrorTol, adaptive);
            
             // Add stopping conditions
            if(stopOnApogee)
            {
                StoppingCondition sc = new ApsisStopCond(ApsisStopCond.APOAPSIS,ephemeris);
                text.addStoppingCondition(sc);
            }
            if(stopOnPerigee)
            {
                StoppingCondition sc = new ApsisStopCond(ApsisStopCond.PERIAPSIS,ephemeris);
                text.addStoppingCondition(sc);
            }
            
            long simt = text.solve();
            
            double minStep = text.getMinStep();
            double maxStep = text.getMaxStep();
            int ns = text.getNumSteps();
            
            //guiApp.addMessagetoLog("RK7-8 Solver took: : " + simt / 1000.0 + " sec (" + name + "), Number of Steps: " + ns + ", Min/Max Step Sizes: " + minStep + "/" + maxStep);

            propSuccess = true;
            
        } // RK78
        
        
        // copy final ephemeris state:
        lastStateVector = ephemeris.lastElement();
        
        // copy internal ephemeris to the external ephemeris, making conversion from TT to UT??
        // nope Custom sat internal time is TT not UTC
        
    }// execute
    
    
    // passes in main app to add the internal frame to
    public void displaySettings(JSatTrak app)
    {
        
        String windowName = "" + getValueAt(0);
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        // show satellite browser window
        PropogatorPanel gsBrowser = new PropogatorPanel(this,iframe); // non-modal version       
        
        iframe.setContentPane( gsBrowser );
        iframe.setSize(415+20,386+115); // w,h
        iframe.setLocation(5,5);
        
        app.addInternalFrame(iframe);
          
    } // displaySettings

    
    
    // ==========================================
    // Get-Set Methods ==========================
    // ==========================================
    
    public int getPropogator()
    {
        return propogator;
    }

    public void setPropogator(int propogator)
    {
        this.propogator = propogator;
    }

    public int getN_max()
    {
        return n_max;
    }

    public void setN_max(int n_max)
    {
        this.n_max = n_max;
    }

    public int getM_max()
    {
        return m_max;
    }

    public void setM_max(int m_max)
    {
        this.m_max = m_max;
    }

    public boolean isIncludeLunarPert()
    {
        return includeLunarPert;
    }

    public void setIncludeLunarPert(boolean includeLunarPert)
    {
        this.includeLunarPert = includeLunarPert;
    }

    public boolean isIncludeSunPert()
    {
        return includeSunPert;
    }

    public void setIncludeSunPert(boolean includeSunPert)
    {
        this.includeSunPert = includeSunPert;
    }

    public boolean isIncludeSolRadPress()
    {
        return includeSolRadPress;
    }

    public void setIncludeSolRadPress(boolean includeSolRadPress)
    {
        this.includeSolRadPress = includeSolRadPress;
    }

    public boolean isIncludeAtmosDrag()
    {
        return includeAtmosDrag;
    }

    public void setIncludeAtmosDrag(boolean includeAtmosDrag)
    {
        this.includeAtmosDrag = includeAtmosDrag;
    }

    public double getMass()
    {
        return mass;
    }

    public void setMass(double mass)
    {
        this.mass = mass;
    }

    public double getArea()
    {
        return area;
    }

    public void setArea(double area)
    {
        this.area = area;
    }

    public double getCR()
    {
        return CR;
    }

    public void setCR(double CR)
    {
        this.CR = CR;
    }

    public double getCD()
    {
        return CD;
    }

    public void setCD(double CD)
    {
        this.CD = CD;
    }

    public double getStepSize()
    {
        return stepSize;
    }

    public void setStepSize(double stepSize)
    {
        this.stepSize = stepSize;
    }

    public double getMinStepSize()
    {
        return minStepSize;
    }

    public void setMinStepSize(double minStepSize)
    {
        this.minStepSize = minStepSize;
    }

    public double getMaxStepSize()
    {
        return maxStepSize;
    }

    public void setMaxStepSize(double maxStepSize)
    {
        this.maxStepSize = maxStepSize;
    }

    public double getRelAccuracy()
    {
        return relAccuracy;
    }

    public void setRelAccuracy(double relAccuracy)
    {
        this.relAccuracy = relAccuracy;
    }
    
    public double getPopogateTimeLen()
    {
        return popogateTimeLen;
    }

    public void setPopogateTimeLen(double popogateTimeLen)
    {
        this.popogateTimeLen = popogateTimeLen;
    }

    // ====================================================
    // =======  ORBIT Problem Functions ===================
    // ====================================================
    /***************************************************************************
     * Equations of Motion (accelerations) for 2-Body Problem + perturbations
     *************************************************************************
     * @param var
     * @param vel
     * @param t
     * @return 
     */
    public double[] deriv(double[] var, double[] vel, double t)
    {
        double[] acc = new double[3];
        double[][] E = new double[3][3];
        double[][] T = new double[3][3];

        // CAREFUL on time, should use TT then convert to UTC later or something??
        // otherwise UTC time is not uniform length??

        // prepare - Transformation matrix to body-fixed system
        //double Mjd_TT = Mjd0_TT + t / 86400.0; // mjd_tt = start epic
        double Mjd_TT = (JD_TT0 + t/86400.0) - AstroConst.JDminusMJD; // Convert sim time to JD to MJD
        
        // calculate current UT time from TT
        double Mjd_UT1 = Mjd_TT - Time.deltaT(Mjd_TT); //

        // careful if Mjd_TT > J2000.0 - should be take care of in PrecMatrix_Equ_Mjd
        // good use of PrecMatrix_Equ_Mjd - followed by nutation to get TOD
        T = MathUtils.mult(CoordinateConversion.NutMatrix(Mjd_TT), CoordinateConversion.PrecMatrix_Equ_Mjd(AstroConst.MJD_J2000, Mjd_TT));
        //E = CoordinateConversion.GHAMatrix(Mjd_UT1);
        E = MathUtils.mult(CoordinateConversion.GHAMatrix(Mjd_UT1), T);

        // Acceleration due to harmonic gravity field
        acc = GravityField.AccelHarmonic(var, E, AstroConst.GM_Earth, AstroConst.R_Earth, AstroConst.CS, n_max, m_max);

        // Luni-solar perturbations 
        double[] r_Sun = new double[3];
        if (includeSunPert || includeSolRadPress)
        {
            r_Sun = Sun.calculateSunPositionLowTT(Mjd_TT);
        }
        
        if (includeSunPert)
        {
            acc = MathUtils.add(acc, GravityField.AccelPointMass(var, r_Sun, AstroConst.GM_Sun));
        }

        double[] r_Moon = new double[3];
        if (includeLunarPert)
        {
            r_Moon = Moon.MoonPosition(Mjd_TT);
            acc = MathUtils.add(acc, GravityField.AccelPointMass(var, r_Moon, AstroConst.GM_Moon));
        }

        // Solar radiation pressure
        if (includeSolRadPress)
        {
            acc = MathUtils.add(acc, MathUtils.scale(Sun.AccelSolrad(var, r_Sun, area, mass, CR, AstroConst.P_Sol, AstroConst.AU), Sun.Illumination(var, r_Sun)));
        }

        // Atmospheric drag [uses, altitude]
        if (includeAtmosDrag)
        {
            acc = MathUtils.add(acc, Atmosphere.AccelDrag(Mjd_TT, var, vel, T, area, mass, CD));
        }

        return acc;

    } // deriv
    
    // verbose - debug
    private boolean verbose = false;
    public void setVerbose(boolean verbose){ this.verbose=verbose;}
    public boolean getVerbose(){ return verbose;}

    //public Vector getEphemerisVector();
    public void addState2Ephemeris(StateVector state)
    {
        // add new points to ephemeris converting back to TT (and skipping 0)
        if(! (state.state[0] == 0.0) )
        {
            state.state[0] = (JD_TT0 + state.state[0]/86400.0);
            ephemeris.add(state);
        }
    }
    
     // meant to be over ridden if there are any input vars
    public double getVar(int varInt)
    {
        double var = 0;
        
        switch(varInt)
        {
            case 0:
                var = popogateTimeLen;
                break;
            default:
                var = 0;
                break;
        }
        
        return var;
    }

    // meant to be over ridden if there are any input vars
    public void setVar(int varInt, double val)
    {
        switch(varInt)
        {
            case 0:
                popogateTimeLen = val;
                break;
            default:
                break;
        }
    }

    // meant to be over ridden if there are any input vars
    public Vector<InputVariable> getInputVarVector()
    {
        Vector<InputVariable> varVec = new Vector<InputVariable>(1);
        
        InputVariable inVar = new InputVariable(this, 0, varNames[0], popogateTimeLen);
        varVec.add(inVar);
        
        return varVec;
    }
    
    // meant to be over ridden if there are any input vars
    public Vector<GoalParameter> getGoalParamVector()
    {
        Vector<GoalParameter> varVec = new Vector<GoalParameter>(17);
        
        for (int i = 0; i < goalNames.length; i++)
        {
            GoalParameter inVar = new GoalParameter(this, i, goalNames[i], getGoal(i)); // hmm, need to put current value here if possible
            varVec.add(inVar);
        }
        
        return varVec;
    }

    // meant to be over ridden if there are any input vars
    public Double getGoal(int goalInt)
    {
        Double val = null;
        
        if(lastStateVector != null)
        {
            // calculate goals value
            switch(goalInt)
            {
                case 0: // X Position (J2000) [m]
                    val = lastStateVector.state[1];
                    break;
                case 1: // Y Position (J2000) [m]
                    val = lastStateVector.state[2];
                    break;
                case 2: // Z Position (J2000) [m]
                    val = lastStateVector.state[3];
                    break;
                case 3: // X Velocity (J2000) [m/s]
                    val = lastStateVector.state[4];
                    break;
                case 4: // Y Velocity (J2000) [m/s]
                    val = lastStateVector.state[5];
                    break;
                case 5: // Z Velocity (J2000) [m/s]
                    val = lastStateVector.state[6];
                    break;
                case 6: // Semimajor axis (Osculating) [m]
                    val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[0];
                    break;
                case 7: // Eccentricity (Osculating)
                    val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[1];
                    break;
                case 8: // Inclination (Osculating) [deg]
                    val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[2] * 180.0/Math.PI;
                    break;
                case 9: // Longitude of the ascending node (Osculating) [deg]
                    val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[3] * 180.0/Math.PI;
                    break;
                case 10: // Argument of pericenter (Osculating) [deg]
                    val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[4] * 180.0/Math.PI;
                    break;
                case 11: // Mean anomaly (Osculating) [deg]
                    val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[5] * 180.0/Math.PI;
                    break;
                case 12: // Orbital Radius [m]
                    val = Math.sqrt(Math.pow(lastStateVector.state[1], 2.0)+Math.pow(lastStateVector.state[2], 2.0)+Math.pow(lastStateVector.state[3], 2.0));
                    break;
                case 13: // Derivative of Orbital Radius [m/s]
                    // rdot = v dot R
                    double[] R = new double[] {lastStateVector.state[1],lastStateVector.state[2],lastStateVector.state[3]};
                    double normR = MathUtils.norm(R);
                    R = MathUtils.scale(R, 1.0/normR); // unit vector
                    
                    double[] v = new double[] {lastStateVector.state[4],lastStateVector.state[5],lastStateVector.state[6]};
                    double rDot = MathUtils.dot(v, R);
                    val = rDot;
                    break;
                case 14: // Latitude [deg]
                    // get current j2k pos
                    double[] currentJ2kPos = new double[]{lastStateVector.state[1], lastStateVector.state[2], lastStateVector.state[3]};
                    // mod pos -needs to be TEME of date
                    //double[] modPos = CoordinateConversion.EquatorialEquinoxFromJ2K(lastStateVector.state[0] - AstroConst.JDminusMJD, currentJ2kPos)
                    // teme pos
                    double[] temePos = CoordinateConversion.J2000toTEME(lastStateVector.state[0] - AstroConst.JDminusMJD, currentJ2kPos);
                    // lla  (submit time in UTC)
                    double deltaTT2UTC = Time.deltaT(lastStateVector.state[0] - AstroConst.JDminusMJD); // = TT - UTC
                    double[] lla = GeoFunctions.GeodeticLLA(temePos, lastStateVector.state[0] - AstroConst.JDminusMJD - deltaTT2UTC); // tt-UTC = deltaTT2UTC

                    val = lla[0] * 180.0 / Math.PI;
                    break;
                case 15: // Longitude [deg]
                    // get current j2k pos
                    currentJ2kPos = new double[]{lastStateVector.state[1], lastStateVector.state[2], lastStateVector.state[3]};
                    // mod pos - needs to be TEME of date
                    //modPos = CoordinateConversion.EquatorialEquinoxFromJ2K(lastStateVector.state[0] - AstroConst.JDminusMJD, currentJ2kPos)
                    // teme pos
                    temePos = CoordinateConversion.J2000toTEME(lastStateVector.state[0] - AstroConst.JDminusMJD, currentJ2kPos);
                    // lla  (submit time in UTC)
                    deltaTT2UTC = Time.deltaT(lastStateVector.state[0] - AstroConst.JDminusMJD); // = TT - UTC
                    lla = GeoFunctions.GeodeticLLA(temePos, lastStateVector.state[0] - AstroConst.JDminusMJD - deltaTT2UTC); // tt-UTC = deltaTT2UTC

                    val = lla[1] * 180.0 / Math.PI;
                    break;
                case 16: // Altitude [m]
                    // get current j2k pos
                    currentJ2kPos = new double[]{lastStateVector.state[1], lastStateVector.state[2], lastStateVector.state[3]};
                    // mod pos - needs to be TEME of date
                    //modPos = CoordinateConversion.EquatorialEquinoxFromJ2K(lastStateVector.state[0] - AstroConst.JDminusMJD, currentJ2kPos)
                    // teme pos
                    temePos = CoordinateConversion.J2000toTEME(lastStateVector.state[0] - AstroConst.JDminusMJD, currentJ2kPos);
                    // lla  (submit time in UTC)
                    deltaTT2UTC = Time.deltaT(lastStateVector.state[0] - AstroConst.JDminusMJD); // = TT - UTC
                    lla = GeoFunctions.GeodeticLLA(temePos, lastStateVector.state[0] - AstroConst.JDminusMJD - deltaTT2UTC); // tt-UTC = deltaTT2UTC

                    val = lla[2];
                    break;   
                    
                
            } // switch
        } // last state not null
        
        return val;
    } // getGoal   

    public // in seconds
    // stopping conditions used
    boolean isStopOnApogee()
    {
        return stopOnApogee;
    }

    public void setStopOnApogee(boolean stopOnApogee)
    {
        this.stopOnApogee = stopOnApogee;
    }

    public boolean isStopOnPerigee()
    {
        return stopOnPerigee;
    }

    public void setStopOnPerigee(boolean stopOnPerigee)
    {
        this.stopOnPerigee = stopOnPerigee;
    }

    
}
