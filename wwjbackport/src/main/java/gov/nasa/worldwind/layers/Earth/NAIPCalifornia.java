/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import org.w3c.dom.Document;

/**
 * @author tag
 * @version $Id: NAIPCalifornia.java 11316 2009-05-26 23:06:47Z dcollins $
 * @deprecated Replaced by {@link NAIPCaliforniaWMS}.
 */
public class NAIPCalifornia extends WMSTiledImageLayer
{
    public NAIPCalifornia()
    {
        super(getConfigurationDocument(), null);

        this.setName(Logging.getMessage("layers.Earth.NAIP.California.Name"));
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Earth/NAIPCaliforniaWMSLayer.xml", null);
    }
}
