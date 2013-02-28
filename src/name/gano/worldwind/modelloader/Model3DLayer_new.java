/*
 * Model3DLayer_new.java
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
 * Created on February 12, 2008, 10:49 PM
 *
 * toolshed - http://forum.worldwindcentral.com/showthread.php?t=15222&page=6
 */

package name.gano.worldwind.modelloader;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import java.util.Iterator;
import java.util.Vector;
import javax.media.opengl.GL;
import net.java.joglutils.model.examples.DisplayListRenderer;
import net.java.joglutils.model.iModel3DRenderer;

/**
 *
 * @author RodgersGB, Shawn Gano
 */
public class Model3DLayer_new extends AbstractLayer {
    private Vector<WWModel3D_new> list;
    // moved to model itself
//    private boolean maitainConstantSize = true; // default true
//    private double size = 1;
    
    /** Creates a new instance of Model3DLayer_new */
    public Model3DLayer_new() {
        list = new Vector<WWModel3D_new>();
    }

    public void addModel(WWModel3D_new model) {
        list.add(model);
    }
    
    public void removeModel(WWModel3D_new model) {
        list.remove(model);
    }
    
    protected void doRender(DrawContext dc) {
        
        // render using models own methods, not this layers
        Iterator<WWModel3D_new> it = list.iterator();
            while (it.hasNext())
                it.next().render(dc);
//        try {
//            beginDraw(dc);
//            Iterator<WWModel3D_new> it = list.iterator();
//            while (it.hasNext())
//                draw(dc, it.next());
//        }
//        // handle any exceptions
//        catch (Exception e) {
//            // handle
//            e.printStackTrace();
//        }
//        // we must end drawing so that opengl
//        // states do not leak through.
//        finally {
//            endDraw(dc);
//        }
    }
    
    // draw this layer
    protected void draw(DrawContext dc, WWModel3D_new model) {
        GL gl = dc.getGL();
        Position pos = model.getPosition();
        Vec4 loc = dc.getGlobe().computePointFromPosition(pos);
        double localSize = this.computeSize(dc, loc, model);
        
        if (dc.getView().getFrustumInModelCoordinates().contains(loc)) 
        {
            dc.getView().pushReferenceCenter(dc, loc);
//            gl.glRotated(pos.getLongitude().degrees, 0,1,0);
//            gl.glRotated(-pos.getLatitude().degrees, 1,0,0);
            gl.glScaled(localSize, localSize, localSize);  /// can change the scale of the model here!!
            
            //model.setRollDeg(pos.getLongitude().degrees);
            //model.setPitchDeg(-pos.getLatitude().degrees);
            
            // attitude
            //if the base of the model is parallel to the x-y plane and the up vector is in the positive z direction it would be...
            //gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glRotated(model.getYawDeg(), 0,0,1);
            gl.glRotated(model.getPitchDeg(), 1,0,0);
            gl.glRotated(model.getRollDeg(), 0,1,0);
//            gl.glRotated(model.getRollDeg(), 0,1,0);
//            gl.glRotated(model.getPitchDeg(), 1,0,0);
//            gl.glRotated(model.getYawDeg(), 0,0,1); // 0,0,1
//             gl.glRotated(model.getYawDeg(),   0,1,0); // 0,0,1
//              gl.glRotated(model.getPitchDeg(), 0,0,1);
//              gl.glRotated(model.getRollDeg(),  0,1,0);
            //gl.glMatrixMode(GL.GL_MODELVIEW);
            
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
        
//
//        float[] lightPosition = {0F, 100000000f, 0f, 0f};
//        //float[] lightPosition = {(float) (cameraPosition.x + 1000), (float) (cameraPosition.y + 1000), (float) (cameraPosition.z + 1000), 1.0f};
//        //float[] lightPosition = new float[] {0,0,0,0};
//        
//        /** Ambient light array */
//        float lightAmb = 1.0f;
//        float[] lightAmbient = {lightAmb, lightAmb, lightAmb, lightAmb}; // was 0.4f (all)
//        /** Diffuse light array */
//        float diff = 1.0f;
//        float[] lightDiffuse = {diff, diff, diff, diff}; // was 1.0f (all)
//        /** Specular light array */
//        float spec = 1.0f;
//        float[] lightSpecular = {spec, spec, spec, spec}; // was 1.0f (all)
//        
//        float ambient = 0.0f;
//        float[] model_ambient = {ambient, ambient, ambient, ambient}; // was 0.5f (all)
//        
//        gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
//        gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
//        gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
//        gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
//        gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);

//        gl.glDisable(GL.GL_LIGHT0);
//        //gl.glEnable(GL.GL_LIGHT0);
//        gl.glEnable(GL.GL_LIGHT1);
//        gl.glEnable(GL.GL_LIGHTING);
//        gl.glDisable(GL.GL_LIGHTING);
//        gl.glEnable(GL.GL_NORMALIZE);
        
        
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
    
    private double computeSize(DrawContext dc, Vec4 loc, WWModel3D_new model) {
        if (model.isConstantSize())
            return model.getSize();
        
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

//    public boolean isConstantSize() {
//        return maitainConstantSize;
//    }
//
//    public void setMaitainConstantSize(boolean maitainConstantSize) {
//        this.maitainConstantSize = maitainConstantSize;
//    }
//
//    public double getSize() {
//        return size;
//    }
//
//    public void setSize(double size) {
//        this.size = size;
//    }
    
     @Override
    public String toString()
    {
        return "3D Model Layer";
    }
}
