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

import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

/** Represents the current vertex/fragment shader, which is applied to
    any drawn geometry. */

public class ShaderElement extends Element {
  // Boilerplate
  private static StateIndex index = State.registerElementType();
  public StateIndex getStateIndex() { return index; }
  public Element newInstance() {
    return new ShaderElement();
  }
  /** Returns the instance of this element in the passed State. */
  public static ShaderElement getInstance(State state) {
    return (ShaderElement) state.getElement(index);
  }
  /** Enables this element in the passed state, which should be the
      default for a given action. */
  public static void enable(State defaultState) {
    ShaderElement tmp = new ShaderElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }
  /** Indicates whether this element is enabled in the given default
      state for a particular action. */
  public static boolean isEnabled(State state) {
    return (state.getDefaults().getElement(index) != null);
  }

  // This particular element refers to the ShaderNode directly.
  // Having it refer to the Shader object doesn't really make sense,
  // because the Shader object implicitly relies on OpenGL and the
  // intent is to make the base element class not reliant on GL.

  // The ShaderNode
  protected ShaderNode shader;

  /** Sets the shader in the given state. */
  public static void set(State state, ShaderNode shader) {
    getInstance(state).setElt(shader);
  }

  /** Returns the current shader in the state. */
  public static ShaderNode get(State state) {
    return getInstance(state).shader;
  }

  public void push(State state) {
    ShaderElement prev = (ShaderElement) getNextInStack();
    if (prev != null) {
      // Pull down the shader from the previous element
      shader = prev.shader;
    }
  }

  /** Sets the shader in this element. */
  public void setElt(ShaderNode shader) {
    this.shader = shader;
  }
}
