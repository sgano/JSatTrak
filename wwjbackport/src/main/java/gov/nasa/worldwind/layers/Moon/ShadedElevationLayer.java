/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Moon;

import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.WWXML;
import org.w3c.dom.Document;

/**
 * @author Patrick Murris
 * @version $Id: ShadedElevationLayer.java 11316 2009-05-26 23:06:47Z dcollins $
 */
public class ShadedElevationLayer extends BasicTiledImageLayer
{
    public ShadedElevationLayer()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Moon/ShadedElevationLayer.xml", null);
    }
}