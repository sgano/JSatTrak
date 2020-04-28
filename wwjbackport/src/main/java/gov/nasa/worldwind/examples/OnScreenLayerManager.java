/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.examples.util.LayerManagerLayer;

/**
 * Test on screen layer manager.
 *
 * @author Patrick Murris
 * @version $Id: OnScreenLayerManager.java 9020 2009-02-26 19:42:10Z patrickmurris $
 */
public class OnScreenLayerManager extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {

        public AppFrame()
        {
            super(true, false, false);

            // Add the layer manager layer to the model layer list
            getWwd().getModel().getLayers().add(new LayerManagerLayer(getWwd()));
        }

    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind On-Screen Layer Manager", AppFrame.class);
    }
}
