/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.features;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwindow.core.*;

/**
 * @author tag
 * @version $Id: Crosshair.java 13273 2010-04-10 09:18:33Z tgaskins $
 */
public class Crosshair extends AbstractOnDemandLayerFeature
{
    public Crosshair(Registry registry)
    {
        super("Crosshair", Constants.FEATURE_CROSSHAIR, null, null, registry);
    }

    @Override
    protected Layer createLayer()
    {
        Layer layer = this.doCreateLayer();

        layer.setPickEnabled(false);

        return layer;
    }

    protected Layer doCreateLayer()
    {
        return new CrosshairLayer();
    }

    @Override
    public void turnOn(boolean tf)
    {
        if (tf == this.on || this.layer == null)
            return;

        if (tf)
            controller.addInternalActiveLayer(this.layer);
        else
            this.controller.getActiveLayers().remove(this.layer);

        this.on = tf;
    }
}
