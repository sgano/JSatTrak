/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import java.awt.*;

/**
 * Attributes to control how a {@link ScrollFrame} is rendered.
 *
 * @author pabercrombie
 * @version $Id $
 * @see ScrollFrame
 */
public interface FrameAttributes
{
    /**
     * Get the color of the text in the frame.
     *
     * @return Text color.
     *
     * @see #setForegroundColor(java.awt.Color)
     */
    Color getForegroundColor();

    /**
     * Set the color of the text in the frame.
     *
     * @param textColor New text color.
     *
     * @see #getForegroundColor()
     */
    void setForegroundColor(Color textColor);

    /**
     * Get the font used to render text.
     *
     * @return frame font.
     *
     * @see #setFont(java.awt.Font)
     */
    Font getFont();

    /**
     * Set the font used to render text.
     *
     * @param font New frame font.
     *
     * @see #getFont()
     */
    void setFont(Font font);

    /**
     * Get the size of the icon in the frame title bar.
     *
     * @return Icon size.
     *
     * @see #setIconSize(java.awt.Dimension)
     */
    Dimension getIconSize();

    /**
     * Set the size of each icon in the frame title bar.
     *
     * @param size New size.
     *
     * @see #getIconSize()
     */
    void setIconSize(Dimension size);

    /**
     * Get the amount of space, in pixels, to leave between an icon in the frame and surrounding text and shapes.
     *
     * @return Icon space in pixels.
     *
     * @see #setIconSpace(int)
     */
    int getIconSpace();

    /**
     * Set the amount of space, in pixels, to leave between an icon in the frame and surrounding text and shapes.
     *
     * @param iconSpace Icon space in pixels.
     *
     * @see #getIconSpace()
     */
    void setIconSpace(int iconSpace);

    /**
     * Get the opacity of the text and images in the frame.
     *
     * @return Opacity of text and images.
     *
     * @see #setForegroundOpacity(double)
     * @see #getBackgroundOpacity()
     */
    double getForegroundOpacity();

    /**
     * Set the opacity of the frame text and images.
     *
     * @param textOpacity New opacity.
     *
     * @see #getForegroundOpacity()
     * @see #setBackgroundOpacity(double)
     */
    void setForegroundOpacity(double textOpacity);

    /**
     * Get the opacity of the frame.
     *
     * @return Frame opacity.
     *
     * @see #setBackgroundOpacity(double)
     */
    double getBackgroundOpacity();

    /**
     * Set the opacity of the frame.
     *
     * @param frameOpacity New frame opacity.
     *
     * @see #getBackgroundOpacity()
     */
    void setBackgroundOpacity(double frameOpacity);

    /**
     * Get the colors that make up the frame's background gradient.
     *
     * @return Two element array containing the colors in the background gradient.
     *
     * @see #setBackgroundColor(java.awt.Color, java.awt.Color)
     */
    Color[] getBackgroundColor();

    /**
     * Set the colors in the background gradient of the frame.
     *
     * @param frameColor1 First color in frame gradient.
     * @param frameColor2 Second color in frame gradient.
     *
     * @see #getBackgroundColor()
     */
    void setBackgroundColor(Color frameColor1, Color frameColor2);

    /**
     * Get the colors that make up the frame's title bar gradient.
     *
     * @return Two element array containing the colors in the title bar gradient.
     *
     * @see #setTitleBarColor(java.awt.Color, java.awt.Color)
     */
    Color[] getTitleBarColor();

    /**
     * Set the colors in the title bar gradient.
     *
     * @param color1 First color in the title bar gradient.
     * @param color2 Second color in the title bar gradient.
     *
     * @see #getBackgroundColor()
     */
    void setTitleBarColor(Color color1, Color color2);

    /**
     * Get the color of the minimize button drawn in the upper right corner of the frame.
     *
     * @return Color of the minimize button.
     */
    Color getMinimizeButtonColor();

    /**
     * Set the color of the minimize button drawn in the upper right corner of the frame.
     *
     * @param color Color of the minimize button.
     */
    void setMinimizeButtonColor(Color color);

    /**
     * Does the frame have a title bar?
     *
     * @return True if the frame will draw a title bar.
     */
    boolean isDrawTitleBar();

    /**
     * Set whether the frame has a title bar.
     *
     * @param drawTitleBar True if the frame will draw a title bar.
     */
    void setDrawTitleBar(boolean drawTitleBar);

    /**
     * Can the user resize the frame by dragging the border?
     *
     * @return True if the user is allowed to resize the frame.
     */
    boolean isAllowResize();

    /**
     * Set whether the user resize the frame by dragging the border.
     *
     * @param allowResize True if the user is allowed to resize the frame.
     */
    void setAllowResize(boolean allowResize);

    /**
     * Can the user move the frame by dragging the title bar?
     *
     * @return True if the user is allowed to move the frame.
     */
    boolean isAllowMove();

    /**
     * Can the user move the frame by dragging the title bar?
     *
     * @param allowMove True if the user is allowed to move the frame.
     */
    void setAllowMove(boolean allowMove);
}
