/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.animation.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.PropertyAccessor;
import gov.nasa.worldwind.view.*;

/**
 * @author jym
 * @version $Id$
 */
public class FlyToOrbitViewAnimator extends CompoundAnimator
{

    boolean endCenterOnSurface;
    OnSurfacePositionAnimator centerAnimator;
    ViewElevationAnimator zoomAnimator;
    AngleAnimator headingAnimator;
    AngleAnimator pitchAnimator;
    BasicOrbitView orbitView;

    public FlyToOrbitViewAnimator(OrbitView orbitView, Interpolator interpolator, boolean endCenterOnSurface,
        PositionAnimator centerAnimator, DoubleAnimator zoomAnimator,
        AngleAnimator headingAnimator, AngleAnimator pitchAnimator)
    {
        super(interpolator, centerAnimator, zoomAnimator, headingAnimator, pitchAnimator);
        this.orbitView = (BasicOrbitView) orbitView;
        this.centerAnimator = (OnSurfacePositionAnimator) centerAnimator;
        this.zoomAnimator = (ViewElevationAnimator) zoomAnimator;
        this.headingAnimator = headingAnimator;
        this.pitchAnimator = pitchAnimator;
        if (interpolator == null)
        {
            this.interpolator = new ScheduledInterpolator(10000);
        }
        this.endCenterOnSurface = endCenterOnSurface;
    }



    public static FlyToOrbitViewAnimator createFlyToOrbitViewAnimator(
        OrbitView orbitView,
        Position beginCenterPos, Position endCenterPos,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom, long timeToMove, boolean endCenterOnSurface)
    {

        OnSurfacePositionAnimator centerAnimator = new OnSurfacePositionAnimator(orbitView.getGlobe(),
            new ScheduledInterpolator(timeToMove),
                beginCenterPos, endCenterPos,
                OrbitViewPropertyAccessor.createCenterPositionAccessor(
                orbitView), endCenterOnSurface);

        ViewElevationAnimator zoomAnimator = new ViewElevationAnimator(orbitView.getGlobe(),
            beginZoom, endZoom, beginCenterPos, endCenterPos,
            OrbitViewPropertyAccessor.createZoomAccessor(orbitView));

        centerAnimator.useMidZoom = zoomAnimator.getUseMidZoom(); 

        AngleAnimator headingAnimator = new AngleAnimator(
                new ScheduledInterpolator(timeToMove),
                beginHeading, endHeading,
                ViewPropertyAccessor.createHeadingAccessor(orbitView));

        AngleAnimator pitchAnimator = new AngleAnimator(
                new ScheduledInterpolator(timeToMove),
                beginPitch, endPitch,
                ViewPropertyAccessor.createPitchAccessor(orbitView));

        FlyToOrbitViewAnimator panAnimator = new FlyToOrbitViewAnimator(orbitView,
            new ScheduledInterpolator(timeToMove), endCenterOnSurface, centerAnimator,
                zoomAnimator, headingAnimator, pitchAnimator);

        return(panAnimator);
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

    public void stop()
    {
        if (endCenterOnSurface)
        {
            orbitView.setViewOutOfFocus(true);
        }
        super.stop();

    }

    //public void set(double interpolant)
    //{
    //
    //}



}
