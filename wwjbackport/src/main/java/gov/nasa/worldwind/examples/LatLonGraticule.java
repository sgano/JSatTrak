/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.layers.LatLonGraticuleLayer;

/**
 * Lat-Lon graticule layer.
 *
 * @author Patrick Murris
 * @version $Id: LatLonGraticule.java 11758 2009-06-19 10:02:02Z patrickmurris $
 */
public class LatLonGraticule extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Add the graticule layer
            insertBeforePlacenames(getWwd(), new LatLonGraticuleLayer());

            // Update layer panel
            this.getLayerPanel().update(this.getWwd());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Lat-Lon Graticule", AppFrame.class);
    }
}