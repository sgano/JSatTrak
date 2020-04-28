/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id: LayerList.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface LayerList extends Iterable<Layer>
{
    int getLayerCount();

    int getIndex(Layer layer);

    Layer getLayer(int index);

    void setLayer(int index, Layer layer);

    void addLayer(int index, Layer layer);

    void addLayer(Layer layer);

    void addLayers(Collection<? extends Layer> c);

    void removeLayer(int index);

    void clearLayers();
}
