/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

/**
 * An animation that can be played in series of steps.
 *
 * @author pabercrombie
 * @version $Id: Animation.java 14044 2010-10-26 02:01:39Z pabercrombie $
 */
public interface Animation
{
    /**
     * Reset the animation to the starting state.
     */
    void reset();

    /**
     * Step the animation.
     */
    void step();

    /**
     * Are there more steps left in the animation?
     *
     * @return True if there are more steps.
     */
    boolean hasNext();
}
