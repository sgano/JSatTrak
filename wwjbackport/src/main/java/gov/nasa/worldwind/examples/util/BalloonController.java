/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.util;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.event.*;

/**
 * Controller to display balloons for KML placemarks. The controller displays the balloon when the placemark is clicked,
 * and hides the balloon when a different object, or the globe, is clicked.
 * <p/>
 * The BalloonController looks for a key of {@link AVKey#BALLOON} in the AVList of the top picked object. If the object
 * attached to this key is of type {@link Balloon}, the controller will make the balloon visible.
 *
 * @author pabercrombie
 * @version $Id: BalloonController.java 14161 2010-11-30 02:34:52Z pabercrombie $
 */
public class BalloonController extends MouseAdapter
{
    protected WorldWindow wwd;

    protected Object lastSelectedObject;
    protected Balloon balloon;

    /**
     * Create a new balloon controller.
     *
     * @param wwd WorldWindow to attach to.
     */
    public BalloonController(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.wwd = wwd;
        this.wwd.getInputHandler().addMouseListener(this);
    }

    /**
     * Handle a mouse click. If the top picked object has a balloon attached to it the balloon will be made visible. A
     * balloon may be attached to a KML feature, or to any picked object though {@link AVKey#BALLOON}.
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        try
        {
            // Handle only left click
            if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() % 2 == 1))
            {
                PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
                if (pickedObjects == null || pickedObjects.getTopPickedObject() == null)
                    return;

                Object topObject = pickedObjects.getTopObject();
                PickedObject topPickedObject = pickedObjects.getTopPickedObject();

                if (this.lastSelectedObject == topObject || this.balloon == topObject)
                {
                    return; // Same thing selected
                }

                if (this.lastSelectedObject != null || topPickedObject.isTerrain())
                {
                    this.hideBalloon(); // Something else selected
                }

                Object balloonObj = null;

                if (topPickedObject != null)
                {
                    // Look for a KMLAbstractFeature context. If the top picked object is part of a KML feature, the
                    // feature will determine the balloon.
                    Object contextObj = topPickedObject.getValue(AVKey.CONTEXT);
                    if (contextObj instanceof KMLAbstractFeature)
                    {
                        KMLAbstractFeature kmlFeature = (KMLAbstractFeature) contextObj;
                        balloonObj = kmlFeature.getField(AVKey.BALLOON);
                    }
                    // If this is not a KML feature, look for a balloon in the AVList
                    else if (topObject instanceof AVList)
                    {
                        AVList avList = (AVList) topObject;
                        balloonObj = avList.getValue(AVKey.BALLOON);
                    }
                }

                if (balloonObj instanceof Balloon)
                {
                    Balloon b = (Balloon) balloonObj;

                    // Don't change balloons that are already visible
                    if (!b.isVisible())
                    {
                        this.lastSelectedObject = topObject;
                        this.showBalloon(b, e.getPoint());
                    }
                }
            }
        }
        catch (Exception ex)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            ex.printStackTrace();
            Logging.logger().warning(ex.getMessage() != null ? ex.getMessage() : ex.toString());
        }
    }

    /**
     * Show a balloon.
     *
     * @param balloon Balloon to make visible.
     * @param point   Point where mouse was clicked.
     */
    protected void showBalloon(Balloon balloon, Point point)
    {
        this.balloon = balloon;

        // If the balloon is attached to the screen rather than the globe, move it to the
        // current point. Otherwise move it to the position under the current point.
        if (Balloon.SCREEN_MODE.equals(this.balloon.getAttachmentMode()))
            this.balloon.setPosition(point);
        else
            this.balloon.setPosition(wwd.getView().computePositionFromScreenPoint(point.x, point.y));

        this.balloon.setVisible(true);
    }

    /** Hide the balloon. */
    protected void hideBalloon()
    {
        if (this.balloon != null)
        {
            this.balloon.setVisible(false);
            this.balloon = null;
        }
        this.lastSelectedObject = null;
    }
}
