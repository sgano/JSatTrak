/*
 * SatelliteObject.java
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
 * Created on September 22, 2007, 8:29 PM
 *
 * Creates a renderable satellite model for use in WorldWind
 */

package name.gano.worldwind.geom;

import com.sun.opengl.util.texture.Texture;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.util.Logging;
import java.awt.Color;
import java.util.Vector;
import javax.media.opengl.GL;
import name.gano.worldwind.texture.TextureUtils;

/**
 *
 * @author Shawn
 */
public class SatelliteObject  implements Renderable
{
    
    // options
    private Vec4 center;  // center of satellite, ECEF center point
    private float satSize;  // used for radius of center sphere and scaling of solar arrays
    private Color sphereColor = Color.LIGHT_GRAY;
    private Color solarArrayColor = Color.YELLOW;
    private int numSphereDivisions = 20; // number of horizontal and verticle divisions in the sphere
    private double lastLat;
    private double lastLon;
    private double lastAlt;
    
    // 
    private double rotateECI = 0.0;
    
    // textures
    private Texture solarPanelTex, foilTex;
    
    // world wind canvas used to calculate lat/lon/alt conversions
    private WorldWindowGLCanvas wwd;  // make sure this is initalized before passed in
    
    // cube data for body of sat
      private float[][] cubeData = { {-0.5f, -0.5f, 0.5f}, {0.5f, -0.5f, 0.5f},
                {0.5f, 0.5f, 0.5f}, {-0.5f, 0.5f, 0.5f}, {-0.5f, -0.5f, -0.5f},
                {0.5f, -0.5f, -0.5f}, {0.5f, 0.5f, -0.5f}, {-0.5f, 0.5f, -0.5f}    };
      
    Vector<double[]> tlla = new Vector<double[]>();
    
    SurfaceCircle surfCirc; 
    
    /** Creates a new instance of SatelliteObject */
    public SatelliteObject(double centerLatRad, double centerLonRad, double centerAltitude, float satSize, WorldWindowGLCanvas wwd)
    {
        this.wwd = wwd;
        
        center = wwd.getModel().getGlobe().computePointFromPosition(
                    Angle.fromRadiansLatitude(centerLatRad), 
                    Angle.fromRadiansLongitude(centerLonRad), centerAltitude);
        
        this.satSize = satSize;
        
        this.lastLat = centerLatRad;
        this.lastLon = centerLonRad;
        this.lastAlt = centerAltitude;

        // WWJ VOTD removed need for argument first argument: wwd.getModel().getGlobe()
        surfCirc = new SurfaceCircle(LatLon.fromRadians(centerLatRad,centerLonRad),calcFootPrintRadiusFromAlt(centerAltitude),32); //calcFootPrintRadiusFromAlt(double alt)  500000.0 
        
        //wwd.getModel().getGlobe().
        
        // set texture options - repeat in all directions
//        solarPanelTex.setTexParameteri(GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
//        solarPanelTex.setTexParameteri(GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
//        foilTex.setTexParameteri(GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
//        foilTex.setTexParameteri(GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        
    } // SatelliteObject
    
    
    public void setLlaVector( Vector<double[]> tlla)
    {
        this.tlla = tlla;
    }
    
     /**
     * Causes this <code>SatelliteObject</code> to render itself using the <code>DrawContext</code> provided. <code>dc</code> may
     * not be null.
     *
     * @param dc the <code>DrawContext</code> to be used
     * @throws IllegalArgumentException if <code>dc</code> is null
     */
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        
        if(foilTex == null)
        {
            // load textures
            System.out.println("loading Textures");
            solarPanelTex = TextureUtils.loadTexture("textures/solarpanel.png");
            foilTex = TextureUtils.loadTexture("textures/goldfoil.png");
        }

        javax.media.opengl.GL gl = dc.getGL();
        
        gl.glEnable(GL.GL_TEXTURE_2D);
                
        gl.glPushAttrib(javax.media.opengl.GL.GL_TEXTURE_BIT | javax.media.opengl.GL.GL_ENABLE_BIT | javax.media.opengl.GL.GL_CURRENT_BIT);
        
        
        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);
        
        
        
        // draw orbit lines here! ---------------------------------
        // draw axis lines!
        gl.glLineWidth(10f);
        gl.glBegin(GL.GL_LINES); //GL_LINE_STRIP
            gl.glColor3d( 1.0 , 0.0 , 0.0 ); // COLOR
            gl.glVertex3f(  0f,  0f, 0f );
            gl.glVertex3f(  -10000000f,  0f, 0f );  //x 
            
            gl.glColor3d( 0.0 , 1.0 , 0.0 ); // COLOR
            gl.glVertex3f(  0f,  0f, 0f );
            gl.glVertex3f(  0f,  10000000f, 0f );  // z
            
            gl.glColor3d( 1.0 , 1.0 , 1.0 ); // COLOR
            gl.glVertex3f(  0f,  0f, 0f );
            gl.glVertex3f(  0f,  0f, 10000000f);  // y
            
        gl.glEnd();
        gl.glLineWidth(1f);
        // end axis lines
        
//        
//        gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
//            gl.glColor3d( 1.0 , 0.0 , 0.0 ); // COLOR
//            gl.glVertex3f(  10000000f,  0f, 0f );
//            gl.glVertex3f(  0f,  10000000f, 0f );
//            gl.glVertex3f(  0f,  0f, 10000000f);
//            gl.glVertex3f(  10000000f,  0f, 0f );            
//        gl.glEnd();
        
        // orbit lines
        gl.glPushMatrix();   // for ECI roation
        
        gl.glRotated(rotateECI, 0.0, 1.0, 0.0); // void glRotated(GLdouble angle, GLdouble x, GLdouble y, GLdouble z );
        
        gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
        double[] tllaArray;
        Vec4 ptLoc;
        if(tlla.size() > 1)
        {
            for(int i=0;i<tlla.size();i++)
            {
                tllaArray = tlla.get(i);
                
                // ECEF - from Lat/Lon/Alt
                ptLoc = wwd.getModel().getGlobe().computePointFromPosition(
                    Angle.fromDegreesLatitude(tllaArray[1]), 
                    Angle.fromDegreesLongitude(tllaArray[2]), tllaArray[3]);
                 
                //gl.glVertex3d( ptLoc.x, ptLoc.y , ptLoc.z); 

                
//                 // use with XYZ data
                gl.glVertex3d( -tllaArray[1], tllaArray[3] , tllaArray[2]);  // ECI
                
            }
        }
        gl.glEnd();
         gl.glPopMatrix(); // pop matrix rotatex for ECI
        
        //---------------------------------------------------------
        gl.glPushMatrix();
        
        
         gl.glColor3d( 1.0 , 1.0 , 1.0 ); // white

        // set center
        gl.glTranslated(this.center.x, this.center.y, this.center.z);
        
        
        // rotate so arrays don't hit earth
        gl.glRotated(-lastLat*180.0/Math.PI, 1.0, 0.0, 0.0); // bank wings 
 //       gl.glRotated(lastLon*180.0/Math.PI, 0.0, 1.0, 0.0); // pitch (spin solar arrays)
////        gl.glRotated(lastLat*180.0/Math.PI, 0.0, 0.0, 1.0); // yaw  (slice)
        
        
  
               
//         // new quadric for sphere
//        GLUquadric quadric = dc.getGLU().gluNewQuadric();
//        dc.getGLU().gluQuadricTexture(quadric, true);  // DID THE TRICK FOR Textures on the sphere
//         // foil texture
//        foilTex.bind(); 
//        // create sphere
//        dc.getGLU().gluSphere(quadric, getSatSize(), getNumSphereDivisions(), getNumSphereDivisions());
        
        drawCube(gl,satSize); // for body of satellite
        
        
        // create "solar panels"
        // seems to jitter in the view... can fix this:
        // http://forum.worldwindcentral.com/showthread.php?t=11385
        // In WWJ we generate geometry in local coordinate systems relative to the geometry tiles/sectors. 
        // wwj rendering methods have a pushReferenceCenter/popReferenceCenter pair surrounding the actual rendering. This eliminates the jitter problem.
        
        // color for solar arrays
     //   gl.glColor3d( solarArrayColor.getRed()/255.0 , solarArrayColor.getGreen()/255.0 , solarArrayColor.getBlue()/255.0 ); // COLOR 

        solarPanelTex.bind();
        
        gl.glBegin(GL.GL_QUADS);
            gl.glNormal3f(0,0,1.0f);
            gl.glTexCoord2f(0f,0f);
            gl.glVertex3f(  (-0.4f*satSize),  (-3.5f*satSize), 0f );
            gl.glTexCoord2f(1f,0f);
            gl.glVertex3f(  (0.4f*satSize),   (-3.5f*satSize), 0f );
            gl.glTexCoord2f(1f,1f);
            gl.glVertex3f(  (0.4f*satSize),   (3.5f*satSize), 0f );
            gl.glTexCoord2f(0f,1f);
            gl.glVertex3f(  (-0.4f*satSize),  (3.5f*satSize), 0f );
        gl.glEnd();


        // clean up ----
        gl.glPopMatrix();
        //dc.getGLU().gluDeleteQuadric(quadric);

        gl.glPopAttrib();
        
        gl.glDisable(GL.GL_TEXTURE_2D);
        
        // render surface circle under satellite
        surfCirc.render(dc);
        
    } //render
    
    
    private void drawCube(GL gl, float satSize)
    {
        foilTex.bind();
        
        gl.glBegin(GL.GL_QUADS);
        // Front face
        gl.glNormal3f(0,0,1.0f);
        gl.glTexCoord2d(0,0);
        //gl.glVertex3fv(cubeData[0],0);
        gl.glVertex3f(cubeData[0][0]*satSize,cubeData[0][1]*satSize,cubeData[0][2]*satSize);
        gl.glTexCoord2d(1,0);
        //gl.glVertex3fv(cubeData[1],0);
        gl.glVertex3f(cubeData[1][0]*satSize,cubeData[1][1]*satSize,cubeData[1][2]*satSize);
        gl.glTexCoord2d(1,1);
        //gl.glVertex3fv(cubeData[2],0);
        gl.glVertex3f(cubeData[2][0]*satSize,cubeData[2][1]*satSize,cubeData[2][2]*satSize);
        gl.glTexCoord2d(0,1);
        //gl.glVertex3fv(cubeData[3],0);
        gl.glVertex3f(cubeData[3][0]*satSize,cubeData[3][1]*satSize,cubeData[3][2]*satSize);
        // Back face
        gl.glNormal3f(0,0,-1.0f);
        gl.glTexCoord2d(0,0);
        //gl.glVertex3fv(cubeData[5],0);
        gl.glVertex3f(cubeData[5][0]*satSize,cubeData[5][1]*satSize,cubeData[5][2]*satSize);
        gl.glTexCoord2d(1,0);
        //gl.glVertex3fv(cubeData[4],0);
        gl.glVertex3f(cubeData[4][0]*satSize,cubeData[4][1]*satSize,cubeData[4][2]*satSize);
        gl.glTexCoord2d(1,1);
        //gl.glVertex3fv(cubeData[7],0);
        gl.glVertex3f(cubeData[7][0]*satSize,cubeData[7][1]*satSize,cubeData[7][2]*satSize);
        gl.glTexCoord2d(0,1);
        //gl.glVertex3fv(cubeData[6],0);
        gl.glVertex3f(cubeData[6][0]*satSize,cubeData[6][1]*satSize,cubeData[6][2]*satSize);
        // Top face
        gl.glNormal3f(0,1.0f,0.0f);
        gl.glTexCoord2d(0,0);
        //gl.glVertex3fv(cubeData[3],0);
        gl.glVertex3f(cubeData[3][0]*satSize,cubeData[3][1]*satSize,cubeData[3][2]*satSize);
        gl.glTexCoord2d(1,0);
        //gl.glVertex3fv(cubeData[2],0);
        gl.glVertex3f(cubeData[2][0]*satSize,cubeData[2][1]*satSize,cubeData[2][2]*satSize);
        gl.glTexCoord2d(1,1);
        //gl.glVertex3fv(cubeData[6],0);
        gl.glVertex3f(cubeData[6][0]*satSize,cubeData[6][1]*satSize,cubeData[6][2]*satSize);
        gl.glTexCoord2d(0,1);
        //gl.glVertex3fv(cubeData[7],0);
        gl.glVertex3f(cubeData[7][0]*satSize,cubeData[7][1]*satSize,cubeData[7][2]*satSize);
//		gl.glEnd();
//
//
//		gl.glBegin(GL.GL_QUADS);
        // Left face
        gl.glNormal3f(-1.0f, 0.0f,0.0f);
        gl.glTexCoord2d(0,0);
        //gl.glVertex3fv(cubeData[4],0);
        gl.glVertex3f(cubeData[4][0]*satSize,cubeData[4][1]*satSize,cubeData[4][2]*satSize);
        gl.glTexCoord2d(1,0);
        //gl.glVertex3fv(cubeData[0],0);
        gl.glVertex3f(cubeData[0][0]*satSize,cubeData[0][1]*satSize,cubeData[0][2]*satSize);
        gl.glTexCoord2d(1,1);
        //gl.glVertex3fv(cubeData[3],0);
        gl.glVertex3f(cubeData[3][0]*satSize,cubeData[3][1]*satSize,cubeData[3][2]*satSize);
        gl.glTexCoord2d(0,1);
        //gl.glVertex3fv(cubeData[7],0);
        gl.glVertex3f(cubeData[7][0]*satSize,cubeData[7][1]*satSize,cubeData[7][2]*satSize);
        // Right face
        gl.glNormal3f(1.0f,0.0f,0.0f);
        gl.glTexCoord2d(0,0);
        //gl.glVertex3fv(cubeData[1],0);
        gl.glVertex3f(cubeData[1][0]*satSize,cubeData[1][1]*satSize,cubeData[1][2]*satSize);
        gl.glTexCoord2d(1,0);
        //gl.glVertex3fv(cubeData[5],0);
        gl.glVertex3f(cubeData[5][0]*satSize,cubeData[5][1]*satSize,cubeData[5][2]*satSize);
        gl.glTexCoord2d(1,1);
        //gl.glVertex3fv(cubeData[6],0);
        gl.glVertex3f(cubeData[6][0]*satSize,cubeData[6][1]*satSize,cubeData[6][2]*satSize);
        gl.glTexCoord2d(0,1);
        //gl.glVertex3fv(cubeData[2],0);
        gl.glVertex3f(cubeData[2][0]*satSize,cubeData[2][1]*satSize,cubeData[2][2]*satSize);
        // Bottom face
        gl.glNormal3f(0,-1.0f,0.0f);
        gl.glTexCoord2d(0,0);
        //gl.glVertex3fv(cubeData[4],0);
        gl.glVertex3f(cubeData[4][0]*satSize,cubeData[4][1]*satSize,cubeData[4][2]*satSize);
        gl.glTexCoord2d(1,0);
        //gl.glVertex3fv(cubeData[5],0);
        gl.glVertex3f(cubeData[5][0]*satSize,cubeData[5][1]*satSize,cubeData[5][2]*satSize);
        gl.glTexCoord2d(1,1);
        //gl.glVertex3fv(cubeData[1],0);
        gl.glVertex3f(cubeData[1][0]*satSize,cubeData[1][1]*satSize,cubeData[1][2]*satSize);
        gl.glTexCoord2d(0,1);
        //gl.glVertex3fv(cubeData[0],0);
        gl.glVertex3f(cubeData[0][0]*satSize,cubeData[0][1]*satSize,cubeData[0][2]*satSize);
        gl.glEnd();
    }

    public float getSatSize()
    {
        return satSize;
    }

    public void setSatSize(float satSize)
    {
        this.satSize = satSize;
    }

    public Color getSphereColor()
    {
        return sphereColor;
    }

    public void setSphereColor(Color sphereColor)
    {
        this.sphereColor = sphereColor;
    }

    public int getNumSphereDivisions()
    {
        return numSphereDivisions;
    }

    public void setNumSphereDivisions(int numSphereDivisions)
    {
        this.numSphereDivisions = numSphereDivisions;
    }
    
    
    public void setLLA(double centerLatRad, double centerLonRad, double centerAltitude)
    {
       
        center = wwd.getModel().getGlobe().computePointFromPosition(
                    Angle.fromRadiansLatitude(centerLatRad), 
                    Angle.fromRadiansLongitude(centerLonRad), centerAltitude);
        
        
        this.lastLat = centerLatRad;
        this.lastLon = centerLonRad;
        this.lastAlt = centerAltitude;
        
        // momve surface circle
        surfCirc.setCenter(LatLon.fromRadians(centerLatRad,centerLonRad));
        surfCirc.setRadius(calcFootPrintRadiusFromAlt(centerAltitude));
        
    } // SatelliteObject

    public double getLastLat()
    {
        return lastLat;
    }

    public double getLastLon()
    {
        return lastLon;
    }

    public double getLastAlt()
    {
        return lastAlt;
    }
    
    public double calcFootPrintRadiusFromAlt(double alt) // double lat, double lon, 
    {
        double earthRad = wwd.getModel().getGlobe().getEquatorialRadius();
        double lambda0 = Math.acos(earthRad/(earthRad+alt));
        
        double radius = earthRad*Math.sin(lambda0);
        return radius;
    }

    public double getRotateECI()
    {
        return rotateECI;
    }

    public void setRotateECI(double rotateECI)
    {
        this.rotateECI = rotateECI;
    }
    
}
