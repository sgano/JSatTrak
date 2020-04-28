/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;

import java.awt.*;

/**
 * Defines the relationship of an image, label or other screen-space item relative to another screen-space item. An
 * offset contains an X coordinate, a Y coordinate, and for each of these a separate "units" string indicating the
 * coordinate units.
 * <p/>
 * Recognized "units" values are {@link AVKey#PIXELS}, which indicates pixel units relative to the lower left corner
 * of the placemark image, {@link AVKey#FRACTION}, which indicates the units are fractions of the placemark image
 * width and height, and {@link AVKey#INSET_PIXELS}, which indicates units of pixels but with origin in the upper
 * left.
 * <p/>
 * This class implements the functionality of a KML <i>Offset</i>.
 *
 * @author tag
 * @version $Id: Offset.java 13804 2010-09-15 19:36:50Z pabercrombie $
 */
public class Offset
{
    protected Double x;
    protected Double y;
    protected String xUnits;
    protected String yUnits;

    public Offset(Double x, Double y, String xUnits, String yUnits)
    {
        this.x = x;
        this.y = y;
        this.xUnits = xUnits;
        this.yUnits = yUnits;
    }

    /**
     * Returns the hot spot's X coordinate. See {@link #setXUnits(String)} for a description of the hot spot.
     *
     * @return the hot spot's X coordinate.
     */
    public Double getX()
    {
        return x;
    }

    /**
     * Specifies the hot spot's X coordinate, in units specified by {@link #setXUnits(String)}.
     *
     * @param x the hot spot's X coordinate. May be null, in which case 0 is used during rendering.
     */
    public void setX(Double x)
    {
        this.x = x;
    }

    /**
     * Returns the hot spot's Y coordinate. See {@link #setYUnits(String)} for a description of the hot spot.
     *
     * @return the hot spot's Y coordinate.
     */
    public Double getY()
    {
        return y;
    }

    /**
     * Specifies the hot spot's Y coordinate, in units specified by {@link #setYUnits(String)}.
     *
     * @param y the hot spot's Y coordinate. May be null, in which case 0 is used during rendering.
     */
    public void setY(Double y)
    {
        this.y = y;
    }

    /**
     * Returns the units of the offset X value. See {@link #setXUnits(String)} for a description of the recognized
     * values.
     *
     * @return the units of the offset X value, or null.
     */
    public String getXUnits()
    {
        return xUnits;
    }

    /**
     * Specifies the units of the offset X value. Recognized values are {@link AVKey#PIXELS}, which indicates pixel
     * units relative to the lower left corner of the placemark image, {@link AVKey#FRACTION}, which indicates the
     * units are fractions of the placemark image width and height, and {@link AVKey#INSET_PIXELS}, which indicates
     * units of pixels but with origin in the upper left.
     *
     * @param units the units of the offset X value. If null, {@link AVKey#PIXELS} is used during rendering.
     */
    public void setXUnits(String units)
    {
        this.xUnits = units;
    }

    /**
     * Returns the units of the offset Y value. See {@link #setYUnits(String)} for a description of the recognized
     * values.
     *
     * @return the units of the offset Y value, or null.
     */
    public String getYUnits()
    {
        return yUnits;
    }

    /**
     * Specifies the units of the offset Y value. Recognized values are {@link AVKey#PIXELS}, which indicates pixel
     * units relative to the lower left corner of the placemark image, {@link AVKey#FRACTION}, which indicates the
     * units are fractions of the placemark image width and height, and {@link AVKey#INSET_PIXELS}, which indicates
     * units of pixels but with origin in the upper left.
     *
     * @param units the units of the offset Y value. If null, {@link AVKey#PIXELS} is used during rendering.
     */
    public void setYUnits(String units)
    {
        this.yUnits = units;
    }

    /**
     * Computes the X and Y offset specified by this offset applied to a specified rectangle.
     *
     * @param width  the rectangle width.
     * @param height the rectangle height.
     * @param xScale an optional scale to apply to the X coordinate of the offset. May be null.
     * @param yScale an optional scale to apply to the Y coordinate of the offset. May be null.
     *
     * @return the result of applying this offset to the specified rectangle and incorporating the optional scales.
     */
    public Point.Double computeOffset(double width, double height, Double xScale, Double yScale)
    {
        double dx = 0;
        double dy = 0;

        if (this.getX() != null)
        {
            String units = this.getXUnits();
            if (AVKey.PIXELS.equals(units))
                dx = this.getX();
            else if (AVKey.INSET_PIXELS.equals(units))
                dx = this.getX() - width;
            else if (AVKey.FRACTION.equals(units))
                dx = (width * this.getX());
            else
                dx = this.getX(); // treat as pixels
        }

        if (this.getY() != null)
        {
            String units = this.getYUnits();
            if (AVKey.PIXELS.equals(units))
                dy = this.getY();
            else if (AVKey.INSET_PIXELS.equals(units))
                dy = this.getY() - height;
            else if (AVKey.FRACTION.equals(units))
                dy = (height * this.getY());
            else
                dy = this.getY(); // treat as pixels
        }

        if (xScale != null)
            dx *= xScale;

        if (yScale != null)
            dy *= yScale;

        return new Point.Double(dx, dy);
    }
}
