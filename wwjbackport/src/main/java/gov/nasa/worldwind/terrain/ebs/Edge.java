/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.terrain.ebs;

import gov.nasa.worldwind.geom.LatLon;

/**
 * @author Jim Miller
 * @version $Id: Edge.java 7181 2008-10-24 18:54:33Z jmiller $
 */

public interface Edge
{
	public enum EdgeType { ConstantLatitude, ConstantLongitude, GreatEllipse }

	LatLon arcLengthPointOnEdge(double t); // generates a point at a fraction, t,
					// of the arc length along the edge.
	EdgeType	getEdgeType();
	LatLon		getEndPoint();
	LatLon		getStartPoint();
	double		maxLongitudeDegrees();
	LatLon		pointOnEdge(double t); // generates a point based on a (1-t)-t blend
	                              // of start/end points, but does NOT guarantee that
								  // the point will be at that arc length fraction EXCEPT
								  // for t=0.0, t=0.5, and t=1.0.
	// following returns the edge type as a convenience so that the caller (usually
	// someone subdividing a sector) will know what kind of sector to create.
	EdgeType	subdivide(Edge[] halves);
	String		toString();
}
