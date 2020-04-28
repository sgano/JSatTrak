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

public class OSMCycleMapLayer extends BasicMercatorTiledImageLayer
{
	public OSMCycleMapLayer()
	{
		super(makeLevels());
    }

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 256);
		params.setValue(AVKey.TILE_HEIGHT, 256);
		params.setValue(AVKey.DATA_CACHE_NAME, "Earth/OSM-Mercator/OpenStreetMap Cycle");
		params.setValue(AVKey.SERVICE, "http://b.andy.sandbox.cloudmade.com/tiles/cycle/");
		params.setValue(AVKey.DATASET_NAME, "*");
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
	public String toString()
	{
		return "OpenStreetMap Cycle";
	}
}
