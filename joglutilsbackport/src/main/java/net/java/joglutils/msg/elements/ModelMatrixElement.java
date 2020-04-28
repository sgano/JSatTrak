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

import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.misc.*;

/** Represents the model matrix, which is the transformation applied
    to objects in the scene. */

public class ModelMatrixElement extends Element {
  // Boilerplate
  private static StateIndex index = State.registerElementType();
  public StateIndex getStateIndex() { return index; }
  public Element newInstance() {
    return new ModelMatrixElement();
  }
  /** Returns the instance of this element in the passed State. */
  public static ModelMatrixElement getInstance(State state) {
    return (ModelMatrixElement) state.getElement(index);
  }
  /** Enables this element in the passed state, which should be the
      default for a given action. */
  public static void enable(State defaultState) {
    Element tmp = new ModelMatrixElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }
  /** Indicates whether this element is enabled in the given default
      state for a particular action. */
  public static boolean isEnabled(State state) {
    return (state.getDefaults().getElement(index) != null);
  }

  // The matrix data
  protected Mat4f matrix;
  protected Mat4f temp = new Mat4f();

  public ModelMatrixElement() {
    matrix = new Mat4f();
    matrix.makeIdent();
  }

  public void push(State state) {
    ModelMatrixElement prev = (ModelMatrixElement) getNextInStack();
    if (prev != null) {
      matrix.set(prev.matrix);
    }
  }

  /** Returns the current model matrix; callers should not mutate this
      directly but instead use the accessor methods to change it. */
  public Mat4f getMatrix() {
    return matrix;
  }

  /** Sets the current element to the identity matrix. */
  public static void makeIdent(State state) {
    ModelMatrixElement elt = getInstance(state);
    elt.makeEltIdent();
  }

  /** Sets this element to the identity matrix. */
  public void makeEltIdent() {
    matrix.makeIdent();
  }

  /** Multiplies the current element by the given matrix. */
  public static void mult(State state, Mat4f matrix) {
    ModelMatrixElement elt = getInstance(state);
    elt.multElt(matrix);
  }

  /** Multiplies this element by the given matrix. */
  public void multElt(Mat4f matrix) {
    temp.set(this.matrix);
    this.matrix.mul(temp, matrix);
  }
}
