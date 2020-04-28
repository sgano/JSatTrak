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

import java.lang.reflect.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

/** An action which performs rendering of a scene graph via OpenGL. <P>

    When applied to the root of the scene graph, this action does not
    perform any clearing of the color or depth buffer; this is the
    responsibility of the caller. The render action pushes, pops, and
    initializes enough OpenGL state to isolate itself, at least in
    theory, from any surrounding OpenGL state that the application may
    have set up. There should in theory be no user-visible OpenGL side
    effects as a result of rendering with this action.
*/

public class GLRenderAction extends Action {
  // Boilerplate
  private static State defaults = new State();
  /** Returns the default state all instances of this class are initialized with. */
  public static State getDefaultState() {
    return defaults;
  }
  private static ActionTable table = new ActionTable(GLRenderAction.class);

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
      addActionMethod(Node.class, GLRenderAction.class.getMethod("render", GLRenderAction.class, Node.class));
    } catch (Exception e) {
      throw new RuntimeException("Error initializing action method for GLRenderAction class", e);
    }
  }

  // For automatically setting the aspect ratios of cameras we encounter
  private float curAspectRatio = 1.0f;

  private int applyDepth = 0;
  private GL gl;

  public void apply(Node node) {
    int depth = applyDepth++;
    try {
      if (depth == 0) {
        gl = GLU.getCurrentGL();
        // Applying to the root of the scene graph
        // Push necessary GL state
        // FIXME: add in additional bits as we add more capabilities
        gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_TRANSFORM_BIT);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glColor4f(1, 1, 1, 1);
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        // FIXME: should add in disabling of normal array
        // Figure out the aspect ratio of the current viewport
        int[] viewport = new int[4];
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        curAspectRatio = (float) viewport[2] / (float) viewport[3];
      }
      apply(table, node);
    } finally {
      if (depth == 0) {
        gl.glPopClientAttrib();
        gl.glPopAttrib();
        gl = null;
      }
      --applyDepth;
    }
  }

  /** Returns the GL instance being used for rendering. */
  public GL getGL() {
    return gl;
  }

  /** Fetches the current aspect ratio of the viewport this
      GLRenderAction is rendering into. */
  public float getCurAspectRatio() {
    return curAspectRatio;
  }

  /** Action method which dispatches to per-node rendering functionality. */
  public static void render(GLRenderAction action, Node node) {
    node.render(action);
  }
}
