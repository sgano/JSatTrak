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

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.media.opengl.*;
import com.sun.opengl.util.j2d.*;
import com.sun.opengl.util.texture.*;

import net.java.joglutils.msg.actions.*;
import net.java.joglutils.msg.elements.*;

/** Represents a two-dimensional texture which can be set up from all
    of the image sources supported by the JOGL TextureIO classes or
    the JOGL TextureRenderer. If set up with a texture, supports
    updating of the texture as well. Supports switching between use of
    a TextureRenderer and a Texture. */

public class Texture2 extends Node {
  private TextureData data;
  private Texture texture;
  private int texEnvMode = MODULATE;
  private boolean dirty;

  // For now, to make things simpler, keep separate state for the sub-image updating
  private TextureData subImageData;
  private int subImageMipmapLevel;
  private int subImageDstX;
  private int subImageDstY;
  private int subImageSrcX;
  private int subImageSrcY;
  private int subImageWidth;
  private int subImageHeight;
  private boolean subImageDirty;

  private TextureRenderer textureRenderer;

  // Disposed Textures and TextureRenderers, used to allow app to be
  // oblivious and switch back and forth between them
  private List<Texture> disposedTextures = new ArrayList<Texture>();
  private List<TextureRenderer> disposedRenderers = new ArrayList<TextureRenderer>();

  static {
    // Enable the elements this node affects for known actions
    GLTextureElement.enable(GLRenderAction.getDefaultState());

    TextureElement  .enable(RayPickAction.getDefaultState());
  }

  /** Represents the OpenGL MODULATE texture environment mode. */
  public static final int MODULATE = 1;
  /** Represents the OpenGL DECAL texture environment mode. */
  public static final int DECAL    = 2;
  /** Represents the OpenGL BLEND texture environment mode. */
  public static final int BLEND    = 3;
  /** Represents the OpenGL REPLACE texture environment mode. */
  public static final int REPLACE  = 4;

  /** Initializes this texture from the given file. No OpenGL work is
      done during this call; it is done lazily when the Texture is
      fetched. */
  public void setTexture(File file, boolean mipmap, String fileSuffix) throws IOException {
    disposeTextureRenderer();
    data = TextureIO.newTextureData(file, mipmap, fileSuffix);
    dirty = true;
  }

  /** Initializes this texture from the given InputStream. No OpenGL
      work is done during this call; it is done lazily when the
      Texture is fetched. */
  public void setTexture(InputStream stream, boolean mipmap, String fileSuffix) throws IOException {
    disposeTextureRenderer();
    data = TextureIO.newTextureData(stream, mipmap, fileSuffix);
    dirty = true;
  }

  /** Initializes this texture from the given URL. No OpenGL work is
      done during this call; it is done lazily when the Texture is
      fetched. */
  public void setTexture(URL url, boolean mipmap, String fileSuffix) throws IOException {
    disposeTextureRenderer();
    data = TextureIO.newTextureData(url, mipmap, fileSuffix);
    dirty = true;
  }

  /** Initializes this texture from the given BufferedImage. No OpenGL
      work is done during this call; it is done lazily when the
      Texture is fetched. */
  public void setTexture(BufferedImage image, boolean mipmap) {
    disposeTextureRenderer();
    data = TextureIO.newTextureData(image, mipmap);
    dirty = true;
  }
  
  /** Initializes this texture from the given TextureData. No OpenGL
      work is done during this call; it is done lazily when the
      Texture is fetched. */
  public void setTexture(TextureData data) {
    disposeTextureRenderer();
    this.data = data;
    dirty = true;
  }

  /** Returns the width of the texture or TextureRenderer this
      Texture2 node is referencing, or 0 if it has not been set up
      yet. */
  public int getWidth() {
    if (data != null) {
      return data.getWidth();
    }

    if (texture != null) {
      return texture.getWidth();
    }

    if (textureRenderer != null) {
      return textureRenderer.getWidth();
    }

    return 0;
  }

  /** Returns the height of the texture or TextureRenderer this
      Texture2 node is referencing, or 0 if it has not been set up
      yet. */
  public int getHeight() {
    if (data != null) {
      return data.getHeight();
    }

    if (texture != null) {
      return texture.getHeight();
    }

    if (textureRenderer != null) {
      return textureRenderer.getHeight();
    }

    return 0;
  }

  /**
   * Updates a subregion of the content area of this texture using the
   * specified sub-region of the given data. Only updates the
   * specified mipmap level and does not re-generate mipmaps if they
   * were originally produced or loaded. This method is only supported
   * for uncompressed TextureData sources, and may only be called if a
   * TextureRenderer has not been set up for this Texture2 node.
   *
   * @param data the image data to be uploaded to this texture
   * @param mipmapLevel the mipmap level of the texture to set. If
   * this is non-zero and the TextureData contains mipmap data, the
   * appropriate mipmap level will be selected.
   * @param dstx the x offset (in pixels) relative to the lower-left corner
   * of this texture where the update will be applied
   * @param dsty the y offset (in pixels) relative to the lower-left corner
   * of this texture where the update will be applied
   * @param srcx the x offset (in pixels) relative to the lower-left corner
   * of the supplied TextureData from which to fetch the update rectangle
   * @param srcy the y offset (in pixels) relative to the lower-left corner
   * of the supplied TextureData from which to fetch the update rectangle
   * @param width the width (in pixels) of the rectangle to be updated
   * @param height the height (in pixels) of the rectangle to be updated
   */
  public void updateSubImage(TextureData data, int mipmapLevel,
                             int dstx, int dsty,
                             int srcx, int srcy,
                             int width, int height) {
    if (textureRenderer != null) {
      throw new IllegalStateException("May not call updateSubImage if a TextureRenderer has been set");
    }
    subImageData = data;
    subImageMipmapLevel = mipmapLevel;
    subImageDstX = dstx;
    subImageDstY = dsty;
    subImageSrcX = srcx;
    subImageSrcY = srcy;
    subImageWidth = width;
    subImageHeight = height;
    subImageDirty = true;
  }

  /** Initializes this node to operate upon a TextureRenderer of the
      specified width, height, and presence of an alpha channel.
      Updates to the TextureRenderer are automatically propagated to
      the texture as long as <CODE>TextureRenderer.markDirty()</CODE>
      is used properly. */
  public void initTextureRenderer(int width, int height, boolean alpha) {
    disposeTexture();
    textureRenderer = new TextureRenderer(width, height, alpha);
  }

  /** Returns the TextureRenderer, if one has been set, that is
      associated with this Texture2 node. */
  public TextureRenderer getTextureRenderer() {
    return textureRenderer;
  }

  /** Fetches the Texture object associated with this Texture2 node,
      refreshing its content if necessary. It is required to call this
      each frame during rendering. An OpenGL context must be current
      at the time this method is called or a GLException will be
      thrown. */
  public Texture getTexture() throws GLException {
    lazyDispose();

    if (textureRenderer != null) {
      return textureRenderer.getTexture();
    }

    if (dirty) {
      if (texture != null) {
        texture.dispose();
        texture = null;
      }
      texture = TextureIO.newTexture(data);
      data = null;
      dirty = false;
    }
    if (subImageDirty) {
      texture.updateSubImage(subImageData,
                             subImageMipmapLevel,
                             subImageDstX,
                             subImageDstY,
                             subImageSrcX,
                             subImageSrcY,
                             subImageWidth,
                             subImageHeight);
      subImageDirty = false;
    }
    return texture;
  }

  /** Sets the texture environment mode. Default is MODULATE. */
  public void setTexEnvMode(int mode) {
    if (mode < MODULATE || mode > REPLACE) {
      throw new IllegalArgumentException("Illegal texture environment mode");
    }
    this.texEnvMode = mode;
  }

  /** Returns the texture environment mode: one of MODULATE, DECAL,
      BLEND, or REPLACE. */
  public int getTexEnvMode() {
    return texEnvMode;
  }

  public void doAction(Action action) {
    if (TextureElement.isEnabled(action.getState())) {
      TextureElement.set(action.getState(), this);
    }
  }

  /** Disposes of the OpenGL texture and/or TextureRenderer this
      Texture2 node refers to. An OpenGL context must be current at
      the point this method is called. */
  public void dispose() throws GLException {
    disposeTexture();
    disposeTextureRenderer();
    lazyDispose();
    data = null;
    subImageData = null;
    dirty = false;
    subImageDirty = false;
  }

  /** Resets the OpenGL state of this node without explicitly
      disposing of any resources. This should only be called when you
      know you are using this Texture2 node across the destruction and
      re-creation of OpenGL contexts and know how to re-initialize the
      Texture2 from its previous state. */
  public void resetGL(GLResetAction action) {
    disposeTexture();
    disposeTextureRenderer();
    synchronized(this) {
      disposedTextures.clear();
      disposedRenderers.clear();
    }
    data = null;
    subImageData = null;
    dirty = false;
    subImageDirty = false;
  }

  private synchronized void disposeTextureRenderer() {
    if (textureRenderer != null) {
      disposedRenderers.add(textureRenderer);
      textureRenderer = null;
    }
  }

  private synchronized void disposeTexture() {
    if (texture != null) {
      disposedTextures.add(texture);
      texture = null;
      data = null;
      subImageData = null;
      dirty = false;
      subImageDirty = false;
    }
  }

  private void lazyDispose() {
    while (!disposedTextures.isEmpty()) {
      Texture t = null;
      synchronized (this) {
        t = disposedTextures.remove(disposedTextures.size() - 1);
      }
      t.dispose();
    }

    while (!disposedRenderers.isEmpty()) {
      TextureRenderer r = null;
      synchronized (this) {
        r = disposedRenderers.remove(disposedRenderers.size() - 1);
      }
      r.dispose();
    }
  }
}
