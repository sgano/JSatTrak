/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.EllipsoidalGlobe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: ImageEditor.java 13365 2010-05-04 04:38:08Z tgaskins $
 */
public class ImageEditor implements SelectListener
{
    private static final int NONE = 0;

    private static final int MOVING = 1;
    private static final int SIZING = 2;

    private static final double EDGE_FACTOR = 0.10;

    private final WorldWindow wwd;
    private int operation = NONE;
    private Position previousPosition = null;

    private RenderableLayer layer;
    private RegionShape shape;

    public ImageEditor(WorldWindow wwd, SurfaceImage image)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.layer = new RenderableLayer();
        this.layer.setPickEnabled(true);
        this.shape = new RegionShape(image);
        this.layer.addRenderable(this.shape);

        wwd.addSelectListener(this);

        this.wwd.getInputHandler().addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                if (MouseEvent.BUTTON1_DOWN_MASK != mouseEvent.getModifiersEx())
                    return;

                if (!shape.armed)
                    return;

                shape.resizeable = true;
                shape.startPosition = null;
                shape.armed = false;

                mouseEvent.consume();
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent)
            {
                if (MouseEvent.BUTTON1 != mouseEvent.getButton())
                    return;

                if (shape.resizeable)
                    ((Component) ImageEditor.this.wwd).setCursor(Cursor.getDefaultCursor());
                shape.resizeable = false;

                mouseEvent.consume();
            }
        });

        this.wwd.getInputHandler().addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseDragged(MouseEvent mouseEvent)
            {
                if (shape.resizeable)
                    mouseEvent.consume();
            }
        });
    }

    public void enableLayer()
    {
        this.shape.startPosition = null;

        LayerList layers = this.wwd.getModel().getLayers();

        if (!layers.contains(this.layer))
            layers.add(this.layer);

        if (!this.layer.isEnabled())
            this.layer.setEnabled(true);

        this.shape.armed = true;
    }

    public void disableLayer()
    {
        LayerList layers = this.wwd.getModel().getLayers();
        layers.remove(this.layer);
        this.shape.clear();
    }

    public Sector getSelectedSector()
    {
        return this.shape.hasSelection() ? this.shape.getBoundingSector() : null;
        // TODO: Determine how to handle date-line spanning sectors.
    }

    public void selected(SelectEvent event)
    {
        if (event == null)
        {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        if (event.getTopObject() != null && !(event.getTopObject() == this.shape.image))
        {
            ((Component) this.wwd).setCursor(Cursor.getDefaultCursor());
            this.shape.setHighlight(NONE);
            return;
        }

        if (event.getEventAction().equals(SelectEvent.DRAG_END))
        {
            this.operation = NONE;
            this.previousPosition = null;
        }
        else if (event.getEventAction().equals(SelectEvent.ROLLOVER))
        {
            if (!(this.wwd instanceof Component))
                return;

            if (event.getTopObject() == null || event.getTopPickedObject().isTerrain())
            {
                ((Component) this.wwd).setCursor(Cursor.getDefaultCursor());
                this.shape.setHighlight(NONE);
                return;
            }

            if (!(event.getTopObject() instanceof Movable))
                return;

            int side = this.determineAdjustmentSide((Movable) event.getTopObject(), EDGE_FACTOR);
            this.shape.setHighlight(side);
            Cursor cursor = Cursor.getDefaultCursor();
            switch (side)
            {
                case NONE:
                    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                    break;
                case GeoQuad.NORTH:
                    cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                    break;
                case GeoQuad.SOUTH:
                    cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                    break;
                case GeoQuad.EAST:
                    cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                    break;
                case GeoQuad.WEST:
                    cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                    break;
                case GeoQuad.NORTHWEST:
                    cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                    break;
                case GeoQuad.NORTHEAST:
                    cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                    break;
                case GeoQuad.SOUTHWEST:
                    cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                    break;
                case GeoQuad.SOUTHEAST:
                    cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                    break;
            }
            ((Component) this.wwd).setCursor(cursor);
        }
        else if (event.getEventAction().equals(SelectEvent.LEFT_PRESS))
        {
            this.previousPosition = this.wwd.getCurrentPosition();
        }
        else if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            DragSelectEvent dragEvent = (DragSelectEvent) event;
            Object topObject = dragEvent.getTopObject();
            if (topObject == null)
                return;

            if (!(topObject instanceof Movable))
                return;
            Movable dragObject = (Movable) topObject;

            int side = this.determineAdjustmentSide(dragObject, EDGE_FACTOR);
            if (side == NONE || this.operation == MOVING)
            {
                this.operation = MOVING;
                this.dragWholeShape(dragEvent, dragObject);
            }
            else if (dragObject instanceof SurfaceImage)
            {
                List<LatLon> corners = this.resizeShape(dragObject, side);
                ((SurfaceImage) dragObject).setCorners(corners);
                this.operation = SIZING;
            }

            this.previousPosition = this.wwd.getCurrentPosition();
        }
    }

    private int determineAdjustmentSide(Movable dragObject, double factor)
    {
        if (dragObject instanceof SurfaceImage)
        {
            SurfaceImage image = (SurfaceImage) dragObject;
            Position p = this.wwd.getCurrentPosition();
            if (p == null)
                return NONE;

            GeoQuad gq = new GeoQuad(image.getCorners());
            double sLat = factor * image.getSector().getDeltaLatDegrees();
            double sLon = factor * image.getSector().getDeltaLonDegrees();
            double dd = 0.5 * (sLat + sLon);

            if (gq.distanceToNW(p).degrees < dd)
                return GeoQuad.NORTHWEST;
            if (gq.distanceToNE(p).degrees < dd)
                return GeoQuad.NORTHEAST;
            if (gq.distanceToSW(p).degrees < dd)
                return GeoQuad.SOUTHWEST;
            if (gq.distanceToSE(p).degrees < dd)
                return GeoQuad.SOUTHEAST;
            if (gq.distanceToNorthEdge(p).degrees < dd)
                return GeoQuad.NORTH;
            if (gq.distanceToSouthEdge(p).degrees < dd)
                return GeoQuad.SOUTH;
            if (gq.distanceToEastEdge(p).degrees < dd)
                return GeoQuad.EAST;
            if (gq.distanceToWestEdge(p).degrees < dd)
                return GeoQuad.WEST;
        }

        return NONE;
    }

    private List<LatLon> resizeShape(Movable dragObject, int side)
    {
        if (dragObject instanceof SurfaceImage)
        {
            SurfaceImage image = (SurfaceImage) dragObject;
            List<LatLon> corners = image.getCorners();
            Position p = this.wwd.getCurrentPosition();

            Angle dLat = p.getLatitude().subtract(this.previousPosition.getLatitude());
            Angle dLon = p.getLongitude().subtract(this.previousPosition.getLongitude());
            LatLon delta = new LatLon(dLat, dLon);

            LatLon sw = corners.get(0);
            LatLon se = corners.get(1);
            LatLon ne = corners.get(2);
            LatLon nw = corners.get(3);

            if (side == GeoQuad.NORTH)
            {
                nw = nw.add(delta);
                ne = ne.add(delta);
            }
            else if (side == GeoQuad.SOUTH)
            {
                sw = sw.add(delta);
                se = se.add(delta);
            }
            else if (side == GeoQuad.EAST)
            {
                se = se.add(delta);
                ne = ne.add(delta);
            }
            else if (side == GeoQuad.WEST)
            {
                sw = sw.add(delta);
                nw = nw.add(delta);
            }
            else if (side == GeoQuad.NORTHWEST)
            {
                nw = nw.add(delta);
            }
            else if (side == GeoQuad.NORTHEAST)
            {
                ne = ne.add(delta);
            }
            else if (side == GeoQuad.SOUTHWEST)
            {
                sw = sw.add(delta);
            }
            else if (side == GeoQuad.SOUTHEAST)
            {
                se = se.add(delta);
            }

            return Arrays.asList(sw, se, ne, nw);
        }

        return null;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private static double minAbs(double a, double b, double c, double d)
    {
        double m = a;

        if (abs(b) < abs(m))
            m = b;
        if (abs(c) < abs(m))
            m = c;
        if (abs(d) < abs(m))
            m = d;

        return m;
    }

    private static double abs(double a)
    {
        return a >= 0 ? a : -a;
    }

    private void dragWholeShape(DragSelectEvent dragEvent, Movable dragObject)
    {
        View view = wwd.getView();
        EllipsoidalGlobe globe = (EllipsoidalGlobe) wwd.getModel().getGlobe();

        // Compute ref-point position in screen coordinates.
        Position refPos = dragObject.getReferencePosition();
        if (refPos == null)
            return;

        Vec4 refPoint = globe.computePointFromPosition(refPos);
        Vec4 screenRefPoint = view.project(refPoint);

        // Compute screen-coord delta since last event.
        int dx = dragEvent.getPickPoint().x - dragEvent.getPreviousPickPoint().x;
        int dy = dragEvent.getPickPoint().y - dragEvent.getPreviousPickPoint().y;

        // Find intersection of screen coord ref-point with globe.
        double x = screenRefPoint.x + dx;
        double y = dragEvent.getMouseEvent().getComponent().getSize().height - screenRefPoint.y + dy - 1;
        Line ray = view.computeRayFromScreenPoint(x, y);
        Intersection inters[] = globe.intersect(ray, refPos.getElevation());

        if (inters != null)
        {
            // Intersection with globe. Move reference point to the intersection point.
            Position p = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
            dragObject.moveTo(p);
        }
    }

    public static class RegionShape implements Renderable
    {
        private SurfaceImage image;
        private boolean armed = false;
        private boolean resizeable = false;
        private Position startPosition;
        private Position endPosition;
        private int highlightSide = NONE;
        private Polyline handle;

        public RegionShape(SurfaceImage image)
        {
            this.image = image;

            this.handle = new Polyline();
            this.handle.setFollowTerrain(true);
            this.handle.setPathType(Polyline.LINEAR);
            this.handle.setClosed(false);
            this.handle.setColor(new Color(1f, 0f, 0f, 0.5f));
            this.handle.setLineWidth(3);
        }

        public Sector getBoundingSector()
        {
            return this.image.getSector();
        }

        public boolean hasSelection()
        {
            return startPosition != null && endPosition != null;
        }

        private void clear()
        {
            this.startPosition = this.endPosition = null;
        }

        public void setHighlight(int highlightPlace)
        {
            this.highlightSide = highlightPlace;
        }

        public void render(DrawContext dc)
        {
            if (dc.isPickingMode() && resizeable)
                return;

            if (!resizeable)
            {
                if (startPosition != null && endPosition != null)
                {
                    this.image.render(dc);
                }
            }

            List<LatLon> corners = this.image.getCorners();
            List<LatLon> handlePositions = new ArrayList<LatLon>();

            switch (this.highlightSide)
            {
                case GeoQuad.NORTH:
                {
                    handlePositions.addAll(Arrays.asList(corners.get(2), corners.get(3)));
                    break;
                }
                case GeoQuad.SOUTH:
                {
                    handlePositions.addAll(Arrays.asList(corners.get(0), corners.get(1)));
                    break;
                }
                case GeoQuad.EAST:
                {
                    handlePositions.addAll(Arrays.asList(corners.get(1), corners.get(2)));
                    break;
                }
                case GeoQuad.WEST:
                {
                    handlePositions.addAll(Arrays.asList(corners.get(3), corners.get(0)));
                    break;
                }
                case GeoQuad.NORTHWEST:
                {
                    break;
                }
                case GeoQuad.NORTHEAST:
                {
                    break;
                }
                case GeoQuad.SOUTHWEST:
                {
                    break;
                }
                case GeoQuad.SOUTHEAST:
                {
                    break;
                }
                default:
                {
                    break;
                }

//                if (handlePositions.size() > 1)
            }
            {
                this.handle.setPositions(handlePositions, 0);
                this.handle.render(dc);
            }
            return;

//            PickedObjectList pos = dc.getPickedObjects();
//            PickedObject terrainObject = pos != null ? pos.getTerrainObject() : null;
//
//            if (terrainObject == null)
//                return;
//
//            if (this.startPosition != null)
//            {
//                Position end = terrainObject.getPosition();
//                if (!this.startPosition.equals(end))
//                {
//                    this.endPosition = end;
//                    this.setSector(Sector.boundingSector(this.startPosition, this.endPosition));
//                    super.render(dc);
//                    this.border.setPositions(this.getBoundingSector(), 0);
//                    this.border.render(dc);
//                }
//            }
//            else
//            {
//                this.startPosition = pos.getTerrainObject().getPosition();
//            }
        }
    }
//
//    public static class RegionShape2 extends SurfaceSector
//    {
//        private boolean armed = false;
//        private boolean resizeable = false;
//        private Position startPosition;
//        private Position endPosition;
//        private Polyline border;
//
//        public RegionShape(Sector sector, Color color, Color borderColor, Dimension textureSize)
//        {
//            super(sector, color, borderColor, textureSize);
//            this.setDrawBorder(false);
//            this.border = new Polyline();
//            this.border.setFollowTerrain(true);
//            this.border.setPathType(Polyline.LINEAR);
//            this.border.setClosed(true);
//            this.border.setColor(new Color(1f, 0f, 0f, 0.5f));
//            this.border.setLineWidth(3);
//        }
//
//        public boolean hasSelection()
//        {
//            return startPosition != null && endPosition != null;
//        }
//
//        private void clear()
//        {
//            this.startPosition = this.endPosition = null;
//        }
//
//        @Override
//        public void render(DrawContext dc)
//        {
//            if (dc.isPickingMode() && resizeable)
//                return;
//
//            if (!resizeable)
//            {
//                if (startPosition != null && endPosition != null)
//                {
//                    super.render(dc);
//                    this.border.setPositions(this.getBoundingSector(), 0);
//                    this.border.render(dc);
//                }
//                return;
//            }
//
//            PickedObjectList pos = dc.getPickedObjects();
//            PickedObject terrainObject = pos != null ? pos.getTerrainObject() : null;
//
//            if (terrainObject == null)
//                return;
//
//            if (this.startPosition != null)
//            {
//                Position end = terrainObject.getPosition();
//                if (!this.startPosition.equals(end))
//                {
//                    this.endPosition = end;
//                    this.setSector(Sector.boundingSector(this.startPosition, this.endPosition));
//                    super.render(dc);
//                    this.border.setPositions(this.getBoundingSector(), 0);
//                    this.border.render(dc);
//                }
//            }
//            else
//            {
//                this.startPosition = pos.getTerrainObject().getPosition();
//            }
//        }
//    }
}
