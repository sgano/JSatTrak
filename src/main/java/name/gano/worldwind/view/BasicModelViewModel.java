/*
 * view model for orbiting view around any point in space not nessisarily centered on the earth
 *  Shawn Gano, 26 Feb 2008
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


import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

/**
 * 
 * Striped down and modified verstion of:
 * OrbitViewModel.java 3570 2007-11-18 19:56:49Z dcollins $
 */
class BasicModelViewModel
{
    private Matrix transformMatrix = null;
    
    // pivot point at look at point (are these used here?)
    private Vec4 currentLookAtPoint = null;
    private Vec4 currentPivotPoint = null;
    
    // model viewing parameters
    // center of viewing orbit
    private double[] centerPos = new double[] {0,0,7000000};//{0,0,6378100 + 10000000};
    private double[] offCenterDist = new double[] {0,0,0}; // not working right yet -need a model reference axis

    // viewing agles about center position
    private Angle theta1 = Angle.fromDegrees(0.0); // jogl x-axis rotation
    private Angle theta2 = Angle.fromDegrees(0.0); // jogl y-axis rotation
    private Angle theta3 = Angle.fromDegrees(0.0); // jogl z-axis rotation
    // viewing distance from center
    private double radiusAboutCenter = 1500000; // distance from model

   
    
    public BasicModelViewModel(DrawContext dc)
    {
        
        updateViewTransform(dc);
    }
    

    // ====================================================
    
    public void addToTheta1(double addDeg)
    {
        theta1 = theta1.addDegrees(addDeg);
        
        //System.out.println("here" + addDeg);
    }
    
    public void addToTheta2(double addDeg)
    {
        theta2 = theta2.addDegrees(addDeg);
    }
    
    public void addToTheta3(double addDeg)
    {
        theta3 = theta3.addDegrees(addDeg);
    }
    
    public void addToRadiusAboutCenter(double m)
    {
        radiusAboutCenter += m;
        
        if(radiusAboutCenter < 0)
        {
            radiusAboutCenter = 0;
        }
    }
    
    public void addToXOffsetAboutCenter(double m)
    {
        offCenterDist[0] += m;
    }
    
    public void addToYOffsetAboutCenter(double m)
    {
        offCenterDist[1] += m;
    }
    
    public void addToZOffsetAboutCenter(double m)
    {
        offCenterDist[2] += m;
    }
    
    
   


    // SEG ====================================
     // SEG
    public void updateViewTransform(DrawContext dc)
    {
        Matrix initTransform = Matrix.IDENTITY;
        
        Globe globe = dc.getGlobe();
        
        // look at angles around point
//        Angle lat = Angle.fromDegrees(0);
//        Angle lon = Angle.fromDegrees(0);
        
        // move out to earths surface
        initTransform = initTransform.multiply(Matrix.fromTranslation(
            0.0,
            0.0 ,
             -radiusAboutCenter )); // -centerPos[2] 
//        
//        initTransform = initTransform.multiply(Matrix.fromRotationX(Angle.fromDegrees(30).multiply(-1)));
//        
//        initTransform = initTransform.multiply(Matrix.fromRotationZ(pitch));
//        
        // move out to altitude
//        initTransform = initTransform.multiply(Matrix.fromTranslation(
//            0.0,
//            0.0,
//            0.0 - 10000000));
        
        // rotate around obj
        initTransform = initTransform.multiply(Matrix.fromRotationX(theta1));
        // rotat around obj        
        initTransform = initTransform.multiply(Matrix.fromRotationY(theta2.multiply(-1)));
        
        initTransform = initTransform.multiply(Matrix.fromRotationZ(theta3));
        
        // move to center point
        initTransform = initTransform.multiply(Matrix.fromTranslation(
            -centerPos[0]-offCenterDist[0],
            -centerPos[1]-offCenterDist[1] ,
            -centerPos[2]-offCenterDist[2] )); // -centerPos[2] 
        
        
        this.setTransform(dc, initTransform);
    }

    // ========================================
    
    
    
    public Matrix getTransformMatrix()
    {
        return this.transformMatrix;
    }

   
    private void setTransform(DrawContext dc, Matrix newTransformMatrix)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newTransformMatrix == null)
        {
            String message = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.transformMatrix = newTransformMatrix;
        /*this.onTransformChange(dc, newTransformMatrix);*/
    }

    private void transform(DrawContext dc, Matrix transformMatrix)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (transformMatrix == null)
        {
            String message = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return;

        Matrix newTransformMatrix = this.transformMatrix.multiply(transformMatrix);
        this.setTransform(dc, newTransformMatrix);
    }


    public Quaternion getRotation()
    {
        return Quaternion.fromMatrix(this.transformMatrix);
    }

    public void setRotation(DrawContext dc, Quaternion newRotation)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRotation == null)
        {
            String message = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return;

        Matrix posMatrix = Matrix.fromTranslation(
                this.transformMatrix.m14,
                this.transformMatrix.m24,
                this.transformMatrix.m34);
        Matrix rotMatrix = Matrix.fromQuaternion(newRotation);
        Matrix newTransformMatrix = posMatrix.multiply(rotMatrix);
        this.setTransform(dc, newTransformMatrix);
        this.clearCurrentLookAtPoint();
        this.clearCurrentPivotPoint();
    }


//    // ============== Viewing Attributes ======================= //
//
    public Vec4 getEyeVector()
    {
        if (this.transformMatrix == null)
            return null;

        return Vec4.UNIT_W.transformBy4(this.transformMatrix.getInverse());
    }

    // good method to reference - convert Vec4 to Position
    public Position getEyePosition(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 eyeVec = this.getEyeVector();
        if (eyeVec == null)
            return null;

        if (dc.getGlobe() == null)
            return null;

        return dc.getGlobe().computePositionFromPoint(eyeVec);
    }

    private void clearCurrentLookAtPoint()
    {
        this.currentLookAtPoint = null;
    }

    private void clearCurrentPivotPoint()
    {
        this.currentPivotPoint = null;
    }

    public Angle getTheta1()
    {
        return theta1;
    }

    public void setTheta1(Angle theta1)
    {
        this.theta1 = theta1;
    }

    public Angle getTheta2()
    {
        return theta2;
    }

    public void setTheta2(Angle theta2)
    {
        this.theta2 = theta2;
    }

    public Angle getTheta3()
    {
        return theta3;
    }

    public void setTheta3(Angle theta3)
    {
        this.theta3 = theta3;
    }

    public double getRadiusAboutCenter()
    {
        return radiusAboutCenter;
    }

    public void setRadiusAboutCenter(double radiusAboutCenter)
    {
        this.radiusAboutCenter = radiusAboutCenter;
    }

    public double[] getCenterPos()
    {
        return centerPos;
    }

    public void setCenterPos(double[] centerPos)
    {
        this.centerPos = centerPos;
    }

}