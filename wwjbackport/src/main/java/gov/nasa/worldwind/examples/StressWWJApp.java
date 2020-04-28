/* Copyright (C) 2001, 2007 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.layers.MarkerLayer;

import java.awt.*;

/**
 * App for stress testing WWJ functions.  Just start it and let it run.
 *
 * @author jparsons
 * @version $Id$
 */
public class StressWWJApp extends ApplicationTemplate
{

    private static class AppFrame extends ApplicationTemplate.AppFrame
    {

        public AppFrame()
        {
            super(true, true, true);

            final StressWWJIterator wwjIterator = new StressWWJIterator(this);

            //add marker & airspace layer
            MarkerLayer layer = new MarkerLayer();
            layer.setOverrideMarkerElevation(true);
            layer.setKeepSeparated(false);
            layer.setElevation(500d);
            layer.setMarkers(wwjIterator.getMarkers());
            insertBeforePlacenames(this.getWwd(), layer);
            insertBeforePlacenames(this.getWwd(), wwjIterator.getAirspaceLayer());

            this.getWwd().addRenderingListener(new RenderingListener()
                {
                    public void stageChanged(RenderingEvent event)
                    {
                        if (event.getSource() instanceof WorldWindow)
                        {
                            EventQueue.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    wwjIterator.updateStressStatsPanel();
                                }
                            });
                        }
                    }
                });

            this.getLayerPanel().add(wwjIterator.makeControlPanel(), BorderLayout.SOUTH);
            this.getLayerPanel().update(this.getWwd());
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            wwjIterator.runWWJIterator();
        }
    }


    public static void main(String[] args)
    {
		ApplicationTemplate.start("World Wind Stress Test", AppFrame.class);
    }
}
