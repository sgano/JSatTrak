/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Mars.*;

import java.awt.*;

/**
 * Using Moon components.
 * 
 * @author Patrick Murris
 * @version $Id: Mars.java 13978 2010-10-15 03:14:10Z tgaskins $
 */
public class Mars extends ApplicationTemplate
{
    private static final String LAYERS = "gov.nasa.worldwind.layers.StarsLayer"
            + ",gov.nasa.worldwind.layers.SkyGradientLayer"
            + ",gov.nasa.worldwind.layers.Mars.MOCColorizedBaseLayer"
            + ",gov.nasa.worldwind.layers.Mars.MOCLayer"
            + ",gov.nasa.worldwind.layers.Mars.MOCColorizedLayer"
            + ",gov.nasa.worldwind.layers.Mars.MolaColorASULayer"
            + ",gov.nasa.worldwind.layers.Mars.MolaColoredJPLLayer"
            + ",gov.nasa.worldwind.layers.Mars.MDIMLayer"
            + ",gov.nasa.worldwind.layers.Mars.THEMISLayer"
            + ",gov.nasa.worldwind.layers.Mars.THEMISColorLayer"
            + ",gov.nasa.worldwind.layers.ScalebarLayer"
            + ",gov.nasa.worldwind.layers.CompassLayer";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Adjust sky layer
            LayerList layers = this.getWwd().getModel().getLayers();
            for (Layer layer : layers)
            {
                if(layer instanceof SkyGradientLayer)
                {
                    SkyGradientLayer sky = (SkyGradientLayer) layer;
                    sky.setHorizonColor(new Color(236, 223, 174));
                    sky.setZenithColor(new Color(183, 117, 3));
                    sky.setAtmosphereThickness(40000);
                }
                else if(layer instanceof MOCLayer
                        || layer instanceof MolaColorASULayer
                        || layer instanceof MolaColoredJPLLayer
                        || layer instanceof THEMISLayer
                        || layer instanceof MDIMLayer)
                {
                    layer.setEnabled(false);
                }
                else if(layer instanceof THEMISColorLayer)
                {
                    layer.setOpacity(.7);
                    layer.setEnabled(false);
                }
            }
            // Update layer panel
            this.getLayerPanel().update(this.getWwd());
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, gov.nasa.worldwind.globes.Mars.class.getName());
        Configuration.setValue(AVKey.MARS_ELEVATION_MODEL_CONFIG_FILE, "config/Mars/MarsElevationModel.xml");
        Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, LAYERS);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 10000e3);  // 10000km
        ApplicationTemplate.start("World Wind Mars", AppFrame.class);
    }
}