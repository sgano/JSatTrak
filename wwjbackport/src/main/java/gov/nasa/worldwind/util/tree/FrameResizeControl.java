/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.AbstractResizeHotSpot;

import java.awt.*;
import java.awt.geom.*;

/**
 * A screen control for resizing a frame. This class handles the resize input events, but does does not actually draw
 * the resize control.
 *
 * @author pabercrombie
 * @version $Id: FrameResizeControl.java 14221 2010-12-10 23:47:55Z pabercrombie $
 */
public class FrameResizeControl extends AbstractResizeHotSpot
{
    protected ScrollFrame frame;

    /**
     * Create a resize control.
     *
     * @param frame     Frame to resize.
     */
    public FrameResizeControl(ScrollFrame frame)
    {
        this.frame = frame;
    }

    @Override
    protected Dimension getSize()
    {
        return this.frame.getCurrentSize();
    }

    @Override
    protected void setSize(Dimension newSize)
    {
        this.frame.setSize(Size.fromPixels(newSize.width, newSize.height));
    }

    @Override
    protected Point getScreenPoint()
    {
        Point2D point2D = this.frame.getScreenPoint();
        return new Point((int)point2D.getX(), (int)point2D.getY());
    }

    @Override
    protected void setScreenPoint(Point newPoint)
    {
        this.frame.setScreenLocation(new Offset(newPoint.getX(), newPoint.getY(), AVKey.PIXELS, AVKey.PIXELS));
    }

    @Override
    protected Dimension getMinimumSize()
    {
        return this.frame.getMinimumSize();
    }
}
