/*
 * EphemerisFromFileNode.java
 * 
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
 * Created: 10 Jul 2009
 */

package jsattrak.customsat;

import java.awt.Toolkit;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import jsattrak.customsat.gui.EphemerisFromFilePanel;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.StateVector;
import name.gano.file.StkEphemerisReader;
import name.gano.swingx.treetable.CustomTreeTableNode;

/**
 *
 * @author Shawn E. Gano, shawn@gano.name
 */
public class EphemerisFromFileNode extends CustomTreeTableNode
{

    private String filename = "";

    public EphemerisFromFileNode(CustomTreeTableNode parentNode)
    {
        super(new String[] {"Ephemeris From File","",""}); // initialize node, default values

        // set icon for this type
        setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/customSatIcons/file.png")) ) );
        //set Node Type
        setNodeType("Ephemeris From File");

       
        // add this node to parent - last thing
        if( parentNode != null)
            parentNode.add(this);
    }


     // meant to be overridden by implementing classes
    @Override
    public void execute(Vector<StateVector> ephemeris)
    {
         // try to read from file -- if error report to log!  and continue!
        StkEphemerisReader r = new StkEphemerisReader();
        try
        {
            Vector<StateVector> e = r.readStkEphemeris(filename);
           
            for(StateVector sv : e)
            {
                // add state to ephemeris
                ephemeris.add(sv);
            }

            // set inital time for this node
            this.setStartTTjulDate(e.get(0).state[0]);

            System.out.println( " - Node:" + getValueAt(0) + ", Ephemeris Points Read: " + e.size() );

        }
        catch(Exception e)
        {
            System.out.println("Error Reading Ephemeris from File- Node:" + getValueAt(0) + ", message : " + e.toString());
        }

        

    }// execute


    // passes in main app to add the internal frame to
    public void displaySettings(JSatTrak app)
    {

        String windowName = "" + getValueAt(0);
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);

        // show satellite browser window
        EphemerisFromFilePanel gsBrowser = new EphemerisFromFilePanel(this); // non-modal version
        gsBrowser.setIframe(iframe);

        iframe.setContentPane( gsBrowser );
        iframe.setSize(365,150); // w,h
        iframe.setLocation(5,5);

        app.addInternalFrame(iframe);
    }

    /**
     * @return the filename
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

}
