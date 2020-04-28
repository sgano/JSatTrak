/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.terrain.ebs;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * @author Jim Miller
 * @version $Id: EdgeBoundedSector.java 7478 2008-11-11 14:54:53Z jmiller $
 */

public class EdgeBoundedSector extends Sector
{
	private Edge[] boundary;

	private static Globe theGlobe = null;

	private EdgeBoundedSector(Angle minLat, Angle maxLat, Angle minLon, Angle maxLon, Edge[] b)
	{
		super(minLat,maxLat,minLon,maxLon);
		boundary = new Edge[4];
        System.arraycopy(b,0,boundary,0,4);
	}

	public LatLon averageOfCorners()
	{
		Angle sumLat = Angle.fromDegrees(0.0);
		Angle sumLon = Angle.fromDegrees(0.0);
		for (int i=0 ; i<4 ; i++)
		{
			LatLon ll = boundary[i].getStartPoint();
			sumLat = sumLat.add(ll.getLatitude());
			sumLon = sumLon.add(ll.getLongitude());
		}
		return new LatLon(sumLat.multiply(0.25), sumLon.multiply(0.25));
	}

	@Override
    public Vec4 computeCenterPoint(Globe globe, double exaggeration)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

		LatLon average = averageOfCorners();
		Angle avgLat = average.getLatitude();
		Angle avgLon = average.getLongitude();

        return globe.computePointFromPosition(avgLat, avgLon, exaggeration * globe.getElevation(avgLat, avgLon));
    }

	@Override
    public Vec4[] computeCornerPoints(Globe globe, double exaggeration)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        Vec4[] corners = new Vec4[4];
		for (int i=0 ; i<4 ; i++)
		{
			LatLon ll = boundary[i].getStartPoint();
			Angle lat = ll.getLatitude();
			Angle lon = ll.getLongitude();
			corners[i] = globe.computePointFromPosition(lat,lon, exaggeration * globe.getElevation(lat,lon));
		}

        return corners;
    }

    @Override
    public boolean containsDegrees(double degreesLatitude, double degreesLongitude)
    {
        double f = Math.PI / 180.0;
        return containsRadians(f*degreesLatitude, f*degreesLongitude);
    }

    @Override
    public boolean containsRadians(double radiansLatitude, double radiansLongitude)
    {
        // initial cull
        if (!super.containsRadians(radiansLatitude,radiansLongitude))
            return false;

        // Need to look closer with respect to any GreatEllipseEdges that
        // might be present
        for (int i=0 ; i<4 ; i++)
        {
            if (boundary[i] instanceof GreatEllipseEdge)
            {
                GreatEllipseEdge ge = (GreatEllipseEdge)boundary[i];
                Vec4 normal = ge.getOutwardPointingNormal();
                Vec4 sp = ge.getStartPointXYZ();
                Vec4 givenPoint = theGlobe.computePointFromPosition(
                        Angle.fromRadians(radiansLatitude),Angle.fromRadians(radiansLongitude),0.0);
                Vec4 toPoint = givenPoint.subtract3(sp);
                if (normal.dot3(toPoint) > 0.0)
                    return false;
            }
        }
        return true;
    }

    private static EdgeBoundedSector fromEdges(Edge[] b)
	{
		double minLat=0.0, maxLat=0.0, minLon=0.0, maxLon=0.0;
		// Determine the overall min/max latitude/longitude. This bounding box
		// information will be loose for great ellipse edges.
		for (int i=0 ; i<4 ; i++)
		{
			LatLon start = b[i].getStartPoint();
			double lat = start.getLatitude().getDegrees();
			double lon = start.getLongitude().getDegrees();
			if (i == 0)
			{
				minLat = maxLat = lat;
				minLon = maxLon = lon;
			}
			else
			{
				if (lat < minLat) minLat = lat;
				else if (lat > maxLat) maxLat = lat;
				if (lon < minLon) minLon = lon;
				else if (lon > maxLon) maxLon = lon;
			}
		}
		return new EdgeBoundedSector(
			Angle.fromDegrees(minLat),Angle.fromDegrees(maxLat),
			Angle.fromDegrees(minLon),Angle.fromDegrees(maxLon),b);
	}

    @Override
    public LatLon getCentroid()
    {
        return averageOfCorners();
    }

    public Edge getEdge(int i) { return boundary[i]; }

	public static EdgeBoundedSector[] initialCubeMapping(Angle latitudeCutoff)
	{
		EdgeBoundedSector[] s = new EdgeBoundedSector[6];

		Edge[] e = new Edge[4];

		// start at the dateline and work around, creating the four lateral Sectors
		// We ensure that the edges are stored in a CCW order (when viewed from outside
		// the ellipsoid) around the sector.

		Angle negLatitudeCutoff = Angle.fromDegrees(-latitudeCutoff.getDegrees());

		// face 0
		e[0] = new ConstantLonEdge(negLatitudeCutoff,latitudeCutoff,   Angle.NEG180);
		e[1] = new ConstantLatEdge(   latitudeCutoff,Angle.NEG180,     Angle.NEG90);
		e[2] = new ConstantLonEdge(   latitudeCutoff,negLatitudeCutoff,Angle.NEG90);
		e[3] = new ConstantLatEdge(negLatitudeCutoff,Angle.NEG90,      Angle.NEG180);
		s[0] = EdgeBoundedSector.fromEdges(e);

		// face 1
		e[0] = new ConstantLonEdge(negLatitudeCutoff,latitudeCutoff,    Angle.NEG90);
		e[1] = new ConstantLatEdge(   latitudeCutoff,Angle.NEG90,       Angle.ZERO);
		e[2] = new ConstantLonEdge(   latitudeCutoff,negLatitudeCutoff, Angle.ZERO);
		e[3] = new ConstantLatEdge(negLatitudeCutoff,Angle.ZERO,        Angle.NEG90);
		s[1] = EdgeBoundedSector.fromEdges(e);

		// face 2
		e[0] = new ConstantLonEdge(negLatitudeCutoff,latitudeCutoff,    Angle.ZERO);
		e[1] = new ConstantLatEdge(   latitudeCutoff,Angle.ZERO,        Angle.POS90);
		e[2] = new ConstantLonEdge(   latitudeCutoff,negLatitudeCutoff, Angle.POS90);
		e[3] = new ConstantLatEdge(negLatitudeCutoff,Angle.POS90,       Angle.ZERO);
		s[2] = EdgeBoundedSector.fromEdges(e);

		// face 3
		e[0] = new ConstantLonEdge(negLatitudeCutoff,latitudeCutoff,    Angle.POS90);
		e[1] = new ConstantLatEdge(   latitudeCutoff,Angle.POS90,       Angle.POS180);
		e[2] = new ConstantLonEdge(   latitudeCutoff,negLatitudeCutoff, Angle.POS180);
		e[3] = new ConstantLatEdge(negLatitudeCutoff,Angle.POS180,      Angle.POS90);
		s[3] = EdgeBoundedSector.fromEdges(e);

		// face 4 - south pole region
		e[0] = new ConstantLatEdge(negLatitudeCutoff,Angle.POS180,Angle.POS90);
		e[1] = new ConstantLatEdge(negLatitudeCutoff,Angle.POS90, Angle.ZERO);
		e[2] = new ConstantLatEdge(negLatitudeCutoff,Angle.ZERO,  Angle.NEG90);
		e[3] = new ConstantLatEdge(negLatitudeCutoff,Angle.NEG90, Angle.NEG180);
		s[4] = EdgeBoundedSector.fromEdges(e);

		// face 5 - north pole region
		e[0] = new ConstantLatEdge(latitudeCutoff,Angle.NEG180, Angle.NEG90);
		e[1] = new ConstantLatEdge(latitudeCutoff,Angle.NEG90,  Angle.ZERO);
		e[2] = new ConstantLatEdge(latitudeCutoff,Angle.ZERO,   Angle.POS90);
		e[3] = new ConstantLatEdge(latitudeCutoff,Angle.POS90,  Angle.POS180);
		s[5] = EdgeBoundedSector.fromEdges(e);

		return s;
	}

    /**
     * Creates an iterator over the four corners of the sector, starting with the southwest position and continuing
     * counter-clockwise.
     *
     * @return an iterator for the sector.
     */
    public Iterator<LatLon> iterator()
    {
        return new Iterator<LatLon>()
        {
            private int position = 0;

            public boolean hasNext()
            {
                return this.position < 4;
            }

            public LatLon next()
            {
                if (this.position > 3)
                    throw new NoSuchElementException();

                return boundary[position++].getStartPoint();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

	public static void recordGlobe(Globe globe)
	{
		EdgeBoundedSector.theGlobe = globe;
	}

	@Override
	public Sector[] subdivide()
	{
		// subdivide the sector into four pieces. The north and south poles
		// are special cases for the initial subdivision. To ensure that the international
		// dateline is never crossed, we propogate the original four constant longitude
		// lines (PI=-PI, -PI/2, 0.0, PI/2) up to the north/south poles. Other than these
		// two initial special cases, subdivison proceeds by computing the midpoints of
		// the four sector edges, computing their intersection in the middle, and then
		// forming the four sectors as a result.
		// As in the initial cube mapping (see above), we continue to ensure that the edges
		// are stored in a CCW order (when viewed from outside the ellipsoid) around the sector.

		// the two pole sectors have four constant latitude edge boundaries
		int numConstantLatEdges = 0;
		for (int i=0 ; i<4 ; i++)
			if (boundary[i] instanceof ConstantLatEdge)
				numConstantLatEdges++;
		if (numConstantLatEdges == 4)
			return subdividePolarSector();
		return subdivideNonPolarSector();
	}

	private EdgeBoundedSector[] subdivideNonPolarSector() // sector does NOT contain a pole
	{
		// working storage:
		Edge[][] newEdges = new Edge[4][2];
		Edge.EdgeType[] eT = new Edge.EdgeType[4];

		boolean newEdgesMustBeGreatEllipses = false;
		for (int i=0 ; i<4 ; i++)
		{
			eT[i] = boundary[i].subdivide(newEdges[i]);
			if (eT[i] == Edge.EdgeType.GreatEllipse)
				newEdgesMustBeGreatEllipses = true;
		}
		if (eT[0] != eT[2])
			newEdgesMustBeGreatEllipses = true;
		if (newEdgesMustBeGreatEllipses)
			return subdivideUsingGreatEllipses(newEdges);
		return subdivideUsingConstantLatLonEdges(newEdges,eT);
	}

	private EdgeBoundedSector[] subdividePolarSector()
	// all four edges have Constant latitude. The north or south pole is at the center of this
	// sector and will become the single point common to all subdivided patches. Each edge
	// gets split in half and its endpoints get connected to the pole to form a four-sided
	// sector.
	{
		// working storage:
		Edge[] newEdges = new Edge[4];

		EdgeBoundedSector[] result = new EdgeBoundedSector[4];
		for (int i=0 ; i<4 ; i++)
		{
			LatLon start = boundary[i].getStartPoint();
			LatLon end = boundary[i].getEndPoint();
			Angle thePoleLat = Angle.POS90;
			if (start.getLatitude().getRadians() < 0.0) thePoleLat = Angle.NEG90;
			boundary[i].subdivide(newEdges); // fills positions 0 and 1
			newEdges[2] = new ConstantLonEdge(end.getLatitude(),thePoleLat,end.getLongitude());
			newEdges[3] = new ConstantLonEdge(thePoleLat,start.getLatitude(),start.getLongitude());
			result[i] = EdgeBoundedSector.fromEdges(newEdges);
		}
		return result;
	}

	private EdgeBoundedSector[] subdivideUsingConstantLatLonEdges(Edge[][] newEdges, Edge.EdgeType[] eT)
	{
		// simple case of subdividing one of the Sectors on the lateral sides of the globe
		EdgeBoundedSector[] result = new EdgeBoundedSector[4];
		// first find central point
		LatLon center = this.getCentroid();
		Angle centerLat = center.getLatitude(), centerLon = center.getLongitude();
		Edge[] oneSet = new Edge[4];
		// see diagram for reasoning
		int prevI = 3, curI = 0;
		for (int ns=0 ; ns<4 ; ns++) // creating 4 new sectors (ns)
		{
			oneSet[0] = newEdges[prevI][1]; oneSet[1] = newEdges[curI][0];
			LatLon start = oneSet[1].getStartPoint();
			LatLon end = oneSet[1].getEndPoint();
			if (eT[curI] == Edge.EdgeType.ConstantLatitude)
			{
				oneSet[2] = new ConstantLonEdge(end.getLatitude(),centerLat,centerLon);
				oneSet[3] = new ConstantLatEdge(centerLat,centerLon,start.getLongitude());
			}
			else
			{
				oneSet[2] = new ConstantLatEdge(centerLat,start.getLongitude(),centerLon);
				oneSet[3] = new ConstantLonEdge(centerLat,start.getLatitude(),centerLon);
			}
			result[ns] = EdgeBoundedSector.fromEdges(oneSet);
			prevI = (prevI + 1) % 4; curI++;
		}
		return result;
	}

	private EdgeBoundedSector[] subdivideUsingGreatEllipses(Edge[][] newEdges)
	{
		// Logic is basically the same as in "subdivideUsingConstantLatLonEdges", except
		// all edges are Great Ellipses
		EdgeBoundedSector[] result = new EdgeBoundedSector[4];

		// first find central point
		LatLon[] edgeSplitPoints = new LatLon[4];
		for (int i=0 ; i<4 ; i++)
			// the new point in edge i of parent Sector is newEdges[i][0]'s end
			// (or equivalently newEdges[i][1]'s start)
			edgeSplitPoints[i] = newEdges[i][0].getEndPoint();

		LatLon centerLatLon = GreatEllipseEdge.findCentralPoint(EdgeBoundedSector.theGlobe,edgeSplitPoints);
		Edge[] oneSet = new Edge[4];
		// see diagram for reasoning
		int prevI = 3, curI = 0;
		for (int ns=0 ; ns<4 ; ns++) // creating 4 new sectors (ns)
		{
			oneSet[0] = newEdges[prevI][1]; oneSet[1] = newEdges[curI][0];
			oneSet[2] = new GreatEllipseEdge(EdgeBoundedSector.theGlobe,
				edgeSplitPoints[curI].getLatitude(),centerLatLon.getLatitude(),
				edgeSplitPoints[curI].getLongitude(),centerLatLon.getLongitude());
			oneSet[3] = new GreatEllipseEdge(EdgeBoundedSector.theGlobe,
				centerLatLon.getLatitude(), edgeSplitPoints[prevI].getLatitude(),
				centerLatLon.getLongitude(), edgeSplitPoints[prevI].getLongitude());
			result[ns] = EdgeBoundedSector.fromEdges(oneSet);
			prevI = (prevI + 1) % 4; curI++;
		}

		return result;
	}

	public String toString()
	{
		String str = "EdgeBoundedSector[" +
			super.toString() +
			", Sector edges: ";
		for (int i=0 ; i<4 ; i++)
		{
			str += boundary[i].toString();
			if (i != 3) str += ", ";
		}
		return str + "]";
	}
}
