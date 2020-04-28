/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.render.*;

import java.awt.*;

/**
 * Draws nodes of a {@link Tree}. A {@link TreeLayout} is responsible for the overall layout and rendering of a tree.
 * The layout will invoke a TreeNodeRenderer to draw each node.
 *
 * @author pabercrombie
 * @version $Id: TreeNodeRenderer.java 14065 2010-11-02 00:24:16Z pabercrombie $
 * @see TreeLayout
 */
public interface TreeNodeRenderer
{
    /**
     * Get the node's preferred size.
     *
     * @param dc         Draw context.
     * @param node       Node to find size of.
     * @param attributes Attributes that control the display of the tree.
     *
     * @return The size that the renderer prefers to draw the node.
     */
    Dimension getPreferredSize(DrawContext dc, TreeNode node, TreeAttributes attributes);

    /**
     * Draw a node.
     *
     * @param dc         Draw context.
     * @param tree       Tree that is being rendered.
     * @param node       Node to draw.
     * @param attributes Attributes that control the display of the tree.
     * @param bounds     Node bounds. The renderer should not draw outside of this rectangle.
     */
    void render(DrawContext dc, Tree tree, TreeNode node, TreeAttributes attributes, Rectangle bounds);
}
