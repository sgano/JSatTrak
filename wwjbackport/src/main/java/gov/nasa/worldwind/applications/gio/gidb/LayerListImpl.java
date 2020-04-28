/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: LayerListImpl.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class LayerListImpl implements LayerList
{
    private List<Layer> layerList;

    public LayerListImpl()
    {
        this.layerList = new ArrayList<Layer>();
    }

    public int getLayerCount()
    {
        return this.layerList.size();
    }

    public int getIndex(Layer layer)
    {
        return this.layerList.indexOf(layer);
    }

    public Layer getLayer(int index)
    {
        if (index < 0 || index >= this.layerList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.layerList.get(index);
    }

    public void setLayer(int index, Layer layer)
    {
        if (index < 0 || index >= this.layerList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.layerList.set(index, layer);
    }

    public void addLayer(int index, Layer layer)
    {
        if (index < 0 || index > this.layerList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.layerList.add(index, layer);
    }

    public void addLayer(Layer layer)
    {
        this.layerList.add(layer);
    }

    public void addLayers(Collection<? extends Layer> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullLayer.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.layerList.addAll(c);
    }

    public void removeLayer(int index)
    {
        if (index < 0 || index >= this.layerList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.layerList.remove(index);
    }

    public void clearLayers()
    {
        this.layerList.clear();
    }

    public Iterator<Layer> iterator()
    {
        return this.layerList.iterator();
    }
}
