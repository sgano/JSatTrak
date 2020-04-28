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

/** Arbitrary-length single-precision vector class. Currently very
    simple and only supports a few needed operations. */

public class Vecf {
  private float[] data;

  public Vecf(int n) {
    data = new float[n];
  }

  public Vecf(Vecf arg) {
    data = new float[arg.data.length];
    System.arraycopy(arg.data, 0, data, 0, data.length);
  }

  public int length() {
    return data.length;
  }

  public float get(int i) {
    return data[i];
  }

  public void set(int i, float val) {
    data[i] = val;
  }

  public Vec2f toVec2f() throws DimensionMismatchException {
    if (length() != 2)
      throw new DimensionMismatchException();
    Vec2f out = new Vec2f();
    for (int i = 0; i < 2; i++) {
      out.set(i, get(i));
    }
    return out;
  }

  public Vec3f toVec3f() throws DimensionMismatchException {
    if (length() != 3)
      throw new DimensionMismatchException();
    Vec3f out = new Vec3f();
    for (int i = 0; i < 3; i++) {
      out.set(i, get(i));
    }
    return out;
  }

  public Veci toInt() {
    Veci out = new Veci(length());
    for (int i = 0; i < length(); i++) {
      out.set(i, (int) get(i));
    }
    return out;
  }
}
