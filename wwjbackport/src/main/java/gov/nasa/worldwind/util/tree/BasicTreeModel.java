/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

/**
 * Basic implementation of a {@link TreeModel}.
 *
 * @author pabercrombie
 * @version $Id: BasicTreeModel.java 14032 2010-10-24 23:19:13Z pabercrombie $
 */
public class BasicTreeModel implements TreeModel
{
    protected BasicTree tree;

    /** The root node. */
    protected TreeNode root;

    /**
     * Create a new tree model.
     *
     * @param tree The tree that is using the model.
     */
    public BasicTreeModel(BasicTree tree)
    {
        this.tree = tree;
    }

    /**
     * Create a tree model with a root node.
     *
     * @param tree The tree that is using the model.
     * @param root The root node.
     */
    public BasicTreeModel(BasicTree tree, TreeNode root)
    {
        this.tree = tree;
        this.setRoot(root);
    }

    /** {@inheritDoc} */
    public TreeNode getRoot()
    {
        return this.root;
    }

    /**
     * Set the root node.
     *
     * @param root New root.
     */
    public void setRoot(TreeNode root)
    {
        this.root = root;
    }

    public BasicTree getTree()
    {
        return this.tree;
    }
}
