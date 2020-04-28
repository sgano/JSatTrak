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
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;
import gov.nasa.worldwind.layers.Mercator.*;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

public class VirtualEarthLayer extends BasicMercatorTiledImageLayer
{
	public static enum Dataset
	{
		AERIAL("Aerial", "a", ".jpg"),
		HYBRID("Hybrid", "h", ".jpg"),
		ROAD("Road", "r", ".png");

		public final String label;
		public final String dataset;
		public final String formatSuffix;

		private Dataset(String label, String dataset, String formatSuffix)
		{
			this.label = label;
			this.dataset = dataset;
			this.formatSuffix = formatSuffix;
		}
	}

	private VirtualEarthLogo logo = new VirtualEarthLogo();
	private final Dataset dataset;

	public VirtualEarthLayer()
	{
		this(Dataset.HYBRID);
	}

	public VirtualEarthLayer(Dataset dataset)
	{
		super(makeLevels(dataset));
		if (dataset == null)
			throw new NullPointerException("Dataset cannot be null");
		this.dataset = dataset;
		this.setValue(AVKey.DISPLAY_NAME, "Microsoft Virtual Earth "
				+ dataset.label);
	}

	protected static LevelSet makeLevels(Dataset dataset)
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 256);
		params.setValue(AVKey.TILE_HEIGHT, 256);
		params.setValue(AVKey.DATA_CACHE_NAME, "Earth/MS Virtual Earth Mercator/MSVE "
				+ dataset.label);
		params.setValue(AVKey.SERVICE,
				"http://a0.ortho.tiles.virtualearth.net/tiles/");
		params.setValue(AVKey.DATASET_NAME, dataset.dataset);
		params.setValue(AVKey.FORMAT_SUFFIX, dataset.formatSuffix);
		params.setValue(AVKey.NUM_LEVELS, 16);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(22.5d), Angle.fromDegrees(45d)));
		params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0,
				Angle.NEG180, Angle.POS180));
		params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());
		params.setValue(AVKey.DISPLAY_NAME, "Microsoft Virtual Earth "
				+ dataset.label);

		return new LevelSet(params);
	}

	private static class URLBuilder implements TileUrlBuilder
	{
		public URL getURL(Tile tile, String imageFormat)
				throws MalformedURLException
		{
			String quadkey = tileToQuadKey(tile.getColumn(), tile.getRow(),
					tile.getLevelNumber() + 2);
			return new URL(tile.getLevel().getService()
					+ tile.getLevel().getDataset() + quadkey + ".jpeg?g=1");
		}
	}

	protected static String tileToQuadKey(int col, int row, int level)
	{
		String quad = "";
		for (int i = level; i >= 0; i--)
		{
			int mask = 1 << i;
			int cell = 0;
			if ((col & mask) != 0)
			{
				cell++;
			}
			if ((row & mask) == 0)
			{
				cell += 2;
			}
			quad += cell;
		}
		return quad;
	}

	@Override
	public void render(DrawContext dc)
	{
		super.render(dc);
		if (isEnabled())
		{
			dc.addOrderedRenderable(logo);
		}
	}

	@Override
	protected boolean isTileValid(BufferedImage image)
	{
		//return false if the tile is white (this will mark the tile as absent)
		boolean white = true;
		//JPEG compression will cause white to be not quite white
		String lowercaseFormat = getDataset().formatSuffix.toLowerCase();
		int threshold = lowercaseFormat.contains("jpg")
				|| lowercaseFormat.contains("jpeg") ? 200 : 250;
		for (int x = 0; x < image.getWidth(); x++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				int rgb = image.getRGB(x, y);
				white = isWhite(rgb, threshold);
				if (!white)
					break;
			}
			if (!white)
				break;
		}
		return !white;
	}

	private boolean isWhite(int rgb, int threshold)
	{
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = (rgb >> 0) & 0xff;
		return r + b + g > threshold * 3;
	}

	public Dataset getDataset()
	{
		return dataset;
	}
}
