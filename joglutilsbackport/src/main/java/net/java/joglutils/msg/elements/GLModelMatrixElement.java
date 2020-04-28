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

/** Represents the model matrix, which is the transformation applied
    to objects in the scene, and causes side-effects in OpenGL. */

public class GLModelMatrixElement extends ModelMatrixElement {
  // Boilerplate for concrete element subclasses
  public Element newInstance() {
    return new GLModelMatrixElement();
  }
  public static GLModelMatrixElement getInstance(State state) {
    return (GLModelMatrixElement) ModelMatrixElement.getInstance(state);
  }
  public static void enable(State defaultState) {
    Element tmp = new GLModelMatrixElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }

  // State which we need in order to reset the modelview matrix
  private State state;

  public void push(State state) {
    super.push(state);
    this.state = state;

    // Cause side-effects in OpenGL
    GL gl = GLU.getCurrentGL();
    gl.glPushMatrix();
  }

  public void pop(State state, Element previousTopElement) {
    super.pop(state, previousTopElement);

    // Cause side-effects in OpenGL
    GL gl = GLU.getCurrentGL();
    gl.glPopMatrix();
  }

  public void makeEltIdent() {
    super.makeEltIdent();
    // Cause side-effects in OpenGL
    // Recompute the complete modelview matrix
    Mat4f mat = ViewingMatrixElement.getInstance(state).getMatrix();
    GL gl = GLU.getCurrentGL();
    if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
        gl.glLoadTransposeMatrixf(mat.getRowMajorData(), 0);
    } else {
        float[] tmp = new float[16];
        mat.getColumnMajorData(tmp);
        gl.glLoadMatrixf(tmp, 0);
    }
  }

  public void multElt(Mat4f matrix) {
    super.multElt(matrix);
    GL gl = GLU.getCurrentGL();
    if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
        gl.glMultTransposeMatrixf(matrix.getRowMajorData(), 0);
    } else {
        float[] tmp = new float[16];
        matrix.getColumnMajorData(tmp);
        gl.glMultMatrixf(tmp, 0);
    }
  }
}
