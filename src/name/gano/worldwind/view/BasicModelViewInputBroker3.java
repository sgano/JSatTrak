/*
 * BasicModelViewInputBroker3.java
 * 
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
// modification of OrbitViewInputBroker.java  (gov.nasa.worldwind.awt)
// NOTE - inital zoom changes when view first initalized -- BUG
//

package name.gano.worldwind.view;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.view.OrbitView;
import gov.nasa.worldwind.view.ScheduledOrbitViewStateIterator;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;


/**
 * @author dcollins
 * @version $Id: OrbitViewInputBroker.java 5240 2008-04-30 23:56:02Z dcollins $
 */
public class BasicModelViewInputBroker3
        implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener, RenderingListener
{
    private OrbitView view;
    private WorldWindow wwd;
    private ModelViewInputSupport modelViewInputSupport = new ModelViewInputSupport();
    private boolean smoothViewChanges = true;
    private boolean lockHeading = true;
    // Current mouse state.
    private java.awt.Point mousePoint = null;
    private Position selectedPosition = null;
    private final Integer[] POLLED_KEYS =
        {
            KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
            KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_PAGE_UP,
            KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_ADD, KeyEvent.VK_EQUALS,
            KeyEvent.VK_SUBTRACT, KeyEvent.VK_MINUS, KeyEvent.VK_R
        };
    private KeyPollTimer keyPollTimer = new KeyPollTimer(25, Arrays.asList(POLLED_KEYS),
        new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (actionEvent == null)
                    return;

                Object source = actionEvent.getSource();
                if (source == null || !(source instanceof Integer))
                    return;

                keyPolled((Integer) source, actionEvent.getModifiers());
            }
        });
        
    public BasicModelViewInputBroker3()
    {
    }

    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    public void setWorldWindow(WorldWindow newWorldWindow)
    {
        if (newWorldWindow == this.wwd)
            return;

        if (this.wwd != null)
        {
            this.wwd.removeRenderingListener(this);
        }

        this.wwd = newWorldWindow;

        if (this.wwd != null)
        {
            this.wwd.addRenderingListener(this);
        }

        if (this.wwd != null && this.wwd.getView() != null && this.wwd.getView() instanceof OrbitView)
            this.view = (OrbitView) this.wwd.getView();
        else
            this.view = null;

        this.modelViewInputSupport.setOrbitView(this.view);
    }

    public boolean isSmoothViewChanges()
    {
        return this.smoothViewChanges;
    }

    public void setSmoothViewChanges(boolean smoothViewChanges)
    {
        this.smoothViewChanges = smoothViewChanges;
    }

    public boolean isLockHeading()
    {
        return this.lockHeading;
    }

    public void setLockHeading(boolean lockHeading)
    {
        this.lockHeading = lockHeading;
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

    private Position computePositionAtPoint(double mouseX, double mouseY)
    {
        Position position = null;
        if (this.view != null
            && this.wwd != null
            && this.wwd.getModel() != null
            && this.wwd.getModel().getGlobe() != null)
        {
            Globe globe = this.wwd.getModel().getGlobe();
            Line line = this.view.computeRayFromScreenPoint(mouseX, mouseY);
            if (line != null)
            {
                // Attempt to intersect with spheroid of scaled radius.
                // This will simulate dragging the selected position more accurately.
                double eyeElevation = this.view.getEyePosition().getElevation();
                double selectedElevation = this.selectedPosition != null ? this.selectedPosition.getElevation() : 0;
                // Intersect with the scaled spheroid, but only when the eye is not inside that spheroid.
                if (eyeElevation > selectedElevation)
                {
                    Intersection[] intersection = globe.intersect(line, selectedElevation);
                    if (intersection != null && intersection.length != 0)
                        position = globe.computePositionFromPoint(intersection[0].getIntersectionPoint());
                }
            }
        }
        return position;
    }

    public void keyPolled(int keyCode, int modifiers)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        int slowMask = (modifiers & InputEvent.ALT_DOWN_MASK);
        boolean slow = slowMask != 0x0;

//        if (areModifiersExactly(modifiers, slowMask))
//        {
//            if (isLockHeading())
//            {
//                double sinHeading = view.getHeading().sin();
//                double cosHeading = view.getHeading().cos();
//                double latFactor = 0;
//                double lonFactor = 0;
//                if (keyCode == KeyEvent.VK_LEFT)
//                {
//                    latFactor = sinHeading;
//                    lonFactor = -cosHeading;
//                }
//                else if (keyCode == KeyEvent.VK_RIGHT)
//                {
//                    latFactor = -sinHeading;
//                    lonFactor = cosHeading;
//                }
//                else if (keyCode == KeyEvent.VK_UP)
//                {
//                    latFactor = cosHeading;
//                    lonFactor = sinHeading;
//                }
//                else if (keyCode == KeyEvent.VK_DOWN)
//                {
//                    latFactor = -cosHeading;
//                    lonFactor = -sinHeading;
//                }
//                if (latFactor != 0 || lonFactor != 0)
//                {
//                    Angle latChange = computeLatOrLonChange(latFactor, slow);
//                    Angle lonChange = computeLatOrLonChange(lonFactor, slow);
//                    setCenterLatLon(
//                        this.view.getCenterPosition().getLatitude().add(latChange),
//                        this.view.getCenterPosition().getLongitude().add(lonChange));
//                    return;
//                }
//            }
//            
//        }

        double headingFactor = 0;
        double pitchFactor = 0;
        if (areModifiersExactly(modifiers, slowMask))
        {
            if (keyCode == KeyEvent.VK_PAGE_DOWN)
            {
                pitchFactor = 1;
                //System.out.println("pitch");
            }
            else if (keyCode == KeyEvent.VK_PAGE_UP)
            {
                pitchFactor = -1;
            }
            
            // added here to remove need for holding shift
            if (keyCode == KeyEvent.VK_LEFT)
                headingFactor = -1;
            else if (keyCode == KeyEvent.VK_RIGHT)
                headingFactor = 1;
            else if (keyCode == KeyEvent.VK_UP)
                pitchFactor = -1;
            else if (keyCode == KeyEvent.VK_DOWN)
                pitchFactor = 1;
            else if(keyCode == KeyEvent.VK_R) // SEG - if R is pushed, rest offsets (seems to be already in use but ok for now)
            {
                ((BasicModelView3)view).resetOffsets();
                fireViewChangedEvent();
            }
        }
        else if (areModifiersExactly(modifiers, InputEvent.SHIFT_DOWN_MASK | slowMask))
        // SEG - remove need to hold shift
        //else if (areModifiersExactly(modifiers, slowMask))    
        // SHOULD MAKE THESE PAN in x/y/z axis! (shift center point)
        {
            if (keyCode == KeyEvent.VK_LEFT)
                headingFactor = -1;
            else if (keyCode == KeyEvent.VK_RIGHT)
                headingFactor = 1;
            else if (keyCode == KeyEvent.VK_UP)
                pitchFactor = -1;
            else if (keyCode == KeyEvent.VK_DOWN)
                pitchFactor = 1;
        }
        
        
        
        // handle moves
        if (headingFactor != 0)
        {
            Angle newHeading = computeNewHeading(4 * headingFactor, slow);
            setHeading(newHeading);
            return;
        }
        else if (pitchFactor != 0)
        {
            Angle newPitch = computeNewPitch(4 * pitchFactor, slow);
            //System.out.println("Pitch:" + newPitch.degrees);
            setPitch(newPitch);
            return;
        }

        double zoomFactor = 0;
        if (areModifiersExactly(modifiers, slowMask))
        {
            if (keyCode == KeyEvent.VK_ADD ||
                keyCode == KeyEvent.VK_EQUALS)
                zoomFactor = -1;
            else if (keyCode == KeyEvent.VK_SUBTRACT ||
                keyCode == KeyEvent.VK_MINUS)
                zoomFactor = 1;
        }
        else if (areModifiersExactly(modifiers, InputEvent.CTRL_DOWN_MASK | slowMask)
            || areModifiersExactly(modifiers, InputEvent.META_DOWN_MASK | slowMask))
        {
            if (keyCode == KeyEvent.VK_UP)
                zoomFactor = -1;
            else if (keyCode == KeyEvent.VK_DOWN)
                zoomFactor = 1;
        }
        if (zoomFactor != 0)
        {
            double newZoom = computeNewZoom(zoomFactor, slow);
            setZoom(newZoom);
        }
    }

    public void keyTyped(KeyEvent keyEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (keyEvent == null)
            return;

        this.keyPollTimer.keyTyped(keyEvent);
    }

    public void keyPressed(KeyEvent keyEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (keyEvent == null)
            return;

        this.keyPollTimer.keyPressed(keyEvent);
    }

    public void keyReleased(KeyEvent keyEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (keyEvent == null)
            return;

        this.keyPollTimer.keyReleased(keyEvent);

        int keyCode = keyEvent.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE)
        {
            stopViewMovement();
        }
        else if (keyCode == KeyEvent.VK_N)
        {
            this.view.applyStateIterator(ScheduledOrbitViewStateIterator.createHeadingIterator(
                this.view.getHeading(),
                Angle.ZERO)); // Reset heading.
        }
        else if (keyCode == KeyEvent.VK_R)
        {
            this.view.applyStateIterator(ScheduledOrbitViewStateIterator.createHeadingPitchIterator(
                this.view.getHeading(),
                Angle.ZERO,   // Reset heading.
                this.view.getPitch(),
                Angle.ZERO)); // Reset pitch.
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

        // SEG -- uncommented to keep view from recenter/zooming when user clicks on globe
//        PickedObject top = pickedObjects.getTopPickedObject();
//        Position topPosition = top.getPosition();
//
//        if (isLockHeading())
//        {
//            this.modelViewInputSupport.setCenterTarget(null);
//            // TODO: it's possible to use picked elevation as center elevation, but is this what we want?
//            Position pos = new Position(topPosition.getLatLon(), this.view.getCenterPosition().getElevation());
//            setCenterPosition(pos, true, 0.9);
//        }

    }

    public void mousePressed(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

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
        }
        
        // Compute the current selected position if none exists.
        if (this.selectedPosition == null)
            this.updateSelectedPosition();

        //if (areModifiersExactly(mouseEvent, InputEvent.BUTTON1_DOWN_MASK))
        // SEG -- not used, move to 3rd button (locked out in this view), now also zoom
        if (areModifiersExactly(mouseEvent, InputEvent.BUTTON3_DOWN_MASK))
        {
            // zoom
            if (mouseMove != null)
            {
                if (mouseMove.y != 0)
                {
                    // Reduce the amount of zoom changed by mouse movement.
                    double scaledMouseY = mouseMove.y  / 10d;
                    double newZoom = computeNewZoom(scaledMouseY, false);
                    setZoom(newZoom);
                }
            }
//            if (prevMousePoint != null && curMousePoint != null)
//            {
//                Position prevPosition = computePositionAtPoint(prevMousePoint.x, prevMousePoint.y);
//                Position curPosition = computePositionAtPoint(curMousePoint.x, curMousePoint.y);
//                // Keep selected position under cursor.
//                if (prevPosition != null && curPosition != null)
//                {
//                    if (!prevPosition.equals(curPosition))
//                    {
//                        if (isLockHeading())
//                        {
//                            setCenterLatLon(
//                                this.view.getCenterPosition().getLatitude().add(prevPosition.getLatitude()).subtract(curPosition.getLatitude()),
//                                this.view.getCenterPosition().getLongitude().add(prevPosition.getLongitude()).subtract(curPosition.getLongitude()));
//                        }
//                        
//                    }
//                }
//                // Cursor is off the globe, simulate globe dragging.
//                else
//                {
//                    if (isLockHeading())
//                    {
//                        double sinHeading = this.view.getHeading().sin();
//                        double cosHeading = this.view.getHeading().cos();
//                        double latFactor = (cosHeading * mouseMove.y + sinHeading * mouseMove.x) / 10.0;
//                        double lonFactor = (sinHeading * mouseMove.y - cosHeading * mouseMove.x) / 10.0;
//                        Angle latChange = computeLatOrLonChange(latFactor, false);
//                        Angle lonChange = computeLatOrLonChange(lonFactor, false);
//                        setCenterLatLon(
//                            this.view.getCenterPosition().getLatitude().add(latChange),
//                            this.view.getCenterPosition().getLongitude().add(lonChange));
//                    }
//
//                    // Cursor went off the globe. Clear the selected position to ensure a new one will be
//                    // computed if the cursor returns to the globe.
//                    clearSelectedPosition();
//                }
//            }
        }
        //else if (areModifiersExactly(mouseEvent, InputEvent.BUTTON3_DOWN_MASK)
        //    || areModifiersExactly(mouseEvent, InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK))
        // SEG -- make rotation the first button (no, control+mouse1 option here)
        else if (areModifiersExactly(mouseEvent, InputEvent.BUTTON1_DOWN_MASK))
        {
            if (mouseMove != null)
            {
                if (mouseMove.x != 0)
                {
                    // Switch the direction of heading change depending on whether the cursor is above or below
                    // the center of the screen.
                    double headingDirection = 1;
                    Object source = mouseEvent.getSource();
                    if (source != null && source instanceof java.awt.Component)
                    {
                        java.awt.Component component = (java.awt.Component) source;
                        if (mouseEvent.getPoint().y < (component.getHeight() / 2))
                            headingDirection = -1;
                    }
                    Angle newHeading = computeNewHeading(headingDirection * mouseMove.x, false);
                    setHeading(newHeading);
                }
                if (mouseMove.y != 0)
                {
                    Angle newPitch = computeNewPitch(mouseMove.y, false);
                    setPitch(newPitch);
                }
            }
        }
        else if (areModifiersExactly(mouseEvent, InputEvent.BUTTON2_DOWN_MASK)) // ZOOM
        {
            if (mouseMove != null)
            {
                if (mouseMove.y != 0)
                {
                    // Reduce the amount of zoom changed by mouse movement.
                    double scaledMouseY = mouseMove.y  / 10d;
                    double newZoom = computeNewZoom(scaledMouseY, false);
                    setZoom(newZoom);
                }
            }
        } // zoom, 2nd button down
        // SEG -- mouse button 1 + control, moves center of rotation x-z plane
        else if( areModifiersExactly(mouseEvent, InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK) )
        {
            // Stop ViewStateIterators, so we are the only one affecting the view.
            stopViewIterators();
            
            // make updates
             if (mouseMove.x > 0)
             {
                ((BasicModelView3)view).incrementXOffset(-1.0);
             }
            if (mouseMove.x < 0)
             {
                ((BasicModelView3)view).incrementXOffset(1.0);
             }
            
//            // make updates
//             if (mouseMove.y > 0)
//             {
//                ((BasicModelView3)view).incrementZOffset(1.0);
//             }
//            if (mouseMove.y < 0)
//             {
//                ((BasicModelView3)view).incrementZOffset(-1.0);
//             }
            
            // fire change view event
            fireViewChangedEvent();
        }
        // SEG -- mouse button 3 + control, moves center of rotation y-z plane
        else if( areModifiersExactly(mouseEvent, InputEvent.BUTTON3_DOWN_MASK | InputEvent.CTRL_DOWN_MASK) )
        {
            // Stop ViewStateIterators, so we are the only one affecting the view.
            stopViewIterators();
            
            // make updates
             if (mouseMove.x > 0)
             {
                ((BasicModelView3)view).incrementYOffset(-1.0);
             }
            if (mouseMove.x < 0)
             {
                ((BasicModelView3)view).incrementYOffset(1.0);
             }
            
//            // make updates
//             if (mouseMove.y > 0)
//             {
//                ((BasicModelView3)view).incrementZOffset(1.0);
//             }
//            if (mouseMove.y < 0)
//             {
//                ((BasicModelView3)view).incrementZOffset(-1.0);
//             }
            
            // fire change view event
            fireViewChangedEvent();
        }
        // mouse drag + mouse button 2 + Control = reset of offsets to 0 
        else if( areModifiersExactly(mouseEvent, InputEvent.BUTTON2_DOWN_MASK | InputEvent.CTRL_DOWN_MASK) )
        {
            stopViewIterators();
            
//            ((BasicModelView3)view).resetOffsets();
            
            // make updates
             if (mouseMove.y > 0)
             {
                ((BasicModelView3)view).incrementZOffset(1.0);
             }
            if (mouseMove.y < 0)
             {
                ((BasicModelView3)view).incrementZOffset(-1.0);
             }
            
            fireViewChangedEvent();
        }
        
        
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
        double newZoom = computeNewZoom(wheelDirection, false);
        setZoom(newZoom);
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

        stopViewMovement();
        this.keyPollTimer.stop();
    }

    public void stageChanged(RenderingEvent event)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (event == null)
            return;

        if (event.getStage().equals(RenderingEvent.BEFORE_RENDERING))
        {
            // Cancel any InputHandler view changes if someone has set a view state iterator.
            if (this.view.hasStateIterator())
                this.modelViewInputSupport.clearTargets();

            if (this.modelViewInputSupport.hasTargets())
                this.modelViewInputSupport.moveViewTowardTargets();
        }
    }

    private static boolean areModifiersExactly(InputEvent inputEvent, int mask)
    {
        return areModifiersExactly(inputEvent.getModifiersEx(), mask);
    }

    private static boolean areModifiersExactly(int modifiersEx, int mask)
    {
        return modifiersEx == mask;
    }

    // ============== View State Changes ======================= //
    // ============== View State Changes ======================= //
    // ============== View State Changes ======================= //

    private void stopViewMovement()
    {
        if (this.view == null)
            return;

        this.view.stopMovement();
    }

    private void stopViewIterators()
    {
        if (this.view == null)
            return;

        if (this.view.hasStateIterator())
            this.view.stopStateIterators();
    }

    private void fireViewChangedEvent()
    {
        if (this.view == null)
            return;

        this.view.firePropertyChange(AVKey.VIEW, null, this.view);
    }

    private void setCenterLatLon(Angle latitude, Angle longitude)
    {
        if (this.view == null)
            return;

        setCenterPosition(new Position(latitude, longitude, this.view.getCenterPosition().getElevation()));
    }

    private void setCenterPosition(Position newCenter)
    {
        if (this.view == null)
            return;

        setCenterPosition(newCenter, this.smoothViewChanges, 0.4);
    }

    private void setCenterPosition(Position newCenter, boolean smoothed, double smoothingAmount)
    {
        if (this.view == null)
            return;

        // Stop ViewStateIterators, so we are the only one affecting the view.
        stopViewIterators();

        Position prevTarget = this.modelViewInputSupport.getCenterTarget();
        if (smoothed && prevTarget != null)
        {
            Position curCenter = this.view.getCenterPosition();
            newCenter = new Position(
                prevTarget.getLatitude().add(newCenter.getLatitude()).subtract(curCenter.getLatitude()),
                prevTarget.getLongitude().add(newCenter.getLongitude()).subtract(curCenter.getLongitude()),
                prevTarget.getElevation() + newCenter.getElevation() - curCenter.getElevation());
        }

        this.modelViewInputSupport.setCenterSmoothing(smoothed ? smoothingAmount : 0);
        this.modelViewInputSupport.setCenterTarget(newCenter);
        fireViewChangedEvent();
    }

    private void setHeading(Angle newHeading)
    {
        if (this.view == null)
            return;

        setHeading(newHeading, this.smoothViewChanges, 0.7);
    }

    private void setHeading(Angle newHeading, boolean smoothed, double smoothingAmount)
    {
        if (this.view == null)
            return;

        // Stop ViewStateIterators, so we are the only one affecting the view.
        stopViewIterators();

        Angle prevTarget = this.modelViewInputSupport.getHeadingTarget();
        if (smoothed && prevTarget != null)
            newHeading = prevTarget.add(newHeading).subtract(this.view.getHeading());

        this.modelViewInputSupport.setHeadingSmoothing(smoothed ? smoothingAmount : 0);
        this.modelViewInputSupport.setHeadingTarget(newHeading);
        fireViewChangedEvent();
    }

    private void setPitch(Angle newPitch)
    {
        if (this.view == null)
            return;

        setPitch(newPitch, this.smoothViewChanges, 0.7);
    }

    private void setPitch(Angle newPitch, boolean smoothed, double smoothingAmount)
    {
        if (this.view == null)
            return;

        // Stop ViewStateIterators, so we are the only one affecting the view.
        stopViewIterators();
        
        //System.out.println("Pitch:" + newPitch.degrees);

        Angle prevTarget = this.modelViewInputSupport.getPitchTarget();
        if (smoothed && prevTarget != null)
        {
            newPitch = prevTarget.add(newPitch).subtract(this.view.getPitch());
        }

        this.modelViewInputSupport.setPitchSmoothing(smoothed ? smoothingAmount : 0);
        this.modelViewInputSupport.setPitchTarget(newPitch);
        fireViewChangedEvent();
    }

    private void setZoom(double newZoom)
    {
        if (this.view == null)
            return;

        setZoom(newZoom, this.smoothViewChanges, 0.9);
    }

    private void setZoom(double newZoom, boolean smoothed, double smoothingAmount)
    {
        if (this.view == null)
            return;

        // Stop ViewStateIterators, so we are the only one affecting the view.
        stopViewIterators();

        double prevTarget = this.modelViewInputSupport.getZoomTarget();
        if (smoothed && prevTarget >= 0)
            newZoom = computeNewZoomTarget(prevTarget, newZoom, this.view.getZoom());

        this.modelViewInputSupport.setZoomSmoothing(smoothed ? smoothingAmount : 0);
        this.modelViewInputSupport.setZoomTarget(newZoom);
        fireViewChangedEvent();
    }

    private Angle computeLatOrLonChange(double amount, boolean slow)
    {
        if (this.wwd == null
                || this.wwd.getModel() == null
                || this.wwd.getModel().getGlobe() == null
                || this.view == null
                || this.view.getEyePosition() == null)
        {
            return Angle.ZERO;
        }

        Position eyePos = this.view.getEyePosition();
        double normAlt = (eyePos.getElevation() / this.wwd.getModel().getGlobe().getRadiusAt(eyePos.getLatLon()));
        if (normAlt < 0)
            normAlt = 0;
        else if (normAlt > 1)
            normAlt = 1;

        double coeff = (0.0001 * (1 - normAlt)) + (2 * normAlt);
        if (slow)
            coeff /= 4.0;

        return Angle.fromDegrees(coeff * amount);
    }

    private Angle computeNewHeading(double amount, boolean slow)
    {
        if (this.view == null)
            return Angle.ZERO;

        return computeNewHeadingOrPitch(this.view.getHeading(), amount, slow);
    }

    private Angle computeNewPitch(double amount, boolean slow)
    {
        if (this.view == null)
            return Angle.ZERO;

        return computeNewHeadingOrPitch(this.view.getPitch(), amount, slow);
    }

    private Angle computeNewHeadingOrPitch(Angle value, double amount, boolean slow)
    {
        double coeff = 1.0/4.0;
        if (slow)
            coeff /= 4.0;

        Angle change = Angle.fromDegrees(coeff * amount);
        return value.add(change);
    }

    private double computeNewZoom(double amount, boolean slow)
    {
        if (this.view == null)
            return 0;

        double coeff = 0.05;
        if (slow)
            coeff /= 4.0;

        double change = coeff * amount;
        double logZoom = this.view.getZoom() != 0 ? Math.log(this.view.getZoom()) : 0;
        // Zoom changes are treated as logarithmic values. This accomplishes two things:
        // 1) Zooming is slow near the globe, and fast at great distances.
        // 2) Zooming in then immediately zooming out returns the viewer to the same zoom value.
        return Math.exp(logZoom + change);
    }

    private double computeNewZoomTarget(double prevTarget, double newTarget, double curZoom)
    {
        double lonPrevTarget = prevTarget != 0 ? Math.log(prevTarget) : 0;
        double logNewTarget = newTarget != 0 ? Math.log(newTarget) : 0;
        double logCurZoom = curZoom != 0 ? Math.log(curZoom) : 0;
        return Math.exp(lonPrevTarget + logNewTarget - logCurZoom);
    }

    
}
