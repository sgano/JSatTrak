/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: FilterByRegion.java 6036 2008-08-18 15:58:14Z tgaskins $
 */
public class FilterByRegion extends ApplicationTemplate
{
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame() throws IOException, ParserConfigurationException, SAXException
        {
            super(true, true, false);

            final MyMarkerLayer layer = new MyMarkerLayer();
            layer.setKeepSeparated(false);
            layer.setPickEnabled(false);
            insertBeforePlacenames(this.getWwd(), layer);
            this.getLayerPanel().update(this.getWwd());

            this.getWwd().addPositionListener(new PositionListener()
            {
                public void moved(PositionEvent event)
                {
                    layer.setCursorLocation(event.getPosition());
                }
            });
        }
    }

    private static class MyMarkerLayer extends MarkerLayer
    {
        private static final double[] REGION_SIZES = new double[] {5, 1};
        private static final long TIME_LIMIT = 10; // ms

        private Position position;
        private Iterable<Marker> markers;

        public MyMarkerLayer()
        {
            this.setOverrideMarkerElevation(true);
        }

        public void setCursorLocation(Position position)
        {
            this.position = position;
        }

        protected void draw(DrawContext dc, Point pickPoint)
        {
            if (this.position == null)
                return;

            Vec4 cursorPoint = dc.getGlobe().computePointFromPosition(this.position);

            // Refresh the visibility tree only during the pick pass, or the display pass if picking is disabled
            if (!this.isPickEnabled() || dc.isPickingMode() || this.markers == null)
                this.markers = this.sortPositions(dc, cursorPoint);

            this.setMarkers(this.markers);
            super.draw(dc, pickPoint);
        }

        private Iterable<Marker> sortPositions(DrawContext dc, Vec4 cursorPoint)
        {
            BasicMarkerAttributes attrs = new BasicMarkerAttributes();
            ArrayList<Marker> markers = new ArrayList<Marker>();
            for (Sector s : dc.getVisibleSectors(REGION_SIZES, TIME_LIMIT, Sector.FULL_SPHERE))
            {
                LatLon c = s.getCentroid();
                Vec4 center = dc.getGlobe().computePointFromPosition(c.getLatitude(), c.getLongitude(), 0);
                if (center.distanceTo3(cursorPoint) <= 1000000)
                    markers.add(new BasicMarker(new Position(c.getLatitude(), c.getLongitude(), 0), attrs));
            }

            return markers;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Filtering by Region", AppFrame.class);
    }
}
