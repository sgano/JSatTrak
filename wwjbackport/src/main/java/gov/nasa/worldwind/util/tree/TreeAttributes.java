/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import java.awt.*;

/**
 * Attributes to control how a {@link Tree} is rendered. The class captures a set of attributes found in a typical tree
 * layout, but some layouts may not use all of these properties.
 *
 * @author pabercrombie
 * @version $Id $
 * @see TreeLayout
 */
public interface TreeAttributes
{
    /**
     * Should be root node be drawn?
     *
     * @return True if the root node should be drawn.
     *
     * @see #setRootVisible(boolean)
     */
    boolean isRootVisible();

    /**
     * Set the root node to visibile or not visible.
     *
     * @param visible True if the root node should be drawn.
     *
     * @see #isRootVisible()
     */
    void setRootVisible(boolean visible);

    /**
     * Get the color of the text in the tree.
     *
     * @return Text color.
     *
     * @see #setForegroundColor(java.awt.Color)
     */
    Color getForegroundColor();

    /**
     * Set the color of the text in the tree.
     *
     * @param textColor New text color.
     *
     * @see #getForegroundColor()
     */
    void setForegroundColor(Color textColor);

    /**
     * Get the font used to render text.
     *
     * @return Tree font.
     *
     * @see #setFont(java.awt.Font)
     */
    Font getFont();

    /**
     * Get the font used to render the node description.
     *
     * @return Font for node description.
     */
    Font getDescriptionFont();

    /**
     * Set the font used to render the node descriptions.
     *
     * @param font New font for descriptions.
     */
    void setDescriptionFont(Font font);

    /**
     * Set the font used to render text.
     *
     * @param font New tree font.
     *
     * @see #getFont()
     */
    void setFont(Font font);

    /**
     * Get the space, in pixels, to leave between rows in the tree.
     *
     * @return Space in pixels between rows.
     *
     * @see #setRowSpacing(int)
     */
    int getRowSpacing();

    /**
     * Set the space, in pixels, to leave between rows in the tree.
     *
     * @param spacing Row spacing.
     *
     * @see #getRowSpacing()
     */
    void setRowSpacing(int spacing);

    /**
     * Get the number of pixels used to indent each new level in the tree.
     *
     * @return Indent applied to each new level in the tree.
     *
     * @see #setIndent(int)
     */
    int getIndent();

    /**
     * Set the indent, in pixels, that is applied to each new level in the tree.
     *
     * @param indent Indent in pixels.
     *
     * @see #getIndent()
     */
    void setIndent(int indent);

    /**
     * Get the size of each icon in the tree. If the icon images do not match this size, they will be scaled to fit.
     *
     * @return Icon size.
     *
     * @see #setIconSize(java.awt.Dimension)
     */
    Dimension getIconSize();

    /**
     * Set the size of each icon in the tree.
     *
     * @param size New size.
     *
     * @see #getIconSize()
     */
    void setIconSize(Dimension size);

    /**
     * Get the amount of space, in pixels, to leave between an icon in the tree and surrounding text and shapes.
     *
     * @return Icon space in pixels.
     *
     * @see #setIconSpace(int)
     */
    int getIconSpace();

    /**
     * Set the amount of space, in pixels, to leave between an icon in the tree and surrounding text and shapes.
     *
     * @param iconSpace Icon space in pixels.
     *
     * @see #getIconSpace()
     */
    void setIconSpace(int iconSpace);

    /**
     * Get the opacity of the text and images in the tree.
     *
     * @return Opacity of text and images.
     *
     * @see #setForegroundOpacity(double)
     */
    double getForegroundOpacity();

    /**
     * Set the opacity of the tree text and images.
     *
     * @param textOpacity New opacity.
     *
     * @see #getForegroundOpacity()
     */
    void setForegroundOpacity(double textOpacity);
}
