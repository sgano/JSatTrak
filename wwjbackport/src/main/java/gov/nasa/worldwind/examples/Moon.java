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
import gov.nasa.worldwind.layers.Moon.*;

/**
 * Using Moon components.
 *
 * @author Patrick Murris
 * @version $Id: Moon.java 13978 2010-10-15 03:14:10Z tgaskins $
 */
public class Moon extends ApplicationTemplate
{
    private static final String LAYERS = "gov.nasa.worldwind.layers.StarsLayer"
            + ",gov.nasa.worldwind.layers.Moon.Clementine40BaseLayer"
            + ",gov.nasa.worldwind.layers.Moon.Clementine40Layer"
            + ",gov.nasa.worldwind.layers.Moon.Clementine30Layer"
            + ",gov.nasa.worldwind.layers.Moon.ShadedElevationLayer"
            + ",gov.nasa.worldwind.layers.ScalebarLayer"
            + ",gov.nasa.worldwind.layers.CompassLayer";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Adjust layers states
            LayerList layers = this.getWwd().getModel().getLayers();
            for (Layer layer : layers)
            {
                if(layer instanceof Clementine30Layer || layer instanceof ShadedElevationLayer)
                    layer.setEnabled(false);
            }
            // Update layer panel
            this.getLayerPanel().update(this.getWwd());
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, gov.nasa.worldwind.globes.Moon.class.getName());
        Configuration.setValue(AVKey.MOON_ELEVATION_MODEL_CONFIG_FILE, "config/Moon/MoonElevationModel.xml");
        Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, LAYERS);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 6000e3);  // 6000km
        ApplicationTemplate.start("World Wind Moon", AppFrame.class);
    }
}