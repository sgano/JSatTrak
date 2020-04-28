/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.features;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindow.core.Registry;
import gov.nasa.worldwindow.core.layermanager.LayerPath;

/**
 * @author tag
 * @version $Id: GraticuleLayer.java 13266 2010-04-10 02:47:09Z tgaskins $
 */
public abstract class GraticuleLayer extends AbstractOnDemandLayerFeature
{
    protected abstract Layer doCreateLayer();

    public GraticuleLayer(String name, String featureID, String iconPath, String group, Registry registry)
    {
        super(name, featureID, iconPath, group, registry);
    }

    @Override
    protected Layer createLayer()
    {
        Layer layer = this.doCreateLayer();

        layer.setPickEnabled(false);

        return layer;
    }

    @Override
    protected void addLayer(LayerPath path)
    {
        controller.addInternalActiveLayer(this.layer);
    }

    @Override
    protected void removeLayer()
    {
        this.controller.getWWPanel().removeLayer(this.layer);
    }
}