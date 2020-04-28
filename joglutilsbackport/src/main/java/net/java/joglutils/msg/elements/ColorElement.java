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

import net.java.joglutils.msg.misc.*;

/** Represents the current set of colors, which are applied on a
    per-vertex basis to any drawn geometry. */

public class ColorElement extends Element {
  // Boilerplate
  private static StateIndex index = State.registerElementType();
  public StateIndex getStateIndex() { return index; }
  public Element newInstance() {
    return new ColorElement();
  }
  /** Returns the instance of this element in the passed State. */
  public static ColorElement getInstance(State state) {
    return (ColorElement) state.getElement(index);
  }
  /** Enables this element in the passed state, which should be the
      default for a given action. */
  public static void enable(State defaultState) {
    ColorElement tmp = new ColorElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }
  /** Indicates whether this element is enabled in the given default
      state for a particular action. */
  public static boolean isEnabled(State state) {
    return (state.getDefaults().getElement(index) != null);
  }

  // The actual color data
  protected FloatBuffer colors;
  // The color binding to material parameter (not yet implemented)
  protected int colorBinding;

  /** Sets the color data in the passed state. */
  public static void set(State state, FloatBuffer colors) {
    getInstance(state).setElt(colors);
  }

  /** Returns the color data in the passed state. */
  public static FloatBuffer get(State state) {
    return getInstance(state).colors;
  }

  public void push(State state) {
    ColorElement prev = (ColorElement) getNextInStack();
    if (prev != null) {
      // Pull down the data from the previous element
      colors = prev.colors;
    }
  }

  /** Sets the color data in this element. */
  public void setElt(FloatBuffer colors) {
    this.colors = colors;
  }
}
