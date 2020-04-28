/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Basic implementation of a {@link Tree} control.
 *
 * @author pabercrombie
 * @version $Id: BasicTree.java 14065 2010-11-02 00:24:16Z pabercrombie $
 */
public class BasicTree extends AVListImpl implements Tree
{
    protected TreeLayout layout;

    protected TreeModel model;

    protected List<TreeListener> listeners = new CopyOnWriteArrayList<TreeListener>();

    protected Set<TreePath> expandedNodes = new HashSet<TreePath>();

    /** Create an empty tree. */
    public BasicTree()
    {
    }

    /** {@inheritDoc} */
    public void makeVisible(TreePath path)
    {
        TreeLayout layout = this.getLayout();
        if (layout != null)
            layout.makeVisible(path);
    }

    /** {@inheritDoc} */
    public void expandPath(TreePath path)
    {
        this.expandedNodes.add(path);
        this.fireTreeEvent(new TreeEvent(this, this.getNode(path), TreeEvent.PATH_EXPANDED));
    }

    /** {@inheritDoc} */
    public void collapsePath(TreePath path)
    {
        this.expandedNodes.remove(path);
        this.fireTreeEvent(new TreeEvent(this, this.getNode(path), TreeEvent.PATH_COLLAPSED));
    }

    /** {@inheritDoc} */
    public TreeNode getNode(TreePath path)
    {
        TreeNode node = this.getModel().getRoot();
        if (!node.getText().equals(path.get(0))) // Test root node
            return null;

        Iterator<String> iterator = path.iterator();
        iterator.next(); // Skip root node, we already tested it above
        while (iterator.hasNext())
        {
            String nodeText = iterator.next();
            boolean foundMatch = false;
            for (TreeNode child : node.getChildren())
            {
                if (child.getText().equals(nodeText))
                {
                    node = child;
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch)
                return null;
        }
        return node;
    }

    /** {@inheritDoc} */
    public void togglePath(TreePath path)
    {
        if (this.isPathExpanded(path))
            this.collapsePath(path);
        else
            this.expandPath(path);
    }

    /** {@inheritDoc} */
    public boolean isPathExpanded(TreePath path)
    {
        return this.expandedNodes.contains(path);
    }

    /** {@inheritDoc} */
    public boolean isNodeExpanded(TreeNode node)
    {
        return this.expandedNodes.contains(node.getPath());
    }

    /** {@inheritDoc} */
    public void addTreeListener(TreeListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        listeners.add(listener);
    }

    /** {@inheritDoc} */
    public void removeTreeListener(TreeListener listener)
    {
        listeners.remove(listener);
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        TreeLayout layout = this.getLayout();
        if (layout != null)
        {
            if (!dc.isOrderedRenderingMode())
                dc.addOrderedRenderable(this);
            else
                layout.render(dc);
        }
    }

    /** {@inheritDoc} */
    public void pick(DrawContext dc, Point pickPoint)
    {
        TreeLayout layout = this.getLayout();
        if (layout != null)
            layout.render(dc);        
    }

    /** {@inheritDoc} */
    public double getDistanceFromEye()
    {
        return 1;
    }

    /** {@inheritDoc} */
    public TreeLayout getLayout()
    {
        return layout;
    }

    /** {@inheritDoc} */
    public void setLayout(TreeLayout layout)
    {
        this.layout = layout;
    }

    /** {@inheritDoc} */
    public TreeModel getModel()
    {
        return model;
    }

    /** {@inheritDoc} */
    public void setModel(TreeModel model)
    {
        this.model = model;
    }

    protected void fireTreeEvent(TreeNode source, String event)
    {
        this.fireTreeEvent(new TreeEvent(this, source, event));
    }

    /**
     * Notify listeners of a tree event.
     *
     * @param event Event that has occurred on the tree.
     */
    protected void fireTreeEvent(TreeEvent event)
    {
        for (TreeListener listener : listeners)
        {
            try
            {
                listener.onTreeEvent(event);
            }
            catch (Exception e)
            {
                Logging.logger().warning(e.getLocalizedMessage());
            }
        }
    }
}
