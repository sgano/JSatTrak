package gov.nasa.worldwind.util;

import java.net.URL;
/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author lado
 * @version $Id: TileUrlBuilder   Jun 19, 2007  12:47:51 AM
 */
public interface TileUrlBuilder
{
        public URL getURL(Tile tile, String imageFormat) throws java.net.MalformedURLException;
}
