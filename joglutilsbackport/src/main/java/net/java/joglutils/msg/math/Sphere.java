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

/** Represents a sphere. */

public class Sphere {
  private Vec3f center = new Vec3f();
  private float radius;
  private float radSq;

  /** Default constructor creates a sphere with center (0, 0, 0) and
      radius 0 */
  public Sphere() {
    makeEmpty();
  }

  public Sphere(Vec3f center, float radius) {
    set(center, radius);
  }

  public Sphere(Sphere other) {
    set(other);
  }

  public Sphere(Box3f box) {
    set(box);
  }

  /** Re-initialize this sphere to center (0, 0, 0) and radius 0 */
  public void makeEmpty() {
    center.set(0, 0, 0);
    radius = radSq = 0;
  }

  public void  setCenter(Vec3f center) { this.center.set(center); }
  public Vec3f getCenter()             { return center;           }

  public void  setRadius(float radius) { this.radius = radius;
                                         radSq = radius * radius; }
  public float getRadius()             { return radius;           }

  public void set(Vec3f center, float radius) {
    setCenter(center); setRadius(radius); 
  }
  /** Returns radius and mutates passed "center" vector */
  float get(Vec3f center) {
    center.set(this.center); return radius;
  }

  /** Sets the parameters of this sphere to those of the given one */
  public void set(Sphere other) {
    set(other.center, other.radius);
  }

  /** Sets this sphere to encompass the given box */
  public void set(Box3f box) {
    Vec3f max = box.getMax();
    Vec3f ctr = box.getCenter();
    Vec3f diff = max.minus(ctr);
    set(ctr, diff.length());
  }

  /** Mutate this sphere to encompass both itself and the argument.
      Ignores zero-size arguments. */
  public void extendBy(Sphere arg) {
    if ((radius == 0.0f) || (arg.radius == 0.0f))
      return;
    // FIXME: This algorithm is a quick hack -- minimum bounding
    // sphere of a set of other spheres is a well studied problem, but
    // not by me
    Vec3f diff = arg.center.minus(center);
    if (diff.lengthSquared() == 0.0f) {
      setRadius(Math.max(radius, arg.radius));
      return;
    }
    IntersectionPoint[] intPt = new IntersectionPoint[4];
    for (int i = 0; i < intPt.length; i++) {
      intPt[i] = new IntersectionPoint();
    }
    int numIntersections;
    numIntersections = intersectRay(center, diff, intPt[0], intPt[1]);
    assert numIntersections == 2;
    numIntersections = intersectRay(center, diff, intPt[2], intPt[3]);
    assert numIntersections == 2;
    IntersectionPoint minIntPt = intPt[0];
    IntersectionPoint maxIntPt = intPt[0];
    // Find minimum and maximum t values, take associated intersection
    // points, find midpoint and half length of line segment -->
    // center and radius.
    for (int i = 0; i < 4; i++) {
      if (intPt[i].getT() < minIntPt.getT()) {
        minIntPt = intPt[i];
      } else if (intPt[i].getT() > maxIntPt.getT()) {
        maxIntPt = intPt[i];
      }
    }
    // Compute the average -- this is the new center
    center.add(minIntPt.getIntersectionPoint(),
               maxIntPt.getIntersectionPoint());
    center.scale(0.5f);
    // Compute half the length -- this is the radius
    setRadius(
      0.5f *
      minIntPt.getIntersectionPoint().
        minus(maxIntPt.getIntersectionPoint()).
          length()
    );
  }

  /** Intersect a ray with the sphere. This is a one-sided ray
      cast. Mutates one or both of intPt0 and intPt1. Returns number
      of intersections which occurred. */
  int intersectRay(Vec3f rayStart,
                   Vec3f rayDirection,
                   IntersectionPoint intPt0,
                   IntersectionPoint intPt1) {
    // Solve quadratic equation
    float a = rayDirection.lengthSquared();
    if (a == 0.0)
      return 0;
    float b = 2.0f * (rayStart.dot(rayDirection) - rayDirection.dot(center));
    Vec3f tempDiff = center.minus(rayStart);
    float c = tempDiff.lengthSquared() - radSq;
    float disc = b * b - 4 * a * c;
    if (disc < 0.0f)
      return 0;
    int numIntersections;
    if (disc == 0.0f)
      numIntersections = 1;
    else
      numIntersections = 2;
    intPt0.setT((0.5f * (-1.0f * b + (float) Math.sqrt(disc))) / a);
    if (numIntersections == 2)
      intPt1.setT((0.5f * (-1.0f * b - (float) Math.sqrt(disc))) / a);
    Vec3f tmp = new Vec3f(rayDirection);
    tmp.scale(intPt0.getT());
    tmp.add(tmp, rayStart);
    intPt0.setIntersectionPoint(tmp);
    if (numIntersections == 2) {
      tmp.set(rayDirection);
      tmp.scale(intPt1.getT());
      tmp.add(tmp, rayStart);
      intPt1.setIntersectionPoint(tmp);
    }
    return numIntersections;
  }
}
