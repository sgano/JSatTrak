/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.cache;

import com.sun.opengl.util.texture.Texture;

/**
 * @author tag
 * @version $Id: TextureCache.java 6911 2008-10-03 19:35:45Z tgaskins $
 */
public interface TextureCache
{
    void put(Object key, Texture texture);

    Texture get(Object key);

    void remove(Object key);

    int getNumObjects();

    long getCapacity();

    long getUsedCapacity();

    long getFreeCapacity();

    boolean contains(Object key);

    void clear();

    void setCapacity(long newCapacity);

    void setLowWater(long loWater);

    long getLowWater();
}
