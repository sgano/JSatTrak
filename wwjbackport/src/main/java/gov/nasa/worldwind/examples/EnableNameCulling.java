/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.examples.util.StatusLayer;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;

/**
 * @author jparsons
 * @version $Id$
 */
public class EnableNameCulling extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected StatusLayer layer;

        public AppFrame() throws IllegalAccessException, InstantiationException, ClassNotFoundException
        {
            super(false, true, false);

            //Remove scalebar for this example
            LayerList layers = this.getWwd().getModel().getLayers();
            for (Layer layer1 : layers)
            {
                if (layer1 instanceof PlaceNameLayer)
                    ((PlaceNameLayer)layer1).setCullNames(true);
            }
        }

    }

        public static void main(String[] args)
        {
            ApplicationTemplate.start("Culling Place Names", AppFrame.class);
        }

}