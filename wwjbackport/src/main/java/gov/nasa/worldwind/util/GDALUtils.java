/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import org.gdal.gdal.*;
import org.gdal.gdalconst.*;
import org.gdal.ogr.ogr;
import org.gdal.osr.*;

import java.awt.*;
import java.awt.color.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALUtils.java 14222 2010-12-11 00:27:50Z garakl $
 */
public class GDALUtils
{
    public static long ALPHA_MASK = 0xFFFFFFFFL;
    protected static byte ALPHA_TRANSPARENT = (byte) 0x00;
    protected static byte ALPHA_OPAQUE = (byte) 0xFF;

    protected static final LinkedHashMap<String, String> GDAL_LIBS = new LinkedHashMap<String, String>();

    static
    {
        if (Configuration.isWindowsOS())
        {
            GDAL_LIBS.put("proj", "proj.dll");
            GDAL_LIBS.put("gdal", "gdal17.dll");
            GDAL_LIBS.put("ogrjni", "ogrjni.dll");
            GDAL_LIBS.put("osrjni", "osrjni.dll");
            GDAL_LIBS.put("gdaljni", "gdaljni.dll");
            GDAL_LIBS.put("gdalconstjni", "gdalconstjni.dll");
            GDAL_LIBS.put("gdalalljni", "gdalalljni.dll");
        }
        else if (Configuration.isMacOS())
        {
//            GDAL_LIBS.put( "proj", "libproj.dylib" );
            GDAL_LIBS.put("gdalalljni", "libgdalalljni.jnilib");
            GDAL_LIBS.put("gdal", "libgdal.jnilib");
            GDAL_LIBS.put("ogrjni", "libogrjni.jnilib");
            GDAL_LIBS.put("osrjni", "libosrjni.jnilib");
            GDAL_LIBS.put("gdaljni", "libgdaljni.jnilib");
            GDAL_LIBS.put("gdalconstjni", "libgdalconstjni.jnilib");
        }
        else if (Configuration.isUnixOS())  // covers Solaris and Linux
        {
            GDAL_LIBS.put("proj", "libproj.so");
            GDAL_LIBS.put("gdal", "jnilibgdal.so");
            GDAL_LIBS.put("ogrjni", "libogrjni.so");
            GDAL_LIBS.put("osrjni", "libosrjni.so");
            GDAL_LIBS.put("gdaljni", "libgdaljni.so");
            GDAL_LIBS.put("gdalconstjni", "libgdalconstjni.so");
            GDAL_LIBS.put("gdalalljni", "libgdalalljni.so");
        }
    }

    protected static final String JAVA_LIBRARY_PATH = "java.library.path";
    protected static final String GDAL_DRIVER_PATH = "GDAL_DRIVER_PATH";
    protected static final String OGR_DRIVER_PATH = "OGR_DRIVER_PATH";

    protected static final String GDAL_DATA_PATH = "GDAL_DATA";

    protected static final String DATA = "data";
    protected static final String WWJ_LIB_EXTERNAL = "lib-external";
    protected static final String WWJ_LIB_EXTERNAL_GDAL_DATA =
        WWJ_LIB_EXTERNAL + File.separator + "gdal" + File.separator + DATA;

    protected static boolean isGDALLoaded = false;
    protected static boolean initAttempMade = false;
    protected static boolean runningAsJavaWebStart = false;

    protected static Object initLock = new Object();

    protected static class GdalLibrariesFinder implements FileFilter
    {
        private HashSet<String> listFolders = new HashSet<String>();
        private String searchPattern = null;

        public GdalLibrariesFinder(String searchPattern)
        {
            if (null == searchPattern || searchPattern.length() == 0)
            {
                String message = Logging.getMessage("nullValue.StringIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            this.searchPattern = searchPattern;
            Logging.logger().severe("Debug: GdalLibrariesFinder: searching for " + searchPattern);
            listFolders.clear();
        }

        public boolean accept(File pathname)
        {
            String filename = null;
            String dir = null;
            if (null != pathname
                && null != (dir = pathname.getParent())
                && !this.listFolders.contains(dir)                  // skip already discovered
                && !(pathname.getAbsolutePath().contains(".svn"))   // ignore Subversion .svn folders 
                && null != (filename = pathname.getName())          // get folder name
                && null != (filename = filename.toLowerCase())      // change to lower case
                && (filename.contains(this.searchPattern)))
            {
                Logging.logger().info("Added " + dir);
                this.listFolders.add(dir);
                return true;
            }
            Thread.yield();
            return false;
        }

        public String[] getFolders()
        {
            String[] folders = new String[listFolders.size()];
            return this.listFolders.toArray(folders);
        }
    }

    private static class GDALLibraryLoader 
    {
        private static HashSet<String> loadedLibraries = new HashSet<String>();

        public void load(String libName) throws UnsatisfiedLinkError
        {
            if (null == libName || 0 == libName.length())
            {
                String message = Logging.getMessage("nullValue.LibraryIsNull");
                Logging.logger().severe(message);
                throw new java.lang.UnsatisfiedLinkError(message);
            }

            if (loadedLibraries.contains(libName))
            {
                String message = Logging.getMessage("generic.LibraryAlreadyLoaded", libName);
                Logging.logger().finest(message);
                return;
            }

            String libFullName = null;

            // try first to load it by it's short name and let JVM and OS to do it's work
            try
            {
                System.loadLibrary(libName);
                Logging.logger().finest(Logging.getMessage("generic.LibraryLoadedOK", libName));
                loadedLibraries.add(libName);
                return; // GOOD! Leaving now
            }
            catch (java.lang.UnsatisfiedLinkError ule)
            {
                String message = Logging.getMessage("gdal.NativeLibraryNotLoaded", libName, ule.getMessage());
                Logging.logger().severe(message);
                libFullName = GDAL_LIBS.get(libName);
                if (null == libFullName)
                    throw ule;

//                //TODO
//                if( true )
//                    throw ule;
            }

            //  let's get a full name of the lib and try to load by it's full name
            // This may work on some OS like Windows
            try
            {
                String pathToLibs = locateLibraries();
                doLoadLibrary(pathToLibs + File.separator + libFullName);
                Logging.logger().finest(Logging.getMessage("generic.LibraryLoadedOK", libFullName));
                loadedLibraries.add(libName);
                return; // Finally!!! Leaving now
            }
            catch (java.lang.UnsatisfiedLinkError ule)
            {
                String message = Logging.getMessage("gdal.NativeLibraryNotLoaded", libName, ule.getMessage());
                Logging.logger().severe(message);
                throw ule;
            }
        }
    }

    protected static void replaceLibraryLoader()
    {
        try
        {
//            Class gdalClass = ClassLoader.getSystemClassLoader().loadClass("org.gdal.gdal.gdal");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class gdalClass = cl.loadClass("org.gdal.gdal.gdal");

            boolean isKnownBuild = false;
            Method[] methods = gdalClass.getDeclaredMethods();
            for (Method m : methods)
            {
                if ("setLibraryLoader".equals(m.getName()))
                {
                    //damico gdal.setLibraryLoader(new GDALLibraryLoader());
                    Logging.logger().finest(Logging.getMessage("gdal.LibraryLoaderReplacedOK"));
                    isKnownBuild = true;
                    break;
                }
            }

            if (!isKnownBuild)
            {
                String message = Logging.getMessage("gdal.UnknownBuild", gdal.VersionInfo());
                Logging.logger().info(message);
            }
        }
        catch (ClassNotFoundException cnf)
        {
            Logging.logger().severe(cnf.getMessage());
            Logging.logger().info(Logging.getMessage("gdal.GDALNotAvailable"));
        }
        catch (Throwable t)
        {
            Logging.logger().severe(t.getMessage());
            Logging.logger().info(Logging.getMessage("gdal.UnknownBuild", 0));
        }
    }

    public static boolean isGDALAvailable()
    {
        if (isGDALLoaded)
            return true;
        else
        {
            if (initAttempMade)
                return false;
            else
            {
                initialize();
                return isGDALLoaded;
            }
        }
    }

    public static void initialize()
    {
        synchronized (initLock)
        {
            if (isGDALLoaded)
            {
                String message = Logging.getMessage("gdal.NativeLibraryAlreadyLoaded");
                Logging.logger().finest(message);
                return;
            }

            if (initAttempMade)
            {
                String message = Logging.getMessage("gdal.GDALNotAvailable");
                Logging.logger().finest(message);
                return;
            }

            initAttempMade = true;

            runningAsJavaWebStart = (null != System.getProperty("javawebstart.version", null));

            try
            {
                replaceLibraryLoader(); // This must be the first line of initialization
                doInitialize();
                isGDALLoaded = true;
            }
            catch (java.lang.UnsatisfiedLinkError ule)
            {
                isGDALLoaded = false;
                String message = Logging.getMessage("gdal.NativeLibraryNotLoaded", "GDAL", ule.getMessage());
                Logging.logger().log(Level.SEVERE, message, ule);
            }
            catch (WWRuntimeException wwre)
            {
                isGDALLoaded = false;
            }
            catch (java.lang.Throwable t)
            {
                isGDALLoaded = false;
                String message = Logging.getMessage("gdal.NativeLibraryNotLoaded", "GDAL", t.getMessage());
                Logging.logger().log(Level.SEVERE, message, t);
            }
        }
    }

    protected static void doLoadLibrary(String libPath) throws UnsatisfiedLinkError
    {
        if (null == libPath)
        {
            String message = Logging.getMessage("nullValue.PathIsNull", libPath);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File libFile = new File(libPath);
        if (!libFile.exists())
        {
            String message = Logging.getMessage("generic.FileDoesNotExists", libPath);
            Logging.logger().severe(message);
        }
        else
        {
            System.load(libPath);
            String message = Logging.getMessage("generic.LibraryLoadedOK", libPath);
            Logging.logger().info(message);
        }
    }

    protected static void loadGDALLibraries(String pathToLibs)
    {
        String[] libs = (String[]) (GDAL_LIBS.values().toArray(new String[0]));

        String libName = null;
        String libPath = null;
        for (String lib : libs)
        {
            try
            {
//                String libName = System.mapLibraryName( "gdal" );
//                Logging.logger().info( lib + "----->" + libName );
                libName = lib;
                libPath = pathToLibs + File.separator + libName;
                doLoadLibrary(libPath);
            }
            catch (Throwable ex)
            {
                String message = Logging.getMessage("gdal.NativeLibraryNotLoaded", libName, libPath);
                Logging.logger().log(Level.SEVERE, message, ex);
            }
        }
    }

    protected static void doInitialize() throws java.lang.UnsatisfiedLinkError, Throwable
    {
//        String pathToLibs = locateLibraries();
//        String pathToData = locateSharedData();
//
        try
        {
            String newJavaLibraryPath = null;

            if (gdalJNI.isAvailable() && gdalconstJNI.isAvailable())
            {
                String msg = Logging.getMessage("generic.LibraryLoadedOK", "GDAL v" + gdal.VersionInfo("RELEASE_NAME"));
                Logging.logger().info(msg);
            }
            else if (runningAsJavaWebStart)
            {
//                ClassLoader cl = Thread.currentThread().getContextClassLoader();
//                if( cl != null && cl instanceof JNLPClassLoader )
//                {
//                    JNLPClassLoader jnlpCL = (JNLPClassLoader) cl;
//                }
            }
            else
            {
                String[] folders = findGdalFolders();
                newJavaLibraryPath = buildPathString(folders, true);
            }

//            // create a new java library path by adding a current directory,
//            // user directory, and whatever already is in the "java.library.path"
//            StringBuffer path = new StringBuffer();
//            path.append(pathToLibs).append(del);
//            path.append(".").append(del);
//            path.append(System.getProperty("user.dir")).append(del);
//            path.append(System.getProperty(JAVA_LIBRARY_PATH));
//
//            // Change the value and load the library.
//            String newJavaLibraryPath = path.toString();
//
            if (newJavaLibraryPath != null)
            {
                alterJavaLibraryPath(newJavaLibraryPath);
                Logging.logger().info("newJavaLibraryPath=" + newJavaLibraryPath);
            }

//            if( Configuration.isWindowsOS() )
//            {
//                // the trick to preload GDAL libraries works only in Windows
//                loadGDALLibraries( pathToLibs );
//            }

            if (gdalJNI.isAvailable() && gdalconstJNI.isAvailable())
            {
                if (!runningAsJavaWebStart)
                {
                    //              No need, because we are build one dynamic library that contains ALL  drivers and dependant libraries
                    //                gdal.SetConfigOption(GDAL_DRIVER_PATH, pathToLibs);
                    //                gdal.SetConfigOption(OGR_DRIVER_PATH, pathToLibs);
                    String dataFolder = findGdalDataFolder();
                    if (null != dataFolder)
                    {
                        String msg = Logging.getMessage("gdal.SharedDataFolderFound", dataFolder);
                        Logging.logger().fine(msg);
                        gdal.SetConfigOption(GDAL_DATA_PATH, dataFolder);
                    }
                }

                gdal.AllRegister();
                ogr.RegisterAll();

                /**
                 *  "VERSION_NUM": Returns GDAL_VERSION_NUM formatted as a string.  ie. "1170"
                 *  "RELEASE_DATE": Returns GDAL_RELEASE_DATE formatted as a string. "20020416"
                 *  "RELEASE_NAME": Returns the GDAL_RELEASE_NAME. ie. "1.1.7"
                 *   "--version": Returns full version , ie. "GDAL 1.1.7, released 2002/04/16"
                 */
                String msg = Logging.getMessage("generic.LibraryLoadedOK", "GDAL v" + gdal.VersionInfo("RELEASE_NAME"));
                Logging.logger().fine(msg);
                listAllRegisteredDrivers();
            }
            else
            {
                String message = Logging.getMessage("gdal.GDALNotAvailable");
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }
        }
        catch (UnsatisfiedLinkError ule)
        {
            throw ule;
        }
        finally
        {
//            restoreJavaLibraryPath();
        }
    }

    protected static String getCurrentDirectory()
    {
        String cwd = System.getProperty("user.dir");

        if (null == cwd || cwd.length() == 0)
        {
            String message = Logging.getMessage("generic.UsersHomeDirectoryNotKnown");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        return cwd;
    }

    protected static String[] findGdalFolders()
    {
        try
        {
            String cwd = getCurrentDirectory();

            FileTree fileTree = new FileTree(new File(cwd));
            fileTree.setMode(FileTree.FILES_AND_DIRECTORIES);

            GdalLibrariesFinder filter = new GdalLibrariesFinder("gdal");
            fileTree.asList(filter);
            return filter.getFolders();
        }
        catch (Throwable t)
        {
            Logging.logger().severe(t.getMessage());
        }
        return null;
    }

    protected static String findGdalDataFolder()
    {
        try
        {
            String cwd = getCurrentDirectory();

            FileTree fileTree = new FileTree(new File(cwd));
            fileTree.setMode(FileTree.FILES_AND_DIRECTORIES);

            GdalLibrariesFinder filter = new GdalLibrariesFinder("gdal_datum.csv");
            fileTree.asList(filter);
            String[] folders = filter.getFolders();

            if (null != folders && folders.length > 0)
            {
                if (folders.length > 1)
                {
                    String msg = Logging.getMessage("gdal.MultipleDataFoldersFound", buildPathString(folders, false));
                    Logging.logger().severe(msg);
                }
                return folders[0];
            }
        }
        catch (Throwable t)
        {
            Logging.logger().severe(t.getMessage());
        }

        String message = Logging.getMessage("gdal.SharedDataFolderNotFound");
        Logging.logger().severe(message);
        // throw new WWRuntimeException( message );
        return null;
    }

    protected static String buildPathString(String[] folders, boolean addDefaultValues)
    {
        String del = System.getProperty("path.separator");
        StringBuffer path = new StringBuffer();

        if (null != folders && folders.length > 0)
        {
            for (String folder : folders)
            {
                path.append(folder).append(del);
            }
        }
        if (addDefaultValues)
        {
            path.append(".").append(del); // append current directory
            path.append(System.getProperty("user.dir")).append(del);
            path.append(System.getProperty(JAVA_LIBRARY_PATH));
        }

        return path.toString();
    }

    protected static String locateLibraries()
    {
        String cwd = System.getProperty("user.dir");

        if (null == cwd || cwd.length() == 0)
        {
            String message = Logging.getMessage("generic.UsersHomeDirectoryNotKnown");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        StringBuffer sb = new StringBuffer(cwd);

        if (new File(cwd + File.separator + WWJ_LIB_EXTERNAL).exists())
        {

            // example (for 64bit Windows OS):
            // if cwd = trunk\WorldWindJ, libs are in the trunk\WorldWindJ\lib-external\gdal\win64
            sb.append(File.separator).append(WWJ_LIB_EXTERNAL).append(File.separator).append("gdal").append(
                File.separator);

            if (gov.nasa.worldwind.Configuration.isWindowsOS())
            {
                sb.append("win").append(System.getProperty("sun.arch.data.model"));
            }
            else if (gov.nasa.worldwind.Configuration.isMacOS())
            {
                sb.append("macosx");
            }
            else
            {
                String os = System.getProperty("os.name") + System.getProperty("sun.arch.data.model");
                String message = Logging.getMessage("gdal.UnsupportedOS", os);
                throw new WWRuntimeException(message);
            }
        }

        return sb.toString();
    }

    protected static String locateSharedData()
    {
        String cwd = System.getProperty("user.dir");

        if (null == cwd || cwd.length() == 0)
        {
            String message = Logging.getMessage("generic.UsersHomeDirectoryNotKnown");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        StringBuffer sb = new StringBuffer(cwd);

        if (new File(cwd + File.separator + WWJ_LIB_EXTERNAL).exists())
        {
            // shared data (projection tables) are in the ...\WorldWindJ\lib-external\gdal\data
            sb.append(File.separator).append(WWJ_LIB_EXTERNAL_GDAL_DATA);
        }
        else if (new File(cwd + File.separator + DATA).exists())
        {
            // shared data (projection tables) are in the ...\WorldWindJ\lib-external\gdal\data
            sb.append(File.separator).append(DATA);
        }
        else
        {
            String message = Logging.getMessage("gdal.SharedDataFolderNotFound");
            Logging.logger().finest(message);
        }
        return sb.toString();
    }

    protected static void listAllRegisteredDrivers()
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < gdal.GetDriverCount(); i++)
        {
            Driver drv = gdal.GetDriver(i);
            String msg = Logging.getMessage("gdal.DriverDetails", drv.getShortName(), drv.getLongName(),
                drv.GetDescription());
            sb.append(msg).append("\n");
        }
        Logging.logger().finest(sb.toString());
    }

    /** @return returns an error string, if no errors returns null */
    public static String getErrorMessage()
    {
        try
        {
            if (gdalJNI.isAvailable())
            {
                int errno = gdal.GetLastErrorNo();
                if (0 != errno)
                {
                    String message = Logging.getMessage("gdal.InternalError", errno, gdal.GetLastErrorMsg());
                    Logging.logger().severe(message);
                    return message;
                }
            }
        }
        catch (Throwable t)
        {
            return t.getMessage();
        }
        return null;
    }

    /**
     * Opens image or elevation file, returns a DataSet object
     *
     * @param f A pointer to File
     *
     * @return returns a Dataset object
     *
     * @throws FileNotFoundException    if file not found
     * @throws IllegalArgumentException if file is null
     * @throws SecurityException        if file could not be read
     * @throws WWRuntimeException       if GDAL library was not initialized
     */
    public static Dataset open(File f)
        throws FileNotFoundException, IllegalArgumentException, SecurityException, WWRuntimeException
    {
        if (!isGDALLoaded)
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        if (null == f)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!f.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", f.getAbsolutePath());
            Logging.logger().severe(message);
            throw new FileNotFoundException(message);
        }

        if (!f.canRead())
        {
            String message = Logging.getMessage("generic.FileNoReadPermission", f.getAbsolutePath());
            Logging.logger().severe(message);
            throw new SecurityException(message);
        }

        Dataset ds = gdal.Open(f.getAbsolutePath(), gdalconst.GA_ReadOnly);
        if (ds == null)
        {
            String reason = f.getAbsolutePath() + " : " + GDALUtils.getErrorMessage();
            String message = Logging.getMessage("generic.CannotOpenFile", reason);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        return ds;
    }

    public static boolean canOpen(File f)
    {
        Dataset ds = null;
        boolean canOpen = false;

        try
        {
            if (isGDALLoaded && null != f && f.exists() && f.canRead())
            {
                ds = gdal.Open(f.getAbsolutePath(), gdalconst.GA_ReadOnly);
                canOpen = !(ds == null);
            }
        }
        catch (Throwable t)
        {
        }
        finally
        {
            if (null != ds)
                ds.delete();
        }
        return canOpen;
    }

    /**
     * Opens image or elevation file, returns as a BufferedImage (even for elevations)
     *
     * @param ds GDAL's Dataset object
     *
     * @return returns as a BufferedImage (even for elevations)
     *
     * @throws FileNotFoundException    if file not found
     * @throws IllegalArgumentException if file is null
     * @throws SecurityException        if file could not be read
     * @throws WWRuntimeException       if GDAL library was not initialized
     */
    protected static DataRaster composeImageDataRaster(Dataset ds, AVList params)
        throws IllegalArgumentException, SecurityException, WWRuntimeException
    {
        if (!isGDALLoaded)
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BufferedImage img = null;

        int width = ds.getRasterXSize();
        int height = ds.getRasterYSize();
        int bandCount = ds.getRasterCount();

        Double[] dbls = new Double[16];

        ByteBuffer[] bands = new ByteBuffer[bandCount];
        int[] bandsOrder = new int[bandCount];
        int[] offsets = new int[bandCount];

        int imgSize = width * height;
        int bandDataType = 0, buf_size = 0;

        double maxValue = -Double.MAX_VALUE;

        for (int bandIdx = 0; bandIdx < bandCount; bandIdx++)
        {
            /* Bands are not 0-base indexed, so we must add 1 */
            Band imageBand = ds.GetRasterBand(bandIdx + 1);

            if (null == imageBand)
            {
                String message = Logging.getMessage("nullValue.RasterBandIsNull`");
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            bandDataType = imageBand.getDataType();
            buf_size = imgSize * (gdal.GetDataTypeSize(bandDataType) / 8);

            ByteBuffer data = ByteBuffer.allocateDirect(buf_size);
            data.order(ByteOrder.nativeOrder());

            int colorInt = imageBand.GetRasterColorInterpretation();

            if (params.hasKey(AVKey.RASTER_BAND_MAX_PIXEL_VALUE))
            {
                maxValue = (Double) params.getValue(AVKey.RASTER_BAND_MAX_PIXEL_VALUE);
            }
            else if ((bandDataType == gdalconstConstants.GDT_UInt16 || bandDataType == gdalconstConstants.GDT_UInt32)
                && colorInt != gdalconst.GCI_AlphaBand && colorInt != gdalconst.GCI_Undefined)
            {
                imageBand.GetMaximum(dbls);
                if (dbls[0] == null)
                {
                    double[] minmax = new double[2];
                    imageBand.ComputeRasterMinMax(minmax);
                    maxValue = (minmax[1] > maxValue) ? minmax[1] : maxValue;
                }
                else
                    maxValue = (dbls[0] > maxValue) ? dbls[0] : maxValue;
            }

            int returnVal = imageBand.ReadRaster_Direct(0, 0, imageBand.getXSize(),
                imageBand.getYSize(), width, height, bandDataType, data);

            if (returnVal != gdalconstConstants.CE_None)
                throw new WWRuntimeException(GDALUtils.getErrorMessage());

            int destBandIdx = bandIdx;

            if (colorInt == gdalconst.GCI_RedBand)
                destBandIdx = 0;
            else if (colorInt == gdalconst.GCI_GreenBand)
                destBandIdx = 1;
            else if (colorInt == gdalconst.GCI_BlueBand)
                destBandIdx = 2;

            bands[destBandIdx] = data;
            bandsOrder[destBandIdx] = destBandIdx;
            offsets[destBandIdx] = 0;
        }

        int bitsPerColor = gdal.GetDataTypeSize(bandDataType);

        int actualBitsPerColor = bitsPerColor;

        if (params.hasKey(AVKey.RASTER_BAND_ACTUAL_BITS_PER_PIXEL))
            actualBitsPerColor = (Integer) params.getValue(AVKey.RASTER_BAND_ACTUAL_BITS_PER_PIXEL);
        else if (maxValue > 0d)
            actualBitsPerColor = (int) Math.ceil(Math.log(maxValue) / Math.log(2d));
        else
            actualBitsPerColor = bitsPerColor;

        int[] reqBandOrder = bandsOrder;
        try
        {
            reqBandOrder = extractBandOrder(ds, params);
            if (null == reqBandOrder || 0 == reqBandOrder.length)
                reqBandOrder = bandsOrder;
            else
            {
                offsets = new int[reqBandOrder.length];
                bandsOrder = new int[reqBandOrder.length];
                for (int i = 0; i < reqBandOrder.length; i++)
                {
                    bandsOrder[i] = i;
                    offsets[i] = 0;
                }
            }
        }
        catch (Exception e)
        {
            reqBandOrder = bandsOrder;
            Logging.logger().severe(e.getMessage());
        }

        DataBuffer imgBuffer = null;
        int bufferType = 0;

        // A typical sample RGB:
        //  bitsPerSample is 24=3x8, bitsPerColor { 8,8,8 }, SignificantBitsPerColor {8,8,8}, byteOffsets {2, 1, 0}

        // A typical sample RGBA:
        //  bitsPerSample is 32=4x8, bitsPerColor { 8,8,8,8 }, SignificantBitsPerColor {8,8,8,8}, byteOffsets { 3, 2, 1, 0}

        // A typical Aerial Photo Image RGB
        //  (16 bits per each color, significant bits per color vary from 9bits, 10bits, 11bits, and 12bits
        //  bitsPerSample is 48=3x16, bitsPerColor { 16,16,16 }, SignificantBitsPerColor { 11,11,11 }, byteOffsets {  4, 2, 0}

        // A typical Aerial Photo Image RGBA
        //  (16 bits per each color, significant bits per color vary from 9bits, 10bits, 11bits, and 12bits
        //  bitsPerSample is 64=4x16, bitsPerColor { 16,16,16,16 }, SignificantBitsPerColor { 12,12,12,12 }, byteOffsets {  6, 4, 2, 0 }

        int reqBandCount = reqBandOrder.length;
        boolean hasAlpha = (reqBandCount == 2) || (reqBandCount == 4);

        IntBuffer imageMask = null;
        if (hasAlpha && params.hasKey(AVKey.GDAL_MASK_DATASET))
            imageMask = extractImageMask(params);

        if (bandDataType == gdalconstConstants.GDT_Byte)
        {
            byte[][] int8 = new byte[reqBandCount][];
            for (int i = 0; i < reqBandCount; i++)
            {
                int srcBandIndex = reqBandOrder[i];
                int8[i] = new byte[imgSize];
                bands[srcBandIndex].get(int8[i]);
            }

            if (hasAlpha && null != imageMask)
                applyImageMask(int8[reqBandCount - 1], imageMask);

            imgBuffer = new DataBufferByte(int8, imgSize);

            bufferType = DataBuffer.TYPE_BYTE;
        }
        else if (bandDataType == gdalconstConstants.GDT_Int16)
        {
            short[][] int16 = new short[reqBandCount][];
            for (int i = 0; i < reqBandCount; i++)
            {
                int srcBandIndex = reqBandOrder[i];
                int16[i] = new short[imgSize];
                bands[srcBandIndex].asShortBuffer().get(int16[i]);
            }

            if (hasAlpha && null != imageMask)
                applyImageMask(int16[reqBandCount - 1], imageMask);

            imgBuffer = new DataBufferShort(int16, imgSize);
            bufferType = DataBuffer.TYPE_SHORT;
        }
        else if (bandDataType == gdalconstConstants.GDT_Int32 || bandDataType == gdalconstConstants.GDT_UInt32)
        {
            int[][] uint32 = new int[reqBandCount][];
            for (int i = 0; i < reqBandCount; i++)
            {
                int srcBandIndex = reqBandOrder[i];
                uint32[i] = new int[imgSize];
                bands[srcBandIndex].asIntBuffer().get(uint32[i]);
            }
            if (hasAlpha && null != imageMask)
                applyImageMask(uint32[reqBandCount - 1], imageMask);

            imgBuffer = new DataBufferInt(uint32, imgSize);
            bufferType = DataBuffer.TYPE_INT;
        }
        else if (bandDataType == gdalconstConstants.GDT_UInt16)
        {

            short[][] uint16 = new short[reqBandCount][];
            for (int i = 0; i < reqBandCount; i++)
            {
                int srcBandIndex = reqBandOrder[i];
                uint16[i] = new short[imgSize];
                bands[srcBandIndex].asShortBuffer().get(uint16[i]);
            }
            if (hasAlpha && null != imageMask)
                applyImageMask(uint16[reqBandCount - 1], imageMask);

            imgBuffer = new DataBufferUShort(uint16, imgSize);
            bufferType = DataBuffer.TYPE_USHORT;
        }
        else
        {
            String message = Logging.getMessage("generic.UnrecognizedDataType", bandDataType);
            Logging.logger().severe(message);
        }

        SampleModel sm = new BandedSampleModel(bufferType, width, height, width, bandsOrder, offsets);
        WritableRaster raster = Raster.createWritableRaster(sm, imgBuffer, null);
        ColorModel cm = null;

        Band band1 = ds.GetRasterBand(1);
        if (band1.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex)
        {
            cm = band1.GetRasterColorTable().getIndexColorModel(gdal.GetDataTypeSize(bandDataType));
            img = new BufferedImage(cm, raster, false, null);
        }
        else
        {
            // Determine the color space.
            int transparency = hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
            int baseColorSpace = (reqBandCount > 2) ? ColorSpace.CS_sRGB : ColorSpace.CS_GRAY;
            ColorSpace cs = ColorSpace.getInstance(baseColorSpace);

            int[] nBits = new int[reqBandCount];
            for (int i = 0; i < reqBandCount; i++)
            {
                nBits[i] = actualBitsPerColor;
            }

            cm = new ComponentColorModel(cs, nBits, hasAlpha, false, transparency, bufferType);
            img = new BufferedImage(cm, raster, false, null);
        }

        // TODO garakl read parameter from a configuration file 
        if (null != img)
            img = ImageUtil.bucketFill(img);

        return BufferedImageRaster.wrap(img, params);
    }

    protected static void applyImageMask(byte[] alphaBand, IntBuffer maskBand)
    {
        if (null == alphaBand || null == maskBand || alphaBand.length != maskBand.capacity())
            return;

        int size = alphaBand.length;

        maskBand.rewind();
        for (int i = 0; i < size; i++)
        {
            long pixel = ALPHA_MASK & maskBand.get();
            if (pixel == ALPHA_MASK)
                alphaBand[i] = ALPHA_TRANSPARENT;
        }
        maskBand.rewind();
    }

    protected static void applyImageMask(short[] alphaBand, IntBuffer maskBand)
    {
        if (null == alphaBand || null == maskBand || alphaBand.length != maskBand.capacity())
            return;

        int size = alphaBand.length;

        maskBand.rewind();
        for (int i = 0; i < size; i++)
        {
            long pixel = ALPHA_MASK & maskBand.get();
            if (pixel == ALPHA_MASK)
                alphaBand[i] = ALPHA_TRANSPARENT;
        }
        maskBand.rewind();
    }

    protected static void applyImageMask(int[] alphaBand, IntBuffer maskBand)
    {
        if (null == alphaBand || null == maskBand || alphaBand.length != maskBand.capacity())
            return;

        int size = alphaBand.length;

        maskBand.rewind();
        for (int i = 0; i < size; i++)
        {
            long pixel = ALPHA_MASK & maskBand.get();
            if (pixel == ALPHA_MASK)
                alphaBand[i] = ALPHA_TRANSPARENT;
        }
        maskBand.rewind();
    }

    protected static IntBuffer extractImageMask(AVList params)
    {
        if (null == params || !params.hasKey(AVKey.GDAL_MASK_DATASET))
            return null;

        try
        {
            Object o = params.getValue(AVKey.GDAL_MASK_DATASET);
            if (o instanceof Dataset)
            {
                Dataset maskDS = (Dataset) o;

                Band maskBand = maskDS.GetRasterBand(1);
                if (null == maskBand)
                {
                    String message = Logging.getMessage("nullValue.RasterBandIsNull");
                    Logging.logger().severe(message);
                    return null;
                }

                int width = maskDS.getRasterXSize();
                int height = maskDS.getRasterYSize();

                int maskBandDataType = maskBand.getDataType();
                int maskDataSize = width * height * (gdal.GetDataTypeSize(maskBandDataType) / 8);

                ByteBuffer maskData = ByteBuffer.allocateDirect(maskDataSize);
                maskData.order(ByteOrder.nativeOrder());

                int returnVal = maskBand.ReadRaster_Direct(0, 0, maskBand.getXSize(),
                    maskBand.getYSize(), width, height, maskBandDataType, maskData);

                if (returnVal != gdalconstConstants.CE_None)
                    throw new WWRuntimeException(GDALUtils.getErrorMessage());

                return maskData.asIntBuffer();
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Calculates geo-transform matrix for a north-up raster
     *
     * @param sector
     * @param width  none-zero width of a raster
     * @param height none-zero height of a raster
     *
     * @return IllegalArgumentException if sector is null, or raster size is zero
     */
    public static double[] calcGetGeoTransform(Sector sector, int width, int height) throws IllegalArgumentException
    {
        if (null == sector)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (0 == width)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (0 == height)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

//        * geotransform[1] : width of pixel
//        * geotransform[4] : rotational coefficient, zero for north up images.
//        * geotransform[2] : rotational coefficient, zero for north up images.
//        * geotransform[5] : height of pixel (but negative)
//        * geotransform[0] + 0.5 * geotransform[1] + 0.5 * geotransform[2] : x offset to center of top left pixel.
//        * geotransform[3] + 0.5 * geotransform[4] + 0.5 * geotransform[5] : y offset to center of top left pixel.

        double[] gx = new double[6];

        gx[GDAL.GT_0_ORIGIN_LON] = sector.getMinLongitude().degrees;
        gx[GDAL.GT_1_PIXEL_WIDTH] = Math.abs(sector.getDeltaLonDegrees() / (double) width);
        gx[GDAL.GT_2_ROTATION_X] = 0d;
        gx[GDAL.GT_3_ORIGIN_LAT] = sector.getMaxLatitude().degrees;
        gx[GDAL.GT_4_ROTATION_Y] = 0d;
        gx[GDAL.GT_5_PIXEL_HEIGHT] = -Math.abs(sector.getDeltaLatDegrees() / (double) height);

//      correct for center of pixel vs. top left of pixel

//      GeoTransform[0] -= 0.5 * GeoTransform[1];
//      GeoTransform[0] -= 0.5 * GeoTransform[2];
//      GeoTransform[3] -= 0.5 * GeoTransform[4];
//      GeoTransform[3] -= 0.5 * GeoTransform[5];

        return gx;
    }

    public static SpatialReference createGeographicSRS() throws WWRuntimeException
    {
        if (!isGDALLoaded)
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        SpatialReference srs = new SpatialReference();
        srs.ImportFromProj4("+proj=latlong +datum=WGS84 +no_defs");
        return srs;
    }

    protected static LatLon getLatLonForRasterPoint(double[] gt, int x, int y, CoordinateTransformation ct)
    {
        if (!isGDALLoaded)
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        java.awt.geom.Point2D geoPoint = GDAL.getGeoPointForRasterPoint(gt, x, y);
        if (null == geoPoint)
            return null;

        double[] latlon = ct.TransformPoint(geoPoint.getX(), geoPoint.getY());
        return LatLon.fromDegrees(latlon[1] /* latitude */, latlon[0] /* longitude */);
    }

    public static AVList extractRasterParameters(Dataset ds) throws IllegalArgumentException, WWRuntimeException
    {
        return extractRasterParameters(ds, null);
    }

    /**
     * The extractRasterParameters() sets next key/value pairs:
     * <p/>
     * AVKey.WIDTH - the maximum width of the image
     * <p/>
     * AVKey.HEIGHT - the maximum height of the image
     * <p/>
     * AVKey.COORDINATE_SYSTEM - one of the next values: AVKey.COORDINATE_SYSTEM_SCREEN
     * AVKey.COORDINATE_SYSTEM_GEOGRAPHIC AVKey.COORDINATE_SYSTEM_PROJECTED
     * <p/>
     * AVKey.SECTOR - in case of Geographic CS, contains a regular Geographic Sector defined by lat/lon coordinates of
     * corners in case of Projected CS, contains a bounding box of the area
     * <p/>
     * AVKey.COORDINATE_SYSTEM_NAME
     * <p/>
     * <p/>
     * AVKey.PIXEL_WIDTH (Double) pixel size, UTM images usually specify 1 (1 meter); if missing and Geographic
     * Coordinate System is specified will be calculated as LongitudeDelta/WIDTH
     * <p/>
     * AVKey.PIXEL_HEIGHT (Double) pixel size, UTM images usually specify 1 (1 meter); if missing and Geographic
     * Coordinate System is specified will be calculated as LatitudeDelta/HEIGHT
     * <p/>
     * AVKey.ORIGIN (LatLon) specifies coordinate of the image's origin (one of the corners, or center) If missing,
     * upper left corner will be set as origin
     * <p/>
     * AVKey.DATE_TIME (0 terminated String, length == 20) if missing, current date & time will be used
     * <p/>
     * AVKey.PIXEL_FORMAT required (valid values: AVKey.ELEVATION | AVKey.IMAGE } specifies weather it is a digital
     * elevation model or image
     * <p/>
     * AVKey.RASTER_TYPE required (valid values: AVKey.RASTER_TYPE_ELEVATION, AVKey.RASTER_TYPE_COLOR_IMAGE, or
     * AVKey.RASTER_TYPE_MONOCHROME_IMAGE)
     * <p/>
     * AVKey.DATA_TYPE required ( valid values: AVKey.INT16, and AVKey.FLOAT32 )
     * <p/>
     * AVKey.VERSION optional, if missing a default will be used "NASA World Wind"
     * <p/>
     * AVKey.DISPLAY_NAME, (String) optional, specifies a name of the document/image
     * <p/>
     * AVKey.DESCRIPTION (String) optional, for any kind of descriptions
     * <p/>
     * AVKey.MISSING_DATA_SIGNAL optional, set the AVKey.MISSING_DATA_SIGNAL ONLY if you know for sure that the
     * specified value actually represents void (NODATA) areas. Elevation data usually has "-32767" (like DTED), or
     * "-32768" like SRTM, but some has "0" (mostly images) and "-9999" like NED. Note! Setting "-9999" is very ambiguos
     * because -9999 for elevation is valid value;
     * <p/>
     * AVKey.MISSING_DATA_REPLACEMENT (String type forced by spec) Most images have "NODATA" as "0", elevations have as
     * "-9999", or "-32768" (sometimes "-32767")
     * <p/>
     * AVKey.COORDINATE_SYSTEM required, valid values AVKey.COORDINATE_SYSTEM_GEOGRAPHIC or
     * AVKey.COORDINATE_SYSTEM_PROJECTED
     * <p/>
     * AVKey.COORDINATE_SYSTEM_NAME Optional, A name of the Coordinates System as a String
     * <p/>
     * AVKey.PROJECTION_EPSG_CODE Required; Integer; EPSG code or Projection Code If CS is Geodetic and EPSG code is not
     * specified, a default WGS84 (4326) will be used
     * <p/>
     * AVKey.PROJECTION_DATUM  Optional, AVKey.PROJECTION_DESC   Optional, AVKey.PROJECTION_NAME   Optional,
     * AVKey.PROJECTION_UNITS  Optional,
     * <p/>
     * AVKey.ELEVATION_UNIT Required, if AVKey.PIXEL_FORMAT = AVKey.ELEVATION, value: AVKey.ELEVATION_UNIT_FEET or
     * AVKey.ELEVATION_UNIT_METER (default, if not specified)
     * <p/>
     * AVKey.RASTER_PIXEL, optional, values: AVKey.RASTER_PIXEL_IS_AREA or AVKey.RASTER_PIXEL_IS_POINT if not specified,
     * default for images is RASTER_PIXEL_IS_AREA, and AVKey.RASTER_PIXEL_IS_POINT for elevations
     */
    public static AVList extractRasterParameters(Dataset ds, AVList params)
        throws IllegalArgumentException, WWRuntimeException
    {
        if (null == params)
            params = new AVListImpl();

        if (!isGDALLoaded)
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int width = ds.getRasterXSize();
        if (0 >= width)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        params.setValue(AVKey.WIDTH, width);

        int height = ds.getRasterYSize();
        if (0 >= height)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        params.setValue(AVKey.HEIGHT, height);

        int bandCount = ds.getRasterCount();
        if (0 >= bandCount)
        {
            String message = Logging.getMessage("generic.UnexpectedBandCount", bandCount);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        params.setValue(AVKey.NUM_BANDS, bandCount);

        Band band = ds.GetRasterBand(1);
        if (null != band)
        {
            if (band.GetOverviewCount() > 0)
                params.setValue(AVKey.RASTER_HAS_OVERVIEWS, Boolean.TRUE);

            int dataType = band.getDataType();

            int bpp = gdal.GetDataTypeSize(dataType);
            params.setValue(AVKey.RASTER_BAND_BITS_PER_PIXEL, bpp);

            if (dataType == gdalconst.GDT_Int16 || dataType == gdalconst.GDT_CInt16)
            {
                params.setValue(AVKey.RASTER_TYPE, AVKey.RASTER_TYPE_ELEVATION);
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT16);
            }
            else if (dataType == gdalconst.GDT_Int32 || dataType == gdalconst.GDT_CInt32)
            {
                params.setValue(AVKey.RASTER_TYPE, AVKey.RASTER_TYPE_ELEVATION);
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT32);
            }
            else if (dataType == gdalconst.GDT_Float32 || dataType == gdalconst.GDT_CFloat32)
            {
                params.setValue(AVKey.RASTER_TYPE, AVKey.RASTER_TYPE_ELEVATION);
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
                params.setValue(AVKey.DATA_TYPE, AVKey.FLOAT32);
            }
            else if (dataType == gdalconst.GDT_Byte)
            {
                // if has only one band => one byte index of the palette, 216 marks voids
                params.setValue(AVKey.RASTER_TYPE, AVKey.RASTER_TYPE_COLOR_IMAGE);
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT8);
            }
            else if (dataType == gdalconst.GDT_UInt16)
            {
                params.setValue(AVKey.RASTER_TYPE,
                    ((bandCount >= 3) ? AVKey.RASTER_TYPE_COLOR_IMAGE : AVKey.RASTER_TYPE_MONOCHROME_IMAGE));
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT16);
            }
            else if (dataType == gdalconst.GDT_UInt32)
            {
                params.setValue(AVKey.RASTER_TYPE,
                    ((bandCount >= 3) ? AVKey.RASTER_TYPE_COLOR_IMAGE : AVKey.RASTER_TYPE_MONOCHROME_IMAGE));
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT32);
            }
            else
            {
                String msg = Logging.getMessage("generic.UnrecognizedDataType", dataType);
                Logging.logger().severe(msg);
                throw new WWRuntimeException(msg);
            }

            Double[] dbls = new Double[16];
            band.GetNoDataValue(dbls);
            if (null != dbls[0])
                params.setValue(AVKey.MISSING_DATA_SIGNAL, dbls[0]);

            if (AVKey.RASTER_TYPE_ELEVATION.equals(params.getValue(AVKey.RASTER_TYPE)))
            {
                band.GetMinimum(dbls);
                if (null != dbls[0])
                    params.setValue(AVKey.ELEVATION_MIN, dbls[0]);

                band.GetMaximum(dbls);
                if (null != dbls[0])
                    params.setValue(AVKey.ELEVATION_MAX, dbls[0]);

                // skip this heavy calculation if the file is opened in Quick Reading Node (when checking canRead())
                if (!GDAL.READING_MODE_QUICK.equals(params.getValue(GDAL.READING_MODE)))
                {
                    if (!params.hasKey(AVKey.ELEVATION_MIN) || !params.hasKey(AVKey.ELEVATION_MAX))
                    {
                        double[] minmax = new double[2];

                        band.ComputeRasterMinMax(minmax);

                        double nodata = (double) (Short.MIN_VALUE);
                        if (params.hasKey(AVKey.MISSING_DATA_SIGNAL))
                            nodata = (Double) params.getValue(AVKey.MISSING_DATA_SIGNAL);

                        if (minmax[0] == -32767d || minmax[0] == -32768d || minmax[0] == nodata)
                        {
                            params.setValue(AVKey.MISSING_DATA_SIGNAL, minmax[0]);
                            boolean success = gdalconst.CE_None == band.SetNoDataValue(minmax[0]);
                            band.ComputeRasterMinMax(minmax);
                        }

                        band.SetStatistics(minmax[0], minmax[1], 0d, 0d);

                        params.setValue(AVKey.ELEVATION_MIN, minmax[0]);
                        params.setValue(AVKey.ELEVATION_MAX, minmax[1]);
                    }
                }
            }
        }

        String proj_wkt = null;

        if (params.hasKey(AVKey.SPATIAL_REFERENCE_WKT))
            proj_wkt = params.getStringValue(AVKey.SPATIAL_REFERENCE_WKT);

        if (WWUtil.isEmpty(proj_wkt))
            proj_wkt = ds.GetProjectionRef();

        if (WWUtil.isEmpty(proj_wkt))
            proj_wkt = ds.GetProjection();

        SpatialReference srs = null;
        if (!WWUtil.isEmpty(proj_wkt))
        {
            params.setValue(AVKey.SPATIAL_REFERENCE_WKT, proj_wkt);
            srs = new SpatialReference(proj_wkt);
        }

        double[] gt = new double[6];
        ds.GetGeoTransform(gt);

        if (gt[GDAL.GT_5_PIXEL_HEIGHT] > 0)
            gt[GDAL.GT_5_PIXEL_HEIGHT] = -gt[GDAL.GT_5_PIXEL_HEIGHT];

        // calculate geo-coordinates in image's native CS and Projection (these are NOT lat/lon coordinates)
        java.awt.geom.Point2D[] corners = GDAL.computeCornersFromGeotransform(gt, width, height);

        double minX = GDAL.getMinX(corners);
        double minY = GDAL.getMinY(corners);
        double maxX = GDAL.getMaxX(corners);
        double maxY = GDAL.getMaxY(corners);

        double rotX = gt[GDAL.GT_2_ROTATION_X];
        double rotY = gt[GDAL.GT_4_ROTATION_Y];
        double pixelWidth = gt[GDAL.GT_1_PIXEL_WIDTH];
        double pixelHeight = gt[GDAL.GT_5_PIXEL_HEIGHT];

        params.setValue(AVKey.PIXEL_WIDTH, pixelWidth);
        params.setValue(AVKey.PIXEL_HEIGHT, pixelHeight);

        if (minX == 0d && pixelWidth == 1d && rotX == 0d && maxY == 0d && rotY == 0d && pixelHeight == 1d)
        {
            params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_SCREEN);
        }
        else if (Angle.isValidLongitude(minX) && Angle.isValidLatitude(maxY)
            && Angle.isValidLongitude(maxX) && Angle.isValidLatitude(minY))
        {
            if (null == srs)
                srs = createGeographicSRS();
            else if (srs.IsGeographic() == 0)
            {
                String msg = Logging.getMessage("generic.UnexpectedCoordinateSystem", srs.ExportToWkt());
                Logging.logger().warning(msg);
                srs = createGeographicSRS();
            }
        }

        if (null != srs)
        {
            Sector sector = null;

            if (!params.hasKey(AVKey.SPATIAL_REFERENCE_WKT))
                params.setValue(AVKey.SPATIAL_REFERENCE_WKT, srs.ExportToWkt());

            if (srs.IsLocal() == 1)
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_UNKNOWN);
                String msg = Logging.getMessage("generic.UnknownCoordinateSystem", proj_wkt);
                Logging.logger().severe(msg);
                return params;
//                throw new WWRuntimeException(msg);
            }

            // save area in image's native CS and Projection 
            GDAL.Area area = new GDAL.Area(srs, ds);

            if (null != area)
            {
                params.setValue(AVKey.GDAL_AREA, area);
                sector = area.getSector();
                if (null != sector)
                {
                    params.setValue(AVKey.SECTOR, sector);
                    LatLon origin = new LatLon(sector.getMaxLatitude(), sector.getMinLongitude());
                    params.setValue(AVKey.ORIGIN, origin);
                }
            }

            if (srs.IsGeographic() == 1)
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
                // no need to extract anything, all parameters were extracted above
            }
            else if (srs.IsProjected() == 1)
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_PROJECTED);

                // ----8><----------------------------------------------------------------------------------------
                // Example of a non-typical GDAL projection string
                //
                // PROJCS
                // [
                //      "NAD83 / Massachusetts Mainland",
                //      GEOGCS
                //      [
                //          "NAD83",
                //          DATUM
                //          [
                //              "North_American_Datum_1983",
                //              SPHEROID [ "GRS 1980", 6378137, 298.2572221010002, AUTHORITY[ "EPSG", "7019" ]],
                //              AUTHORITY [ "EPSG", "6269" ]
                //          ],
                //          PRIMEM [ "Greenwich", 0 ],
                //          UNIT [ "degree", 0.0174532925199433 ],
                //          AUTHORITY [ "EPSG", "4269" ]
                //      ],
                //      PROJECTION [ "Lambert_Conformal_Conic_2SP" ],
                //      PARAMETER [ "standard_parallel_1",42.68333333333333 ],
                //      PARAMETER["standard_parallel_2",41.71666666666667],
                //      PARAMETER["latitude_of_origin",41],
                //      PARAMETER["central_meridian",-71.5],
                //      PARAMETER["false_easting",200000],
                //      PARAMETER["false_northing",750000],
                //      UNIT [ "metre", 1, AUTHORITY [ "EPSG", "9001" ]],
                //      AUTHORITY [ "EPSG", "26986" ]
                //  ]
                // ----8><----------------------------------------------------------------------------------------

//                String projcs = srs.GetAttrValue("PROJCS");
//                String geocs = srs.GetAttrValue("PROJCS|GEOGCS");
//                String projcs_unit = srs.GetAttrValue("PROJCS|GEOGCS|UNIT");

                String projection = srs.GetAttrValue("PROJCS|PROJECTION");
                String unit = srs.GetAttrValue("PROJCS|UNIT");
                if (null != unit)
                {
                    unit = unit.toLowerCase();
                    if ("meter".equals(unit) || "meters".equals(unit) || "metre".equals(unit) || "metres".equals(unit))
                        params.setValue(AVKey.PROJECTION_UNITS, AVKey.UNIT_METER);
                    else if ("foot".equals(unit) || "feet".equals(unit))
                        params.setValue(AVKey.PROJECTION_UNITS, AVKey.UNIT_FOOT);
                    else
                        Logging.logger().warning(Logging.getMessage("generic.UnknownProjectionUnits", unit));
                }

                if (null != projection && 0 < projection.length())
                    params.setValue(AVKey.PROJECTION_NAME, projection);
            }
            else if (srs.IsLocal() == 1)
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_SCREEN);
            }
            else
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_UNKNOWN);
                String msg = Logging.getMessage("generic.UnknownCoordinateSystem", proj_wkt);
                Logging.logger().severe(msg);
//                throw new WWRuntimeException(msg);
            }
        }

        if (!params.hasKey(AVKey.COORDINATE_SYSTEM))
            params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_UNKNOWN);

        return params;
    }

    public static DataRaster composeDataRaster(Dataset ds, AVList params)
        throws IllegalArgumentException, WWRuntimeException
    {
        if (!isGDALLoaded)
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        params = extractRasterParameters(ds, params);

        String rasterType = params.getStringValue(AVKey.RASTER_TYPE);
        if (AVKey.RASTER_TYPE_ELEVATION.equals(rasterType))
        {
            return composeNonImageDataRaster(ds, params);
        }
        else if (AVKey.RASTER_TYPE_COLOR_IMAGE.equals(rasterType)
            || AVKey.RASTER_TYPE_MONOCHROME_IMAGE.equals(rasterType))
        {
            return composeImageDataRaster(ds, params);
        }
        else
        {
            String message = Logging.getMessage("generic.UnexpectedRasterType", rasterType);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
    }

    public static int[] extractBandOrder(Dataset ds, AVList params)
        throws IllegalArgumentException, WWRuntimeException
    {
        if (!isGDALLoaded)
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (null == params)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int[] bandsOrder = null;

        if (params.hasKey(AVKey.BANDS_ORDER))
        {
            int bandsCount = ds.getRasterCount();

            Object o = params.getValue(AVKey.BANDS_ORDER);

            if (null != o && o instanceof Integer[])
            {
                Integer[] order = (Integer[]) o;
                bandsOrder = new int[order.length];
                for (int i = 0; i < order.length; i++)
                {
                    bandsOrder[i] = order[i];
                }
            }
            else if (null != o && o instanceof int[])
            {
                bandsOrder = (int[]) o;
            }

            if (null == bandsOrder)
            {
                String message = Logging.getMessage("nullValue.BandOrderIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (0 == bandsOrder.length)
            {
                String message = Logging.getMessage("generic.BandOrderIsEmpty");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            for (int i = 0; i < bandsOrder.length; i++)
            {
                if (bandsOrder[i] < 0 || bandsOrder[i] >= bandsCount)
                {
                    String message = Logging.getMessage("generic.InvalidBandOrder", bandsOrder[i], bandsCount);
                    Logging.logger().severe(message);
                    throw new IllegalArgumentException(message);
                }
            }
        }
        return bandsOrder;
    }

    /**
     * The "composeDataRaster" method creates a ByteBufferRaster from an elevation (or non-image) Dataset.
     *
     * @param ds     The GDAL dataset with data raster (expected only elevation raster); f or imagery rasters use
     *               composeImageDataRaster() method
     * @param params , The AVList with properties (usually used to force projection info or sector)
     *
     * @return ByteBufferRaster as DataRaster
     *
     * @throws IllegalArgumentException if raster parameters (height, width, sector, etc) are invalid
     * @throws WWRuntimeException       when invalid raster detected (like attempt to use the method for imagery
     *                                  raster)
     */
    protected static DataRaster composeNonImageDataRaster(Dataset ds, AVList params)
        throws IllegalArgumentException, WWRuntimeException
    {
        String rasterType = params.getStringValue(AVKey.RASTER_TYPE);
        if (!AVKey.RASTER_TYPE_ELEVATION.equals(rasterType))
        {
            String message = Logging.getMessage("generic.UnexpectedRasterType", rasterType);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        Object o = params.getValue(AVKey.SECTOR);
        if (null == o || !(o instanceof Sector))
        {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.SECTOR);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        Sector sector = (Sector) o;

        int bandCount = ds.getRasterCount();
        // we expect here one band (elevation rasters have -32767 or -32768 in void places) data raster
        if (bandCount != 1)
        {
            String message = Logging.getMessage("generic.UnexpectedBandCount", bandCount);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        ByteOrder byteOrder = ByteOrder.nativeOrder();
        if (params.hasKey(AVKey.BYTE_ORDER))
        {
            byteOrder = AVKey.LITTLE_ENDIAN.equals(params.getStringValue(AVKey.BYTE_ORDER))
                ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        }
        else
        {
            params.setValue(AVKey.BYTE_ORDER,
                (byteOrder == ByteOrder.BIG_ENDIAN) ? AVKey.BIG_ENDIAN : AVKey.LITTLE_ENDIAN);
        }

        int width = ds.getRasterXSize();
        int height = ds.getRasterYSize();

        Band band = ds.GetRasterBand(1);
        if (null == band)
        {
            String message = Logging.getMessage("nullValue.RasterBandIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        int dataType = band.getDataType();
        int dataTypeSize = gdal.GetDataTypeSize(dataType);
        int bufferSize = width * height * (dataTypeSize / 8);

        ByteBuffer data = null;
        try
        {
            data = ByteBuffer.allocateDirect(bufferSize);
        }
        catch (Throwable t)
        {
            String message = Logging.getMessage("generic.MemoryAllocationError", bufferSize);
            Logging.logger().log(Level.SEVERE, message, t);
            throw new WWRuntimeException(message);
        }

        data.order(byteOrder);

        int returnVal = band.ReadRaster_Direct(0, 0, band.getXSize(), band.getYSize(),
            width, height, band.getDataType(), data);

        if (returnVal != gdalconstConstants.CE_None)
            throw new WWRuntimeException(GDALUtils.getErrorMessage());

        return new ByteBufferRaster(width, height, sector, data, params);
    }

    protected static void alterJavaLibraryPath(String newJavaLibraryPath)
        throws IllegalAccessException, NoSuchFieldException
    {
        System.setProperty(JAVA_LIBRARY_PATH, newJavaLibraryPath);

        newClassLoader = ClassLoader.class;
        fieldSysPaths = newClassLoader.getDeclaredField("sys_paths");
        if (null != fieldSysPaths)
        {
            fieldSysPaths_accessible = fieldSysPaths.isAccessible();
            if (!fieldSysPaths_accessible)
                fieldSysPaths.setAccessible(true);

            originalClassLoader = fieldSysPaths.get(newClassLoader);

            // Reset it to null so that whenever "System.loadLibrary" is called,
            // it will be reconstructed with the changed value.
            fieldSysPaths.set(newClassLoader, null);
        }
    }

    protected static void restoreJavaLibraryPath()
    {
        try
        {
            //Revert back the changes.
            if (null != originalClassLoader && null != fieldSysPaths)
            {
                fieldSysPaths.set(newClassLoader, originalClassLoader);
                fieldSysPaths.setAccessible(fieldSysPaths_accessible);
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static Class newClassLoader = null;
    private static Object originalClassLoader = null;
    private static Field fieldSysPaths = null;
    private static boolean fieldSysPaths_accessible = false;
}