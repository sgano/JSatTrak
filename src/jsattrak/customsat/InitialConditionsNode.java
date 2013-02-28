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
 */

package jsattrak.customsat;

import java.awt.Toolkit;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import jsattrak.customsat.gui.InitialConditionsPanel;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.StateVector;
import name.gano.astro.AstroConst;
import name.gano.astro.Kepler;
import name.gano.astro.time.Time;
import name.gano.swingx.treetable.CustomTreeTableNode;

/**
 *
 * @author sgano
 */
public class InitialConditionsNode extends CustomTreeTableNode
{  
    
    private double[] keplarianElements = new double[] {6678140,0.01,45.0*Math.PI/180.0,0.0,0.0,0.0}; // see Kepler.java for order of elements (radians)
    private double[] j2kIniState = new double[6]; // x,y,z,dx,dy,dz
    
    private boolean usingKepElements = true; // if user is inputting keplarian elements
    
    private double iniJulDate = 0; // (UTC) julian date of the inital conditions (should set to epock of scenario by default)
  
    private Time scenarioEpochDate; 
    
        
    public InitialConditionsNode(CustomTreeTableNode parentNode, Time scenarioEpochDate)
    {
        super(new String[] {"Initial Conditions","",""}); // initialize node, default values
        
        this.scenarioEpochDate = scenarioEpochDate;
        iniJulDate = scenarioEpochDate.getJulianDate();
        
        // set icon for this type
        setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/customSatIcons/ini.png")) ) );
        //set Node Type
        setNodeType("Initial Conditions");
        
        // setup j2K state
        j2kIniState = Kepler.state(AstroConst.GM_Earth, keplarianElements, 0.0);
        
        
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
        
        // convert UTC to terestrial time
        double iniJulDate_TT =  iniJulDate + Time.deltaT(iniJulDate - AstroConst.JDminusMJD);
        
        // set inial time of the node ( TT)
        this.setStartTTjulDate(iniJulDate_TT);
        
        StateVector iniState = new StateVector(j2kIniState,iniJulDate_TT);
        
        // add state to ephemeris
        ephemeris.add(iniState);
        
    }// execute
    
    
    // passes in main app to add the internal frame to
    public void displaySettings(JSatTrak app)
    {
        
        String windowName = "" + getValueAt(0);
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        // show satellite browser window
        InitialConditionsPanel gsBrowser = new InitialConditionsPanel(this,scenarioEpochDate); // non-modal version
        gsBrowser.setIframe(iframe);        
        
        iframe.setContentPane( gsBrowser );
        iframe.setSize(365,304+50); // w,h
        iframe.setLocation(5,5);
        
        app.addInternalFrame(iframe);
        
       
    }

    public double[] getKeplarianElements()
    {
        return keplarianElements;
    }

    /**
     *  Sets Keplarian elements initial State. Be sure to set epoch first, this method also automatically updates Cartesian j2k to agree. (assumes element set is for current epoch)
     * @param keplarianElements keplarian elements
     */
    public void setKeplarianElements(double[] keplarianElements)
    {
        this.keplarianElements = keplarianElements;
        
        j2kIniState = Kepler.state(AstroConst.GM_Earth, keplarianElements, 0.0);
    }

    public double[] getJ2kIniState()
    {
        return j2kIniState;
    }

    /**
     *  Sets j2k cartesian coordinate initial State. Be sure to set epoch first, this method also automatically updates keplarian elements to agree.
     * @param j2kIniState j2k cartesian state
     */
    public void setJ2kIniState(double[] j2kIniState)
    {
        this.j2kIniState = j2kIniState;
        
        keplarianElements = Kepler.SingularOsculatingElements(AstroConst.GM_Earth, j2kIniState);
    }

    public boolean isUsingKepElements()
    {
        return usingKepElements;
    }

    public void setUsingKepElements(boolean usingKepElements)
    {
        this.usingKepElements = usingKepElements;
    }

    public double getIniJulDate()
    {
        return iniJulDate;
    }

    public void setIniJulDate(double iniJulDate)
    {
        this.iniJulDate = iniJulDate;
    }
    
}
