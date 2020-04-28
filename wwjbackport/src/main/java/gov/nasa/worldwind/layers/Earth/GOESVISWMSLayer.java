/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;

import java.util.*;

/**
 * @author jparsons
 * @version $Id$
 */
public class GOESVISWMSLayer extends WMSTiledImageLayer
{
    private static final String xmlState;

    static
    {
        long expiryTime = new GregorianCalendar().getTimeInMillis();

        xmlState = new String(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<restorableState>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.NumEmptyLevels\">0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.ImageFormat\">image/png</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.DataCacheNameKey\">Earth/IEM GOES VIS</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.ServiceURLKey\">http://mesonet.agron.iastate.edu/cgi-bin/wms/goes/conus_vis.cgi?</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.Title\">IEM GOES CONUS VIS</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.NumLevels\">5</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.FormatSuffixKey\">.png</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.LevelZeroTileDelta.Latitude\">36.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.LevelZeroTileDelta.Longitude\">36.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.DatasetNameKey\">goes_conus_visible</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.TileHeightKey\">512</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.TileWidthKey\">512</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.LayerNames\">goes_conus_vis</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MinLatitude\">24.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MaxLatitude\">50.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MinLongitude\">-126.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MaxLongitude\">-66.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.ExpiryTime\">" + expiryTime + "</stateObject>"
                + "<stateObject name=\"Layer.Name\">goes_conus_vis</stateObject>"
                + "<stateObject name=\"Layer.Enabled\">true</stateObject>"
                + "<stateObject name=\"TiledImageLayer.UseTransparentTextures\">false</stateObject>"
                + "<stateObject name=\"wms.Version\">1.1.1</stateObject>"
                + "<stateObject name=\"wms.Crs\">&amp;srs=EPSG:4326</stateObject>"
                + "</restorableState>"
        );
    }

    public GOESVISWMSLayer()
    {
        super(xmlState);

        this.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        this.setValue(AVKey.DISPLAY_NAME, String.format("IEM GOES CONUS VIS"));
        this.setAvailableImageFormats(new String[] {"image/png" });
    }

    @Override
    public String toString()
    {
         return "gov.nasa.worldwind.layers.Earth.GOESVISWMSLayer";
    }
}
