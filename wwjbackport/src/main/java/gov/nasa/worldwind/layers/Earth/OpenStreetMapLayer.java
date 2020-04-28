/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import org.w3c.dom.Document;

/**
 * OpenStreetMap WMS layer(s).
 * @author Patrick Murris
 * @version $Id: OpenStreetMapLayer.java 11327 2009-05-27 17:42:09Z dcollins $
 */
public class OpenStreetMapLayer extends WMSTiledImageLayer
{
    protected static final String defaultDatasetName = "osm-4326-hybrid";

    /**
     * Default OpenStreetMap hybrid layer - transparent, see-through.
     */
    public OpenStreetMapLayer()
    {
        super(getHybridConfigurationDocument(), null);
    }

    /**
     * Access to a specific layer from OSM WMS server - eg 'osm-4326'.
     * @param datasetName the layer dataset name.
     */
    public OpenStreetMapLayer(String datasetName)
    {
        super(createConfigurationDocument(datasetName), null);
    }

    protected static Document getHybridConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Earth/OpenStreetMapHybridLayer.xml", null);
    }

    protected static Document createConfigurationDocument(String layerName)
    {
        String configurationXml =
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Layer version=\"1\" layerType=\"TiledImageLayer\">"
            + "    <DisplayName>OpenStreetMap " + layerName + "</DisplayName>"
            + "    <Service serviceName=\"OGC:WMS\" version=\"1.1.1\">"
            + "        <GetCapabilitiesURL>http://t2.hypercube.telascience.org/tiles</GetCapabilitiesURL>"
            + "        <GetMapURL>http://t2.hypercube.telascience.org/tiles</GetMapURL>"
            + "        <LayerNames>" + layerName + "</LayerNames>"
            + "    </Service>"
            + "    <!-- Wed, 25 Feb 2009 00:00:00 PDT -->"
            + "    <LastUpdate>1235548800000</LastUpdate>"
            + "    <DataCacheName>Earth/OSM-WMS/" + layerName + "</DataCacheName>"
            + "    <ImageFormat>image/png</ImageFormat>"
            + "    <AvailableImageFormats>"
            + "        <ImageFormat>image/png</ImageFormat>"
            + "    </AvailableImageFormats>"
            + "    <FormatSuffix>.dds</FormatSuffix>"
            + "    <NumLevels count=\"20\" numEmpty=\"0\"/>"
            + "    <TileOrigin>"
            + "        <LatLon units=\"degrees\" latitude=\"-90\" longitude=\"-180\"/>"
            + "    </TileOrigin>"
            + "    <LevelZeroTileDelta>"
            + "        <LatLon units=\"degrees\" latitude=\"180\" longitude=\"180\"/>"
            + "    </LevelZeroTileDelta>"
            + "    <TileSize>"
            + "        <Dimension width=\"256\" height=\"256\"/>"
            + "    </TileSize>"
            + "    <Sector>"
            + "        <SouthWest>"
            + "            <LatLon units=\"degrees\" latitude=\"-90\" longitude=\"-180\"/>"
            + "        </SouthWest>"
            + "        <NorthEast>"
            + "            <LatLon units=\"degrees\" latitude=\"90\" longitude=\"180\"/>"
            + "        </NorthEast>"
            + "    </Sector>"
            + "    <UseTransparentTextures>true</UseTransparentTextures>"
            + "</Layer>";

        java.io.InputStream inputStream = WWIO.getInputStreamFromString(configurationXml);
        return WWXML.openDocumentStream(inputStream);
    }
}
