/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.WWXML;
import org.w3c.dom.Document;

/**
 * @author tag
 * @version $Id: LandsatI3.java 11316 2009-05-26 23:06:47Z dcollins $
 * @deprecated Replaced by {@link LandsatI3WMSLayer}.
 */
public class LandsatI3 extends BasicTiledImageLayer
{
    public LandsatI3()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Earth/LegacyLandsatI3Layer.xml", null);
    }
}
