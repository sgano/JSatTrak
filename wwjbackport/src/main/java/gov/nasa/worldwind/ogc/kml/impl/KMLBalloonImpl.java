/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;

/**
 * Implementation of Balloon that takes its attributes from a KML BalloonStyle.
 *
 * @author pabercrombie
 * @version $Id $
 */
public class KMLBalloonImpl extends AnnotationBalloon
{
    public static final String DISPLAY_MODE_HIDE = "hide";
    public static final String DISPLAY_MODE_DEFAULT = "default";

    protected KMLAbstractFeature parent;
    protected String displayMode = DISPLAY_MODE_DEFAULT;

    /**
     * Create a globe attached Balloon Impl object for a KML feature.
     *
     * @param feature  Feature to create balloon annotation for.
     * @param text     Balloon text.
     * @param position The initial position of the balloon.
     */
    public KMLBalloonImpl(KMLAbstractFeature feature, String text, Position position)
    {
        super(text, position);

        if (feature == null)
        {
            String msg = Logging.getMessage("nullValue.FeatureIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.parent = feature;
        this.setTextDecoder(this.createTextDecoder(feature));
    }

    /**
     * Create a screen attached Balloon Impl object for a KML feature.
     *
     * @param feature Feature to create balloon annotation for.
     * @param text    Balloon text.
     * @param point   The initial position of the balloon.
     */
    public KMLBalloonImpl(KMLAbstractFeature feature, String text, Point point)
    {
        super(text, point);

        if (feature == null)
        {
            String msg = Logging.getMessage("nullValue.FeatureIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.parent = feature;
        this.setTextDecoder(this.createTextDecoder(feature));
    }

    /**
     * Render the balloon. This method will attempt to resolve the balloon style, if it has not already been resolved.
     *
     * @param dc Draw context
     */
    @Override
    public void render(DrawContext dc)
    {
        if (this.isHighlighted())
        {
            BalloonAttributes b = this.getHighlightAttributes();
            if (b == null || b.isUnresolved())
            {
                this.makeAttributesCurrent(KMLConstants.HIGHLIGHT);
            }
        }
        else
        {
            BalloonAttributes b = this.getAttributes();
            if (b == null || b.isUnresolved())
            {
                this.makeAttributesCurrent(KMLConstants.NORMAL);
            }
        }

        if (!WWUtil.isEmpty(this.getText()) && !DISPLAY_MODE_HIDE.equals(this.getDisplayMode()))
            super.render(dc);
    }

    /**
     * Adjust the balloon text based on the highlight state.
     *
     * @param highlighted true to highlight the shape, otherwise false.
     */
    @Override
    public void setHighlighted(boolean highlighted)
    {
        String state = highlighted ? KMLConstants.HIGHLIGHT : KMLConstants.NORMAL;
        KMLBalloonStyle balloonStyle = (KMLBalloonStyle) this.parent.getSubStyle(new KMLBalloonStyle(null), state);

        final String text = balloonStyle.getText();
        if (text != null)
            this.setText(text);

        super.setHighlighted(highlighted);
    }

    /**
     * Update the balloon attributes to match the KML BalloonStyle.
     *
     * @param attrType Type of attributes to update. Either {@link KMLConstants#NORMAL} or {@link
     *                 KMLConstants#HIGHLIGHT}.
     */
    protected void makeAttributesCurrent(String attrType)
    {
        BalloonAttributes attrs = this.getInitialBalloonAttributes(
            this.isHighlighted() ? KMLConstants.HIGHLIGHT : KMLConstants.NORMAL);

        KMLBalloonStyle balloonStyle = (KMLBalloonStyle) this.parent.getSubStyle(new KMLBalloonStyle(null), attrType);

        String displayMode = balloonStyle.getDisplayMode();
        if (displayMode != null)
            this.setDisplayMode(displayMode);

        this.assembleBalloonAttributes(balloonStyle, attrs);
        if (balloonStyle.hasField(AVKey.UNRESOLVED))
            attrs.setUnresolved(true);

        if (KMLConstants.NORMAL.equals(attrType))
        {
            this.setAttributes(attrs);

            String text = balloonStyle.getText();
            if (text != null)
                this.setText(text);
            else
                this.setText(this.createDefaultBalloonText());
        }
        else
        {
            this.setHighlightAttributes(attrs);
        }
    }

    /**
     * Build a default balloon text string for the feature.
     *
     * @return Default balloon text.
     */
    protected String createDefaultBalloonText()
    {
        StringBuilder sb = new StringBuilder();

        // Create default text for features that have a description
        final String description = this.parent.getDescription();
        if (!WWUtil.isEmpty(description))
        {
            final String name = this.parent.getName();
            if (!WWUtil.isEmpty(name))
                sb.append("<b>").append(name).append("</b>");

            sb.append("<br/>").append(description);
        }

        return sb.toString();
    }

    /**
     * Create the default attributes applied to the balloon. These attributes will be modified by {@link
     * #assembleBalloonAttributes(gov.nasa.worldwind.ogc.kml.KMLBalloonStyle, gov.nasa.worldwind.render.BalloonAttributes)}
     * to reflect the settings in the KML <i>BalloonStyle</i>.
     *
     * @param attrType Type of attributes to create. Either {@link KMLConstants#NORMAL} or {@link
     *                 KMLConstants#HIGHLIGHT}.
     *
     * @return Initial balloon attributes.
     */
    protected BalloonAttributes getInitialBalloonAttributes(String attrType)
    {
        return new BasicBalloonAttributes();
    }

    /**
     * Apply a KML <i>BalloonStyle</i> to the balloon attributes object.
     *
     * @param style             KML style to apply.
     * @param balloonAttributes Attributes to modify.
     */
    protected void assembleBalloonAttributes(KMLBalloonStyle style, BalloonAttributes balloonAttributes)
    {
        String bgColor = style.getBgColor();
        if (bgColor != null)
            balloonAttributes.setBackgroundColor(WWUtil.decodeColorABGR(bgColor));

        String textColor = style.getTextColor();
        if (textColor != null)
            balloonAttributes.setTextColor(WWUtil.decodeColorABGR(textColor));
    }

    /**
     * Create the text decoder that will process the text in the balloon.
     *
     * @param feature Feature to decode text for.
     *
     * @return New text decoder.
     */
    protected TextDecoder createTextDecoder(KMLAbstractFeature feature)
    {
        return new KMLBalloonTextDecoder(feature);
    }

    /**
     * Get the balloon display mode, either {@link #DISPLAY_MODE_DEFAULT} or {@link #DISPLAY_MODE_HIDE}.
     *
     * @return The current display mode.
     *
     * @see #setDisplayMode(String)
     */
    public String getDisplayMode()
    {
        return this.displayMode;
    }

    /**
     * Set the balloon's display mode, either {@link #DISPLAY_MODE_DEFAULT} or {@link #DISPLAY_MODE_HIDE}. When the mode
     * is {@link #DISPLAY_MODE_HIDE}, the balloon will not be drawn.
     *
     * @param displayMode New display mode.
     *
     * @see #getDisplayMode()
     */
    public void setDisplayMode(String displayMode)
    {
        if (displayMode == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.displayMode = displayMode;
    }
}
