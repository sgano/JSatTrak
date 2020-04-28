/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.URL;

/**
 * A frame that can scroll its contents.
 *
 * @author pabercrombie
 * @version $Id: ScrollFrame.java 14221 2010-12-10 23:47:55Z pabercrombie $
 */
public class ScrollFrame extends DragControl implements Renderable
{
    protected WorldWindow wwd;

    protected FrameAttributes normalAttributes = new BasicTreeAttributes();
    protected FrameAttributes highlightAttributes = new BasicTreeAttributes();

    protected String frameTitle;

    protected Scrollable contents;

    protected Offset screenLocation;

    protected Insets insets = new Insets(0, 2, 2, 2);
    protected int titleBarHeight = 25;
    protected int scrollBarSize = 15;
    protected int frameBorder = 3;
    /** Space in pixels between the title bar icon and the left edge of the frame. */
    protected int iconInset = 5;

    protected OGLStackHandler oglStackHandler = new OGLStackHandler();

    protected ScrollBar verticalScrollBar;
    protected ScrollBar horizontalScrollBar;

    protected PickSupport pickSupport = new PickSupport();

    protected boolean minimized = false;

    protected Size size = Size.fromPixels(250, 300);

    /** Image source for the icon drawn in the upper left corner of the frame. */
    protected Object iconImageSource;
    /** Texture loaded from {@link #iconImageSource}. */
    protected BasicWWTexture texture;

    /** An animation to play when the frame is minimized or maximized. */
    protected Animation minimizeAnimation;
    /** The active animation that is currently playing. */
    protected Animation animation;
    /** Delay in milliseconds between frames of an animation. */
    protected int animationDelay = 5;

    // UI controls
    protected HotSpot minimizeButton;
    protected FrameResizeControl frameResizeControl;
    protected int resizePickWidth = 10;

    // Computed each frame
    protected long frameNumber = -1;
    protected Point2D screenPoint;
    protected Rectangle frameBounds;     // Bounds of the full frame
    protected Rectangle innerBounds;     // Bounds of the frame inside the frame border
    protected Rectangle contentBounds;   // Bounds of the visible portion of the tree
    protected Rectangle treeContentBounds;
    protected Dimension contentSize;     // Size of the scroll frame contents
    protected Dimension frameSize;
    protected boolean highlighted;
    protected boolean showVerticalScrollbar;
    protected boolean showHorizontalScrollbar;

    public ScrollFrame(WorldWindow wwd)
    {
        super(null);
        this.wwd = wwd;
        this.initializeUIControls();
    }

    public ScrollFrame(WorldWindow wwd, int x, int y)
    {
        this(wwd, new Offset((double) x, (double) y, AVKey.PIXELS, AVKey.PIXELS));
    }

    public ScrollFrame(WorldWindow wwd, Offset screenLocation)
    {
        super(null);
        this.wwd = wwd;
        this.setScreenLocation(screenLocation);
        this.initializeUIControls();
    }

    public Scrollable getContents()
    {
        return contents;
    }

    public void setContents(Scrollable contents)
    {
        this.contents = contents;
    }

    protected void initializeUIControls()
    {
        this.minimizeAnimation = new WindowShadeAnimation(this);
        this.frameResizeControl = new FrameResizeControl(this);

        this.minimizeButton = new TreeHotSpot(this)
        {
            public void selected(SelectEvent event)
            {
                if (event.isLeftClick())
                {
                    ScrollFrame.this.setMinimized(!ScrollFrame.this.isMinimized());
                }
            }
        };

        this.verticalScrollBar = new ScrollBar(this, AVKey.VERTICAL);
        this.horizontalScrollBar = new ScrollBar(this, AVKey.HORIZONTAL);
    }

    /**
     * Get the bounds of the tree frame.
     *
     * @param dc Draw context
     *
     * @return The bounds of the tree frame on screen, in screen coordinates (origin at upper left).
     */
    public Rectangle getBounds(DrawContext dc)
    {
        this.computeBounds(dc);

        return new Rectangle((int) this.screenPoint.getX(), (int) this.screenPoint.getY(), this.frameSize.width,
            this.frameSize.height);
    }

    public Rectangle getVisibleBounds()
    {
        return this.contentBounds;
    }

    public void render(DrawContext dc)
    {
        Offset screenLocation = this.getScreenLocation();
        if (screenLocation == null)
            return;

        this.computeBounds(dc);

        Point pickPoint = dc.getPickPoint();
        if (pickPoint != null)
            this.setHighlighted(this.getBounds(dc).contains(pickPoint));

        try
        {
            this.beginRendering(dc);

            if (this.isMinimized())
                this.drawMinimized(dc);
            else
                this.drawMaximized(dc);
        }
        finally
        {
            this.endRendering(dc);
        }
    }

    protected void stepAnimation(DrawContext dc)
    {
        if (this.animation != null)
        {
            this.animation.step();

            if (this.animation.hasNext())
                dc.setRedrawRequested(this.animationDelay);
            else
                this.animation = null;
        }
    }

    /**
     * Compute the bounds of the content frame, if the bounds have not already been computed in this frame.
     *
     * @param dc Draw context.
     */
    protected void computeBounds(DrawContext dc)
    {
        if (dc.getFrameTimeStamp() == this.frameNumber)
            return;

        this.stepAnimation(dc);

        Rectangle viewport = dc.getView().getViewport();

        this.screenPoint = this.screenLocation.computeOffset(viewport.width, viewport.height, 1.0, 1.0);

        this.contentSize = this.contents.getSize(dc);

        // Compute point in OpenGL coordinates
        Point upperLeft = new Point((int) this.screenPoint.getX(), (int) (viewport.height - this.screenPoint.getY()));

        this.frameSize = this.getSize().compute(viewport.width, viewport.height, this.contentSize.width,
            this.contentSize.height);

        this.frameBounds = new Rectangle(upperLeft.x, upperLeft.y - frameSize.height, frameSize.width,
            frameSize.height);
        this.innerBounds = new Rectangle(upperLeft.x + this.frameBorder,
            upperLeft.y - frameSize.height + this.frameBorder, frameSize.width - this.frameBorder * 2,
            frameSize.height - this.frameBorder * 2);

        // Try laying out the frame without scroll bars
        this.contentBounds = this.computeBounds(dc, false, false);

        this.showVerticalScrollbar = this.contentSize.height > this.contentBounds.height;
        this.showHorizontalScrollbar = this.contentSize.width > this.contentBounds.width;

        // If we need a scroll bar, compute the bounds again to take into account the space occupied by the scroll bar.
        if (this.showHorizontalScrollbar || this.showVerticalScrollbar)
            this.contentBounds = this.computeBounds(dc, this.showVerticalScrollbar, this.showHorizontalScrollbar);

        // Add a little extra space to the scroll bar max value to allow the tree to be scrolled a little
        // bit further than its height. This avoids chopping off the bottom of descending characters because the
        // text is too close to the scissor box.
        final int scrollPadding = 10;

        this.verticalScrollBar.setMaxValue(this.contentSize.height + scrollPadding);
        this.verticalScrollBar.setExtent(this.contentBounds.height);

        this.horizontalScrollBar.setMaxValue(this.contentSize.width + scrollPadding);
        this.horizontalScrollBar.setExtent(this.contentBounds.width);

        this.treeContentBounds = new Rectangle(this.contentBounds);
        this.treeContentBounds.x -= this.horizontalScrollBar.getValue();
        this.treeContentBounds.y += this.verticalScrollBar.getValue();

        this.frameNumber = dc.getFrameTimeStamp();
    }

    /**
     * Compute the content bounds, taking into account the frame size and the presence of scroll bars.
     *
     * @param dc                      Draw context.
     * @param showVerticalScrollBar   True if the frame will have a vertical scroll bar. A vertical scroll bar will make
     *                                the content frame narrower.
     * @param showHorizontalScrollBar True if the frame will have a horizontal scroll bar. A horizontal scroll bar will
     *                                make the content frame shorter.
     *
     * @return The bounds of the content frame.
     */
    protected Rectangle computeBounds(DrawContext dc, boolean showVerticalScrollBar, boolean showHorizontalScrollBar)
    {
        int hScrollBarSize = (showHorizontalScrollBar ? this.scrollBarSize : 0);
        int vScrollBarSize = (showVerticalScrollBar ? this.scrollBarSize : 0);

        int titleBarHeight = this.getActiveAttributes().isDrawTitleBar() ? this.titleBarHeight : 0;

        return new Rectangle(this.innerBounds.x + this.insets.left,
            this.innerBounds.y + this.insets.bottom + hScrollBarSize,
            this.innerBounds.width - this.insets.right - this.insets.left - vScrollBarSize,
            this.innerBounds.height - this.insets.bottom - this.insets.top - titleBarHeight - hScrollBarSize);
    }

    /**
     * Get the smallest dimension that the frame can draw itself. This user is not allowed to resize the frame to be
     * smaller than this dimension.
     *
     * @return The frame's minimum size.
     */
    protected Dimension getMinimumSize()
    {
        // Reserve enough space to draw the border, both scroll bars, and the title bar
        int minWidth = this.frameBorder * 2 + this.scrollBarSize * 3; // left scroll arrow + right + vertical scroll bar
        int minHeight = this.frameBorder * 2 + this.scrollBarSize * 3
            + this.titleBarHeight; // Up arrow + down arrow + horizontal scroll bar
        return new Dimension(minWidth, minHeight);
    }

    protected void drawMaximized(DrawContext dc)
    {
        this.drawFrame(dc);

        GL gl = dc.getGL();
        gl.glEnable(GL.GL_SCISSOR_TEST);
        gl.glScissor(this.contentBounds.x, this.contentBounds.y, this.contentBounds.width, this.contentBounds.height);

        this.contents.renderScrollable(dc, this.treeContentBounds);
    }

    protected void drawMinimized(DrawContext dc)
    {
        GL gl = dc.getGL();

        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            oglStack.pushAttrib(gl, GL.GL_COLOR_BUFFER_BIT | GL.GL_CURRENT_BIT);
            oglStack.pushModelviewIdentity(gl);

            FrameAttributes attributes = this.getActiveAttributes();

            gl.glTranslated(this.frameBounds.x, this.frameBounds.y, 0.0);

            if (!dc.isPickingMode())
            {
                Color[] color = attributes.getBackgroundColor();

                OGLUtil.applyColor(gl, color[0], 1.0, false);
                FrameFactory.drawShape(dc, FrameFactory.SHAPE_RECTANGLE, this.frameBounds.width,
                    this.frameBounds.height, GL.GL_LINE_STRIP, 5);

                gl.glLoadIdentity();
                gl.glTranslated(this.innerBounds.x, this.innerBounds.y, 0.0); // Translate back to inner frame

                TreeUtil.drawRectWithGradient(gl, new Rectangle(0, 0, this.innerBounds.width, this.innerBounds.height),
                    color[0], color[1], attributes.getBackgroundOpacity(), AVKey.VERTICAL);
            }
            else
            {
                Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();
                this.pickSupport.addPickableObject(colorCode, this, null, false);
                gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                gl.glScaled(this.frameSize.width, this.frameSize.height, 1d);
                dc.drawUnitQuad();
            }

            // Draw title bar
            if (attributes.isDrawTitleBar())
            {
                gl.glLoadIdentity();
                gl.glTranslated(this.innerBounds.x, this.innerBounds.y + this.innerBounds.height - this.titleBarHeight, 0);
                this.drawTitleBar(dc);
            }
        }
        finally
        {
            oglStack.pop(gl);
        }
    }

    protected void drawFrame(DrawContext dc)
    {
        GL gl = dc.getGL();

        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            oglStack.pushAttrib(gl, GL.GL_COLOR_BUFFER_BIT | GL.GL_CURRENT_BIT);

            oglStack.pushModelviewIdentity(gl);

            FrameAttributes attributes = this.getActiveAttributes();

            gl.glTranslated(this.frameBounds.x, this.frameBounds.y, 0.0);

            if (!dc.isPickingMode())
            {
                Color[] color = attributes.getBackgroundColor();

                OGLUtil.applyColor(gl, color[0], 1.0, false);
                FrameFactory.drawShape(dc, FrameFactory.SHAPE_RECTANGLE, this.frameBounds.width,
                    this.frameBounds.height, GL.GL_LINE_STRIP, 5);

                gl.glLoadIdentity();
                gl.glTranslated(this.innerBounds.x, this.innerBounds.y, 0.0); // Translate back inner frame

                TreeUtil.drawRectWithGradient(gl, new Rectangle(0, 0, this.innerBounds.width, this.innerBounds.height),
                    color[0], color[1], attributes.getBackgroundOpacity(), AVKey.VERTICAL);
            }
            else
            {
                int frameHeight = this.frameBounds.height;
                int frameWidth = this.frameBounds.width;

                // Draw draggable frame
                TreeUtil.drawPickableRect(dc, this.pickSupport, this, new Rectangle(0, 0, frameWidth, frameHeight));

                if (attributes.isAllowResize())
                {
                    Color color = dc.getUniquePickColor();
                    int colorCode = color.getRGB();
                    pickSupport.addPickableObject(colorCode, this.frameResizeControl);
                    gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                    FrameFactory.drawShape(dc, FrameFactory.SHAPE_RECTANGLE, this.frameBounds.width,
                        this.frameBounds.height, GL.GL_LINE_STRIP, this.resizePickWidth);
                }
            }

            // Draw a vertical scroll bar if the tree extends beyond the visible bounds
            if (this.showVerticalScrollbar)
            {
                int x1 = this.innerBounds.width - this.insets.right - this.scrollBarSize;
                int y1 = this.insets.bottom;
                if (this.showHorizontalScrollbar)
                    y1 += this.scrollBarSize;

                Rectangle scrollBarBounds = new Rectangle(x1, y1, this.scrollBarSize, this.contentBounds.height);

                this.verticalScrollBar.setBounds(scrollBarBounds);
                this.verticalScrollBar.render(dc);
            }

            // Draw a horizontal scroll bar if the tree extends beyond the visible bounds
            if (this.showHorizontalScrollbar)
            {
                int x1 = this.insets.right;
                int y1 = this.insets.bottom;

                Rectangle scrollBarBounds = new Rectangle(x1, y1, this.contentBounds.width, this.scrollBarSize);

                this.horizontalScrollBar.setBounds(scrollBarBounds);
                this.horizontalScrollBar.render(dc);
            }

            // Draw title bar
            if (attributes.isDrawTitleBar())
            {
                gl.glTranslated(0, this.innerBounds.height - this.titleBarHeight, 0);
                this.drawTitleBar(dc);
            }
        }
        finally
        {
            oglStack.pop(gl);
        }
    }

    protected void drawTitleBar(DrawContext dc)
    {
        GL gl = dc.getGL();

        FrameAttributes attributes = this.getActiveAttributes();

        int x = 0;
        int y = 0;

        if (!dc.isPickingMode())
        {
            Color[] color = attributes.getTitleBarColor();
            TreeUtil.drawRectWithGradient(gl, new Rectangle(0, 0, this.innerBounds.width, this.getTitleBarHeight()),
                color[0], color[1], attributes.getBackgroundOpacity(), AVKey.VERTICAL);

            OGLUtil.applyColor(gl, attributes.getForegroundColor(), 1.0, false);

            if (!this.isMinimized())
            {
                // Draw a line to separate the title bar from the frame
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(0, y);
                gl.glVertex2f(this.innerBounds.width, y);
                gl.glEnd();
            }

            // Draw icon in upper left corner
            BasicWWTexture texture = this.getTexture();
            OGLStackHandler oglStack = new OGLStackHandler();
            try
            {
                oglStack.pushAttrib(gl, GL.GL_CURRENT_BIT | GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT);

                gl.glEnable(GL.GL_TEXTURE_2D);

                if (texture != null && texture.bind(dc))
                {
                    Dimension iconSize = attributes.getIconSize();

                    oglStack.pushModelview(gl);

                    gl.glColor4d(1d, 1d, 1d, 1.0);

                    int vertAdjust = (this.titleBarHeight - iconSize.height) / 2;
                    TextureCoords texCoords = texture.getTexCoords();
                    gl.glTranslated(x + this.iconInset, y + vertAdjust, 1.0);
                    gl.glScaled((double) iconSize.width, (double) iconSize.height, 1d);
                    dc.drawUnitQuad(texCoords);

                    x += iconSize.getWidth() + attributes.getIconSpace();
                }
            }
            finally
            {
                oglStack.pop(gl);
            }

            // Draw text title
            String frameTitle = this.getFrameTitle();
            if (frameTitle != null)
            {
                TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
                    attributes.getFont());

                Rectangle2D textSize = textRenderer.getBounds(frameTitle);

                try
                {
                    textRenderer.begin3DRendering();
                    OGLUtil.applyColor(gl, Color.WHITE, 1.0, false);

                    int vertAdjust = (int) (this.titleBarHeight - textSize.getHeight()) / 2;
                    textRenderer.draw(frameTitle, x, y + vertAdjust);
                }
                finally
                {
                    textRenderer.end3DRendering();
                }
            }
        }

        this.drawMinimizeButton(dc);
    }

    protected void drawMinimizeButton(DrawContext dc)
    {
        GL gl = dc.getGL();

        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            oglStack.pushModelviewIdentity(gl);

            int buttonSize = this.scrollBarSize;

            gl.glTranslated(this.innerBounds.x + this.innerBounds.width - this.insets.left - buttonSize,
                this.innerBounds.y + this.innerBounds.height - (this.titleBarHeight - buttonSize) / 2 - buttonSize,
                1.0);

            if (!dc.isPickingMode())
            {
                Color color = this.getActiveAttributes().getMinimizeButtonColor();

                FrameAttributes attributes = this.getActiveAttributes();
                OGLUtil.applyColor(gl, color, attributes.getForegroundOpacity(), false);
                FrameFactory.drawShape(dc, FrameFactory.SHAPE_RECTANGLE, buttonSize, buttonSize, GL.GL_TRIANGLE_FAN, 5);

                OGLUtil.applyColor(gl, attributes.getForegroundColor(), false);
                FrameFactory.drawShape(dc, FrameFactory.SHAPE_RECTANGLE, buttonSize, buttonSize, GL.GL_LINE_STRIP, 5);

                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(buttonSize / 4, buttonSize / 2);
                gl.glVertex2f(buttonSize - buttonSize / 4, buttonSize / 2);
                gl.glEnd();
            }
            else
            {
                Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();

                this.pickSupport.addPickableObject(colorCode, this.minimizeButton, null, false);
                gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                gl.glScaled(buttonSize, buttonSize, 1d);
                dc.drawUnitQuad();
            }
        }
        finally
        {
            oglStack.pop(gl);
        }
    }

    protected void beginRendering(DrawContext dc)
    {
        GL gl = dc.getGL();
        GLU glu = dc.getGLU();

        this.oglStackHandler.pushAttrib(gl, GL.GL_DEPTH_BUFFER_BIT
            | GL.GL_COLOR_BUFFER_BIT
            | GL.GL_ENABLE_BIT
            | GL.GL_CURRENT_BIT
            | GL.GL_SCISSOR_BIT);

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glEnable(GL.GL_POLYGON_SMOOTH);
        gl.glDisable(GL.GL_DEPTH_TEST);

        // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
        // into the GL projection matrix.
        this.oglStackHandler.pushProjectionIdentity(gl);

        java.awt.Rectangle viewport = dc.getView().getViewport();

        glu.gluOrtho2D(0d, viewport.width, 0d, viewport.height);
        this.oglStackHandler.pushModelviewIdentity(gl);

        if (dc.isPickingMode())
        {
            this.pickSupport.clearPickList();
            this.pickSupport.beginPicking(dc);
        }
    }

    protected void endRendering(DrawContext dc)
    {
        if (dc.isPickingMode())
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, dc.getPickPoint(), dc.getCurrentLayer());
        }

        GL gl = dc.getGL();
        this.oglStackHandler.pop(gl);
    }

    /**
     * Get the location of the upper left corner of the tree, measured in screen coordinates with the origin at the
     * upper left corner of the screen.
     *
     * @return Screen location, measured in pixels from the upper left corner of the screen.
     */
    public Offset getScreenLocation()
    {
        return this.screenLocation;
    }

    /**
     * Set the location of the upper left corner of the tree, measured in screen coordinates with the origin at the
     * upper left corner of the screen.
     *
     * @param screenLocation New screen location.
     */
    public void setScreenLocation(Offset screenLocation)
    {
        this.screenLocation = screenLocation;
    }

    /**
     * Get the location of the upper left corner of the frame, measured from the upper left corner of the screen.
     *
     * @return The location of the upper left corner of the frame. This method will return null until the has been
     *         rendered.
     */
    protected Point2D getScreenPoint()
    {
        return this.screenPoint;
    }

    public FrameAttributes getAttributes()
    {
        return this.normalAttributes;
    }

    public void setAttributes(FrameAttributes attributes)
    {
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.normalAttributes = attributes;
    }

    public FrameAttributes getHighlightAttributes()
    {
        return this.highlightAttributes;
    }

    public void setHighlightAttributes(FrameAttributes attributes)
    {
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.highlightAttributes = attributes;
    }

    protected FrameAttributes getActiveAttributes()
    {
        if (this.isHighlighted())
            return this.getHighlightAttributes();
        else
            return this.getAttributes();
    }

    public boolean isMinimized()
    {
        return this.minimized;
    }

    public void setMinimized(boolean minimized)
    {
        if (minimized != this.isMinimized())
        {
            this.minimized = minimized;
            if (this.animation == null && this.minimizeAnimation != null)
            {
                this.animation = this.minimizeAnimation;
                this.animation.reset();
                this.wwd.redraw();
            }
        }
    }

    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    public void setHighlighted(boolean highlighted)
    {
        if (this.highlighted != highlighted)
        {
            this.highlighted = highlighted;

            this.contents.setHighlighted(highlighted);

            FrameAttributes attrs = this.getActiveAttributes();
            this.verticalScrollBar.setLineColor(attrs.getForegroundColor());
            this.verticalScrollBar.setOpacity(attrs.getForegroundOpacity());
            this.horizontalScrollBar.setLineColor(attrs.getForegroundColor());
            this.horizontalScrollBar.setOpacity(attrs.getForegroundOpacity());
        }
    }

    /**
     * Get the title of the tree frame.
     *
     * @return The frame title.
     *
     * @see #setFrameTitle(String)
     */
    public String getFrameTitle()
    {
        return this.frameTitle;
    }

    /**
     * Set the title of the tree frame.
     *
     * @param frameTitle New frame title.
     *
     * @see #getFrameTitle()
     */
    public void setFrameTitle(String frameTitle)
    {
        this.frameTitle = frameTitle;
    }

    /**
     * Get the current size of the tree frame. This size may be different than the normal size of the frame returned by
     * {!link #getSize()} if the frame is minimized or animating between maximized and minimized states.
     *
     * @return the
     *
     * @see #setSize(gov.nasa.worldwind.render.Size)
     */
    public Size getSize()
    {
        return this.size;
    }

    /**
     * Set the size of the frame.
     *
     * @param size New size.
     */
    public void setSize(Size size)
    {
        if (size == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.size = size;
    }

    /**
     * Return the amount of screen that space that the frame is currently using. The frame size may change due to a
     * window resize, or an animation.
     *
     * @return The size of the frame on screen, in pixels. This method will return null until the frame has been
     *         rendered at least once.
     */
    public Dimension getCurrentSize()
    {
        return this.frameSize;
    }

    public int getTitleBarHeight()
    {
        return this.titleBarHeight;
    }

    public void setTitleBarHeight(int titleBarHeight)
    {
        this.titleBarHeight = titleBarHeight;
    }

    /**
     * Get the insets that seperate the frame contents from the frame.
     *
     * @return Active insets.
     */
    public Insets getInsets()
    {
        return this.insets;
    }

    /**
     * Set the frame insets. This is the amount of space between the frame and its contents.
     *
     * @param insets New insets.
     */
    public void setInsets(Insets insets)
    {
        if (insets == null)
        {
            String message = Logging.getMessage("nullValue.InsetsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.insets = insets;
    }

    /**
     * Get the animation that is played when the tree frame is minimized.
     *
     * @return Animation played when the frame is minimized.
     *
     * @see #setMinimizeAnimation(Animation)
     */
    public Animation getMinimizeAnimation()
    {
        return minimizeAnimation;
    }

    /**
     * Set the animation that is played when the tree frame is minimized.
     *
     * @param minimizeAnimation New minimize animation.
     *
     * @see #getMinimizeAnimation()
     */
    public void setMinimizeAnimation(Animation minimizeAnimation)
    {
        this.minimizeAnimation = minimizeAnimation;
    }

    /**
     * Get the image source for the frame icon.
     *
     * @return The icon image source, or null if no image source has been set.
     *
     * @see #setIconImageSource(Object)
     */
    public Object getIconImageSource()
    {
        return this.iconImageSource;
    }

    /**
     * Set the image source of the frame icon. This icon is drawn in the upper right hand corner of the tree frame.
     *
     * @param imageSource New image source. May be a String, URL, or BufferedImage.
     */
    public void setIconImageSource(Object imageSource)
    {
        this.iconImageSource = imageSource;
    }

    /**
     * Get the texture loaded for the icon image source. If the texture has not been loaded, this method will attempt to
     * load it in the background.
     *
     * @return The icon texture, or no image source has been set, or if the icon has not been loaded yet.
     */
    protected BasicWWTexture getTexture()
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
        Object imageSource = this.getIconImageSource();
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

    /**
     * Get a reference to one of the frame's scroll bars.
     *
     * @param direction Determines which scroll bar to get. Either {@link AVKey#VERTICAL} or {@link AVKey#HORIZONTAL}.
     *
     * @return The horizontal or vertical scroll bar.
     */
    public ScrollBar getScrollBar(String direction)
    {
        if (AVKey.VERTICAL.equals(direction))
            return this.verticalScrollBar;
        else
            return this.horizontalScrollBar;
    }

    @Override
    protected void beginDrag(Point point)
    {
        if (this.getActiveAttributes().isAllowMove())
        {
            Point2D location = this.screenPoint;
            this.dragRefPoint = new Point((int) location.getX() - point.x, (int) location.getY() - point.y);
        }
    }

    public void drag(Point point)
    {
        if (this.getActiveAttributes().isAllowMove())
        {
            double x = point.x + this.dragRefPoint.x;
            double y = point.y + this.dragRefPoint.y;
            this.setScreenLocation(new Offset(x, y, AVKey.PIXELS, AVKey.PIXELS));
        }
    }

    @Override
    public void selected(SelectEvent event)
    {
        super.selected(event);

        if (event.isLeftDoubleClick())
        {
            this.setMinimized(!this.isMinimized());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        this.verticalScrollBar.scroll(e.getUnitsToScroll());
        e.consume();
        this.wwd.redraw();
    }
}
