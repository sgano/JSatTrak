/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render.airspaces;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.Cacheable;

import javax.media.opengl.GL;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * @author dcollins
 * @version $Id: Geometry.java 6423 2008-09-05 05:12:56Z dcollins $
 */
public class Geometry extends AVListImpl implements Cacheable
{
    public static class CacheKey
    {
        private final Class cls;
        private final String key;
        private final Object[] params;
        private int hash = 0;

        public CacheKey(Class cls, String key, Object... params)
        {
            this.cls = cls;
            this.key = key;
            this.params = params;
        }

        public CacheKey(String key, Object... params)
        {
            this(null, key, params);
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            CacheKey cacheKey = (CacheKey) o;

            if (this.cls != null ? !this.cls.equals(cacheKey.cls) : cacheKey.cls != null)
                return false;
            if (this.key != null ? !this.key.equals(cacheKey.key) : cacheKey.key != null)
                return false;
            //noinspection RedundantIfStatement
            if (!Arrays.deepEquals(this.params, cacheKey.params))
                return false;

            return true;
        }

        public int hashCode()
        {
            if (this.hash == 0)
            {
                int result;
                result = (this.cls != null ? cls.hashCode() : 0);
                result = 31 * result + (this.key != null ? this.key.hashCode() : 0);
                result = 31 * result + (this.params != null ? Arrays.deepHashCode(this.params) : 0);
                this.hash = result;
            }
            return this.hash;
        }
    }
    
    protected static final int ELEMENT = 1;
    protected static final int VERTEX  = 2;
    protected static final int NORMAL  = 3;

    private int[] mode;
    private int[] count;
    private int[] size;
    private int[] glType;
    private int[] stride;
    private Buffer[] buffer;

    public Geometry()
    {
        this.mode    = new int[4];
        this.count   = new int[4];
        this.size    = new int[4];
        this.glType  = new int[4];
        this.stride  = new int[4];
        this.buffer  = new Buffer[4];
    }

    public int getMode(int object)
    {
        return this.mode[object];
    }

    public void setMode(int type, int mode)
    {
        this.mode[type] = mode;
    }

    public int getCount(int type)
    {
        return this.count[type];
    }

    public int getSize(int type)
    {
        return this.size[type];
    }

    public int getGLType(int type)
    {
        return this.glType[type];
    }

    public int getStride(int type)
    {
        return this.stride[type];
    }

    public Buffer getBuffer(int type)
    {
        return this.buffer[type];
    }

    public void setData(int type, int size, int glType, int stride, int count, int[] src, int srcPos)
    {
        this.size[type]   = size;
        this.glType[type] = glType;
        this.stride[type] = stride;
        this.count[type]  = count;

        int numCoords = size * count;
        if (   this.buffer[type] == null
            || this.buffer[type].capacity() < numCoords
            || !(this.buffer[type] instanceof IntBuffer))
        {
            this.buffer[type] = BufferUtil.newIntBuffer(numCoords);
        }

        this.bufferCopy(src, srcPos, (IntBuffer) this.buffer[type], 0, numCoords);
    }

    public void setData(int type, int size, int stride, int count, float[] src, int srcPos)
    {
        this.size[type]   = size;
        this.glType[type] = GL.GL_FLOAT;
        this.stride[type] = stride;
        this.count[type]  = count;

        int numCoords = size * count;
        if (   this.buffer[type] == null
            || this.buffer[type].capacity() < numCoords
            || !(this.buffer[type] instanceof FloatBuffer))
        {
            this.buffer[type] = BufferUtil.newFloatBuffer(numCoords);
        }

        this.bufferCopy(src, srcPos, (FloatBuffer) this.buffer[type], 0, numCoords);
    }

    public void setElementData(int mode, int count, int[] src)
    {
        this.setMode(ELEMENT, mode);
        this.setData(ELEMENT, 1, GL.GL_UNSIGNED_INT, 0, count, src, 0);
    }

    public void setVertexData(int count, float[] src)
    {
        this.setData(VERTEX, 3, 0, count, src, 0);
    }

    public void setNormalData(int count, float[] src)
    {
        this.setData(NORMAL, 3, 0, count, src, 0);
    }

    public void clear(int type)
    {
        this.mode[type]    = 0;
        this.count[type]   = 0;
        this.size[type]    = 0;
        this.glType[type]  = 0;
        this.stride[type]  = 0;
        this.buffer[type]  = null;
    }

    public long getSizeInBytes()
    {
        return this.bufferSize(ELEMENT) + this.bufferSize(VERTEX) + this.bufferSize(NORMAL);
    }

    private long bufferSize(int bufferType)
    {
        long size = 0L;
        if (this.buffer[bufferType] != null)
            size = this.sizeOf(this.glType[bufferType]) * this.buffer[bufferType].capacity();
        return size;
    }

    private long sizeOf(int glType)
    {
        long size = 0L;
        switch (glType)
        {
        case GL.GL_BYTE:
            size = 1L;
            break;
        case GL.GL_SHORT:
        case GL.GL_UNSIGNED_SHORT:
            size = 2L;
            break;
        case GL.GL_INT:
        case GL.GL_UNSIGNED_INT:
        case GL.GL_FLOAT:
            size = 4L;
            break;
        case GL.GL_DOUBLE:
            size = 8L;
            break;
        }
        return size;
    }

    private void bufferCopy(int[] src, int srcPos, IntBuffer dest, int destPos, int length)
    {
        dest.position(destPos);
        dest.put(src, srcPos, length);
        dest.position(destPos);
    }

    private void bufferCopy(float[] src, int srcPos, FloatBuffer dest, int destPos, int length)
    {
        dest.position(destPos);
        dest.put(src, srcPos, length);
        dest.position(destPos);
    }
}
