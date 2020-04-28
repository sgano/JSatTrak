/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.event.*;

/**
 * A scrollbar component. The scrollable range is defined by four values: min, max, value, and extent. {@code value} is
 * the current position of the scroll bar. {@code extent} represents the visible region. The four values must always
 * satisfy this relationship:
 * <p/>
 * <pre>
 *   min &lt;= value &lt;= value + extent &lt;= max
 * </pre>
 *
 * @author pabercrombie
 * @version $Id: ScrollBar.java 14107 2010-11-12 20:27:06Z dcollins $
 */
public class ScrollBar implements Renderable
{
    public static final String UNIT_UP = "gov.nasa.util.ScrollBar.UnitUp";
    public static final String UNIT_DOWN = "gov.nasa.util.ScrollBar.UnitDown";
    public static final String BLOCK_UP = "gov.nasa.util.ScrollBar.BlockUp";
    public static final String BLOCK_DOWN = "gov.nasa.util.ScrollBar.BlockDown";

    protected int minValue = 0;
    protected int maxValue = 100;
    protected int value = 0;
    protected int extent = 0;

    protected int unitIncrement = 5;
    protected int scrollArrowHeight = 15;

    protected PickSupport pickSupport = new PickSupport();

    protected Rectangle bounds = new Rectangle();
    protected Rectangle scrollBounds = new Rectangle();

    protected Insets arrowInsets = new Insets(1, 1, 1, 1);

    protected String orientation;

    protected double opacity = 1.0;
    protected Color lineColor = Color.BLACK;

    protected Color knobColor1 = new Color(29, 78, 169);
    protected Color knobColor2 = new Color(93, 158, 223);

    // Support for long-running scroll operations
    protected int autoScrollDelay = 100;
    protected boolean autoScrolling;
    protected String autoScrollIncrement;

    // UI controls
    protected HotSpot scrollUpControl;
    protected HotSpot scrollDownControl;
    protected HotSpot scrollUpBlockControl;
    protected HotSpot scrollDownBlockControl;
    protected ScrollKnob scrollKnobControl;

    // Values computed once per frame and reused during the frame as needed.
    protected long frameNumber = -1;             // Identifies frame used to calculate these values
    protected Rectangle scrollUpControlBounds;   // Bounds of the "up arrow" control
    protected Rectangle scrollDownControlBounds; // Bounds of the "down arrow" control
    protected Rectangle scrollKnobBounds;        // Bounds of the scroll knob
    protected Rectangle scrollUpBarBounds;       // Bounds of the scroll bar area above the knob
    protected Rectangle scrollDownBarBounds;     // Bounds of the scroll bar area below the knob

    /**
     * Create a scroll bar in the vertical orientation.
     *
     * @param parent The screen component that contains the scroll bar. Input events that cannot be handled by the
     *               scroll bar will be passed to this component. May be null.
     */
    public ScrollBar(HotSpot parent)
    {
        this.setOrientation(AVKey.VERTICAL);
        this.initializeUIControls(parent);
    }

    /**
     * Create a scroll bar with an orientation.
     *
     * @param orientation Either {@link AVKey#VERTICAL} or {@link AVKey#HORIZONTAL}.
     * @param parent      The screen component that contains the scroll bar. Input events that cannot be handled by the
     *                    scroll bar will be passed to this component. May be null.
     */
    public ScrollBar(HotSpot parent, String orientation)
    {
        this.setOrientation(orientation);
        this.initializeUIControls(parent);
    }

    /**
     * Initialize the objects that represent the UI controls.
     *
     * @param parent The screen component that contains the scroll bar. Input events that cannot be handled by the
     *               scroll bar will be passed to this component. May be null.
     */
    protected void initializeUIControls(HotSpot parent)
    {
        this.scrollKnobControl = new ScrollKnob(parent, this);
        this.scrollUpControl = new ScrollControl(parent, this, UNIT_UP);
        this.scrollDownControl = new ScrollControl(parent, this, UNIT_DOWN);
        this.scrollUpBlockControl = new ScrollControl(parent, this, BLOCK_UP);
        this.scrollDownBlockControl = new ScrollControl(parent, this, BLOCK_DOWN);
    }

    /**
     * Get the bounds of the scroll bar.
     *
     * @return Scroll bar bounds.
     */
    public Rectangle getBounds()
    {
        return bounds;
    }

    /**
     * Set the bounds of the scroll bar.
     *
     * @param bounds New bounds.
     */
    public void setBounds(Rectangle bounds)
    {
        this.bounds = bounds;

        this.scrollBounds = new Rectangle(bounds.x, bounds.y + this.getScrollArrowHeight(), bounds.width,
            bounds.height - 2 * this.getScrollArrowHeight());
    }

    /**
     * Draw the scroll bar.
     *
     * @param dc the <code>DrawContext</code> to be used
     */
    public void render(DrawContext dc)
    {
        // If an auto-scroll operation is in progress, adjust the scroll value and request that the scene be repainted
        // and a delay so that the next scroll value can be applied. 
        if (this.isAutoScrolling())
        {
            this.scroll(this.autoScrollIncrement);
            dc.setRedrawRequested(this.autoScrollDelay);
        }

        this.computeBounds(dc);

        if (dc.isPickingMode())
        {
            this.doPick(dc);
        }
        else
        {
            this.draw(dc);
        }
    }

    protected void computeBounds(DrawContext dc)
    {
        if (dc.getFrameTimeStamp() == this.frameNumber)
            return;

        this.frameNumber = dc.getFrameTimeStamp();

        int x1 = this.bounds.x;
        int y1 = this.bounds.y;

        int x2 = this.bounds.x + this.bounds.width;
        int y2 = this.bounds.y + this.bounds.height;

        int scrollControlSize = this.getScrollArrowHeight();

        if (AVKey.VERTICAL.equals(this.getOrientation()))
        {
            this.scrollDownControlBounds = new Rectangle(x1, y1, scrollControlSize, scrollControlSize);
            this.scrollUpControlBounds = new Rectangle(x1, y2 - scrollControlSize, scrollControlSize,
                scrollControlSize);

            int scrollAreaHeight = this.bounds.height - 2 * scrollControlSize;
            int position = (int) (scrollAreaHeight * this.getValueAsPercentage());

            int knobEnd = y2 - scrollControlSize - position - this.getKnobSize(scrollAreaHeight);
            this.scrollKnobBounds = new Rectangle(x1, knobEnd, scrollControlSize, this.getKnobSize(scrollAreaHeight));

            this.scrollDownBarBounds = new Rectangle(x1, y1 + scrollControlSize, scrollControlSize,
                knobEnd - y1 - scrollControlSize);
            int knobStart = (int) this.scrollKnobBounds.getMaxY();
            this.scrollUpBarBounds = new Rectangle(x1, knobStart, scrollControlSize,
                this.scrollUpControlBounds.y - knobStart);
        }
        else
        {
            this.scrollUpControlBounds = new Rectangle(x1, y1, scrollControlSize, scrollControlSize);
            this.scrollDownControlBounds = new Rectangle(x2 - scrollControlSize, y1, scrollControlSize,
                scrollControlSize);

            int scrollAreaWidth = this.bounds.width - 2 * scrollControlSize;
            int position = (int) (scrollAreaWidth * this.getValueAsPercentage());

            int knobStart = x1 + scrollControlSize + position;
            this.scrollKnobBounds = new Rectangle(knobStart, y1, this.getKnobSize(scrollAreaWidth), scrollControlSize);

            this.scrollUpBarBounds = new Rectangle(x1 + scrollControlSize, y1,
                this.scrollKnobBounds.x - scrollControlSize - x1, scrollControlSize);
            int knobEnd = (int) this.scrollKnobBounds.getMaxX();
            this.scrollDownBarBounds = new Rectangle(knobEnd, y1, this.scrollDownControlBounds.x - knobEnd,
                scrollControlSize);
        }
    }

    protected void draw(DrawContext dc)
    {
        GL gl = dc.getGL();
        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            oglStack.pushAttrib(gl,
                GL.GL_COLOR_BUFFER_BIT
                    | GL.GL_CURRENT_BIT
                    | GL.GL_LINE_BIT
                    | GL.GL_POLYGON_BIT);

            gl.glLineWidth(1f);
            OGLUtil.applyColor(gl, this.getLineColor(), this.getOpacity(), false);

            gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);

            // Draw scroll bar frame
            TreeUtil.drawRect(gl, this.bounds);

            // Draw boxes for up and down arrows
            TreeUtil.drawRect(gl, this.scrollDownControlBounds);
            TreeUtil.drawRect(gl, this.scrollUpControlBounds);

            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

            // Draw background gradient
            String gradientDirection;
            if (AVKey.VERTICAL.equals(this.getOrientation()))
                gradientDirection = AVKey.HORIZONTAL;
            else
                gradientDirection = AVKey.VERTICAL;
            TreeUtil.drawRectWithGradient(gl, this.scrollKnobBounds, knobColor2, knobColor1, this.getOpacity(),
                gradientDirection);

            // Draw a border around the knob
            OGLUtil.applyColor(gl, this.getLineColor(), this.getOpacity(), false);
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
            TreeUtil.drawRect(gl, this.scrollKnobBounds);

            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
            if (AVKey.VERTICAL.equals(this.getOrientation()))
            {
                this.drawTriangle(dc, 90, this.scrollUpControlBounds, arrowInsets);
                this.drawTriangle(dc, -90, this.scrollDownControlBounds, arrowInsets);
            }
            else
            {
                this.drawTriangle(dc, 180, this.scrollUpControlBounds, arrowInsets);
                this.drawTriangle(dc, 0, this.scrollDownControlBounds, arrowInsets);
            }
        }
        finally
        {
            oglStack.pop(gl);
        }
    }

    protected void doPick(DrawContext dc)
    {
        try
        {
            this.pickSupport.clearPickList();
            this.pickSupport.beginPicking(dc);

            TreeUtil.drawPickableRect(dc, this.pickSupport, this.scrollDownControl, this.scrollDownControlBounds);
            TreeUtil.drawPickableRect(dc, this.pickSupport, this.scrollUpControl, this.scrollUpControlBounds);
            TreeUtil.drawPickableRect(dc, this.pickSupport, this.scrollDownBlockControl, this.scrollDownBarBounds);
            TreeUtil.drawPickableRect(dc, this.pickSupport, this.scrollUpBlockControl, this.scrollUpBarBounds);

            // The knob, for dragging
            TreeUtil.drawPickableRect(dc, this.pickSupport, this.scrollKnobControl, this.scrollKnobBounds);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, dc.getPickPoint(), dc.getCurrentLayer());
        }
    }

    /**
     * Get the minimum value in the scroll range.
     *
     * @return Minimum value.
     */
    public int getMinValue()
    {
        return minValue;
    }

    /**
     * Set the minimum value in the scroll range.
     *
     * @param minValue New minimum.
     */
    public void setMinValue(int minValue)
    {
        if (minValue < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "minValue < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.minValue = minValue;

        if (this.getValue() < this.minValue)
            this.setValue(this.minValue);
    }

    /**
     * Get the maximum value in the scroll range.
     *
     * @return Maximum value.
     *
     * @see #getMinValue()
     * @see #setMaxValue(int)
     */
    public int getMaxValue()
    {
        return maxValue;
    }

    /**
     * Set the maximum value in the scroll range.
     *
     * @param maxValue New maximum.
     */
    public void setMaxValue(int maxValue)
    {
        if (maxValue < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "maxValue < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.maxValue = maxValue;

        if (this.getValue() > this.maxValue)
            this.setValue(this.maxValue);
    }

    /**
     * Get the current value of the scroll bar.
     *
     * @return Current value.
     */
    public int getValue()
    {
        return this.value;
    }

    /**
     * Set the value of the scroll bar.
     *
     * @param value New value. The value will be clamped to the range [minValue : maxValue - extent].
     */
    public void setValue(int value)
    {
        this.value = WWMath.clamp(value, this.getMinValue(), this.getMaxValue() - this.getExtent());
    }

    /**
     * Get the unit increment. This is the amount that the scroll bar scrolls by when one of the arrow controls is
     * clicked.
     *
     * @return Unit increment.
     *
     * @see #setUnitIncrement(int)
     */
    public int getUnitIncrement()
    {
        return this.unitIncrement;
    }

    /**
     * Set the unit increment.
     *
     * @param unitIncrement New unit increment. Must be a positive number.
     *
     * @see #getUnitIncrement()
     */
    public void setUnitIncrement(int unitIncrement)
    {
        if (unitIncrement <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "unitIncrement <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.unitIncrement = unitIncrement;
    }

    /**
     * Get the block increment. This is the amount that the scroll bar scrolls by when the bar is clicked above or below
     * the knob.
     *
     * @return The block increment. This implementation returns the extent, so the scroll bar will adjust by a full
     *         visible page.
     */
    public int getBlockIncrement()
    {
        return this.extent;
    }

    /**
     * Get the scroll bar orientation.
     *
     * @return The scroll bar orientation, either {@link AVKey#VERTICAL} or {@link AVKey#HORIZONTAL}.
     */
    public String getOrientation()
    {
        return this.orientation;
    }

    /**
     * Set the scroll bar orientation.
     *
     * @param orientation The scroll bar orientation, either {@link AVKey#VERTICAL} or {@link AVKey#HORIZONTAL}.
     */
    public void setOrientation(String orientation)
    {
        if (orientation == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.orientation = orientation;
    }

    /**
     * Adjust the scroll value.
     *
     * @param amount Amount to add to the current value. A positive value indicates a scroll down; a negative value
     *               indicates a scroll up.
     */
    public void scroll(int amount)
    {
        this.setValue(this.getValue() + amount);
    }

    /**
     * Adjust the scroll bar by the unit amount or the block amount.
     *
     * @param amount One of {@link #UNIT_UP}, {@link #UNIT_DOWN}, {@link #BLOCK_UP}, or {@link #BLOCK_DOWN}.
     */
    public void scroll(String amount)
    {
        if (UNIT_UP.equals(amount))
            this.scroll(-this.getUnitIncrement());
        else if (UNIT_DOWN.equals(amount))
            this.scroll(this.getUnitIncrement());
        else if (BLOCK_UP.equals(amount))
            this.scroll(-this.getBlockIncrement());
        else if (BLOCK_DOWN.equals(amount))
            this.scroll(this.getBlockIncrement());
    }

    /**
     * Start an auto-scroll operation. During auto-scroll, the scroll bar will adjust its value and repaint continuously
     * until the auto-scroll is stopped.
     *
     * @param increment Amount to adjust scroll bar each time. One of {@link #UNIT_UP}, {@link #UNIT_DOWN}, {@link
     *                  #BLOCK_UP}, or {@link #BLOCK_DOWN}.
     *
     * @see #stopAutoScroll()
     * @see #isAutoScrolling()
     * @see #scroll(String)
     */
    public void startAutoScroll(String increment)
    {
        this.autoScrolling = true;
        this.autoScrollIncrement = increment;
    }

    /**
     * Stop an auto-scroll operation.
     *
     * @see #startAutoScroll(String)
     * @see #isAutoScrolling()
     */
    public void stopAutoScroll()
    {
        this.autoScrolling = false;
    }

    /**
     * Is the scroll bar auto-scrolling?
     *
     * @return True if an auto-scroll operation is in progress.
     *
     * @see #startAutoScroll(String)
     * @see #stopAutoScroll()
     */
    public boolean isAutoScrolling()
    {
        return this.autoScrolling;
    }

    /**
     * Get the extent. The extent the amount of the scrollable region that is visible.
     *
     * @return The extent.
     *
     * @see #setExtent(int)
     */
    public int getExtent()
    {
        return extent;
    }

    /**
     * Set the extent. The extent the amount of the scrollable region that is visible. This method may change the value
     * of the scroll bar to maintain the relationship:
     * <pre>
     *   min &lt;= value &lt;= value + extent &lt;= max
     * </pre>
     *
     * @param extent New extent. If {@code extent} is greater than the range of the scroll bar (max - min), then the
     *               extent will be set to the maximum valid value.
     *
     * @see #getExtent()
     */
    public void setExtent(int extent)
    {
        this.extent = Math.min(extent, this.getMaxValue() - this.getMinValue());
        if (this.getValue() + this.getExtent() > this.getMaxValue())
            this.setValue(this.getMaxValue() - this.getExtent());
    }

    /**
     * Get the value as a percentage of the scroll range.
     *
     * @return Current value as percentage.
     */
    public double getValueAsPercentage()
    {
        return (double) this.getValue() / (this.getMaxValue() - this.getMinValue());
    }

    /**
     * Get the size of the scroll knob, in pixels.
     *
     * @param scrollAreaSize The size of the scroll area, in pixels.
     *
     * @return Size of the scroll knob, in pixels.
     */
    protected int getKnobSize(int scrollAreaSize)
    {
        return (int) (scrollAreaSize * ((double) this.getExtent() / (this.getMaxValue() - this.minValue)));
    }

    /**
     * Get the height of the scroll arrow controls at the top and bottom of the scroll bar.
     *
     * @return Height of arrow control, in pixels.
     */
    protected int getScrollArrowHeight()
    {
        return this.scrollArrowHeight;
    }

    /**
     * Get the color used to draw the lines of the scroll bar boundary and the scroll arrows.
     *
     * @return Color used for the scroll bar lines.
     *
     * @see #setLineColor(java.awt.Color)
     * @see #getKnobColor()
     */
    public Color getLineColor()
    {
        return lineColor;
    }

    /**
     * Set the color of the lines of the scroll bar boundary. This color is also used for the arrows in the scroll
     * controls.
     *
     * @param color Color for lines and scroll arrows.
     *
     * @see #getLineColor()
     * @see #setKnobColor(java.awt.Color, java.awt.Color)
     */
    public void setLineColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.lineColor = color;
    }

    /**
     * Set the color of scroll knob. The knob is drawn with a gradient made up of two colors.
     *
     * @param color1 First color in the gradient.
     * @param color2 Second color in the gradient.
     *
     * @see #getKnobColor()
     * @see #setLineColor(java.awt.Color)
     */
    public void setKnobColor(Color color1, Color color2)
    {
        if (color1 == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (color2 == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.knobColor1 = color1;
        this.knobColor2 = color2;
    }

    /**
     * Get the color of scroll knob. The knob is drawn with a gradient made up of two colors.
     *
     * @return Two element array containing the two colors that form the gradient.
     *
     * @see #setKnobColor(java.awt.Color, java.awt.Color)
     * @see #getLineColor()
     */
    public Color[] getKnobColor()
    {
        return new Color[] {this.knobColor1, this.knobColor2};
    }

    public double getOpacity()
    {
        return opacity;
    }

    public void setOpacity(double opacity)
    {
        this.opacity = opacity;
    }

    protected void drawTriangle(DrawContext dc, float rotation, Rectangle bounds, Insets insets)
    {
        GL gl = dc.getGL();

        try
        {
            gl.glPushMatrix();

            Dimension triSize = bounds.getSize();
            int halfHeight = triSize.height / 2;
            int halfWidth = triSize.width / 2;

            gl.glTranslated(bounds.x + halfWidth, bounds.y + halfHeight, 1.0);
            gl.glRotatef(rotation, 0, 0, 1);

            halfHeight = halfHeight - insets.top - insets.bottom;
            halfWidth = halfWidth - insets.left - insets.right;

            gl.glBegin(GL.GL_TRIANGLES);
            gl.glVertex2f(-halfWidth, halfHeight);
            gl.glVertex2f(halfWidth, 0);
            gl.glVertex2f(-halfWidth, -halfHeight);
            gl.glEnd();
        }
        finally
        {
            gl.glPopMatrix();
        }
    }

    /** Control for the scroll arrows and areas of the scroll bar above and below the knob. */
    public class ScrollControl extends TreeHotSpot
    {
        protected ScrollBar scrollBar;
        protected String adjustment;

        public ScrollControl(HotSpot parent, ScrollBar owner, String adjustment)
        {
            super(parent);
            this.scrollBar = owner;
            this.adjustment = adjustment;
        }

        @Override
        public void mousePressed(MouseEvent event)
        {
            if (event.getButton() == MouseEvent.BUTTON1)
                scrollBar.startAutoScroll(this.adjustment);
        }

        @Override
        public void mouseReleased(MouseEvent event)
        {
            if (event.getButton() == MouseEvent.BUTTON1)
                this.scrollBar.stopAutoScroll();
        }

        @Override
        public void selected(SelectEvent event)
        {
            // Don't let super class pass this event to parent component
        }

        @Override
        public void mouseClicked(MouseEvent event)
        {
            // Don't let super class pass this event to parent component
        }
    }

    /** Control for dragging the scroll knob. */
    public class ScrollKnob extends DragControl
    {
        protected ScrollBar scrollBar;
        protected int dragRefValue;

        public ScrollKnob(HotSpot parent, ScrollBar owner)
        {
            super(parent);
            this.scrollBar = owner;
        }

        @Override
        public void mouseClicked(MouseEvent event)
        {
            // Don't let super class pass this event to parent component
        }

        @Override
        protected void beginDrag(Point point)
        {
            super.beginDrag(point);
            this.dragRefValue = this.scrollBar.getValue();
        }

        protected void drag(Point point)
        {
            int delta;
            int adjustment;
            int screenDimension;

            if (AVKey.VERTICAL.equals(scrollBar.getOrientation()))
            {
                delta = point.y - this.dragRefPoint.y;
                screenDimension = this.scrollBar.scrollBounds.height;
            }
            else
            {
                delta = point.x - this.dragRefPoint.x;
                screenDimension = this.scrollBar.scrollBounds.width;
            }

            int scrollRange = this.scrollBar.getMaxValue() - this.scrollBar.getMinValue();
            adjustment = (int) (((double) delta / screenDimension) * scrollRange);

            this.scrollBar.setValue(this.dragRefValue + adjustment);
        }
    }
}
