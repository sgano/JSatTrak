package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.*;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: jym Date: Aug 31, 2009 Time: 3:19:16 PM To change this template use File | Settings |
 * File Templates.
 */
public class MarkerBug extends ApplicationTemplate
{
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private static final MarkerAttributes markerAttrs =
            new BasicMarkerAttributes(Material.WHITE, BasicMarkerShape.SPHERE, 1d, 10, 5);
        WorldWindow wwd;
        public AppFrame()
        {
            super(true, true, false);
            double lat = 40.0;
            double minLon = -180.0;
            double maxLon = 180.0;
            double lonDelta = 1.0;
            ArrayList<Marker> markers = new ArrayList<Marker>();
            for (double lon = minLon; lon <= maxLon; lon += lonDelta)
            {
                markerAttrs.setMaxMarkerSize(200000);
                Marker marker = new BasicMarker(Position.fromDegrees(lat, lon, 0),
                    markerAttrs);
                marker.setPosition(Position.fromDegrees(lat, lon, 0));
                marker.setHeading(Angle.fromDegrees(lat * 5));
                markers.add(marker);
            }

            final MarkerLayer layer = new MarkerLayer();
            layer.setOverrideMarkerElevation(true);
            //layer.setKeepSeparated(false);
            layer.setElevation(1000d);
            layer.setMarkers(markers);
            insertBeforePlacenames(this.getWwd(), layer);
            this.getLayerPanel().update(this.getWwd());
        }


    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Marker Bug", AppFrame.class);
    }
}
