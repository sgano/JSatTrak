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

package name.gano.swingx.treetable;

import java.io.Serializable;
import java.util.Vector;
import javax.swing.Icon;
import jsattrak.customsat.GoalParameter;
import jsattrak.customsat.InputVariable;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.StateVector;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 * @author Shawn
 */
public class CustomTreeTableNode extends DefaultMutableTreeTableNode implements Serializable
{
    private String nodeType = "default";
    
    protected Icon icon;
    protected String iconName;
    
    Object[] userObject;
        
    private double startTTjulDate = -1; // save julian date when this node starts (-1) if not set yet.
    
    
    public CustomTreeTableNode(Object[] userObject) 
    {
        super(userObject);
        
        this.userObject = userObject;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object aValue, int column) 
    {
        if( column > (getColumnCount()-1) )
        {
            // no nothing
        }
        else
        {
            //((Object[])getUserObject())[column] = aValue;
            userObject[column] = aValue;
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    public int getColumnCount() 
    {      
        //return  ((Object[])getUserObject()).length;
        return  userObject.length;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getValueAt(int column) 
    {
        if( column > (getColumnCount()-1) )
        {
            return "";
        }
        else
        {
            //return ((Object[])getUserObject())[column];
            return userObject[column];
        }
    }
    
    // newNode.setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/toolbarButtonGraphics/treeicons/Satellite_TLE.png")) ) );
    public void setIcon(Icon icon)
    {
        this.icon = icon;
    }

    public Icon getIcon()
    {
        return icon;
    }

    public String getIconName()
    {
        if (iconName != null)
        {
            return iconName;
        }
        else
        {
            String str = userObject.toString();
            int index = str.lastIndexOf(".");
            if (index != -1)
            {
                return str.substring(++index);
            }
            else
            {
                return null;
            }
        }
    }

    public void setIconName(String name)
    {
        iconName = name;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    protected void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }
    
    // meant to be overridden by implementing classes
    // passes in main app to add the internal frame to
    public void displaySettings(JSatTrak app)
    {
        // dummy should open a window for node settings
        //System.out.println("Should open settings for : " + getValueAt(0) );
    }
    
    // meant to be overridden by implementing classes
    public void execute(Vector<StateVector> ephemeris)
    {
         // dummy but should do something based on input ephemeris
        //System.out.println("Executing : " + getValueAt(0) );
        
        // save initial time of the node ( TT)
        this.setStartTTjulDate(ephemeris.lastElement().state[0]);
        
    }// execute

    // meant to be over ridden if there are any input vars
    public double getVar(int varInt)
    {
        return 0;
    }

    // meant to be over ridden if there are any input vars
    public void setVar(int varInt, double val)
    {
    }

    // meant to be over ridden if there are any input vars
    public Vector<InputVariable> getInputVarVector()
    {
        return new Vector<InputVariable>(1);
    }
    
    
    // meant to be over ridden if there are any input vars
    public Double getGoal(int goalInt)
    {
        return new Double(0);
    }

    // meant to be over ridden if there are any input vars
    public Vector<GoalParameter> getGoalParamVector()
    {
        return new Vector<GoalParameter>(1);
    }

    public double getStartTTjulDate()
    {
        return startTTjulDate;
    }

    public void setStartTTjulDate(double startTTjulDate)
    {
        this.startTTjulDate = startTTjulDate;
    }
    
}
