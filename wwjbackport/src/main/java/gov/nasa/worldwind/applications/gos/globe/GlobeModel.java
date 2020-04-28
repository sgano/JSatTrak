/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.globe;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.wms.*;

/**
 * @author dcollins
 * @version $Id: GlobeModel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public interface GlobeModel
{
    boolean hasWMSLayer(String uuid, WMSLayerCapabilities layer, WMSLayerStyle style);

    void addWMSLayer(String uuid, WMSCapabilities caps, WMSLayerCapabilities layer, WMSLayerStyle style);

    void removeWMSLayer(String uuid, WMSLayerCapabilities layer, WMSLayerStyle style);

    void moveViewTo(Sector sector);
}
