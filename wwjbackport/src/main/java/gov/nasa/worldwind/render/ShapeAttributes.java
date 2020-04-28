/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.util.RestorableSupport;

/**
 * @author dcollins
 * @version $Id$
 */
public interface ShapeAttributes extends Exportable
{
    long getLastModifiedTime();

    boolean isDrawInterior();

    void setDrawInterior(boolean draw);

    boolean isDrawOutline();

    void setDrawOutline(boolean draw);

    boolean isEnableAntialiasing();

    void setEnableAntialiasing(boolean enable);

    Material getInteriorMaterial();

    void setInteriorMaterial(Material material);

    Material getOutlineMaterial();

    void setOutlineMaterial(Material material);

    double getInteriorOpacity();

    void setInteriorOpacity(double opacity);

    double getOutlineOpacity();

    void setOutlineOpacity(double opacity);

    double getOutlineWidth();

    void setOutlineWidth(double width);

    int getOutlineStippleFactor();

    void setOutlineStippleFactor(int factor);

    short getOutlineStipplePattern();

    void setOutlineStipplePattern(short pattern);

    Object getInteriorImageSource();

    void setInteriorImageSource(Object imageSource);

    double getInteriorImageScale();

    void setInteriorImageScale(double scale);

    void getRestorableState(RestorableSupport rs, RestorableSupport.StateObject so);

    void restoreState(RestorableSupport rs, RestorableSupport.StateObject so);

    /**
     * Returns a new ShapeAttributes instance of the same type as this ShapeAttributes, who's properties are configured
     * exactly as this ShapeAttributes.
     *
     * @return a copy of this ShapeAttributes.
     */
    ShapeAttributes copy();

    /**
     * Copies the specified ShapeAttributes' properties into this object's properties. This does nothing if the
     * specified attributes is null.
     *
     * @param attributes the attributes to copy.
     */
    void copy(ShapeAttributes attributes);

    boolean isUnresolved();

    void setUnresolved(boolean unresolved);

    /**
     * Indicates whether lighting is applied to the shape.
     *
     * @return true to apply lighting, otherwise false.
     */
    boolean isEnableLighting();

    /**
     * Specifies whether to apply lighting to the shape.
     *
     * @param enableLighting true to apply lighting, otherwise false.
     */
    void setEnableLighting(boolean enableLighting);
}
