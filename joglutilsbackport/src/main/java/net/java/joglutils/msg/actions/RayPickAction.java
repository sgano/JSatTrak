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

package net.java.joglutils.msg.actions;

import java.awt.Component;
import java.lang.reflect.*;
import java.util.*;

import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

public class RayPickAction extends Action {
  // Boilerplate
  private static State defaults = new State();
  /** Returns the default state all instances of this class are initialized with. */
  public static State getDefaultState() {
    return defaults;
  }
  private static ActionTable table = new ActionTable(RayPickAction.class);

  /** Adds an action method for the given node type to this action.
      This should only be called by developers adding new node types
      and not desiring to use the standard overriding mechanisms. */
  public static void addActionMethod(Class<? extends Node> nodeType, Method m) {
    table.addActionMethod(nodeType, m);
  }

  private State state = new State(defaults);
  public State getState() {
    return state;
  }

  static {
    // Set up action methods
    // Note that because this action is built-in, we use virtual
    // method dispatch to allow overriding of rendering functionality.
    // We could optionally pull in all of the action methods into this
    // class. However, factoring the setting of the elements into the
    // nodes provides for more sharing of common functionality among
    // actions.
    try {
      addActionMethod(Node.class, RayPickAction.class.getMethod("rayPick", RayPickAction.class, Node.class));
    } catch (Exception e) {
      throw new RuntimeException("Error initializing action method for RayPickAction class", e);
    }
  }

  private int applyDepth = 0;
  private Vec2f normalizedPoint;
  private Line ray;

  private Line computedRay;

  static class RayPickedPoint implements Comparable<RayPickedPoint> {
    float t;
    PickedPoint point;

    RayPickedPoint(float t, PickedPoint point) {
      this.t = t;
      this.point = point;
    }

    public boolean equals(Object o) {
      return (o == this);
    }

    public int compareTo(RayPickedPoint p) {
      return (int) Math.signum(t - p.t);
    }
  }

  // While we're in the process of traversing the scene graph, we
  // memorize both the paths to intersected geometry as well as the
  // t parameter indicating the distance to the geometry.
  private List<RayPickedPoint> tempPickedPoints = new ArrayList<RayPickedPoint>();

  // The list of picked points, sorted by increasing distance from the camera
  private List<PickedPoint> pickedPoints = new ArrayList<PickedPoint>();

  public void apply(Node node) {
    int depth = applyDepth++;
    try {
      if (depth == 0) {
        reset();
      }
      apply(table, node);
    } finally {
      --applyDepth;
      if (depth == 0) {
        tabulate();
      }
    }
  }

  /** Sets the point for this RayPickAction based on x and y
      coordinates relative to the specified AWT Component. */
  public void setPoint(int x, int y, Component component) {
    setNormalizedPoint(new Vec2f((float) x / (float) component.getWidth(),
                                 1.0f - ((float) y / (float) component.getHeight())));
  }

  /** Sets the normalized point for this RayPickAction, where x and y
      are relative to the lower-left of the viewport and range from
      [0..1]. */
  public void setNormalizedPoint(Vec2f normalizedPoint) {
    this.normalizedPoint = normalizedPoint;
    ray = null;
    computedRay = null;
  }

  /** Sets the ray in world coordinates that this RayPickAction should
      use for its computation. */
  public void setRay(Line ray) {
    this.ray = ray;
    computedRay = ray;
    normalizedPoint = null;
  }

  /** Returns the list of points this action selected during the last
      traversal, sorted in increasing order of distance from the
      camera. Typically applications will only need to deal with the
      first point in the returned list. */
  public List<PickedPoint> getPickedPoints() {
    return pickedPoints;
  }

  /** Returns the closest point to the camera this action selected
      during the last traversal, or null if no points were picked. */
  public PickedPoint getPickedPoint() {
    List<PickedPoint> pickedPoints = getPickedPoints();
    if (pickedPoints == null || pickedPoints.isEmpty())
      return null;
    return pickedPoints.get(0);
  }

  /** Returns the computed 3D ray in world coordinates that this
      RayPickAction is using for its picking. If the action is
      configured with on-screen coordinates instead of with a 3D ray,
      then this is automatically updated every time the action
      traverses a Camera node. May return null if this has not been
      computed yet. End users should not need to call this method. */
  public Line getComputedRay() {
    return computedRay;
  }

  /** Called during scene graph traversal to update the 3D ray
      associated with this action if it was configured with on-screen
      coordinates. End users should not need to call this method. */
  public void recomputeRay(Camera camera) {
    if (normalizedPoint != null && ray == null) {
      if (computedRay == null) {
        computedRay = new Line();
      }
      camera.unproject(normalizedPoint, computedRay);
    }
  }

  /** Registers a picked point with the RayPickAction during scene
      graph traversal. The t argument is the time parameter indicating
      the distance from the camera. A reference to the PickedPoint is
      maintained internally so the caller should add a copy if the
      original is still mutable. End users should not need to call
      this method. */
  public void addPickedPoint(PickedPoint p, float t) {
    tempPickedPoints.add(new RayPickedPoint(t, p));
  }

  private void reset() {
    tempPickedPoints.clear();
    pickedPoints.clear();
  }

  private void tabulate() {
    Collections.sort(tempPickedPoints);
    for (RayPickedPoint p : tempPickedPoints) {
      pickedPoints.add(p.point);
    }
  }

  /** Action method which dispatches to per-node rendering functionality. */
  public static void rayPick(RayPickAction action, Node node) {
    node.rayPick(action);
  }
}
