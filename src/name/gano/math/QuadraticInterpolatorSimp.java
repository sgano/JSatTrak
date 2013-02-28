/*
 * Shawn E. Gano - simple Quadratic Interpolator - equally space points
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

package name.gano.math;

/**
 *
 * @author sgano
 */
public class QuadraticInterpolatorSimp 
{
    // exterma point
    private double[] extremaPt = new double[2];
    
    // roots x-value
    private double lowerRoot = 0;
    private double upperRoot = 0;
    
    // root count in domain [x0,x2]
    private int rootCountInDomain = 0;
    
    // constructor -- x0 < x1 < x2 !!
    // points much be equally spaced!
    public QuadraticInterpolatorSimp(double x0, double y0, double x1, double y1, double x2, double y2)
    {
        // coefficients for x0=-1,x1=0,x2=1, y=a*x^2+b*x+c
        double a = 0.5*(y2+y0) - y1;
        double b = 0.5*(y2-y0);
        double c = y1;
        
        // find extreme value
        double xe0 = -b/(2.0*a);
        extremaPt[0] = ( (x0-x1)*xe0 + x0 + x1 ) /2.0 ;
        extremaPt[1] = (a*xe0+b)*xe0+c;
        
        // discriminant
        double dis = b*b - 4.0*a*c;
        
        if(dis >= 0) // if roots are real
        {
            double dx = 0.5 * Math.sqrt(dis) / Math.abs(a);
            lowerRoot = xe0-dx; // un scaled
            upperRoot = xe0+dx; // un scaled
            
            if(Math.abs(lowerRoot) <= 1.0)
                rootCountInDomain++;
            if(Math.abs(upperRoot) <= 1.0)
                rootCountInDomain++;
            
            if(lowerRoot < -1.0 ) 
                lowerRoot = upperRoot; // shortcut  -- if only one root then lowerRoot will always store that value
            
            // scale roots back to orginal domain 
            if(rootCountInDomain > 0)
            {
                lowerRoot = ( (x0-x1)*lowerRoot + x0 + x1 ) /2.0 ;
                upperRoot = ( (x0-x1)*upperRoot + x0 + x1 ) /2.0 ;
            }
            
        } // real roots exist
        
    }

    // exterma point
    public double[] getExtremaPt()
    {
        return extremaPt;
    }

    // roots x-value
    public double getLowerRoot()
    {
        return lowerRoot;
    }

    public double getUpperRoot()
    {
        return upperRoot;
    }

    // root count in domain [x0,x2]
    public int getRootCountInDomain()
    {
        return rootCountInDomain;
    }
    
}
