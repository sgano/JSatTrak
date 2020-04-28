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

import net.java.joglutils.msg.misc.*;
import net.java.joglutils.msg.nodes.*;

/** An action which resets the OpenGL state of notes holding on to
    server-side OpenGL objects. This is needed to clear out for
    example Texture2 nodes if the OpenGL context being used to render
    them was destroyed and re-created. */

public class GLResetAction extends Action {
  // Boilerplate
  private static State defaults = new State();
  /** Returns the default state all instances of this class are initialized with. */
  public static State getDefaultState() {
    return defaults;
  }
  private static ActionTable table = new ActionTable(GLResetAction.class);

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

    // Note that even though this is a built-in action, we currently
    // attach individual action methods for a couple of node classes
    // to avoid baking in the resetGL notion, which has not been fully
    // thought through yet, at the base Node level.
    try {
      addActionMethod(Texture2.class,   GLResetAction.class.getMethod("resetGL", GLResetAction.class, Texture2.class));
      addActionMethod(ShaderNode.class, GLResetAction.class.getMethod("resetGL", GLResetAction.class, ShaderNode.class));
      // FIXME: add something for the ShaderNode
    } catch (Exception e) {
      throw new RuntimeException("Error initializing action methods for GLResetAction class", e);
    }
  }

  public void apply(Node node) {
    apply(table, node);
  }

  /** Action method which dispatches to per-node rendering functionality. */
  public static void resetGL(GLResetAction action, Texture2 node) {
    node.resetGL(action);
  }

  /** Action method which dispatches to per-node rendering functionality. */
  public static void resetGL(GLResetAction action, ShaderNode node) {
    node.resetGL(action);
  }
}
