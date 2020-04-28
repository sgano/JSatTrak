/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.catalogui.AVListNode;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: GIDBResultNode.java 5551 2008-07-18 23:50:47Z dcollins $
 */
public class GIDBResultNode extends AVListNode<GIDBResultModel>
{
    public GIDBResultNode(GIDBResultModel resultModel)
    {
        super(resultModel);

        setAllowsChildren(true);
        setLeaf(false);
        setSortChildren(true);
        setSortKey(CatalogKey.TITLE);
        update();
    }

    public void update()
    {
        LayerList layerList = getObject().getLayerList();
        if (layerList != null)
            setLayers(layerList);
    }

    protected void setLayers(Iterable<Layer> iterable)
    {
        List<AVListNode> children = new ArrayList<AVListNode>();
        for (Layer layer : iterable)
            if (layer != null)
                children.add(new LayerNode(layer));
        doSetChildren(children.size() > 0 ? children : null);
    }
}
