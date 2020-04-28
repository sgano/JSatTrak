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
import net.java.joglutils.msg.collections.*;

/** Represents a set of 4-valued colors which are applied on a
    per-vertex basis to any drawn polygons. Currently the color can
    only be bound to one material parameter of the fixed-function
    OpenGL pipeline, defaulting to "ambient and diffuse". <P>

    For correct rendering results, the colors stored in this node
    should have their alpha premultiplied; in other words, the red,
    green, and blue (x, y, and z components of the stored vectors)
    should be multiplied by the alpha value (the w component) before
    storing them in this node.
*/

public class Color4 extends Node {
  private Vec4fCollection data;

  static {
    // Enable the elements this node affects for known actions
    GLColorElement.enable(GLRenderAction.getDefaultState());
  }

  //  private int colorBinding = AMBIENT_AND_DIFFUSE;

  /******

   * Note: these aren't needed until we have lighting

  // FIXME: factor this out into a separate ColorBinding node
  public static final int EMISSION            = 1;
  public static final int AMBIENT             = 2;
  public static final int DIFFUSE             = 3;
  public static final int SPECULAR            = 4;
  public static final int AMBIENT_AND_DIFFUSE = 5;

  public void setColorBinding(int colorBinding) {
    if (binding < EMISSION || binding > AMBIENT_AND_DIFFUSE) {
      throw new IllegalArgumentException("Illegal color binding " + binding);
    }
    this.colorBinding = colorBinding;
  }

  public int getColorBinding() {
    return colorBinding;
  }

  */

  public void setData(Vec4fCollection data) {
    this.data = data;
  }

  public Vec4fCollection getData() {
    return data;
  }

  public void doAction(Action action) {
    if (ColorElement.isEnabled(action.getState())) {
      ColorElement.set(action.getState(), getData().getData());
    }
  }
}
