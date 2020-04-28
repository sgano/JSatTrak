/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.globe;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.util.Set;

/**
 * @author dcollins
 * @version $Id: WorldWindowGlobeModel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class WorldWindowGlobeModel implements GlobeModel
{
    protected WorldWindow wwd;
    protected LegendLayer legendLayer;

    public WorldWindowGlobeModel(WorldWindow wwd)
    {
        this.wwd = wwd;
    }

    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    public boolean hasWMSLayer(String uuid, WMSLayerCapabilities layer, WMSLayerStyle style)
    {
        LayerList wwLayerList = this.getMatchingLayers(uuid, layer, style);
        return wwLayerList != null && !wwLayerList.isEmpty();
    }

    public void addWMSLayer(String uuid, WMSCapabilities caps, WMSLayerCapabilities layer, WMSLayerStyle style)
    {
        AVList params = new AVListImpl();
        params.setValue(GeodataKey.UUID, uuid);
        params.setValue(AVKey.LAYER_NAMES, layer.getName());
        if (style != null)
            params.setValue(AVKey.STYLE_NAMES, style.getName());

        Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
        Layer wwLayer = (Layer) factory.createFromConfigSource(caps, params.copy());
        wwLayer.setName(this.makeWMSLayerDisplayName(layer, style));
        wwLayer.setValues(params);
        this.addWWLayer(wwLayer);

        if (style != null)
            this.addStyleLegendLayer(layer, style, wwLayer);
    }

    public void removeWMSLayer(String uuid, WMSLayerCapabilities layer, WMSLayerStyle style)
    {
        LayerList wwLayerList = this.getMatchingLayers(uuid, layer, style);
        if (wwLayerList != null && wwLayerList.size() > 0)
        {
            this.wwd.getModel().getLayers().removeAll(wwLayerList);

            for (Layer wwLayer : wwLayerList)
            {
                Object legend = wwLayer.getValue(GeodataKey.LEGEND);
                if (legend != null)
                    this.getLegendLayer().removeLegend((LegendLayer.Legend) legend);
            }
        }
    }

    public void moveViewTo(Sector sector)
    {
        Globe globe = this.wwd.getModel().getGlobe();
        double ve = this.wwd.getSceneController().getVerticalExaggeration();

        Extent extent = Sector.computeBoundingCylinder(globe, ve, sector);
        if (extent == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().warning(message);
            return;
        }

        View view = this.wwd.getView();
        Angle fov = view.getFieldOfView();

        Position centerPos = new Position(sector.getCentroid(), 0d);
        double zoom = extent.getRadius() / (fov.tanHalfAngle() * fov.cosHalfAngle());
        this.wwd.getView().goTo(centerPos, zoom);
    }

    protected LayerList getMatchingLayers(String uuid, WMSLayerCapabilities layer, WMSLayerStyle style)
    {
        if (WWUtil.isEmpty(uuid) || layer == null)
            return null;

        LayerList layers = null; // List is lazily constructed below.

        for (Layer wwjLayer : this.wwd.getModel().getLayers())
        {
            String s = wwjLayer.getStringValue(GeodataKey.UUID);
            if (s == null || !s.equalsIgnoreCase(uuid))
                continue;

            s = wwjLayer.getStringValue(AVKey.LAYER_NAMES);
            if (s == null || !s.equalsIgnoreCase(layer.getName()))
                continue;

            s = wwjLayer.getStringValue(AVKey.STYLE_NAMES);
            if (style != null ? (s == null || !s.equalsIgnoreCase(style.getName())) : s != null)
                continue;

            if (layers == null)
                layers = new LayerList();
            layers.add(wwjLayer);
        }

        return layers;
    }

    protected String makeWMSLayerDisplayName(WMSLayerCapabilities layer, WMSLayerStyle style)
    {
        return ResourceUtil.makeWMSLayerDisplayName(layer, style);
    }

    protected void addStyleLegendLayer(WMSLayerCapabilities layer, WMSLayerStyle style, Layer wwLayer)
    {
        Set<WMSLogoURL> legendURLs = style.getLegendURLs();
        if (legendURLs == null || legendURLs.isEmpty())
            return;

        WMSLogoURL[] array = legendURLs.toArray(new WMSLogoURL[legendURLs.size()]);
        if (array == null || array.length == 0)
            return;

        String legendHref = array[0].getOnlineResource().getHref();
        if (WWUtil.isEmpty(legendHref))
            return;

        String displayName = this.makeWMSLayerDisplayName(layer, style);
        LegendLayer.Legend legend = new LegendLayer.Legend(legendHref, displayName, wwLayer);

        if (!this.getLegendLayer().hasLegend(legend))
        {
            wwLayer.setValue(GeodataKey.LEGEND, legend);
            this.getLegendLayer().addLegend(legend);
            this.getLegendLayer().firePropertyChange(AVKey.LAYER, null, this.getLegendLayer());
        }
    }

    protected LegendLayer getLegendLayer()
    {
        if (this.legendLayer == null)
        {
            this.legendLayer = new LegendLayer();
            this.addWWLayer(this.legendLayer);
        }

        return this.legendLayer;
    }

    protected void addWWLayer(final Layer layer)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    addWWLayer(layer);
                }
            });
        }
        else
        {
            // Insert the layer into the layer list just after the RecordList layer.
            int lastTiledImageLayer = 0;
            LayerList layers = this.wwd.getModel().getLayers();
            for (Layer l : layers)
            {
                if (l instanceof RecordListLayer)
                    lastTiledImageLayer = layers.indexOf(l);
            }
            layers.add(lastTiledImageLayer + 1, layer);
        }
    }
}
