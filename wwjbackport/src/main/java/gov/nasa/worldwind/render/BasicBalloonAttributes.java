/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.*;

import java.awt.*;

/**
 * Basic implementation of a {@link Balloon} attributes set.
 *
 * @author pabercrombie
 * @version $Id: BasicBalloonAttributes.java 14159 2010-11-29 21:50:05Z pabercrombie $
 * @see FrameFactory
 */
public class BasicBalloonAttributes implements BalloonAttributes
{
    protected String balloonShape;
    protected Size size;
    protected String leader;
    protected int leaderGapWidth;
    protected int cornerRadius;
    protected Offset drawOffset;
    protected Font font;
    protected Color textColor;
    protected Color backgroundColor;
    protected Color borderColor;
    protected double borderWidth;
    protected Insets insets;
    protected Object imageSource;
    protected WWTexture backgroundTexture;
    protected double imageScale;
    protected Point imageOffset;
    protected double imageOpacity;
    protected String imageRepeat;
    protected boolean unresolved;

    /**
     * Construct balloon attributes with the default values.
     */
    public BasicBalloonAttributes()
    {
        this.setBalloonShape(FrameFactory.SHAPE_RECTANGLE);
        this.setSize(Size.fromPixels(160, 0));
        this.setLeader(FrameFactory.LEADER_TRIANGLE);
        this.setLeaderWidth(40);
        this.setCornerRadius(20);
        this.setDrawOffset(new Offset(40.0, 60.0, AVKey.PIXELS, AVKey.PIXELS));
        this.setInsets(new Insets(20, 15, 15, 15));
        this.setFont(Font.decode("Arial-PLAIN-12"));
        this.setTextColor(Color.BLACK);
        this.setBackgroundColor(Color.WHITE);
        this.setBorderColor(new Color(171, 171, 171));
        this.setBorderWidth(1);
        this.setImageScale(1);
        this.setImageOffset(new Point(0, 0));
        this.setImageOpacity(1);
        this.setImageRepeat(AVKey.REPEAT_XY);
    }

    /**
     * Create a new attributes object with the same configuration as an existing attributes object.
     *
     * @param attributes Object to copy configuration from.
     */
    public BasicBalloonAttributes(BalloonAttributes attributes)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.copy(attributes);
    }
    
    /** {@inheritDoc} */
    public String getBalloonShape()
    {
        return this.balloonShape;
    }

    /** {@inheritDoc} */
    public void setBalloonShape(String shape)
    {
        if (shape == null)
        {
            String message = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.balloonShape = shape;
    }

    /** {@inheritDoc} */
    public Size getSize()
    {
        return this.size;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public String getLeader()
    {
        return this.leader;
    }

    /** {@inheritDoc} */
    public void setLeader(String leader)
    {
        if (leader == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.leader = leader;
    }

    /** {@inheritDoc} */
    public int getLeaderWidth()
    {
        return this.leaderGapWidth;
    }

    /** {@inheritDoc} */
    public void setLeaderWidth(int width)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.leaderGapWidth = width;
    }

    /** {@inheritDoc} */
    public int getCornerRadius()
    {
        return this.cornerRadius;
    }

    /** {@inheritDoc} */
    public void setCornerRadius(int radius)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.cornerRadius = radius;
    }

    /** {@inheritDoc} */
    public Offset getDrawOffset()
    {
        return this.drawOffset;
    }

    /** {@inheritDoc} */
    public void setDrawOffset(Offset offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.OffsetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.drawOffset = offset;
    }

    /** {@inheritDoc} */
    public Insets getInsets()
    {
        return this.insets;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public double getBorderWidth()
    {
        return this.borderWidth;
    }

    /** {@inheritDoc} */
    public void setBorderWidth(double width)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.borderWidth = width;
    }

    /** {@inheritDoc} */
    public Font getFont()
    {
        return this.font;
    }

    /** {@inheritDoc} */
    public void setFont(Font font)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.font = font;
    }

    /** {@inheritDoc} */
    public Color getTextColor()
    {
        return this.textColor;
    }

    /** {@inheritDoc} */
    public void setTextColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.textColor = color;
    }

    /** {@inheritDoc} */
    public Color getBackgroundColor()
    {
        return this.backgroundColor;
    }

    /** {@inheritDoc} */
    public void setBackgroundColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.backgroundColor = color;
    }

    /** {@inheritDoc} */
    public Color getBorderColor()
    {
        return this.borderColor;
    }

    /** {@inheritDoc} */
    public void setBorderColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.borderColor = color;
    }

    /** {@inheritDoc} */
    public Object getImageSource()
    {
        return this.imageSource;
    }

    /** {@inheritDoc} */
    public void setImageSource(Object imageSource)
    {
        if (imageSource == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageSource = imageSource;
        this.backgroundTexture = new BasicWWTexture(imageSource, true);
    }

    /** {@inheritDoc} */
    public WWTexture getBackgroundTexture()
    {
        return this.backgroundTexture;
    }

    /** {@inheritDoc} */
    public double getImageScale()
    {
        return this.imageScale;
    }

    /** {@inheritDoc} */
    public void setImageScale(double scale)
    {
        if (scale <  0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "scale < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.imageScale = scale;
    }

    /** {@inheritDoc} */
    public Point getImageOffset()
    {
        return this.imageOffset;
    }

    /** {@inheritDoc} */
    public void setImageOffset(Point offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.OffsetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.imageOffset = offset;
    }

    /** {@inheritDoc} */
    public double getImageOpacity()
    {
        return this.imageOpacity;
    }

    /** {@inheritDoc} */
    public void setImageOpacity(double opacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "opacity < 0 or opacity > 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.imageOpacity = opacity;
    }

    /** {@inheritDoc} */
    public String getImageRepeat()
    {
        return this.imageRepeat;
    }

    /** {@inheritDoc} */
    public void setImageRepeat(String repeat)
    {
        if (repeat == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.imageRepeat = repeat;
    }

    /** {@inheritDoc} */
    public String getPath()
    {
        Object imageSource = this.getImageSource();
        return (imageSource instanceof String) ? (String) imageSource : null;
    }

    /** {@inheritDoc} */
    public boolean isUnresolved()
    {
        return unresolved;
    }

    /** {@inheritDoc} */
    public void setUnresolved(boolean unresolved)
    {
        this.unresolved = unresolved;
    }

    /** {@inheritDoc} */
    public BalloonAttributes copy()
    {
        return new BasicBalloonAttributes(this);
    }

    /** {@inheritDoc} */
    public void copy(BalloonAttributes attributes)
    {
        if (attributes != null)
        {
            this.balloonShape = attributes.getBalloonShape();
            this.size = attributes.getSize();
            this.leader = attributes.getLeader();
            this.leaderGapWidth = attributes.getLeaderWidth();
            this.cornerRadius = attributes.getCornerRadius();
            this.drawOffset = attributes.getDrawOffset();
            this.insets = attributes.getInsets();
            this.font = attributes.getFont();
            this.textColor = attributes.getTextColor();
            this.backgroundColor = attributes.getBackgroundColor();
            this.borderColor = attributes.getBorderColor();
            this.borderWidth = attributes.getBorderWidth();
            this.imageScale = attributes.getImageScale();
            this.imageOffset = attributes.getImageOffset();
            this.imageOpacity = attributes.getImageOpacity();
            this.imageRepeat = attributes.getImageRepeat();
        }
    }

    /** {@inheritDoc} */
    public void getRestorableState(RestorableSupport restorableSupport, RestorableSupport.StateObject context)
    {
        if (restorableSupport == null)
        {
            String message = Logging.getMessage("nullValue.RestorableSupportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        restorableSupport.addStateValueAsString(context, "balloonShape", this.getBalloonShape());

        // TODO serialize Size class
        RestorableSupport.StateObject sizeStateObj = restorableSupport.addStateObject(context, "size");
        if (sizeStateObj != null)
        {
            restorableSupport.addStateValueAsDouble(sizeStateObj, "width", this.getSize().getWidth());
            restorableSupport.addStateValueAsDouble(sizeStateObj, "height", this.getSize().getHeight());
        }

        restorableSupport.addStateValueAsString(context, "leader", this.getLeader());
        restorableSupport.addStateValueAsInteger(context, "leaderGapWidth", this.getLeaderWidth());
        restorableSupport.addStateValueAsInteger(context, "cornerRadius", this.getCornerRadius());

        // TODO serialize Size class
        RestorableSupport.StateObject drawOffsetStateObj = restorableSupport.addStateObject(context, "drawOffset");
        if (drawOffsetStateObj != null)
        {
            restorableSupport.addStateValueAsDouble(drawOffsetStateObj, "x", this.getDrawOffset().getX());
            restorableSupport.addStateValueAsDouble(drawOffsetStateObj, "y", this.getDrawOffset().getY());
        }

        RestorableSupport.StateObject insetsStateObj = restorableSupport.addStateObject(context, "insets");
        if (insetsStateObj != null)
        {
            Insets insets = this.getInsets();
            restorableSupport.addStateValueAsInteger(insetsStateObj, "top", insets.top);
            restorableSupport.addStateValueAsInteger(insetsStateObj, "left", insets.left);
            restorableSupport.addStateValueAsInteger(insetsStateObj, "bottom", insets.bottom);
            restorableSupport.addStateValueAsInteger(insetsStateObj, "right", insets.right);
        }

        restorableSupport.addStateValueAsDouble(context, "borderWidth", this.getBorderWidth());

        // Save the name, style, and size of the font. These will be used to restore the font using the
        // constructor: new Font(name, style, size).
        RestorableSupport.StateObject fontStateObj = restorableSupport.addStateObject(context, "font");
        if (fontStateObj != null)
        {
            Font font = this.getFont();
            restorableSupport.addStateValueAsString(fontStateObj, "name", font.getName());
            restorableSupport.addStateValueAsInteger(fontStateObj, "style", font.getStyle());
            restorableSupport.addStateValueAsInteger(fontStateObj, "size", font.getSize());
        }

        String encodedColor = RestorableSupport.encodeColor(this.getTextColor());
        if (encodedColor != null)
            restorableSupport.addStateValueAsString(context, "textColor", encodedColor);

        encodedColor = RestorableSupport.encodeColor(this.getBackgroundColor());
        if (encodedColor != null)
            restorableSupport.addStateValueAsString(context, "backgroundColor", encodedColor);

        encodedColor = RestorableSupport.encodeColor(this.getBorderColor());
        if (encodedColor != null)
            restorableSupport.addStateValueAsString(context, "borderColor", encodedColor);

        // Save the imagePath property only when the imagethis property is a simple String path. If the imagethis
        // property is a BufferedImage (or some other object), we make no effort to save that state. We save under
        // the name "imagePath" to denote that it is a special case of "imagethis".
        if (this.getPath() != null)
            restorableSupport.addStateValueAsString(context, "imagePath", this.getPath(), true);

        restorableSupport.addStateValueAsDouble(context, "imageScale", this.getImageScale());

        RestorableSupport.StateObject imageOffsetStateObj = restorableSupport.addStateObject(context, "imageOffset");
        if (imageOffsetStateObj != null)
        {
            restorableSupport.addStateValueAsDouble(imageOffsetStateObj, "x", this.getImageOffset().getX());
            restorableSupport.addStateValueAsDouble(imageOffsetStateObj, "y", this.getImageOffset().getY());
        }

        restorableSupport.addStateValueAsDouble(context, "imageOpacity", this.getImageOpacity());
        restorableSupport.addStateValueAsString(context, "imageRepeat", this.getImageRepeat());
    }

    /** {@inheritDoc} */
    public void restoreState(RestorableSupport restorableSupport, RestorableSupport.StateObject context)
    {
        if (restorableSupport == null)
        {
            String message = Logging.getMessage("nullValue.RestorableSupportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String frameShapeState = restorableSupport.getStateValueAsString(context, "frameShape");
        if (frameShapeState != null)
            this.setBalloonShape(frameShapeState);

        // Restore the size property only if all parts are available.
        // We will not restore a partial size (for example, just the width).
        RestorableSupport.StateObject sizeStateObj = restorableSupport.getStateObject(context, "size");
        if (sizeStateObj != null)
        {
            Double widthState = restorableSupport.getStateValueAsDouble(sizeStateObj, "width");
            Double heightState = restorableSupport.getStateValueAsDouble(sizeStateObj, "height");
            if (widthState != null && heightState != null)
                this.setSize(Size.fromPixels(widthState.intValue(), heightState.intValue()));
        }

        String leaderState = restorableSupport.getStateValueAsString(context, "leader");
        if (leaderState != null)
            this.setLeader(leaderState);

        Integer leaderWidth = restorableSupport.getStateValueAsInteger(context, "leaderGapWidth");
        if (leaderWidth != null)
            this.setLeaderWidth(leaderWidth);

        Integer cornerRadiusState = restorableSupport.getStateValueAsInteger(context, "cornerRadius");
        if (cornerRadiusState != null)
            this.setCornerRadius(cornerRadiusState);

        // Restore the drawOffset property only if all parts are available.
        // We will not restore a partial drawOffset (for example, just the x value).
        RestorableSupport.StateObject drawOffsetStateObj = restorableSupport.getStateObject(context, "drawOffset");
        if (drawOffsetStateObj != null)
        {
            Double xState = restorableSupport.getStateValueAsDouble(drawOffsetStateObj, "x");
            Double yState = restorableSupport.getStateValueAsDouble(drawOffsetStateObj, "y");
            if (xState != null && yState != null)
                this.setDrawOffset(new Offset(xState, yState, AVKey.PIXELS, AVKey.PIXELS));
        }

        // Restore the insets property only if all parts are available.
        // We will not restore a partial insets (for example, just the top value).
        RestorableSupport.StateObject insetsStateObj = restorableSupport.getStateObject(context, "insets");
        if (insetsStateObj != null)
        {
            Integer topState = restorableSupport.getStateValueAsInteger(insetsStateObj, "top");
            Integer leftState = restorableSupport.getStateValueAsInteger(insetsStateObj, "left");
            Integer bottomState = restorableSupport.getStateValueAsInteger(insetsStateObj, "bottom");
            Integer rightState = restorableSupport.getStateValueAsInteger(insetsStateObj, "right");
            if (topState != null && leftState != null && bottomState != null && rightState != null)
                this.setInsets(new Insets(topState, leftState, bottomState, rightState));
        }

        Double borderWidthState = restorableSupport.getStateValueAsDouble(context, "borderWidth");
        if (borderWidthState != null)
            this.setBorderWidth(borderWidthState);

        // Restore the font property only if all parts are available.
        // We will not restore a partial font (for example, just the size).
        RestorableSupport.StateObject fontStateObj = restorableSupport.getStateObject(context, "font");
        if (fontStateObj != null)
        {
            // The "font name" of toolTipFont.
            String nameState = restorableSupport.getStateValueAsString(fontStateObj, "name");
            // The style attributes.
            Integer styleState = restorableSupport.getStateValueAsInteger(fontStateObj, "style");
            // The simple font size.
            Integer sizeState = restorableSupport.getStateValueAsInteger(fontStateObj, "size");
            if (nameState != null && styleState != null && sizeState != null)
                this.setFont(new Font(nameState, styleState, sizeState));
        }

        String textColorState = restorableSupport.getStateValueAsString(context, "textColor");
        if (textColorState != null)
        {
            Color color = RestorableSupport.decodeColor(textColorState);
            if (color != null)
                this.setTextColor(color);
        }

        String backgroundColorState = restorableSupport.getStateValueAsString(context, "backgroundColor");
        if (backgroundColorState != null)
        {
            Color color = RestorableSupport.decodeColor(backgroundColorState);
            if (color != null)
                this.setBackgroundColor(color);
        }

        String borderColorState = restorableSupport.getStateValueAsString(context, "borderColor");
        if (borderColorState != null)
        {
            Color color = RestorableSupport.decodeColor(borderColorState);
            if (color != null)
                this.setBorderColor(color);
        }

        // The imagePath property should exist only if the imageSource property was a simple String path.
        // If the imageSource property was a BufferedImage (or some other object), it should not exist in the
        // state document. We save under the name "imagePath" to denote that it is a special case of "imageSource".
        String imagePathState = restorableSupport.getStateValueAsString(context, "imagePath");
        if (imagePathState != null)
            this.setImageSource(imagePathState);

        Double imageScaleState = restorableSupport.getStateValueAsDouble(context, "imageScale");
        if (imageScaleState != null)
            this.setImageScale(imageScaleState);

        // Restore the imageOffset property only if all parts are available.
        // We will not restore a partial imageOffset (for example, just the x value).
        RestorableSupport.StateObject imageOffsetStateObj = restorableSupport.getStateObject(context, "imageOffset");
        if (imageOffsetStateObj != null)
        {
            Double xState = restorableSupport.getStateValueAsDouble(imageOffsetStateObj, "x");
            Double yState = restorableSupport.getStateValueAsDouble(imageOffsetStateObj, "y");
            if (xState != null && yState != null)
                this.setImageOffset(new Point(xState.intValue(), yState.intValue()));
        }

        Double imageOpacityState = restorableSupport.getStateValueAsDouble(context, "imageOpacity");
        if (imageOpacityState != null)
            this.setImageOpacity(imageOpacityState);

        String imageRepeatState = restorableSupport.getStateValueAsString(context, "imageRepeat");
        if (imageRepeatState != null)
            this.setImageRepeat(imageRepeatState);
    }
}
