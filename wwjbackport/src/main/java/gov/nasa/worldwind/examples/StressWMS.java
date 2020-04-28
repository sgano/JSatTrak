/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;

import java.net.*;
import java.util.*;

/**
 * @author jparsons
 * @version $Id: StressWMS.java 13323 2010-04-21 04:59:20Z tgaskins $
 */
public class StressWMS
{
    private static class LayerInfo
    {
        private WMSCapabilities caps;
        private AVListImpl params = new AVListImpl();

        private String getTitle()
        {
            return params.getStringValue(AVKey.TITLE);
        }
    }

    private final int MAXNUMLAYERS = 20;
    private int numAvailableLayers;
    private final URI externalURI;
    private final URI bluemarbleURI = new URI("http://www.nasa.network.com/wms");
    private final TreeSet<LayerInfo> externalLayerInfos = new TreeSet<LayerInfo>(new Comparator<LayerInfo>()
    {
        public int compare(LayerInfo infoA, LayerInfo infoB)
        {
            String nameA = infoA.getTitle();
            String nameB = infoB.getTitle();
            return nameA.compareTo(nameB);
        }
    });

    private final TreeSet<LayerInfo> blueMarbleLayerInfos = new TreeSet<LayerInfo>(new Comparator<LayerInfo>()
    {
        public int compare(LayerInfo infoA, LayerInfo infoB)
        {
            String nameA = infoA.getTitle();
            String nameB = infoB.getTitle();
            return nameA.compareTo(nameB);
        }
    });

    public StressWMS(String server) throws URISyntaxException
    {
        // See if the server name is a valid URI. Throw an exception if not.
        this.externalURI = new URI(server.trim()); // throws an exception if server name is not a valid uri.

        // Thread off a retrieval of the server's capabilities document and update of this panel.
        Thread loadingThread = new Thread(new Runnable()
        {
            public void run()
            {
                load(bluemarbleURI, blueMarbleLayerInfos);
                load(externalURI, externalLayerInfos);
                setSize();
            }
        });
        loadingThread.setPriority(Thread.MIN_PRIORITY);
        loadingThread.start();
    }

    private void load(URI uri, TreeSet<LayerInfo> layerInfos)
    {
        WMSCapabilities caps;

        try
        {
            caps = new WMSCapabilities(uri);
            caps.parse();
        }
        catch (Exception e)
        {
            Logging.logger().severe("Error initializing WMS URI: " + uri.toString() + "  " + e.getMessage());
            return;
        }

        // Gather up all the named layers and make a world wind layer for each.
        try
        {
            for (WMSLayerCapabilities layerCaps : caps.getNamedLayers())
            {
                Set<WMSLayerStyle> styles = layerCaps.getStyles();
                if (styles == null)
                {
                    LayerInfo layerInfo = createLayerInfo(caps, layerCaps, null);
                    layerInfos.add(layerInfo);
                }
                else
                {
                    for (WMSLayerStyle style : styles)
                    {
                        LayerInfo layerInfo = createLayerInfo(caps, layerCaps, style);
                        layerInfos.add(layerInfo);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Logging.logger().severe("Error initializing WMS URI: " + uri.toString() + "  Error: " + e.getMessage());
        }
    }

    private void setSize()
    {
        numAvailableLayers = 0;
        if (blueMarbleLayerInfos.size() >= 4)
            numAvailableLayers = 4;

        if (externalLayerInfos != null && externalLayerInfos.size() > 0)//return layerInfos.size();
            numAvailableLayers = numAvailableLayers + externalLayerInfos.size();
    }

    public int size()
    {
        return Math.min(numAvailableLayers, MAXNUMLAYERS);
    }

    //Add first 4 layers from blueMarble then add from external, if available
    public Layer getLayer(int index)
    {
        if (index < 4)
        {
            Object[] infos = blueMarbleLayerInfos.toArray();
            LayerInfo layerInfo = (LayerInfo) infos[index];
            Layer layer = new WMSTiledImageLayer(layerInfo.caps, layerInfo.params);
            layer.setEnabled(false);
            layer.setEnabled(true);

            // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
            layer.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
            layer.setValue(AVKey.URL_READ_TIMEOUT, 30000);
            layer.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

            return layer;
        }
        else if (index < size())
        {
            index = index - 4;
            Object[] infos = externalLayerInfos.toArray();
            LayerInfo layerInfo = (LayerInfo) infos[index];
            Layer layer = new WMSTiledImageLayer(layerInfo.caps, layerInfo.params);
            layer.setEnabled(false);
            layer.setEnabled(true);

            // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
            layer.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
            layer.setValue(AVKey.URL_READ_TIMEOUT, 30000);
            layer.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

            return layer;
        }
        else
            return null;
    }

    private LayerInfo createLayerInfo(WMSCapabilities caps, WMSLayerCapabilities layerCaps, WMSLayerStyle style)
    {
        // Create the layer info specified by the layer's capabilities entry and the selected style.
        LayerInfo linfo = new LayerInfo();
        linfo.caps = caps;
        linfo.params = new AVListImpl();
        linfo.params.setValue(AVKey.LAYER_NAMES, layerCaps.getName());
        if (style != null)
            linfo.params.setValue(AVKey.STYLE_NAMES, style.getName());

        linfo.params.setValue(AVKey.TITLE, makeTitle(linfo));

        return linfo;
    }

    private static String makeTitle(LayerInfo layerInfo)
    {
        String layerNames = layerInfo.params.getStringValue(AVKey.LAYER_NAMES);
        String styleNames = layerInfo.params.getStringValue(AVKey.STYLE_NAMES);
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++)
        {
            if (sb.length() > 0)
                sb.append(", ");

            String layerName = lNames[i];
            WMSLayerCapabilities layer = layerInfo.caps.getLayerByName(layerName);
            String layerTitle = layer.getTitle();
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i)
                continue;

            String styleName = sNames[i];
            WMSLayerStyle style = layer.getStyleByName(styleName);
            if (style == null)
                continue;

            sb.append(" : ");
            String styleTitle = style.getTitle();
            sb.append(styleTitle != null ? styleTitle : styleName);
        }

        return sb.toString();
    }
}
