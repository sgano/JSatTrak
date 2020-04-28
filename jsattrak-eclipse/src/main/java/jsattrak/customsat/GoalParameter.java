/*
 * Goal Parameter for the solver class - can represent a number of parameters from different node types
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
 * 
 */

package jsattrak.customsat;

import name.gano.swingx.treetable.CustomTreeTableNode;

/**
 *
 * @author sgano
 */
public class GoalParameter 
{
    private CustomTreeTableNode parentNode; // parent Node that the variable belongs to
    int goalInt; // variable number of the parent Node - used for getting and setting value in node
    private String nameDescription; // name and short discription of the parameter
    private Double currentValue;  // current value of the variable (unscaled) (used as an object so can be null)
    
    private double goalValue = 0; // what the solver should be driving this parameter to
    // parameters
    private double scale = 1; // value to scale variable when computing error (use inial value for now)

    public GoalParameter(CustomTreeTableNode parentNode, int goalInt, String nameDescription, Double currentValue)
    {
        this.parentNode = parentNode;
        this.goalInt = goalInt;
        this.nameDescription = nameDescription;
        this.currentValue = currentValue;

        // set scale
        if (currentValue != null)
        {
            if (Math.abs(currentValue) > 1E-12)
            {
                scale = Math.abs(currentValue); // abs so scale not negative
            }
        }
    }

    
    // returns name of the item -- used for displaying in a JTree
    public String toString()
    {
        return getNameDescription();
    }

   
    /**
     *  returns unscaled value
     * @return unscaled value
     */
    public Double getValue()
    {
        currentValue = getParentNode().getGoal(goalInt);
        return currentValue;
    }

    /**
     * Set value unscaled
     * @param currentValue
     */
    public void setValue(double currentValue)
    {
        this.currentValue = currentValue;
        //getParentNode().setVar(goalInt, currentValue);
    }
    
    // used to get scaled Value of the variable 
    public double getScaledValue()
    {
        currentValue = getParentNode().getGoal(goalInt);
        return currentValue/getScale();
    }


    public // current value of the variable (unscaled)
    double getGoalValue()
    {
        return goalValue;
    }

    public void setGoalValue(double goalValue)
    {
        this.goalValue = goalValue;
    }

    public CustomTreeTableNode getParentNode()
    {
        return parentNode;
    }

    public String getNameDescription()
    {
        return nameDescription;
    }

    public double getScale()
    {
        return scale;
    }

    public void setScale(double scale)
    {
        this.scale = scale;
    }
}
