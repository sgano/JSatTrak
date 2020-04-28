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

package net.java.joglutils.msg.misc;

import java.lang.reflect.*;
import java.util.*;

import net.java.joglutils.msg.actions.*;
import net.java.joglutils.msg.nodes.*;

/** Keeps track of methods which are attached to a particular Action
    type for a given set of Node types. This mechanism is different
    than using the typical Visitor design pattern because Actions,
    even built-in Actions, need to easily support the addition of
    newly added Node types. Dispatch occurs via reflection on a
    per-Node-type basis. */

public class ActionTable {
  private Class<? extends Action> actionClass;
  private Map<Class<? extends Node>, Method> methodMap = new HashMap<Class<? extends Node>, Method>();

  /** Creates an ActionTable intended for use with a particular Action
      subclass. */
  public ActionTable(Class<? extends Action> actionClass) {
    this.actionClass = actionClass;
  }

  /** Adds an action method for a particular node type to this
      table. This method will apply to this type and all subtypes
      unless one is added for a more specific subtype. An action
      method is defined as a public static method with the following
      signature: <P>

      <CODE>public static void [method name]([Action subclass] action, [Node subclass] node)</CODE> <P>

      The action subclass may be the action class this ActionTable was
      created with, or any supertype up to and including the Action
      class. This avoids unnecessary downcasting of the Action in
      action methods. <P>

      The node subclass may be the node type which is passed in to the
      addActionMethod call, or any supertype up to and including the
      Node class. This allows convenient overloading of action
      methods. <P>

      Note that it does not matter which type the action method is
      attached to. This is a key mechanism for extensibility; new
      action methods can be attached to existing action types to
      handle new node types which are added to the system. <P>

      An action uses reflection to dispatch to these methods at run
      time, caching lookup results for node types which have not been
      encountered yet.
  */
  public void addActionMethod(Class<? extends Node> nodeType, Method actionMethod)
    throws IllegalArgumentException
  {
    // Check the method to see whether it obeys our properties
    if (!Modifier.isPublic(actionMethod.getModifiers())) {
      throw new IllegalArgumentException("Action method must be public");
    }
    if (!Modifier.isStatic(actionMethod.getModifiers())) {
      throw new IllegalArgumentException("Action method must be static");
    }
    Class<?>[] argTypes = actionMethod.getParameterTypes();
    if (argTypes.length != 2) {
      throw new IllegalArgumentException("Action method must take 2 arguments");
    }
    if (!argTypes[0].isAssignableFrom(actionClass)) {
      throw new IllegalArgumentException("Action method must take a " + actionClass +
                                         " or superclass as its first argument");
    }
    if (!argTypes[1].isAssignableFrom(nodeType)) {
      throw new IllegalArgumentException("Action method must take a " + nodeType +
                                         " or superclass as its second argument");
    }
    methodMap.put(nodeType, actionMethod);
  }

  /** Looks up the appropriate action method for the given node.
      Performs caching internally to avoid excessive reflective
      lookups. Returns null if no action method was registered. This
      should only happen if an action method was not registered for
      the base Node type. */
  public Method lookupActionMethod(Node node) {
    Class<? extends Node> nodeType = node.getClass();
    Class<? extends Node> curType = nodeType;
    do {
      Method m = methodMap.get(curType);
      if (m != null) {
        // See whether we need to cache this result
        if (curType != nodeType) {
          methodMap.put(curType, m);
        }
        return m;
      }
      Class<?> nextType = curType.getSuperclass();
      if (Node.class.isAssignableFrom(nextType)) {
        curType = (Class<? extends Node>) nextType;
      } else {
        curType = null;
      }
    } while (curType != null);
    return null;
  }
}
