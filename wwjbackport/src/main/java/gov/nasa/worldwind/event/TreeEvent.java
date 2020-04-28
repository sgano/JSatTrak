/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.event;

import gov.nasa.worldwind.util.tree.*;

import java.util.EventObject;

/**
 * An event that occurs on a {@link gov.nasa.worldwind.util.tree.Tree}.
 *
 * @author pabercrombie
 * @version $Id: TreeEvent.java 14032 2010-10-24 23:19:13Z pabercrombie $
 * @see gov.nasa.worldwind.util.tree.Tree
 */
public class TreeEvent extends EventObject
{
    /** Event triggered when a path is expanded. */
    public static final String PATH_EXPANDED = "gov.nasa.worldwind.TreeEvent.PathExpanded";
    /** Event triggered when a path is collapsed. */
    public static final String PATH_COLLAPSED = "gov.nasa.worldwind.TreeEvent.PathCollapsed";
    /** Event triggered when a node is selected or deselected. */
    public static final String NODE_SELECTION_CHANGED = "gov.nasa.worldwind.TreeEvent.NodeSelectionChanged";
    /** Event triggered when a node is added. */
    public static final String NODE_ADDED = "gov.nasa.worldwind.TreeEvent.NodeAdded";
    /** Event triggered when a node is removed. */
    public static final String NODE_REMOVED = "gov.nasa.worldwind.TreeEvent.NodeRemoved";

    protected TreeNode node;
    protected final String eventAction;

    /**
     * Create a TreeEvent.
     *
     * @param source      The tree on which the occurred.
     * @param node        The node involved in the event. May be null.
     * @param eventAction String that identifies the type of event.
     *
     * @throws IllegalArgumentException if source is null.
     */
    public TreeEvent(Tree source, TreeNode node, String eventAction)
    {
        super(source);
        this.eventAction = eventAction;
        this.node = node;
    }

    /** {@inheritDoc} */
    @Override
    public Tree getSource()
    {
        return (Tree) super.getSource();
    }

    /**
     * Get the node involved in the event.
     *
     * @return The node. May return null if no node was involved in the event.
     */
    public TreeNode getNode()
    {
        return this.node;
    }

    /**
     * Get the string that identifies the type of event.
     *
     * @return The type of event that occurred.
     */
    public String getEventAction()
    {
        return this.eventAction;
    }
}
