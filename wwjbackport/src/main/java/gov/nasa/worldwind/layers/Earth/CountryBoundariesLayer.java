/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import org.w3c.dom.Document;

/**
 * @author tag
 * @version $Id: CountryBoundariesLayer.java 11316 2009-05-26 23:06:47Z dcollins $
 */
public class CountryBoundariesLayer extends WMSTiledImageLayer
{
    public CountryBoundariesLayer()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Earth/CountryBoundariesLayer.xml", null);
    }
}
