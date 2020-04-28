/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.animation.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

/**
 * An {@link gov.nasa.worldwind.animation.Animator} for elevation values.  Calculates a mid-zoom value that
 * gives the effect of flying up and them back down again.
 *
 * @author jym
 * @version $Id: ViewElevationAnimator.java 12652 2009-09-25 14:55:33Z jterhorst $
 */
public class ViewElevationAnimator extends DoubleAnimator
{
    protected double midZoom;
    protected boolean useMidZoom = true;
    protected double trueEndZoom;
    public ViewElevationAnimator(Globe globe, double beginZoom, double endZoom, LatLon beginLatLon,
        LatLon endLatLon, PropertyAccessor.DoubleAccessor propertyAccessor)
    {
        super(null, beginZoom, endZoom, propertyAccessor);

        if (globe == null)
        {
            useMidZoom = false;
        }
        else
        {
            this.midZoom = computeMidZoom(globe, beginLatLon, endLatLon, beginZoom, endZoom);
            useMidZoom = useMidZoom(beginZoom, endZoom, midZoom);
        }

        if (useMidZoom)
        {
            this.trueEndZoom = endZoom;
            this.setEnd(this.midZoom);
        }

    }

    /**
     * return the true position to end the elevation animation at.
     * @return the true end elevation position.
     */
    public double getTrueEndZoom()
    {
        return(trueEndZoom);
    }

    /**
     * determines whether this Animator is using midZoom.
     * The mid-point zoom is an interpolated value between minimum(the lesser of beginZoom and endZoom,
     * and maximum zoom (3* the radius of the globe).
     * @return whether this Animator is using midZoom.
     */
    public boolean getUseMidZoom()
    {
        return useMidZoom;
    }

    /**
     * Set the value of the field being animated based on the given interpolant.
     * @param interpolant A value between 0 and 1.
     */
    public void set(double interpolant)
    {
        final int MAX_SMOOTHING = 1;
        final double ZOOM_START = 0.0;
        final double ZOOM_STOP = 1.0;
        if (interpolant >= 1.0)
            this.stop();
        double  zoomInterpolant;


        if (this.useMidZoom)
        {
            double value;
            zoomInterpolant = this.zoomInterpolant(interpolant, ZOOM_START, ZOOM_STOP, MAX_SMOOTHING);
            if (interpolant <= .5)
            {
                value = nextDouble(zoomInterpolant, this.begin, this.end);
            }
            else
            {
                value = nextDouble(zoomInterpolant, this.end, this.trueEndZoom);
            }
            this.propertyAccessor.setDouble(value);
        }
        else
        {
            zoomInterpolant = AnimationSupport.basicInterpolant(interpolant, ZOOM_START, ZOOM_STOP, MAX_SMOOTHING);
            super.set(zoomInterpolant);
        }

    }

    private double zoomInterpolant(double interpolant, double startInterpolant, double stopInterpolant,
            int maxSmoothing)
    {
        // Map interpolant in to range [start, stop].
        double normalizedInterpolant = AnimationSupport.interpolantNormalized(
            interpolant, startInterpolant, stopInterpolant);

        // During first half of iteration, zoom increases from begin to mid,
        // and decreases from mid to end during second half.
        if (normalizedInterpolant <= 0.5)
        {
            normalizedInterpolant = (normalizedInterpolant * 2.0);
        }
        else
        {
            normalizedInterpolant = ((normalizedInterpolant - .5) * 2.0);
        }

        return AnimationSupport.interpolantSmoothed(normalizedInterpolant, maxSmoothing);
    }

    /**
     * computes the value for the given interpolant.
     * @param interpolant the interpolant to use for interpolating
     * @param start the lower end of the interpolated range.
     * @param end the upper end of the interpolated range.
     * @return the interpolated value.
     */
    protected double nextDouble(double interpolant, double start, double end)
    {
        return AnimationSupport.mixDouble(
           interpolant,
           start,
           end);
    }

    protected void setImpl(double interpolant)
    {
       Double newValue = this.nextDouble(interpolant);
       if (newValue == null)
           return;

       boolean success = this.propertyAccessor.setDouble(newValue);
       if (!success)
       {
           this.flagLastStateInvalid();
       }
       if (interpolant >= 1.0)
           this.stop();
    }


    protected static double computeMidZoom(
        Globe globe,
        LatLon beginLatLon, LatLon endLatLon,
        double beginZoom, double endZoom)
    {
        // Scale factor is angular distance over 180 degrees.
        Angle sphericalDistance = LatLon.greatCircleDistance(beginLatLon, endLatLon);
        double scaleFactor = AnimationSupport.angularRatio(sphericalDistance, Angle.POS180);

        // Mid-point zoom is interpolated value between minimum and maximum zoom.
        final double MIN_ZOOM = Math.min(beginZoom, endZoom);
        final double MAX_ZOOM = 3.0 * globe.getRadius();
        return AnimationSupport.mixDouble(scaleFactor, MIN_ZOOM, MAX_ZOOM);
    }

    /**
     * Determines if the animation will use mid-zoom.  Mid-zoom animation is used if the difference between the beginZoom
     * and endZoom values is less than the difference between the midZoom value and the larger of the beginZoom
     * or endZoom values.
     * @param beginZoom the begin zoom value
     * @param endZoom the end zoom value
     * @param midZoom the elevation at the middle of the animation
     * @return true if it is appropriate to use the midZoom value.
     */
    protected boolean useMidZoom(double beginZoom, double endZoom, double midZoom)
    {
        double a = Math.abs(endZoom - beginZoom);
        double b = Math.abs(midZoom - Math.max(beginZoom, endZoom));
        return a < b;
    }

}
