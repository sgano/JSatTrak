/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.TextDecoder;

import java.awt.*;

/**
 * A text label that can be attached to a point on the screen, or a point on the globe.
 *
 * @author pabercrombie
 * @version $Id: Balloon.java 14063 2010-11-01 22:38:06Z pabercrombie $
 * @see BalloonAttributes
 */
public interface Balloon extends Renderable, Highlightable, AVList
{
    /**
     * Indicates that the balloon is attached to a point on the screen.
     *
     * @see #getAttachmentMode()
     */
    public final static String SCREEN_MODE = "render.Balloon.ScreenMode";

    /**
     * Indicates that the balloon is attached to a point on the globe.
     *
     * @see #getAttachmentMode()
     */
    public final static String GLOBE_MODE = "render.Balloon.GlobeMode";

    /**
     * Is the balloon always on top?
     *
     * @return True if the balloon will always render above other items.
     */
    boolean isAlwaysOnTop();

    /**
     * Set the balloon to always on top or not.
     *
     * @param alwaysOnTop True if the balloon should always render above other objects.
     */
    void setAlwaysOnTop(boolean alwaysOnTop);

    /**
     * Is the balloon enabled for picking?
     *
     * @return True if the balloon can be picked, false if not.
     */
    boolean isPickEnabled();

    /**
     * Set the balloon to be pick enabled or not.
     *
     * @param enable True if the balloon can be picked, false if not.
     */
    void setPickEnabled(boolean enable);

    /**
     * Get the balloon text. The method returns the raw text, before decoding.
     *
     * @return The balloon text.
     *
     * @see #getTextDecoder()
     * @see #setTextDecoder(gov.nasa.worldwind.util.TextDecoder)
     */
    String getText();

    /**
     * Set the balloon text.
     *
     * @param text New balloon text.
     */
    void setText(String text);

    /**
     * Get the "normal" balloon attributes.
     *
     * @return Balloon attributes.
     */
    BalloonAttributes getAttributes();

    /**
     * Set the "normal" balloon attributes.
     *
     * @param attrs New attributes
     */
    void setAttributes(BalloonAttributes attrs);

    /**
     * Get the highlight attributes.
     *
     * @return Balloon's highlight attributes.
     */
    BalloonAttributes getHighlightAttributes();

    /**
     * Set the highlight attributes.
     *
     * @param attrs Attributes to use when the balloon is highlighted.
     */
    void setHighlightAttributes(BalloonAttributes attrs);

    /**
     * Get the text decoder that will process the balloon text.
     *
     * @return Active text decoder.
     */
    TextDecoder getTextDecoder();

    /**
     * Set a text decoder to process the balloon text.
     *
     * @param decoder New decoder.
     */
    void setTextDecoder(TextDecoder decoder);

    /**
     * Set the screen position of the balloon. This point is interpreted in a coordinate system with the origin at the
     * upper left corner of the screen.
     *
     * @param point Point in screen coordinates, with origin at upper left corner.
     *
     * @see #setPosition(java.awt.Point)
     */
    void setPosition(Point point);

    /**
     * Attach the balloon to a position on the globe.
     *
     * @param position New position for the balloon.
     *
     * @see #setPosition(java.awt.Point)
     */
    void setPosition(Position position);

    /**
     * Get the position of the balloon. This method returns either {@link java.awt.Point} or {@link Position}, depending
     * on the balloon attachment mode.
     *
     * @return The position of the balloon, as either a Point if the balloon is in screen attachment mode, or a Position
     *         if the balloon is in globe attachment mode.
     *
     * @see #getAttachmentMode()
     */
    Object getPosition();

    /**
     * Get the balloon's attachment mode, either {@link #SCREEN_MODE} or {@link #GLOBE_MODE}. The attachment mode is
     * determined when the balloon is created, and cannot be changed.
     *
     * @return The attachment mode.
     */
    String getAttachmentMode();

    /**
     * Returns the delegate owner of the balloon. If non-null, the returned object replaces the balloon as the pickable
     * object returned during picking. If null, the balloon itself is the pickable object returned during picking.
     *
     * @return the object used as the pickable object returned during picking, or null to indicate the the balloon is
     *         returned during picking.
     */
    Object getDelegateOwner();

    /**
     * Specifies the delegate owner of the surface object. If non-null, the delegate owner replaces the balloon as the
     * pickable object returned during picking. If null, the balloon itself is the pickable object returned during
     * picking.
     *
     * @param owner the object to use as the pickable object returned during picking, or null to return the balloon.
     */
    void setDelegateOwner(Object owner);

    /**
     * Get whether the annotation is visible and should be rendered.
     *
     * @return true if the annotation is visible and should be rendered.
     */
    public boolean isVisible();

    /**
     * Set whether the balloon is visible and should be rendered.
     *
     * @param visible true if the balloon is visible and should be rendered.
     */
    public void setVisible(boolean visible);

    /**
     * Returns the balloon's altitude mode. See {@link #setAltitudeMode(int)} for a description of the modes.
     *
     * @return the balloon's altitude mode.
     */
    public int getAltitudeMode();

    /**
     * Specifies the balloon's altitude mode. Recognized modes are: <ul> <li><b>@link WorldWind#CLAMP_TO_GROUND}</b> --
     * the balloon is placed on the terrain at the latitude and longitude of its position.</li> <li><b>@link
     * WorldWind#RELATIVE_TO_GROUND}</b> -- the balloon is placed above the terrain at the latitude and longitude of its
     * position and the distance specified by its elevation.</li> <li><b>{@link gov.nasa.worldwind.WorldWind#ABSOLUTE}</b>
     * -- the balloon is placed at its specified position. </ul>
     *
     * @param altitudeMode the altitude mode
     */
    public void setAltitudeMode(int altitudeMode);

    /**
     * Get the balloon bounding {@link java.awt.Rectangle} using OGL coordinates - bottom-left corner x and y relative
     * to the {@link gov.nasa.worldwind.WorldWindow} bottom-left corner, and the balloon callout width and height.
     * <p/>
     * The balloon offset from it's reference point is factored in such that the callout leader shape and reference
     * point are included in the bounding rectangle.
     *
     * @param dc the current DrawContext.
     *
     * @return the balloon bounding {@link java.awt.Rectangle} using OGL viewport coordinates.
     *
     * @throws IllegalArgumentException if <code>dc</code> is null.
     */
    java.awt.Rectangle getBounds(DrawContext dc);

    /**
     * Returns the minimum eye altitude, in meters, for which the balloon is displayed.
     *
     * @return the minimum altitude, in meters, for which the balloon is displayed.
     *
     * @see #setMinActiveAltitude(double)
     * @see #getMaxActiveAltitude()
     */
    double getMinActiveAltitude();

    /**
     * Specifies the minimum eye altitude, in meters, for which the balloon is displayed.
     *
     * @param minActiveAltitude the minimum altitude, in meters, for which the balloon is displayed.
     *
     * @see #getMinActiveAltitude()
     * @see #setMaxActiveAltitude(double)
     */
    void setMinActiveAltitude(double minActiveAltitude);

    /**
     * Returns the maximum eye altitude, in meters, for which the balloon is displayed.
     *
     * @return the maximum altitude, in meters, for which the balloon is displayed.
     *
     * @see #setMaxActiveAltitude(double)
     * @see #getMinActiveAltitude()
     */
    double getMaxActiveAltitude();

    /**
     * Specifies the maximum eye altitude, in meters, for which the balloon is displayed.
     *
     * @param maxActiveAltitude the maximum altitude, in meters, for which the balloon is displayed.
     *
     * @see #getMaxActiveAltitude()
     * @see #setMinActiveAltitude(double)
     */
    void setMaxActiveAltitude(double maxActiveAltitude);
}
