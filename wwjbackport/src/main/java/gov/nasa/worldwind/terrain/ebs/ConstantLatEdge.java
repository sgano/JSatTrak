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
 * @version $Id: ConstantLatEdge.java 7181 2008-10-24 18:54:33Z jmiller $
 */
public class ConstantLatEdge implements gov.nasa.worldwind.terrain.ebs.Edge
{
	private Angle lat, startLon, endLon;

	public ConstantLatEdge(Angle lat, Angle lon1, Angle lon2)
	{
		this.lat = lat; startLon = lon1; endLon = lon2;
	}

	// generates a point at a fraction, t, of the arc length along the edge.
	public LatLon arcLengthPointOnEdge(double t)
	{
		// For constant latitude edges, this is simple:
		return pointOnEdge(t);
	}

	public Edge.EdgeType getEdgeType() { return Edge.EdgeType.ConstantLatitude; }
	
	public LatLon getStartPoint()
	{
		return new LatLon(lat, startLon);
	}

	public LatLon getEndPoint()
	{
		return new LatLon(lat, endLon);
	}

	public double maxLongitudeDegrees()
	{
		double l1 = startLon.getDegrees();
		double l2 = endLon.getDegrees();
		if (l1 > l2) return l1;
		return l2;
	}

	public LatLon pointOnEdge(double t)
	{
		return new LatLon(
			lat,
			startLon.multiply(1.0-t).add(endLon.multiply(t)));
	}

	public EdgeType subdivide(Edge[] halves)
	{
		Angle midLon = Angle.midAngle(startLon, endLon);
		halves[0] = new ConstantLatEdge(lat, startLon, midLon);
		halves[1] = new ConstantLatEdge(lat, midLon, endLon);
		return Edge.EdgeType.ConstantLatitude;
	}

	public String toString()
	{
		return "Constant lat = " + lat.toString() + "; lon range: " +
			startLon.toString() + " to " + endLon.toString();
	}
}
