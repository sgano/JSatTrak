/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.catalogui.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.util.Logging;

import java.net.URI;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: GetCapabilities.java 13375 2010-05-05 23:43:39Z dcollins $
 */
public class GetCapabilities
{
    private GIDBResultModel resultModel;
    private Server server;

    public GetCapabilities(GIDBResultModel resultModel)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (resultModel.getServer() == null)
        {
            String message = "nullValue.ResultModelServerIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.resultModel = resultModel;
        this.server = resultModel.getServer();
    }

    public void executeRequest() throws Exception
    {
        // Attempt to get WMS capabilities if the service is not known,
        // or if the service is WMS.
        Object o = this.resultModel.getValue(CatalogKey.SERVICE_TYPE);
        if (o == null || o.equals(CatalogKey.WMS))
        {
            setCapabilities();
            setLayers();
        }
    }

    protected void setCapabilities() throws Exception
    {
        String uriString = null;
        if (this.server.getURL() != null)
            uriString = this.server.getURL().getValue();

        WMSCapabilities caps = null;
        if (uriString != null)
        {
            try
            {
                caps = WMSCapabilities.retrieve(new URI(uriString));
                if (caps != null)
                    caps.parse();
            }
            catch (Exception e)
            {
                String message = "Cannot read WMS Capabilities document at " + uriString;
                Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
                CatalogException ex = new CatalogException(message, null);
                this.resultModel.addException(ex);
            }
        }
        else
        {
            String message = "Service does not specify access points for GetCapabilities and GetMap.";
            CatalogException e = new CatalogException(message, null);
            this.resultModel.addException(e);
        }

        this.resultModel.setCapabilities(caps);

        // WMS capabilities request succeeded, so the service must be WMS.
        // Designate the services as WMS unless a designation already exists.
        if (caps != null)
            if (this.resultModel.getValue(CatalogKey.SERVICE_TYPE) == null)
                this.resultModel.setValue(CatalogKey.SERVICE_TYPE, CatalogKey.WMS);
    }

    protected void setLayers()
    {
        WMSCapabilities caps = this.resultModel.getCapabilities();
        if (caps == null)
            return;

        // Gather up all the named layers and make a world wind layer for each.
        List<WMSLayerCapabilities> layers = caps.getNamedLayers();
        if (layers == null)
            return;

        for (WMSLayerCapabilities layerCaps : layers)
        {
            if (layerCaps == null)
                continue;

            Set<WMSLayerStyle> styles = layerCaps.getStyles();

            if (styles != null && styles.size() > 0)
            {
                for (WMSLayerStyle styleCaps : styles)
                {
                    this.addLayer(layerCaps, styleCaps);
                }
            }
            else
            {
                this.addLayer(layerCaps, null);
            }
        }
    }

    protected void addLayer(WMSLayerCapabilities layerCaps, WMSLayerStyle styleCaps)
    {
        Layer layer = new Layer();
        layer.setServer(this.server);

        String s = layerCaps.getName();
        if (s != null)
            layer.setName(s);

        if (styleCaps != null)
        {
            s = styleCaps.getName();
            if (s != null)
                layer.setStyle(s);
        }

        makeLayerParams(layerCaps, styleCaps, layer);

        LayerList ll = this.resultModel.getLayerList();
        if (ll == null)
        {
            ll = new LayerListImpl();
            this.resultModel.setLayerList(ll);
        }
        ll.addLayer(layer);
    }

    protected void makeLayerParams(WMSLayerCapabilities layerCaps, WMSLayerStyle styleCaps, Layer dest)
    {
        if (layerCaps == null)
        {
            String message = "nullValue.LayerCapsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String name = layerCaps.getName();
        if (name != null)
        {
            dest.setValue(AVKey.LAYER_NAMES, name);
            dest.setValue(CatalogKey.NAME, name);
        }

        String style;
        if (styleCaps != null)
        {
            style = styleCaps.getName();
            if (style != null)
                dest.setValue(AVKey.STYLE_NAMES, style);
        }

        String s = makeTitle(layerCaps, styleCaps);
        if (s != null)
        {
            dest.setValue(AVKey.TITLE, makeWWJTitle(s));
            dest.setValue(CatalogKey.TITLE, s);
        }

        s = layerCaps.getLayerAbstract();
        if (s != null)
        {
            dest.setValue(CatalogKey.ABSTRACT, s);
            dest.setValue(CatalogKey.DESCRIPTION, s);
        }

        dest.setValue(CatalogKey.LAYER_STATE, CatalogKey.LAYER_STATE_READY);

        // Provide a non-null value for UI elements looking for this action.
        dest.setValue(GIDBKey.ACTION_COMMAND_LAYER_PRESSED, dest);
    }

    protected String makeWWJTitle(String title)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(title);
        sb.append("<br>");
        sb.append("(Retrieved from Catalog)");
        sb.append("</html>");
        return sb.toString();
    }

    private static String makeTitle(WMSLayerCapabilities layerCaps, WMSLayerStyle styleCaps)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(layerCaps.getTitle());

        if (styleCaps != null)
        {
            sb.append(":");
            sb.append(styleCaps.getTitle());
        }

        return sb.toString();
    }
}
