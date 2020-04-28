/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.examples.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.tree.*;

/**
 * A tree node that represents a layer. The layer will be enabled or disabled when the node is selected or deselected.
 * 
 * @author pabercrombie
 * @version $Id $
 */
public class LayerTreeNode extends BasicTreeNode
{
    public static final String DEFAULT_ICON_PATH = "images/16x16-icon-earth.png";

    protected Layer layer;

    /**
     * Create a new layer node.
     *
     * @param model The tree model that contains this node.
     * @param layer The layer represented by this node.
     */
    public LayerTreeNode(BasicTreeModel model, Layer layer)
    {
        super(model, layer.getName());
        this.layer = layer;

        Object icon = layer.getValue(AVKey.IMAGE);
        if (icon == null)
            icon = DEFAULT_ICON_PATH;
        this.setImageSource(icon);
    }

    /** {@inheritDoc} */
    @Override
    public String getText()
    {
        return this.layer.getName();
    }

    /**
     * Is the layer enabled?
     *
     * @return True if the layer is enabled.
     */
    @Override
    public boolean isSelected()
    {
        return this.layer.isEnabled();
    }

    /**
     * The layer will be enabled or disabled when the node selection is changed.
     *
     * @param selected True if the node is selected (and the layer should be enabled).
     */
    @Override
    public void setSelected(boolean selected)
    {
        super.setSelected(selected);
        this.layer.setEnabled(selected);
    }
}
