package gov.nasa.worldwind.cache;
/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author Tom Gaskins
 * @version $Id: Cacheable.java 2422 2007-07-25 23:07:49Z tgaskins $
 */
public interface Cacheable
{
    // TODO: search for size queries that do not used this interface and change them to do so.
    // currently (22 Nov 2006), only  BasicElevationModel.addTileToCache(Tile, ShortBuffer) does not use Cacheable

    /**
     * Retrieves the approximate size of this object in bytes. Implementors are encouraged to calculate the exact size
     * for smaller objects, but use approximate values for objects that include such large components that the
     * approximation would produce an error so small that the extra computation would be wasteful.
     *
     * @return this <code>Cacheable</code> object's size in bytes
     */
    long getSizeInBytes();
}
