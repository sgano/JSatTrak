/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.awt.*;

/**
 * Example of Balloon usage.
 *
 * @author pabercrombie
 * @version $Id: Balloons.java 14159 2010-11-29 21:50:05Z pabercrombie $
 */
public class Balloons extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            /// Create screen attached balloon
            BasicBalloonAttributes attributes = new BasicBalloonAttributes();
            attributes.setLeader(FrameFactory.LEADER_NONE);

            Balloon balloon = new AnnotationBalloon("Balloon attached to screen", new Point(100, 200));
            balloon.setAttributes(attributes);

            layer.addRenderable(balloon);

            /// Create globe attached balloon
            attributes = new BasicBalloonAttributes();
            BasicBalloonAttributes highlightAttributes = new BasicBalloonAttributes();

            highlightAttributes.setTextColor(Color.RED);

            balloon = new AnnotationBalloon("<b>Lake Tahoe</b><br/>Balloon attached to globe",
                Position.fromDegrees(39.108, -120.0528));
            balloon.setAttributes(attributes);
            balloon.setHighlightAttributes(highlightAttributes);
            layer.addRenderable(balloon);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            // Update layer panel
            this.getLayerPanel().update(this.getWwd());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Balloons", AppFrame.class);
    }
}
