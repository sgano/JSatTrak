/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.avlist.AVKey;
import org.w3c.dom.Document;

/**
 * @author garakl
 * @version $Id: USDANAIPWMSImageLayer.java 11316 2009-08-04 23:00:00Z garakl $
 */

public class USDANAIPWMSImageLayer extends WMSTiledImageLayer
{
    public USDANAIPWMSImageLayer()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Earth/USDANAIPWMSImageLayer.xml", null);
    }

    public String toString()
    {
        Object o = this.getValue(AVKey.DISPLAY_NAME);
        return o != null ? (String) o : "USDA FSA Imagery";
    }
}

