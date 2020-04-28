/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;

import java.util.*;

/**
 * This class handles the storage and retrieval of geographic objects inside a quadtree.
 *
 * @author Patrick Murris
 * @version $Id: LatLonQuadTree.java 12469 2009-08-17 20:26:19Z patrickmurris $
 */

public class LatLonQuadTree<O>
{
    protected static final Sector DEFAULT_SECTOR = Sector.FULL_SPHERE;
    protected static final int DEFAULT_MAX_LIST_SIZE = 500;
    protected static final int DEFAULT_MAX_LEVELS = 10;

    private final Sector sector;
    private final int maxListSize;
    private final int maxLevels;

    protected List<LatLonEntry<O>> entries;
    protected List<LatLonQuadTree<O>> childs;

    /**
     * A filter for retrieval of objects inside the quadtree
     *<p> Instances of this interface may be passed to the {@link LatLonQuadTree#get(Sector, RetrievalFilter, List)}
     * method of the {@link LatLonQuadTree} class.
     */
    public interface RetrievalFilter<O>
    {
        /**
         * Tests whether or not the specified object should be included in a list of object. For performance reasons,
         * the test should be kept relatively simple.
         *
         * @param object the object to be tested
         * @param latLon the {@link LatLon} where the object is located.
         * @return <code>true</code> if and only if the <code>object</code> should be included.
         */
        boolean accept(LatLon latLon, O object);
    }

    protected class LatLonEntry<O>
    {
        private LatLon latLon;
        private O object;

        public LatLonEntry(LatLon latLon, O object)
        {
            this.latLon = latLon;
            this.object = object;
        }
    }

    /**
     * Define a quadtree for the default {@link Sector} (full sphere), max list size (500) and levels (10).
     */
    public LatLonQuadTree()
    {
        this(DEFAULT_SECTOR, DEFAULT_MAX_LIST_SIZE, DEFAULT_MAX_LEVELS);
    }

    /**
     * Define a quadtree for the given <code>Sector</code>, with the default max list size (500) and levels (10).
     *
     * @param sector the {@link Sector} covered by this quadtree.
     * @throws IllegalArgumentException if <code>sector</code> is <code>null</code>.
     */
    public LatLonQuadTree(Sector sector)
    {
        this(sector, DEFAULT_MAX_LIST_SIZE, DEFAULT_MAX_LEVELS);
    }

    /**
     * Define a quadtree for the given <code>Sector</code>, max list size and levels.
     *
     * @param sector the {@link Sector} covered by this quadtree.
     * @param maxListSize the number of entries for one level before it splits to another level.
     * @param maxLevels the maximum number of levels. Must be greater or equal to one.
     * @throws IllegalArgumentException if <code>sector</code> is <code>null</code> or any of <code>maxListSize</code>
     * or <code>maxLevels</code> is smaller then one.
     */
    public LatLonQuadTree(Sector sector, int maxListSize, int maxLevels)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (maxListSize < 1 || maxLevels < 1)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.sector = sector;
        this.maxListSize = maxListSize;
        this.maxLevels = maxLevels;
    }

    public Sector getSector()
    {
        return this.sector;
    }

    public int getMaxListSize()
    {
        return this.maxListSize;
    }

    public int getMaxLevels()
    {
        return this.maxLevels;
    }

    /**
     * Add an object to the quadtree at a given {@link LatLon}. If the object was succesfully
     * inserted, returns a reference to the quadtree where the insertion occured. Returns <code>null</code>
     * if it failed (coordinates where likely outside of the quadtree sector).
     *
     * @param object the object reference to keep for the given lat-lon coordinates.
     * @param latLon the {@link LatLon} for the object.
     * @return a reference to the {@link LatLonQuadTree} where the object was inserted or <code>null</code> if the
     * insertion failed.
     * @throws IllegalArgumentException if <code>object</code> or <code>latLon</code> is <code>null</code>.
     */
    public LatLonQuadTree add(O object, LatLon latLon)
    {
        if (object == null)
        {
            String msg = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (latLon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return add(new LatLonEntry<O>(latLon, object));
    }

    protected LatLonQuadTree add(LatLonEntry<O> entry)
    {
        if (!this.sector.contains(entry.latLon))
            return null;

        if (this.entries == null)
            this.entries = new ArrayList<LatLonEntry<O>>();

        if (this.childs == null && (this.entries.size() < this.maxListSize || this.maxLevels == 1))
        {
            // Add entry at this level
            this.entries.add(entry);
            return this;
        }

        // This level is full and maxLevels > 1, add to childs
        if (this.childs == null)
            this.split();

        LatLonQuadTree qt;
        for (LatLonQuadTree<O> child : childs)
            if ((qt = child.add(entry)) != null)
                   return qt;

        return null;
    }

    protected void split()
    {
        // Create child quadtrees
        this.childs = new ArrayList<LatLonQuadTree<O>>();
        Sector[] sectors = this.sector.subdivide();
        this.childs.add(new LatLonQuadTree<O>(sectors[0], this.maxListSize, this.maxLevels - 1));
        this.childs.add(new LatLonQuadTree<O>(sectors[1], this.maxListSize, this.maxLevels - 1));
        this.childs.add(new LatLonQuadTree<O>(sectors[2], this.maxListSize, this.maxLevels - 1));
        this.childs.add(new LatLonQuadTree<O>(sectors[3], this.maxListSize, this.maxLevels - 1));

        // Pass entries to the children
        for (LatLonEntry<O> entry : this.entries)
            for (LatLonQuadTree<O> child : childs)
                if (child.add(entry) != null)
                       break;

        // Clear this level entries
        this.entries.clear();
    }

    /**
     * Get a list of objects in a given {@link Sector}.
     *
     * @param getSector the {@link Sector} encompassing the objects.
     * @return the list of objects contained in the given {@link Sector}.
     * @throws IllegalArgumentException if <code>sector</code> is <code>null</code>.
     */
    public List<O> get(Sector getSector)
    {
        return this.get(getSector, null, null);
    }
    
    /**
     * Get a list of objects in a given {@link Sector} that are to be filtered against a {@link RetrievalFilter}.
     *
     * @param getSector the {@link Sector} encompassing the objects.
     * @param filter the {@link RetrievalFilter} to be used - can be null.
     * @param locations a {@link List<LatLon>} where the locations for the returned objects will be added - can be null.
     * @return the list of objects contained in the given <code>Sector</code>.
     * @throws IllegalArgumentException if <code>sector</code> is <code>null</code>.
     */
    public List<O> get(Sector getSector, RetrievalFilter<O> filter, List<LatLon> locations)
    {
        if (getSector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.sector.intersects(getSector))
            return null;

        if (this.entries == null)
            return null;

        List<O> list = new ArrayList<O>();
        List<O> childList;
        if (this.childs != null)
        {
            // Get list from childs
            for (LatLonQuadTree<O> child : childs)
                if ((childList = child.get(getSector, filter, locations)) != null)
                    list.addAll(childList); // note: list has been filtered by child
        }
        else
        {
            // Compose list from this level entries
            if (this.sector.intersection(getSector).equals(this.sector))
            {
                // Whole quadtree is included, add all objects
                for (LatLonEntry<O> entry : this.entries)
                    this.filterAndAddToList(list, locations, filter, entry);
            }
            else
            {
                // Quadtree is not completly included, test individual objects
                for (LatLonEntry<O> entry : this.entries)
                    if (getSector.contains(entry.latLon))
                        this.filterAndAddToList(list, locations, filter, entry);
            }
        }

        return list;
    }

    protected void filterAndAddToList(List<O> list, List<LatLon> locations, RetrievalFilter<O> filter, LatLonEntry<O> entry)
    {
        if (filter == null)
        {
            list.add(entry.object);
            if (locations != null)
                locations.add(entry.latLon);
        }
        else
        {
            if (filter.accept(entry.latLon, entry.object))
            {
                list.add(entry.object);
                if (locations != null)
                    locations.add(entry.latLon);
            }
        }
    }

    /**
     * Remove the given object from the quadtree.
     *
     * @param object the object to remove.
     * @param latLon the {@link LatLon} of the object when it was added.
     * @return a reference to the {@link LatLonQuadTree} where the removal occured or <code>null</code> if the
     * object was not removed.
     * <p>Failure from removal arise if the given coordinates are outside the quadtree sector, or not the
     * ones used when the object was added, or the object was not found at this location.</p>
     * @throws IllegalArgumentException if <code>object</code> or <code>latLon</code> is <code>null</code>.
     */
    public LatLonQuadTree<O> remove(O object, LatLon latLon)
    {
        if (object == null)
        {
            String msg = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (latLon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.sector.contains(latLon))
            return null;

        if (this.entries == null)
            return null;

        // Remove object from childs if any
        LatLonQuadTree<O> qt = null;
        if (this.childs != null)
        {
            for (LatLonQuadTree<O> child : this.childs)
                if ((qt = child.remove(object, latLon)) != null)
                    break;

            if (qt != null && qt.maxLevels == this.maxLevels - 1)
                this.collapseChilds();

            return this.childs != null ? qt : (qt != null ? this : null);
        }

        // Remove object from local list
        for (int i = 0; i < entries.size(); i++)
            if (entries.get(i).object == object)
            {
                entries.remove(i);
                return this;
            }

        return null;
    }

    protected void collapseChilds()
    {
        if (this.childs == null)
            return; // do not collapse if there is no childs

        int numEntries = 0;
        for (LatLonQuadTree<O> child : this.childs)
        {
            if (child.childs != null)
                return; // do not collapse if one child has childs
            numEntries += child.entries != null ? child.entries.size() : 0;
        }

        if (numEntries > this.maxListSize)
            return; // do not collapse if tot entries greater then max list size

        // Bring back child entries to this level
        for (LatLonQuadTree<O> child : this.childs)
            if (child.entries != null)
                this.entries.addAll(child.entries);

        // Clear childs
        this.childs.clear();
        this.childs = null;
    }

    public String toString()
    {
        String s = "LatLonQuadTree: max Levels: " + maxLevels + ", entries: "
                + (entries != null ? entries.size() : "0") + " " + sector;
        if (childs != null)
            for (LatLonQuadTree<O> child : childs)
                s += "\n" + child.toString();
        return s;
    }

    protected class RadiusFilter implements LatLonQuadTree.RetrievalFilter<O>
    {
        private final LatLon center;
        private final double radius;
        private final double globeRadius;

        public RadiusFilter(Globe globe, LatLon center, double radius)
        {
            this.center = center;
            this.radius = radius;
            this.globeRadius = globe.getRadiusAt(this.center);
        }

        public boolean accept(LatLon latLon, O object)
        {
            return LatLon.greatCircleDistance(this.center, latLon).radians * this.globeRadius <= this.radius;
        }
    }

    // Tests
    public static void main(String[] args)
    {
        LatLonQuadTree<Object> qt = new LatLonQuadTree<Object>();
        long startTime, elapsedTime;

        // Test addition
        long numEntries = (long)1e5; // one hundred thousand entries
        System.out.println("Adding " + numEntries + " entries...");
        startTime = System.nanoTime();
        for (int i = 0; i < numEntries; i++)
        {
            LatLon ll = LatLon.fromDegrees(Math.random() * 180 - 90, Math.random() * 360 - 180);
            qt.add(ll, ll);
        }
        elapsedTime = System.nanoTime() - startTime;
        System.out.println("Elapsed: " + elapsedTime / (long)1e3 + " micro sec. (average " + elapsedTime / numEntries / 1e3 + " micro sec.)");

        //System.out.println(qt);    // Dump qt structure

        // Test query
        List<LatLon> locations = new ArrayList<LatLon>();
        Sector getSector = Sector.fromDegrees(0, 15, -100, -80);
        startTime = System.nanoTime();
        List<Object> list = qt.get(getSector, null, locations);
        elapsedTime = System.nanoTime() - startTime;
        if (list != null)
            System.out.println(list.size() + " objects found inside " + getSector + " in " + elapsedTime / (long)1e3 + " micro sec.");
        else
            System.out.println("No object found inside " + getSector);

        // Check whether reported locations are correct
        if (list != null)
        {
        for (int i = 0; i < list.size(); i++)
            if (!list.get(i).equals(locations.get(i)))
                System.out.println("Location error for " + list.get(i));
            System.out.println("Location check done for " + list.size() + " objects.");
        }

        // Test removal
        if (list != null)
        {
            // Remove found objects
            System.out.println("Removing found objects...");
            startTime = System.nanoTime();
            for (Object o : list)
                qt.remove(o, (LatLon)o);
            elapsedTime = System.nanoTime() - startTime;
            System.out.println("...in " + elapsedTime / (long)1e3 + " micro sec.");

            // Query again - should yield zero
            startTime = System.nanoTime();
            list = qt.get(getSector);
            elapsedTime = System.nanoTime() - startTime;
            if (list != null)
                System.out.println(list.size() + " objects found inside " + getSector + " in " + elapsedTime / (long)1e3 + " micro sec.");
            else
                System.out.println("No object found inside " + getSector);
        }


    }

}
