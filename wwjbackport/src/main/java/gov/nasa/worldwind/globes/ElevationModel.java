/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;

import java.util.List;

/**
 * <p/>
 * Provides the elevations a {@link Globe} or other object holding elevations.
 * <p/>
 * An <code>ElevationModel</code> often approximates elevations at multiple levels of spatial resolution. For any given
 * viewing position the model determines an appropriate target resolution. That target resolution may not be immediately
 * achievable, however, because the corresponding elevation data might not be locally available and must be retrieved
 * from a remote location. When this is the case, the <code>Elevations</code> object returned for a sector holds the
 * resolution achievable with the data currently available. That resolution may not be the same as the target
 * resolution. The achieved resolution is made available in the interface.
 * <p/>
 *
 * @author Tom Gaskins
 * @version $Id: ElevationModel.java 13695 2010-09-02 18:54:27Z tgaskins $
 */
public interface ElevationModel extends WWObject, Restorable, Disposable
{
    /**
     * Returns the elevation model's name, as specified in the most recent call to {@link #setName}.
     *
     * @return the elevation model's name.
     */
    String getName();

    /**
     * Set the elevation model's name. The name is a convenience attribute typically used to identify the elevation
     * model in user interfaces. By default, an elevation model has no name.
     *
     * @param name the name to assign to the elevation model.
     */
    void setName(String name);

    /**
     * Indicates whether the elevation model is allowed to retrieve data from the network. Some elevation models have no
     * need to retrieve data from the network. This state is meaningless for such elevation models.
     *
     * @return <code>true</code> if the elevation model is enabled to retrieve network data, else <code>false</code>.
     */
    boolean isNetworkRetrievalEnabled();

    /**
     * Controls whether the elevation model is allowed to retrieve data from the network. Some elevation models have no
     * need for data from the network. This state may be set but is meaningless for such elevation models.
     *
     * @param networkRetrievalEnabled <code>true</code> if network retrieval is allowed, else <code>false</code>.
     */
    void setNetworkRetrievalEnabled(boolean networkRetrievalEnabled);

    /**
     * Returns the current expiry time.
     *
     * @return the current expiry time.
     */
    long getExpiryTime();

    /**
     * Specifies the time of the elevation model's most recent dataset update. If greater than zero, the model ignores
     * and eliminates any previously cached data older than the time specfied, and requests new information from the
     * data source. If zero, the model uses any expiry times intrinsic to the model, typically initialized at model
     * construction. The default expiry time is 0, thereby enabling a model's intrinsic expiration criteria.
     *
     * @param expiryTime the expiry time of any cached data, expressed as a number of milliseconds beyond the epoch.
     *
     * @see System#currentTimeMillis() for a description of milliseconds beyond the epoch.
     */
    void setExpiryTime(long expiryTime);

    /**
     * Specifies the value used to identify missing data in an elevation model. Locations with this elevation value are
     * assigned the missing-data replacement value, specified by {@link #setMissingDataReplacement(double)}.
     * <p/>
     * The missing-data value is often specified by the metadata of the data set, in which case the elevation model
     * automatically defines that value to be the missing-data signal. When the missing-data signal is not specified in
     * the metadata, the application may specify it via this method.
     *
     * @param flag a reference to the missing-data signal value. The default reference is null, indicating that there is
     *             no missing-data signal so all elevation values are considered valid.
     *
     * @see #setMissingDataReplacement(double), #getMissingDataSignal
     */
    void setMissingDataSignal(double flag);

    /**
     * Returns the current missing-data signal.
     *
     * @return the missing-data signal, or null if no signal has been specified by either the application or the data
     *         set.
     *
     * @see #getMissingDataReplacement()
     */
    double getMissingDataSignal();

    /**
     * Indicates whether the elevation model overlaps a specified sector.
     *
     * @param sector the sector in question.
     *
     * @return 0 if the elevation model fully contains the sector, 1 if the elevation model intersects the sector but
     *         does not fully contain it, or -1 if the elevation does not intersect the elevation model.
     */
    int intersects(Sector sector);

    /**
     * Indicates whether a specified location is within the elevation model's domain.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return true if the location is within the elevation model's domain, otherwise false.
     */
    boolean contains(Angle latitude, Angle longitude);

    /**
     * Returns the maximum elevation contained in the elevevation model. When the elevation model is associated with a
     * globe, this value is the elevation of the highest point on the globe.
     *
     * @return The maximum elevation of the elevation model.
     */
    double getMaxElevation();

    /**
     * Returns the minimum elevation contained in the elevevation model. When associated with a globe, this value is the
     * elevation of the lowest point on the globe. It may be negative, indicating a value below mean surface level. (Sea
     * level in the case of Earth.)
     *
     * @return The minimum elevation of the model.
     */
    double getMinElevation();

    /**
     * Returns the minimum and maximum elevations at a specified location.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return A two-element <code>double</code> array indicating the minimum and maximum elevations at the specified
     *         location, respectively. These values are the global minimum and maximum if the local minimum and maximum
     *         values are currently unknown.
     */
    double[] getExtremeElevations(Angle latitude, Angle longitude);

    /**
     * Returns the minimum and maximum elevations within a specified sector of the elevation model.
     *
     * @param sector the sector in question.
     *
     * @return A two-element <code>double</code> array indicating the sector's minimum and maximum elevations,
     *         respectively. These elements are the global minimum and maximum if the local minimum and maximum values
     *         are currently unknown.
     */
    double[] getExtremeElevations(Sector sector);

    /**
     * Indicates the best resolution attainable for a specified sector.
     *
     * @param sector the sector in question. If null, the elevation model's best overall resolution is returned. This is
     *               the best attainable at <em>some</> locations but not necessarily at all locations.
     *
     * @return the best resolution attainable for the specified sector, in radians, or {@link Double#MAX_VALUE} if the
     *         sector does not intersect the elevation model.
     */
    double getBestResolution(Sector sector);

    /**
     * Returns the detail hint associated with the specified sector. If the elevation model does not have any detail
     * hint for the sector, this will return zero.
     *
     * @param sector the sector in question.
     *
     * @return The detail hint corresponding to the specified sector.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null.
     */
    double getDetailHint(Sector sector);

    /**
     * Returns the elevation at a specified location. If the elevation at the specified location is the elevation
     * model's missing data signal, or if the location specified is outside the elevation model's coverage area, the
     * elevation model's missing data replacement value is returned.
     * <p/>
     * The elevation returned from this method is the best available in memory. If no elevation is in memory, the
     * elevation model's minimum extreme elevation at the location is returned. Local disk caches are not consulted.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return The elevation corresponding to the specified location, or the elevation model's missing-data replacement
     *         value if there is no elevation for the given location.
     *
     * @see #setMissingDataSignal(double)
     * @see #getUnmappedElevation(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle)
     */
    double getElevation(Angle latitude, Angle longitude);

    /**
     * Returns the elevation at a specified location, but without mapping missing data to the elevation model's missing
     * data replacement value. When a missing data signal is found, the signal value is returned, not the replacement
     * value.
     *
     * @param latitude  the latitude of the location for which to return the elevation.
     * @param longitude the longitude of the location for which to return the elevation.
     *
     * @return the elevation at the specified location, or the elevation model's missing data signal. If no data is
     *         currently in memory for the location, and the location is within the elevation model's coverage area, the
     *         elevation model's minimum elevation at that location is returned.
     */
    double getUnmappedElevation(Angle latitude, Angle longitude);

    /**
     * Returns the elevations of a collection of locations. Maps any elevation values corresponding to the missing data
     * signal to the elevation model's missing data replacement value. If a location within the elevation model's
     * coverage area cannot currently be determined, the elevation model's minimum extreme elevation for that location
     * is returned in the output buffer. If a location is outside the elevation model's coverage area, the output buffer
     * for that location is not modified; it retains the buffer's original value.
     *
     * @param sector           the sector in question.
     * @param latlons          the locations to return elevations for. If a location is null, the output buffer for that
     *                         location is not modified.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.)
     * @param buffer           an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolution achieved, in radians, or {@link Double.MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations.
     *
     * @see #setMissingValueSentinel(Double)
     */
    @SuppressWarnings({"JavadocReference"})
    double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer);

    /**
     * Returns the elevations of a collection of locations. Does not map any elevation values corresponding to the
     * missing data signal to the elevation model's missing data replacement value. If a location within the elevation
     * model's coverage area cannot currently be determined, the elevation model's minimum extreme elevation for that
     * location is returned in the output buffer. If a location is outside the elevation model's coverage area, the
     * output buffer for that location is not modified; it retains the buffer's original value.
     *
     * @param sector           the sector in question.
     * @param latlons          the locations to return elevations for. If a location is null, the output buffer for that
     *                         location is not modified.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.)
     * @param buffer           an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolution achieved, in radians, or {@link Double.MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations.
     *
     * @see #setMissingValueSentinel(Double)
     */
    @SuppressWarnings({"JavadocReference"})
    double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution,
        double[] buffer);

    void composeElevations(Sector sector, List<? extends LatLon> latlons, int tileWidth, double[] buffer)
        throws Exception;

    /**
     * Returns the elevation used for missing values in the elevation dataset.
     *
     * @return the elevation, in meters, used for locations in the dataset whose value is the missing-data signal.
     *
     * @see #setMissingDataSignal(double), #getMissingDataSignal
     */
    double getMissingDataReplacement();

    /**
     * Specifies the elevation used for missing values in the elevation dataset.
     *
     * @param missingDataValue the elevation, in meters, used for locations in the dataset whose value is the
     *                         missing-data signal.
     *
     * @see #setMissingDataSignal(double)
     */
    void setMissingDataReplacement(double missingDataValue);

    /**
     * Returns the current "transparent" elevation value indicating that no elevation is assigned to the corresponding
     * location during the bulk elevation methods {@link #getElevations(gov.nasa.worldwind.geom.Sector, java.util.List,
     * double, double[])}, {@link #getUnmappedElevations(gov.nasa.worldwind.geom.Sector, java.util.List, double,
     * double[])}, and {@link #composeElevations(gov.nasa.worldwind.geom.Sector, java.util.List, int, double[])}.
     *
     * @return the "transparent" elevation value.
     */
    Double getTransparentElevationValue();

    /**
     * Specifies the elevation value indicating a "transparent" elevation. When the value is encountered during bulk
     * computation of elevations no value is returned for the corresponding location. The bulk computation methods are
     * {@link #getElevations(gov.nasa.worldwind.geom.Sector, java.util.List, double, double[])}, {@link
     * #getUnmappedElevations(gov.nasa.worldwind.geom.Sector, java.util.List, double, double[])}, and {@link
     * #composeElevations(gov.nasa.worldwind.geom.Sector, java.util.List, int, double[])}.
     *
     * @param transparentElevationValue the "transparent" elevation signal. May be null, indicating that there is no
     *                                  transparent value and transparent-value detection is not performed.
     */
    void setTransparentElevationValue(Double transparentElevationValue);
}
