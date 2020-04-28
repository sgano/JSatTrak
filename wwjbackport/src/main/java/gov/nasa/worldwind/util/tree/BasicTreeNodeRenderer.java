/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.geom.*;

/**
 * Tree node renderer that draws a node in a {@link BasicTreeLayout}. Each node is drawn as one or more lines of text,
 * with an icon, and a checkbox to indicate if the node is selected. Group nodes are drawn with a triangle symbol to
 * indicate if the node is expanded or collapsed.
 *
 * @author pabercrombie
 * @version $Id: BasicTreeNodeRenderer.java 14200 2010-12-07 02:49:23Z pabercrombie $
 * @see gov.nasa.worldwind.util.tree.BasicTreeLayout
 */
public class BasicTreeNodeRenderer implements TreeNodeRenderer
{
    protected PickSupport pickSupport = new PickSupport();

    protected boolean showDescription = true;

    protected boolean drawNodeStateSymbol = true;

    protected boolean drawSelectedSymbol = true;

    protected HotSpot parent;

    /**
     * Create a node renderer.
     *
     * @param parent The screen area that contains the node. Input events that cannot be handled by the tree node will
     *               be passed to this component. May be null.
     */
    public BasicTreeNodeRenderer(HotSpot parent)
    {
        this.parent = parent;
    }

    /** {@inheritDoc} */
    public Dimension getPreferredSize(DrawContext dc, TreeNode node, TreeAttributes attributes)
    {
        Dimension size = new Dimension();
        Rectangle2D textBounds = this.getTextRenderBounds(dc, this.getText(node), attributes);
        size.width = (int) textBounds.getWidth();
        size.height = (int) textBounds.getHeight();

        String description = this.getDescriptionText(node);
        if (description != null)
        {
            Rectangle2D descriptionBounds = this.getTextRenderBounds(dc, description, attributes);
            size.width = (int) Math.max(size.width, descriptionBounds.getWidth());
            size.height += (int) descriptionBounds.getHeight();
        }

        if (node.hasImage())
        {
            Dimension iconSize = attributes.getIconSize();
            if (iconSize.height > size.height)
                size.height = iconSize.height;

            size.width += iconSize.width + attributes.getIconSpace();
        }

        if (node.isLeaf())
            size.width += this.getSelectedSymbolSize(dc).width + attributes.getIconSpace();
        else
            size.width += this.getNodeStateSymbolSize(dc).width + attributes.getIconSpace();

        return size;
    }

    /** {@inheritDoc} */
    // TODO scale node to fit within bounds
    public void render(DrawContext dc, Tree tree, TreeNode node, TreeAttributes attributes, Rectangle bounds)
    {
        if (!node.isVisible())
            return;

        GL gl = dc.getGL();

        OGLStackHandler oglStack = new OGLStackHandler();

        int x = bounds.x;
        int y = bounds.y;

        Rectangle2D textBounds = this.getTextRenderBounds(dc, this.getText(node), attributes);
        Dimension selectedSymbolSize = this.getSelectedSymbolSize(dc);

        // Calculate height of text from baseline to top of text. Note that this dos not include descenders below the baseline. 
        int textHeight = (int) Math.abs(textBounds.getY());

        int maxHeight = Math.max(textHeight, selectedSymbolSize.height);
        if (node.hasImage())
        {
            Dimension iconSize = attributes.getIconSize();
            maxHeight = Math.max(maxHeight, iconSize.height);
        }

        try
        {
            if (dc.isPickingMode())
            {
                this.pickSupport.beginPicking(dc);
            }
            else
            {
                oglStack.pushAttrib(gl,
                    GL.GL_DEPTH_BUFFER_BIT
                        | GL.GL_COLOR_BUFFER_BIT
                        | GL.GL_TEXTURE_BIT
                        | GL.GL_CURRENT_BIT);
            }

            // If the node is not a leaf, draw a symbol to indicate if it is expanded or collapsed
            if (!node.isLeaf() && this.isDrawNodeStateSymbol())
            {
                Dimension symbolSize = this.getNodeStateSymbolSize(dc);
                int vertAdjust = bounds.height - symbolSize.height - (maxHeight - selectedSymbolSize.height) / 2;

                Rectangle symbolBounds = new Rectangle(x, y + vertAdjust, symbolSize.width, symbolSize.height);
                if (!dc.isPickingMode())
                    this.drawNodeStateSymbol(dc, tree, node, tree.isNodeExpanded(node), attributes, symbolBounds,
                        bounds);
                else
                    TreeUtil.drawPickableRect(dc, this.pickSupport, this.createTogglePathControl(tree, node),
                        symbolBounds);
            }
            if (this.isDrawNodeStateSymbol())
                x += this.getNodeStateSymbolSize(dc).width + attributes.getIconSpace();

            int vertAdjust = bounds.height - selectedSymbolSize.height - (maxHeight - selectedSymbolSize.height) / 2;

            Rectangle selectedSymbolBounds = new Rectangle(x, y + vertAdjust, selectedSymbolSize.width,
                selectedSymbolSize.height);

            if (this.isDrawSelectedSymbol())
            {
                if (!dc.isPickingMode())
                    this.drawSelectedSymbol(dc, node, attributes, selectedSymbolBounds, bounds);
                else
                    TreeUtil.drawPickableRect(dc, this.pickSupport, this.createSelectControl(node),
                        selectedSymbolBounds);

                x += selectedSymbolSize.width + attributes.getIconSpace();
            }

            if (!dc.isPickingMode())
            {
                // Draw node icon
                if (node.hasImage())
                {
                    this.drawIcon(dc, node, attributes, x, y, bounds);
                    x += attributes.getIconSize().width + attributes.getIconSpace();
                }

                vertAdjust = bounds.height - textHeight - (maxHeight - textHeight) / 2;
                this.drawText(dc, node, attributes, x, y + vertAdjust, bounds);
            }
            else
            {
                Rectangle textAndIconBounds = new Rectangle(x, y, (int) bounds.getMaxX() - bounds.x, bounds.height);
                TreeUtil.drawPickableRect(dc, this.pickSupport, this.createSelectControl(node), textAndIconBounds);
            }
        }
        finally
        {
            oglStack.pop(gl);

            if (dc.isPickingMode())
            {
                this.pickSupport.endPicking(dc);
                this.pickSupport.resolvePick(dc, dc.getPickPoint(), dc.getCurrentLayer());
            }
        }
    }

    /**
     * Draw the text for a node. The coordinate argument specifies the position of the baseline of the node title. This
     * method may also draw the node description text below this point.
     *
     * @param dc         Draw context.
     * @param node       Node to render.
     * @param attributes Attributes that control the node rendering.
     * @param x          X coordinate of the text area, measured from left edge of screen.
     * @param y          Y coordinate of the text area, measured from the bottom edge of the screen.
     * @param nodeBounds Bounds of the entire tree node.
     */
    protected void drawText(DrawContext dc, TreeNode node, TreeAttributes attributes, int x, int y,
        Rectangle nodeBounds)
    {
        String text = this.getText(node);
        String description = this.getDescriptionText(node);

        Color color = attributes.getForegroundColor();
        float[] colorRGB = color.getRGBColorComponents(null);

        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            attributes.getFont(), true, false, false);

        textRenderer.begin3DRendering();
        try
        {
            textRenderer.setColor(colorRGB[0], colorRGB[1], colorRGB[2], (float) attributes.getForegroundOpacity());
            textRenderer.draw(text, x, y);
        }
        finally
        {
            textRenderer.end3DRendering();
        }

        if (this.isShowDescription() && description != null)
        {
            textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
                attributes.getDescriptionFont(), false, false, false);
            MultiLineTextRenderer mltr = new MultiLineTextRenderer(textRenderer);
            textRenderer.begin3DRendering();
            try
            {
                textRenderer.setColor(colorRGB[0], colorRGB[1], colorRGB[2], (float) attributes.getForegroundOpacity());

                // MultiLineTextRenderer takes a coordinate that specifies the top left corner of the text instead of
                // the bottom left, we can pass the same coordinate that we used for the node title, and the description
                // will be drawn below the title.
                mltr.draw(description, x, y);
            }
            finally
            {
                textRenderer.end3DRendering();
            }
        }
    }

    /**
     * Draw the node icon.
     *
     * @param dc         Draw context.
     * @param node       Node to render.
     * @param attributes Attributes that control the node rendering.
     * @param x          X coordinate of the icon area, measured from left edge of screen.
     * @param y          Y coordinate of the icon area, measured from the bottom edge of the screen.
     * @param nodeBounds Bounds of the entire tree node.
     */
    protected void drawIcon(DrawContext dc, TreeNode node, TreeAttributes attributes, int x, int y,
        Rectangle nodeBounds)
    {
        GL gl = dc.getGL();

        WWTexture texture = node.getTexture();

        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            if (texture != null && texture.bind(dc))
            {
                oglStack.pushAttrib(gl, GL.GL_TEXTURE_BIT
                    | GL.GL_COLOR_BUFFER_BIT
                    | GL.GL_ENABLE_BIT
                    | GL.GL_CURRENT_BIT);

                gl.glEnable(GL.GL_TEXTURE_2D);

                Dimension iconSize = attributes.getIconSize();

                // If the total node height is greater than the image height, vertically center the image
                int vertAdjustment = 0;
                if (iconSize.height < nodeBounds.height)
                {
                    vertAdjustment = nodeBounds.height - iconSize.height;
                }

                oglStack.pushModelview(gl);

                gl.glColor4d(1d, 1d, 1d, attributes.getForegroundOpacity());

                TextureCoords texCoords = texture.getTexCoords();
                gl.glTranslated(x, y + vertAdjustment, 1.0);
                gl.glScaled((double) iconSize.width, (double) iconSize.width, 1d);
                dc.drawUnitQuad(texCoords);
            }
        }
        finally
        {
            oglStack.pop(gl);
        }
    }

    /**
     * Draw a symbol to indicate if the node is expanded or collapsed. This implementation draws a triangle pointing to
     * the right if the node is collapsed, or pointing down if the node is expanded.
     *
     * @param dc           Draw context.
     * @param tree         Tree that contains this node.
     * @param node         The node to render.
     * @param isExpanded   Is the node expanded?
     * @param attributes   Attributes to control how the node is rendered.
     * @param symbolBounds The bounds of the node state symbol. This method should not draw outside of these bounds.
     * @param nodeBounds   The bounds of the entire tree node.
     */
    protected void drawNodeStateSymbol(DrawContext dc, final Tree tree, final TreeNode node, boolean isExpanded,
        TreeAttributes attributes, Rectangle symbolBounds, Rectangle nodeBounds)
    {
        if (isExpanded)
            this.drawTriangle(dc, -90, attributes.getForegroundColor(), attributes.getForegroundOpacity(),
                symbolBounds);
        else
            this.drawTriangle(dc, 0, attributes.getForegroundColor(), attributes.getForegroundOpacity(), symbolBounds);
    }

    /**
     * Create a pickable object to represent a toggle control in the tree. The toggle control will expand or collapse a
     * node in response to user input.
     *
     * @param tree Tree that contains the node.
     * @param node The node to expand or collapse.
     *
     * @return A {@link TreeHotSpot} that will be added as a pickable object to the screen area occupied by the toggle
     *         control.
     */
    protected HotSpot createTogglePathControl(final Tree tree, final TreeNode node)
    {
        return new TreeHotSpot(this.parent)
        {
            public void selected(SelectEvent event)
            {
                if (event.isLeftClick())
                {
                    tree.togglePath(node.getPath());
                }
            }
        };
    }

    /**
     * Create a pickable object to represent selection control in the tree. The selection control will select or
     * deselect a node in response to user input.
     *
     * @param node The node to expand or collapse.
     *
     * @return A {@link TreeHotSpot} that will be added as a pickable object to the screen area occupied by the toggle
     *         control.
     */
    protected HotSpot createSelectControl(final TreeNode node)
    {
        TreeHotSpot hs = new TreeHotSpot(this.parent)
        {
            public void selected(SelectEvent event)
            {
                if (event.isLeftClick())
                {
                    node.setSelected(!node.isSelected());
                }
            }
        };
        hs.setValue(AVKey.DISPLAY_NAME, node.getText());
        return hs;
    }

    /**
     * Draw a symbol to indicate if the node is selected or not. This implementation draws a checkbox.
     *
     * @param dc           Draw context.
     * @param node         The node to render.
     * @param attributes   Attributes to control how the node is rendered.
     * @param symbolBounds The bounds of the node state symbol. This method should not draw outside of these bounds.
     * @param nodeBounds   The bounds of the entire tree node.
     */
    protected void drawSelectedSymbol(DrawContext dc, TreeNode node, TreeAttributes attributes, Rectangle symbolBounds,
        Rectangle nodeBounds)
    {
        boolean filled = false;
        boolean checked = false;

        String selected = node.isTreeSelected();
        if (TreeNode.SELECTED.equals(selected))
        {
            filled = false;
            checked = true;
        }
        else if (TreeNode.NOT_SELECTED.equals(selected))
        {
            filled = false;
            checked = false;
        }
        else if (TreeNode.PARTIALLY_SELECTED.equals(selected))
        {
            filled = true;
            checked = false;
        }

        drawCheckBox(dc, attributes.getForegroundColor(), attributes.getForegroundOpacity(), symbolBounds, filled,
            checked);
    }

    /**
     * Draw an open or filled square to indicate that a leaf node is selected or not selected.
     *
     * @param dc      Draw context to draw into.
     * @param color   Color to apply to symbol.
     * @param opacity Opacity to apply to symbol.
     * @param bounds  Bounds of the squage.
     * @param filled  True if the square should be filled.
     * @param checked True if the checkbox should be drawn in the checked state.
     */
    protected void drawCheckBox(DrawContext dc, Color color, double opacity, Rectangle bounds, boolean filled,
        boolean checked)
    {
        GL gl = dc.getGL();

        try
        {
            gl.glPushAttrib(GL.GL_POLYGON_BIT
                | GL.GL_CURRENT_BIT
                | GL.GL_LINE_BIT);

            gl.glLineWidth(1f);

            Color color1 = new Color(29, 78, 169);
            Color color2 = new Color(93, 158, 223);

            if (filled)
            {
                // Fill box with a diagonal gradient
                gl.glBegin(GL.GL_QUADS);
                OGLUtil.applyColor(gl, color1, opacity, false);
                gl.glVertex2d(bounds.getMaxX(), bounds.getMaxY());
                gl.glVertex2d(bounds.getMinX(), bounds.getMaxY());
                gl.glVertex2d(bounds.getMinX(), bounds.getMinY());

                OGLUtil.applyColor(gl, color2, opacity, false);
                gl.glVertex2d(bounds.getMaxX(), bounds.getMinY());
                gl.glEnd();
            }

            OGLUtil.applyColor(gl, color, opacity, false);
            if (checked)
            {
                gl.glBegin(GL.GL_LINE_STRIP);
                gl.glVertex2d(bounds.getMinX() + bounds.width * 0.3, bounds.getMinY() + bounds.height * 0.6);
                gl.glVertex2d(bounds.getMinX() + bounds.width * 0.3, bounds.getMinY() + bounds.height * 0.2);
                gl.glVertex2d(bounds.getMinX() + bounds.width * 0.8, bounds.getMinY() + bounds.height * 0.8);
                gl.glEnd();
            }

            gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
            gl.glRecti(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
        }
        finally
        {
            gl.glPopAttrib();
        }
    }

    /**
     * Draw a small triangle as the node expanded or collapsed symbol.
     *
     * @param dc       Draw context to draw into.
     * @param rotation Rotation to apply to triangle. Positive is counter clock-wise.
     * @param color    Color to apply to symbol.
     * @param opacity  Opacity to apply to symbol.
     * @param bounds   Draw a triangle pointing to the right in the enter of this rectangle, and then apply rotation.
     */
    protected void drawTriangle(DrawContext dc, float rotation, Color color, double opacity, Rectangle bounds)
    {
        GL gl = dc.getGL();

        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            oglStack.pushModelviewIdentity(gl);

            oglStack.pushAttrib(gl, GL.GL_COLOR_BUFFER_BIT | GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);

            gl.glLineWidth(1f);
            OGLUtil.applyColor(gl, color, opacity, false);

            int halfHeight = bounds.height / 2;
            int halfWidth = bounds.width / 2;

            gl.glTranslated(bounds.x + halfWidth, bounds.y + halfHeight, 1.0);
            gl.glRotatef(rotation, 0, 0, 1);

            gl.glBegin(GL.GL_TRIANGLES);
            gl.glVertex2f(0, halfHeight);
            gl.glVertex2f(halfWidth, 0);
            gl.glVertex2f(0, -halfHeight);
            gl.glEnd();
        }
        finally
        {
            oglStack.pop(gl);
        }
    }

    /**
     * Get the bounds of a text string.
     *
     * @param dc         Draw context.
     * @param text       Text to get bounds of.
     * @param attributes Attributes that define the text font and size.
     *
     * @return A rectangle that describes the node bounds. See {@link com.sun.opengl.util.j2d.TextRenderer#getBounds}
     *         for information on how this rectangle should be interpreted.
     */
    protected Rectangle2D getTextRenderBounds(DrawContext dc, String text, TreeAttributes attributes)
    {
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            attributes.getFont());
        Rectangle2D rectangle = textRenderer.getBounds(text);

        return rectangle.getBounds();
    }

    /**
     * Get the text for a node.
     *
     * @param node Node to get text for.
     *
     * @return Text for node.
     */
    protected String getText(TreeNode node)
    {
        return node.getText();
    }

    /**
     * Get the description text for a node.
     *
     * @param node Node to get text for.
     *
     * @return Description text for {@code node}. May return null if there is no description.
     */
    protected String getDescriptionText(TreeNode node)
    {
        return node.getDescription();
    }

    /**
     * Get the size of the symbol that indicates that a node is expanded or collapsed.
     *
     * @param dc Draw context.
     *
     * @return The size of the node state symbol.
     */
    protected Dimension getNodeStateSymbolSize(DrawContext dc)
    {
        return new Dimension(10, 10);
    }

    /**
     * Get the size of the symbol that indicates that a node is selected or not selected.
     *
     * @param dc Draw context.
     *
     * @return The size of the node selection symbol.
     */
    protected Dimension getSelectedSymbolSize(DrawContext dc)
    {
        return new Dimension(12, 12);
    }

    /**
     * Should the node renderer include node descriptions?
     *
     * @return True if the renderer should renderer node descriptions.
     */
    public boolean isShowDescription()
    {
        return showDescription;
    }

    /**
     * Set the renderer to renderer node descriptions (additional text rendered under the node title).
     *
     * @param showDescription True if the description should be rendered. False if only the icon and title should be
     *                        rendered.
     */
    public void setShowDescription(boolean showDescription)
    {
        this.showDescription = showDescription;
    }

    /**
     * Will the renderer draw a symbol to indicate that the node is selected? The default symbol is a checkbox.
     *
     * @return True if the node selected symbol (a checkbox by default) will be drawn.
     */
    public boolean isDrawSelectedSymbol()
    {
        return this.drawSelectedSymbol;
    }

    /**
     * Set whether or not the renderer will draw a symbol to indicate that the node is selected. The default symbol is a
     * checkbox.
     *
     * @param drawSelectedSymbol True if the node selected symbol (a checkbox by default) will be drawn.
     */
    public void setDrawSelectedSymbol(boolean drawSelectedSymbol)
    {
        this.drawSelectedSymbol = drawSelectedSymbol;
    }

    /**
     * Will the renderer draw a symbol to indicate that the node is expanded or collapsed (applies only to non-leaf
     * nodes). The default symbol is a triangle pointing to the right, for collapsed nodes, or down for expanded nodes.
     *
     * @return True if the node state symbol (default is a triangle pointing either to the right or down) will be
     *         drawn.
     */
    public boolean isDrawNodeStateSymbol()
    {
        return this.drawNodeStateSymbol;
    }

    /**
     * Set whether or not the renderer will draw a symbol to indicate that the node is expanded or collapsed (applies
     * only to non-leaf nodes). The default symbol is a triangle pointing to the right, for collapsed nodes, or down for
     * expanded nodes.
     *
     * @param drawNodeStateSymbol True if the node state symbol (default is a triangle pointing either to the right or
     *                            down) will be drawn.
     */
    public void setDrawNodeStateSymbol(boolean drawNodeStateSymbol)
    {
        this.drawNodeStateSymbol = drawNodeStateSymbol;
    }
}
