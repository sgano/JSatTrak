/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.terrain.ebs;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;

/**
 * @author Jim Miller
 * @version $Id: ConstantLonEdge.java 7181 2008-10-24 18:54:33Z jmiller $
 */
public class ConstantLonEdge implements gov.nasa.worldwind.terrain.ebs.Edge
{
	private Angle startLat, endLat, lon;

	public ConstantLonEdge(Angle lat1, Angle lat2, Angle lon)
	{
		startLat = lat1; endLat = lat2; this.lon = lon;
	}

	// generates a point at a fraction, t, of the arc length along the edge.
	public LatLon arcLengthPointOnEdge(double t)
	{
		// For constant longitude edges, this is simple:
		return pointOnEdge(t);
	}

	public Edge.EdgeType getEdgeType() { return Edge.EdgeType.ConstantLongitude; }
	
	public LatLon getStartPoint()
	{
		return new LatLon(startLat, lon);
	}

	public LatLon getEndPoint()
	{
		return new LatLon(endLat, lon);
	}

	public double maxLongitudeDegrees()
	{
		return lon.getDegrees();
	}

	public LatLon pointOnEdge(double t)
	{
		return new LatLon(
			startLat.multiply(1.0-t).add(endLat.multiply(t)),
			lon);
	}

	public EdgeType subdivide(Edge[] halves)
	{
		Angle midLat = Angle.midAngle(startLat, endLat);
		halves[0] = new ConstantLonEdge(startLat, midLat, lon);
		halves[1] = new ConstantLonEdge(midLat, endLat, lon);
		return Edge.EdgeType.ConstantLongitude;
	}

	public String toString()
	{
		return "Constant lon = " + lon.toString() + "; lat range: "
			+ startLat.toString() + " to " + endLat.toString();
	}
}
