/*
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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;


/**
 * Adds better auto clipping calculations for JSatTrak applications (needing to see beyond the earth)
 * Currently only works for Earth Centered view - not model centered
 * @author Shawn E. Gano
 */
public class AutoClipBasicOrbitView extends BasicOrbitView
{
    private double refRadius = 6378137.0;

    private float autoNearClipFactor = 5.0f; // the near clip plane is set to zoomFactor/this value
    private float autoFarClipFactor = 200.0f;


    
    public double getAutoNearClipDistance()
    {
        Position eyePos = getCurrentEyePosition();
        double near = computeNearDistance(eyePos);

        //near = (near - zeroZoom) / autoNearClipFactor;
        // nearClippingPlaneDist = (zoomFactor - zeroZoom) / autoNearClipFactor;

        return near/4.0;
    }

    
    public double getAutoFarClipDistance()
    {
        Position eyePos = getCurrentEyePosition();
        return computeFarDistance(eyePos)*4.0; // SEG - 2x
    }

    @Override
    protected double computeNearDistance(Position eyePosition)
    {
        double near = 0;
        if (eyePosition != null && this.dc != null)
        {
        	
            double elevation = ViewUtil.computeElevationAboveSurface(this.dc, eyePosition);
            double tanHalfFov = getFieldOfView().tanHalfAngle();
            near = elevation / (2 * Math.sqrt(2 * tanHalfFov * tanHalfFov + 1));
        }
        return near < MINIMUM_NEAR_DISTANCE ? MINIMUM_NEAR_DISTANCE : near;
    }

    @Override
    protected double computeFarDistance(Position eyePosition)
    {
        double far = 0;
        if (eyePosition != null)
        {
            far = computeHorizonDistance(eyePosition);
        }

        return far < MINIMUM_FAR_DISTANCE ? MINIMUM_FAR_DISTANCE : far;
    }
}
