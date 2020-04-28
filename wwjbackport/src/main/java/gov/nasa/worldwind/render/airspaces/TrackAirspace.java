/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * @author garakl
 * @version $Id: TrackAirspace.java 13347 2010-04-27 23:50:20Z dcollins $
 */
public class TrackAirspace extends AbstractAirspace
{
    private List<Box> legs = new ArrayList<Box>();
    private boolean enableInnerCaps = true;
    private boolean legsOutOfDate = true;

    public TrackAirspace(Collection<Box> legs)
    {
        this.addLegs(legs);
    }

    public TrackAirspace(AirspaceAttributes attributes)
    {
        super(attributes);
    }

    public TrackAirspace()
    {
    }

    public List<Box> getLegs()
    {
        return Collections.unmodifiableList(this.legs);
    }

    public void setLegs(Collection<Box> legs)
    {
        this.legs.clear();
        this.addLegs(legs);
    }

    protected void addLegs(Iterable<Box> newLegs)
    {
        if (newLegs != null)
        {
            for (Box b : newLegs)
            {
                if (b != null)
                    this.addLeg(b);
            }
            this.setLegsOutOfDate();
        }
    }

    public Box addLeg(LatLon start, LatLon end, double lowerAltitude, double upperAltitude,
        double leftWidth, double rightWidth)
    {
        if (start == null)
        {
            String message = "nullValue.StartIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (end == null)
        {
            String message = "nullValue.EndIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean[] terrainConformant = this.isTerrainConforming();

        Box leg = new Box();
        leg.setAltitudes(lowerAltitude, upperAltitude);
        leg.setTerrainConforming(terrainConformant[0], terrainConformant[1]);
        leg.setLocations(start, end);
        leg.setWidths(leftWidth, rightWidth);
        this.addLeg(leg);
        return leg;
    }

    protected void addLeg(Box leg)
    {
        if (leg == null)
        {
            String message = "nullValue.LegIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        leg.setForceCullFace(true);
        this.legs.add(leg);
        this.setExtentOutOfDate();
        this.setLegsOutOfDate();
    }

    public void removeAllLegs()
    {
        this.legs.clear();
    }

    public boolean isEnableInnerCaps()
    {
        return this.enableInnerCaps;
    }

    public void setEnableInnerCaps(boolean draw)
    {
        this.enableInnerCaps = draw;
        this.setLegsOutOfDate();
    }

    public void setAltitudes(double lowerAltitude, double upperAltitude)
    {
        super.setAltitudes(lowerAltitude, upperAltitude);

        for (Box l : this.legs)
        {
            l.setAltitudes(lowerAltitude, upperAltitude);
        }

        this.setLegsOutOfDate();
    }

    public void setTerrainConforming(boolean lowerTerrainConformant, boolean upperTerrainConformant)
    {
        super.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);

        for (Box l : this.legs)
        {
            l.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);
        }

        this.setLegsOutOfDate();
    }

    public boolean isAirspaceVisible(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // If the parent TrackAirspace is not visible, then return false immediately without testing the child legs.
        if (!super.isAirspaceVisible(dc))
            return false;

        boolean visible = false;

        // The parent TrackAirspace is visible. Since the parent TrackAirspace's extent potentially contains volumes
        // where no child geometry exists, test that at least one of the child legs are visible.
        for (Box b : this.legs)
        {
            if (b.isAirspaceVisible(dc))
            {
                visible = true;
                break;
            }
        }

        return visible;
    }

    public Position getReferencePosition()
    {
        ArrayList<LatLon> locations = new ArrayList<LatLon>(2 * this.legs.size());
        for (Box box : this.legs)
        {
            LatLon[] ll = box.getLocations();
            locations.add(ll[0]);
            locations.add(ll[1]);
        }

        return this.computeReferencePosition(locations, this.getAltitudes());
    }

    protected Extent computeExtent(Globe globe, double verticalExaggeration)
    {
        // Update the child leg vertices if they're out of date. Since the leg vertices are input to the parent
        // TrackAirspace's extent computation, they must be current before computing the parent's extent.
        if (this.isLegsOutOfDate())
        {
            this.doUpdateLegs(globe, verticalExaggeration);
        }

        ArrayList<Extent> childExtents = new ArrayList<Extent>();

        for (Box b : this.getLegs())
        {
            childExtents.add(b.getExtent(globe, verticalExaggeration));
        }

        if (childExtents.size() == 0)
        {
            return null;
        }
        else if (childExtents.size() == 1)
        {
            return childExtents.get(0);
        }
        else
        {
            return Sphere.createBoundingSphere(childExtents);
        }
    }

    protected void doMoveTo(Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Don't call super.moveTo(). Each box should move itself according to the properties it was constructed with.
        for (Box box : this.legs)
        {
            box.doMoveTo(oldRef, newRef);
        }

        this.setExtentOutOfDate();
        this.setLegsOutOfDate();
    }

    protected boolean isLegsOutOfDate()
    {
        return this.legsOutOfDate;
    }

    protected void setLegsOutOfDate()
    {
        this.legsOutOfDate = true;
    }

    protected void doUpdateLegs(Globe globe, double verticalExaggeration)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int count = this.legs.size();
        Plane prevJoinPlane = null;

        for (int i = 0; i < count; i++)
        {
            Box leg1 = this.legs.get(i);
            Box leg2 = (i + 1 < count) ? this.legs.get(i + 1) : null;

            Vec4[] vertices = Box.computeStandardVertices(globe, verticalExaggeration, leg1);

            Line low_left_line = Line.fromSegment(vertices[Box.A_LOW_LEFT], vertices[Box.B_LOW_LEFT]);
            Line low_right_line = Line.fromSegment(vertices[Box.A_LOW_RIGHT], vertices[Box.B_LOW_RIGHT]);
            Line up_left_line = Line.fromSegment(vertices[Box.A_UPR_LEFT], vertices[Box.B_UPR_LEFT]);
            Line up_right_line = Line.fromSegment(vertices[Box.A_UPR_RIGHT], vertices[Box.B_UPR_RIGHT]);

            Plane joinPlane = null;

            if (i == 0)
            {
                leg1.setEnableStartCap(true);
            }

            if (leg2 != null)
            {
                leg1.setEnableEndCap(this.isEnableInnerCaps());
                leg2.setEnableStartCap(this.isEnableInnerCaps());

                // If the two legs are disjoint, do not compute a join on this endcap.
                if (!this.areLegsDisjoint(leg1, leg2))
                {
                    joinPlane = this.computeJoiningPlane(globe, leg1, leg2);

                    vertices[Box.B_LOW_LEFT] = joinPlane.intersect(low_left_line);
                    vertices[Box.B_LOW_RIGHT] = joinPlane.intersect(low_right_line);
                    vertices[Box.B_UPR_LEFT] = joinPlane.intersect(up_left_line);
                    vertices[Box.B_UPR_RIGHT] = joinPlane.intersect(up_right_line);
                }
                else
                {
                    joinPlane = null;

                    leg1.setEnableEndCap(true);
                    leg2.setEnableStartCap(true);
                }
            }
            else
            {
                leg1.setEnableEndCap(true);
            }

            if (prevJoinPlane != null && i > 0)
            {
                vertices[Box.A_LOW_LEFT] = prevJoinPlane.intersect(low_left_line);
                vertices[Box.A_LOW_RIGHT] = prevJoinPlane.intersect(low_right_line);
                vertices[Box.A_UPR_LEFT] = prevJoinPlane.intersect(up_left_line);
                vertices[Box.A_UPR_RIGHT] = prevJoinPlane.intersect(up_right_line);
            }

            leg1.setVertices(vertices);
            prevJoinPlane = joinPlane;
        }

        this.legsOutOfDate = false;
    }

    protected Plane computeJoiningPlane(Globe globe, Box leg1, Box leg2)
    {
        LatLon[] leg1Loc = leg1.getLocations();
        LatLon[] leg2Loc = leg2.getLocations();
        double[] leg1Altitudes = leg1.getAltitudes();
        double[] leg2Altitudes = leg2.getAltitudes();

        // 'al' is the lower center point of leg1's begining.
        Vec4 al = globe.computePointFromPosition(leg1Loc[0].getLatitude(), leg1Loc[0].getLongitude(),
            leg1Altitudes[0]);
        // 'bl' is the lower center point of leg1's ending.
        Vec4 bl = globe.computePointFromPosition(leg1Loc[1].getLatitude(), leg1Loc[1].getLongitude(),
            leg1Altitudes[0]);
        // 'bu' is the upper center point of leg1's ending.
        Vec4 bu = globe.computePointFromPosition(leg1Loc[1].getLatitude(), leg1Loc[1].getLongitude(),
            leg1Altitudes[1]);
        // 'cl' is the lower center point of the leg2's ending.
        Vec4 cl = globe.computePointFromPosition(leg2Loc[1].getLatitude(), leg2Loc[1].getLongitude(),
            leg2Altitudes[0]);

        Vec4 b_ul = bu.subtract3(bl).normalize3();
        Vec4 ab_l = al.subtract3(bl).perpendicularTo3(b_ul).normalize3();
        Vec4 cb_l = cl.subtract3(bl).perpendicularTo3(b_ul).normalize3();

        Vec4 n;

        Vec4 ab_plus_cb = ab_l.add3(cb_l);
        if (ab_plus_cb.getLength3() < 0.0000001)
        {
            n = ab_l.normalize3();
        }
        else
        {
            n = b_ul.cross3(ab_plus_cb).normalize3();
        }

        double d = -bl.dot3(n);

        return new Plane(n.getX(), n.getY(), n.getZ(), d);
    }

    protected boolean areLegsDisjoint(Box leg1, Box leg2)
    {
        LatLon[] leg1Loc = leg1.getLocations();
        LatLon[] leg2Loc = leg2.getLocations();
        double[] leg1Altitudes = leg1.getAltitudes();
        double[] leg2Altitudes = leg2.getAltitudes();
        boolean[] leg1TerrainConformance = leg1.isTerrainConforming();
        boolean[] leg2TerrainConformance = leg2.isTerrainConforming();

        return !leg1Loc[1].equals(leg2Loc[0])
            || leg1Altitudes[0] != leg2Altitudes[0]
            || leg1Altitudes[1] != leg2Altitudes[1]
            || leg1TerrainConformance[0] != leg2TerrainConformance[0]
            || leg1TerrainConformance[1] != leg2TerrainConformance[1];
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    protected void doRenderGeometry(DrawContext dc, String drawStyle)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Update the child leg vertices if they're out of date. Since the leg vertices are used to determine how each
        // leg is shaped with respect to its neighbors, the vertices must be current before rendering each leg.
        if (this.isLegsOutOfDate())
        {
            this.doUpdateLegs(dc.getGlobe(), dc.getVerticalExaggeration());
        }

        for (Box b : this.getLegs())
        {
            if (!b.isVisible())
                continue;

            if (!b.isAirspaceVisible(dc))
                continue;

            b.renderGeometry(dc, drawStyle);
        }
    }

    protected void doRenderExtent(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.doRenderExtent(dc);

        for (Box b : this.legs)
        {
            b.renderExtent(dc);
        }
    }

    //**************************************************************//
    //********************  END Geometry Rendering  ****************//
    //**************************************************************//

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsBoolean(context, "enableInnerCaps", this.isEnableInnerCaps());

        RestorableSupport.StateObject so = rs.addStateObject(context, "legs");
        for (Box leg : this.legs)
        {
            RestorableSupport.StateObject lso = rs.addStateObject(so, "leg");
            leg.doGetRestorableState(rs, lso);
        }
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        Boolean b = rs.getStateValueAsBoolean(context, "enableInnerCaps");
        if (b != null)
            this.setEnableInnerCaps(b);

        RestorableSupport.StateObject so = rs.getStateObject(context, "legs");
        if (so == null)
            return;

        RestorableSupport.StateObject[] lsos = rs.getAllStateObjects(so, "leg");
        if (lsos == null || lsos.length == 0)
            return;

        ArrayList<Box> legList = new ArrayList<Box>(lsos.length);

        for (RestorableSupport.StateObject lso : lsos)
        {
            if (lso != null)
            {
                Box leg = new Box();
                leg.doRestoreState(rs, lso);
                legList.add(leg);
            }
        }

        this.setLegs(legList);
    }
}
