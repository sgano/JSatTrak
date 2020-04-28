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

/** Represents the projection matrix, which transforms view-space
    coordinates into screen-space coordinates, and performs
    side-effects in OpenGL. */

public class GLProjectionMatrixElement extends ProjectionMatrixElement {
  // Boilerplate for concrete element subclasses
  public Element newInstance() {
    return new GLProjectionMatrixElement();
  }
  public static GLProjectionMatrixElement getInstance(State state) {
    return (GLProjectionMatrixElement) ProjectionMatrixElement.getInstance(state);
  }
  public static void enable(State defaultState) {
    Element tmp = new GLProjectionMatrixElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }

  public void push(State state) {
    super.push(state);
  }

  public void setElt(Mat4f matrix) {
    super.setElt(matrix);
    GL gl = GLU.getCurrentGL();
    gl.glMatrixMode(GL.GL_PROJECTION);
    if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
        gl.glLoadTransposeMatrixf(matrix.getRowMajorData(), 0);
    } else {
        float[] tmp = new float[16];
        matrix.getColumnMajorData(tmp);
        gl.glLoadMatrixf(tmp, 0);
    }
    gl.glMatrixMode(GL.GL_MODELVIEW);
  }
}
