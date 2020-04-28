/* radial grid that changes when you zoom in and out
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

import java.awt.Color;

import javax.media.opengl.GL;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.view.orbit.OrbitView;
import name.gano.astro.AstroConst;

/**
 *
 * @author sgano
 */
public class ECIRadialGrid implements Renderable
{
    private boolean showGrid = false;
    private Color color = new Color(0,128,0);
    private boolean drawAxis = false;

    // drawing parameters
    private int numMajorSectionsDrawn = 30; // major segments draw
    private int numMinorSectionsDrawn = numMajorSectionsDrawn; // can be less than numMajorSectionsDrawn since it is off in the distance
    private int circleSegments = 48; // number of segments for each circle drawn
    private int numRadialSegments = 24; // number of radial lines out of earth drawn
    // blending parameter
    private double blendExponent = 2;// needs to be greater than 0, the larger the faster the minor rings disappear
    // axis length
    private float axisLength = 10000000f;
    

    public void render(DrawContext dc)
    {
        if(!showGrid)
        {
            return;
        }

        javax.media.opengl.GL gl = dc.getGL();
        gl.glPushAttrib(javax.media.opengl.GL.GL_TEXTURE_BIT | javax.media.opengl.GL.GL_ENABLE_BIT | javax.media.opengl.GL.GL_CURRENT_BIT);

        // Added so that the colors wouldn't depend on sun shading
        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        //calcs for determining grid sizes
        Position eyePos = ((OrbitView)dc.getView()).getCurrentEyePosition(); // all views used are based on orbitview so far
        double distEarthCenter = eyePos.elevation + AstroConst.R_Earth_mean;
        //System.out.println("ele:" + distEarthCenter);

        int minPowerRings = (int) Math.floor( Math.log10(distEarthCenter) );
        int maxPowerRings = minPowerRings+1;

        double percent = (Math.pow(10, maxPowerRings) - distEarthCenter) / Math.pow(10, maxPowerRings);
        //System.out.println("percent:" + percent);

//        majorUnitSpacing =

        // draw axis
        if(drawAxis)
        {
            gl.glLineWidth(4f);
            gl.glBegin(GL.GL_LINES); //GL_LINE_STRIP
            gl.glColor3d(1.0, 0.0, 0.0); // COLOR
            gl.glVertex3f(0f, 0f, 0f);
            gl.glVertex3f(axisLength, 0f, 0f);  //x

            gl.glColor3d(0.0, 1.0, 0.0); // COLOR
            gl.glVertex3f(0f, 0f, 0f);
            gl.glVertex3f(0f, axisLength, 0f);  // z

            gl.glColor3d(0.0, 0.0, 1.0); // COLOR
            gl.glVertex3f(0f, 0f, 0f);
            gl.glVertex3f(0f, 0f, axisLength);  // y

            gl.glEnd();
        }

        // set line width
        gl.glLineWidth(1f);

        // draw radial lines

        float length = (float)Math.pow(10, minPowerRings)*numMajorSectionsDrawn;
        

        gl.glColor4ub((byte) getColor().getRed(), (byte) getColor().getGreen(), (byte) getColor().getBlue(), (byte) getColor().getAlpha());
        gl.glBegin(GL.GL_LINES); //GL_LINE_STRIP
        for(int i=0;i<numRadialSegments;i++)
        {
            double angle = i*(2.0*Math.PI/(numRadialSegments));
            float z = 0.0f;
            float x = (float)(length*Math.sin(angle));
            float y = (float)(length*Math.cos(angle));

            gl.glVertex3f(  0f,  0f, 0f );
            gl.glVertex3f(  x,  z, y );  //x

        }
        gl.glEnd();

        // radial circles
       
        //float radRadius = 6378137.0f * 1.2f;
        float radRadius = (float)(Math.pow(10, minPowerRings));
        float deltaR = radRadius;

        // Draw major sections!
        for(int j = 0; j < numMajorSectionsDrawn; j++)
        {
            gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
            for(int i = 0; i < circleSegments+1; i++) // +1 loops back to orginal point
            {
                double angle = i * (2.0 * Math.PI / (circleSegments));
                float z = 0.0f;
                float x = (float)(radRadius * Math.sin(angle));
                float y = (float)(radRadius * Math.cos(angle));

                //gl.glVertex3f(  0f,  0f, 0f );
                gl.glVertex3f(x, z, y);  //x

            }
            gl.glEnd();
            radRadius += deltaR;
        }

        // draw all the minor rings
        radRadius = (float)(Math.pow(10, minPowerRings)/10.0);
        deltaR = radRadius;

        int alpha = (int)Math.round(255*Math.pow(percent,blendExponent)); // blending, works great!  1.5 is not bad either
        gl.glColor4ub((byte) getColor().getRed(), (byte) getColor().getGreen(), (byte) getColor().getBlue(), (byte) (alpha));

        for(int j = 0; j < numMinorSectionsDrawn; j++)
        {
            for(int k = 1; k < 10; k++)
            {
                gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
                for(int i = 0; i < circleSegments + 1; i++) // +1 loops back to orginal point
                {
                    double angle = i * (2.0 * Math.PI / (circleSegments));
                    float z = 0.0f;
                    float x = (float)(radRadius * Math.sin(angle));
                    float y = (float)(radRadius * Math.cos(angle));

                    //gl.glVertex3f(  0f,  0f, 0f );
                    gl.glVertex3f(x, z, y);  //x

                } // i
                gl.glEnd();
                radRadius += deltaR;
            } // k
            radRadius += deltaR; // skip the major line
        } // j

        gl.glPopAttrib();
    }

    /**
     * @return the showGrid
     */
    public boolean isShowGrid()
    {
        return showGrid;
    }

    /**
     * @param showGrid the showGrid to set
     */
    public void setShowGrid(boolean showGrid)
    {
        this.showGrid = showGrid;
    }

    /**
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color)
    {
        this.color = color;
    }

    /**
     * @return the drawAxis
     */
    public boolean isDrawAxis()
    {
        return drawAxis;
    }

    /**
     * @param drawAxis the drawAxis to set
     */
    public void setDrawAxis(boolean drawAxis)
    {
        this.drawAxis = drawAxis;
    }

    /**
     * @return the numMajorSectionsDrawn
     */
    public int getNumMajorSectionsDrawn()
    {
        return numMajorSectionsDrawn;
    }

    /**
     * @param numMajorSectionsDrawn the numMajorSectionsDrawn to set
     */
    public void setNumMajorSectionsDrawn(int numMajorSectionsDrawn)
    {
        this.numMajorSectionsDrawn = numMajorSectionsDrawn;
    }

    /**
     * @return the numMinorSectionsDrawn
     */
    public int getNumMinorSectionsDrawn()
    {
        return numMinorSectionsDrawn;
    }

    /**
     * @param numMinorSectionsDrawn the numMinorSectionsDrawn to set
     */
    public void setNumMinorSectionsDrawn(int numMinorSectionsDrawn)
    {
        this.numMinorSectionsDrawn = numMinorSectionsDrawn;
    }

    /**
     * @return the circleSegments
     */
    public int getCircleSegments()
    {
        return circleSegments;
    }

    /**
     * @param circleSegments the circleSegments to set
     */
    public void setCircleSegments(int circleSegments)
    {
        this.circleSegments = circleSegments;
    }

    /**
     * @return the numRadialSegments
     */
    public int getNumRadialSegments()
    {
        return numRadialSegments;
    }

    /**
     * @param numRadialSegments the numRadialSegments to set
     */
    public void setNumRadialSegments(int numRadialSegments)
    {
        this.numRadialSegments = numRadialSegments;
    }

    /**
     * @return the blendExponent
     */
    public double getBlendExponent()
    {
        return blendExponent;
    }

    /**
     * value needs to be > 1, else it is set to 1.0
     * @param blendExponent the blendExponent to set
     */
    public void setBlendExponent(double blendExponent)
    {
        if(blendExponent < 0.0)
        {
            blendExponent = 0.0;
        }
        this.blendExponent = blendExponent;
    }

    /**
     * @return the axisLength
     */
    public float getAxisLength()
    {
        return axisLength;
    }

    /**
     * @param axisLength the axisLength to set
     */
    public void setAxisLength(float axisLength)
    {
        this.axisLength = axisLength;
    }
}
