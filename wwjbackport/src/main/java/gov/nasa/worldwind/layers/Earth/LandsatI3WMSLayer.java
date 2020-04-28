package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import org.w3c.dom.Document;

/**
 * @author tag
 * @version $Id: LandsatI3WMSLayer.java 11316 2009-05-26 23:06:47Z dcollins $
 */
public class LandsatI3WMSLayer extends WMSTiledImageLayer
{
    public LandsatI3WMSLayer()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Earth/LandsatI3WMSLayer.xml", null);
    }
}
