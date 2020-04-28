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
 */

package net.java.joglutils.msg.elements;

import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

/** Represents the current shader, which is applied to any drawn
    geometry, and performs side-effects in OpenGL. */

public class GLShaderElement extends ShaderElement {
  // Boilerplate for concrete element subclasses
  public Element newInstance() {
    return new GLShaderElement();
  }
  public static GLShaderElement getInstance(State state) {
    return (GLShaderElement) ShaderElement.getInstance(state);
  }
  public static void enable(State defaultState) {
    Element tmp = new GLShaderElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }

  public void pop(State state, Element previousTopElement) {
    // Put things back the way they were
    switchShaders(((GLShaderElement) previousTopElement).shader, shader);
  }

  public void setElt(ShaderNode shader) {
    ShaderNode prev = this.shader;
    super.setElt(shader);
    switchShaders(prev, shader);
  }

  private void switchShaders(ShaderNode prev, ShaderNode shader) {
    GL gl = GLU.getCurrentGL();
    Shader prevShader = null;
    Shader curShader  = null;
    if (prev != null) {
      prevShader = prev.getShader();
    }
    if (shader != null) {
      curShader = shader.getShader();
    }

    // FIXME: should be smarter about this; if the target is the same
    // for the previous and current shaders, just bind the new one
    if (prevShader != null) {
      prevShader.disable();
    }
    if (curShader != null) {
      curShader.enable();
    }
  }
}
