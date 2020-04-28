/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.*;

import java.beans.PropertyChangeEvent;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Basic implementation of {@link FileStore}.
 *
 * @author Tom Gaskins
 * @version $Id: BasicDataFileStore.java 14218 2010-12-10 03:44:10Z tgaskins $
 */
public class BasicDataFileStore extends AbstractFileStore
{
    /** The number of milliseconds to wait before a retrieval request for the same file can be reissued. */
    protected static final long TIMEOUT = (long) 5e3;
    /** The map of cached entries. */
    protected BasicMemoryCache db = new BasicMemoryCache((long) 3e5, (long) 5e5);
    protected AbsentResourceList absentResources = new AbsentResourceList();

    /**
     * Create an instance.
     *
     * @throws IllegalStateException if the configuration file name cannot be determined from {@link Configuration} or
     *                               the configuration file cannot be found.
     */
    public BasicDataFileStore()
    {
        String configPath = Configuration.getStringValue(AVKey.DATA_FILE_STORE_CONFIGURATION_FILE_NAME);
        if (configPath == null)
        {
            String message = Logging.getMessage("FileStore.NoConfiguration");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        java.io.InputStream is = null;
        File configFile = new File(configPath);
        if (configFile.exists())
        {
            try
            {
                is = new FileInputStream(configFile);
            }
            catch (FileNotFoundException e)
            {
                String message = Logging.getMessage("FileStore.LocalConfigFileNotFound", configPath);
                Logging.logger().finest(message);
            }
        }

        if (is == null)
        {
            is = this.getClass().getClassLoader().getResourceAsStream(configPath);
        }

        if (is == null)
        {
            String message = Logging.getMessage("FileStore.ConfigurationNotFound", configPath);
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.initialize(is);
    }

    /**
     * Create an instance to manage a specified directory.
     *
     * @param directoryPath the directory to manage as a file store.
     */
    public BasicDataFileStore(File directoryPath)
    {
        if (directoryPath == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?>");

        sb.append("<dataFileStore><writeLocations><location wwDir=\"");
        sb.append(directoryPath.getAbsolutePath());
        sb.append("\" create=\"true\"/></writeLocations></dataFileStore>");

        this.initialize(WWIO.getInputStreamFromString(sb.toString()));
    }

    /** Holds information for entries in the cache database. */
    protected static class DBEntry implements Cacheable
    {
        protected final static int NONE = 0;
        protected final static int PENDING = 1;
        protected final static int LOCAL = 2;

        protected String name;
        protected String contentType;
        protected URL localUrl;
        protected long lastUpdateTime;
        protected int state;

        public DBEntry(String name)
        {
            this.name = name;
            this.state = NONE;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public long getSizeInBytes()
        {
            return 40 + (name != null ? 2 * name.length() : 0);
        }
    }

    /**
     * Requests a file. If the file exists locally, including as a resouce on the classpath, a {@link URL} to the file
     * is returned. Otherwise if the specified address is a URL to a remote location, a request for the file is
     * initiated. When the request succeeds the file will be stored in the local World Wind cache and subsequent
     * invocations of this method will return a URL to the retrieved file.
     *
     * @param address the file address, either a URL or a path relative to the root of the file store.
     *
     * @return a URL for the file if it was found locally, otherwise null.
     *
     * @throws IllegalArgumentException if the address is null.
     */
    public synchronized URL requestFile(String address)
    {
        if (address == null)
        {
            String message = Logging.getMessage("nullValue.AddressIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        DBEntry entry = (DBEntry) this.db.getObject(address);
        if (entry != null)
        {
            if (entry.state == DBEntry.LOCAL)
                return entry.localUrl;

            if (entry.state == DBEntry.PENDING && (System.currentTimeMillis() - entry.lastUpdateTime <= TIMEOUT))
                return null;
        }

        URL url = WWIO.makeURL(address); // this may or may not make a URL, depending on address type
        URL localUrl = this.getLocalFileUrl(address, url);
        if (localUrl != null)
            return localUrl;

        if (url != null && !this.absentResources.isResourceAbsent(address))
            this.makeLocal(address, url);

        return null;
    }

    /**
     * Returns a file from the cache, the local file system or the classpath if the file exists. The specified address
     * may be a jar URL. See {@link java.net.JarURLConnection} for a description of jar URLs.
     *
     * @param address      the name used to identify the cached file.
     * @param retrievalUrl the URL to obtain the file if it is not in the cache. Used only to determine a location to
     *                     search in the local cache. May be null.
     *
     * @return the requested file if it exists, otherwise null.
     *
     * @throws IllegalArgumentException if the specified address is null.
     */
    protected synchronized URL getLocalFileUrl(String address, URL retrievalUrl)
    {
        if (address == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        URL cacheFileUrl = null;

        if (address.trim().startsWith("jar:"))
        {
            URL jarUrl = WWIO.makeURL(address); // retrieval URL may be other than the address' URL
            if (WWIO.isLocalJarAddress(jarUrl))
            {
                if (this.getJarLength(jarUrl) > 0)
                    cacheFileUrl = jarUrl;
                else
                {
                    absentResources.markResourceAbsent(address);
                    return null;
                }
            }
        }

        if (cacheFileUrl == null)
        {
            File f = new File(address);
            if (f.exists())
                cacheFileUrl = WWIO.makeURL(address, "file"); // makes a file URL if the address is not yet a URL
        }

        if (cacheFileUrl == null)
            cacheFileUrl = WorldWind.getDataFileStore().findFile(address, true);

        if (cacheFileUrl == null && retrievalUrl != null)
            cacheFileUrl = WorldWind.getDataFileStore().findFile(makeCachePath(retrievalUrl, null), true);

        if (cacheFileUrl != null)
        {
            DBEntry entry = new DBEntry(address);
            entry.localUrl = cacheFileUrl;
            entry.state = DBEntry.LOCAL;
            entry.contentType = WWIO.makeMimeTypeForSuffix(WWIO.getSuffix(cacheFileUrl.getPath()));
            this.db.add(address, entry);
            this.absentResources.unmarkResourceAbsent(address);

            return cacheFileUrl;
        }

        return null;
    }

    /**
     * Returns the length of the resource referred to by a jar URL. Can be used to test whether the resource exists.
     * <p/>
     * Note: This method causes the URL to open a connection and retrieve content length.
     *
     * @param jarUrl the jar URL.
     *
     * @return the jar file's content length, or -1 if a connection to the URL can't be formed or queried.
     */
    protected int getJarLength(URL jarUrl)
    {
        try
        {
            return jarUrl.openConnection().getContentLength();
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.JarOpenFailed", jarUrl.toString());
            Logging.logger().log(java.util.logging.Level.WARNING, message, e);

            return -1;
        }
    }

    /**
     * Retrieves a specified file and adds it to the cache.
     *
     * @param address the name used to identify the cached file.
     * @param url     the URL to obtain the file.
     */
    protected synchronized void makeLocal(final String address, final URL url)
    {
        if (WorldWind.getNetworkStatus().isHostUnavailable(url) || !WorldWind.getRetrievalService().isAvailable())
            return;

        final DBEntry newEntry = new DBEntry(address);
        this.db.add(address, newEntry);
        newEntry.state = DBEntry.PENDING;

        Retriever retriever = URLRetriever.createRetriever(url, new PostProcessor(address, url));

        if (retriever != null && !WorldWind.getRetrievalService().contains(retriever))
            WorldWind.getRetrievalService().runRetriever(retriever);
//            Retriever retriever = new HTTPRetriever(url, new AbstractRetrievalPostProcessor()
//            {
//                protected URL localFileUrl = null;
//
//                @Override
//                protected boolean overwriteExistingFile()
//                {
//                    return true;
//                }
//
//                protected File doGetOutputFile()
//                {
//                    String path = makeCachePath(url, this.getRetriever().getContentType());
//                    File file = WorldWind.getDataFileStore().newFile(path);
//                    if (file == null)
//                        return null;
//
//                    try
//                    {
//                        this.localFileUrl = file.toURI().toURL();
//                        return file;
//                    }
//                    catch (MalformedURLException e)
//                    {
//                        String message = Logging.getMessage("generic.MalformedURL", file.toURI());
//                        Logging.logger().finest(message);
//                        return null;
//                    }
//                }
//
//                @Override
//                protected boolean saveBuffer() throws IOException
//                {
//                    boolean tf = super.saveBuffer();
//                    updateEntry(address, this.localFileUrl);
//                    return tf;
//                }
//
//                @Override
//                protected ByteBuffer handleSuccessfulRetrieval()
//                {
//                    ByteBuffer buffer = super.handleSuccessfulRetrieval();
//
//                    BasicDataFileStore.this.firePropertyChange(
//                        new PropertyChangeEvent(BasicDataFileStore.this, AVKey.RETRIEVAL_STATE_SUCCESSFUL, url,
//                            this.localFileUrl));
//
//                    return buffer;
//                }
//
//                @Override
//                protected void markResourceAbsent()
//                {
//                    absentResources.markResourceAbsent(address);
//                }
//            });
//        }
    }

    protected class PostProcessor extends AbstractRetrievalPostProcessor
    {
        protected String address;
        protected URL retrievalUrl;
        protected URL localFileUrl = null;

        public PostProcessor(String address, URL url)
        {
            this.address = address;
            this.retrievalUrl = url;
        }

        @Override
        protected boolean overwriteExistingFile()
        {
            return true;
        }

        protected File doGetOutputFile()
        {
            String path = makeCachePath(this.retrievalUrl, this.getRetriever().getContentType());
            File file = WorldWind.getDataFileStore().newFile(path);
            if (file == null)
                return null;

            try
            {
                this.localFileUrl = file.toURI().toURL();
                return file;
            }
            catch (MalformedURLException e)
            {
                String message = Logging.getMessage("generic.MalformedURL", file.toURI());
                Logging.logger().finest(message);
                return null;
            }
        }

        @Override
        protected boolean saveBuffer() throws IOException
        {
            boolean tf = super.saveBuffer();
            updateEntry(this.address, this.localFileUrl);
            return tf;
        }

        @Override
        protected ByteBuffer handleSuccessfulRetrieval()
        {
            ByteBuffer buffer = super.handleSuccessfulRetrieval();

            firePropertyChange(
                new PropertyChangeEvent(BasicDataFileStore.this, AVKey.RETRIEVAL_STATE_SUCCESSFUL, this.retrievalUrl,
                    this.localFileUrl));

            return buffer;
        }

        @Override
        protected void markResourceAbsent()
        {
            BasicDataFileStore.this.absentResources.markResourceAbsent(this.address);
        }
    }

    /**
     * Updates a cache entry with information available once the file is retrieved.
     *
     * @param address      the name used to identify the file in the cache.
     * @param localFileUrl the path to the local copy of the file.
     */
    protected synchronized void updateEntry(String address, URL localFileUrl)
    {
        DBEntry entry = (DBEntry) this.db.getObject(address);
        if (entry == null)
            return;

        entry.state = DBEntry.LOCAL;
        entry.localUrl = localFileUrl;
        entry.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Makes a path to the file in the cache from the file's URL and content type.
     *
     * @param url         the URL to obtain the file.
     * @param contentType the mime type of the file's contents.
     *
     * @return a path name.
     */
    protected String makeCachePath(URL url, String contentType)
    {
        String cacheDir;
        String fileName;

        if ("jar".equals(url.getProtocol()))
        {
            String innerAddress = url.getPath();
            URL innerUrl = WWIO.makeURL(innerAddress);
            cacheDir = WWIO.replaceIllegalFileNameCharacters(innerUrl.getHost());
            fileName = WWIO.replaceIllegalFileNameCharacters(innerUrl.getPath().replace("!/", "#"));
        }
        else
        {
            cacheDir = WWIO.replaceIllegalFileNameCharacters(url.getHost());
            fileName = WWIO.replaceIllegalFileNameCharacters(url.getPath());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(cacheDir);
        sb.append(File.separator);
        sb.append(fileName);

        String suffix = contentType != null ? WWIO.makeSuffixForMimeType(contentType) : null;
        String existingSuffix = WWIO.getSuffix(url.toString());
        if (suffix != null && (existingSuffix == null || !existingSuffix.equals(suffix.substring(1))))
            sb.append(suffix);

        return sb.toString();
    }

    public String getContentType(String address)
    {
        if (address == null)
            return null;

        DBEntry entry = (DBEntry) this.db.getObject(address);
        return entry != null ? entry.contentType : null;
    }
}
