/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.*;

import java.util.List;

/**
 * @author Tom Gaskins
 * @version $Id: Globe.java 13553 2010-07-15 07:40:06Z tgaskins $
 */
public interface Globe extends WWObject, Extent
{
    Extent getExtent();

    double getEquatorialRadius();

    double getPolarRadius();

    double getMaximumRadius();

    double getRadiusAt(Angle latitude, Angle longitude);

    double getElevation(Angle latitude, Angle longitude);

    double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] elevations);

    double getMaxElevation();

    double getMinElevation();

    Position getIntersectionPosition(Line line);

    double getEccentricitySquared();

    Vec4 computePointFromPosition(Angle latitude, Angle longitude, double metersElevation);

    Vec4 computePointFromPosition(LatLon latLon, double metersElevation);

    Vec4 computePointFromPosition(Position position);

    Vec4 computePointFromLocation(LatLon location);

    Position computePositionFromPoint(Vec4 point);

    Vec4 computeSurfaceNormalAtLocation(Angle latitude, Angle longitude);

    Vec4 computeSurfaceNormalAtPoint(Vec4 point);

    Vec4 computeNorthPointingTangentAtLocation(Angle latitude, Angle longitude);

    /**
     * Returns the cartesian transform Matrix that maps model coordinates to a local coordinate system at (latitude,
     * longitude, metersElevation). They X axis is mapped to the vector tangent to the globe and pointing East. The Y
     * axis is mapped to the vector tangent to the Globe and pointing to the North Pole. The Z axis is mapped to the
     * Globe normal at (latitude, longitude, metersElevation). The origin is mapped to the cartesian position of
     * (latitude, longitude, metersElevation).
     *
     * @param latitude        the latitude of the position.
     * @param longitude       the longitude of the position.
     * @param metersElevation the number of meters above or below mean sea level.
     *
     * @return the cartesian transform Matrix that maps model coordinates to the local coordinate system at the
     *         specified position.
     */
    Matrix computeModelCoordinateOriginTransform(Angle latitude, Angle longitude, double metersElevation);

    /**
     * Returns the cartesian transform Matrix that maps model coordinates to a local coordinate system at (latitude,
     * longitude, metersElevation). They X axis is mapped to the vector tangent to the globe and pointing East. The Y
     * axis is mapped to the vector tangent to the Globe and pointing to the North Pole. The Z axis is mapped to the
     * Globe normal at (latitude, longitude, metersElevation). The origin is mapped to the cartesian position of
     * (latitude, longitude, metersElevation).
     *
     * @param position the latitude, longitude, and number of meters above or below mean sea level.
     *
     * @return the cartesian transform Matrix that maps model coordinates to the local coordinate system at the
     *         specified position.
     */
    Matrix computeModelCoordinateOriginTransform(Position position);

    double getRadiusAt(LatLon latLon);

    /**
     * Returns the minimum and maximum elevations at a specified location on the Globe. This returns a two-element array
     * filled with zero if this Globe has no elevation model.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return A two-element <code>double</code> array indicating the minimum and maximum elevations at the specified
     *         location, respectively. These values are the global minimum and maximum if the local minimum and maximum
     *         values are currently unknown, or zero if this Globe has no elevation model.
     */
    double[] getMinAndMaxElevations(Angle latitude, Angle longitude);

    /**
     * Returns the minimum and maximum elevations within a specified sector on the Globe. This returns a two-element
     * array filled with zero if this Globe has no elevation model.
     *
     * @param sector the sector in question.
     *
     * @return A two-element <code>double</code> array indicating the sector's minimum and maximum elevations,
     *         respectively. These elements are the global minimum and maximum if the local minimum and maximum values
     *         are currently unknown, or zero if this Globe has no elevation model.
     */
    double[] getMinAndMaxElevations(Sector sector);

    Intersection[] intersect(Line line, double altitude);

    Intersection[] intersect(Triangle t, double altitude);

    Tessellator getTessellator();

    void setTessellator(Tessellator tessellator);

    SectorGeometryList tessellate(DrawContext dc);

    Object getStateKey(DrawContext dc);

    ElevationModel getElevationModel();

    void setElevationModel(ElevationModel elevationModel);

    boolean isPointAboveElevation(Vec4 point, double elevation);
}
