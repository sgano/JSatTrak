/*
 * Copyright (c) 2006 Erik Tollerud (erik.tollerud@gmail.com) All Rights Reserved.
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
 * The names of Erik Tollerud, Sun Microsystems, Inc. or the names of
 * contributors may not be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *    
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. ERIK TOLLERUD,
 * SUN MICROSYSTEMS, INC. ("SUN"), AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL ERIK
 * TOLLERUD, SUN, OR SUN'S LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT 
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF BEN
 * CHAPPELL OR SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *   
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

package net.java.joglutils;

import javax.media.opengl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.sun.opengl.util.Animator;

/**
 * A JFrame containing a heavyweight {@link GLCanvas} with a single attached {@link GLEventListener}. 
 * Note: the default close operation has been changed to exit rather than dispose.
 * @author Erik J. Tollerud
 */
public class GLJFrame extends JFrame {
    private GLEventListener listener;
    private GLCapabilities caps;
    private GLCapabilitiesChooser chooser;
    private Animator animator;
    private GLContext contextToShareWith;
    
    /**
     * Creates new form GLJFrame
     * @param listener the GLEventListener to attach to the GLCanvas
     */
    public GLJFrame(GLEventListener listener) {
        this("OpenGL Window", listener);
    }
    /**
     * Creates new form GLJFrame
     * @param title the title for the window
     * @param listener the GLEventListener to attach to the GLCanvas
     */
    public GLJFrame(String title, GLEventListener listener) {
        super(title);
        this.listener = listener;
        this.caps = new GLCapabilities();
        this.chooser = null; //can be null because that will choose the default
        initComponents();
        ((GLCanvas)mainCanvas).addGLEventListener(listener);
        animator = null;
        this.contextToShareWith = null;
    }
    /**
     * Creates new form GLJFrame
     * @param caps the GLCapabilities to request for the GLCanvas
     * @param chooser the capabilities chooser to use in creating the GLCanvas on this frame
     * @param title the title for the window
     * @param listener the GLEventListener to attach to the GLCanvas
     * @param contextToShareWith the context to share with
     */
    public GLJFrame(String title, GLEventListener listener, GLCapabilities caps,GLCapabilitiesChooser chooser,GLContext contextToShareWith) {
        super(title);
        this.listener = listener;
        this.caps = caps;
        this.chooser = chooser;
        this.contextToShareWith = contextToShareWith;
        initComponents();
        ((GLCanvas)mainCanvas).addGLEventListener(listener);
        animator = null;
    }
    
    /**
     * Creates new form GLJFrame
     * @param title title for the window
     * @param listener the GLEventListener to attach to the GLCanvas
     * @param capabilities the GLCapabilities to request for the GLCanvas
     */
    public GLJFrame(String title, GLEventListener listener, GLCapabilities capabilities) {
        this(title,listener,capabilities,null,null);
    }
    
    /**
     * Creates new form GLJFrame
     * @param title title for the window
     * @param listener the GLEventListener to attach to the GLCanvas
     * @param capabilities the GLCapabilities to request for the GLCanvas
     * @param chooser the capabilities chooser to use in creating the GLCanvas on this frame
     */
    public GLJFrame(String title, GLEventListener listener, GLCapabilities capabilities, GLCapabilitiesChooser chooser) {
        this(title,listener,capabilities,chooser,null);
    }
    
    /**
     * Creates new form GLJFrame
     * @param listener the GLEventListener to attach to the GLCanvas
     * @param contextToShareWith the context to share with
     * @see javax.media.opengl.GLCanvas#javax.media.opengl.GLCanvas(javax.media.opengl.GLCapabilities,javax.media.opengl.GLCapabilitiesChooser,javax.media.opengl.GLContext,javax.media.opengl.GraphicsDevice)
     */
    public GLJFrame(GLEventListener listener, GLContext contextToShareWith) {
        this(listener);
        this.contextToShareWith = contextToShareWith;
        
    }
    /**
     * Creates new form GLJFrame
     * @param listener the GLEventListener to attach to the GLCanvas
     * @param capabilities the GLCapabilities to request for the GLCanvas
     */
    public GLJFrame(GLEventListener listener, GLCapabilities capabilities) {
        this("OpenGL Window", listener, capabilities);
    }
    /**
     * Creates new form GLJFrame
     * @param listener the GLEventListener to attach to the GLCanvas
     * @param width the horizontal size for the GLCanvas in pixels
     * @param height the vertical size for the GLCanvas in pixels
     */
    public GLJFrame(GLEventListener listener, int width, int height) {
        this("OpenGL Window",listener);
        mainCanvas.setSize(width,height);
        this.pack();
    }
    /**
     * Creates new form GLJFrame
     * @param title the title for the window
     * @param listener the GLEventListener to attach to the GLCanvas
     * @param width the horizontal size for the GLCanvas in pixels
     * @param height the vertical size for the GLCanvas in pixels
     */
    public GLJFrame(String title, GLEventListener listener, int width, int height) {
        this(title,listener);
        mainCanvas.setSize(width,height);
        this.pack();
    }
    /**
     * Creates new form GLJFrame
     * @param listener the GLEventListener to attach to the GLCanvas
     * @param fullscreen if true, this window is generated in fullscreen mode
     */
    public GLJFrame(GLEventListener listener, boolean fullscreen) {
        this("OpenGL Window",listener);
        this.setFullscreen(fullscreen);
    }
    /**
     * Creates new form GLJFrame
     * @param title the title for the window
     * @param listener the GLEventListener to attach to the GLCanvas
     * @param fullscreen if true, this window is generated in fullscreen mode
     */
    public GLJFrame(String title, GLEventListener listener, boolean fullscreen) {
        this(title,listener);
        this.setFullscreen(fullscreen);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        mainCanvas = new GLCanvas(caps,chooser,contextToShareWith,null);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        mainCanvas.setSize(500,500);
        getContentPane().add(mainCanvas, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        this.mainCanvas.setSize(this.getRootPane().getSize());
    }//GEN-LAST:event_formComponentResized
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Canvas mainCanvas;
    // End of variables declaration//GEN-END:variables
    /**
     * Sets the event listener attached to the GLcanvas.  Note that this method does not repaint this component, so it will not immediately update.
     * @param listener the GLEventListener to attach to the GLCanvas
     */
    public void setGLEventListener(GLEventListener listener) {
        ((GLCanvas)this.mainCanvas).removeGLEventListener(this.listener);
        this.listener = listener;
        ((GLCanvas)this.mainCanvas).addGLEventListener(listener);
        
    }
    /**
     * Retrieves the event listener for the GLJFrame
     * @return the GLEventListener associated with the GLCanvas
     */
    public GLEventListener getGLEventListener() {
        return this.listener;
    }
    /**
     * Sets the fullscreen status of this window.  If full screen exclusive mode is not supported, fullscreen is emulated by swing (see <a href="http://java.sun.com/docs/books/tutorial/extra/fullscreen/exclusivemode.html"> Full-Screen Exclusive Mode Tutorial </a>). If fullscreen is to be invoked under Windows, it is recommended that the VM be run with -Dsun.java2d.noddraw=true
     * @param fs the mode to set this window to - true for full screen, false for windowed mode
     * @return true if full screen exclusive mode is supported, false if not.
     */
    public boolean setFullscreen(boolean fs) {
        //TODO: fix to make double-buffered and return to non-fs mode properly (use initComponents()?)
        GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        boolean visible = this.isVisible();
        if (fs) {
            this.dispose();
            this.setUndecorated(true);
            this.setResizable(false);
            try{
                dev.setFullScreenWindow(this);
            } catch (Exception e) {
                dev.setFullScreenWindow(null);
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,"Could not enter fullscreen exclusive mode.","Fullscreen error",JOptionPane.ERROR_MESSAGE);
            }
            this.setVisible(visible);
        } else {
            this.dispose();
            this.setUndecorated(false);
            this.setResizable(true);
            dev.setFullScreenWindow(null);
            this.setVisible(visible);
        }
        return dev.isFullScreenSupported();
    }
    /**
     * Resizes this GLJFrame to match a GLCanvas of the specified size.  This has no effect if fullscreen mode is active.
     * @param width the new horizontal size for the GLCanvass in pixels
     * @param height the new vertical size for the GLCanvass in pixels
     */
    public void setSize(int width, int height) {
        
        java.awt.GraphicsDevice dev = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (dev.getFullScreenWindow() != this) {
            mainCanvas.setSize(width,height);
            this.pack();
            mainCanvas.repaint();
        }
    }
    /**
     * Resizes this GLJFrame to match a GLCanvas of the specified size. This has no effect if fullscreen mode is active.
     * @param d the new size for the GLCanvass in pixels
     */
    public void setSize(java.awt.Dimension d) {
        java.awt.GraphicsDevice dev = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (dev.getFullScreenWindow() != this) {
            mainCanvas.setSize(d);
            this.pack();
        }
    }
    /**
     * Determines if the Frame is the Full-Screen Exclusive Mode window.
     * @return true if this window is in fullscreen mode
     */
    public boolean isFullscreen() {
        java.awt.GraphicsDevice dev = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        return (dev.getFullScreenWindow() == this);
    }
    /**
     * The {@link GLCapabilities} used with the GLCanvas.
     * @return a copy of the capabilities used to generate the attached GLCanvas
     */
    public GLCapabilities getGLCapabilities() {
        return (GLCapabilities)this.caps.clone();
    }
    /**
     * Rebuilds the GLCanvas with the specified capbilities. Will dispose and re-create the JFrame and GLCanvas with the new capabilities.
     * @param caps the capabilities to be copied into this GLJFrame
     */
    public void setGLCapabilities(GLCapabilities caps) {
        //get old setup
        boolean vis = this.isVisible();
        this.caps = (GLCapabilities)caps.clone();
        boolean fs = this.isFullscreen();
        java.awt.Dimension d = this.mainCanvas.getSize();
        
        this.dispose();
        
        //re-form canvas with new caps
        this.mainCanvas = new GLCanvas(caps);
        this.getContentPane().removeAll();
        this.add(mainCanvas);
        
        
        mainCanvas.setSize(d);
        
        GLCanvas glc = (GLCanvas) mainCanvas;
        //re-apply GLEventListener
        glc.addGLEventListener(listener);
        //re-apply input listeners from the JPanel listeners
        for(InputMethodListener l : this.getInputMethodListeners())
            glc.addInputMethodListener(l);
        for(KeyListener l : this.getKeyListeners())
            glc.addKeyListener(l);
        for(MouseListener l : this.getMouseListeners())
            glc.addMouseListener(l);
        for(MouseMotionListener l : this.getMouseMotionListeners())
            glc.addMouseMotionListener(l);
        for(MouseWheelListener l : this.getMouseWheelListeners())
            glc.addMouseWheelListener(l);
        
        this.setFullscreen(fs);
        this.setVisible(vis);
        
    }
    /**
     * Repaint the JFrame and render the GLCanvas if no animator is present.  If animator is attached and running, only repaints the JFrame.
     */
    public void repaint() {
        super.repaint();
        if (this.animator == null)
            ((GLCanvas)this.mainCanvas).display();
        else if (!this.animator.isAnimating())
            ((GLCanvas)this.mainCanvas).display();
    }
    /**
     * Removes InputMethodListeners, KeyListeners,MouseListeners, MouseMotionListeners, and MouseWheelListeners from this and the GLCanvas.
     */
    public void clearInputListeners() {
        InputMethodListener[] imls = super.getInputMethodListeners();
        KeyListener[] kls = super.getKeyListeners();
        MouseListener[] mls = super.getMouseListeners();
        MouseMotionListener[] mmls = super.getMouseMotionListeners();
        MouseWheelListener[] mwls = super.getMouseWheelListeners();
        for(InputMethodListener l : imls) {
            mainCanvas.removeInputMethodListener(l);
            super.removeInputMethodListener(l);
        }
        
        for(KeyListener l : kls){
            mainCanvas.removeKeyListener(l);
            super.removeKeyListener(l);
        }
        for(MouseListener l : mls){
            mainCanvas.removeMouseListener(l);
            super.removeMouseListener(l);
        }
        for(MouseMotionListener l : mmls){
            mainCanvas.removeMouseMotionListener(l);
            super.removeMouseMotionListener(l);
        }
        for(MouseWheelListener l : mwls){
            mainCanvas.removeMouseWheelListener(l);
            super.removeMouseWheelListener(l);
        }
    }
    /**
     * Generates a new {@link Animator} for updating the GLCanvas, and starts it.
     * @return the generated Animator
     */
    public Animator generateAnimator() {
        if (this.animator != null) {
            this.animator.stop();
            this.animator.remove((GLCanvas)mainCanvas);
        }
        this.animator = new Animator((GLCanvas)mainCanvas);
        this.animator.start();
        return this.animator;
    }
    /**
     * Retrieves the attached Animator connected to the GLCanvas.
     * @return The Animator attached to this, or null if there is no Animator
     */
    public Animator getAnimator() {
        return this.animator;
    }
    /**
     * Specifies an {@link Animator} for updating the GLCanvas, and starts it.
     * @param anim Animator to use to animate the GLCanvas
     */
    public void setAnimator(Animator anim) {
        this.setAnimator(anim,true);
    }
    /**
     * Specifies an {@link Animator} for updating the GLCanvas, and optionally starts it.
     * @param anim Animator  to use to animate the GLCanvas
     * @param start starts the animator if true, just sets it if not
     */
    public void setAnimator(Animator anim, boolean start) {
        if (this.animator != null) {
            this.animator.stop();
            this.animator.remove((GLCanvas)mainCanvas);
        }
        this.animator = anim;
        this.animator.add((GLCanvas)mainCanvas);
        if (start)
            this.animator.start();
    }
    /**
     * Stops and removes the {@link Animator}.
     */
    public void removeAnimator() {
        this.animator.stop();
        this.animator.remove((GLCanvas)mainCanvas);
        this.animator = null;
    }
    /**
     * Determines if this GLJFrame is running on an {@link Animator}.
     * @return true if the GLJFrame has an attached animator.
     */
    public boolean isAnimated() {
        return (animator!=null);
    }
    
    /**
     * Removes the specified key listener so that it no longer receives key events from this component and the GLCanvas. This method performs no function, nor does it throw an exception, if the listener specified by the argument was not previously added to this component. if the listener is null, no exception is thrown and no action is performed.
     * @param l the key listener.
     */
    public void removeKeyListener(KeyListener l) {
        super.removeKeyListener(l);
        mainCanvas.removeKeyListener(l);
    }
    
    /**
     * Adds the specified key listener to receive key events from this component and the GLCanvas. If l is null, no exception is thrown and no action is performed.
     * @param l the key listener.
     */
    public void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
        mainCanvas.addKeyListener(l);
    }
    
    /**
     * Removes the specified mouse listener so that it no longer receives mouse events from this component and the GLCanvas. This method performs no function, nor does it throw an exception, if the listener specified by the argument was not previously added. if the listener is null, no exception is thrown and no action is performed.
     * @param l the mouse listener
     */
    public void removeMouseListener(MouseListener l) {
        super.removeMouseListener(l);
        mainCanvas.removeMouseListener(l);
    }
    
    /**
     * Adds the specified mouse listener to receive mouse events from this component and the GLCanvas. if the listener is null, no exception is thrown and no action is performed.
     * @param l the mouse listener
     */
    public void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
        mainCanvas.addMouseListener(l);
    }
    
    /**
     * Removes the specified mouse wheel listener so that it no longer receives mouse wheel events from this component and the GLCanvas. This method performs no function, nor does it throw an exception, if the listener specified by the argument was not previously added. If l is null, no exception is thrown and no action is performed.
     * @param l the mouse wheel listener
     */
    public void removeMouseWheelListener(MouseWheelListener l) {
        super.removeMouseWheelListener(l);
        mainCanvas.removeMouseWheelListener(l);
    }
    
    /**
     * Removes the specified mouse motion listener so that it no longer receives mouse motion events from this component and the GLCanvas. This method performs no function, nor does it throw an exception, if the listener specified by the argument was not previously added. if the listener is null, no exception is thrown and no action is performed.
     * @param l the mouse motion listener.
     */
    public void removeMouseMotionListener(MouseMotionListener l) {
        super.removeMouseMotionListener(l);
        mainCanvas.removeMouseMotionListener(l);
    }
    
    /**
     *
     * Adds the specified mouse wheel listener to receive mouse wheel events from this component and the GLCanvas.
     *
     * For information on how mouse wheel events are dispatched, see the class description for {@link MouseWheelEvent}.
     *
     * If l is null, no exception is thrown and no action is performed.
     * @param l the mouse wheel listener
     */
    public void addMouseWheelListener(MouseWheelListener l) {
        super.addMouseWheelListener(l);
        mainCanvas.addMouseWheelListener(l);
    }
    
    /**
     * Adds the specified mouse motion listener to receive mouse motion events from this component and the GLCanvas. if the listener is null, no exception is thrown and no action is performed.
     * @param l the mouse motion listener.
     */
    public void addMouseMotionListener(MouseMotionListener l) {
        super.addMouseMotionListener(l);
        mainCanvas.addMouseMotionListener(l);
    }
    
    /**
     * Removes the specified input method listener so that it no longer receives input method events from this component and the GLCanvas. This method performs no function, nor does it throw an exception, if the listener specified by the argument was not previously added. if the listener is null, no exception is thrown and no action is performed.
     * @param l the input method listener
     */
    public void removeInputMethodListener(java.awt.event.InputMethodListener l) {
        super.removeInputMethodListener(l);
        mainCanvas.removeInputMethodListener(l);
    }
    
    /**
     * Adds the specified input method listener to receive input method events from this component and the GLCanvas. A component will only receive input method events from input methods if it also overrides getInputMethodRequests to return an InputMethodRequests instance. if the listener is null, no exception is thrown and no action is performed.
     * @param l the input method listener
     */
    public void addInputMethodListener(java.awt.event.InputMethodListener l) {
        super.addInputMethodListener(l);
        mainCanvas.addInputMethodListener(l);
    }
    /**
     * Gets the GL pipeline for the canvas in this GLJPane.
     * @return the GL pipeline associated with this GLCanvas
     * @see javax.media.opengl.GLCanvas#getGL()
     */
    public GL getGL() {
        return ((GLCanvas)mainCanvas).getGL();
    }
    /**
     *  Sets the GL pipeline for the canvas in this GLJPane.
     * @see javax.media.opengl.GLCanvas#setGL(javax.media.opengl.GL)
     * @param gl The pipeline to attach to this GLCanvas
     */
    public void setGL(GL gl) {
        ((GLCanvas)mainCanvas).setGL(gl);
    }
    /**
     * Retrieves the GLContext associated with the GLCanvas on this GLJFrame
     * @see javax.media.opengl.GLCanvas#getContext()
     * @return the associated GLContext
     */
    public GLContext getContext() {
        return ((GLCanvas)mainCanvas).getContext();
    }
    
    /**
     * Retrieves a GLAutoDrawable view of the associated GLCanvas
     * @return a GLAutoDrawable corresponding to the GLCanvas on this GLJFrame
     */
    public GLAutoDrawable getAutoDrawable() {
        return (GLAutoDrawable)mainCanvas;
    }
    
}
