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

import java.util.*;

import net.java.joglutils.msg.elements.*;

/** Represents a collection of state elements, which are updated by
    actions during scene graph traversal. */

public class State {
  // Provides each concrete Element subclass a unique slot in the State.
  // Note that subclasses of concrete Element subclasses share the
  // slot with their parent class. Which one is used for a given
  // Action is decided by which elements are enabled for that action.
  private static int curStateIndex = 0;
  
  // The representation of the State.
  private List<Element> elements = new ArrayList<Element>();

  // The default enabled elements for this State. The State instances
  // created by each Action instance point to the default enabled
  // elements; this is where the individual Action's State is
  // initialized from.
  // Note that this mechanism is needed because different actions
  // install different Element subclasses (i.e., ModelMatrixElement
  // vs. GLModelMatrixElement) into the same element slot.
  private State defaults;

  // A linked list of elements touched since the last push, used to
  // reduce the number of elements that need to be accessed during a
  // pop() operation.
  private Element topElement;

  // The depth at which we are operating, to implement lazy pushing
  // and popping of state elements
  private int depth;

  /** This constructor should only be used for the default State for a
      given Action subclass. */
  public State() {
  }

  /** This constructor should be used to create the concrete State
      instances for each Action instance. The default State given
      should be that for the particular Action class. */
  public State(State defaults) {
    this.defaults = defaults;
    // Do a push() to ensure that we always have a non-null and
    // pristine entry at the top of each stack
    push();
  }

  /** Returns the default State, or this State if it corresponds to
      the defaults for a given Action subclass. */
  public State getDefaults() {
    if (defaults != null)
      return defaults;
    return this;
  }

  /** Gets the state element at the given index. */
  public Element getElement(StateIndex index) {
    // The comments in the Open Inventor implementation indicate that
    // a bug that was found and fixed was that this method must not be
    // called in the process of popping the state. This assert
    // attempts to guard against that happening.
    assert depth >= ((topElement == null) ? 0 : topElement.getDepth()) :
      "Elements must not be changed while the state is being popped (element being changed: " +
      elements.get(index.getIndex()).getClass().getName() + ").";

    int idx = index.getIndex();

    if (defaults == null) {
      // This State contains the defaults for a particular Action
      // class. Don't do anything fancy -- just return the element at
      // the particular index.
      if (idx >= elements.size()) {
        return null;
      }
      return elements.get(idx);
    }

    if (idx >= elements.size()) {
      // Expand list to the needed size
      while (idx >= elements.size()) {
        elements.add(null);
      }
    }

    Element elt = elements.get(idx);
    if (elt == null) {
      // Lazily create a copy of the default and put it in place
      elt = defaults.getElement(index);
      if (elt == null) {
        throw new RuntimeException("Error in initialization of default element for state index " + idx);
      }
      elt = elt.newInstance();
      elt.setDepth(0);
      elements.set(idx, elt);
    }

    // If element is not at current depth, we have to push a new
    // element on the stack
    if (elt.getDepth() < depth) {
      // FIXME: consider doubly-linked-list scheme as in Inventor to
      // avoid excessive object creation during scene graph traversal
      Element newElt = elt.newInstance();
      newElt.setNextInStack(elt);
      newElt.setDepth(depth);
      // Add newly-created element to the all-element stack
      newElt.setNext(topElement);
      topElement = newElt;
      elements.set(idx, newElt);
      // Call push on new element in case it has side effects
      newElt.push(this);
      // Return new element
      elt = newElt;
    }

    return elt;
  }

  /** Sets the element at the given state index. This should only be
      used by Action, Element and Node subclasses to initialize the
      default state for a given Action class. */
  public void setElement(StateIndex index, Element element) {
    if (defaults != null) {
      throw new RuntimeException("Misuse of setElement(); should only be used to initialize default State for an Action");
    }
    int idx = index.getIndex();
    if (idx >= elements.size()) {
      while (idx >= elements.size()) {
        elements.add(null);
      }
    }
    elements.set(idx, element);
  }

  /** Pushes (saves) the current state until a pop() restores it. The
      push is done lazily: this just increments the depth in the
      state. When an element is accessed with getElement() and its
      depth is less than the current depth, it is then pushed
      individually. */
  public void push() {
    ++depth;
  }

  /** Pops the state, restoring the state to just before the last push(). */
  public void pop() {
    --depth;

    Element poppedElt = null;
    Element nextInStack = null;

    // As in Open Inventor, the popping is done in two passes. This is
    // apparently needed if Open Inventor-style caching is added in
    // the future. The first pass calls pop() on all of the elements
    // that will be popped; the second pass actually removes the
    // elements from their respective stacks.
    for (poppedElt = topElement;
	 poppedElt != null && poppedElt.getDepth() > depth;
	 poppedElt = poppedElt.getNext()) {

      // Find the next element in the same stack as the element being
      // popped. This element will become the new top of that stack.
      nextInStack = poppedElt.getNextInStack();

      // Give the new top element in the stack a chance to update
      // things. Pass old element instance just in case.
      poppedElt.getNextInStack().pop(this, poppedElt);
    }
    
    // Remove all such elements from their respective stacks
    while (topElement != null && topElement.getDepth() > depth) {
      poppedElt = topElement;

      // Remove from main element list
      topElement = topElement.getNext();

      // Remove from element stack
      elements.set(poppedElt.getStateIndex().getIndex(), poppedElt.getNextInStack());
    }
  }

  /** Should be called by Element subclasses to register themselves
      with the State class. This provides them a StateIndex with which
      they can index into the State. */
  public static synchronized StateIndex registerElementType() {
    return new StateIndex(curStateIndex++);
  }
}
