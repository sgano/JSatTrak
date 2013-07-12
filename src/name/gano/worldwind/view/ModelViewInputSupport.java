/*
 * ModelViewInputSupport.java
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

package name.gano.worldwind.view;
/*
Copyright (C) 2001, 2007 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.OrbitView;

/**
 * @author dcollins
 * @version $Id: ModelViewInputSupport.java 5276 2008-05-02 04:33:57Z dcollins $
 */
class ModelViewInputSupport implements java.beans.PropertyChangeListener
{
    // OrbitView that will receive value changes.
    private OrbitView orbitView;
    private boolean viewChanged;
    private boolean viewOutOfFocus;
    private boolean targetStopped;
    // Target OrbitView values.
    private Position centerTarget; // Initially, do not change OrbitView's center position.
    private Angle headingTarget; // Initially, do not change OrbitView's heading.
    private Angle pitchTarget; // Initially, do not change OrbitView's pitch.
    private double zoomTarget = -1; // Initially, do not change OrbitView's zoom.
    // OrbitView value error thresholds.
    private double centerMinEpsilon;
    private double headingMinEpsilon;
    private double pitchMinEpsilon;
    private double zoomMinEpsilon;
    // OrbitView value smoothing coefficients.
    private double centerSmoothing;
    private double headingSmoothing;
    private double pitchSmoothing;
    private double zoomSmoothing;

    ModelViewInputSupport()
    {
        this(null);
    }

    ModelViewInputSupport(OrbitView orbitView)
    {
        setOrbitView(orbitView);
        loadConfigurationValues();
        // We don't know what state the view is in, so we assume it is "out of focus".
        setViewOutOfFocus(true);
    }

    private void loadConfigurationValues()
    {
        // No configuration keys exist for these values yet (they may never).
        setCenterMinEpsilon(1e-9);
        setHeadingMinEpsilon(1e-4);
        setPitchMinEpsilon(1e-4);
        setZoomMinEpsilon(1e-3);

        // No configuration keys exist for these values yet (they may never).
        setCenterSmoothing(0.4);
        setHeadingSmoothing(0.7);
        setPitchSmoothing(0.7);
        setZoomSmoothing(0.9);
    }

    public OrbitView getOrbitView()
    {
        return this.orbitView;
    }

    public void setOrbitView(OrbitView orbitView)
    {
        if (orbitView == this.orbitView)
            return;

        if (this.orbitView != null)
        {
            this.orbitView.removePropertyChangeListener(this);
        }

        this.orbitView = orbitView;
        
        if (this.orbitView != null)
        {
            this.orbitView.addPropertyChangeListener(this);
        }
    }

    public void propertyChange(java.beans.PropertyChangeEvent event)
    {
        if (event == null)
            return;

        if (event.getPropertyName().equals(View.VIEW_STOPPED))
        {
            clearTargets();
        }
        else if (event.getPropertyName().equalsIgnoreCase(OrbitView.CENTER_STOPPED))
        {
            this.centerTarget = null;
            setViewOutOfFocus(false);
        }
    }

    private boolean isViewChanged()
    {
        boolean result = this.viewChanged;
        this.viewChanged = false;
        return result;
    }

    private void flagViewChanged()
    {
        this.viewChanged = true;
    }

    private void refreshView()
    {
        if (isViewChanged() && this.orbitView != null)
            this.orbitView.firePropertyChange(AVKey.VIEW, null, this);
    }

    private boolean isViewOutOfFocus()
    {
        return this.viewOutOfFocus;
    }

    private void setViewOutOfFocus(boolean b)
    {
        this.viewOutOfFocus = b;
    }

    private void focusView()
    {
        if (this.orbitView == null)
            return;
        
        try
        {
            // Update the View's focus.
            if (this.orbitView.canFocusOnViewportCenter())
            {
                this.orbitView.focusOnViewportCenter();
                setViewOutOfFocus(false);
                flagViewChanged();
            }
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            // If updating the View's focus failed, raise the flag again.
            setViewOutOfFocus(true);
        }
    }

    private boolean isTargetStopped()
    {
        boolean result = this.targetStopped;
        this.targetStopped = false;
        return result;
    }

    private void flagTargetStopped()
    {
        this.targetStopped = true;
    }

    public Position getCenterTarget()
    {
        return this.centerTarget;
    }

    public void setCenterTarget(Position centerTarget)
    {
        Position newTarget = clampedCenter(centerTarget);

        // If smoothing is disabled, and centerTarget != null, then set center position directly.
        if (this.centerSmoothing == 0 && newTarget != null && this.orbitView != null)
        {
            this.centerTarget = null;
            this.orbitView.setCenterPosition(newTarget);
            flagViewChanged();
            setViewOutOfFocus(true);
        }
        // Otherwise, just set the target.
        else
        {
            this.centerTarget = newTarget;
        }

        // Cancel heading, pitch, and zoom targets.
        if (newTarget != null)
        {
            this.headingTarget = null;
            this.pitchTarget = null;
            this.zoomTarget = -1;
        }

        refreshView();
    }

    public Angle getHeadingTarget()
    {
        return this.headingTarget;
    }

    public void setHeadingTarget(Angle headingTarget)
    {
        Angle newTarget = normalizedHeading(headingTarget);

        if (isViewOutOfFocus())
            focusView();

        // If smoothing is disabled, and headingTarget != null, then set heading directly.
        if (this.headingSmoothing == 0 && newTarget != null && this.orbitView != null)
        {
            this.headingTarget = null;
            this.orbitView.setHeading(newTarget);
            flagViewChanged();
        }
        // Otherwise, just set the target.
        else
        {
            this.headingTarget = newTarget;
        }

        // Cancel center and zoom targets.
        if (newTarget != null)
        {
            this.centerTarget = null;
            this.zoomTarget = -1;
        }

        refreshView();
    }

    public Angle getPitchTarget()
    {
        return this.pitchTarget;
    }

    public void setPitchTarget(Angle pitchTarget)
    {
        Angle newTarget = clampedPitch(pitchTarget);

        if (isViewOutOfFocus())
            focusView();

        // If smoothing is disabled, and pitchTarget != null, then set pitch directly.
        if (this.pitchSmoothing == 0 && newTarget != null && this.orbitView != null)
        {
            this.pitchTarget = null;
            this.orbitView.setPitch(newTarget);
            flagViewChanged();
        }
        // Otherwise, just set the target.
        else
        {
            this.pitchTarget = newTarget;
        }

        // Cancel center and zoom targets,
        if (newTarget != null)
        {
            this.centerTarget = null;
            this.zoomTarget = -1;
        }

        refreshView();
    }

    public double getZoomTarget()
    {
        return this.zoomTarget;
    }

    public void setZoomTarget(double zoomTarget)
    {
        double newTarget = normalizedZoom(zoomTarget);

        if (isViewOutOfFocus())
        {
            double beforeFocus, afterFocus;
            beforeFocus = this.orbitView.getZoom();
            focusView();
            afterFocus = this.orbitView.getZoom();
            newTarget = normalizedZoom(newTarget + (afterFocus - beforeFocus));
        }

        // If smoothing is disabled, and zoomTarget >= 0, then set zoom directly.
        if (this.zoomSmoothing == 0 && newTarget >= 0 && this.orbitView != null)
        {
            this.zoomTarget = -1;
            this.orbitView.setZoom(newTarget);
            flagViewChanged();
        }
        // Otherwise, just set the target.
        else
        {
            this.zoomTarget = newTarget;
        }

        // Cancel center, heading, and pitch targets.
        if (newTarget >= 0)
        {
            this.centerTarget = null;
            this.headingTarget = null;
            this.pitchTarget = null;
        }

        refreshView();
    }

    public boolean hasTargets()
    {
        return this.centerTarget != null
            || this.headingTarget != null
            || this.pitchTarget != null
            || this.zoomTarget >= 0;
    }

    public void clearTargets()
    {
        this.centerTarget = null;
        this.headingTarget = null;
        this.pitchTarget = null;
        this.zoomTarget = -1;
        // Clear viewing flags.
        this.viewChanged = false;
        setViewOutOfFocus(false);
        this.targetStopped = false;
    }

    private static Position clampedCenter(Position unclampedCenter)
    {
        if (unclampedCenter == null)
            return null;

        // Clamp latitude to the range [-90, 90],
        // Normalize longitude to the range [-180, 180],
        // Don't change elevation.
        double lat = unclampedCenter.getLatitude().degrees;
        double lon = unclampedCenter.getLongitude().degrees;
        double elev = unclampedCenter.getElevation();
        lon = lon % 360;
        return Position.fromDegrees(
                lat > 90 ? 90 : (lat < -90 ? -90 : lat),
                lon > 180 ? lon - 360 : (lon < -180 ? 360 + lon : lon),
                elev);
    }

    private static Angle normalizedHeading(Angle unnormalizedHeading)
    {
        if (unnormalizedHeading == null)
            return null;

        // Normalize heading to the range [-180, 180].
        double degrees = unnormalizedHeading.degrees;
        double heading = degrees % 360;
        return Angle.fromDegrees(heading > 180 ? heading - 360 : (heading < -180 ? 360 + heading : heading));
    }

    private static Angle clampedPitch(Angle unclampedPitch)
    {
        if (unclampedPitch == null)
            return null;

        // Clamp pitch to the range [0, 90].
        double pitch = unclampedPitch.degrees;
        // SEG!!
        // THIS LINE LIMITS PITCH -- before 0 -> 90  Now 0 to 180
        return Angle.fromDegrees(pitch > 180 ? 180 : (pitch < 0 ? 0 : pitch));
    }

    private static double normalizedZoom(double unnormalizedZoom)
    {
        return unnormalizedZoom < 0 ? -1 : unnormalizedZoom;
    }

    public double getCenterMinEpsilon()
    {
        return this.centerMinEpsilon;
    }

    public void setCenterMinEpsilon(double centerMinEpsilon)
    {
        if (centerMinEpsilon < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", centerMinEpsilon);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centerMinEpsilon = centerMinEpsilon;
    }

    public double getHeadingMinEpsilon()
    {
        return headingMinEpsilon;
    }

    public void setHeadingMinEpsilon(double headingMinEpsilon)
    {
        if (headingMinEpsilon < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", headingMinEpsilon);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.headingMinEpsilon = headingMinEpsilon;
    }

    public double getPitchMinEpsilon()
    {
        return this.pitchMinEpsilon;
    }

    public void setPitchMinEpsilon(double pitchMinEpsilon)
    {
        if (pitchMinEpsilon < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", pitchMinEpsilon);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pitchMinEpsilon = pitchMinEpsilon;
    }

    public double getZoomMinEpsilon()
    {
        return this.zoomMinEpsilon;
    }

    public void setZoomMinEpsilon(double zoomMinEpsilon)
    {
        if (zoomMinEpsilon < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", zoomMinEpsilon);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.zoomMinEpsilon = zoomMinEpsilon;
    }

    public double getCenterSmoothing()
    {
        return this.centerSmoothing;
    }

    public void setCenterSmoothing(double centerSmoothing)
    {
        if (centerSmoothing < 0 || centerSmoothing > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", centerSmoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centerSmoothing = centerSmoothing;
    }

    public double getHeadingSmoothing()
    {
        return this.headingSmoothing;
    }

    public void setHeadingSmoothing(double headingSmoothing)
    {
        if (headingSmoothing < 0 || headingSmoothing > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", headingSmoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.headingSmoothing = headingSmoothing;
    }

    public double getPitchSmoothing()
    {
        return this.pitchSmoothing;
    }

    public void setPitchSmoothing(double pitchSmoothing)
    {
        if (pitchSmoothing < 0 || pitchSmoothing > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", pitchSmoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pitchSmoothing = pitchSmoothing;
    }

    public double getZoomSmoothing()
    {
        return this.zoomSmoothing;
    }

    public void setZoomSmoothing(double zoomSmoothing)
    {
        if (zoomSmoothing < 0 || zoomSmoothing > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", zoomSmoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.zoomSmoothing = zoomSmoothing;
    }

    public void moveViewTowardTargets()
    {
        if (this.orbitView == null)
            return;

        // Clear any locally tracked view state.
        isViewChanged();
        isTargetStopped();

        if (this.centerTarget != null)
            moveTowardCenterTarget();
        if (this.headingTarget != null)
            moveTowardHeadingTarget();
        if (this.pitchTarget != null)
            moveTowardPitchTarget();
        if (this.zoomTarget >= 0)
            moveTowardZoomTarget();

        refreshView();

        if (isTargetStopped() && !hasTargets())
        {
            if (isViewOutOfFocus())
                focusView();
            
            clearTargets();
            this.orbitView.firePropertyChange(AVKey.VIEW_QUIET, null, this.orbitView);
        }
    }

    private void moveTowardCenterTarget()
    {
        Position nextCenter = this.centerTarget;
        Position curCenter = this.orbitView.getCenterPosition();

        double latlonDifference = LatLon.greatCircleDistance(nextCenter.getLatLon(), curCenter.getLatLon()).degrees;
        double elevDifference = Math.abs(nextCenter.getElevation() - curCenter.getElevation());
        boolean stopMoving = Math.max(latlonDifference, elevDifference) < this.centerMinEpsilon;

        if (!stopMoving)
        {
            double interpolant = 1 - this.centerSmoothing;
            nextCenter = new Position(
                Angle.mix(interpolant, curCenter.getLatitude(), this.centerTarget.getLatitude()),
                Angle.mix(interpolant, curCenter.getLongitude(), this.centerTarget.getLongitude()),
                (1 - interpolant) * curCenter.getElevation() + interpolant * this.centerTarget.getElevation());
        }

        try
        {
            // Clear any previous collision state the view may have.
            this.orbitView.hadCollisions();
            this.orbitView.setCenterPosition(nextCenter);
            // If the change caused a collision, update the target center position with the
            // elevation that resolved the collision.
            if (this.orbitView.hadCollisions())
                this.centerTarget = new Position(
                        this.centerTarget.getLatLon(), this.orbitView.getCenterPosition().getElevation());
            flagViewChanged();
            setViewOutOfFocus(true);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            stopMoving = true;
        }

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.centerTarget = null;
            flagTargetStopped();
        }
    }

    private void moveTowardHeadingTarget()
    {
        Angle nextHeading = this.headingTarget;
        Angle curHeading = this.orbitView.getHeading();

        double difference = Math.abs(nextHeading.subtract(curHeading).degrees);
        boolean stopMoving = difference < this.headingMinEpsilon;

        if (!stopMoving)
        {
            double interpolant = 1 - this.headingSmoothing;
            nextHeading = Angle.mix(interpolant, curHeading, this.headingTarget);
        }

        try
        {
            this.orbitView.setHeading(nextHeading);
            flagViewChanged();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            stopMoving = true;
        }

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.headingTarget = null;
            flagTargetStopped();
        }
    }

    private void moveTowardPitchTarget()
    {
        Angle nextPitch = this.pitchTarget;
        Angle curPitch = this.orbitView.getPitch();

        double difference = Math.abs(nextPitch.subtract(curPitch).degrees);
        boolean stopMoving = difference < this.pitchMinEpsilon;

        if (!stopMoving)
        {
            double interpolant = 1 - this.pitchSmoothing;
            nextPitch = Angle.mix(interpolant, curPitch, this.pitchTarget);
        }

        try
        {
            // Clear any previous collision state the view may have.
            this.orbitView.hadCollisions();
            this.orbitView.setPitch(nextPitch);
            // If the change caused a collision, cancel future pitch changes.
            if (this.orbitView.hadCollisions())
                stopMoving = true;
            flagViewChanged();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            stopMoving = true;
        }

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.pitchTarget = null;
            flagTargetStopped();
        }
    }

    private void moveTowardZoomTarget()
    {
        double nextZoom = this.zoomTarget;
        double curZoom = this.orbitView.getZoom();

        double difference = Math.abs(nextZoom - curZoom);
        boolean stopMoving = difference < this.zoomMinEpsilon;

        if (!stopMoving)
        {
            double interpolant = 1 - this.zoomSmoothing;
            nextZoom = (1 - interpolant) * curZoom + interpolant * this.zoomTarget;
        }

        try
        {
            this.orbitView.setZoom(nextZoom);
            flagViewChanged();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            stopMoving = true;
        }

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.zoomTarget = -1;
            flagTargetStopped();
        }
    }
}
