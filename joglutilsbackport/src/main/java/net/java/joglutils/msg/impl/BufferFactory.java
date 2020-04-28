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

package net.java.joglutils.msg.impl;

import java.nio.*;
import java.util.*;
import com.sun.opengl.util.*;

/** Assists in allocation of direct Buffers. On some platforms when a
    small direct Buffer is allocated there is a large amount of
    rounding up which occurs. This BufferFactory allocates direct
    Buffers in chunks and hands out slices of those chunks to
    clients. */

public class BufferFactory {
  private static ByteBuffer   curByteBuf;
  private static ShortBuffer  curShortBuf;
  private static IntBuffer    curIntBuf;
  private static FloatBuffer  curFloatBuf;
  private static DoubleBuffer curDoubleBuf;

  // I believe the rounding-up size of direct Buffers on Unix platforms is 8K
  private static final int CHUNK_SIZE = 8 * 1024;

  public static synchronized ByteBuffer newByteBuffer(int numElements) {
    int sz = numElements * BufferUtil.SIZEOF_BYTE;
    if (sz > CHUNK_SIZE) {
      // Just allocate a fresh ByteBuffer and don't worry about
      // rounding up its allocation size and re-using the end portion
      return BufferUtil.newByteBuffer(numElements);
    }
    if (curByteBuf == null || curByteBuf.remaining() < numElements) {
      curByteBuf = BufferUtil.newByteBuffer(CHUNK_SIZE / BufferUtil.SIZEOF_BYTE);
    }
    curByteBuf.limit(curByteBuf.position() + numElements);
    ByteBuffer res = curByteBuf.slice();
    curByteBuf.position(curByteBuf.limit());
    return res;
  }

  public static synchronized ShortBuffer newShortBuffer(int numElements) {
    int sz = numElements * BufferUtil.SIZEOF_SHORT;
    if (sz > CHUNK_SIZE) {
      // Just allocate a fresh ShortBuffer and don't worry about
      // rounding up its allocation size and re-using the end portion
      return BufferUtil.newShortBuffer(numElements);
    }
    if (curShortBuf == null || curShortBuf.remaining() < numElements) {
      curShortBuf = BufferUtil.newShortBuffer(CHUNK_SIZE / BufferUtil.SIZEOF_SHORT);
    }
    curShortBuf.limit(curShortBuf.position() + numElements);
    ShortBuffer res = curShortBuf.slice();
    curShortBuf.position(curShortBuf.limit());
    return res;
  }

  public static synchronized IntBuffer newIntBuffer(int numElements) {
    int sz = numElements * BufferUtil.SIZEOF_INT;
    if (sz > CHUNK_SIZE) {
      // Just allocate a fresh IntBuffer and don't worry about
      // rounding up its allocation size and re-using the end portion
      return BufferUtil.newIntBuffer(numElements);
    }
    if (curIntBuf == null || curIntBuf.remaining() < numElements) {
      curIntBuf = BufferUtil.newIntBuffer(CHUNK_SIZE / BufferUtil.SIZEOF_INT);
    }
    curIntBuf.limit(curIntBuf.position() + numElements);
    IntBuffer res = curIntBuf.slice();
    curIntBuf.position(curIntBuf.limit());
    return res;
  }

  public static synchronized FloatBuffer newFloatBuffer(int numElements) {
    int sz = numElements * BufferUtil.SIZEOF_FLOAT;
    if (sz > CHUNK_SIZE) {
      // Just allocate a fresh FloatBuffer and don't worry about
      // rounding up its allocation size and re-using the end portion
      return BufferUtil.newFloatBuffer(numElements);
    }
    if (curFloatBuf == null || curFloatBuf.remaining() < numElements) {
      curFloatBuf = BufferUtil.newFloatBuffer(CHUNK_SIZE / BufferUtil.SIZEOF_FLOAT);
    }
    curFloatBuf.limit(curFloatBuf.position() + numElements);
    FloatBuffer res = curFloatBuf.slice();
    curFloatBuf.position(curFloatBuf.limit());
    return res;
  }

  public static synchronized DoubleBuffer newDoubleBuffer(int numElements) {
    int sz = numElements * BufferUtil.SIZEOF_DOUBLE;
    if (sz > CHUNK_SIZE) {
      // Just allocate a fresh DoubleBuffer and don't worry about
      // rounding up its allocation size and re-using the end portion
      return BufferUtil.newDoubleBuffer(numElements);
    }
    if (curDoubleBuf == null || curDoubleBuf.remaining() < numElements) {
      curDoubleBuf = BufferUtil.newDoubleBuffer(CHUNK_SIZE / BufferUtil.SIZEOF_DOUBLE);
    }
    curDoubleBuf.limit(curDoubleBuf.position() + numElements);
    DoubleBuffer res = curDoubleBuf.slice();
    curDoubleBuf.position(curDoubleBuf.limit());
    return res;
  }
}
