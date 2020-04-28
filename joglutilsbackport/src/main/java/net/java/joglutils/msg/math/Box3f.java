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

/** Represents an axis-aligned box in 3D space. */

public class Box3f {
  private Vec3f min = new Vec3f();
  private Vec3f max = new Vec3f();

  public Box3f() {}

  public Box3f(Vec3f min, Vec3f max) {
    setMin(min);
    setMax(max);
  }

  public void  setMin(Vec3f min) { this.min.set(min); }
  public Vec3f getMin()          { return min;        }

  public void  setMax(Vec3f max) { this.max.set(max); }
  public Vec3f getMax()          { return max;        }

  public Vec3f getCenter() {
    return new Vec3f(0.5f * (min.x() + max.x()),
                     0.5f * (min.y() + max.y()),
                     0.5f * (min.z() + max.z()));
  }

  public void extendBy(Vec3f point) {
    if (point.x() < min.x()) min.setX(point.x());
    if (point.y() < min.y()) min.setY(point.y());
    if (point.z() < min.z()) min.setZ(point.z());
    if (point.x() > max.x()) max.setX(point.x());
    if (point.y() > max.y()) max.setY(point.y());
    if (point.z() > max.z()) max.setZ(point.z());
  }

  public void extendBy(Box3f box) {
    if (box.min.x() < min.x()) min.setX(box.min.x());
    if (box.min.y() < min.y()) min.setY(box.min.y());
    if (box.min.z() < min.z()) min.setZ(box.min.z());
    if (box.max.x() > max.x()) max.setX(box.max.x());
    if (box.max.y() > max.y()) max.setY(box.max.y());
    if (box.max.z() > max.z()) max.setZ(box.max.z());
  }
}
