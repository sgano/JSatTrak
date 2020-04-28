/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples.Hawaii;

import gov.nasa.worldwind.layers.SurfaceImageLayer;

import java.io.IOException;

/**
 * @author tag
 * @version $Id: HawaiianIslandsLayer.java 6914 2008-10-05 00:05:58Z tgaskins $
 */
public class HawaiianIslandsLayer extends SurfaceImageLayer
{
    public HawaiianIslandsLayer() throws IOException
    {
        this.setPickEnabled(false);
//        this.addImage("testData/Hawaii/Hawaii/imagery/hawaii-landsat-geo.png");
        this.addImage("testData/Hawaii/Kauai/imagery/kauai-niihau_lehua2-geo.png");
//        this.addImage("testData/Hawaii/Lanai/imagery/lanai-landsat-geo.png");
//        this.addImage("testData/Hawaii/Maui/imagery/landsat_maui-kahoolawe-geo.png");
//        this.addImage("testData/Hawaii/Molokai/imagery/molokai-landsat-geo.png");
//        this.addImage("testData/Hawaii/Oahu/imagery/oahu-landsat-geo.png");
    }

    @Override
    public String getName()
    {
        return "Hawaiian Islands";
    }
}
