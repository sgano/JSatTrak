/*
 * ColorMap.java
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
 * Color mapping class - default colors = Jet, interpolates between colors points
 * for a continuos color scale
 * 
 */

package jsattrak.coverage;

import java.awt.Color;

/**
 *
 * @author Shawn
 */
public class ColorMap 
{
    
    
    public ColorMap()
    {
        
    } // ColorMap
    
    public Color getColor(double val, double low, double high)
    {
        return getColor(val, low, high, 255); // get color with no alpha
    } // get color 
    
    // conviencent for using a double value for alpha instead of an integer
    public Color getColor(double val, double low, double high, double alpha)
    {
        return getColor(val, low, high, (int)Math.round(alpha*255)); // get color with no alpha
    } // get color 
    
    public Color getColor(double val, double low, double high, int alpha)
    {
        // error conditions to insure val is in [low,high]
        if(val > high)
        {
            high = val;
        }
        else if(val < low)
        {
            low = val;
        }
        
        // percentile of color
        float percentile = (float)((val-low)/(high-low));
        // calc index of in colormap (integer part = index, fraction part is for interpolation)
        float indexF = percentile*(colormap.length-1); // temp hold index and indexF (index fraction)
        // integer index
        int index = (int)Math.floor(indexF);
        // fraction part
        indexF = indexF - index;
        
        // return color if index is at the bounds (no interpolation)
        if(index == 0 || index == colormap.length-1)
        {
            return new Color(colormap[index][0],colormap[index][1],colormap[index][2],alpha);
        }
        
        // interpolate color
        int red   = Math.round(colormap[index][0] + indexF*(colormap[index+1][0]-colormap[index][0]));
        int green = Math.round(colormap[index][1] + indexF*(colormap[index+1][1]-colormap[index][1]));
        int blue  = Math.round(colormap[index][2] + indexF*(colormap[index+1][2]-colormap[index][2]));
        
        // create and return color
        return new Color(red,green,blue,alpha);
        
    } // getColor (with alpha)
    
    protected void setColorMap(int[][] newColorMap)
    {
        colormap = newColorMap;
    }
    
    private int[][] colormap = 
    {{0,     0,   143},
     {0,     0,   159},
     {0,     0,   175},
     {0,     0,   191},
     {0,     0,   207},
     {0,     0,   223},
     {0,     0,   239},
     {0,     0,   255},
     {0,    16,   255},
     {0,    32,   255},
     {0,    48,   255},
     {0,    64,   255},
     {0,    80,   255},
     {0,    96,   255},
     {0,   112,   255},
     {0,   128,   255},
     {0,   143,   255},
     {0,   159,   255},
     {0,   175,   255},
     {0,   191,   255},
     {0,   207,   255},
     {0,   223,   255},
     {0,   239,   255},
     {0,   255,   255},
    {16,   255,   239},
    {32,   255,   223},
    {48,   255,   207},
    {64,   255,   191},
    {80,   255,   175},
    {96,   255,   159},
   {112,   255,   143},
   {128,   255,   128},
   {143,   255,   112},
   {159,   255,    96},
   {175,   255,    80},
   {191,   255,    64},
   {207,   255,    48},
   {223,   255,    32},
   {239,   255,    16},
   {255,   255,     0},
   {255,   239,     0},
   {255,   223,     0},
   {255,   207,     0},
   {255,   191,     0},
   {255,   175,     0},
   {255,   159,     0},
   {255,   143,     0},
   {255,   128,     0},
   {255,   112,     0},
   {255,    96,     0},
   {255,    80,     0},
   {255,    64,     0},
   {255,    48,     0},
   {255,    32,     0},
   {255,    16,     0},
   {255,     0,     0},
   {239,     0,     0},
   {223,     0,     0},
   {207,     0,     0},
   {191,     0,     0},
   {175,     0,     0},
   {159,     0,     0}};
            
} // ColorMap
