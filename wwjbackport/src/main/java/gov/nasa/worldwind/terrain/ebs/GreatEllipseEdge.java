/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.terrain.ebs;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

/**
 * @author Jim Miller
 * @version $Id: GreatEllipseEdge.java 7478 2008-11-11 14:54:53Z jmiller $
 */

public class GreatEllipseEdge implements gov.nasa.worldwind.terrain.ebs.Edge
{
	private Angle		startLat, endLat, startLon, endLon;
	private Globe		theGlobe;
	private Vec4		startPnt, endPnt; // cached to make "pointOnEdge" faster

	public GreatEllipseEdge(Globe globe, Angle lat1, Angle lat2, Angle lon1, Angle lon2)
	{
		this.startLat = lat1; this.endLat = lat2;
		this.startLon = lon1; this.endLon = lon2;
		theGlobe = globe;
		startPnt = globe.computePointFromPosition(lat1,lon1,globe.getElevation(lat1, lon1));
		endPnt   = globe.computePointFromPosition(lat2,lon2,globe.getElevation(lat2, lon2));		
	}

	public GreatEllipseEdge(Globe globe, LatLon start, LatLon end)
	{
		this(globe,start.getLatitude(),end.getLatitude(),start.getLongitude(),end.getLongitude());
	}

	// generates a point at a fraction, t, of the arc length along the edge.
	public LatLon arcLengthPointOnEdge(double t)
	{
		// We'll cheat for now:
		return pointOnEdge(t);
	}

	private static Vec4 centralPlaneNormal(Vec4 pt1, Vec4 pt2)
	{
		// compute and return the unit normal to the plane determined by the center of
		// the globe (assumed to be at (0,0,0)) and the two given points. That is, we
		// compute w = (pt1 - O) x (pt2 - O):
		Vec4 w = pt1.cross3(pt2);
		return w.normalize3();
	}

	public static LatLon findCentralPoint(Globe globe, LatLon[] edgeSplitPoints)
	{
		// a pair of great ellipse arcs are being generated, but must be represented as
		// two pairs, trimmed to their common point. One great ellipse runs from
		// edgeSplitPoints[0] to edgeSplitPoints[2]; the other runs from
		// edgeSplitPoints[1] to edgeSplitPoints[3].  This routine finds their
		// intersection (which MUST exist, unless the caller screwed up) by first creating
		// and intersecting the pair of planes determined by the center of the sphere and
		// the two point pairs, then by intersecting that line of intersection with the
		// globe. This minimizes numerical problems that might arise by trying to intersect
		// the two ellipses directly or by independently computing the midpoints of the
		// arcs using Angle.midAngle independently for the two edges.

		Vec4 p0 = globe.computePointFromPosition(
			edgeSplitPoints[0].getLatitude(),edgeSplitPoints[0].getLongitude(),0.0);
		Vec4 p1 = globe.computePointFromPosition(
			edgeSplitPoints[1].getLatitude(),edgeSplitPoints[1].getLongitude(),0.0);
		Vec4 p2 = globe.computePointFromPosition(
			edgeSplitPoints[2].getLatitude(),edgeSplitPoints[2].getLongitude(),0.0);
		Vec4 p3 = globe.computePointFromPosition(
			edgeSplitPoints[3].getLatitude(),edgeSplitPoints[3].getLongitude(),0.0);

		Vec4 w02 = centralPlaneNormal(p0,p2);
		Vec4 w13 = centralPlaneNormal(p1,p3);
		Vec4 lineDir = w02.cross3(w13);
		lineDir = lineDir.normalize3();
		// make  sure the line is pointing in the right direction because EllipticalGlobe.intersect
		// is only going to return one point when it sees the base point of the ray is inside the globe.
		if (lineDir.dot3(p0) < 0.0)
			lineDir = lineDir.getNegative3();

		return globe.getIntersectionPosition(new Line(Vec4.ZERO,lineDir));
	}

	public Edge.EdgeType getEdgeType() { return Edge.EdgeType.GreatEllipse; }

    public Vec4 getOutwardPointingNormal()
    {
        return centralPlaneNormal(endPnt,startPnt);
    }

    public LatLon getStartPoint()
	{
		return new LatLon(startLat, startLon);
	}

    public Vec4 getStartPointXYZ()
    {
        return startPnt;
    }

    public LatLon getEndPoint()
	{
		return new LatLon(endLat, endLon);
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
		// 'dir' is the point at P = ((1-t)startPnt + t*endPnt); since the globe is centered
		// at the origin, 'dir' is also the vector towards the ellipsoid from the center
		// through P.
		Vec4 dir = Vec4.mix3(t,startPnt,endPnt);
		return theGlobe.getIntersectionPosition(
			new Line(Vec4.ZERO,dir.normalize3()));
	}

	public EdgeType subdivide(Edge[] halves)
	{
		LatLon midLatLon = this.pointOnEdge(0.5);
		halves[0] = new GreatEllipseEdge(theGlobe, startLat, midLatLon.getLatitude(),
						startLon, midLatLon.getLongitude());
		halves[1] = new GreatEllipseEdge(theGlobe, midLatLon.getLatitude(), endLat,
						midLatLon.getLongitude(), endLon);
		return Edge.EdgeType.GreatEllipse;
	}

	public String toString()
	{
		return "GreatEllipse, lat range: " + startLat + " to " + endLat
			+ "; lon range: " + startLon + " to " + endLon;
	}
}
