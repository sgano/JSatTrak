/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceQuad;

import javax.swing.*;
import java.awt.event.*;

/**
 * Illustrates rotating a sector from standard position. A sector is created, it's width and height computed and a
 * SurfaceQuad created from the sector's centroid and the computed width and height. The SurfaceQuad's heading is then
 * set to the desired rotation angle.
 *
 * @author tag
 * @version $Id: RotatedSector.java 13588 2010-07-27 16:22:35Z dcollins $
 */
public class RotatedSector extends ApplicationTemplate
{
    private static final Sector sector = Sector.fromDegrees(45, 47, -123, -122);

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                // Create the Quad from a Sector
                Globe globe = this.getWwd().getModel().getGlobe();
                double radius = globe.getRadiusAt(sector.getCentroid());
                double quadWidth = sector.getDeltaLonRadians() * radius;
                double quadHeight = sector.getDeltaLatRadians() * radius;
                final SurfaceQuad quad = new SurfaceQuad(sector.getCentroid(), quadWidth, quadHeight, Angle.ZERO);

                // Create the layer to hold it
                final RenderableLayer layer = new RenderableLayer();
                layer.setName("Rotating Sector");
                layer.addRenderable(quad);

                // Add the layer to the model and update the ApplicationTemplate's layer manager
                insertBeforeCompass(this.getWwd(), layer);
                this.getLayerPanel().update(this.getWwd());

                // Rotate the quad continuously
                Timer timer = new Timer(50, new ActionListener()
                {
                    public void actionPerformed(ActionEvent actionEvent)
                    {
                        // Increment the current heading if the layer is visible
                        if (layer.isEnabled())
                        {
                            quad.setHeading(Angle.fromDegrees((quad.getHeading().getDegrees() + 1) % 360));
                            getWwd().redraw();
                        }
                    }
                });
                timer.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Rotated Sector", AppFrame.class);
    }
}
