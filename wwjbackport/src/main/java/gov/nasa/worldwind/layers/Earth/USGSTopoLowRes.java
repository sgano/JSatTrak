package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.WWXML;
import org.w3c.dom.Document;

/**
 * @author tag
 * @version $Id: USGSTopoLowRes.java 11346 2009-05-28 04:21:48Z dcollins $
 */
public class USGSTopoLowRes extends BasicTiledImageLayer
{
    public USGSTopoLowRes()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Earth/USGSTopoLowResLayer.xml", null);
    }
}
