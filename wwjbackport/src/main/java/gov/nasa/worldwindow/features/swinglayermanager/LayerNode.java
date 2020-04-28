/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.features.swinglayermanager;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindow.core.WMSLayerInfo;

/**
 * @author tag
 * @version $Id: LayerNode.java 13798 2010-09-15 04:17:45Z tgaskins $
 */
public interface LayerNode
{
    Object getID();

    String getTitle();

    void setTitle(String title);

    Layer getLayer();

    void setLayer(Layer layer);

    boolean isSelected();

    void setSelected(boolean selected);

    WMSLayerInfo getWmsLayerInfo();

    String getToolTipText();

    void setToolTipText(String toolTipText);

    void setEnableSelectionBox(boolean tf);

    boolean isEnableSelectionBox();

    void setAllowsChildren(boolean tf);
}
