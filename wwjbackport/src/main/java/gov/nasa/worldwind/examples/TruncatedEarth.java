/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

import java.awt.*;
import java.awt.image.*;

/**
 * Elevation model override example to produce a truncated globe.
 *
 * @author Patrick Murris
 * @version $Id: TruncatedEarth.java 12579 2009-09-12 01:07:05Z tgaskins $
 */
public class TruncatedEarth extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        /**
         * The BasicOrbitView does not allow setting of the farClipDistance, so we derive from it here, and implement a
         * settable farClipDistance.
         */
        public class TruncatedEarthView extends BasicOrbitView
        {
            public void setFarClipDistance(double distance)
            {
                this.farClipDistance = distance;
            }

            /**
             * Clip distance is set by the setFarClipDistance method, so override the method(called by
             * BasicOrbitView.apply) that calculates it automatically, so it does not get reset.
             */
            protected double computeFarClipDistance()
            {
                return farClipDistance;
            }
        }

        public AppFrame()
        {
            super(true, true, false);

            // Force the view to be a FlyView
            TruncatedEarthView view = new TruncatedEarthView();
            getWwd().setView(view);

            Model model = getWwd().getModel();
            Globe globe = model.getGlobe();
            // Max out orbit view far clipping distance to keep the whole globe visible
            view.setFarClipDistance(globe.getDiameter() * 3);

            // Use our truncated elevation model
            Sector sector = Sector.fromDegrees(0, 90, -100, -20);
            double elevation = -4e6;
            globe.setElevationModel(new TruncatedEarthElevationModel(sector, elevation));

            // Add truncated sector surface image layer
            RenderableLayer sectionLayer = new RenderableLayer();
            sectionLayer.setPickEnabled(false);
            sectionLayer.setName("Earth Section");
            BufferedImage sectionImage = PatternFactory.createPattern(PatternFactory.GRADIENT_HLINEAR,
                1f, Color.RED.brighter().brighter(), Color.RED.darker().darker());
            BufferedImage coreImage = PatternFactory.createPattern(PatternFactory.GRADIENT_HLINEAR,
                1f, Color.ORANGE.brighter().brighter(), Color.ORANGE.darker().darker());
            // Extend image one degree west and east to properly cover the section edge
            sectionLayer.addRenderable(new SurfaceImage(sectionImage, Sector.fromDegrees(
                Math.max(sector.getMinLatitude().degrees - 2, -90), Math.min(sector.getMaxLatitude().degrees + 2, 90),
                sector.getMinLongitude().degrees - 1, sector.getMaxLongitude().degrees + 1)));
            // Shrink image sector to paint core only
            sectionLayer.addRenderable(new SurfaceImage(coreImage, Sector.fromDegrees(
                sector.getMinLatitude().degrees + 1, sector.getMaxLatitude().degrees - 1,
                sector.getMinLongitude().degrees + 2, sector.getMaxLongitude().degrees - 2)));

            insertBeforePlacenames(getWwd(), sectionLayer);

            // Turn off sky gradient
            for (Layer layer : model.getLayers())
            {
                if (layer instanceof SkyGradientLayer)
                    layer.setEnabled(false);
            }

            // Update layer panel
            getLayerPanel().update(getWwd());
        }

        private class TruncatedEarthElevationModel extends CompoundElevationModel
        {
            private Sector truncatedSector;
            private double truncatedElevation;

            public TruncatedEarthElevationModel(Sector truncatedSector, Double truncatedElevation)
            {
                this.addElevationModel((ElevationModel) BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY,
                    "config/Earth/LegacyEarthElevationModel.xml"));
                this.truncatedSector = truncatedSector;
                this.truncatedElevation = truncatedElevation;
            }

            public double getElevation(Angle latitude, Angle longitude)
            {
                if (latitude == null || longitude == null)
                {
                    String message = Logging.getMessage("nullValue.LatLonIsNull");
                    Logging.logger().severe(message);
                    throw new IllegalArgumentException(message);
                }

                if (this.truncatedSector.contains(latitude, longitude))
                    return this.truncatedElevation;

                return super.getElevation(latitude, longitude);
            }

            public double getElevations(Sector sector, java.util.List<? extends LatLon> latlons,
                double targetResolution, double[] buffer)
            {
                double resolutionAchieved = super.getElevations(sector, latlons, targetResolution, buffer);

                int i = 0;
                for (LatLon ll : latlons)
                {
                    if (this.truncatedSector.contains(ll))
                        buffer[i] = this.truncatedElevation;
                    i++;
                }

                return resolutionAchieved;
            }

            public double[] getExtremeElevations(Sector sector)
            {
                double[] elevations = super.getExtremeElevations(sector);
                if (this.truncatedSector.intersects(sector))
                    elevations[0] = this.truncatedElevation;
                return elevations;
            }
        }

//        private BufferedImage createSectionImage(Dimension size)
//        {
//            BufferedImage image = new BufferedImage(size.width,  size.height, BufferedImage.TYPE_4BYTE_ABGR);
//            Graphics2D g2 = image.createGraphics();
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            // Background gradient
//            g2.setPaint(new GradientPaint((float)size.width / 2, 0f, Color.ORANGE,
//                    (float)size.height / 2, (float)size.height - 1, Color.RED));
//            g2.fillRect(0, 0, size.width, size.height);
//            // Some layers
//            g2.setPaint(Color.BLACK);
//            g2.fillRect(0, (int)((float)size.height * .9), size.width, 2);
//            g2.fillRect(0, (int)((float)size.height * .6), size.width, 4);
//            g2.fillRect(0, (int)((float)size.height * .4), size.width, 1);
//            return image;
//        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Truncated Earth", AppFrame.class);
    }
}
