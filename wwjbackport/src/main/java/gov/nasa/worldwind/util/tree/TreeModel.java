/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

/**
 * Contents of a {@link Tree}.
 * 
 * @author pabercrombie
 * @version $Id: TreeModel.java 14065 2010-11-02 00:24:16Z pabercrombie $
 */
public interface TreeModel
{
    /**
     * Get the root node of the tree.
     *
     * @return The root node.
     */
    TreeNode getRoot();

    /**
     * Get the tree that this model is attached to.
     *
     * @return Tree that is using the model.
     */
    Tree getTree();
}
