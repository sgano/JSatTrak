/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;

/**
 * Provides operations on the best available terrain. Operations such as line/terrain intersection and surface point
 * computation use the highest resolution terrain data available from the globe's elevation model. Because the best
 * available data may not be available when the operations are performed, the operations block while they retrieve the
 * required data from either the local disk cache or a remote server. A timeout may be specified to limit the amount of
 * time allowed for retrieving data. Operations fail if the timeout is exceeded.
 *
 * @author tag
 * @version $Id: Terrain.java 14177 2010-12-03 00:28:05Z tgaskins $
 */
public interface Terrain
{
    Globe getGlobe();

    double getVerticalExaggeration();

    Vec4 getSurfacePoint(Position position);

    Vec4 getSurfacePoint(Angle latitude, Angle longitude, double metersOffset);

    Intersection[] intersect(Position pA, Position pB);
}
