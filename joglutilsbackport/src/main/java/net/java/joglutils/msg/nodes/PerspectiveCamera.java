/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 */

package net.java.joglutils.msg.nodes;

import net.java.joglutils.msg.actions.*;
import net.java.joglutils.msg.math.*;

/** Represents a camera utilizing a perspective projection. <P>

    The default height angle is Math.PI / 4 radians (45 degrees),
    meaning that the camera has a total vertical field of view of 90
    degrees.
*/

public class PerspectiveCamera extends Camera {
  private static final float DEFAULT_HEIGHT_ANGLE = (float) (Math.PI / 4);
  // Amount the most recently set height angle differed from the default
  private float vertFOVScale = 1.0f;
  
  public Mat4f getProjectionMatrix() {
    if (projDirty) {
      projMatrix.makeIdent();
      projDirty = false;

      // Recompute matrix based on current parameters
      float zNear = getNearDistance();
      float zFar  = getFarDistance();
      float deltaZ = zFar - zNear;
      float aspect = getAspectRatio();
      float radians = vertFOVScale * DEFAULT_HEIGHT_ANGLE;
      float sine = (float) Math.sin(radians);
      if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
        return projMatrix;
      }
      
      float cotangent = (float) Math.cos(radians) / sine;
      projMatrix.set(0, 0, cotangent / aspect);
      projMatrix.set(1, 1, cotangent);
      projMatrix.set(2, 2, -(zFar + zNear) / deltaZ);
      projMatrix.set(3, 2, -1);
      projMatrix.set(2, 3, -2 * zNear * zFar / deltaZ);
      projMatrix.set(3, 3, 0);
    }

    return projMatrix;
  }

  /** Sets the height angle, in radians, of this perspective camera.
      The default height angle is Math.PI / 4 radians, or 45 degrees. */
  public void setHeightAngle(float heightAngle) {
    vertFOVScale = heightAngle / DEFAULT_HEIGHT_ANGLE;
    projDirty = true;
  }

  /** Returns the height angle, in radians, of this perspective
      camera. */
  public float getHeightAngle() {
    return vertFOVScale * DEFAULT_HEIGHT_ANGLE;
  }

  /** Returns the width angle, in radians, of this perspective
      camera, assuming the passed-in aspect ratio. */
  public float getWidthAngle(float aspectRatio) {
    return (float) Math.atan(aspectRatio * Math.tan(getHeightAngle()));
  }

  /** Returns the width angle, in radians, of this perspective camera,
      assuming the camera's currently-set aspect ratio. */
  public float getWidthAngle() {
    return getWidthAngle(getAspectRatio());
  }

  protected Vec3f getRayStartPoint(Vec2f point, Vec3f unprojectedPoint) {
    return getPosition();
  }

  public void render(GLRenderAction action) {
    // FIXME: unclear whether we should be doing this, or whether we
    // should have a mechanism which doesn't require mutation of the
    // camera
    setAspectRatio(action.getCurAspectRatio());
    doAction(action);
  }
}
