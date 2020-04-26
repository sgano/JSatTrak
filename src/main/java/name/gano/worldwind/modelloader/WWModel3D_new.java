/*
 * WWModel3D_new.java
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
 * Created on February 14, 2008, 9:11 PM
 *
 * toolshed - http://forum.worldwindcentral.com/showthread.php?t=15222&page=6
 */

package name.gano.worldwind.modelloader;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import javax.media.opengl.GL;
import jsattrak.objects.AbstractSatellite;
import name.gano.astro.AstroConst;
import name.gano.astro.MathUtils;
import name.gano.worldwind.layers.Earth.ECIRenderableLayer;
import net.java.joglutils.model.examples.DisplayListRenderer;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.iModel3DRenderer;

/**
 *
 * @author RodgersGB, Shawn Gano
 */

public class WWModel3D_new implements Renderable
{
    private Position position;
    private Model model;
    
    private double yawDeg = 0; // in degrees
    private double pitchDeg = 0; //in degrees
    private double rollDeg = 0; // in degrees
    
    
    private boolean maitainConstantSize = true; // default true
    private double size = 1;
    
    // test rotation 
    public double angle = 0;
    public double xAxis = 0;
    public double yAxis = 0;
    public double zAxis = 0;
    
    public double[] velUnitVec = new double[3];
    
    private double eciRotAngleDeg = 0; // angle satellite already rotated due to ECI rotation (through z-axis)

    private double[] testNorm = new double[3];
    double angle2Rad = 0;
    
    // STK - model - Nadir Alignment with ECF velocity constraint
    
    /** Creates a new instance of WWModel3D_new
     * @param model
     * @param pos 
     */
    public WWModel3D_new(Model model, Position pos)
    {
        this.model = model;
        this.setPosition(pos);
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    public Model getModel()
    {
        return model;
    }

    public double getYawDeg()
    {
        return yawDeg;
    }

    public void setYawDeg(double yawDeg)
    {
        this.yawDeg = yawDeg;
    }

    public double getPitchDeg()
    {
        return pitchDeg;
    }

    public void setPitchDeg(double pitchDeg)
    {
        this.pitchDeg = pitchDeg;
    }

    public double getRollDeg()
    {
        return rollDeg;
    }

    public void setRollDeg(double rollDeg)
    {
        this.rollDeg = rollDeg;
    }
    
     public boolean isConstantSize() 
    {
        return maitainConstantSize;
    }

    public void setMaitainConstantSize(boolean maitainConstantSize) {
        this.maitainConstantSize = maitainConstantSize;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
    
    // Rendering routines so the object can render itself ------
    //===============================================================
    // old doRender
    public void render(DrawContext dc) 
    { 
        try {
            beginDraw(dc);
            
            draw(dc, this);
        }
        // handle any exceptions
        catch (Exception e) {
            // handle
            e.printStackTrace();
        }
        // we must end drawing so that opengl
        // states do not leak through.
        finally {
            endDraw(dc);
        }
    }
    
    // draw this layer
    protected void draw(DrawContext dc, WWModel3D_new model) 
    {
        GL gl = dc.getGL();
        Position pos = model.getPosition();
        Vec4 loc = dc.getGlobe().computePointFromPosition(pos);
        double localSize = this.computeSize(dc, loc);
       
       
        if (dc.getView().getFrustumInModelCoordinates().contains(loc)) 
        {       
            // MAYBE REPLACE "PUSH REFERENCE CENTER" - with gl. move to new center... (maybe not)
            
            dc.getView().pushReferenceCenter(dc, loc);   
//            gl.glRotated(pos.getLongitude().degrees, 0,1,0);
//            gl.glRotated(-pos.getLatitude().degrees, 1,0,0);
            gl.glScaled(localSize, localSize, localSize);  /// can change the scale of the model here!!
            
            // re/un rotate ECI layer
            //System.out.println("------------");
            gl.glRotated(-eciRotAngleDeg, 0.0, 1.0, 0.0); // -- WORKS!!?!! (re rotate because pushReferenceCenter, undoes the ECI roation?)
            //System.out.println("ECI:" + (-eciRotAngleDeg));
            
//            // Earth fixed Inertial  -- yes, inertial? (no, because it was spun around by ECI layer)
//            double radius = 0.6;
//         gl.glLineWidth(3.0f);
//            gl.glColor3d(1, 0, 0); // COLOR 
//            gl.glBegin(gl.GL_LINES); // X-Axis (J2000)
//                gl.glVertex3d(-radius * 3, 0, 0);
//                gl.glVertex3d(0, 0, 0);
//            gl.glEnd();
//            // Draw in axis
//            gl.glColor3d(0, 1, 0); // COLOR 
//            gl.glBegin(gl.GL_LINES); // Y-Axis (J2000)
//                gl.glVertex3d(0, 0, radius * 3);
//                gl.glVertex3d(0, 0, 0);
//            gl.glEnd();
//            // Draw in axis
//            gl.glColor3d(0, 0, 1); // COLOR 
//            gl.glBegin(gl.GL_LINES); // Z-Axis (J2000)
//                gl.glVertex3d(0, radius * 3,0 );
//                gl.glVertex3d(0, 0, 0);
//            gl.glEnd();
            
//            // plot velocity Vector here -- NEEDS A DEROTATION FROM ECI coordinates
//            // because model was spun by xxx degrees already (from ECI layer) around the z-axis so attitude is not where it normally is
//            gl.glColor3d(0,1,1); // COLOR 
//            gl.glBegin(gl.GL_LINES);
//                //gl.glVertex3d(-velUnitVec[0]*radius*3, velUnitVec[2]*radius*3, velUnitVec[1]*radius*3);
//                gl.glVertex3d(-velUnitVec[0]*radius*1, velUnitVec[2]*radius*1, velUnitVec[1]*radius*1);
//                gl.glVertex3d(0, 0, 0);
//            gl.glEnd();
            
            // test normal compontnet -- only works when angle2Rad == 0 below (used for inital debug
//            gl.glColor3d(1,0,1); // COLOR - magenta
//            gl.glBegin(gl.GL_LINES);
//                gl.glVertex3d(-testNorm[0]*radius*4, testNorm[2]*radius*4, testNorm[1]*radius*4);
//                gl.glVertex3d(0, 0, 0);
//            gl.glEnd(); 
            
            
            // rotate around specified angle to align with velocity
            gl.glRotated(angle,-xAxis,zAxis,yAxis); // in J2K coordinate frame
            
            // Rotation of body around velocity
            gl.glRotated(angle2Rad*180/Math.PI,0,1,0); 
            //System.out.println("Rot-body:" + (angle2Rad*180/Math.PI));
        
            
//            // body axis
//            double radius2 = 0.3;
//         gl.glLineWidth(3.0f);
//            gl.glColor3d(1, 0, 0); // COLOR 
//            gl.glBegin(gl.GL_LINES); // X-Axis (body)
//                gl.glVertex3d(-radius2 * 3, 0, 0);
//                gl.glVertex3d(0, 0, 0);
//            gl.glEnd();
//            // Draw in axis
//            gl.glColor3d(0, 1, 0); // COLOR 
//            gl.glBegin(gl.GL_LINES); // Y-Axis (body)
//                gl.glVertex3d(0, 0, radius2 * 3);
//                gl.glVertex3d(0, 0, 0);
//            gl.glEnd();
//            // Draw in axis
//            gl.glColor3d(0, 0, 1); // COLOR 
//            gl.glBegin(gl.GL_LINES); // Z-Axis (body)
//                gl.glVertex3d(0,radius2 * 3,0);
//                gl.glVertex3d(0, 0, 0);
//            gl.glEnd();
            
            //gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            gl.glLineWidth(0.0f);
            //gl.glColor3d(1, 1, 1); // COLOR 
            gl.glColor4d(1, 1, 1, 0);
            
            // add lights?
            //gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMB IENT, model_ambient, 0);
            float[] model_ambient = {1.0f, 1.0f, 1.0f, 1.0f};
            gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, model_ambient, 1);
            
            float lightPosition[] = { 0, 50000000, 0, 1.0f };
            float[] lightAmbient = {0.9f, 0.9f, 0.9f, 1.0f};
        
        /** Diffuse light array */
        float[] lightDiffuse = {0.5f, 0.5f, 0.5f, 1.0f};
        
        /** Specular light array */
        float[] lightSpecular = {0.5f, 0.5f, 0.5f, 1.0f};
        
            gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
            //gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
            //gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
            gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
            //gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);
            
            
            gl.glEnable(GL.GL_LIGHT1);
            gl.glEnable(GL.GL_LIGHTING);
            //gl.glEnable(GL.GL_NORMALIZE);
//
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glShadeModel(GL.GL_SMOOTH);
//            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthFunc(GL.GL_LEQUAL);
            gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
            
            // Get an instance of the display list renderer
            iModel3DRenderer renderer = DisplayListRenderer.getInstance();
            
            renderer.render(gl, model.getModel());
            dc.getView().popReferenceCenter(dc);
        }
    }
    
    // puts opengl in the correct state for this layer
    protected void beginDraw(DrawContext dc) {
        GL gl = dc.getGL();
        
        // SEG - MAYBE USE LIGHTING TO SIMULATE SUN ON SPACE CRAFT??
        
//        Vec4 cameraPosition = dc.getView().getEyePoint();
        
        gl.glPushAttrib(
            GL.GL_TEXTURE_BIT |
            GL.GL_COLOR_BUFFER_BIT |
            GL.GL_DEPTH_BUFFER_BIT |
            GL.GL_HINT_BIT |
            GL.GL_POLYGON_BIT |
            GL.GL_ENABLE_BIT | 
            GL.GL_CURRENT_BIT | 
            GL.GL_LIGHTING_BIT | 
            GL.GL_TRANSFORM_BIT);
        
        
        
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
    }
    
    // resets opengl state
    protected void endDraw(DrawContext dc) {
        GL gl = dc.getGL();
        
        
        
        
        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);
        gl.glPopMatrix();
        
        gl.glPopAttrib();
    }
    
    public void setMainRotationAngleAxis(double[] v, double[] pos)
    {
        if(v == null || pos == null)
        {
            return; // do nothing
        }
        
        double[] orgOrientation = new double[] {0,0,1};
        //double[] orgOrientation = new double[] {0,1,0};
    
//        double[] v = new double[3];
//        v[0] = Double.parseDouble(xBox.getText());
//        v[1] = Double.parseDouble(yBox.getText());
//        v[2] = Double.parseDouble(zBox.getText());
        
        double normV = MathUtils.norm(v);
        double normOr = MathUtils.norm(orgOrientation);
        
        double[] unitOrient = new double[] {orgOrientation[0]/normOr,orgOrientation[1]/normOr,orgOrientation[2]/normOr};
        double[] unitV = new double[] {v[0]/normV,v[1]/normV,v[2]/normV};
        
        double angleRad = Math.acos(MathUtils.dot(unitOrient, unitV));
        
        double[] axis = MathUtils.cross(unitOrient, unitV);
        double normAxis = MathUtils.norm(axis);
        
        //JOptionPane.showMessageDialog(this, "Angle: " + (angleRad*180.0/Math.PI) + ",\n normAxis: " + normAxis + ",\n Axis: " + axis[0] +"," + axis[1] +"," + axis[2]);
        
        //System.out.println("MOD unit VEL:" + unitV[0] +"," + unitV[1] +"," + unitV[2]);
        
        // reorient model
        this.angle =     angleRad*180.0/Math.PI;
        this.xAxis = axis[0];
        this.yAxis = axis[1];
        this.zAxis = axis[2];
        
        // compute angle for rotation require to align sat body normal to veleoctiy (up away from Earth)
        double[] normTop = new double[] {0,1,0}; // is this right?
        
        double c = Math.cos(angleRad);
        double s = Math.sin(angleRad);
        double t = 1-Math.cos(angleRad);
        double[] unitAxis = MathUtils.UnitVector(axis);
        double X = unitAxis[0]; // or axis - seems the same
        double Y = unitAxis[1];
        double Z = unitAxis[2];
        
        double[][] rotThroughAxis = new double[][] {{t*X*X+c,t*X*Y-s*Z,t*X*Z+s*Y},
                                                    {t*X*Y+s*Z,t*Y*Y+c,t*Y*Z-s*X},
                                                    {t*X*Z-s*Y,t*Y*Z+s*X,t*Z*Z+c}};
        double[] resultingNormDir = MathUtils.UnitVector( MathUtils.mult(rotThroughAxis, normTop) );
        
        //double dot = MathUtils.dot(resultingNormDir, v);
        //System.out.println("dot="+dot);
        testNorm = resultingNormDir;
        
        // above works fin (tested)
        
        // now to solve - paralllel with position vector component in this plane
        double[] prosProjVelPlane = MathUtils.UnitVector( MathUtils.sub(pos, MathUtils.scale(unitV,MathUtils.dot(pos, unitV)) )   );

        
        //double rndDOTppvp = MathUtils.dot(resultingNormDir, prosProjVelPlane);
        // find angle between this and the resultingNormDir
        // method 1 - less accurate (around 1/-1), faster CPU-wise
        angle2Rad = Math.acos(MathUtils.dot(resultingNormDir, prosProjVelPlane)); // assumes smallest angle, no +/- onl 0-180
        // method 2 - more accurate and more CPU expensive
        //angle2Rad = Math.atan2( MathUtils.norm(MathUtils.cross(resultingNormDir, prosProjVelPlane)) , MathUtils.dot(resultingNormDir, prosProjVelPlane) );
                
        // check sign of angle2Rad by doting prosProjVelPlane with the body fixed x-axis
        double[] bodyXUnit = MathUtils.UnitVector( MathUtils.mult(rotThroughAxis, new double[] {1,0,0}) );
        
        // correct sign of angle2Rad by checking the dot product
        if(MathUtils.dot(prosProjVelPlane,bodyXUnit ) > 0)
        {
            angle2Rad = -angle2Rad; // flip sign
        }
        
        //System.out.println("Rot-body: " + (angle2Rad*180/Math.PI) + " " + resultingNormDir[0]+ " " + resultingNormDir[1]+ " " + resultingNormDir[2]+ " " + prosProjVelPlane[0]+ " " + prosProjVelPlane[1]+ " " + prosProjVelPlane[2]);
        
    } // setMainRotationAngleAxis
    
    
    private double computeSize(DrawContext dc, Vec4 loc) {
        if (this.maitainConstantSize)
            return size;
        
        if (loc == null) {
            System.err.println("Null location when computing size of model");
            return 1;
        }
        double d = loc.distanceTo3(dc.getView().getEyePoint());
        double currentSize = 60 * dc.getView().computePixelSizeAtDistance(d);
        if (currentSize < 2)
            currentSize = 2;
        
        return currentSize;
    }

    public double getEciRotAngleDeg()
    {
        return eciRotAngleDeg;
    }

    public void setEciRotAngleDeg(double eciRotAngleDeg)
    {
        this.eciRotAngleDeg = eciRotAngleDeg;
    }
    
    // update axis and alll rotational things when time updates -- currently only runs when new 3D model is loaded - otherwise similar functions run from OrbitModelRenderable to reduce CPU expense
    public void updateAttitude( AbstractSatellite sat)
    {
        double MJD = sat.getCurrentJulDate() - AstroConst.JDminusMJD;
        
         setPosition(new Position(Angle.fromRadians(sat.getLatitude()),
                Angle.fromRadians(sat.getLongitude()),
                sat.getAltitude()));
        // set roll pitch yaw (assume user wants LVLH, velcorty aligned)

        // calculate TEME velocity and set rotation angles and axis
        setMainRotationAngleAxis(sat.getTEMEVelocity(), sat.getTEMEPos());

        // set velcoity for test plotting
        this.velUnitVec = MathUtils.UnitVector(sat.getTEMEVelocity());

        // Set ECI angle
        double T = (MJD-51544.5)/36525.0;  // centuries since J2000.0
        double rotateECIdeg =  ( (280.46061837 + 360.98564736629*(MJD-51544.5)) + 0.000387933*T*T - T*T*T/38710000.0 + ECIRenderableLayer.offsetRotdeg) % 360.0;
        setEciRotAngleDeg(rotateECIdeg);
    }
     
    
}