/**
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
package name.gano.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import jsattrak.utilities.OrbitModelRenderable;

/**
 * This layer automatically rotates its collection of renderables so that their 
 * coordinates are plotted in ECI coordinates based on current time (MDJ).
 * <br><i>Note:</i> Because of the JOGL coordinate system is different than the ECI (e.g. J2000.0) coordinate system
 *       the renderables added to this collection should plot the ECI coordinates (x,y,z) as ( -x, z, y) 
 *       For example: gl.glVertex3d( -x, z , y);
 *
 * <p><b>!!Precession and Nutation are not yet accouted for.</b>
 *
 * <p>Shawn E. Gano
 * <br>Created on October 12, 2007 - update May 5 2008
 *
 * 
 * @author Shawn E. Gano (shawn@gano.name)
 * @version $id$
 */
public class ECIRenderableLayer extends RenderableLayer
{
    
    
    // ECI rotation variable
    /**
     * jogl coordinate Greenwich to ECI x-axis offset
     */
    public static final double offsetRotdeg = -90.0; // jogl coordinate Greenwich to ECI x-axis offset
    private double rotateECIdeg = 280.46061837+offsetRotdeg; // rotation in degrees (default j2k)
    private double currentMJD = 51544.5; // current modified julian date, universal time (default J2k)

    
    // SEG added -------------
    /**
     * Creates layer with initial time
     * @param iniMJD inital Modified Julian Date
     */
    public ECIRenderableLayer(double iniMJD)
    {
        super();
        //this.delegateOwner = null;
        setCurrentMJD(iniMJD);
    } // ECIRenderableLayer - constructor

//    public ECIRenderableLayer(Layer delegateOwner)
//    {
//        this.delegateOwner = delegateOwner;
//    }
    
//    // SEG added -------------
//    /**
//     * Creates layer with delagate layer owner and initial time
//     * @param delegateOwner delegate owner
//     * @param iniMJD inital Modified Julian Date
//     */
//    public ECIRenderableLayer(Layer delegateOwner, double iniMJD)
//    {
//        this.delegateOwner = delegateOwner;
//        setCurrentMJD(iniMJD);
//    } // ECIRenderableLayer - constructor

    // SEG modified -------------
    @Override
    //  This method has been modified to include rotating for ECI coordinates based on current MJD
    protected void doRender(DrawContext dc)
    {
        javax.media.opengl.GL gl = dc.getGL();
        
        // this line must be before matrix push - otherwise when coverage is off, an ECI is on EFEF lines don't show
       gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW); // add to prevent interatction with star layer // MUST INCLUDE THIS -- 5 May 2008 SEG
        
        gl.glPushMatrix();   // push for ECI roation
        gl.glRotated(-rotateECIdeg, 0.0, 1.0, 0.0); // rotate about Earth's spin axis (z-coordinate in J2K, y-coordinate in JOGL)
         
        for (Renderable renderable : super.getRenderables())
        {
            // If the caller has specified their own Iterable,
            // then we cannot make any guarantees about its contents.
            if (renderable != null)
            {
                renderable.render(dc);
            }
        }
        
        gl.glPopMatrix(); // pop matrix rotatex for ECI
        
    } // do Render


    @Override
    public String toString()
    {
        return "ECI Layer";
    }

    // SEG added -------------
    public double getCurrentMJD()
    {
        return currentMJD;
    } // getCurrentMJD

    // SEG added -------------
    /**
     * Set the time for the ECI layer.  
     * Mean Sidereal formula taken from <i>Astronomical Algorithms</i> 2nd ed., Jean Meesus. 
     *
     * @param currentMJD  modified Julian Date (Universal Time)
     */
    public void setCurrentMJD(double currentMJD)
    {
        this.currentMJD = currentMJD;
        
        // centuries since J2000.0
        double T = (currentMJD-51544.5)/36525.0;  
        
        // now calculate the mean sidereal time at Greenwich (UT time) in degrees
        rotateECIdeg =  ( (280.46061837 + 360.98564736629*(currentMJD-51544.5)) + 0.000387933*T*T - T*T*T/38710000.0 +offsetRotdeg) % 360.0;
        
        //System.out.println("Rotat:" + rotateECIdeg);
        
        // set ECI angle to all OrbitModelRenderables
         for (Renderable renderable : super.getRenderables())
        {
             if(renderable instanceof OrbitModelRenderable)
             {
                 ((OrbitModelRenderable)renderable).updateMJD(currentMJD, rotateECIdeg);
             }//OrbitModelRenderable
         } // for renderables
        
    } // setCurrentMJD

    // SEG added -------------
    public double getRotateECIdeg()
    {
        return rotateECIdeg;
    } //getRotateECIdeg
    
    
}
