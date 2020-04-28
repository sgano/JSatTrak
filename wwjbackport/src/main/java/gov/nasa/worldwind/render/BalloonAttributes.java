/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.RestorableSupport;

import java.awt.*;

/**
 * {@link Balloon} attributes set.
 *
 * @author pabercrombie
 * @version $Id: BalloonAttributes.java 14159 2010-11-29 21:50:05Z pabercrombie $
 * @see Balloon
 * @see FrameFactory
 */
public interface BalloonAttributes
{
    /**
     * Get the callout frame shape. Can be one of {@link FrameFactory}.SHAPE_RECTANGLE (default), SHAPE_ELLIPSE or
     * SHAPE_NONE.
     *
     * @return the callout frame shape.
     */
    public String getBalloonShape();

    /**
     * Set the callout frame shape. Can be one of {@link FrameFactory}.SHAPE_RECTANGLE (default), SHAPE_ELLIPSE or
     * SHAPE_NONE. Set to <code>null</code> to use the default shape. <p> Note that SHAPE_ELLIPSE draws an ellipse
     * <u>inside</u> the callout bounding rectangle set by its size (see setSize()) or its text bounding rectangle (see
     * setAdjustWidthToText() and setSize() with height set to zero). It is often necessary to have larger Insets
     * dimensions (see setInsets()) to avoid having the text drawn outside the shape border. </p>
     *
     * @param shape the callout frame shape.
     */
    public void setBalloonShape(String shape);

    /**
     * Get the annotation callout preferred total dimension in pixels.
     *
     * @return the callout preferred total dimension in pixels.
     */
    public Size getSize();

    /**
     * Set the balloon callout preferred total dimension in pixels. <p> If necessary, the text will be wrapped into
     * several lines so as not to exceed the callout preferred <code><b>width</b></code> (minus the <code>Insets</code>
     * <code>left</code> and <code>right</code> dimensions - see setInsets()). However, if setAdjustWidthToText() is set
     * to true, the final callout width will follow that of the final text bounding rectangle. </p> <p> If necessary,
     * the text will also be truncated so as not to exceed the given <code><b>height</b></code>. A <code>zero</code>
     * value (default) will have the callout follow the final text bounding rectangle height (including the
     * <code>Insets</code> <code>top</code> and <code>bottom</code>). </p> Set to <code>null</code> to use the default
     * size.
     *
     * @param size the callout preferred total dimension in pixels.
     */
    public void setSize(Size size);

    /**
     * Get the balloon shape leader type. Can be one of {@link FrameFactory}.LEADER_TRIANGLE (default) or LEADER_NONE.
     *
     * @return the balloon shape leader type.
     */
    public String getLeader();

    /**
     * Set the balloon shape leader type. Can be one of {@link FrameFactory}.LEADER_TRIANGLE (default) or LEADER_NONE.
     *
     * @param leader the balloon shape leader type.
     */
    public void setLeader(String leader);

    /**
     * Get the balloon shape leader gap width in pixels. Default is 40.
     *
     * @return the balloon shape leader gap width.
     */
    public int getLeaderWidth();

    /**
     * Set the balloon shape leader gap width in pixels. Set this attribute to minus one (<code>-1</code>) to use the
     * default value.
     *
     * @param width the balloon shape leader gap width in pixels.
     */
    public void setLeaderWidth(int width);

    /**
     * Get the balloon shape rounded corners radius in pixels. A value of <code>zero</code> means no rounded corners.
     *
     * @return the balloon shape rounded corners radius in pixels.
     */
    public int getCornerRadius();

    /**
     * Set the balloon shape rounded corners radius in pixels. A value of <code>zero</code> means no rounded corners.
     * Set this attribute to minus one (<code>-1</code>) to use the default value.
     *
     * @param radius the balloon shape rounded corners radius in pixels.
     */
    public void setCornerRadius(int radius);

    /**
     * Get the balloon displacement offset in pixels from the globe Position or screen point at which it is associated.
     * When the balloon has a leader (see setLeader(String leader)), it will lead to the original point. In the actual
     * implementation, the balloon is drawn above its associated point and the leader connects at the bottom of the
     * frame, in the middle. Positive X increases toward the right and positive Y in the up direction.
     *
     * @return the balloon displacement offset in pixels
     */
    public Offset getDrawOffset();

    /**
     * Set the balloon displacement offset in pixels from the globe Position or screen point at which it is associated.
     * When the balloon has a leader (see setLeader(String leader)), it will lead to the original point. Positive X
     * increases toward the right and positive Y in the up direction. Set to <code>null</code> to use the default
     * offset.
     *
     * @param offset the balloon displacement offset in pixels
     */
    public void setDrawOffset(Offset offset);

    /**
     * Get the balloon <code>Insets</code> dimensions in pixels. The text is drawn inside the balloon frame while
     * keeping a distance from the balloon border defined in the Insets.
     *
     * @return the balloon <code>Insets</code> dimensions in pixels.
     */
    public Insets getInsets();

    /**
     * Set the balloon <code>Insets</code> dimensions in pixels. The text will be drawn inside the balloon frame while
     * keeping a distance from the balloon border defined in the Insets. Set to <code>null</code> to use the default
     * Insets.
     *
     * @param insets the balloon <code>Insets</code> dimensions in pixels.
     */
    public void setInsets(Insets insets);

    /**
     * Get the balloon border line width. A value of <code>zero</code> means no border is being drawn.
     *
     * @return the balloon border line width.
     */
    public double getBorderWidth();

    /**
     * Set the balloon border line width. A value of <code>zero</code> means no border will is drawn. Set to minus one
     * (<code>-1</code>) to use the default value.
     *
     * @param width the balloon border line width.
     */
    public void setBorderWidth(double width);

    /**
     * Get the <code>Font</code> used for text rendering.
     *
     * @return the <code>Font</code> used for text rendering.
     */
    public Font getFont();

    /**
     * Set the <code>Font</code> used for text rendering. Set to <code>null</code> to use the default value.
     *
     * @param font the <code>Font</code> used for text rendering.
     */
    public void setFont(Font font);

    /**
     * Get the text <code>Color</code>.
     *
     * @return the text <code>Color</code>.
     */
    public Color getTextColor();

    /**
     * Set the text <code>Color</code>. Set to <code>null</code> to use the default value.
     *
     * @param color the text <code>Color</code>.
     */
    public void setTextColor(Color color);

    /**
     * Get the balloon background <code>Color</code>.
     *
     * @return the balloon background <code>Color</code>.
     */
    public Color getBackgroundColor();

    /**
     * Set the balloon background <code>Color</code>. Set to <code>null</code> to use the default value.
     *
     * @param color the balloon background <code>Color</code>.
     */
    public void setBackgroundColor(Color color);

    /**
     * Get the balloon border <code>Color</code>.
     *
     * @return the balloon border <code>Color</code>.
     */
    public Color getBorderColor();

    /**
     * Set the balloon border <code>Color</code>. Set to <code>null</code> to use the default value.
     *
     * @param color the balloon border <code>Color</code>.
     */
    public void setBorderColor(Color color);

    /**
     * Get the background image source. Can be a <code>String</code> providing the path to a local image, a {@link
     * java.awt.image.BufferedImage} or <code>null</code>.
     *
     * @return the background image source.
     */
    public Object getImageSource();

    /**
     * Set the background image source. Can be a <code>String</code> providing the path to a local image or a {@link
     * java.awt.image.BufferedImage}. Set to null for no background image rendering.
     *
     * @param imageSource the background image source.
     */
    public void setImageSource(Object imageSource);

    /**
     * Get the background image as a {@link WWTexture}. This returns null if the background image source returned by
     * {@link #getImageSource()} is null.
     *
     * @return the background image as a WWTexture, or null if this BalloonAttributes has no background image
     *         source.
     */
    public WWTexture getBackgroundTexture();

    /**
     * Get the background image scaling factor.
     *
     * @return the background image scaling factor.
     */
    public double getImageScale();

    /**
     * Set the background image scaling factor. Set to minus one (<code>-1</code>) to use the default value.
     *
     * @param scale the background image scaling factor.
     */
    public void setImageScale(double scale);

    /**
     * Get the background image offset in pixels (before background scaling).
     *
     * @return the background image offset in pixels
     */
    public Point getImageOffset();

    /**
     * Set the background image offset in pixels (before background scaling). Set to <code>null</code> to use the
     * default value.
     *
     * @param offset the background image offset in pixels
     */
    public void setImageOffset(Point offset);

    /**
     * Get the opacity of the background image (0 to 1).
     *
     * @return the opacity of the background image (0 to 1).
     */
    public double getImageOpacity();

    /**
     * Set the opacity of the background image (0 to 1). Set to minus one (<code>-1</code>) to use the default value.
     *
     * @param opacity the opacity of the background image (0 to 1).
     */
    public void setImageOpacity(double opacity);

    /**
     * Get the repeat behavior or the background image. Can be one of {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_NONE},
     * {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_Y}, {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_XY} (default) or
     * {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_NONE}.
     *
     * @return the repeat behavior or the background image.
     */
    public String getImageRepeat();

    /**
     * Set the repeat behavior or the background image. Can be one of {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_NONE},
     * {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_Y}, {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_XY} (default) or
     * {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_NONE}. Set to <code>null</code> to use the default value.
     *
     * @param repeat the repeat behavior or the background image.
     */
    public void setImageRepeat(String repeat);

    /**
     * Get the path to the image used for background image. Returns <code>null</code> if the image source is null or a
     * memory BufferedImage.
     *
     * @return the path to the image used for background image.
     */
    public String getPath();

    /**
     * Indicates whether one or more members of <i>this</> remain unresolved because they must be retrieved from an
     * external source.
     *
     * @return true if there are unresolved fields, false if no fields remain unresolved.
     */
    public boolean isUnresolved();

    /**
     * Specifies whether one or more fields of <i>this</> remain unresolved because they must be retrieved from an
     * external source.
     *
     * @param unresolved true if there are unresolved fields, false if no fields remain unresolved.
     */
    public void setUnresolved(boolean unresolved);

    /**
     * Returns a new BalloonAttributes instance of the same type as this BalloonAttributes, who's properties are
     * configured exactly as this BalloonAttributes.
     *
     * @return a copy of this BalloonAttributes.
     */
    BalloonAttributes copy();

    /**
     * Copies the specified BalloonAttributes' properties into this object's properties. This does nothing if the
     * specified attributes is null.
     *
     * @param attributes the attributes to copy.
     */
    void copy(BalloonAttributes attributes);

    /**
     * Save the attributes in the specified <code>restorableSupport</code>.
     * If <code>context</code> is not null, attributes will be saved beneath it. Otherwise, they will be saved at the
     * document root.
     *
     * @param restorableSupport RestorableSupport to write attribute values to.
     * @param context           RestorableSupport.StateObject that attributes will be saved under, if not null.
     *
     * @throws IllegalArgumentException If <code>restorableSupport</code> is null.
     */
    void getRestorableState(RestorableSupport restorableSupport, RestorableSupport.StateObject context);

    /**
     * Restores the any attributes appearing in the specified <code>restorableSupport</code>. If <code>context</code> is
     * not null, this will search for attributes beneath it. Otherwise, this will search for attributes beneath the
     * document root.
     *
     * @param restorableSupport RestorableSupport to read attribute values from.
     * @param context           RestorableSupport.StateObject under which attributes will be looked, if not null.
     *
     * @throws IllegalArgumentException If either <code>restorableSupport</code>.
     */
    void restoreState(RestorableSupport restorableSupport, RestorableSupport.StateObject context);
}
