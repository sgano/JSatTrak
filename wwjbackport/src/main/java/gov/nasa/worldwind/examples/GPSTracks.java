/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.formats.gpx.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: GPSTracks.java 7671 2008-11-17 00:18:14Z tgaskins $
 */
public class GPSTracks extends ApplicationTemplate
{
    private static final String TRACK_FILE = "demodata/tuolumne.gpx";

    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            MarkerLayer layer = this.buildTracksLayer();
            insertBeforeCompass(this.getWwd(), layer);
            this.getLayerPanel().update(this.getWwd());

            this.getWwd().addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (event.getTopObject() != null)
                    {
                        if (event.getTopPickedObject().getParentLayer() instanceof MarkerLayer)
                        {
                            PickedObject po = event.getTopPickedObject();
                            System.out.printf("Track position %s, %s, size = %f\n", po.getValue(AVKey.PICKED_OBJECT_ID).toString(),
                                po.getPosition(), (Double) po.getValue(AVKey.PICKED_OBJECT_SIZE));
                        }
                    }
                }
            });
        }

        private MarkerLayer buildTracksLayer()
        {
            try
            {
                GpxReader reader = new GpxReader();
                reader.readFile(TRACK_FILE);
                Iterator<Position> positions = reader.getTrackPositionIterator();

                BasicMarkerAttributes attrs =
                    new BasicMarkerAttributes(Material.WHITE, BasicMarkerShape.SPHERE, 1d);

                ArrayList<Marker> markers = new ArrayList<Marker>();
                while (positions.hasNext())
                {
                    markers.add(new BasicMarker(positions.next(), attrs));
                }

                MarkerLayer layer = new MarkerLayer(markers);
                layer.setOverrideMarkerElevation(true);
                layer.setElevation(0);
                layer.setEnablePickSizeReturn(true);

                return layer;
            }
            catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Tracks", AppFrame.class);
    }
}
