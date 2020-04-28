/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.examples.util.StatusLayer;
import gov.nasa.worldwind.layers.*;

/**
 * @author jparsons
 * @version $Id$
 */
public class StatusApp extends ApplicationTemplate
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
                if (layer1 instanceof ScalebarLayer)
                    layer1.setEnabled(false);
            }

            this.layer = new StatusLayer();
            //this.layer = new StatusMGRSLayer();
            //this.layer = new StatusUTMLayer();

            layer.setEventSource(this.getWwd());
            layer.setCoordDecimalPlaces(2);  // default is 4
            //layer.setElevationUnits(StatusLayer.UNIT_IMPERIAL);
            insertBeforeCompass(this.getWwd(), layer);
        }

    }

        public static void main(String[] args)
        {
            ApplicationTemplate.start("StatusLayer", AppFrame.class);
        }

}
