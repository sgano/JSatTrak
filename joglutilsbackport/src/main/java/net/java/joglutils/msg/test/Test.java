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

package net.java.joglutils.msg.test;

import java.awt.Frame;
import java.awt.event.*;
import java.io.*;

import javax.media.opengl.*;
import com.sun.opengl.util.texture.*;

import net.java.joglutils.msg.actions.*;
import net.java.joglutils.msg.collections.*;
import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.nodes.*;

/** A very basic test of the Minimal Scene Graph library. */

public class Test {
  public static void main(String[] args) {
    Frame frame = new Frame("Minimal Scene Graph (MSG) Test");
    GLCanvas canvas = new GLCanvas();
    canvas.addGLEventListener(new Listener());
    frame.add(canvas);
    frame.setSize(512, 512);
    frame.setVisible(true);
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          new Thread(new Runnable() {
              public void run() {
                System.exit(0);
              }
            }).start();
        }
      });
  }

  static class Listener implements GLEventListener {
    private Separator root;
    private GLRenderAction renderAction;

    public void init(GLAutoDrawable drawable) {
      root = new Separator();
      PerspectiveCamera cam = new PerspectiveCamera();
      cam.setPosition(new Vec3f(0, 0, 2));
      root.addChild(cam);
      Coordinate3 coordNode = new Coordinate3();
      Vec3fCollection coords = new Vec3fCollection();
      // First triangle
      coords.add(new Vec3f( 1,  1, 0));
      coords.add(new Vec3f(-1,  1, 0));
      coords.add(new Vec3f(-1, -1, 0));
      // Second triangle
      coords.add(new Vec3f( 1,  1, 0));
      coords.add(new Vec3f(-1, -1, 0));
      coords.add(new Vec3f( 1, -1, 0));
      coordNode.setData(coords);
      root.addChild(coordNode);

      // Texture coordinates
      TextureCoordinate2 texCoordNode = new TextureCoordinate2();
      Vec2fCollection texCoords = new Vec2fCollection();
      // First triangle
      texCoords.add(new Vec2f( 1,  1));
      texCoords.add(new Vec2f( 0,  1));
      texCoords.add(new Vec2f( 0,  0));
      // Second triangle
      texCoords.add(new Vec2f( 1,  1));
      texCoords.add(new Vec2f( 0,  0));
      texCoords.add(new Vec2f( 1,  0));
      texCoordNode.setData(texCoords);
      root.addChild(texCoordNode);

      // Colors
      Color4 colorNode = new Color4();
      Vec4fCollection colors = new Vec4fCollection();
      // First triangle
      colors.add(new Vec4f(1.0f, 1.0f, 1.0f, 1.0f));
      colors.add(new Vec4f(1.0f, 1.0f, 1.0f, 1.0f));
      colors.add(new Vec4f(0.0f, 0.0f, 0.0f, 0.0f));
      // Second triangle
      colors.add(new Vec4f(1.0f, 1.0f, 1.0f, 1.0f));
      colors.add(new Vec4f(0.0f, 0.0f, 0.0f, 0.0f));
      colors.add(new Vec4f(0.0f, 0.0f, 0.0f, 0.0f));
      colorNode.setData(colors);
      root.addChild(colorNode);

      TriangleSet tris = new TriangleSet();
      tris.setNumTriangles(2);
      root.addChild(tris);

      // Testing transforms
      Transform xform = new Transform();
      xform.getTransform().setTranslation(new Vec3f(2, -2, 0));
      //      xform.getTransform().setRotation(new Rotf(new Vec3f(0, 1, 0), (float) (-Math.PI / 4)));
      root.addChild(xform);

      root.addChild(tris);

      GL gl = drawable.getGL();
      gl.glEnable(GL.GL_DEPTH_TEST);

      renderAction = new GLRenderAction();
    }

    public void display(GLAutoDrawable drawable) {
      GL gl = drawable.getGL();
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
      renderAction.apply(root);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {}
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
  }
}
