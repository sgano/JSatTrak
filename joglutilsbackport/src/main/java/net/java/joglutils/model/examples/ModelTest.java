/*
 * Copyright (c) 2006 Greg Rodgers All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * The names of Greg Rodgers, Sun Microsystems, Inc. or the names of
 * contributors may not be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. GREG RODGERS,
 * SUN MICROSYSTEMS, INC. ("SUN"), AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL GREG
 * RODGERS, SUN, OR SUN'S LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF GREG
 * RODGERS OR SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Modifications made by Z-Knight
 */

package net.java.joglutils.model.examples;

import net.java.joglutils.model.*;
import com.sun.opengl.util.Animator;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.opengl.GLCanvas;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import java.awt.*;
import javax.media.opengl.glu.GLU;
import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.iModel3DRenderer;

public class ModelTest {
   
    /** Creates a new instance of Main */
    public ModelTest() {
    }

    public static void main(String[] args)
    {
        Frame frame = new Frame();
        GLCanvas canvas = new GLCanvas();
        final Renderer renderer = new Renderer();
        
        MouseHandler inputMouseHandler = new MouseHandler(renderer);
	canvas.addMouseListener(inputMouseHandler);
	canvas.addMouseMotionListener(inputMouseHandler);
        canvas.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                Model model = renderer.getModel();
                if (model == null)
                    return;
                
                switch (e.getKeyChar()) {
                    case 'w':
                    model.setRenderAsWireframe(!model.isRenderingAsWireframe());
                    break;
                    
                    case 'l':
                    model.setUseLighting(!model.isUsingLighting());
                    break;
                }
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        
        canvas.addGLEventListener(renderer);
        frame.add(canvas);
        frame.add(canvas);
        frame.setSize(600, 600);
        final Animator animator = new Animator(canvas);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              // Run this on another thread than the AWT event queue to
              // make sure the call to Animator.stop() completes before
              // exiting
              new Thread(new Runnable() {
                  public void run() {
                    animator.stop();
                    System.exit(0);
                  }
                }).start();
            }
          });
        frame.setVisible(true);
        animator.start();
    }

    static class Renderer implements GLEventListener
    {
        private GLU glu = new GLU();
        private Model model;
        private iModel3DRenderer modelRenderer;
        
        /** Scale Factor for zooming */
        private float scaleAll = 1.0f;
        
        /** Rotation factor around X */
        private float rotX = 0.0f;
        
        /** Rotation factor around Y */        
        private float rotY = 0.0f;
        
        /** A mouse point */
        private Point mousePoint = new Point();
        
        /** The closest that one can zoom in to */
        private static final float MIN_SCALE = 0.1f;
        
        /** The farthest that one can zoom out to */
        private static final float MAX_SCALE = 10000.9f;
        
        /** The radius of the boundary of the model that is loaded */
        private float radius = 1.0f;
        
        /** Ambient light array */
        float[] lightAmbient = {0.3f, 0.3f, 0.3f, 1.0f};
        
        /** Diffuse light array */
        float[] lightDiffuse = {0.5f, 0.5f, 0.5f, 1.0f};
        
        /** Specular light array */
        float[] lightSpecular = {0.5f, 0.5f, 0.5f, 1.0f};
 
        /**
         * Display method
         * 
         * @param gLDrawable
         */
        public void display(GLAutoDrawable gLDrawable)
        {
            final GL gl = gLDrawable.getGL();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            
            glu.gluLookAt(0,0,10, 0,0,0, 0,1,0);
        
            // Make sure the rotations don't keep growing
            if (Math.abs(rotX) >= 360.0f) rotX = 0.0f;
            if (Math.abs(rotY) >= 360.0f) rotY = 0.0f;
            
            // Draw the scene (by default, the lighting, material and textures 
            // are enabled/disabled within the renderer for the model)
            gl.glPushMatrix();
                // Scale the model (used for zooming purposes)
                gl.glScalef(scaleAll, scaleAll, scaleAll);
                
                // Rotate the model based on mouse inputs
                gl.glRotatef(rotY, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(rotX, 0.0f, 1.0f, 0.0f);
        
                // Render the model
                modelRenderer.render(gl, model);
            gl.glPopMatrix();
            
            gl.glFlush();
        }


        /** Called when the display mode has been changed.  <B>!! CURRENTLY UNIMPLEMENTED IN JOGL !!</B>
         * @param gLDrawable The GLDrawable object.
         * @param modeChanged Indicates if the video mode has changed.
         * @param deviceChanged Indicates if the video device has changed.
         */
        public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

        /** Called by the drawable immediately after the OpenGL context is
         * initialized for the first time. Can be used to perform one-time OpenGL
         * initialization such as setup of lights and display lists.
         * @param gLDrawable The GLDrawable object.
         */
        public void init(GLAutoDrawable gLDrawable)
        {
            final GL gl = gLDrawable.getGL();
            
            try
            {
                // Get an instance of the display list renderer a renderer
                modelRenderer = DisplayListRenderer.getInstance();
                
                // Turn on debugging
                modelRenderer.debug(true);
                
                // Call the factory for a model from a local file
                //model = ModelFactory.createModel("C:\\models\\apollo.3ds");
                
                // Call the factory for a model from a jar file
                model = ModelFactory.createModel("net/java/joglutils/model/examples/models/max3ds/apollo.3ds");
                //model = ModelFactory.createModel("net/java/joglutils/model/examples/models/obj/penguin.obj");
                
                // When loading the model, adjust the center to the boundary center
                model.centerModelOnPosition(true);

                model.setUseTexture(true);

                // Render the bounding box of the entire model
                model.setRenderModelBounds(false);

                // Render the bounding boxes for all of the objects of the model
                model.setRenderObjectBounds(false);

                // Make the model unit size
                model.setUnitizeSize(true);

                // Get the radius of the model to use for lighting and view presetting
                radius = model.getBounds().getRadius();
                
            }
            catch (ModelLoadException ex)
            {
                ex.printStackTrace();
            }
            
            // Set the light
            float lightPosition[] = { 0, 50000000, 0, 1.0f };
            float[] model_ambient = {0.5f, 0.5f, 0.5f, 1.0f};
        
            gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
            gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPosition, 0);
            gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);
            gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);
            gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, lightSpecular, 0);

            gl.glEnable(GL.GL_LIGHT0);
            gl.glEnable(GL.GL_LIGHTING);
            gl.glEnable(GL.GL_NORMALIZE);

            gl.glEnable(GL.GL_CULL_FACE);
            gl.glShadeModel(GL.GL_SMOOTH);
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            gl.glClearDepth(1.0f);
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthFunc(GL.GL_LEQUAL);
            gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
            //gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, 0);
            
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
        }


        /** Called by the drawable during the first repaint after the component has
         * been resized. The client can update the viewport and view volume of the
         * window appropriately, for example by a call to
         * GL.glViewport(int, int, int, int); note that for convenience the component
         * has already called GL.glViewport(int, int, int, int)(x, y, width, height)
         * when this method is called, so the client may not have to do anything in
         * this method.
         * @param gLDrawable The GLDrawable object.
         * @param x The X Coordinate of the viewport rectangle.
         * @param y The Y coordinate of the viewport rectanble.
         * @param width The new width of the window.
         * @param height The new height of the window.
         */
        public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height)
        {
            final GL gl = gLDrawable.getGL();

            if (height <= 0) // avoid a divide by zero error!
                height = 1;
            final float h = (float)width / (float)height;
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glOrtho(-1, 1, -1, 1, -50, 50);
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glLoadIdentity();
        }
        
        /** 
         * Get the point at the start of the mouse drag
         * 
         * @param MousePt
         */
        void startDrag( Point MousePt ) {
            mousePoint.x = MousePt.x;
            mousePoint.y = MousePt.y;  
        }       
        
        /**
         * Calculate the delta and rotation values for the dragging of the mouse
         * @param MousePt
         */
        void drag( Point MousePt ) {
            Point delta = new Point();
            delta.x = MousePt.x - mousePoint.x;
            delta.y = MousePt.y - mousePoint.y;

            mousePoint.x = MousePt.x;
            mousePoint.y = MousePt.y;

            rotX += delta.x * 2.0f / scaleAll;
            rotY += delta.y * 2.0f / scaleAll;
        }
        
        /** 
         * Get the point at the start of the mouse zoom action
         * @param MousePt
         */
        void startZoom( Point MousePt ) {
            mousePoint.x = MousePt.x;
            mousePoint.y = MousePt.y;
        }
        
        /** 
         * Caclaulte the scaling parameters for zooming while the zoom drag
         * is ongoing
         * 
         * @param MousePt
         */
        void zoom( Point MousePt ) {       
            Point delta = new Point();
            delta.x = MousePt.x - mousePoint.x;
            delta.y = MousePt.y - mousePoint.y;

            mousePoint.x = MousePt.x;
            mousePoint.y = MousePt.y;

            float addition = -(delta.x + delta.y) / 250.0f;

            if (addition < 0.0  &&  (scaleAll+addition) > MIN_SCALE) {
                scaleAll += addition;            
            } 

            if (addition > 0.0  &&  (scaleAll+addition) < MAX_SCALE) {
                scaleAll += addition;
            }           
        }

        public Model getModel() {
            return model;
        }

        public void setModel(Model model) {
            this.model = model;
        }
    }
    
    /**
     * Moue handler class that allows the user to rotate and zoom on the model
     * 
     * 
     */
    static class MouseHandler extends MouseInputAdapter {
        private Renderer renderer;

        /**
         * Creates a new instance of the moouse handler
         * 
         * @param renderer
         */
        public MouseHandler(Renderer renderer) {
            //System.out.println(" Mouse Handler ");
            this.renderer = renderer;
        }


        /**
         * Handles the mouse click events
         * 
         * @param e
         */
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                //System.out.println(" ---> RIGHT MOUSE BUTTON CLICKED ");
            }
        }


        /**
         * Handles the mouse press events
         * 
         * @param mouseEvent
         */
        public void mousePressed(MouseEvent mouseEvent) {
            if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
                //System.out.println(" ---> LEFT MOUSE BUTTON PRESSED ");            
                renderer.startDrag(mouseEvent.getPoint());
            } else if (SwingUtilities.isMiddleMouseButton(mouseEvent)) {
                //System.out.println(" ---> MIDDLE MOUSE BUTTON PRESSED ");            
                renderer.startZoom(mouseEvent.getPoint());
            }
        }


        /**
         * Handles the mouse drag events
         * 
         * @param mouseEvent
         */
        public void mouseDragged(MouseEvent mouseEvent) {
            if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
                //System.out.println(" ---> LEFT MOUSE BUTTON DRAGGED ");                        
                renderer.drag(mouseEvent.getPoint());
            } else if (SwingUtilities.isMiddleMouseButton(mouseEvent)) {
                //System.out.println(" ---> MIDDLE MOUSE BUTTON DRAGGED ");                                    
                renderer.zoom(mouseEvent.getPoint());
            }
        }
    }
}
