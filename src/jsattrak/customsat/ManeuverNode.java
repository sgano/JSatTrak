/*
 * Node for Custom Sat Class Mission Designer
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
import jsattrak.customsat.gui.ManeuverPanel;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.StateVector;
import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.Kepler;
import name.gano.astro.MathUtils;
import name.gano.astro.coordinates.CoordinateConversion;
import name.gano.astro.time.Time;
import name.gano.swingx.treetable.CustomTreeTableNode;

/**
 *
 * @author sgano
 */
public class ManeuverNode  extends CustomTreeTableNode
{  
        
    private double[] vncThrustVector = new double[3];
    
    public static final int VTHRUST = 0;
    public static final int NTHRUST = 0;
    public static final int CTHRUST = 0;
    
    // variables that can be set data ----------
    String[] varNames = new String[] {"V-Thrust [m/s]","N-Thrust [m/s]","C-Thrust [m/s]"};
    
    // -----------------------------------------
    
     // USED FOR GOAL CALCULATIONS
    StateVector lastStateVector = null; // last state -- to calculate goal properties
    
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
    
    public ManeuverNode(CustomTreeTableNode parentNode)
    {
        super(new String[] {"Burn","",""}); // initialize node, default values
        // set icon for this type
        setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/customSatIcons/burn.png")) ) );
        //set Node Type
        setNodeType("Maneuver");
        
        // add this node to parent - last thing
        if( parentNode != null)
            parentNode.add(this);
    }
    
    
     // meant to be overridden by implementing classes
    @Override
    public void execute(Vector<StateVector> ephemeris)
    {
         // dummy but should do something based on input ephemeris
        //System.out.println("Executing : " + getValueAt(0) );
        
        // get last stat vector (we are going to change it - impulse burn)
        StateVector lastState = ephemeris.lastElement();
        
        // for VNC system see: http://www.stk.com/resources/help/stk613/helpSystem/extfile/gator/eq-coordsys.htm
        
        // set inial time of the node ( TT)
        this.setStartTTjulDate(lastState.state[0]);
        
        // get r and v vectors
        double[] r = new double[] {lastState.state[1],lastState.state[2],lastState.state[3]};
        double[] v = new double[] {lastState.state[4],lastState.state[5],lastState.state[6]};
        
        // calculate unit vector in V direction (in J2K coordinate frame)
        double normV = MathUtils.norm(v);
        double[] unitV = new double[] {v[0]/normV,v[1]/normV,v[2]/normV};
        
        // calculate unit vector in N direction
        double[] unitNorm = MathUtils.cross(r, v);
        double normNorm = MathUtils.norm(unitNorm);
        unitNorm[0] = unitNorm[0]/normNorm;
        unitNorm[1] = unitNorm[1]/normNorm;
        unitNorm[2] = unitNorm[2]/normNorm;
        
        // calculate unit vector in the Co-Normal direction
        double[] unitCoNorm = MathUtils.cross(unitV, unitNorm);
        
        // calculate Thrust Vector in J2000.0 
        double[] thrustj2K = new double[] {0.0,0.0,0.0};
        // add V component
        thrustj2K = MathUtils.add(thrustj2K,  MathUtils.scale(unitV,vncThrustVector[0] )  );
        // add N component
        thrustj2K = MathUtils.add(thrustj2K,  MathUtils.scale(unitNorm,vncThrustVector[1] )  );
        // add C component
        thrustj2K = MathUtils.add(thrustj2K,  MathUtils.scale(unitCoNorm,vncThrustVector[2] )  );
        
        // add the trustj2k as a delta V to the last state
        lastState.state[4] += thrustj2K[0];
        lastState.state[5] += thrustj2K[1];
        lastState.state[6] += thrustj2K[2];
        
        // copy final ephemeris state: - for goal calculations
        lastStateVector = ephemeris.lastElement();

    }// execute
    
    
    // passes in main app to add the internal frame to
    public void displaySettings(JSatTrak app)
    {
        
        String windowName = "" + getValueAt(0);
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        // show satellite browser window
        ManeuverPanel panel = new ManeuverPanel(this,iframe); // non-modal version
        panel.setIframe(iframe);        
        
        iframe.setContentPane( panel );
        iframe.setSize(300,200+25); // w,h
        iframe.setLocation(5,5);
        
        app.addInternalFrame(iframe);
        
    }

    public

    double[] getVncThrustVector()
    {
        return vncThrustVector;
    }

    public void setVncThrustVector(double[] vncThrustVector)
    {
        this.vncThrustVector = vncThrustVector;
    }
    
    
    // method to get variable by its integer
    public double getVar(int varInt)
    {
        double val = 0;
        
        // don't need switch case here since this one is easy
        if(varInt >= 0 && varInt <= 2)
        {
            val = vncThrustVector[varInt];
        }
        
        return val;
    } // getVar
    
    // method to set variable by its integer
    public void setVar(int varInt, double val)
    {
        // don't need switch case here since this one is easy
        if(varInt >= 0 && varInt <= 2)
        {
            vncThrustVector[varInt] = val;
        }
    } // setVar
    
    // returns the Vector list of all the Variables in this Node
    public Vector<InputVariable> getInputVarVector()
    {
        Vector<InputVariable> varVec = new Vector<InputVariable>(3);
        
        // create list
        for(int i=0; i<3; i++)
        {
            InputVariable inVar = new InputVariable(this, i, varNames[i], vncThrustVector[i]);
            varVec.add(inVar);
        }
        
        return varVec;
    } // getInputVarVector
    
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
                    // mod pos
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
                    // mod pos
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
                    // mod pos
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
    
}
