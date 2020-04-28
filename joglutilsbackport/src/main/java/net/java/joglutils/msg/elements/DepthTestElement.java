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

import javax.media.opengl.*;

import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

/** Represents the depth test state of the OpenGL fixed-function pipeline. */

public class DepthTestElement extends Element {

  // Boilerplate
  private static StateIndex index = State.registerElementType();
  public StateIndex getStateIndex() { return index; }
  public Element newInstance() {
    return new DepthTestElement();
  }
  /** Returns the instance of this element in the passed State. */
  public static DepthTestElement getInstance(State state) {
    return (DepthTestElement) state.getElement(index);
  }
  /** Enables this element in the passed state, which should be the
      default for a given action. */
  public static void enable(State defaultState) {
    DepthTestElement tmp = new DepthTestElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }
  /** Indicates whether this element is enabled in the given default
      state for a particular action. */
  public static boolean isEnabled(State state) {
    return (state.getDefaults().getElement(index) != null);
  }

  // TODO: eventually we'll want to add other state associated with
  // depth testing, such as the the depth function and values
  protected boolean enabled;

  /** Sets the depth test state. */
  public static void set(State state, boolean enabled) {
    getInstance(state).setElt(enabled);
  }

  /** Returns whether depth testing is enabled. */
  public static boolean getEnabled(State state) {
    return getInstance(state).enabled;
  }

  public void push(State state) {
    DepthTestElement prev = (DepthTestElement) getNextInStack();
    if (prev != null) {
      // Pull down the data from the previous element
      enabled = prev.enabled;
    }
  }

  /** Sets all of the portions of the depth test state in this element. */
  public void setElt(boolean enabled) {
    this.enabled = enabled;
  }
}
