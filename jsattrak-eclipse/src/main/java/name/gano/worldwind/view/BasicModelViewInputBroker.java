/*
 * Modified OrbitViewInputBroker.java, to allow view to be centered on a point not the center of the Earth.
 *  Shawn Gano, 26 Feb 2008
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

package name.gano.worldwind.view;


/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;

import java.awt.*;
import java.awt.event.*;

/**
 * @author dcollins
 * @version $Id: OrbitViewInputBroker.java 3548 2007-11-16 06:27:16Z dcollins $
 */
public class BasicModelViewInputBroker
        implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener
{
    private BasicModelView view;
    private WorldWindow wwd;
    
    // sensitivity in moust movements
    float offsetMaxScale = 10000000.0f;
    float zoomScale = 30000000.0f;
    

    // Current mouse state.
    private java.awt.Point mousePoint = null;
    private Position selectedPosition = null;
    private final Integer[] POLLED_KEYS =
        {
            KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
            KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_PAGE_UP,
            KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_ADD, KeyEvent.VK_EQUALS,
            KeyEvent.VK_SUBTRACT, KeyEvent.VK_MINUS
        };
//    private KeyPollTimer keyPollTimer = new KeyPollTimer(25, Arrays.asList(POLLED_KEYS),
//        new ActionListener()
//        {
//            public void actionPerformed(ActionEvent actionEvent)
//            {
//                if (actionEvent == null)
//                    return;
//
//                Object source = actionEvent.getSource();
//                if (source == null || !(source instanceof Integer))
//                    return;
//
//                keyPolled((Integer) source, actionEvent.getModifiers());
//            }
//        });

    public BasicModelViewInputBroker()
    {
    }

    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    public void setWorldWindow(WorldWindow newWorldWindow)
    {
        this.wwd = newWorldWindow;

        if (this.wwd == null || this.wwd.getView() == null || !(this.wwd.getView() instanceof BasicModelView))
            this.view = null;
        else
            this.view = (BasicModelView) this.wwd.getView();
    }

   

    private Point getMousePoint()
    {
        return this.mousePoint;
    }

    private void updateMousePoint(MouseEvent event)
    {
        if (event != null)
        {
            if (this.wwd instanceof Component)
                this.mousePoint = constrainPointToComponentBounds(event.getX(), event.getY(), (Component) this.wwd);
            else
                this.mousePoint = new Point(event.getX(), event.getY());
        }
        else
        {
            this.mousePoint = null;
        }
    }

    private Point constrainPointToComponentBounds(int x, int y, Component c)
    {
        if (c != null)
        {
            if (x < 0)
                x = 0;
            if (y < 0)
                y = 0;

            if (x > c.getWidth())
                x = c.getWidth();
            if (y > c.getHeight())
                y = c.getHeight();
        }

        return new Point(x, y);
    }


    private void updateSelectedPosition()
    {
        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
        if (pickedObjects != null
         && pickedObjects.getTopPickedObject() != null
         && pickedObjects.getTopPickedObject().isTerrain())
        {
            this.selectedPosition = pickedObjects.getTopPickedObject().getPosition();
        }
        else
        {
            this.selectedPosition = null;
        }
    }

    private void clearSelectedPosition()
    {
        this.selectedPosition = null;
    }


    public void keyTyped(KeyEvent keyEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (keyEvent == null)
            return;

//        this.keyPollTimer.keyTyped(keyEvent);
    }

    public void keyPressed(KeyEvent keyEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (keyEvent == null)
            return;

//        this.keyPollTimer.keyPressed(keyEvent);
    }

    public void keyReleased(KeyEvent keyEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (keyEvent == null)
            return;

//        this.keyPollTimer.keyReleased(keyEvent);

        int keyCode = keyEvent.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE)
        {
            if (this.view.isAnimating())
                this.view.stopAnimations();
        }
        else if (keyCode == KeyEvent.VK_N)
        {
//            this.view.applyStateIterator(ScheduledOrbitViewStateIterator.createHeadingIterator(
//                this.view,
//                Angle.ZERO)); // Reset heading.
        }
        else if (keyCode == KeyEvent.VK_R)
        {
//            this.view.applyStateIterator(ScheduledOrbitViewStateIterator.createHeadingAndPitchIterator(
//                this.view,
//                Angle.ZERO,   // Reset heading.
//                Angle.ZERO)); // Reset pitch.
        }
    }

    public void mouseClicked(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
        if (pickedObjects == null
            || pickedObjects.getTopPickedObject() == null
            || !pickedObjects.getTopPickedObject().isTerrain())
            return;

        PickedObject top = pickedObjects.getTopPickedObject();
        Position topPosition = top.getPosition();

        final double SMOOTHING = 0.9;
        final boolean DO_COALESCE = false;

    }

    int mouseButton,mouseButtonMod;
    
    public void mousePressed(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        mouseButton = mouseEvent.getButton();
        mouseButtonMod = mouseEvent.getModifiersEx();
        
        
        updateMousePoint(mouseEvent);
        updateSelectedPosition();
    }

    public void mouseReleased(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        updateMousePoint(mouseEvent);
        clearSelectedPosition();
    }

    public void mouseEntered(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;
    }

    public void mouseExited(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;
    }

    public void mouseDragged(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        // Store previous mouse point.
        java.awt.Point prevMousePoint = getMousePoint();
        // Update mouse point.
        updateMousePoint(mouseEvent);
        // Store new mouse point.
        java.awt.Point curMousePoint = getMousePoint();
        // Compute mouse movement.
        java.awt.Point mouseMove = null;
        
        if (curMousePoint != null && prevMousePoint != null)
        {
            mouseMove = new java.awt.Point(
                curMousePoint.x - prevMousePoint.x,
                curMousePoint.y - prevMousePoint.y);
            
            // CONTROLS FOR THIS MODEL VIEW TYPE
            
            // button 1 - controls lat/long spin
            if (mouseButtonMod == MouseEvent.BUTTON1_DOWN_MASK)
            {
                // change lat (theta1)  -- NOT QUITE RIGHT... but should move!!
                float theta1f = 360.0f * (float) (curMousePoint.y - prevMousePoint.y) / (float) view.getViewport().height;
                view.addToTheta1(theta1f);
                
                float theta3f = 360.0f * (float) (curMousePoint.x - prevMousePoint.x) / (float) view.getViewport().width;
                view.addToTheta3(theta3f);
                
            }
            
            System.out.println("here:" + mouseEvent.getButton());
            if (mouseButtonMod == MouseEvent.BUTTON2_DOWN_MASK)
            {
                //System.out.println("here:");
                // zoom!
                float meters = zoomScale * (float) (curMousePoint.y - prevMousePoint.y) / (float) view.getViewport().height;
                view.addToRadiusAboutCenter(meters);
                //System.out.println("here:" + meters);
            }
            
            if (mouseButtonMod == MouseEvent.BUTTON3_DOWN_MASK)
            {
                //System.out.println("here:" + mouseEvent.getButton());
                
                float theta2f = -360.0f * (float) (curMousePoint.x - prevMousePoint.x) / (float) view.getViewport().width;
                view.addToTheta2(theta2f);
                
            }
            
            if(mouseButtonMod == MouseEvent.CTRL_DOWN_MASK + MouseEvent.BUTTON1_DOWN_MASK)
            {
                //System.out.println("YES:" + mouseButtonMod);
                
                float dx = -offsetMaxScale * (float) (curMousePoint.x - prevMousePoint.x) / (float) view.getViewport().width;
                view.addToXOffsetAboutCenter(dx);
                
                float dy = offsetMaxScale * (float) (curMousePoint.y - prevMousePoint.y) / (float) view.getViewport().width;
                view.addToZOffsetAboutCenter(dy);
            }
            
            if(mouseButtonMod == MouseEvent.CTRL_DOWN_MASK + MouseEvent.BUTTON2_DOWN_MASK)
            {
                //System.out.println("YES:" + mouseButtonMod);
                
                float dx = offsetMaxScale * (float) (curMousePoint.x - prevMousePoint.x) / (float) view.getViewport().width;
                
                float dy = offsetMaxScale * (float) (curMousePoint.y - prevMousePoint.y) / (float) view.getViewport().width;
                
                // use which ever has changed the most
                if(Math.abs(dx) > Math.abs(dy))
                {
                    view.addToYOffsetAboutCenter(dx);
                }
                else
                {
                    view.addToYOffsetAboutCenter(dy);
                }
                
                
            }
            
            if(mouseButtonMod == MouseEvent.CTRL_DOWN_MASK + MouseEvent.BUTTON3_DOWN_MASK)
            {
               // moved to button 1 up/down
               // float dx = offsetMaxScale * (float) (curMousePoint.x - prevMousePoint.x) / (float) view.getViewport().width;
               // view.addToZOffsetAboutCenter(dx);
            }
              
        } // cursor not null
        
    } // mouse dragged

    public void mouseMoved(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        updateMousePoint(mouseEvent);
    }

    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseWheelEvent == null)
            return;

        int wheelRotation = mouseWheelEvent.getWheelRotation();
        double wheelDirection = Math.signum(wheelRotation);
//        double newZoom = this.computeNewZoom(this.view, wheelDirection, false);
//        this.setZoom(this.view, newZoom);
    }

    public void focusGained(FocusEvent focusEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;
    }

    public void focusLost(FocusEvent focusEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (focusEvent == null)
            return;

        this.view.stopAnimations();
        //this.keyPollTimer.stop();
    }

   
   
}

