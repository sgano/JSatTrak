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

/** Represents the depth test state of the OpenGL fixed-function
    pipeline and causes side-effects in OpenGL for rendering. */

public class GLDepthTestElement extends DepthTestElement {
  // Boilerplate for concrete element subclasses
  public Element newInstance() {
    return new GLDepthTestElement();
  }
  public static GLDepthTestElement getInstance(State state) {
    return (GLDepthTestElement) DepthTestElement.getInstance(state);
  }
  public static void enable(State defaultState) {
    Element tmp = new GLDepthTestElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }

  public void pop(State state, Element previousTopElement) {
    send();
  }

  public void setElt(boolean enabled) {
    super.setElt(enabled);
    send();
  }

  private void send() {
    GL gl = GLU.getCurrentGL();
    // Don't try to optimize what we send to OpenGL at this point -- too complicated
    if (enabled) {
      gl.glEnable(GL.GL_DEPTH_TEST);
    } else {
      gl.glDisable(GL.GL_DEPTH_TEST);
    }
  }
}
