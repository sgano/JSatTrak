/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;

/**
 * Surface images examples - local and remote.
 * 
 * @author Patrick Murris
 * @version $Id: RemoteSurfaceImage.java 8315 2009-01-02 06:57:35Z tgaskins $
 */
public class RemoteSurfaceImage extends ApplicationTemplate
{
    // Global base images at:
    // http://worldwind28.arc.nasa.gov/public/      (earth BM and BMNG)
    // http://worldwind25.arc.nasa.gov/baseImages/  (other planets and moons)
    private static final String MARS_MOC_COLOR_URL = "http://worldwind25.arc.nasa.gov/baseImages/mocC256_base.jpg";
    private static final String JUPITER_BASE_URL = "http://worldwind25.arc.nasa.gov/baseImages/jupiter_2048x1024.jpg";

    // From NASA Visible Earth http://visibleearth.nasa.gov/view_rec.php?id=1438
    private static final String EARTH_NIGHT_LIGHTS_URL = "http://veimages.gsfc.nasa.gov/1438/earth_lights_lrg.jpg";

    // Naval Research Lab archived and latest weather datasets, some global other local:
    // http://www.nrlmry.navy.mil/archdat/
    private static final String NRL_BM_DAY_NIGHT_STITCHED = "http://www.nrlmry.navy.mil/archdat/global/stitched/day_night_bm/20080425.1800.multisat.visir.bckgr.Global_Global_bm.DAYNGT.jpg";

    // Latest global cloud cover, updated every 3 hours:
    // http://worldwind25.arc.nasa.gov/GlobalClouds/GlobalClouds.aspx
    // http://xplanet.sourceforge.net/clouds/clouds_2048.jpg
    private static final String GLOBAL_CLOUDS = "http://worldwind25.arc.nasa.gov/GlobalClouds/GlobalClouds.aspx";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);
            
            SurfaceImage si;
            RenderableLayer layer;
            LayerList layers = this.getWwd().getModel().getLayers();
            try
            {
                // TODO: Re-enable after implementing image retrieval
//                // clear all layers
//                layers.clear();
//
//                // Add stars and sky gradient layer
//                layers.add(new StarsLayer());
//                layers.add(new SkyGradientLayer());
//
//                // Local surface image - equivalent to BMNGOneImage layer
//                layer = new RenderableLayer();
//                String path = Configuration.getStringValue(AVKey.BMNG_ONE_IMAGE_PATH);
//                si = new SurfaceImage(path, Sector.FULL_SPHERE);
//                layer.setName("Local Surface Image");
//                layer.addRenderable(si);
//                layer.setPickEnabled(false);
//                layers.add(layer);
//
//                // Remote surface image
//                layer = new RenderableLayer();
//                si = new SurfaceImage(EARTH_NIGHT_LIGHTS_URL, Sector.FULL_SPHERE, layer);
//                //si = new SurfaceImage(NRL_BM_DAY_NIGHT_STITCHED, Sector.fromDegrees(-65.12525, 64.87212, -180, 180), layer);
//                layer.setName("Remote Surface Image");
//                layer.addRenderable(si);
//                layer.setPickEnabled(false);
//                layer.setOpacity(.5);
//                layers.add(layer);
//
//                // Update the layer panel
//                this.getLayerPanel().update(this.getWwd());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Remote Surface Image", RemoteSurfaceImage.AppFrame.class);
    }
}
