/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Mercator.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;
import gov.nasa.worldwind.layers.Mercator.*;

import java.net.MalformedURLException;
import java.net.URL;

public class YahooMapsLayer extends BasicMercatorTiledImageLayer
{
	public YahooMapsLayer()
	{
		super(makeLevels());
		setSplitScale(1.3);
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 256);
		params.setValue(AVKey.TILE_HEIGHT, 256);
		params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Yahoo Maps Mercator");
		params.setValue(AVKey.SERVICE, "http://maps.yimg.com/cv/img"); 
		params.setValue(AVKey.DATASET_NAME, "a");
		params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
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
			int row = tile.getRow()
					- (int) Math.pow(2, (tile.getLevelNumber() + 2));
			return new URL(tile.getLevel().getService() + "?md=200705152300"
					+ "&x=" + tile.getColumn() + "&y=" + row + "&z="
					+ (15 - tile.getLevelNumber()) + "&v=1.7&t=a");
		}
	}

	@Override
	public String toString()
	{
		return "Yahoo Maps";
	}
}
