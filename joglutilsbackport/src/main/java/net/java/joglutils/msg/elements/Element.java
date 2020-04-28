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

import net.java.joglutils.msg.misc.*;

/** Represents an element in the global {@link
    net.java.joglutils.msg.misc.State state}, such as the current 3D
    coordinates or texture to be applied. The organization of the
    global state into elements is one of the key mechanisms for
    extendability of the library which was pioneered by Open Inventor.
*/

public abstract class Element {
  // Elements are organized into stacks. When we descend past (for
  // example) a Separator node, we need to push all of the state
  // elements, traverse the children, and then pop the state elements
  // so afterward we restore the original state. This is implemented
  // in the State class using a linked list of Elements, where the
  // State keeps track of the top of each element stack.
  private Element nextInStack;

  // Additionally we maintain a linked list through all Element
  // instances pushed and popped in the State, so that we don't have
  // to traverse all Element slots when performing a state pop.
  private Element next;

  // Elements need to keep track of their depth in the stack in order
  // for the State to maintain itself
  private int depth;

  protected Element() {}

  /** Creates a new instance initialized to the default values for the
      state element. All concrete Element subclasses must implement
      this operation. */
  public abstract Element newInstance();

  /** Returns the next element in the stack. */
  public Element getNextInStack()                    { return nextInStack;             }
  /** Sets the next element in the stack. */
  public void    setNextInStack(Element nextInStack) { this.nextInStack = nextInStack; }

  /** Returns the next element in the linked list of elements which
      were modified since the last state push. */
  public Element getNext()             { return next;      }
  /** Sets the next element in the linked list of elements which were
      modified since the last state push. */
  public void    setNext(Element next) { this.next = next; }

  /** Returns the depth of this element in its stack, used to implement lazy state pushing. */
  public int     getDepth()          { return depth;       }
  /** Sets the depth of this element in its stack, used to implement lazy state pushing. */
  public void    setDepth(int depth) { this.depth = depth; }

  /** Pushes the element, allowing for side effects to occur. Default method does nothing. */
  public void    push(State state) {}
  /** Pops the element, allowing for side effects to occur. Default
      method does nothing. NOTE that it is not legal to call
      State.getElement() in the implementation of this method, which
      is why the previous top element is provided as an argument. */
  public void    pop (State state, Element previousTopElement) {}

  /** All concrete element subclasses must register themselves with
      the State in order to reserve a slot, or index, in the
      state. This method must be overridden to return this slot. */
  public abstract StateIndex getStateIndex();
}
