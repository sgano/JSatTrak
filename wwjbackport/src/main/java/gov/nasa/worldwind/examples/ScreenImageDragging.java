/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.ScreenImage;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;

/**
 * @author tag
 * @version $Id: ScreenImageDragging.java 5856 2008-08-06 19:15:28Z tgaskins $
 */
public class ScreenImageDragging extends ApplicationTemplate
{
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame() throws IOException, ParserConfigurationException, SAXException
        {
            super(true, true, false);

            // Create a screen image and containing layer for the image/icon
            final ScreenImage screenImage = new ScreenImage();
            screenImage.setImageSource(ImageIO.read(new File("src/images/32x32-icon-nasa.png")));

            RenderableLayer layer = new RenderableLayer();
            layer.setName("Screen Image");
            layer.addRenderable(screenImage);

            this.getWwd().getModel().getLayers().add(layer);
            this.getLayerPanel().update(this.getWwd());

            // Tell the input handler to pass mouse events here
            this.getWwd().getInputHandler().addMouseMotionListener(new MouseMotionAdapter()
            {
                public void mouseDragged(MouseEvent event)
                {
                    // Update the layer's image location
                    screenImage.setScreenLocation(event.getPoint());
                    event.consume(); // tell the input handler that we've handled the event
                }
            });
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Screen Image Dragging", AppFrame.class);
    }
}
