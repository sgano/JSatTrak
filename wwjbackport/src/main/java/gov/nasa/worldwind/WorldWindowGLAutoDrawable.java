/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.cache.TextureCache;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.ScreenCreditController;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.dashboard.DashboardController;

import javax.media.opengl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;

/**
 * A non-platform specific {@link WorldWindow} class. This class can be aggregated into platform-specific classes to
 * provide the core functionality of World Wind.
 *
 * @author Tom Gaskins
 * @version $Id: WorldWindowGLAutoDrawable.java 13506 2010-06-30 01:29:07Z tgaskins $
 */
public class WorldWindowGLAutoDrawable extends WorldWindowImpl implements WorldWindowGLDrawable, GLEventListener
{
    private GLAutoDrawable drawable;
    private DashboardController dashboard;
    private boolean shuttingDown = false;
    private Timer redrawTimer;
    private boolean firstInit = true;

    /** Construct a new <code>WorldWindowGLCanvase</code> for a specified {@link GLDrawable}. */
    public WorldWindowGLAutoDrawable()
    {
        SceneController sc = this.getSceneController();
        if (sc != null)
            sc.addPropertyChangeListener(this);
    }

    public void initDrawable(GLAutoDrawable glAutoDrawable)
    {
        if (glAutoDrawable == null)
        {
            String msg = Logging.getMessage("nullValue.DrawableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.drawable = glAutoDrawable;
        this.drawable.setAutoSwapBufferMode(false);
        this.drawable.addGLEventListener(this);
    }

    public void initTextureCache(TextureCache textureCache)
    {
        if (textureCache == null)
        {
            String msg = Logging.getMessage("nullValue.TextureCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setTextureCache(textureCache);
    }

    public void endInitialization()
    {
        initializeCreditsController();
        this.dashboard = new DashboardController(this, (Component) this.drawable);
    }

    protected void initializeCreditsController()
    {
        new ScreenCreditController((WorldWindow) this.drawable);
    }

    @Override
    public void shutdown()
    {
        this.shuttingDown = true;
        this.drawable.display(); // Invokes a repaint, where the rest of the shutdown work is done.
    }

    protected void doShutdown()
    {
        super.shutdown();
        this.drawable.removeGLEventListener(this);
        if (this.dashboard != null)
            this.dashboard.dispose();
        this.shuttingDown = false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        if (propertyChangeEvent == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyChangeEventIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.drawable != null)
            this.drawable.repaint(); // Queue a JOGL repaint request.
    }

    protected String[] getRequiredOglFunctions()
    {
        return new String[] {"glActiveTexture", "glClientActiveTexture"};
    }

    protected String[] getRequiredOglExtensions()
    {
        return new String[] {};
    }

    /**
     * See {@link GLEventListener#init(GLAutoDrawable)}.
     *
     * @param glAutoDrawable the drawable
     */
    public void init(GLAutoDrawable glAutoDrawable)
    {
        for (String funcName : this.getRequiredOglFunctions())
        {
            if (!glAutoDrawable.getGL().isFunctionAvailable(funcName))
            {
                //noinspection ThrowableInstanceNeverThrown
                this.callRenderingExceptionListeners(new WWAbsentRequirementException(funcName + " not available"));
            }
        }

        for (String extName : this.getRequiredOglExtensions())
        {
            if (!glAutoDrawable.getGL().isExtensionAvailable(extName))
            {
                //noinspection ThrowableInstanceNeverThrown
                this.callRenderingExceptionListeners(new WWAbsentRequirementException(extName + " not available"));
            }
        }

        if (this.firstInit)
            this.firstInit = false;
        else
            this.reinitialize(glAutoDrawable);

//        this.drawable.setGL(new DebugGL(this.drawable.getGL())); // uncomment to use the debug drawable
    }

    protected void reinitialize(GLAutoDrawable glAutoDrawable)
    {
        // Clear the texture cache if the window is reinitializing, most likely with a new gl hardware context.
        if (this.getTextureCache() != null)
            this.getTextureCache().clear();

        this.getSceneController().reinitialize();

        // TODO: ...convey/enforce the contract of just disposing of system resources, not the object instance.
        if (this.getModel() != null && this.getModel().getLayers() != null)
        {
            for (Layer layer : this.getModel().getLayers())
            {
                layer.dispose();
            }
        }
    }

    /**
     * See {@link GLEventListener#display(GLAutoDrawable)}.
     *
     * @param glAutoDrawable the drawable
     *
     * @throws IllegalStateException if no {@link SceneController} exists for this canvas
     */
    public void display(GLAutoDrawable glAutoDrawable)
    {
        // Performing shutdown here in order to do so with a current GL context for GL resource disposal.
        if (this.shuttingDown)
        {
            try
            {
                this.doShutdown();
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, Logging.getMessage(
                    "WorldWindowGLCanvas.ExceptionWhileShuttingDownWorldWindow"), e);
            }
            return;
        }

        try
        {
            SceneController sc = this.getSceneController();
            if (sc == null)
            {
                Logging.logger().severe("WorldWindowGLCanvas.ScnCntrllerNullOnRepaint");
                throw new IllegalStateException(Logging.getMessage("WorldWindowGLCanvas.ScnCntrllerNullOnRepaint"));
            }

            Position positionAtStart = this.getCurrentPosition();
            PickedObject selectionAtStart = this.getCurrentSelection();

            try
            {
                this.callRenderingListeners(new RenderingEvent(this.drawable, RenderingEvent.BEFORE_RENDERING));
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE,
                    Logging.getMessage("WorldWindowGLAutoDrawable.ExceptionDuringGLEventListenerDisplay"), e);
            }

            int redrawDelay = this.doDisplay();
            if (redrawDelay > 0)
            {
                if (this.redrawTimer == null)
                {
                    this.redrawTimer = new Timer(redrawDelay, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent actionEvent)
                        {
                            drawable.repaint();
                            redrawTimer = null;
                        }
                    });
                    redrawTimer.setRepeats(false);
                    redrawTimer.start();
                }
            }

            try
            {
                this.callRenderingListeners(new RenderingEvent(this.drawable, RenderingEvent.BEFORE_BUFFER_SWAP));
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE,
                    Logging.getMessage("WorldWindowGLAutoDrawable.ExceptionDuringGLEventListenerDisplay"), e);
            }

            this.doSwapBuffers(this.drawable);

            Double frameTime = sc.getFrameTime();
            if (frameTime != null)
                this.setValue(PerformanceStatistic.FRAME_TIME, frameTime);

            Double frameRate = sc.getFramesPerSecond();
            if (frameRate != null)
                this.setValue(PerformanceStatistic.FRAME_RATE, frameRate);

            // Dispatch the rendering exceptions accumulated by the SceneController during this frame to our
            // RenderingExceptionListeners.
            Iterable<Throwable> renderingExceptions = sc.getRenderingExceptions();
            if (renderingExceptions != null)
            {
                for (Throwable t : renderingExceptions)
                {
                    if (t != null)
                        this.callRenderingExceptionListeners(t);
                }
            }

            this.callRenderingListeners(new RenderingEvent(this.drawable, RenderingEvent.AFTER_BUFFER_SWAP));

            // Position and selection notification occurs only on triggering conditions, not same-state conditions:
            // start == null, end == null: nothing selected -- don't notify
            // start == null, end != null: something now selected -- notify
            // start != null, end == null: something was selected but no longer is -- notify
            // start != null, end != null, start != end: something new was selected -- notify
            // start != null, end != null, start == end: same thing is selected -- don't notify

            Position positionAtEnd = this.getCurrentPosition();
            if (positionAtStart != null || positionAtEnd != null)
            {
                // call the listener if both are not null or positions are the same
                if (positionAtStart != null && positionAtEnd != null && !positionAtStart.equals(positionAtEnd))
                    this.callPositionListeners(new PositionEvent(this.drawable, sc.getPickPoint(),
                        positionAtStart, positionAtEnd));
            }

            PickedObject selectionAtEnd = this.getCurrentSelection();
            if (selectionAtStart != null || selectionAtEnd != null)
            {
                if (selectionAtStart != selectionAtEnd)
                    this.callSelectListeners(new SelectEvent(this.drawable, SelectEvent.ROLLOVER,
                        sc.getPickPoint(), sc.getPickedObjectList()));
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, Logging.getMessage(
                "WorldWindowGLCanvas.ExceptionAttemptingRepaintWorldWindow"), e);
        }
    }

    protected int doDisplay()
    {
        return this.getSceneController().repaint();
    }

    protected void doSwapBuffers(GLAutoDrawable drawable)
    {
        drawable.swapBuffers();
    }

    /**
     * See {@link GLEventListener#reshape(GLAutoDrawable,int,int,int,int)}.
     *
     * @param glAutoDrawable the drawable
     */
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int w, int h)
    {
        // This is apparently necessary to enable the WWJ canvas to resize correctly with JSplitPane.
        ((Component) glAutoDrawable).setMinimumSize(new Dimension(0, 0));
    }

    /**
     * See {@link GLEventListener#displayChanged(GLAutoDrawable,boolean,boolean)}.
     *
     * @param glAutoDrawable the drawable
     */
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1)
    {
        Logging.logger().finest("WorldWindowGLCanvas.DisplayEventListenersDisplayChangedMethodCalled");
    }

    @Override
    public void redraw()
    {
        if (this.drawable != null)
            this.drawable.repaint();
    }

    public void redrawNow()
    {
        if (this.drawable != null)
            this.drawable.display();
    }
}
