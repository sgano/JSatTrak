/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * Defines the dimensions of an image, label or other screen-space item relative to a container (for example, the
 * viewport). A size contains a width, a height, a width size mode, a height size mode, and for each of these a "units"
 * string indicating the coordinate units.
 * <p/>
 * The possible size modes are: <ul> <li> {@link #NATIVE_DIMENSION} - Maintain the native dimensions.</li> <li> {@link
 * #MAINTAIN_ASPECT_RATIO} - Maintain the aspect ratio of the image when one dimension is specified and the other is
 * not.</li> <li> {@link #EXPLICIT_DIMENSION} - Use an explicit dimension. This dimension may be either an absolute
 * pixel value, or a fraction of the container.</li>
 * <p/>
 * Recognized "units" values are {@link AVKey#PIXELS}, which indicates pixel units relative to the lower left corner of
 * the image, {@link AVKey#FRACTION}, which indicates the units are fractions of the image width and height.
 * <p/>
 * Examples:
 * <pre>
 * Width mode      Height mode      Width (Units)      Height (Units)        Result
 * --------------------------------------------------------------------------------------------------------------------
 * Native          Native           N/A                N/A                   Keep native dimensions
 * Aspect ratio    Explicit         N/A                100 (pix)             Scale image so that height is 100 pixels,
 *                                                                           but maintain aspect ratio
 * Explicit        Aspect ratio     0.5 (fraction)     N/A                   Make the width half of the container, and
 *                                                                           scale height to maintain aspect ratio
 * Explicit        Native           1.0 (fraction)     N/A                   Stretch the image to fill the width of the
 *                                                                           container, but do not scale the height.
 * <p/>
 * This class implements the functionality of a KML <i>size</i>.
 *
 * @author pabercrombie
 * @version $Id: Size.java 13897 2010-09-28 21:22:36Z pabercrombie $
 */
public class Size
{
    /** Size value that KML uses to indicate that the image's native dimension should be used. */
    public static final int NATIVE_DIMENSION = 0;

    /** Size value that KML uses to indicate that the image aspect ration should be maintained. */
    public static final int MAINTAIN_ASPECT_RATIO = 1;

    /**
     * Constant to indicate that the size parameter from the KML file indicates the image dimension, not {@link
     * #NATIVE_DIMENSION} or {@link #MAINTAIN_ASPECT_RATIO}.
     */
    public static final int EXPLICIT_DIMENSION = 2;

    protected double widthParam;
    protected double heightParam;
    protected String widthUnits = AVKey.PIXELS;
    protected String heightUnits = AVKey.PIXELS;
    protected int widthMode = NATIVE_DIMENSION;
    protected int heightMode = NATIVE_DIMENSION;

    /** Create a Size object that will preserve native dimensions. */
    public Size()
    {
    }

    /**
     * Create a Size with specified dimensions.
     *
     * @param widthMode   Width mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *                    #EXPLICIT_DIMENSION}.
     * @param widthParam  The width (applies only to {@link #EXPLICIT_DIMENSION} mode).
     * @param widthUnits  Units of {@code width}. Either {@link AVKey#PIXELS} or {@link AVKey#PIXELS}.
     * @param heightMode  height mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *                    #EXPLICIT_DIMENSION}.
     * @param heightParam The height (applies only to {@link #EXPLICIT_DIMENSION} mode).
     * @param heightUnits Units of {@code height}. Either {@link AVKey#PIXELS} or {@link AVKey#PIXELS}.
     *
     * @see #setWidth(int, double, String)
     * @see #setHeight(int, double, String)
     */
    public Size(int widthMode, double widthParam, String widthUnits, int heightMode, double heightParam,
        String heightUnits)
    {
        this.setWidth(widthMode, widthParam, widthUnits);
        this.setHeight(heightMode, heightParam, heightUnits);
    }

    /**
     * Create a size from explicit pixel dimensions.
     *
     * @param widthInPixels  Width of rectangle in pixels.
     * @param heightInPixels Height of rectangle in pixels.
     *
     * @return New size object.
     */
    public static Size fromPixels(int widthInPixels, int heightInPixels)
    {
        return new Size(EXPLICIT_DIMENSION, widthInPixels, AVKey.PIXELS,
            EXPLICIT_DIMENSION, heightInPixels, AVKey.PIXELS);
    }

    /**
     * Set the width.
     *
     * @param mode  Width mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *              #EXPLICIT_DIMENSION}.
     * @param width The width (applies only to {@link #EXPLICIT_DIMENSION} mode).
     * @param units Units of {@code width}. Either {@link AVKey#PIXELS} or {@link AVKey#PIXELS}.
     */
    public void setWidth(int mode, double width, String units)
    {
        this.widthMode = mode;
        this.widthParam = width;
        this.widthUnits = units;
    }

    /**
     * Set the height.
     *
     * @param mode   Width mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *               #EXPLICIT_DIMENSION}.
     * @param height The width (applies only to {@link #EXPLICIT_DIMENSION} mode).
     * @param units  Units of {@code width}. Either {@link AVKey#PIXELS} or {@link AVKey#FRACTION}.
     */
    public void setHeight(int mode, double height, String units)
    {
        this.heightMode = mode;
        this.heightParam = height;
        this.heightUnits = units;
    }

    /**
     * Returns the units of the offset X value. See {@link #setWidth(int, double, String)} for a description of the
     * recognized values.
     *
     * @return the units of the offset X value, or null.
     */
    public String getWidthUnits()
    {
        return widthUnits;
    }

    /**
     * Returns the units of the offset Y value. See {@link #setHeight(int, double, String)} for a description of the
     * recognized values.
     *
     * @return the units of the offset Y value, or null.
     */
    public String getHeightUnits()
    {
        return heightUnits;
    }

    /**
     * Get the mode of the width dimension.
     *
     * @return Width mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *         #EXPLICIT_DIMENSION}.
     */
    public int getWidthMode()
    {
        return this.widthMode;
    }

    /**
     * Get the mode of the height dimension.
     *
     * @return Height mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *         #EXPLICIT_DIMENSION}.
     */
    public int getHeightMode()
    {
        return this.heightMode;
    }

    /**
     * Get the unscaled width.
     *
     * @return Unscaled width. The units of this value depend on the current height units.
     *
     * @see #getWidthMode()
     * @see #getWidthUnits()
     */
    public double getWidth()
    {
        return widthParam;
    }

    /**
     * Get the unscaled height.
     *
     * @return Unscaled height. The units of this value depend on the current height units.
     *
     * @see #getHeightMode()
     * @see #getHeightUnits()
     */
    public double getHeight()
    {
        return heightParam;
    }

    /**
     * Computes the width and height of a rectangle within a container rectangle.
     *
     * @param rectWidth       The width of the rectangle to size.
     * @param rectHeight      The height of the rectangle to size.
     * @param containerWidth  The width of the container.
     * @param containerHeight The height of the container.
     *
     * @return The desired image dimensions.
     */
    public Dimension compute(int rectWidth, int rectHeight, int containerWidth, int containerHeight)
    {
        if (rectWidth <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", rectWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (rectHeight <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", rectHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (containerWidth <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", containerWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (containerHeight <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", containerHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final double aspectRatio = (double) rectWidth / rectHeight;

        int xMode = this.getWidthMode();
        int yMode = this.getHeightMode();

        double width, height;

        if (xMode == NATIVE_DIMENSION && yMode == NATIVE_DIMENSION
            || xMode == NATIVE_DIMENSION && yMode == MAINTAIN_ASPECT_RATIO
            || xMode == MAINTAIN_ASPECT_RATIO && yMode == NATIVE_DIMENSION
            || xMode == MAINTAIN_ASPECT_RATIO && yMode == MAINTAIN_ASPECT_RATIO)
        {
            // Keep original dimensions
            width = rectWidth;
            height = rectHeight;
        }
        else if (xMode == MAINTAIN_ASPECT_RATIO)
        {
            // y dimension is specified, scale x to maintain aspect ratio
            height = computeSize(this.heightParam, this.heightUnits, containerHeight);
            width = height * aspectRatio;
        }
        else if (yMode == MAINTAIN_ASPECT_RATIO)
        {
            // x dimension is specified, scale y to maintain aspect ratio
            width = computeSize(this.widthParam, this.widthUnits, containerWidth);
            height = width / aspectRatio;
        }
        else
        {
            if (xMode == NATIVE_DIMENSION)
                width = rectWidth;
            else
                width = computeSize(widthParam, widthUnits, containerWidth);

            if (yMode == NATIVE_DIMENSION)
                height = rectHeight;
            else
                height = computeSize(heightParam, heightUnits, containerHeight);
        }

        return new Dimension((int) width, (int) height);
    }

    /**
     * Compute a dimension taking into account the units of the dimension.
     *
     * @param size               The size parameter.
     * @param units              One of {@link AVKey#PIXELS} or {@link AVKey#FRACTION}. If the {@code units} value is
     *                           not one of the expected options, {@link AVKey#PIXELS} is used as the default.
     * @param containerDimension The viewport dimension.
     *
     * @return Size in pixels
     */
    protected double computeSize(double size, String units, double containerDimension)
    {
        if (AVKey.FRACTION.equals(units))
            return size * containerDimension;
        else  // Default to pixel
            return size;
    }
}
