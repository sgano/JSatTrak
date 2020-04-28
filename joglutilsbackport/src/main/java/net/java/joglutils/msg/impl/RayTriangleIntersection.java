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

package net.java.joglutils.msg.impl;

import net.java.joglutils.msg.math.*;

/** Intersection of ray with triangle. Computes parameteric t along
    with barycentric coordinates (u, v) indicating weight of vert1 and
    vert2 (weight of vert0 = 1 - u - v) which can be used to
    interpolate normals and texture coordinates. Two versions, one for
    two-sided triangle casts and one including backface
    culling. Algorithm from Moller, Trumbore, "Fast, Minimum Storage
    Ray / Triangle Intersection", Journal of Graphics Tools, Volume 2,
    Number 1, 1997, pp. 21-28. */

public class RayTriangleIntersection {
  private Vec3f edge1 = new Vec3f();
  private Vec3f edge2 = new Vec3f();
  private Vec3f tvec = new Vec3f();
  private Vec3f pvec = new Vec3f();
  private Vec3f qvec = new Vec3f();

  private static final float EPSILON = 0.000001f;

  public boolean intersectTriangle(Line ray,
                                   Vec3f vert0,
                                   Vec3f vert1,
                                   Vec3f vert2,
                                   Vec3f tuv) {
    // Find vectors for two edges sharing vert0
    edge1.sub(vert1, vert0);
    edge2.sub(vert2, vert0);

    // Begin calculating determinant -- also used to calculate U parameter
    pvec.cross(ray.getDirection(), edge2);
    
    // If determinant is near zero, ray lies in plane of triangle
    float det = edge1.dot(pvec);

    if (det > -EPSILON && det < EPSILON)
      return false;

    float invDet = 1.0f / det;

    // Calculate distance from vert0 to ray origin
    tvec.sub(ray.getPoint(), vert0);

    // Calculate U parameter and test bounds
    float u = tvec.dot(pvec) * invDet;
    if (u < 0.0f || u > 1.0f)
      return false;

    // Prepare to test V parameter
    qvec.cross(tvec, edge1);

    // Calculate V parameter and test bounds
    float v = ray.getDirection().dot(qvec) * invDet;
    if (v < 0.0f || (u + v) > 1.0f)
      return false;

    // Calculate t, ray intersects triangle
    float t = edge2.dot(qvec) * invDet;

    tuv.set(t, u, v);
    return true;
  }

  public boolean intersectTriangleBackfaceCulling(Line ray,
                                                  Vec3f vert0,
                                                  Vec3f vert1,
                                                  Vec3f vert2,
                                                  Vec3f tuv) {
    // Find vectors for two edges sharing vert0
    edge1.sub(vert1, vert0);
    edge2.sub(vert2, vert0);

    // Begin calculating determinant -- also used to calculate U parameter
    pvec.cross(ray.getDirection(), edge2);
    
    // If determinant is near zero, ray lies in plane of triangle
    float det = edge1.dot(pvec);

    if (det < EPSILON)
      return false;

    // Calculate distance from vert0 to ray origin
    tvec.sub(ray.getPoint(), vert0);

    // Calculate U parameter and test bounds
    float u = tvec.dot(pvec);
    if (u < 0.0f || u > det)
      return false;

    // Prepare to test V parameter
    qvec.cross(tvec, edge1);

    // Calculate V parameter and test bounds
    float v = ray.getDirection().dot(qvec);
    if (v < 0.0f || (u + v) > det)
      return false;

    // Calculate t, scale parameters, ray intersects triangle
    float t = edge2.dot(qvec);
    float invDet = 1.0f / det;
    t *= invDet;
    u *= invDet;
    v *= invDet;
    tuv.set(t, u, v);
    return true;
  }
}
