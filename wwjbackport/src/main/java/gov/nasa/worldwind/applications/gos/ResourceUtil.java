/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.applications.gos.services.FGDCServiceStatus;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.wms.CapabilitiesRequest;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPath;
import java.awt.image.*;
import java.beans.*;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.*;

/**
 * @author dcollins
 * @version $Id: ResourceUtil.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class ResourceUtil
{
    protected static final int DEFAULT_APP_THREAD_COUNT = 4;
    protected static ExecutorService executorService = Executors.newFixedThreadPool(
        Configuration.getIntegerValue(GeodataKey.APP_THREAD_COUNT, DEFAULT_APP_THREAD_COUNT));

    public static ExecutorService getAppTaskService()
    {
        return executorService;
    }

    public static String createErrorMessage(Exception e)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Error: ");

        if (e instanceof SocketTimeoutException)
            sb.append("Operation timed out.");
        else
            sb.append(e.getMessage());

        return sb.toString();
    }

    public static String makeWMSLayerDisplayName(WMSLayerCapabilities layer, WMSLayerStyle style)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(!WWUtil.isEmpty(layer.getTitle()) ? layer.getTitle() : layer.getName());

        if (style != null && !style.getName().equalsIgnoreCase("default"))
        {
            sb.append(" : ");
            sb.append(!WWUtil.isEmpty(style.getTitle()) ? style.getTitle() : style.getName());
        }

        return sb.toString();
    }

    public static Object getResourceReference(String name)
    {
        Element resourceList = Configuration.getElement("//ResourceList");
        if (resourceList == null)
            return null;

        XPath xpath = WWXML.makeXPath();

        Element resource = WWXML.getElement(resourceList, "./Resource[@name=\"" + name + "\"]", xpath);
        if (resource == null)
            return null;

        String urlString = WWXML.getText(resource, "@url", null);
        if (!WWUtil.isEmpty(urlString))
            return WWIO.makeURL(urlString);

        String path = WWXML.getText(resource, "@path", null);
        if (!WWUtil.isEmpty(path))
            return path;

        return null;
    }

    public static URI fixGetCapabilitiesURI(URI uri)
    {
        try
        {
            String s = uri.toString();

            int pos = s.indexOf("http://");
            if (pos < 0)
                pos = s.indexOf("https://");
            if (pos > 0)
                s = s.substring(pos, s.length());

            pos = s.indexOf("?");
            if (pos > 0)
                s = s.substring(0, pos);

            CapabilitiesRequest request = new CapabilitiesRequest(new URI(s));
            uri = request.getUri();
        }
        catch (URISyntaxException e)
        {
            Logging.logger().severe(Logging.getMessage("gosApp.ResourceURIInvalid", uri.toString()));
        }

        return uri;
    }

    public static BufferedImage getCachedImage(Object source)
    {
        String key = source.toString();
        BufferedImage image = (BufferedImage) getImageCache().getObject(key);

        if (image == null && !getImageCache().contains(key))
        {
            image = openImage(source);
            getImageCache().add(key, image, (image != null) ? ImageUtil.computeSizeInBytes(image) : 1L);
        }

        return image;
    }

    public static ServiceStatus getOrRetrieveServiceStatus(final URL url, final PropertyChangeListener listener)
    {
        ServiceStatus status = (ServiceStatus) WorldWind.getSessionCache().get(url.toString());

        if (status != null || WorldWind.getSessionCache().contains(url.toString()))
        {
            return status;
        }

        getAppTaskService().execute(new Runnable()
        {
            public void run()
            {
                ServiceStatus status = null;
                try
                {
                    status = FGDCServiceStatus.retrieve(url.toURI());
                    WorldWind.getSessionCache().put(url.toString(), status);
                }
                catch (Exception e)
                {
                    Logging.logger().log(java.util.logging.Level.SEVERE,
                        Logging.getMessage("gosApp.ExceptionRetrievingServiceStatus", url.toString()), e);
                }

                if (listener != null)
                {
                    listener.propertyChange(new PropertyChangeEvent(url.toString(), GeodataKey.SERVICE_STATUS,
                        null, status));
                }
            }
        });

        return null;
    }

    protected static BufferedImage openImage(Object source)
    {
        InputStream stream = null;
        BufferedImage image = null;

        try
        {
            stream = WWIO.openStream(source);
            image = ImageIO.read(stream);
        }
        catch (Exception e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE,
                Logging.getMessage("generic.ExceptionAttemptingToReadImageFile", source), e);
        }
        finally
        {
            WWIO.closeStream(stream, source.toString());
        }

        return image;
    }

    protected static MemoryCache getImageCache()
    {
        if (!WorldWind.getMemoryCacheSet().containsCache(GeodataKey.IMAGE_CACHE))
        {
            long capacity = Configuration.getLongValue(GeodataKey.IMAGE_CACHE_SIZE, 10000000L);
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * capacity), capacity);
            WorldWind.getMemoryCacheSet().addCache(GeodataKey.IMAGE_CACHE, cache);
        }

        return WorldWind.getMemoryCache(GeodataKey.IMAGE_CACHE);
    }
}
