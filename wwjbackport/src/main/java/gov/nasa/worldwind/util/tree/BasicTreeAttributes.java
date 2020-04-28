/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * Basic implementation of {@link TreeAttributes} set.
 *
 * @author pabercrombie
 * @version $Id $
 */
public class BasicTreeAttributes implements TreeAttributes, FrameAttributes
{
    protected boolean rootVisible = true;

    protected double frameOpacity = 0.8;
    protected Color frameColor1 = Color.WHITE;
    protected Color frameColor2 = new Color(0xC8D2DE);

    protected boolean drawTitleBar = true;
    protected boolean allowResize = true;
    protected boolean allowMove = true;

    protected Color titleBarColor1 = new Color(29, 78, 169);
    protected Color titleBarColor2 = new Color(93, 158, 223);

    protected Color minimizeButtonColor = new Color(0xEB9BA4);

    protected double textOpacity = 1.0;

    protected Color textColor = Color.BLACK;
    protected Font font = Font.decode("Arial-BOLD-14");
    protected Font descriptionFont = Font.decode("Arial-14");
    protected int indent = 10; // Indent applied to each new level of the tree
    protected int rowSpacing = 8; // Spacing between rows in the tree

    protected Dimension iconSize = new Dimension(16, 16);
    protected int iconSpace = 5;

    /** {@inheritDoc} */
    public boolean isRootVisible()
    {
        return this.rootVisible;
    }

    /** {@inheritDoc} */
    public void setRootVisible(boolean visible)
    {
        this.rootVisible = visible;
    }

    /** {@inheritDoc} */
    public Color getForegroundColor()
    {
        return this.textColor;
    }

    /** {@inheritDoc} */
    public void setForegroundColor(Color textColor)
    {
        if (textColor == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.textColor = textColor;
    }

    /** {@inheritDoc} */
    public Font getFont()
    {
        return this.font;
    }

    /** {@inheritDoc} */
    public Font getDescriptionFont()
    {
        return this.descriptionFont;
    }

    /** {@inheritDoc} */
    public void setDescriptionFont(Font font)
    {
        if (font == null)
        {
            String msg = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.descriptionFont = font;
    }

    /** {@inheritDoc} */
    public void setFont(Font font)
    {
        if (font == null)
        {
            String msg = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.font = font;
    }

    /** {@inheritDoc} */
    public int getRowSpacing()
    {
        return this.rowSpacing;
    }

    /** {@inheritDoc} */
    public void setRowSpacing(int spacing)
    {
        this.rowSpacing = spacing;
    }

    /** {@inheritDoc} */
    public int getIndent()
    {
        return this.indent;
    }

    /** {@inheritDoc} */
    public void setIndent(int indent)
    {
        this.indent = indent;
    }

    /** {@inheritDoc} */
    public Dimension getIconSize()
    {
        return this.iconSize;
    }

    /** {@inheritDoc} */
    public void setIconSize(Dimension iconSize)
    {
        if (iconSize == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.iconSize = iconSize;
    }

    /** {@inheritDoc} */
    public int getIconSpace()
    {
        return this.iconSpace;
    }

    /** {@inheritDoc} */
    public void setIconSpace(int iconSpace)
    {
        this.iconSpace = iconSpace;
    }

    /** {@inheritDoc} */
    public double getForegroundOpacity()
    {
        return textOpacity;
    }

    /** {@inheritDoc} */
    public void setForegroundOpacity(double textOpacity)
    {
        this.textOpacity = textOpacity;
    }

    /** {@inheritDoc} */
    public double getBackgroundOpacity()
    {
        return this.frameOpacity;
    }

    /** {@inheritDoc} */
    public void setBackgroundOpacity(double frameOpacity)
    {
        this.frameOpacity = frameOpacity;
    }

    /** {@inheritDoc} */
    public Color[] getBackgroundColor()
    {
        return new Color[] {this.frameColor1, this.frameColor2};
    }

    /** {@inheritDoc} */
    public void setTitleBarColor(Color color1, Color color2)
    {
        if (frameColor1 == null || frameColor2 == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.titleBarColor1 = color1;
        this.titleBarColor2 = color2;
    }

    /** {@inheritDoc} */
    public Color[] getTitleBarColor()
    {
        return new Color[] {this.titleBarColor1, this.titleBarColor2};
    }

    /** {@inheritDoc} */
    public Color getMinimizeButtonColor()
    {
        return minimizeButtonColor;
    }

    /** {@inheritDoc} */
    public void setMinimizeButtonColor(Color minimizeButtonColor)
    {
        if (frameColor1 == null || frameColor2 == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.minimizeButtonColor = minimizeButtonColor;
    }

    /** {@inheritDoc} */
    public void setBackgroundColor(Color frameColor1, Color frameColor2)
    {
        if (frameColor1 == null || frameColor2 == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.frameColor1 = frameColor1;
        this.frameColor2 = frameColor2;
    }

    /** {@inheritDoc} */
    public boolean isDrawTitleBar()
    {
        return this.drawTitleBar;
    }

    /** {@inheritDoc} */
    public void setDrawTitleBar(boolean drawTitleBar)
    {
        this.drawTitleBar = drawTitleBar;
    }

    /** {@inheritDoc} */
    public boolean isAllowResize()
    {
        return this.allowResize;
    }

    /** {@inheritDoc} */
    public void setAllowResize(boolean allowResize)
    {
        this.allowResize = allowResize;
    }

    /** {@inheritDoc} */
    public boolean isAllowMove()
    {
        return this.allowMove;
    }

    /** {@inheritDoc} */
    public void setAllowMove(boolean allowMove)
    {
        this.allowMove = allowMove;
    }
}
