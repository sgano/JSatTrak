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
import net.java.joglutils.msg.nodes.*;

/** Represents the current texture, which is applied to any drawn
    geometry if texture coordinates are also supplied. */

public class TextureElement extends Element {
  // Boilerplate
  private static StateIndex index = State.registerElementType();
  public StateIndex getStateIndex() { return index; }
  public Element newInstance() {
    return new TextureElement();
  }
  /** Returns the instance of this element in the passed State. */
  public static TextureElement getInstance(State state) {
    return (TextureElement) state.getElement(index);
  }
  /** Enables this element in the passed state, which should be the
      default for a given action. */
  public static void enable(State defaultState) {
    TextureElement tmp = new TextureElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }
  /** Indicates whether this element is enabled in the given default
      state for a particular action. */
  public static boolean isEnabled(State state) {
    return (state.getDefaults().getElement(index) != null);
  }

  // This particular element refers to the Texture2 node directly.
  // Having it refer to the Texture object doesn't really make sense,
  // because the Texture object implicitly relies on OpenGL and the
  // intent is to make the base element class not reliant on GL.

  // The Texture2 node
  protected Texture2 texture;

  /** Sets the texture in the given state. */
  public static void set(State state, Texture2 texture) {
    getInstance(state).setElt(texture);
  }

  /** Returns the current texture in the state. */
  public static Texture2 get(State state) {
    return getInstance(state).texture;
  }

  public void push(State state) {
    TextureElement prev = (TextureElement) getNextInStack();
    if (prev != null) {
      // Pull down the texture from the previous element
      texture = prev.texture;
    }
  }

  /** Sets the texture in this element. */
  public void setElt(Texture2 texture) {
    this.texture = texture;
  }
}
