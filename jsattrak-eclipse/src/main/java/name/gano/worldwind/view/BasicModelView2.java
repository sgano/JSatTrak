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
 * View to orbit any point/position (for instance to view a model)
 * (Earth Inertial View Reference Frame)
 * Shawn Gano
 */

package name.gano.worldwind.view;

import javax.media.opengl.GL;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;



/**
 * @author dcollins
 * @version $Id: BasicOrbitView.java 3557 2007-11-17 04:10:32Z dcollins $
 */
public class BasicModelView2 extends BasicOrbitView //implements OrbitView
{
    // View attributes.
    private BasicModelViewModel basicModelViewModel;
    private Frustum frustum = new Frustum();//null;
    
     private Globe globe;
     
     private Position iniPosition;
     
     double minClipDist = 100; // 900000
     
     // new 
     private double nearClipDistance = -1; // Default to auto-configure.
    private double farClipDistance = -1;  // Default to auto-configure.
    private java.awt.Rectangle viewport = new java.awt.Rectangle();
    private Angle fieldOfView = Angle.fromDegrees(45);
    // Properties updated during the most recent call to apply().
    private DrawContext dc;
    
    // TODO: make configurable
    private static final double MINIMUM_NEAR_DISTANCE = 2;
    private static final double MINIMUM_FAR_DISTANCE = 100;

     // constructors
    public BasicModelView2(Position centerPosition, Globe globe)
    {
        super();
        
        //doInitialize()
        
        this.globe = globe;
        
        iniPosition = centerPosition; // save for inialization  
    }
    
     public void setCenterPosition(Position centerPosition)
    {
         if(globe == null)
         {
            // basicModelViewModel.setCenterPos(new double[] {0,0,5000000});
             return; // DO NOTHING< WHAT ABOUT INI>???
         }
                
         
        Vec4 v4 = globe.computePointFromPosition(centerPosition);
        
        double[] centerPos = new double[3];
        centerPos[0] = v4.getX();
        centerPos[1] = v4.getY();
        centerPos[2] = v4.getZ();
        
        basicModelViewModel.setCenterPos(centerPos);
    }
    
    
    // ============== Viewing State ======================= 
    // updated in input broker
    
    public void addToTheta1(float Dtheta1)
    {
        basicModelViewModel.addToTheta1(Dtheta1);
    }
    
    public void addToTheta2(float Dtheta2)
    {
        basicModelViewModel.addToTheta2(Dtheta2);
    }
    
    public void addToTheta3(float Dtheta3)
    {        
        basicModelViewModel.addToTheta3(Dtheta3);
    }
    
    public void addToRadiusAboutCenter(float meters)
    {
        basicModelViewModel.addToRadiusAboutCenter(meters);
    }
    
    public void addToXOffsetAboutCenter(float meters)
    {
        basicModelViewModel.addToXOffsetAboutCenter(meters);
    }
    
    public void addToYOffsetAboutCenter(float meters)
    {
        basicModelViewModel.addToYOffsetAboutCenter(meters);
    }
    
    public void addToZOffsetAboutCenter(float meters)
    {
        basicModelViewModel.addToZOffsetAboutCenter(meters);
    }
    
    //================================================
    
    protected void doApply(DrawContext dc)
    {
        if (this.basicModelViewModel == null)
        {
            this.dc = dc;
             this.globe = this.dc.getGlobe();
             
            doInitialize(dc);
             
            //return;
        }
            

        Vec4 eyeVec = this.basicModelViewModel.getEyeVector();
        if (eyeVec == null)
            return;

        // Update DrawContext and Globe references.
        this.dc = dc;
        this.globe = this.dc.getGlobe();
        
        // Get the current OpenGL viewport state.
        int[] viewportArray = new int[4];
        dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
        this.viewport = new java.awt.Rectangle(viewportArray[0], viewportArray[1], viewportArray[2], viewportArray[3]);
        
        // Compute current viewing attributes.
        double nearDistance = this.nearClipDistance > 0 ? this.nearClipDistance : getAutoNearClipDistance();
        double farDistance = this.farClipDistance > 0 ? this.farClipDistance : getAutoFarClipDistance();
        
        this.frustum = this.createFrustum(nearDistance, farDistance);

        // SEG  -- creakte ransformation matrix given current viewint properties
        basicModelViewModel.updateViewTransform(dc); // update view angles etc.
        
        // Set current GL viewing state.
        Matrix modelViewMatrix =  this.createModelViewMatrix(this.basicModelViewModel);
        Matrix projectionMatrix = this.createProjectionMatrix(nearDistance, farDistance);
        
        //this.loadModelViewProjection(dc, modelViewMatrix, projectionMatrix);
        loadGLViewState(dc, modelViewMatrix, projectionMatrix);
    }

    public Frustum getFrustum()
    {
        return this.frustum;
    }

    private Matrix createModelViewMatrix(BasicModelViewModel orbitViewModel)
    {
        if (orbitViewModel == null)
            return null;
        
        return orbitViewModel.getTransformMatrix();
    }

    private Matrix createProjectionMatrix(double nearClipDistance, double farClipDistance)
    {
        Angle fov = this.getFieldOfView();
        if (fov == null)
            return null;

        java.awt.Rectangle viewport = this.getViewport();
        if (viewport == null)
            return null;

        // Create a standard perspective projection.
        return Matrix.fromPerspective(
            fov,
            viewport.width, viewport.height,
            nearClipDistance, farClipDistance);
    }

    private Frustum createFrustum(double nearClipDistance, double farClipDistance)
    {
        Angle fov = this.getFieldOfView();
        if (fov == null)
            return null;

        java.awt.Rectangle viewport = this.getViewport();
//        if (viewport == null)
//            return null;
        
        // Create a standard perspective frustum.
        return Frustum.fromPerspective(
            fov, viewport.width, viewport.height,
            nearClipDistance, farClipDistance);
    }

    // ============== Attribute Initialization ======================= //

    protected void doInitialize(DrawContext dc)
    {
        this.basicModelViewModel = new BasicModelViewModel(dc); 
        
        setCenterPosition(iniPosition); 
    }
    
    
    // get / set  -- and other needed functions
    public Angle getFieldOfView()
    {
        return this.fieldOfView;
    }

    public void setFieldOfView(Angle fieldOfView)
    {
        if (fieldOfView == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.fieldOfView = fieldOfView;
    }

    public double getNearClipDistance()
    {
        return this.nearClipDistance;
    }

    public void setNearClipDistance(double distance)
    {
        this.nearClipDistance = distance;
    }

    public double getFarClipDistance()
    {
        return this.farClipDistance;
    }

    public void setFarClipDistance(double distance)
    {
        this.farClipDistance = distance;
    }

    public double getAutoNearClipDistance()
    {
        Position eyePos = getCurrentEyePosition();
        return computeNearDistance(eyePos);
    }

    public double getAutoFarClipDistance()
    {
        Position eyePos = getCurrentEyePosition();
        return computeFarDistance(eyePos);
    }
    
    public java.awt.Rectangle getViewport()
    {
        // java.awt.Rectangle is mutable, so we defensively copy the viewport.
        return new java.awt.Rectangle(this.viewport);
    }
    
    public Position getCurrentEyePosition()
    {
        if (this.globe != null)
        {
//            Matrix modelview = this.orbitViewModel.computeTransformMatrix(this.globe, this.center,
//                    this.heading, this.pitch, this.zoom);
//            if (modelview != null)
//            {
//                Matrix modelviewInv = modelview.getInverse();
//                if (modelviewInv != null)
//                {
//                    Vec4 eyePoint = Vec4.UNIT_W.transformBy4(modelviewInv);
//                    return this.globe.computePositionFromPoint(eyePoint);
//                }
//            }
            
            return basicModelViewModel.getEyePosition(dc);
        }

        return Position.ZERO;
    }
    
     protected double computeNearDistance(Position eyePosition)
    {
        double near = 0;
        if (eyePosition != null && this.dc != null)
        {
            double elevation = ViewUtil.computeElevationAboveSurface(this.dc, eyePosition);
            double tanHalfFov = this.fieldOfView.tanHalfAngle();
            near = elevation / (2 * Math.sqrt(2 * tanHalfFov * tanHalfFov + 1));
        }
        return near < MINIMUM_NEAR_DISTANCE ? MINIMUM_NEAR_DISTANCE : near;
    }

    protected double computeFarDistance(Position eyePosition)
    {
        double far = 0;
        if (eyePosition != null)
        {
            far = computeHorizonDistance(eyePosition);
        }
        
        return far < MINIMUM_FAR_DISTANCE ? MINIMUM_FAR_DISTANCE : far;
    }
    
    protected double computeHorizonDistance(Position eyePosition)
    {
        if (this.globe != null && eyePosition != null)
        {
            double elevation = eyePosition.getElevation();
            double elevationAboveSurface = ViewUtil.computeElevationAboveSurface(this.dc, eyePosition);
            return ViewUtil.computeHorizonDistance(this.globe, Math.max(elevation, elevationAboveSurface));
        }

        return 0;
    }
    
    public double computeHorizonDistance()
    {
        double horizon = 0;
        Position eyePos = computeEyePositionFromModelview();
        if (eyePos != null)
        {
            horizon = computeHorizonDistance(eyePos);
        }

        return horizon;
    }
    
    protected Position computeEyePositionFromModelview()
    {
//        if (this.globe != null)
//        {
//            Vec4 eyePoint = Vec4.UNIT_W.transformBy4(this.modelviewInv);
//            return this.globe.computePositionFromPoint(eyePoint);
//        }

        return Position.ZERO;
    }
    
}

  
