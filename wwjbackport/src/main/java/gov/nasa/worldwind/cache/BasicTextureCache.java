/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.cache;

import com.sun.opengl.util.texture.Texture;

import javax.media.opengl.GLContext;

/**
 * @author tag
 * @version $Id: BasicTextureCache.java 7649 2008-11-15 07:11:30Z tgaskins $
 */
public class BasicTextureCache implements TextureCache
{
    public static class TextureEntry implements Cacheable
    {
        private final Texture texture;

        public TextureEntry(Texture texture)
        {
            this.texture = texture;
        }

        public Texture getTexture()
        {
            return texture;
        }

        public long getSizeInBytes()
        {
            long size = this.texture.getEstimatedMemorySize();

            // JOGL returns a zero estimated memory size for some textures, so calculate a size ourselves.
            if (size < 1)
                size = this.texture.getHeight() * this.texture.getWidth() * 4;

            return size;
        }
    }

    private final BasicMemoryCache textures;

    public BasicTextureCache(long loWater, long hiWater)
    {
        this.textures = new BasicMemoryCache(loWater, hiWater);
        this.textures.setName("Texture Cache");
        this.textures.addCacheListener(new MemoryCache.CacheListener()
        {
            public void entryRemoved(Object key, Object clientObject)
            {
                if (GLContext.getCurrent() == null)
                    return;
                
                // Unbind a tile's texture when the tile leaves the cache.
                if (clientObject != null) // shouldn't be null, but check anyway
                {
                    ((TextureEntry) clientObject).texture.dispose();
                }
            }
        });
    }

    public void put(Object key, Texture texture)
    {
        TextureEntry te = new TextureEntry(texture);
        this.textures.add(key, te);
    }

    public Texture get(Object key)
    {
        TextureEntry entry = (TextureEntry) this.textures.getObject(key);
        return entry != null ? entry.texture : null;
    }

    public void remove(Object key)
    {
        this.textures.remove(key);
    }

    public int getNumObjects()
    {
        return this.textures.getNumObjects();
    }

    public long getCapacity()
    {
        return this.textures.getCapacity();
    }

    public long getUsedCapacity()
    {
        return this.textures.getUsedCapacity();
    }

    public long getFreeCapacity()
    {
        return this.textures.getFreeCapacity();
    }

    public boolean contains(Object key)
    {
        return this.textures.contains(key);
    }

    public void clear()
    {
        this.textures.clear();
    }

    /**
     * Sets the new capacity (in bytes) for the cache. When decreasing cache size, it is recommended to check that the
     * lowWater variable is suitable. If the capacity infringes on items stored in the cache, these items are removed.
     * Setting a new low water is up to the user, that is, it remains unchanged and may be higher than the maximum
     * capacity. When the low water level is higher than or equal to the maximum capacity, it is ignored, which can lead
     * to poor performance when adding entries.
     *
     * @param newCapacity the new capacity of the cache.
     */
    public synchronized void setCapacity(long newCapacity)
    {
        this.textures.setCapacity(newCapacity);
    }

    /**
     * Sets the new low water level in bytes, which controls how aggresively the cache discards items.
     * <p/>
     * When the cache fills, it removes items until it reaches the low water level.
     * <p/>
     * Setting a high loWater level will increase cache misses, but decrease average add time, but setting a low loWater
     * will do the opposite.
     *
     * @param loWater the new low water level in bytes.
     */
    public synchronized void setLowWater(long loWater)
    {
        this.textures.setLowWater(loWater);
    }

    /**
     * Returns the low water level in bytes. When the cache fills, it removes items until it reaches the low water
     * level.
     *
     * @return the low water level in bytes.
     */
    public long getLowWater()
    {
        return this.textures.getLowWater();
    }
}
