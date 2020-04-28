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

import java.nio.*;
import java.util.*;

import javax.media.opengl.*;
import com.sun.opengl.util.texture.*;

import net.java.joglutils.msg.actions.*;
import net.java.joglutils.msg.elements.*;
import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.misc.*;

/** A TriangleSet assembles the coordinates specified by a Coordinate3
    node, and any auxiliary nodes such as a TextureCoordinate2 node,
    into a set of triangles. */

public class TriangleSet extends TriangleBasedShape {
  private int numTriangles;

  /** Sets the number of triangles this TriangleSet references. */
  public void setNumTriangles(int numTriangles) {
    this.numTriangles = numTriangles;
  }

  /** Returns the number of triangles this TriangleSet references. */
  public int getNumTriangles() {
    return numTriangles;
  }

  public void render(GLRenderAction action) {
    State state = action.getState();
    if (!CoordinateElement.isEnabled(state))
      return;

    if (CoordinateElement.get(state) != null) {
      // OK, we have coordinates to send down, at least

      GL gl = action.getGL();

      Texture tex = null;
      boolean haveTexCoords = false;

      if (GLTextureElement.isEnabled(state) &&
          GLTextureCoordinateElement.isEnabled(state)) {
        Texture2 texNode = GLTextureElement.get(state);
        if (texNode != null) {
          tex = texNode.getTexture();
        }
        haveTexCoords = (GLTextureCoordinateElement.get(state) != null);
      }

      if (tex != null) {
        // Set up the texture matrix to uniformly map [0..1] to the used
        // portion of the texture image
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();
        if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
            gl.glLoadTransposeMatrixf(getTextureMatrix(tex).getRowMajorData(), 0);
        } else {
            float[] tmp = new float[16];
            getTextureMatrix(tex).getColumnMajorData(tmp);
            gl.glLoadMatrixf(tmp, 0);
        }
        gl.glMatrixMode(GL.GL_MODELVIEW);
      } else if (haveTexCoords) {
        // Want to turn off the use of texture coordinates to avoid errors
        // FIXME: not 100% sure whether we need to do this, but think we should
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
      }

      // For now, assume the triangle set and the number of available
      // coordinates match -- may want to add debugging information
      // for this later
      gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3 * getNumTriangles());

      if (tex != null) {
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
      } else if (haveTexCoords) {
        // Might want this the next time we render a shape
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
      }
    }
  }

  public void generateTriangles(Action action, TriangleCallback cb) {
    State state = action.getState();
    FloatBuffer coords    = null;
    FloatBuffer texCoords = null;
    // FIXME: normals and lighting not supported yet
    //    FloatBuffer normals   = null;
    FloatBuffer colors    = null;
    if (CoordinateElement.isEnabled(state)) {
      coords = CoordinateElement.get(state);
    }
    // No point in continuing if we don't have coordinates
    if (coords == null)
      return;
    if (TextureCoordinateElement.isEnabled(state)) {
      texCoords = TextureCoordinateElement.get(state);
    }
    //    if (NormalElement.isEnabled(state)) {
    //      texCoords = NormalElement.get(state);
    //    }
    if (ColorElement.isEnabled(state)) {
      colors = ColorElement.get(state);
    }
    PrimitiveVertex v0 = new PrimitiveVertex();
    PrimitiveVertex v1 = new PrimitiveVertex();
    PrimitiveVertex v2 = new PrimitiveVertex();
    v0.setCoord(new Vec3f());
    v1.setCoord(new Vec3f());
    v2.setCoord(new Vec3f());
    if (texCoords != null) {
      v0.setTexCoord(new Vec2f());
      v1.setTexCoord(new Vec2f());
      v2.setTexCoord(new Vec2f());
    }
    if (colors != null) {
      v0.setColor(new Vec4f());
      v1.setColor(new Vec4f());
      v2.setColor(new Vec4f());
    }

    int coordIdx = 0;
    for (int i = 0; i < numTriangles; i++) {
      // Vertex 0
      v0.getCoord().set(coords.get(3 * coordIdx + 0),
                        coords.get(3 * coordIdx + 1),
                        coords.get(3 * coordIdx + 2));
      if (texCoords != null) {
        v0.getTexCoord().set(texCoords.get(2 * coordIdx + 0),
                             texCoords.get(2 * coordIdx + 1));
      }
      if (colors != null) {
        v0.getColor().set(colors.get(4 * coordIdx + 0),
                          colors.get(4 * coordIdx + 1),
                          colors.get(4 * coordIdx + 2),
                          colors.get(4 * coordIdx + 3));
      }

      // Vertex 1
      v1.getCoord().set(coords.get(3 * (coordIdx + 1) + 0),
                        coords.get(3 * (coordIdx + 1) + 1),
                        coords.get(3 * (coordIdx + 1) + 2));
      if (texCoords != null) {
        v1.getTexCoord().set(texCoords.get(2 * (coordIdx + 1) + 0),
                             texCoords.get(2 * (coordIdx + 1) + 1));
      }
      if (colors != null) {
        v1.getColor().set(colors.get(4 * (coordIdx + 1) + 0),
                          colors.get(4 * (coordIdx + 1) + 1),
                          colors.get(4 * (coordIdx + 1) + 2),
                          colors.get(4 * (coordIdx + 1) + 3));
      }

      // Vertex 2
      v2.getCoord().set(coords.get(3 * (coordIdx + 2) + 0),
                        coords.get(3 * (coordIdx + 2) + 1),
                        coords.get(3 * (coordIdx + 2) + 2));
      if (texCoords != null) {
        v2.getTexCoord().set(texCoords.get(2 * (coordIdx + 2) + 0),
                             texCoords.get(2 * (coordIdx + 2) + 1));
      }
      if (colors != null) {
        v2.getColor().set(colors.get(4 * (coordIdx + 2) + 0),
                          colors.get(4 * (coordIdx + 2) + 1),
                          colors.get(4 * (coordIdx + 2) + 2),
                          colors.get(4 * (coordIdx + 2) + 3));
      }

      // Call callback
      cb.triangleCB(i,
                    v0, 3 * i + 0,
                    v1, 3 * i + 1,
                    v2, 3 * i + 2);

      coordIdx += 3;
    }
  }

  // Helper routine for setting up a texture matrix to allow texture
  // coords in the scene graph to always be specified from (0..1)
  private Mat4f textureMatrix = new Mat4f();
  private Mat4f getTextureMatrix(Texture texture) {
    textureMatrix.makeIdent();
    TextureCoords coords = texture.getImageTexCoords();
    // Horizontal scale
    textureMatrix.set(0, 0, coords.right() - coords.left());
    // Vertical scale (may be negative if texture needs to be flipped vertically)
    float vertScale = coords.top() - coords.bottom();
    textureMatrix.set(1, 1, vertScale);
    textureMatrix.set(0, 3, coords.left());
    textureMatrix.set(1, 3, coords.bottom());
    return textureMatrix;
  }
}
