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

package net.java.joglutils.msg.math;

/** Represents a plane in 3D space. */

public class Plane {
  /** Normalized */
  private Vec3f normal;
  private Vec3f point;
  /** Constant for faster projection and intersection */
  float c;

  /** Default constructor initializes normal to (0, 1, 0) and point to
      (0, 0, 0) */
  public Plane() {
    normal = new Vec3f(0, 1, 0);
    point = new Vec3f(0, 0, 0);
    recalc();
  }

  /** Sets all parameters of plane. Plane has normal <b>normal</b> and
      goes through the point <b>point</b>. Normal does not need to be
      unit length but must not be the zero vector. */
  public Plane(Vec3f normal, Vec3f point) {
    this.normal = new Vec3f(normal);
    this.normal.normalize();
    this.point = new Vec3f(point);
    recalc();
  }

  /** Setter does some work to maintain internal caches. Normal does
      not need to be unit length but must not be the zero vector. */
  public void setNormal(Vec3f normal) {
    this.normal.set(normal);
    this.normal.normalize();
    recalc();
  }

  /** Normal is normalized internally, so <b>normal</b> is not
      necessarily equal to <code>plane.setNormal(normal);
      plane.getNormal();</code> */
  public Vec3f getNormal() {
    return normal;
  }

  /** Setter does some work to maintain internal caches */
  public void setPoint(Vec3f point) {
    this.point.set(point);
    recalc();
  }

  public Vec3f getPoint() {
    return point;
  }

  /** Project a point onto the plane */
  public void projectPoint(Vec3f pt,
                           Vec3f projPt) {
    float scale = normal.dot(pt) - c;
    projPt.set(pt.minus(normal.times(normal.dot(point) - c)));
  }

  /** Intersect a ray with the plane. Returns true if intersection occurred, false
      otherwise. This is a two-sided ray cast. */
  public boolean intersectRay(Vec3f rayStart,
                              Vec3f rayDirection,
                              IntersectionPoint intPt) {
    float denom = normal.dot(rayDirection);
    if (denom == 0)
      return false;
    intPt.setT((c - normal.dot(rayStart)) / denom);
    intPt.setIntersectionPoint(rayStart.plus(rayDirection.times(intPt.getT())));
    return true;
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //
  
  private void recalc() {
    c = normal.dot(point);
  }
}
