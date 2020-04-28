/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.view.orbit.FlatOrbitView;

import java.awt.*;

/**
 * Using the FlatGlobe, EarthFlat, FlatOrbitView.
 *
 * @author Patrick Murris
 * @version $Id: FlatWorld.java 12367 2009-07-21 18:38:02Z jterhorst $
 */
public class FlatWorld extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Change atmosphere SkyGradientLayer for SkyColorLayer
            LayerList layers = this.getWwd().getModel().getLayers();
            for(int i = 0; i < layers.size(); i++)
            {
                if(layers.get(i) instanceof SkyGradientLayer)
                    layers.set(i, new SkyColorLayer());
            }
            this.getLayerPanel().update(this.getWwd());

            // Add flat world projection control panel
            this.getLayerPanel().add(new FlatWorldPanel(this.getWwd()),  BorderLayout.SOUTH);
        }

    }

    public static void main(String[] args)
    {
        // Adjust configuration values before instanciation
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        Configuration.setValue(AVKey.VIEW_CLASS_NAME, FlatOrbitView.class.getName());
        ApplicationTemplate.start("World Wind Flat World", AppFrame.class);
    }
}
