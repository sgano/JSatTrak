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

import net.java.joglutils.msg.misc.*;

/** Represents the current set of colors, which are applied on a
    per-vertex basis to any drawn geometry, and causes side-effects in
    OpenGL for rendering. */

public class GLColorElement extends ColorElement {
  // Boilerplate for concrete element subclasses
  public Element newInstance() {
    return new GLColorElement();
  }
  public static GLColorElement getInstance(State state) {
    return (GLColorElement) ColorElement.getInstance(state);
  }
  public static void enable(State defaultState) {
    Element tmp = new GLColorElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }

  // Whether the OpenGL state is currently enabled
  private boolean enabled;

  public void push(State state) {
    super.push(state);
    // Copy enabled state from previous element if any
    GLColorElement prev = (GLColorElement) getNextInStack();
    if (prev != null) {
      enabled = prev.enabled;
    }
  }

  public void pop(State state, Element previousTopElement) {
    GLColorElement prev = (GLColorElement) previousTopElement;
    boolean shouldBeEnabled = enabled;
    enabled = prev.enabled;
    // Put things back the way they were
    setEnabled(shouldBeEnabled);
  }

  public void setElt(FloatBuffer colors) {
    super.setElt(colors);
    setEnabled(colors != null);
  }

  private void setEnabled(boolean enabled) {
    if (this.enabled == enabled)
      return;  // No OpenGL work to do
    this.enabled = enabled;
    GL gl = GLU.getCurrentGL();
    if (enabled) {
      gl.glColorPointer(4, GL.GL_FLOAT, 0, colors);
      gl.glEnableClientState(GL.GL_COLOR_ARRAY);
    } else {
      gl.glDisableClientState(GL.GL_COLOR_ARRAY);
      // Assume we have to reset the current color to the default
      gl.glColor4f(1, 1, 1, 1);
    }
  }
}
