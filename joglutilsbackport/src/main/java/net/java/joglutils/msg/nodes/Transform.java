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

/** Represents a generalized 4x4 matrix transformation. */

public class Transform extends Node {
  private Mat4f transform;
  
  static {
    // Enable the elements this node affects for known actions
    // Note that all of these elements are interdependent
    GLModelMatrixElement     .enable(GLRenderAction.getDefaultState());
    GLProjectionMatrixElement.enable(GLRenderAction.getDefaultState());
    GLViewingMatrixElement   .enable(GLRenderAction.getDefaultState());

    ModelMatrixElement     .enable(RayPickAction.getDefaultState());
    ProjectionMatrixElement.enable(RayPickAction.getDefaultState());
    ViewingMatrixElement   .enable(RayPickAction.getDefaultState());
  }

  public Transform() {
    transform = new Mat4f();
    transform.makeIdent();
  }

  /** Sets the transformation in thie node. */
  public void setTransform(Mat4f transform) {
    this.transform.set(transform);
  }

  /** Returns the transformation in thie node. */
  public Mat4f getTransform() {
    return transform;
  }

  public void doAction(Action action) {
    if (ModelMatrixElement.isEnabled(action.getState())) {
      ModelMatrixElement.mult(action.getState(), getTransform());
    }
  }
}
