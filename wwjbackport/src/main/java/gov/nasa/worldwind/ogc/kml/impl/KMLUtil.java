/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;

/**
 * @author tag
 * @version $Id: KMLUtil.java 14090 2010-11-05 21:05:14Z pabercrombie $
 */
public class KMLUtil
{
    public static final String KML_PIXELS = "pixels";
    public static final String KML_FRACTION = "fraction";
    public static final String KML_INSET_PIXELS = "insetPixels";

    public static ShapeAttributes assembleLineAttributes(ShapeAttributes attrs, KMLLineStyle style)
    {
        // Assign the attributes defined in the KML Feature element.

        if (style.getWidth() != null)
            attrs.setOutlineWidth(style.getWidth());

        if (style.getColor() != null)
            attrs.setOutlineMaterial(new Material(WWUtil.decodeColorABGR(style.getColor())));

        if (style.getColorMode() != null && "random".equals(style.getColorMode()))
            attrs.setOutlineMaterial(new Material(WWUtil.makeRandomColor(attrs.getOutlineMaterial().getDiffuse())));

        return attrs;
    }

    public static ShapeAttributes assembleInteriorAttributes(ShapeAttributes attrs, KMLPolyStyle style)
    {
        // Assign the attributes defined in the KML Feature element.

        if (style.getColor() != null)
        {
            Color color = WWUtil.decodeColorABGR(style.getColor());

            attrs.setInteriorMaterial(new Material(color));
            attrs.setInteriorOpacity((double)color.getAlpha() / 255);
        }

        if (style.getColorMode() != null && "random".equals(style.getColorMode()))
            attrs.setInteriorMaterial(new Material(WWUtil.makeRandomColor(attrs.getOutlineMaterial().getDiffuse())));

        return attrs;
    }

    /**
     * Indicate whether a specified sub-style has the "highlight" style-state field.
     *
     * @param subStyle the sub-style to test. May be null, in which case this method returns false.
     *
     * @return true if the sub-style has the "highlight" field, otherwise false.
     */
    public static boolean isHighlightStyleState(KMLAbstractSubStyle subStyle)
    {
        if (subStyle == null)
            return false;

        String styleState = (String) subStyle.getField(KMLConstants.STYLE_STATE);
        return styleState != null && styleState.equals(KMLConstants.HIGHLIGHT);
    }

    /**
     * Translate a KML units string ("pixels", "insetPixels", or "fraction") into the corresponding WW unit constant
     * ({@link AVKey#PIXELS}, {@link AVKey#INSET_PIXELS}, or {@link AVKey#FRACTION}.
     *
     * @param units KML units to translate.
     *
     * @return WW units, or null if the argument is not a valid KML unit.
     */
    public static String kmlUnitsToWWUnits(String units)
    {
        if (KML_PIXELS.equals(units))
            return AVKey.PIXELS;
        else if (KML_FRACTION.equals(units))
            return AVKey.FRACTION;
        else if (KML_INSET_PIXELS.equals(units))
            return AVKey.INSET_PIXELS;
        else
            return null;
    }

    /**
     * Translate a WorldWind units constant ({@link AVKey#PIXELS}, {@link AVKey#INSET_PIXELS}, or {@link
     * AVKey#FRACTION} to the corresponding KML unit string ("pixels", "insetPixels", or "fraction").
     *
     * @param units World Wind units to translate.
     *
     * @return KML units, or null if the argument is not a valid WW unit.
     */
    public static String wwUnitsToKMLUnits(String units)
    {
        if (AVKey.PIXELS.equals(units))
            return KML_PIXELS;
        else if (AVKey.FRACTION.equals(units))
            return KML_FRACTION;
        else if (AVKey.INSET_PIXELS.equals(units))
            return KML_INSET_PIXELS;
        else
            return null;
    }

    /**
     * Create a {@link Sector} from a {@link KMLLatLonBox}.
     *
     * @param box Box that defines sector.
     *
     * @return New sector.
     */
    public static Sector createSectorFromLatLonBox(KMLLatLonBox box)
    {
        final double north = box.getNorth();
        final double south = box.getSouth();
        final double west = box.getWest();
        final double east = box.getEast();

        double minLat = Math.min(north, south);
        double maxLat = Math.max(north, south);
        double minLon = Math.min(east, west);
        double maxLon = Math.max(east, west);

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);        
    }
}
