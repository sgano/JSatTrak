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

import net.java.joglutils.msg.actions.*;
import net.java.joglutils.msg.misc.*;

/** An IndexedTriangleSet assembles the coordinates specified by a
    Coordinate3 node, and any auxiliary nodes such as a
    TextureCoordinate2 node, into a set of triangles by indexing into
    the pools of coordinates set up by these other nodes. (NOTE:
    (FIXME) rendering support for this node is not yet
    implemented.) */

public class IndexedTriangleSet extends TriangleBasedShape {
  private IntBuffer indices;

  /** Sets the indices this node uses to group vertices into triangles. */
  public void setIndices(IntBuffer indices) {
    this.indices = indices;
  }

  /** Returns the indices this node uses to group vertices into triangles. */
  public IntBuffer getIndices() {
    return indices;
  }

  public void doAction(Action action) {
    throw new RuntimeException("Not yet implemented");
  }

  public void generateTriangles(Action action, TriangleCallback cb) {
    throw new RuntimeException("Not yet implemented");
  }
}
