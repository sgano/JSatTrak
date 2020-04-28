/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.render.Size;

import java.awt.*;

/**
 * Animation to minimize a frame with a window shade effect.
 *
 * @author pabercrombie
 * @version $Id: WindowShadeAnimation.java 14066 2010-11-02 01:43:35Z pabercrombie $
 */
public class WindowShadeAnimation implements Animation
{
    protected ScrollFrame frame;
    protected int delta;
    protected int targetWindowHeight;
    protected int step;
    protected int numSteps = 20;

    protected Size targetWindowSize;
    protected Size originalWindowSize;
    protected Dimension originalDimension;

    public WindowShadeAnimation(ScrollFrame frame)
    {
        this.frame = frame;
    }

    public void reset()
    {
        Dimension currentSize = this.frame.getCurrentSize();
        this.step = 0;

        // The minimized flag is set before the animation starts. So if the layout says that it is minimized, we want to
        // animate toward a minimized size.
        if (this.frame.isMinimized())
        {
            this.originalDimension = currentSize;
            this.originalWindowSize = this.frame.getSize();
            this.targetWindowHeight = this.frame.getTitleBarHeight() + this.frame.frameBorder * 2;
            this.targetWindowSize = Size.fromPixels(currentSize.width, this.targetWindowHeight); 
            this.delta = -(currentSize.height - this.targetWindowHeight) / this.numSteps;
        }
        else
        {
            this.targetWindowSize = this.originalWindowSize;
            this.targetWindowHeight = this.originalDimension.height;
            this.delta = -(currentSize.height - this.targetWindowHeight) / this.numSteps;
        }
    }

    public void step()
    {
        this.step++;

        // Set the target window size in the final step to correct for rounding errors
        if (this.step == this.numSteps)
        {
            this.frame.setSize(this.targetWindowSize);
        }
        else
        {
            Dimension size = this.frame.getCurrentSize();
            this.frame.setSize(Size.fromPixels(size.width, size.height + this.delta));
        }
    }

    public boolean hasNext()
    {
        return this.step < this.numSteps;
    }
}
