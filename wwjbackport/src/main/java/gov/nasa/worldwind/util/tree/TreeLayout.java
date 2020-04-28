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
 * Handles rendering a {@link Tree}. The layout is responsible for the overall arrangement of the tree. It will
 * typically delegate to a {@link gov.nasa.worldwind.util.tree.TreeNodeRenderer} to actually draw the nodes.
 *
 * @author pabercrombie
 * @version $Id: TreeLayout.java 14062 2010-11-01 01:51:13Z pabercrombie $
 * @see Tree
 * @see gov.nasa.worldwind.util.tree.TreeNodeRenderer
 */
public interface TreeLayout extends Renderable
{
    /**
     * Render a tree.
     *
     * @param dc Draw context to draw in.
     */
    void render(DrawContext dc);
   
    /**
     * Set the tree attributes.
     *
     * @param attributes New attributes.
     * @see #getAttributes()
     */
    void setAttributes(TreeAttributes attributes);

    /**
     * Get the tree attributes.
     *
     * @return Tree attributes.
     * @see #setAttributes(TreeAttributes)
     */
    TreeAttributes getAttributes();

    /**
     * Make a node in the tree visible in the rendered tree. For example, scroll the tree viewport so that a path is
     * visible.
     *
     * @param path Path to make visible.
     */
    void makeVisible(TreePath path);
}
