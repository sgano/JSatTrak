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

/** Utility math routines. */

public class MathUtil {
  /** Makes an arbitrary vector perpendicular to <B>src</B> and
      inserts it into <B>dest</B>. Returns false if the source vector
      was equal to (0, 0, 0). */
  public static boolean makePerpendicular(Vec3f src,
                                          Vec3f dest) {
    if ((src.x() == 0.0f) && (src.y() == 0.0f) && (src.z() == 0.0f)) {
      return false;
    }

    if (src.x() != 0.0f) {
      if (src.y() != 0.0f) {
	dest.set(-src.y(), src.x(), 0.0f);
      }	else {
	dest.set(-src.z(), 0.0f, src.x());
      }
    } else {
      dest.set(1.0f, 0.0f, 0.0f);
    }
    return true;
  }

  /** Returns 1 if the sign of the given argument is positive; -1 if
      negative; 0 if 0. */
  public static int sgn(float f) {
    if (f > 0) {
      return 1;
    } else if (f < 0) {
      return -1;
    }
    return 0;
  }

  /** Clamps argument between min and max values. */
  public static float clamp(float val, float min, float max) {
    if (val < min) return min;
    if (val > max) return max;
    return val;
  }

  /** Clamps argument between min and max values. */
  public static int clamp(int val, int min, int max) {
    if (val < min) return min;
    if (val > max) return max;
    return val;
  }
}
