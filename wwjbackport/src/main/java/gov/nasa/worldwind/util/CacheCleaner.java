/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.WorldWind;

import java.io.File;
import java.util.*;

/**
 * @author Patrick Murris
 * @version $Id: CacheCleaner.java 6812 2008-09-24 20:25:26Z dcollins $
 */
public class CacheCleaner
{
    private static class CacheLocationData
    {
        public File cacheLocation;
        public List<CacheDirectory> directories;
        public long fileCount;
        public long sizeInBytes;

        public CacheLocationData(File location)
        {
            this.cacheLocation = location;
            this.fileCount = 0;
            this.sizeInBytes = 0;
        }
    }

    private static class CacheDirectory  implements Comparable<CacheDirectory>
    {
        public File file;
        public int fileCount;
        public long sizeInBytes;

        public CacheDirectory(File file)
        {
            this.file = file;
            this.fileCount = 0;
            this.sizeInBytes = 0;
        }

        public int compareTo(CacheDirectory that)
        {
            // Oldest first - date ascending
            return (int)(this.file.lastModified() - that.file.lastModified());
        }
    }

    /**
     * Analyse a cache location. Sort out all non empty directories, computes total number of files and total size.
     *
     * @param location the cache location to analyse.
     * @return a <code>CacheLocationData</code> structure holding the necessary information for the cleanup process.
     */
    private static CacheLocationData analyseCacheLocation(File location)
    {
        CacheLocationData cld = new CacheLocationData(location);
        // Examine all location directories for non empty ones
        cld.directories = getDirectories(location);
        // Sum up files and size
        for (CacheDirectory cd : cld.directories)
        {
            //System.out.println(cd.file.getAbsolutePath() + " files: " + cd.fileCount + " size: " + cd.sizeInBytes / 1000 + "Kb " + new Date(cd.file.lastModified()));
            cld.fileCount += cd.fileCount;
            cld.sizeInBytes += cd.sizeInBytes;
        }
        // Sort directories by ascending dates - oldest first
        Collections.sort(cld.directories);
        return cld;
    }

    /**
     * Return the list of all non empty directories contained in a given directory.
     *
     * @param directory the directory to discover
     * @return the list of all non empty directories, including this one if it contains any files.
     */
    private static List<CacheDirectory> getDirectories(File directory)
    {
        LinkedList<CacheDirectory> cacheDirList = new LinkedList<CacheDirectory>();
        if (directory.exists())
        {
            CacheDirectory cd = new CacheDirectory(directory);
            List<CacheDirectory> subDirs = new LinkedList<CacheDirectory>();
            // Check every files in this directory
            File[] files = directory.listFiles();
            for (File f : files)
            {
                if (f.isFile())
                {
                    // Count files and sum up sizes
                    cd.fileCount++;
                    cd.sizeInBytes += f.length();
                }
                else if (f.isDirectory())
                {
                    // Get non empty sub directories
                    subDirs.addAll(getDirectories(f));
                }
            }

            // If this folder contains any files, add it to the list
            if (cd.fileCount > 0)
                cacheDirList.add(cd);

            // If there are any sub directories containing files, add them too
            if (subDirs.size() > 0)
                cacheDirList.addAll(subDirs);
        }
        return cacheDirList;
    }

    /**
     * Delete files and directories until the location size is less or equal to the specified size in bytes.
     *
     * @param location the <code>CacheLocationData</code> object associated with the location to be cleaned.
     * @param lowWaterInBytes the size in bytes the cache location should fit-in after cleanup.
     */
    private static void cleanupCacheLocation(CacheLocationData location, long lowWaterInBytes)
    {
        // Delete directories starting with oldest - assumed sort order
        for (CacheDirectory dir : location.directories)
        {
            // Stop when we reach low water
            if (location.sizeInBytes <= lowWaterInBytes)
                break;

            // Delete all files in directory
            File[] files = dir.file.listFiles();
            for (File f : files)
            {
                long size = f.length();
                if (f.delete())
                {
                    //System.out.println("Deleted " + f);
                    location.fileCount--;
                    location.sizeInBytes -= size;
                }
            }
            // Try removing the directory
            dir.file.delete();

        }

    }

    // Tests
    private static long HIGH_WATER_IN_BYTES = 20000 * (long)Math.pow(2, 20); // Megabytes
    private static long LOW_WATER_IN_BYTES = (long)(HIGH_WATER_IN_BYTES * .8); // 80% of high water

    public static void main(String[] args)
    {
        //List<File> cacheLocations = WorldWind.getDataFileStore().getCacheLocations();

        // Analyse and cleanup (if needed) the write cache location
        File writeCache = WorldWind.getDataFileStore().getWriteLocation();

        // Analyse location
        System.out.println("Analyzing: " + writeCache.getAbsolutePath());
        CacheLocationData cld = analyseCacheLocation(writeCache);
        System.out.println("Size: " + (int)(cld.sizeInBytes / Math.pow(2, 20)) + "Mb in " + cld.fileCount + " file(s) from " + cld.directories.size() + " dir(s)");
        //for (CacheDirectory dir : cld.directories)
        //    System.out.println(new Date(dir.file.lastModified()) + " " + dir.sizeInBytes / 1024 + "Kb " + dir.file.getAbsolutePath());

        // Any cleanup needed?
        if (cld.sizeInBytes >= HIGH_WATER_IN_BYTES)
        {
            System.out.println("Cleaning down to " + (int)(LOW_WATER_IN_BYTES / Math.pow(2, 20)) + "Mb");
            cleanupCacheLocation(cld, LOW_WATER_IN_BYTES);
            System.out.println("Size: " + (int)(cld.sizeInBytes / Math.pow(2, 20)) + "Mb in " + cld.fileCount + " file(s)");
        }
        else
            System.out.println("No cleanup needed until " + (int)(HIGH_WATER_IN_BYTES / Math.pow(2, 20)) + "Mb");
    }
}
