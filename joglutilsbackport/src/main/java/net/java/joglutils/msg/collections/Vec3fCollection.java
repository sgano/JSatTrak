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

package net.java.joglutils.msg.collections;

import java.nio.*;

import net.java.joglutils.msg.impl.*;
import net.java.joglutils.msg.math.*;

/** Provides the abstraction of a collection of Vec3f objects while
    allowing access to the backing store in the form of a direct
    FloatBuffer to make it easy to pass down to OpenGL. */

public class Vec3fCollection {
  // Data is stored as a direct FloatBuffer
  private FloatBuffer data;

  private static final int ELEMENT_SIZE = 3;

  /** Creates an empty Vec3fCollection. */
  public Vec3fCollection() {
    // Assume you'll probably want at least four vertices
    this(4);
  }

  /** Creates an empty Vec3fCollection with the backing store sized to
      hold roughly the given number of vectors. */
  public Vec3fCollection(int estimatedSize) {
    data = BufferFactory.newFloatBuffer(ELEMENT_SIZE * estimatedSize);
    data.limit(0);
  }

  /** Returns the number of Vec3fs currently in this collection. */
  public int size() {
    return data.limit() / ELEMENT_SIZE;
  }

  /** Stores the given Vec3f at the given index. If the collection has
      not grown to the given size, throws an exception. */
  public void set(int index, Vec3f value) throws IndexOutOfBoundsException {
    if (index >= size()) {
      throw new IndexOutOfBoundsException("" + index + " >= " + size());
    }
    int base = index * ELEMENT_SIZE;
    FloatBuffer buf = data;
    buf.put(base,     value.x());
    buf.put(base + 1, value.y());
    buf.put(base + 2, value.z());
  }

  /** Fetches the Vec3f at the given index. If the collection has not
      grown to the given size, throws an exception. */
  public Vec3f get(int index) throws IndexOutOfBoundsException {
    if (index >= size()) {
      throw new IndexOutOfBoundsException("" + index + " >= " + size());
    }
    int base = index * ELEMENT_SIZE;
    // Note: could use a small pool of Vec3fs here if allocation rate
    // is an issue. However, escape analysis should eventually take
    // care of this.
    FloatBuffer buf = data;
    return new Vec3f(buf.get(base), buf.get(base + 1), buf.get(base + 2));
  }

  /** Adds the given Vec3f to this collection, expanding it if
      necessary. */
  public void add(Vec3f value) {
    FloatBuffer buf = data;
    if (buf.limit() == buf.capacity()) {
      FloatBuffer newBuf = BufferFactory.newFloatBuffer(Math.max(buf.capacity() + ELEMENT_SIZE,
                                                                 round((int) (buf.capacity() * 1.5f))));
      newBuf.put(buf);
      newBuf.rewind();
      newBuf.limit(buf.limit());
      data = newBuf;
      buf = newBuf;
    }
    int pos = buf.limit();
    buf.limit(pos + ELEMENT_SIZE);
    buf.put(pos,     value.x());
    buf.put(pos + 1, value.y());
    buf.put(pos + 2, value.z());
  }

  /** Removes the given Vec3f from this collection. Moves all Vec3fs
      above it down one slot. */
  public Vec3f remove(int index) throws IndexOutOfBoundsException {
    if (index >= size()) {
      throw new IndexOutOfBoundsException("" + index + " >= " + size());
    }
    FloatBuffer buf = data;
    int pos = index * ELEMENT_SIZE;
    Vec3f res = new Vec3f(buf.get(pos), buf.get(pos + 1), buf.get(pos + 2));
    if (index == size() - 1) {
      // Simply lower the limit
      buf.limit(buf.limit() - ELEMENT_SIZE);
    } else {
      buf.position(pos + ELEMENT_SIZE);
      FloatBuffer rest = buf.slice();
      buf.position(pos);
      buf.put(rest);
      buf.limit(buf.limit() - ELEMENT_SIZE);
      buf.rewind();
    }
    return res;
  }
  
  /** Returns the backing buffer of this collection. */
  public FloatBuffer getData() {
    FloatBuffer buf = data;
    buf.position(0);
    return buf.slice();
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  private static int round(int size) {
    return size - (size % ELEMENT_SIZE);
  }
}
