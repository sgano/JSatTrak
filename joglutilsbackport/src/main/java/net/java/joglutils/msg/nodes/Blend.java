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

/** Provides control over OpenGL blending modes. */

public class Blend extends Node {
  private boolean enabled;
  private Vec4f blendColor = new Vec4f();
  private int srcFunc = ONE;
  private int destFunc = ZERO;
  private int blendEquation = FUNC_ADD;

  static {
    // Enable the elements this node affects for known actions
    GLBlendElement.enable(GLRenderAction.getDefaultState());
  }

  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int ZERO                     = 1;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int ONE                      = 2;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int SRC_COLOR                = 3;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int ONE_MINUS_SRC_COLOR      = 4;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int DST_COLOR                = 5;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int ONE_MINUS_DST_COLOR      = 6;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int SRC_ALPHA                = 7;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int ONE_MINUS_SRC_ALPHA      = 8;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int DST_ALPHA                = 9;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int ONE_MINUS_DST_ALPHA      = 10;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int SRC_ALPHA_SATURATE       = 11;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int CONSTANT_COLOR           = 12;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int ONE_MINUS_CONSTANT_COLOR = 13;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int CONSTANT_ALPHA           = 14;
  /** One of the blend functions. See the OpenGL documentation for glBlendFunc for more details. */
  public static final int ONE_MINUS_CONSTANT_ALPHA = 15;

  /** One of the blend equations. See the OpenGL documentation for glBlendEquation for more details. */
  public static final int FUNC_ADD              = 1;
  /** One of the blend equations. See the OpenGL documentation for glBlendEquation for more details. */
  public static final int FUNC_SUBTRACT         = 2;
  /** One of the blend equations. See the OpenGL documentation for glBlendEquation for more details. */
  public static final int FUNC_REVERSE_SUBTRACT = 3;
  /** One of the blend equations. See the OpenGL documentation for glBlendEquation for more details. */
  public static final int MIN                   = 4;
  /** One of the blend equations. See the OpenGL documentation for glBlendEquation for more details. */
  public static final int MAX                   = 5;

  /** Sets whether blending is enabled. Defaults to false. */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /** Returns whether blending is enabled. Defaults to false. */
  public boolean getEnabled() {
    return enabled;
  }

  /** Sets the source blending function to one of ZERO, ONE,
      SRC_COLOR, ONE_MINUS_SRC_COLOR, DST_COLOR, ONE_MINUS_DST_COLOR,
      SRC_ALPHA, ONE_MINUS_SRC_ALPHA, DST_ALPHA, ONE_MINUS_DST_ALPHA,
      SRC_ALPHA_SATURATE, CONSTANT_COLOR, ONE_MINUS_CONSTANT_COLOR,
      CONSTANT_ALPHA, or ONE_MINUS_CONSTANT_ALPHA. The default is ONE. */
  public void setSourceFunc(int func) {
    if (func < ZERO || func > ONE_MINUS_CONSTANT_ALPHA) {
      throw new IllegalArgumentException("Illegal source blending function " + func);
    }
    srcFunc = func;
  }

  /** Returns the source blending function. The default is ONE. */
  public int getSourceFunc() {
    return srcFunc;
  }

  /** Sets the destination blending function to one of ZERO, ONE,
      SRC_COLOR, ONE_MINUS_SRC_COLOR, DST_COLOR, ONE_MINUS_DST_COLOR,
      SRC_ALPHA, ONE_MINUS_SRC_ALPHA, DST_ALPHA, ONE_MINUS_DST_ALPHA,
      SRC_ALPHA_SATURATE, CONSTANT_COLOR, ONE_MINUS_CONSTANT_COLOR,
      CONSTANT_ALPHA, or ONE_MINUS_CONSTANT_ALPHA. The default is
      ZERO. */
  public void setDestFunc(int func) {
    if (func < ZERO || func > ONE_MINUS_CONSTANT_ALPHA) {
      throw new IllegalArgumentException("Illegal destination blending function " + func);
    }
    destFunc = func;
  }

  /** Returns the destination blending function. The default is ZERO. */
  public int getDestFunc() {
    return destFunc;
  }

  /** Sets the blending equation to one of FUNC_ADD, FUNC_SUBTRACT,
      FUNC_REVERSE_SUBTRACT, MIN, or MAX. Defaults to FUNC_ADD. */
  public void setBlendEquation(int equation) {
    if (equation < FUNC_ADD || equation > MAX) {
      throw new IllegalArgumentException("Illegal blending equation " + equation);
    }
    this.blendEquation = equation;
  }

  /** Returns the blending equation. Defaults to FUNC_ADD. */
  public int getBlendEquation() {
    return blendEquation;
  }

  /** Sets the blending color; see the documentation for glBlendColor
      for more details. Defaults to [0, 0, 0, 0]. */
  public void setBlendColor(Vec4f color) {
    blendColor.set(color);
  }

  /** Gets the blending color; see the documentation for glBlendColor
      for more details. Defaults to [0, 0, 0, 0]. */
  public Vec4f getBlendColor() {
    return blendColor;
  }

  public void doAction(Action action) {
    if (BlendElement.isEnabled(action.getState())) {
      BlendElement.set(action.getState(),
                       getEnabled(),
                       getBlendColor(),
                       getSourceFunc(),
                       getDestFunc(),
                       getBlendEquation());
    }
  }
}
