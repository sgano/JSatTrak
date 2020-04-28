/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Mercator.examples;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.layers.Mercator.*;

import java.awt.image.*;
import java.net.*;

public class OSMMapnikTransparentLayer extends BasicMercatorTiledImageLayer
{
	public OSMMapnikTransparentLayer()
	{
		super(makeLevels());
    }

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 256);
		params.setValue(AVKey.TILE_HEIGHT, 256);
		params.setValue(AVKey.DATA_CACHE_NAME, "Earth/OSM-Mercator/OpenStreetMap Mapnik Transparent");
		params.setValue(AVKey.SERVICE, "http://a.tile.openstreetmap.org/");
		params.setValue(AVKey.DATASET_NAME, "h");
		params.setValue(AVKey.FORMAT_SUFFIX, ".png");
		params.setValue(AVKey.NUM_LEVELS, 16);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(22.5d), Angle.fromDegrees(45d)));
		params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0,
				Angle.NEG180, Angle.POS180));
		params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());

		return new LevelSet(params);
	}

	private static class URLBuilder implements TileUrlBuilder
	{
		public URL getURL(Tile tile, String imageFormat)
				throws MalformedURLException
		{
			return new URL(tile.getLevel().getService()
					+ (tile.getLevelNumber() + 3) +"/"+ tile.getColumn()+"/"+ ((1 << (tile.getLevelNumber()) + 3) - 1 - tile.getRow()) + ".png");
		}
	}

    @Override
    protected BufferedImage modifyImage(BufferedImage image)
    {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image
                .getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++)
                newImage.setRGB(x, y, convertTransparent(image.getRGB(x, y)));
        return newImage;
    }

    private int convertTransparent(int rgb)
    {
        if (rgb == -921880 || rgb == -4861744)
            return rgb & 0xffffff;
        return rgb;
    }


    @Override
	public String toString()
	{
		return "OpenStreetMap Mapnik";
	}
}
