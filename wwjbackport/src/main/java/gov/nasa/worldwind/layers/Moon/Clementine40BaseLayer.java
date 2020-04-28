/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Moon;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.Logging;

/**
 * Base (one image) layer for Moon Clementine 40xx color dataset.
 * @author Patrick Murris
 * @version $Id: Clementine40BaseLayer.java 13978 2010-10-15 03:14:10Z tgaskins $
 */
public class Clementine40BaseLayer extends RenderableLayer
{
    public Clementine40BaseLayer()
    {
        this.setName(Logging.getMessage("layers.Moon.Clementine40BaseLayer.Name"));
        this.addRenderable(new SurfaceImage("http://worldwind28.arc.nasa.gov/public/moon40xxcolor_base.jpg",
            Sector.FULL_SPHERE));

        // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
        this.setPickEnabled(false);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Moon.Clementine40BaseLayer.Name");
    }
}
