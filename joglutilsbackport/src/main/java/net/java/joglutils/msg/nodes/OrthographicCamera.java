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

public class OrthographicCamera extends Camera {
  private static final float DEFAULT_HEIGHT = 2.0f;
  // Amount the most recently set height differed from the default
  private float heightScale = 1.0f;

  public Mat4f getProjectionMatrix() {
    if (projDirty) {
      projMatrix.makeIdent();
      projDirty = false;

      // Recompute matrix based on current parameters
      float zNear = getNearDistance();
      float zFar  = getFarDistance();
      float deltaZ = zFar - zNear;
      float aspect = getAspectRatio();
      float height = heightScale * DEFAULT_HEIGHT;
      float width  = height * aspect;

      if ((height == 0) || (width == 0) || (deltaZ == 0))
        return projMatrix;

      // This is a simplified version of the orthographic projection
      // matrix where it's symmetric about the origin
      projMatrix.set(0, 0,  2.0f / width);
      projMatrix.set(1, 1,  2.0f / height);
      projMatrix.set(2, 2, -2.0f / deltaZ);
      projMatrix.set(2, 3, -(zFar + zNear) / deltaZ);
    }

    return projMatrix;
  }

  /** Sets the height, in units, of the volume this orthographic
      camera views. The default height is 2.0 units. */
  public void setHeight(float height) {
    heightScale = height / DEFAULT_HEIGHT;
  }

  /** Returns the height, in units, of the volume this orthographic
      camera views. */
  public float getHeight() {
    return heightScale * DEFAULT_HEIGHT;
  }

  /** Returns the width, in units, of the volume this orthographic
      camera views, assuming the passed-in aspect ratio. */
  public float getWidth(float aspectRatio) {
    return getHeight() * aspectRatio;
  }

  /** Returns the width, in units, of the volume this orthographic
      camera views, assuming the camera's currently-set aspect
      ratio. */
  public float getWidth() {
    return getWidth(getAspectRatio());
  }

  protected Vec3f getRayStartPoint(Vec2f point, Vec3f unprojectedPoint) {
    // The easiest way to compute a reasonable ray start point is to
    // start from the unprojected 3D point and go back along the Z-axis
    Vec3f backward = Vec3f.Z_AXIS.times(getNearDistance());
    backward = getOrientation().rotateVector(backward);
    return unprojectedPoint.plus(backward);
  }

  public void render(GLRenderAction action) {
    // FIXME: unclear whether we should be doing this, or whether we
    // should have a mechanism which doesn't require mutation of the
    // camera
    setAspectRatio(action.getCurAspectRatio());
    doAction(action);
  }
}
