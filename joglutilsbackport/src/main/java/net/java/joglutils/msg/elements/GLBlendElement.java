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

package net.java.joglutils.msg.elements;

import java.nio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

/** Represents the blending state of the OpenGL fixed-function
    pipeline and causes side-effects in OpenGL for rendering. */

public class GLBlendElement extends BlendElement {
  // Boilerplate for concrete element subclasses
  public Element newInstance() {
    return new GLBlendElement();
  }
  public static GLBlendElement getInstance(State state) {
    return (GLBlendElement) BlendElement.getInstance(state);
  }
  public static void enable(State defaultState) {
    Element tmp = new GLBlendElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }

  public void pop(State state, Element previousTopElement) {
    send();
  }

  public void setElt(boolean enabled,
                     Vec4f blendColor,
                     int srcFunc,
                     int destFunc,
                     int blendEquation) {
    super.setElt(enabled, blendColor, srcFunc, destFunc, blendEquation);
    send();
  }

  private static int oglBlendFunc(int func) {
    switch (func) {
      case Blend.ZERO:                     return GL.GL_ZERO;
      case Blend.ONE:                      return GL.GL_ONE;
      case Blend.SRC_COLOR:                return GL.GL_SRC_COLOR;
      case Blend.ONE_MINUS_SRC_COLOR:      return GL.GL_ONE_MINUS_SRC_COLOR;
      case Blend.DST_COLOR:                return GL.GL_DST_COLOR;
      case Blend.ONE_MINUS_DST_COLOR:      return GL.GL_ONE_MINUS_DST_COLOR;
      case Blend.SRC_ALPHA:                return GL.GL_SRC_ALPHA;
      case Blend.ONE_MINUS_SRC_ALPHA:      return GL.GL_ONE_MINUS_SRC_ALPHA;
      case Blend.DST_ALPHA:                return GL.GL_DST_ALPHA;
      case Blend.ONE_MINUS_DST_ALPHA:      return GL.GL_ONE_MINUS_DST_ALPHA;
      case Blend.SRC_ALPHA_SATURATE:       return GL.GL_SRC_ALPHA_SATURATE;
      case Blend.CONSTANT_COLOR:           return GL.GL_CONSTANT_COLOR;
      case Blend.ONE_MINUS_CONSTANT_COLOR: return GL.GL_ONE_MINUS_CONSTANT_COLOR;
      case Blend.CONSTANT_ALPHA:           return GL.GL_CONSTANT_ALPHA;
      case Blend.ONE_MINUS_CONSTANT_ALPHA: return GL.GL_ONE_MINUS_CONSTANT_ALPHA;
    }
    throw new InternalError("Illegal blend function " + func);
  }

  private int oglBlendEquation(int equation) {
    switch (equation) {
      case Blend.FUNC_ADD:              return GL.GL_FUNC_ADD;
      case Blend.FUNC_SUBTRACT:         return GL.GL_FUNC_SUBTRACT;
      case Blend.FUNC_REVERSE_SUBTRACT: return GL.GL_FUNC_REVERSE_SUBTRACT;
      case Blend.MIN:                   return GL.GL_MIN;
      case Blend.MAX:                   return GL.GL_MAX;
    }
    throw new InternalError("Illegal blend equation " + equation);
  }

  private static void validateFunc(GL gl, int func) {
    if (func == GL.GL_CONSTANT_COLOR ||
        func == GL.GL_ONE_MINUS_CONSTANT_COLOR ||
        func == GL.GL_CONSTANT_ALPHA ||
        func == GL.GL_ONE_MINUS_CONSTANT_ALPHA) {
      if (!gl.isExtensionAvailable("GL_ARB_imaging")) {
        throw new RuntimeException("Blend function requires GL_ARB_imaging extension");
      }
    }
  }

  private void send() {
    GL gl = GLU.getCurrentGL();
    // Don't try to optimize what we send to OpenGL at this point -- too complicated
    if (enabled) {
      gl.glEnable(GL.GL_BLEND);
      int oglSrcFunc  = oglBlendFunc(srcFunc);
      int oglDestFunc = oglBlendFunc(destFunc);
      validateFunc(gl, oglSrcFunc);
      validateFunc(gl, oglDestFunc);
      gl.glBlendFunc(oglSrcFunc, oglDestFunc);
      if (gl.isExtensionAvailable("GL_ARB_imaging")) {
        gl.glBlendEquation(oglBlendEquation(blendEquation));
        gl.glBlendColor(blendColor.x(), blendColor.y(), blendColor.z(), blendColor.w());
      }
    } else {
      gl.glDisable(GL.GL_BLEND);
    }
  }
}
