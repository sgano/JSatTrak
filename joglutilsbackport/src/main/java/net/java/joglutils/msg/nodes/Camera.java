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
import net.java.joglutils.msg.elements.*;
import net.java.joglutils.msg.math.*;

/** Represents a camera which is used to view the scene. The camera
    should be added to the scene graph before the geometry it is
    intended to view. <P>

    The camera's default parameters are a position at (0, 0, 1),
    facing down the negative Z axis with the Y axis up, an aspect
    ratio of 1.0, a near distance of 1.0, a far distance of 100.0, and
    a focal distance of 10.0.
*/

public abstract class Camera extends Node {
  private Vec3f position;
  private Rotf  orientation;
  private float aspectRatio      = 1.0f;
  private float nearDistance     = 1.0f;
  private float farDistance      = 100.0f;
  private float focalDistance    = 10.0f;

  protected boolean projDirty;
  protected boolean viewDirty;
  protected Mat4f projMatrix;
  protected Mat4f viewMatrix;

  static {
    // Enable the elements this node affects for known actions
    // Note that all of these elements are interdependent
    GLModelMatrixElement      .enable(GLRenderAction.getDefaultState());
    GLProjectionMatrixElement .enable(GLRenderAction.getDefaultState());
    GLViewingMatrixElement    .enable(GLRenderAction.getDefaultState());

    ModelMatrixElement     .enable(RayPickAction.getDefaultState());
    ProjectionMatrixElement.enable(RayPickAction.getDefaultState());
    ViewingMatrixElement   .enable(RayPickAction.getDefaultState());
  }

  public Camera() {
    position = new Vec3f(0, 0, 1);
    orientation = new Rotf();

    projMatrix = new Mat4f();
    viewMatrix = new Mat4f();
    projDirty = true;
    viewDirty = true;
  }

  /** Sets the position of the camera. */
  public void setPosition(Vec3f position) {
    this.position.set(position);
    viewDirty = true;
  }

  /** Returns the position of the camera. */
  public Vec3f getPosition() {
    return position;
  }

  /** Sets the orientation of the camera. */
  public void setOrientation(Rotf orientation) {
    this.orientation.set(orientation);
    viewDirty = true;
  }

  /** Returns the orientation of the camera. */
  public Rotf getOrientation() {
    return orientation;
  }

  /** Sets the aspect ratio of the camera -- the width of the viewport
      divided by the height of the viewport. */
  public void setAspectRatio(float aspectRatio) {
    if (aspectRatio == this.aspectRatio)
      return;
    this.aspectRatio = aspectRatio;
    projDirty = true;
  }

  /** Returns the aspect ratio of the camera -- the width of the
      viewport divided by the height of the viewport. */
  public float getAspectRatio() {
    return aspectRatio;
  }

  /** Sets the distance from the eye point to the near clipping plane. */
  public void setNearDistance(float nearDistance) {
    this.nearDistance = nearDistance;
    projDirty = true;
  }

  /** Returns the distance from the eye point to the near clipping plane. */
  public float getNearDistance() {
    return nearDistance;
  }

  /** Sets the distance from the eye point to the far clipping plane. */
  public void setFarDistance(float farDistance) {
    this.farDistance = farDistance;
    projDirty = true;
  }

  /** Returns the distance from the eye point to the far clipping plane. */
  public float getFarDistance() {
    return farDistance;
  }

  /** Sets the distance from the eye point to the focal point of the
      scene. This is only used for mouse-based interaction with the
      scene and is not factored in to the rendering process. */
  public void setFocalDistance(float focalDistance) {
    this.focalDistance = focalDistance;
    projDirty = true;
  }

  /** Returns the distance from the eye point to the focal point of
      the scene. This is only used for mouse-based interaction with
      the scene and is not factored in to the rendering process. */
  public float getFocalDistance() {
    return focalDistance;
  }

  /** Returns the viewing matrix associated with this camera's parameters. */
  public Mat4f getViewingMatrix() {
    if (viewDirty) {
      viewMatrix.makeIdent();
      viewDirty = false;

      viewMatrix.setRotation(getOrientation());
      viewMatrix.setTranslation(getPosition());
      viewMatrix.invertRigid();
    }

    return viewMatrix;
  }

  /** Returns the projection matrix associated with this camera's parameters. */
  public abstract Mat4f getProjectionMatrix();

  /** Un-projects the given on-screen point to a line in 3D space
      which can be used for picking or other operations. The x and y
      coordinates of the point must be in normalized coordinates,
      where (0, 0) is the lower-left corner of the viewport and (1, 1)
      is the upper-right. Allocates new storage for the returned
      Line. */
  public Line unproject(Vec2f point) {
    Line line = new Line();
    unproject(point, line);
    return line;
  }

  /** Un-projects the given on-screen point in to the given line in 3D
      space (in world coordinates) which can be used for picking or
      other operations. The x and y coordinates of the point must be
      in normalized coordinates, where (0, 0) is the lower-left corner
      of the viewport and (1, 1) is the upper-right. */
  public void unproject(Vec2f point, Line line) throws SingularMatrixException {
    // First, we are going to compute the 3D point which corresponds
    // to the given point on the near plane. Map the screen
    // coordinates to the (-1, 1) range. Note that because the camera
    // points down the -Z axis, we use as the initial Z coordinate of
    // the 3D point we need to unproject the negation of the near
    // distance.
    Vec4f pt3d = new Vec4f(2 * point.x() - 1,
                           2 * point.y() - 1,
                           -getNearDistance(),
                           1);
    // Compute the cumulative view and projection matrices
    Mat4f mat = new Mat4f();
    mat.mul(getProjectionMatrix(), getViewingMatrix());
    // Compute the inverse of this matrix
    mat.invert();
    // Multiply
    Vec4f unproj = new Vec4f();
    mat.xformVec(pt3d, unproj);
    if (unproj.z() == 0) {
      // FIXME: is this the right exception to throw in this case?
      throw new SingularMatrixException();
    }
    float ooZ = 1.0f / unproj.w();
    Vec3f to = new Vec3f(unproj.x() * ooZ,
                         unproj.y() * ooZ,
                         unproj.z() * ooZ);
    Vec3f from = getRayStartPoint(point, to);
    Vec3f dir  = to.minus(from);

    //    System.err.println("unprojected point: " + to);
    //    System.err.println("unprojected dir  : " + dir);

    line.setPoint(from);
    line.setDirection(dir);
  }

  /** Computes the start point of a ray for picking, given a point in
      normalized screen coordinates ((0, 0) to (1, 1)) and a 3D point
      which that point unprojects to. */
  protected abstract Vec3f getRayStartPoint(Vec2f point, Vec3f unprojectedPoint);

  public void doAction(Action action) {
    if (ViewingMatrixElement.isEnabled(action.getState())) {
      ViewingMatrixElement.set(action.getState(), getViewingMatrix());
    }
    if (ProjectionMatrixElement.isEnabled(action.getState())) {
      ProjectionMatrixElement.set(action.getState(), getProjectionMatrix());
    }
  }

  public void rayPick(RayPickAction action) {
    doAction(action);
    action.recomputeRay(this);
  }
}
