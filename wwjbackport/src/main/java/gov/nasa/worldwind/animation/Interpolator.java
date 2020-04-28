/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.animation;

/**
 * @author jym
 * @version $Id: Interpolator.java 12631 2009-09-22 02:23:09Z jterhorst $
 */

/**
 * An interface for generating interpolants.
 */
public interface Interpolator
{
    /**
     * Returns the next interpolant
     * @return a value between 0 and 1 that represents the next position of the interpolant.
     */
    double nextInterpolant();
}
