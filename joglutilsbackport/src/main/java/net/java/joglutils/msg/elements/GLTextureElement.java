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
import com.sun.opengl.util.texture.*;

import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

/** Represents the current texture, which is applied to any drawn
    geometry if texture coordinates are also supplied, and performs
    side-effects in OpenGL. */

public class GLTextureElement extends TextureElement {
  // Boilerplate for concrete element subclasses
  public Element newInstance() {
    return new GLTextureElement();
  }
  public static GLTextureElement getInstance(State state) {
    return (GLTextureElement) TextureElement.getInstance(state);
  }
  public static void enable(State defaultState) {
    Element tmp = new GLTextureElement();
    defaultState.setElement(tmp.getStateIndex(), tmp);
  }

  public void pop(State state, Element previousTopElement) {
    // Put things back the way they were
    switchTextures(((GLTextureElement) previousTopElement).texture, texture);
  }

  public void setElt(Texture2 texture) {
    Texture2 prev = this.texture;
    super.setElt(texture);
    switchTextures(prev, texture);
  }

  private void switchTextures(Texture2 prev, Texture2 texture) {
    GL gl = GLU.getCurrentGL();
    Texture prevTexture = null;
    Texture curTexture  = null;
    int texEnvMode = 0;
    if (prev != null) {
      prevTexture = prev.getTexture();
    }
    if (texture != null) {
      curTexture = texture.getTexture();
      texEnvMode = texture.getTexEnvMode();
    }

    // FIXME: should be smarter about this; if the target is the same
    // for the previous and current textures, just bind the new one
    if (prevTexture != null) {
      prevTexture.disable();
    }
    if (curTexture != null) {
      curTexture.enable();
      curTexture.bind();
      int glEnvMode = 0;
      switch (texEnvMode) {
        case Texture2.MODULATE:   glEnvMode = GL.GL_MODULATE; break;
        case Texture2.DECAL:      glEnvMode = GL.GL_DECAL;    break;
        case Texture2.BLEND:      glEnvMode = GL.GL_BLEND;    break;
        case Texture2.REPLACE:    glEnvMode = GL.GL_REPLACE;  break;
      }
      gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, glEnvMode);
    }
  }
}
