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
 * @version $Id: USGSUrbanAreaOrtho.java 11346 2009-05-28 04:21:48Z dcollins $
 */
public class USGSUrbanAreaOrtho extends BasicTiledImageLayer
{
    public USGSUrbanAreaOrtho()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Earth/USGSUrbanAreaOrthoLayer.xml", null);
    }
}
