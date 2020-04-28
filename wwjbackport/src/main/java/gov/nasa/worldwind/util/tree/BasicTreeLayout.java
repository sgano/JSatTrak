/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;

/**
 * Layout that draws a {@link Tree} similar to a file browser tree.
 *
 * @author pabercrombie
 * @version $Id: BasicTreeLayout.java 14183 2010-12-03 03:39:03Z pabercrombie $
 */
public class BasicTreeLayout implements TreeLayout, Scrollable
{
    protected Tree tree;

    protected ScrollFrame frame;

    protected TreeNodeRenderer defaultRenderer;
    protected WorldWindow wwd;

    protected TreeAttributes normalAttributes = new BasicTreeAttributes();
    protected TreeAttributes highlightAttributes = new BasicTreeAttributes();

    protected boolean highlighted;

    /**
     * This field is set by {@link #makeVisible(TreePath)}, and read by {@link #scrollToNode(gov.nasa.worldwind.render.DrawContext,
     * java.awt.Point)} during rendering.
     */
    protected TreeNode scrollToNode;

    /**
     * Create a layout for a tree.
     *
     * @param tree Tree to create layout for.
     * @param wwd  WorldWindow in which layout will appear.
     */
    public BasicTreeLayout(Tree tree, WorldWindow wwd)
    {
        this.wwd = wwd;
        this.tree = tree;
        this.frame = this.createFrame();
        this.frame.setContents(this);
        this.defaultRenderer = this.createDefaultNodeRenderer();
    }

    /**
     * Create a layout for a tree, at a screen location.
     *
     * @param tree Tree to create layout for.
     * @param wwd  WorldWindow in which layout will appear.
     * @param x    X coordinate of the upper left corner of the tree frame.
     * @param y    Y coordinate of the upper left corner of the tree frame, measured from the top of the screen.
     */
    public BasicTreeLayout(Tree tree, WorldWindow wwd, int x, int y)
    {
        this(tree, wwd, new Offset((double)x, (double)y, AVKey.PIXELS, AVKey.PIXELS));
    }

    /**
     * Create a layout for a tree, at a screen location.
     *
     * @param tree           Tree to create layout for.
     * @param wwd            WorldWindow in which layout will appear.
     * @param screenLocation The location of the upper left corner of the tree frame. The point is in screen
     *                       coordinates, measured from the upper left corner of the screen.
     */
    public BasicTreeLayout(Tree tree, WorldWindow wwd, Offset screenLocation)
    {
        this.wwd = wwd;
        this.tree = tree;
        this.frame = this.createFrame();
        this.frame.setContents(this);
        this.defaultRenderer = this.createDefaultNodeRenderer();
        this.setScreenLocation(screenLocation);
    }

    /**
     * Create the frame that the tree will be rendered inside.
     *
     * @return A new frame.
     */
    protected ScrollFrame createFrame()
    {
        return new ScrollFrame(this.wwd);
    }

    /**
     * Create the default node renderer.
     *
     * @return New node renderer.
     */
    protected TreeNodeRenderer createDefaultNodeRenderer()
    {
        return new BasicTreeNodeRenderer(this.frame);
    }

    /**
     * Get the size of the entire tree, including the part that is not visible in the scroll pane.
     *
     * @param dc Draw context.
     *
     * @return Size of the rendered tree.
     */
    public Dimension getSize(DrawContext dc)
    {
        TreeModel model = this.tree.getModel();
        TreeNode root = model.getRoot();

        Dimension size = new Dimension();
        computeSize(this.tree, root, dc, size, 1);

        return size;
    }

    /**
     * Compute the size of a tree. This method invokes itself recursively to calculate the size of the tree, taking into
     * account which nodes are expanded and which are not. This computed size will be stored in the {@code size}
     * parameter.
     *
     * @param tree  Tree that contains the root node.
     * @param root  Root node of the subtree to find the size of. This does not need to be the root node of the tree.
     * @param dc    Draw context.
     * @param size  Size object to modify. This method will change the width and height fields of {@code size} to hold
     *              the new size of the tree.
     * @param level Level of this node. Tree root node is level 1, children of the root are level 2, etc.
     */
    protected void computeSize(Tree tree, TreeNode root, DrawContext dc, Dimension size, int level)
    {
        TreeAttributes attributes = this.getActiveAttributes();

        Dimension thisSize = this.getNodeRenderer(root).getPreferredSize(dc, root, attributes);

        if (this.mustDisplayNode(root, level))
        {
            int indent = attributes.getIndent() * level;
            int thisWidth = thisSize.width + indent;
            if (thisWidth > size.width)
                size.width = thisWidth;

            size.height += thisSize.height;
            size.height += attributes.getRowSpacing();
        }

        if (tree.isNodeExpanded(root))
        {
            for (TreeNode child : root.getChildren())
            {
                this.computeSize(tree, child, dc, size, level + 1);
            }
        }
    }

    /**
     * Determine if a node needs to be displayed. This method examines only one node at a time. It does not take into
     * account that the node's parent may be in the collapsed state, in which the children are not rendered.
     *
     * @param node  Node to test.
     * @param level Level of the node in the tree. The root node is level 1, its children are level 2, etc.
     *
     * @return True if the node must be displayed.
     */
    protected boolean mustDisplayNode(TreeNode node, int level)
    {
        return node.isVisible() && (level > 1 || this.getActiveAttributes().isRootVisible());
    }

    public void render(DrawContext dc)
    {
        this.frame.render(dc);
    }

    /**
     * Scroll the frame to make a the node set in {@link #scrollToNode} node visible. Does nothing if {@link
     * #scrollToNode} is null.
     *
     * @param dc        Draw context.
     * @param upperLeft The upper left corner of the tree, in OpenGL coordinates.
     */
    protected synchronized void scrollToNode(DrawContext dc, Point upperLeft)
    {
        if (this.scrollToNode != null)
        {
            Point drawPoint = new Point(upperLeft);
            Rectangle bounds = this.findNodeBounds(this.scrollToNode, this.tree.getModel().getRoot(), dc, drawPoint, 1);

            Rectangle visibleBounds = this.frame.getVisibleBounds();

            if (bounds.getMaxY() > visibleBounds.getMaxY())
                this.frame.getScrollBar(AVKey.VERTICAL).setValue((int) (bounds.getMaxY() - visibleBounds.getMaxY()));
            else if (bounds.getMinY() < visibleBounds.getMinY())
                this.frame.getScrollBar(AVKey.VERTICAL).setValue((int) (visibleBounds.getMinY() - bounds.getMinY()));

            this.scrollToNode = null;
        }
    }

    /**
     * This method is called by {@link ScrollFrame} to render the contents of the tree at the appropriate position.
     *
     * @param dc     Draw context.
     * @param bounds Rectangle in which to render scrollable content. The rectangle is specified in OpenGL coordinates,
     *               with the origin at the low left corner of the screen.
     */
    public void renderScrollable(DrawContext dc, Rectangle bounds)
    {
        TreeModel model = this.tree.getModel();
        TreeNode root = model.getRoot();

        Point drawPoint = new Point(bounds.x, bounds.y + bounds.height);

        this.scrollToNode(dc, drawPoint);

        renderSubtree(root, dc, drawPoint, 1);
    }

    /**
     * Render a part of the tree.
     *
     * @param root     Root node of the subtree to render.
     * @param dc       Draw context.
     * @param location Location at which to draw the node. The location specifies the upper left corner of the subtree.
     * @param level    The level of this node in the tree. The root node is at level 1, its child nodes are at level 2,
     *                 etc.
     */
    protected void renderSubtree(TreeNode root, DrawContext dc, Point location, int level)
    {
        TreeAttributes attributes = this.getActiveAttributes();
        TreeNodeRenderer renderer = this.getNodeRenderer(root);

        int oldX = location.x;

        if (this.mustDisplayNode(root, level))
        {
            Dimension size = this.getNodeRenderer(root).getPreferredSize(dc, root, attributes);

            // Adjust y to the bottom of the node area
            location.y -= (size.height + this.getActiveAttributes().getRowSpacing());

            Rectangle nodeBounds = new Rectangle(location.x, location.y, size.width, size.height);
            if (nodeBounds.intersects(this.frame.getVisibleBounds()))
                renderer.render(dc, tree, root, attributes, nodeBounds);
            location.x += level * attributes.getIndent();
        }

        // Draw child nodes if the root node is expanded
        if (tree.isNodeExpanded(root))
        {
            for (TreeNode child : root.getChildren())
            {
                this.renderSubtree(child, dc, location, level + 1);
            }
        }
        location.x = oldX; // Restore previous indent level
    }

    /**
     * Find the bounds of a node in the tree.
     *
     * @param needle   The node to find.
     * @param haystack Root node of the subtree to search.
     * @param dc       Draw context.
     * @param location Point in OpenGL screen coordinates (origin lower left corner) that defines the uppper left corner
     *                 of the subtree.
     * @param level    Level of this subtree in the tree. The root node is level 1, its children are level 2, etc.
     *
     * @return Bounds of the node {@code needle}.
     */
    protected Rectangle findNodeBounds(TreeNode needle, TreeNode haystack, DrawContext dc, Point location, int level)
    {
        TreeAttributes attributes = this.getActiveAttributes();

        int oldX = location.x;

        if (level > 1 || attributes.isRootVisible())
        {
            Dimension size = this.getNodeRenderer(haystack).getPreferredSize(dc, haystack, attributes);

            // Adjust y to the bottom of the node area
            location.y -= (size.height + this.getActiveAttributes().getRowSpacing());

            Rectangle nodeBounds = new Rectangle(location.x, location.y, size.width, size.height);

            if (haystack.getPath().equals(needle.getPath()))
                return nodeBounds;

            location.x += level * attributes.getIndent();
        }

        // Draw child nodes if the root node is expanded
        if (this.tree.isNodeExpanded(haystack))
        {
            for (TreeNode child : haystack.getChildren())
            {
                Rectangle bounds = this.findNodeBounds(needle, child, dc, location, level + 1);
                if (bounds != null)
                    return bounds;
            }
        }
        location.x = oldX; // Restore previous indent level

        return null;
    }

    /** {@inheritDoc} */
    public synchronized void makeVisible(TreePath path)
    {
        TreeNode node = this.tree.getNode(path);
        if (node == null)
            return;

        TreeNode parent = node.getParent();
        while (parent != null)
        {
            this.tree.expandPath(parent.getPath());
            parent = parent.getParent();
        }

        // Set the scrollToNode field. This field will be read during rendering, and the frame will be
        // scrolled appropriately.
        this.scrollToNode = node;
    }

    /**
     * Get the location of the upper left corner of the tree, measured in screen coordinates with the origin at the
     * upper left corner of the screen.
     *
     * @return Screen location, measured in pixels from the upper left corner of the screen.
     */
    public Offset getScreenLocation()
    {
        return this.frame.getScreenLocation();
    }

    /**
     * Set the location of the upper left corner of the tree, measured in screen coordinates with the origin at the
     * upper left corner of the screen.
     *
     * @param screenLocation New screen location.
     */
    public void setScreenLocation(Offset screenLocation)
    {
        frame.setScreenLocation(screenLocation);
    }

    /** {@inheritDoc} */
    public TreeAttributes getAttributes()
    {
        return this.normalAttributes;
    }

    /** {@inheritDoc} */
    public void setAttributes(TreeAttributes attributes)
    {
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.normalAttributes = attributes;
    }

    /**
     * Get the attributes to apply when the tree is highlighted.
     *
     * @return Attributes to use when tree is highlighted.
     */
    public TreeAttributes getHighlightAttributes()
    {
        return this.highlightAttributes;
    }

    /**
     * Set the attributes to use when the tree is highlighted.
     *
     * @param attributes New highlight attributes.
     */
    public void setHighlightAttributes(TreeAttributes attributes)
    {
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.highlightAttributes = attributes;
    }

    /**
     * Get the active attributes, based on the highlight state.
     *
     * @return Highlight attributes if the tree is highlighted. Otherwise, the normal attributes.
     */
    protected TreeAttributes getActiveAttributes()
    {
        if (this.isHighlighted())
            return this.getHighlightAttributes();
        else
            return this.getAttributes();
    }

    /**
     * Is the tree highlighted? The tree is highlighted when the mouse is within the bounds of the containing frame.
     *
     * @return True if the tree is highlighted.
     */
    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    /**
     * Set the tree layout to highlighted or not highlighted.
     *
     * @param highlighted True if the tree should be highlighted.
     */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    /**
     * Get the node render that should be used to draw a node. This implementation returns the same {@link
     * BasicTreeNodeRenderer} for all nodes. Subclasses may override this method to use different renderers for
     * different nodes.
     *
     * @param node Node to get renderer for.
     *
     * @return Renderer should should be used to draw {@code node}.
     */
    protected TreeNodeRenderer getNodeRenderer(TreeNode node)
    {
        return this.defaultRenderer;
    }

    /**
     * Get the frame that surrounds the tree.
     *
     * @return The frame that the tree is drawn on.
     */
    public ScrollFrame getFrame()
    {
        return this.frame;
    }
}
