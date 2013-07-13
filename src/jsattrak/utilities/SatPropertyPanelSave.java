/*
 * SatPropertyPanelSave.java
 *=====================================================================
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
 * Created on August 20, 2007, 12:52 PM
 *
 * 
 */

package jsattrak.utilities;

import jsattrak.gui.SatPropertyPanel;
import java.awt.Dimension;
import java.io.Serializable;

/**
 *
 * @author sgano
 */
public class SatPropertyPanelSave implements Serializable
{
    private int xPos; // location of window
    private int yPos;
    
    private int width;
    private int height;
    
    private String name;
    
    
    /** Creates a new instance of SatPropertyPanelSave */
    public SatPropertyPanelSave(SatPropertyPanel panel, int x, int y, Dimension d)
    {
        xPos = x;
        yPos = y;
        
        width = d.width;
        height = d.height;
        
        name = panel.getSatName();
    }

    public int getXPos()
    {
        return xPos;
    }

    public void setXPos(int xPos)
    {
        this.xPos = xPos;
    }

    public int getYPos()
    {
        return yPos;
    }

    public void setYPos(int yPos)
    {
        this.yPos = yPos;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
}
