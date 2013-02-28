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

package name.gano.worldwind.geom;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import java.awt.Color;
import javax.media.opengl.GL;
import jsattrak.coverage.CoverageAnalyzer;

/**
 *
 * @author sgano
 */
public class CoverageDataGeom implements Renderable 
{
    
//    Globe globe;
    CoverageAnalyzer ca;
    
    public CoverageDataGeom(CoverageAnalyzer ca)
    {
        this.ca = ca;
    }
    
    
    public void render(DrawContext dc)
    {
        if(dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        javax.media.opengl.GL gl = dc.getGL();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPushAttrib(javax.media.opengl.GL.GL_TEXTURE_BIT | javax.media.opengl.GL.GL_ENABLE_BIT | javax.media.opengl.GL.GL_CURRENT_BIT);
        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);

        // allow for transparency
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        Color satColor;

        Double nanDbl = new Double(Double.NaN);

        double alt = 100000;

        if(ca != null)
        {

            for(int i = 0; i < ca.getLongPanels(); i++)
            {
                for(int j = 0; j < ca.getLatPanels(); j++)
                {

                    if(ca.getCoverageCumTime()[j][i] > 0)
                    {

                        Vec4 pos1 = dc.getGlobe().computePointFromPosition(
                                Angle.fromDegrees(ca.getLatGridPoints()[j]), // lat
                                Angle.fromDegrees(ca.getLonGridPoints()[i]), // lon
                                alt);
                        Vec4 pos2 = dc.getGlobe().computePointFromPosition(
                                Angle.fromDegrees(ca.getLatGridPoints()[j + 1]), // lat
                                Angle.fromDegrees(ca.getLonGridPoints()[i]), // lon
                                alt);
                        Vec4 pos3 = dc.getGlobe().computePointFromPosition(
                                Angle.fromDegrees(ca.getLatGridPoints()[j + 1]), // lat
                                Angle.fromDegrees(ca.getLonGridPoints()[i + 1]), // lon
                                alt);
                        Vec4 pos4 = dc.getGlobe().computePointFromPosition(
                                Angle.fromDegrees(ca.getLatGridPoints()[j]), // lat
                                Angle.fromDegrees(ca.getLonGridPoints()[i + 1]), // lon
                                alt);

                        satColor = ca.getColorForIndex(j, i);
                        gl.glColor4f(satColor.getRed() / 255.0f, satColor.getGreen() / 255.0f, satColor.getBlue() / 255.0f, ca.getAlpha() / 255.0f); // COLOR

                        gl.glBegin(GL.GL_QUADS);  // counter clock wise?
                        //gl.glTexCoord2f(0, 0);

                        gl.glVertex3f((float)pos1.x, (float)pos1.y, (float)pos1.z);
                        //gl.glTexCoord2f(0, 1);
                        gl.glVertex3f((float)pos2.x, (float)pos2.y, (float)pos2.z);
                        //gl.glTexCoord2f(1, 1);
                        gl.glVertex3f((float)pos3.x, (float)pos3.y, (float)pos3.z);
                        //gl.glTexCoord2f(1, 0);
                        gl.glVertex3f((float)pos4.x, (float)pos4.y, (float)pos4.z);
                        gl.glEnd();
                        
                        // if drawing grid
                        if(ca.isPlotCoverageGrid())
                        {
                            gl.glColor4f(Color.GREEN.getRed() / 255.0f, Color.GREEN.getGreen() / 255.0f, Color.GREEN.getBlue() / 255.0f,0.2f);
                            gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex3f((float)pos1.x, (float)pos1.y, (float)pos1.z);
                                gl.glVertex3f((float)pos2.x, (float)pos2.y, (float)pos2.z);
                                gl.glVertex3f((float)pos3.x, (float)pos3.y, (float)pos3.z);
                                gl.glVertex3f((float)pos4.x, (float)pos4.y, (float)pos4.z);
                            gl.glEnd();
                            
                            // draw center?
                            
                        }// draw coverage grid with point in center
                        
                    } // time > 0
                    else
                    {
                        // not, filled but still draw grid
                        if(ca.isPlotCoverageGrid())
                        {
                            Vec4 pos1 = dc.getGlobe().computePointFromPosition(
                                    Angle.fromDegrees(ca.getLatGridPoints()[j]), // lat
                                    Angle.fromDegrees(ca.getLonGridPoints()[i]), // lon
                                    alt);
                            Vec4 pos2 = dc.getGlobe().computePointFromPosition(
                                    Angle.fromDegrees(ca.getLatGridPoints()[j + 1]), // lat
                                    Angle.fromDegrees(ca.getLonGridPoints()[i]), // lon
                                    alt);
                            Vec4 pos3 = dc.getGlobe().computePointFromPosition(
                                    Angle.fromDegrees(ca.getLatGridPoints()[j + 1]), // lat
                                    Angle.fromDegrees(ca.getLonGridPoints()[i + 1]), // lon
                                    alt);
                            Vec4 pos4 = dc.getGlobe().computePointFromPosition(
                                    Angle.fromDegrees(ca.getLatGridPoints()[j]), // lat
                                    Angle.fromDegrees(ca.getLonGridPoints()[i + 1]), // lon
                                    alt);
                            gl.glColor4f(Color.GREEN.getRed() / 255.0f, Color.GREEN.getGreen() / 255.0f, Color.GREEN.getBlue() / 255.0f,0.2f);
                            gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex3f((float)pos1.x, (float)pos1.y, (float)pos1.z);
                                gl.glVertex3f((float)pos2.x, (float)pos2.y, (float)pos2.z);
                                gl.glVertex3f((float)pos3.x, (float)pos3.y, (float)pos3.z);
                                gl.glVertex3f((float)pos4.x, (float)pos4.y, (float)pos4.z);
                            gl.glEnd();
                        
                            // draw center
                            
                        }// draw coverage grid with point in center
                        
                    }// else (time ==0)
                    
                    // 

                } // for lat

            } // for long

        } // not null


        
        gl.glPopAttrib();
        
     } // render
}
