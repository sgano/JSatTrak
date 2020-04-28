/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.TreeEvent;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.net.URL;
import java.util.*;

/**
 * Default implementation of a {@link TreeNode}.
 *
 * @author pabercrombie
 * @version $Id: BasicTreeNode.java 14129 2010-11-18 18:24:39Z pabercrombie $
 */
public class BasicTreeNode extends AVListImpl implements TreeNode
{
    /** The model that contains this node. */
    protected BasicTreeModel model;

    protected String text;
    protected Object imageSource;
    protected BasicWWTexture texture;

    protected String description;

    protected TreeNode parent;
    protected List<TreeNode> children; // List is created when children are added

    protected boolean enabled = true;
    protected boolean selected;
    protected boolean visible = true;

    /**
     * Create a node with text.
     *
     * @param model The tree model that contains this node.
     * @param text  Node text.
     */
    public BasicTreeNode(BasicTreeModel model, String text)
    {
        this(model, text, null);
    }

    /**
     * Create a node with text and an icon.
     *
     * @param model       The tree model that contains this node.
     * @param text        Node text.
     * @param imageSource Image source for the node icon. May be a String, URL, or BufferedImage.
     */
    public BasicTreeNode(BasicTreeModel model, String text, Object imageSource)
    {
        if (model == null)
        {
            String message = Logging.getMessage("nullValue.ModelIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.text = text;
        this.setImageSource(imageSource);
        this.model = model;
    }

    /** {@inheritDoc} */
    public String getText()
    {
        return this.text;
    }

    /** {@inheritDoc} */
    public TreeNode getParent()
    {
        return this.parent;
    }

    /** {@inheritDoc} */
    public void setParent(TreeNode node)
    {
        this.parent = node;
    }

    /** {@inheritDoc} */
    public Iterable<TreeNode> getChildren()
    {
        if (this.children != null)
            return Collections.unmodifiableList(this.children);
        else
            return Collections.emptyList();
    }

    /** {@inheritDoc} */
    public boolean isEnabled()
    {
        return this.enabled;
    }

    /** {@inheritDoc} */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /** {@inheritDoc} */
    public boolean isSelected()
    {
        return this.selected;
    }

    /** {@inheritDoc} */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
        this.model.getTree().fireTreeEvent(this, TreeEvent.NODE_SELECTION_CHANGED);
    }

    /** {@inheritDoc} */
    public String isTreeSelected()
    {
        String selected = this.isSelected() ? SELECTED : NOT_SELECTED;

        for (TreeNode child : this.getChildren())
        {
            String childSelected = child.isTreeSelected();

            if (!selected.equals(childSelected))
            {
                selected = PARTIALLY_SELECTED;
                break; // No need to look at other nodes
            }
        }

        return selected;
    }

    /** {@inheritDoc} */
    public boolean isVisible()
    {
        return this.visible;
    }

    /** {@inheritDoc} */
    public boolean isLeaf()
    {
        return this.children == null;
    }

    /** {@inheritDoc} */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /** {@inheritDoc} */
    public Object getImageSource()
    {
        return imageSource;
    }

    /** {@inheritDoc} */
    public void setImageSource(Object imageSource)
    {
        this.imageSource = imageSource;
        this.texture = null;
    }

    /** {@inheritDoc} */
    public boolean hasImage()
    {
        return this.getImageSource() != null;
    }

    /** {@inheritDoc} */
    public BasicWWTexture getTexture()
    {
        if (this.texture != null)
            return this.texture;
        else
            return this.initializeTexture();
    }

    /**
     * Create and initialize the texture from the image source. If the image is not in memory this method will request
     * that it be loaded and return null.
     *
     * @return The texture, or null if the texture is not yet available.
     */
    protected BasicWWTexture initializeTexture()
    {
        Object imageSource = this.getImageSource();
        if (imageSource instanceof String || imageSource instanceof URL)
        {
            URL imageURL = WorldWind.getDataFileStore().requestFile(imageSource.toString());
            if (imageURL != null)
            {
                this.texture = new BasicWWTexture(imageURL, true);
            }
        }
        else if (imageSource != null)
        {
            this.texture = new BasicWWTexture(imageSource, true);
            return this.texture;
        }

        return null;
    }

    /** {@inheritDoc} */
    public void addChild(TreeNode child)
    {
        if (this.children == null)
            this.children = new ArrayList<TreeNode>();
        this.children.add(child);
        child.setParent(this);

        this.model.getTree().fireTreeEvent(child, TreeEvent.NODE_ADDED);
    }

    /** {@inheritDoc} */
    public void removeChild(TreeNode child)
    {
        if (this.children != null)
            this.children.remove(child);
        if (child != null && child.getParent() == this)
        {
            child.setParent(null);
            this.model.getTree().fireTreeEvent(child, TreeEvent.NODE_REMOVED);
        }
    }

    /** {@inheritDoc} */
    public void removeAllChildren()
    {
        if (this.children != null)
        {
            Iterator<TreeNode> iterator = this.children.iterator();
            while (iterator.hasNext())
            {
                TreeNode child = iterator.next();
                iterator.remove();

                child.setParent(null);
                this.model.getTree().fireTreeEvent(child, TreeEvent.NODE_REMOVED);
            }
        }
    }

    /** {@inheritDoc} */
    public TreePath getPath()
    {
        TreePath path = new TreePath();

        TreeNode node = this;
        while (node != null)
        {
            path.add(0, node.getText());
            node = node.getParent();
        }

        return path;
    }
}
