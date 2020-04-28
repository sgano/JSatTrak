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

import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

/** Represents the blending state of the OpenGL fixed-function pipeline. */

public class BlendElement extends Element {
  // Boilerplate
  private static StateIndex index = State.registerElementType();
  public StateIndex getStateIndex() { return index; }
  public Element newInstance() {
    return new BlendElement();
  }
  /** Returns the instance of this element in the passed State. */
  public static BlendElement getInstance(State state) {
    return (BlendElement) state.getElement(index);
  }
  /** Enables this element in the passed state, which should be the
      default for a given action. */
  public static void enable(State defaultState) {
    BlendElement tmp = new BlendElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }
  /** Indicates whether this element is enabled in the given default
      state for a particular action. */
  public static boolean isEnabled(State state) {
    return (state.getDefaults().getElement(index) != null);
  }

  // These defaults match those in the Blend node -- is there a better way of factoring them out?

  // Whether blending is enabled
  protected boolean enabled;
  protected Vec4f blendColor = new Vec4f();
  protected int srcFunc = Blend.ONE;
  protected int destFunc = Blend.ZERO;
  protected int blendEquation = Blend.FUNC_ADD;

  /** Sets all of the portions of the blending state in the passed State object. */
  public static void set(State state,
                         boolean enabled,
                         Vec4f blendColor,
                         int srcFunc,
                         int destFunc,
                         int blendEquation) {
    getInstance(state).setElt(enabled,
                              blendColor,
                              srcFunc,
                              destFunc,
                              blendEquation);
  }

  /** Returns whether blending is enabled. */
  public static boolean getEnabled(State state) {
    return getInstance(state).enabled;
  }

  /** Returns the blending color. */
  public static Vec4f getBlendColor(State state) {
    return getInstance(state).blendColor;
  }

  /** Returns the source function for blending. */
  public static int getSourceFunc(State state) {
    return getInstance(state).srcFunc;
  }

  /** Returns the destination function for blending. */
  public static int getDestFunc(State state) {
    return getInstance(state).destFunc;
  }

  /** Returns the blending equation. */
  public static int getBlendEquation(State state) {
    return getInstance(state).blendEquation;
  }

  public void push(State state) {
    BlendElement prev = (BlendElement) getNextInStack();
    if (prev != null) {
      // Pull down the data from the previous element
      enabled = prev.enabled;
      blendColor.set(prev.blendColor);
      srcFunc = prev.srcFunc;
      destFunc = prev.destFunc;
      blendEquation = prev.blendEquation;
    }
  }

  /** Sets all of the portions of the blending state in this element. */
  public void setElt(boolean enabled,
                     Vec4f blendColor,
                     int srcFunc,
                     int destFunc,
                     int blendEquation) {
    this.enabled = enabled;
    this.blendColor.set(blendColor);
    this.srcFunc = srcFunc;
    this.destFunc = destFunc;
    this.blendEquation = blendEquation;
  }
}
