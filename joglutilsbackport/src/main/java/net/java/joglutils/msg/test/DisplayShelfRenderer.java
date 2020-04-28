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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import javax.media.opengl.*;
import com.sun.opengl.util.j2d.*;

import net.java.joglutils.msg.actions.*;
import net.java.joglutils.msg.collections.*;
import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

/**
 * A test implementing a 3D display shelf component. This renderer is
 * pluggable into any JOGL GLAutoDrawable.
 *
 * @author Kenneth Russell
 */

public class DisplayShelfRenderer implements GLEventListener {
  private float DEFAULT_ASPECT_RATIO = 0.665f;
  // This also affects the spacing
  private float DEFAULT_HEIGHT = 1.5f;
  private float DEFAULT_ON_SCREEN_FRAC = 0.5f;
  private float EDITING_ON_SCREEN_FRAC = 0.95f;
  private float offsetFrac;

  private float STACKED_SPACING_FRAC  = 0.3f;
  private float SELECTED_SPACING_FRAC = 0.6f;
  private float EDITED_SPACING_FRAC   = 1.5f;

  // This is how much we raise the geometry above the floor in single image mode
  private float SINGLE_IMAGE_MODE_RAISE_FRAC = 2.0f;

  // The camera
  private PerspectiveCamera camera;

  static class TitleGraph {
    Object imageDescriptor;
    Separator sep   = new Separator();
    Transform xform = new Transform();
    Texture2  texture = new Texture2();
    Coordinate3 coords = new Coordinate3();

    TitleGraph(Object imageDescriptor) {
      this.imageDescriptor = imageDescriptor;
    }
  }

  // This is used to avoid having to re-initialize textures during
  // resizes of Swing components
  private GLPbuffer sharedPbuffer;
  private boolean firstInit = true;

  private GLAutoDrawable drawable;

  private Separator root;
  private Separator imageRoot;
  private Fetcher<Integer> fetcher;
  private ListModel model;
  private List<TitleGraph> titles = new ArrayList<TitleGraph>();
  private GLRenderAction ra = new GLRenderAction();
  private int targetIndex;
  // This encodes both the current position and the horizontal animation alpha
  private float currentIndex;
  // This encodes the animation alpha for the z-coordinate motion
  // associated with going in to and out of editing mode
  private float currentZ;
  private float targetZ;
  // This is effectively a constant
  private float viewingZ;
  // This is also currently effectively a constant, though we need to
  // compute it dynamically for each picture to get it to show up
  // centered
  private float editingZ;
  // This encodes our current Y coordinate in editing mode
  private float currentY;
  // This encodes our target Y coordinate in editing mode
  private float targetY;
  // If the difference between the current and target values of any of
  // the above are > EPSILON, then we will continue repainting
  private static final float EPSILON = 1.0e-3f;
  private SystemTime time;
  private boolean animating;
  private boolean forceRecompute;
  // Single image mode toggle
  private boolean singleImageMode;

  // A scale factor for the animation speed
  private static final float ANIM_SCALE_FACTOR = 7.0f;
  // The rotation angle of the titles
  private static final float ROT_ANGLE = (float) Math.toRadians(75);

  // Visual progress of downloads
  private Texture2 clockTexture;
  private volatile boolean doneLoading;

  class DownloadListener implements ProgressListener<Integer> {
    public void progressStart(ProgressEvent<Integer> evt) {}
    public void progressUpdate(ProgressEvent<Integer> evt) {}
    public void progressEnd(ProgressEvent<Integer> evt) {
      updateImage(evt.getClientIdentifier());
    }
  }

  public DisplayShelfRenderer(ListModel model) {
    // Create a small pbuffer with which we share textures and display
    // lists to avoid having to reload textures during repeated calls
    // to init()
    sharedPbuffer = GLDrawableFactory.getFactory().createGLPbuffer(new GLCapabilities(), null, 1, 1, null);
    sharedPbuffer.display();

    this.fetcher = new BasicFetcher<Integer>();
    fetcher.addProgressListener(new DownloadListener());
    this.model = model;
    root = new Separator();
    time = new SystemTime();
    time.rebase();
    camera = new PerspectiveCamera();
    camera.setNearDistance(1.0f);
    camera.setFarDistance(100.0f);
    camera.setHeightAngle((float) Math.PI / 8);
    // This could / should be computed elsewhere, especially if we add
    // the ability to dynamically adjust the camera's height angle
    viewingZ = 0.5f * DEFAULT_HEIGHT / (DEFAULT_ON_SCREEN_FRAC * (float) Math.tan(camera.getHeightAngle()));
    // Compute the fraction by which we offset the selected title
    // based on a couple of known good points
    offsetFrac = (float) (((3 * Math.PI / 40) / camera.getHeightAngle()) + 0.1f);
  }

  /** Callers must share textures and display lists with this context
      for correct behavior of this renderer. It is used to avoid
      repeated reloading of textures when resizing the renderer
      embedded in a GLJPanel. */
  public GLContext getSharedContext() {
    return sharedPbuffer.getContext();
  }

  public void setSingleImageMode(boolean singleImageMode, boolean animateTransition) {
    this.singleImageMode = singleImageMode;
    if (!animating) {
      time.rebase();
    }
    recomputeTargetYZ(animateTransition);
    forceRecompute = !animateTransition;
    if (drawable != null) {
      drawable.repaint();
    }
  }

  public boolean getSingleImageMode() {
    return singleImageMode;
  }

  public int getNumImages() {
    return titles.size();
  }

  public void setTargetIndex(int index) {
    if (targetIndex == index)
      return;

    this.targetIndex = index;
    if (!animating) {
      time.rebase();
    }
    recomputeTargetYZ(true);
    if (drawable != null) {
      drawable.repaint();
    }
  }

  public int getTargetIndex() {
    return targetIndex;
  }

  public void init(GLAutoDrawable drawable) {
    this.drawable = drawable;
    GL gl = drawable.getGL();

    if (firstInit) {
      firstInit = false;

      // Build the scene graph

      // The clock
      clockTexture = new Texture2();
      clockTexture.initTextureRenderer((int) (300 * DEFAULT_HEIGHT * DEFAULT_ASPECT_RATIO),
                                       (int) (300 * DEFAULT_HEIGHT),
                                       false);

      // The images
      imageRoot = new Separator();

      // The mirrored images under the floor
      Separator mirrorRoot = new Separator();

      Transform mirrorXform = new Transform();
      // Mirror vertically
      mirrorXform.getTransform().set(1, 1, -1.0f);
      mirrorRoot.addChild(mirrorXform);
      // Assume we know what we're doing here with setting per-vertex
      // colors for each piece of geometry in one shot
      Color4 colorNode = new Color4();
      Vec4fCollection colors = new Vec4fCollection();
      Vec4f fadeTop = new Vec4f(0.75f, 0.75f, 0.75f, 0.75f);
      Vec4f fadeBot = new Vec4f(0.25f, 0.25f, 0.25f, 0.25f);
      // First triangle
      colors.add(fadeTop);
      colors.add(fadeTop);
      colors.add(fadeBot);
      // Second triangle    
      colors.add(fadeTop);
      colors.add(fadeBot);
      colors.add(fadeBot);
      colorNode.setData(colors);
      mirrorRoot.addChild(colorNode);

      TriangleSet tris = new TriangleSet();
      tris.setNumTriangles(2);

      for (int i = 0; i < model.getSize(); i++) {
        Object obj = model.getElementAt(i);
        TitleGraph graph = new TitleGraph(obj);
        titles.add(graph);
        computeCoords(graph.coords, DEFAULT_ASPECT_RATIO);
        graph.xform.getTransform().setTranslation(new Vec3f(i, 0, 0));
        Separator sep = graph.sep;
        sep.addChild(graph.xform);
        sep.addChild(graph.coords);
        // Add in the clock texture at the beginning
        sep.addChild(clockTexture);
        TextureCoordinate2 texCoordNode = new TextureCoordinate2();
        Vec2fCollection texCoords = new Vec2fCollection();
        // Texture coordinates for two triangles
        // First triangle
        texCoords.add(new Vec2f( 1,  1));
        texCoords.add(new Vec2f( 0,  1));
        texCoords.add(new Vec2f( 0,  0));
        // Second triangle
        texCoords.add(new Vec2f( 1,  1));
        texCoords.add(new Vec2f( 0,  0));
        texCoords.add(new Vec2f( 1,  0));
        texCoordNode.setData(texCoords);
        sep.addChild(texCoordNode);

        sep.addChild(tris);

        // Add this to each rendering root
        imageRoot.addChild(sep);
        mirrorRoot.addChild(sep);
      }

      // Now produce the floor geometry
      float maxSpacing = DEFAULT_HEIGHT * Math.max(STACKED_SPACING_FRAC, Math.max(SELECTED_SPACING_FRAC, EDITED_SPACING_FRAC));
      int i = model.getSize();
      float minx = -i * maxSpacing;
      float maxx = 2 * i * maxSpacing;
      // Furthest back from the camera
      float minz = -2 * DEFAULT_HEIGHT;
      // Assume this will be close enough to cover all of the mirrored geometry
      float maxz =  DEFAULT_HEIGHT;
      Separator floorRoot = new Separator();
      Blend blend = new Blend();
      blend.setEnabled(true);
      blend.setSourceFunc(Blend.ONE);
      blend.setDestFunc(Blend.ONE_MINUS_SRC_ALPHA);
      floorRoot.addChild(blend);
      Coordinate3 floorCoords = new Coordinate3();
      floorCoords.setData(new Vec3fCollection());
      // First triangle
      floorCoords.getData().add(new Vec3f(maxx, 0, minz));
      floorCoords.getData().add(new Vec3f(minx, 0, minz));
      floorCoords.getData().add(new Vec3f(minx, 0, maxz));
      // Second triangle    
      floorCoords.getData().add(new Vec3f(maxx, 0, minz));
      floorCoords.getData().add(new Vec3f(minx, 0, maxz));
      floorCoords.getData().add(new Vec3f(maxx, 0, maxz));
      floorRoot.addChild(floorCoords);
      // Colors
      Vec4f gray = new Vec4f(0.4f, 0.4f, 0.4f, 0.4f);
      Vec4f clearGray = new Vec4f(0.0f, 0.0f, 0.0f, 0.0f);
      Color4 floorColors = new Color4();
      floorColors.setData(new Vec4fCollection());
      // First triangle
      floorColors.getData().add(gray);
      floorColors.getData().add(gray);
      floorColors.getData().add(clearGray);
      // Second triangle    
      floorColors.getData().add(gray);
      floorColors.getData().add(clearGray);
      floorColors.getData().add(clearGray);
      floorRoot.addChild(floorColors);
    
      floorRoot.addChild(tris);

      // Now set up the overall scene graph
      root.addChild(camera);
      root.addChild(imageRoot);
      root.addChild(mirrorRoot);
      root.addChild(floorRoot);

      // Attach listeners (this is only for testing for now)
      drawable.addMouseListener(new MListener());
      drawable.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
              setSingleImageMode(!getSingleImageMode(), true);
              break;

            case KeyEvent.VK_ENTER:
              setSingleImageMode(!getSingleImageMode(), false);
              break;

            case KeyEvent.VK_LEFT:
              setTargetIndex(Math.max(0, targetIndex - 1));
              break;

            case KeyEvent.VK_RIGHT:
              setTargetIndex(Math.min(titles.size() - 1, targetIndex + 1));
              break;
            }
          }
        });

      startClockAnimation();
      recomputeTargetYZ(false);
      forceRecompute = true;
      recompute();

      // Get the loading started
      for (int j = 0; j < titles.size(); j++) {
        updateImage(j);
      }
    }
  }

  public void display(GLAutoDrawable drawable) {
    // Recompute position of camera and orientation of images
    boolean repaintAgain = recompute();

    if (!doneLoading) {
      if (!repaintAgain) {
        time.update();
      }

      TextureRenderer rend = clockTexture.getTextureRenderer();
      Graphics2D g = rend.createGraphics();
      drawClock(g, (int) (time.time() * 30),
                0, 0, rend.getWidth(), rend.getHeight());
      g.dispose();
      rend.markDirty(0, 0, rend.getWidth(), rend.getHeight());
    }

    // Redraw
    GL gl = drawable.getGL();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    ra.apply(root);

    if (repaintAgain) {
      animating = true;
      drawable.repaint();
    } else {
      animating = false;
    }
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
  }

  public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  private void computeCoords(Coordinate3 coordNode, float aspectRatio) {
    Vec3fCollection coords = coordNode.getData();
    if (coords == null) {
      coords = new Vec3fCollection();
      Vec3f zero = new Vec3f();
      for (int i = 0; i < 6; i++) {
        coords.add(zero);
      }
      coordNode.setData(coords);
    }
    // Now compute the actual values
    Vec3f lowerLeft  = new Vec3f(-0.5f * DEFAULT_HEIGHT * aspectRatio, 0, 0);
    Vec3f lowerRight = new Vec3f( 0.5f * DEFAULT_HEIGHT * aspectRatio, 0, 0);
    Vec3f upperLeft  = new Vec3f(-0.5f * DEFAULT_HEIGHT * aspectRatio, DEFAULT_HEIGHT, 0);
    Vec3f upperRight = new Vec3f( 0.5f * DEFAULT_HEIGHT * aspectRatio, DEFAULT_HEIGHT, 0);
    // First triangle
    coords.set(0, upperRight);
    coords.set(1, upperLeft);
    coords.set(2, lowerLeft);
    // Second triangle
    coords.set(3, upperRight);
    coords.set(4, lowerLeft);
    coords.set(5, lowerRight);
  }

  private static void drawClock(Graphics2D g, int minsPastMidnight,
                                int x, int y, int width, int height) {
    g.setColor(Color.DARK_GRAY);
    g.fillRect(x, y, width, height);
    g.setColor(Color.GRAY);
    int midx = (int) (x + (width / 2.0f));
    int midy = (int) (y + (height / 2.0f));
    int sz = (int) (0.8f * Math.min(width, height));
    g.setStroke(new BasicStroke(sz / 20.0f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_MITER));
    int arcSz = (int) (0.4f * sz);
    int smallHandSz = (int) (0.3f * sz);
    int bigHandSz   = (int) (0.4f * sz);
    g.drawRoundRect(midx - (sz / 2), midy - (sz / 2),
                    sz, sz,
                    arcSz, arcSz);
    float hour = minsPastMidnight / 60.0f;
    int   min  = minsPastMidnight % 60;
    float hourAngle = hour * 2.0f * (float) Math.PI / 12;
    float minAngle  = min * 2.0f * (float) Math.PI / 60;

    g.drawLine(midx, midy,
               midx + (int) (smallHandSz * Math.cos(hourAngle)),
               midy + (int) (smallHandSz * Math.sin(hourAngle)));
    g.drawLine(midx, midy,
               midx + (int) (bigHandSz * Math.cos(minAngle)),
               midy + (int) (bigHandSz * Math.sin(minAngle)));
  }

  private void startClockAnimation() {
    Thread clockAnimThread = new Thread(new Runnable() {
        public void run() {
          while (!doneLoading) {
            drawable.repaint();
            try {
              Thread.sleep(100);
            } catch (InterruptedException e) {
            }
          }
        }
      });
    clockAnimThread.start();
  }

  private void updateImage(int id) {
    TitleGraph graph = titles.get(id);
    // Re-fetch
    BufferedImage img = fetcher.getImage(graph.imageDescriptor,
                                         Integer.valueOf(id),
                                         -1);
    if (img != null) {
      // We don't need the image descriptor any more
      graph.imageDescriptor = null;
      graph.sep.replaceChild(clockTexture, graph.texture);
      graph.texture.setTexture(img, false);
      // Figure out the new aspect ratio based on the image's width and height
      float aspectRatio = (float) img.getWidth() / (float) img.getHeight();
      // Compute new coordinates
      computeCoords(graph.coords, aspectRatio);
      // Schedule a repaint
      drawable.repaint();
    }

    // See whether we're completely done loading
    boolean done = true;
    for (TitleGraph cur : titles) {
      if (cur.imageDescriptor != null) {
        done = false;
        break;
      }
    }
    if (done) {
      doneLoading = true;
    }
  }

  private void recomputeTargetYZ(boolean animate) {
    if (singleImageMode) {
      // Compute a target Y and Z depth based on the image we want to view

      // FIXME: right now the Y and Z targets are always the same, but
      // once we adjust the images to fit within a bounding square,
      // they won't be
      targetY = (0.5f + SINGLE_IMAGE_MODE_RAISE_FRAC) * DEFAULT_HEIGHT;
      editingZ = 0.5f * DEFAULT_HEIGHT / (EDITING_ON_SCREEN_FRAC * (float) Math.tan(camera.getHeightAngle()));
      targetZ = editingZ;
    } else {
      targetY = 0.5f * DEFAULT_HEIGHT;
      targetZ = viewingZ;
    }

    if (!animate) {
      currentY = targetY;
      currentZ = targetZ;
      currentIndex = targetIndex;
    }
  }

  private boolean recompute() {
    if (!forceRecompute) {
      if (Math.abs(targetIndex - currentIndex) < EPSILON &&
          Math.abs(targetZ - currentZ) < EPSILON &&
          Math.abs(targetY - currentY) < EPSILON)
        return false;
    }

    forceRecompute = false;

    time.update();
    float deltaT = (float) time.deltaT();

    // Make the animation speed independent of frame rate
    currentIndex = currentIndex + (targetIndex - currentIndex) * deltaT * ANIM_SCALE_FACTOR;
    currentZ = currentZ + (targetZ - currentZ) * deltaT * ANIM_SCALE_FACTOR;
    currentY = currentY + (targetY - currentY) * deltaT * ANIM_SCALE_FACTOR;
    // An alpha of 0 indicates we're fully in viewing mode
    // An alpha of 1 indicates we're fully in editing mode
    float zAlpha = (currentZ - viewingZ) / (editingZ - viewingZ);

    // Recompute the positions and orientations of each title, and the position of the camera
    int firstIndex  = (int) Math.floor(currentIndex);
    int secondIndex = (int) Math.ceil(currentIndex);
    if (secondIndex == firstIndex) {
      secondIndex = firstIndex + 1;
    }

    float alpha = currentIndex - firstIndex;

    int idx = 0;
    float curPos = 0.0f;
    float stackedSpacing  = DEFAULT_HEIGHT * (zAlpha * EDITED_SPACING_FRAC + (1.0f - zAlpha) * STACKED_SPACING_FRAC);
    float selectedSpacing = DEFAULT_HEIGHT * (zAlpha * EDITED_SPACING_FRAC + (1.0f - zAlpha) * SELECTED_SPACING_FRAC);
    float angle = (1.0f - zAlpha) * ROT_ANGLE;
    float y = zAlpha * DEFAULT_HEIGHT * SINGLE_IMAGE_MODE_RAISE_FRAC;
    Rotf posAngle = new Rotf(Vec3f.Y_AXIS,  angle);
    Rotf negAngle = new Rotf(Vec3f.Y_AXIS, -angle);
    float offset = 0;

    // Only bump the selected title out of the list if we're in viewing mode and close to it
    if (Math.abs(targetIndex - currentIndex) < 3.0) {
      offset = (1.0f - zAlpha) * offsetFrac * DEFAULT_HEIGHT;
    }
    for (TitleGraph graph : titles) {
      if (idx < firstIndex) {
        graph.xform.getTransform().setRotation(posAngle);
        graph.xform.getTransform().setTranslation(new Vec3f(curPos, y, 0));
        curPos += stackedSpacing;
      } else if (idx > secondIndex) {
        graph.xform.getTransform().setRotation(negAngle);
        graph.xform.getTransform().setTranslation(new Vec3f(curPos, y, 0));
        curPos += stackedSpacing;
      } else if (idx == firstIndex) {
        // Bump the position of this title
        curPos += (1.0f - alpha) * (selectedSpacing - stackedSpacing);

        // The camera is glued to this position
        float cameraPos = curPos + alpha * selectedSpacing;

        // Interpolate
        graph.xform.getTransform().setRotation(new Rotf(Vec3f.Y_AXIS, alpha * angle));
        graph.xform.getTransform().setTranslation(new Vec3f(curPos, y, (1.0f - alpha) * offset));

        // Now recompute the position of the camera
        // Aim to get the titles to fill a certain fraction of the vertical field of view
        camera.setPosition(new Vec3f(cameraPos,
                                     currentY,
                                     currentZ));

        // Maintain this much distance between the two animating titles
        curPos += selectedSpacing;
      } else {
        // Interpolate
        graph.xform.getTransform().setRotation(new Rotf(Vec3f.Y_AXIS, (1.0f - alpha) * -angle));
        graph.xform.getTransform().setTranslation(new Vec3f(curPos, y, alpha * offset));

        curPos += stackedSpacing + alpha * (selectedSpacing - stackedSpacing);
      }

      ++idx;
    }

    return true;
  }

  class MListener extends MouseAdapter {
    RayPickAction ra = new RayPickAction();

    public void mousePressed(MouseEvent e) {
      ra.setPoint(e.getX(), e.getY(), e.getComponent());
      // Apply to the scene root
      ra.apply(root);
      List<PickedPoint> pickedPoints = ra.getPickedPoints();
      Path p = null;
      if (!pickedPoints.isEmpty())
        p = pickedPoints.get(0).getPath();
      if (p != null && p.size() > 1) {
        int idx = imageRoot.findChild(p.get(p.size() - 2));
        if (idx >= 0) {
          setTargetIndex(idx);
          // Need to keep the slider and this mechanism in sync
          // FIXME: fire an event here
          //          slider.setValue(idx);
        }
      }
    }
  }
}
