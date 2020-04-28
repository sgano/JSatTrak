/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view.firstperson;

import gov.nasa.worldwind.animation.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.PropertyAccessor;
import gov.nasa.worldwind.view.*;

/**
 * @author jym
 * @version $Id: FlyToFlyViewAnimator.java 12652 2009-09-25 14:55:33Z jterhorst $
 */
public class FlyToFlyViewAnimator extends CompoundAnimator
{

    boolean endCenterOnSurface;
    public FlyToFlyViewAnimator(Interpolator interpolator, boolean endCenterOnSurface,
        PositionAnimator eyePositionAnimator, DoubleAnimator elevationAnimator,
        AngleAnimator headingAnimator, AngleAnimator pitchAnimator)
    {
        super(interpolator, eyePositionAnimator, elevationAnimator, headingAnimator, pitchAnimator);
        if (interpolator == null)
        {
            this.interpolator = new ScheduledInterpolator(10000);
        }
        this.endCenterOnSurface = endCenterOnSurface;
    }

    public static FlyToFlyViewAnimator createFlyToFlyViewAnimator(
        BasicFlyView view,
        Position beginCenterPos, Position endCenterPos,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginElevation, double endElevation, long timeToMove, boolean endCenterOnSurface)
    {
        OnSurfacePositionAnimator centerAnimator = new OnSurfacePositionAnimator(
            view.getGlobe(),
            new ScheduledInterpolator(timeToMove),
                beginCenterPos, endCenterPos,
                ViewPropertyAccessor.createEyePositionAccessor(
                view), false);

        FlyToElevationAnimator elevAnimator = new FlyToElevationAnimator(view, view.getGlobe(),
            beginElevation, endElevation, beginCenterPos, endCenterPos,
            ViewPropertyAccessor.createElevationAccessor(view));

        AngleAnimator headingAnimator = new AngleAnimator(
                new ScheduledInterpolator(timeToMove),
                beginHeading, endHeading,
                ViewPropertyAccessor.createHeadingAccessor(view));

        AngleAnimator pitchAnimator = new AngleAnimator(
                new ScheduledInterpolator(timeToMove),
                beginPitch, endPitch,
                ViewPropertyAccessor.createPitchAccessor(view));

        FlyToFlyViewAnimator panAnimator = new FlyToFlyViewAnimator(
            new ScheduledInterpolator(timeToMove), endCenterOnSurface, centerAnimator,
                elevAnimator, headingAnimator, pitchAnimator);

        return(panAnimator);
    }

    private static class FlyToElevationAnimator extends ViewElevationAnimator
    {
        public FlyToElevationAnimator(BasicFlyView flyView, Globe globe,
            double beginZoom, double endZoom, LatLon beginLatLon,
        LatLon endLatLon, PropertyAccessor.DoubleAccessor propertyAccessor)
        {
            super(globe, beginZoom, endZoom, beginLatLon, endLatLon, propertyAccessor);

            if (globe == null)
            {
                useMidZoom = false;
            }
            else
            {
                this.midZoom = computeMidZoom(globe, beginLatLon, endLatLon, beginZoom, endZoom);
                double maxMidZoom = flyView.getViewPropertyLimits().getEyeElevationLimits()[1];
                if (this.midZoom > maxMidZoom)
                {
                    this.midZoom = maxMidZoom;
                }
                useMidZoom = useMidZoom(beginZoom, endZoom, midZoom);
            }
            if (useMidZoom)
            {
                this.trueEndZoom = endZoom;
                this.setEnd(this.midZoom);
            }

        }

    }

    private static class OnSurfacePositionAnimator extends PositionAnimator
    {
        Globe globe;
        boolean endCenterOnSurface;
        boolean useMidZoom = true;
        public OnSurfacePositionAnimator(Globe globe, Interpolator interpolator,
            Position begin,
            Position end,
            PropertyAccessor.PositionAccessor propertyAccessor, boolean endCenterOnSurface)
        {
            super(interpolator, begin, end, propertyAccessor);
            this.globe = globe;
            this.endCenterOnSurface = endCenterOnSurface;
        }

        protected Position nextPosition(double interpolant)
        {

            final int MAX_SMOOTHING = 1;

            final double CENTER_START = this.useMidZoom ? 0.2 : 0.0;
            final double CENTER_STOP = this.useMidZoom ? 0.8 : 0.8;
            double latLonInterpolant = AnimationSupport.basicInterpolant(interpolant, CENTER_START, CENTER_STOP, MAX_SMOOTHING);

            // Invoke the standard next position functionality.
            Position pos = super.nextPosition(latLonInterpolant);

            // If the caller has flagged endCenterOnSurface, then we override endPosition's elevation with
            // the surface elevation.
            if (endCenterOnSurface)
            {
                // Use interpolated lat/lon.
                LatLon ll = pos;
                // Override end position elevation with surface elevation at end lat/lon.
                double e1 = getBegin().getElevation();
                double e2 = globe.getElevation(getEnd().getLatitude(), getEnd().getLongitude());
                pos = new Position(ll, (1 - latLonInterpolant) * e1 + latLonInterpolant * e2);
            }

            return pos;
        }
    }
}
