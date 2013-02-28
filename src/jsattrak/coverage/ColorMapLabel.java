/*
 * ColorMapLabel.java
 * 
 * Class to analyze Earth coverage metrics from Satellite(s). Also has the 
 * capability to render metrics to 2D and 3D windows.
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
 */

package jsattrak.coverage;

import java.awt.Graphics;
import javax.swing.JLabel;

/**
 *
 * @author sgano
 */
public class ColorMapLabel  extends JLabel
{
    
    private ColorMap colorMap = new ColorMap(); // default "jet"
    
    public ColorMapLabel()
    {
        this.setText("");
    }
    
    public ColorMapLabel(ColorMap colorMap)
    {
        this.colorMap = colorMap;
        this.setText("");
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);  // repaints what is already on the buffer
        
        int width = this.getWidth();
        int height = this.getHeight();
        
        if(width > 0 && height >0)
        {
            for(int i=0;i<width;i++)
            {
                g.setColor(colorMap.getColor(i, 0, width-1));
                g.drawLine(i, 0, i, height);
            }
        }
    } // paintComponent

    public ColorMap getColorMap()
    {
        return colorMap;
    }

    public void setColorMap(ColorMap colorMap)
    {
        this.colorMap = colorMap;
        this.repaint();
    }
}
