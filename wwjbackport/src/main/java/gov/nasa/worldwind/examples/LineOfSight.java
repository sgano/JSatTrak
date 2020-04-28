/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.RayCastingSupport;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.Earth.USGSTopographicMaps;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.view.orbit.OrbitView;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Using the RayCastingSupport for line of sight calculation.
 * @author Patrick Murris
 * @version $Id: LineOfSight.java 13909 2010-09-30 06:33:58Z pabercrombie $
 */
public class LineOfSight extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private double samplingLength = 30; // Ray casting sample length
        private int centerOffset = 100; // meters above ground for center
        private int pointOffset = 10;   // meters above ground for sampled points
        private Vec4 light = new Vec4(1, 1, -1).normalize3();   // Light direction (from South-East)
        private double ambiant = .4;                            // Minimum lighting (0 - 1)

        private RenderableLayer renderableLayer;
        private SurfaceImage surfaceImage;
        private ScreenAnnotation screenAnnotation;
        private JComboBox radiusCombo;
        private JComboBox samplesCombo;
        private JCheckBox shadingCheck;
        private JButton computeButton;

        public AppFrame()
        {
            super(true, true, false);

            // Add USGS Topo Maps
            insertBeforePlacenames(getWwd(), new USGSTopographicMaps());

            // Add our renderable layer for result display
            this.renderableLayer = new RenderableLayer();
            this.renderableLayer.setName("Line of sight");
            this.renderableLayer.setPickEnabled(false);
            insertBeforePlacenames(getWwd(), this.renderableLayer);

            // Add crosshair layer
            insertBeforePlacenames(getWwd(), new CrosshairLayer());

            // Update layer panel
            this.getLayerPanel().update(getWwd());

            // Add control panel
            this.getLayerPanel().add(makeControlPanel(),  BorderLayout.SOUTH);
        }

        private JPanel makeControlPanel()
        {
            JPanel controlPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Line Of Sight")));

            // Radius combo
            JPanel radiusPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            radiusPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            radiusPanel.add(new JLabel("Max radius:"));
            radiusCombo = new JComboBox(new String[] {"5km", "10km",
                    "20km", "30km", "50km", "100km", "200km"});
            radiusCombo.setSelectedItem("10km");
            radiusPanel.add(radiusCombo);

            // Samples combo
            JPanel samplesPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            samplesPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            samplesPanel.add(new JLabel("Samples:"));
            samplesCombo = new JComboBox(new String[] {"128", "256", "512"});
            samplesCombo.setSelectedItem("128");
            samplesPanel.add(samplesCombo);

            // Shading checkbox
            JPanel shadingPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            shadingPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            shadingPanel.add(new JLabel("Light:"));
            shadingCheck = new JCheckBox("Add shading");
            shadingCheck.setSelected(false);
            shadingPanel.add(shadingCheck);

            // Compute button
            JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            computeButton = new JButton("Compute");
            computeButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    update();
                }
            });
            buttonPanel.add(computeButton);

            // Help text
            JPanel helpPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            helpPanel.add(new JLabel("Place view center on an elevated"));
            helpPanel.add(new JLabel("location and click \"Compute\""));

            // Panel assembly
            controlPanel.add(radiusPanel);
            controlPanel.add(samplesPanel);
            controlPanel.add(shadingPanel);
            controlPanel.add(buttonPanel);
            controlPanel.add(helpPanel);

            return controlPanel;
        }

        // Update line of sight computation
        private void update()
        {
            new Thread(new Runnable() {
                public void run()
                {
                    computeLineOfSight();
                }
            }, "LOS thread").start();
        }

        private void computeLineOfSight()
        {
            computeButton.setEnabled(false);
            computeButton.setText("Computing...");

            try
            {
                Globe globe = getWwd().getModel().getGlobe();
                OrbitView view = (OrbitView)getWwd().getView();
                Position centerPosition = view.getCenterPosition();

                // Compute sector
                String radiusString = ((String)radiusCombo.getSelectedItem());
                double radius = 1000 * Double.parseDouble(radiusString.substring(0, radiusString.length() - 2));
                double deltaLatRadians = radius / globe.getEquatorialRadius();
                double deltaLonRadians = deltaLatRadians / Math.cos(centerPosition.getLatitude().radians);
                Sector sector = new Sector(centerPosition.getLatitude().subtractRadians(deltaLatRadians),
                        centerPosition.getLatitude().addRadians(deltaLatRadians),
                        centerPosition.getLongitude().subtractRadians(deltaLonRadians),
                        centerPosition.getLongitude().addRadians(deltaLonRadians));

                // Compute center point
                double centerElevation = globe.getElevation(centerPosition.getLatitude(),
                        centerPosition.getLongitude());
                Vec4 center = globe.computePointFromPosition(
                        new Position(centerPosition, centerElevation + centerOffset));

                // Compute image
                float hueScaleFactor = .7f;
                int samples = Integer.parseInt((String)samplesCombo.getSelectedItem());
                BufferedImage image = new BufferedImage(samples, samples, BufferedImage.TYPE_4BYTE_ABGR);
                double latStepRadians = sector.getDeltaLatRadians() / image.getHeight();
                double lonStepRadians = sector.getDeltaLonRadians() / image.getWidth();
                for (int x = 0; x < image.getWidth(); x++)
                {
                    Angle lon = sector.getMinLongitude().addRadians(lonStepRadians * x + lonStepRadians / 2);
                    for (int y = 0; y < image.getHeight(); y++)
                    {
                        Angle lat = sector.getMaxLatitude().subtractRadians(latStepRadians * y + latStepRadians / 2);
                        double el = globe.getElevation(lat, lon);
                        // Test line of sight from point to center
                        Vec4 point = globe.computePointFromPosition(lat, lon, el + pointOffset);
                        if (RayCastingSupport.intersectSegmentWithTerrain(
                                globe, point, center, samplingLength, samplingLength) == null)
                        {
                            // Center visible from point: set pixel color and shade
                            float hue = (float)Math.min(point.distanceTo3(center) / radius, 1) * hueScaleFactor;
                            float shade = shadingCheck.isSelected() ?
                                    (float)computeShading(globe, lat, lon, light, ambiant) : 0f;
                            image.setRGB(x, y, Color.HSBtoRGB(hue, 1f, 1f - shade));
                        }
                        else if (shadingCheck.isSelected())
                        {
                            // Center not visible: apply shading nonetheless if selected
                            float shade = (float)computeShading(globe, lat, lon, light, ambiant);
                            image.setRGB(x, y, new Color(0f, 0f, 0f, shade).getRGB());
                        }
                    }
                }
                // Blur image
                PatternFactory.blur(PatternFactory.blur(PatternFactory.blur(PatternFactory.blur(image))));

                // Update surface image
                if (this.surfaceImage != null)
                    this.renderableLayer.removeRenderable(this.surfaceImage);
                this.surfaceImage = new SurfaceImage(image, sector);
                this.surfaceImage.setOpacity(.5);
                this.renderableLayer.addRenderable(this.surfaceImage);

                // Compute distance scale image
                BufferedImage scaleImage = new BufferedImage(64, 256, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics g2 = scaleImage.getGraphics();
                int divisions = 10;
                int labelStep = scaleImage.getHeight() / divisions;
                for (int y = 0; y < scaleImage.getHeight(); y++)
                {
                    int x1 = scaleImage.getWidth() / 5;
                    if (y % labelStep == 0 && y != 0)
                    {
                        double d = radius / divisions * y / labelStep / 1000;
                        String label = Double.toString(d) + "km";
                        g2.setColor(Color.BLACK);
                        g2.drawString(label, x1 + 6, y + 6);
                        g2.setColor(Color.WHITE);
                        g2.drawLine(x1, y, x1 + 4 , y);
                        g2.drawString(label, x1 + 5, y + 5);
                    }
                    float hue = (float)y / (scaleImage.getHeight() - 1) * hueScaleFactor;
                    g2.setColor(Color.getHSBColor(hue, 1f, 1f));
                    g2.drawLine(0, y, x1, y);
                }

                // Update distance scale screen annotation
                if (this.screenAnnotation != null)
                    this.renderableLayer.removeRenderable(this.screenAnnotation);
                this.screenAnnotation = new ScreenAnnotation("", new Point(20, 20));
                this.screenAnnotation.getAttributes().setImageSource(scaleImage);
                this.screenAnnotation.getAttributes().setSize(
                        new Dimension(scaleImage.getWidth(), scaleImage.getHeight()));
                this.screenAnnotation.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
                this.screenAnnotation.getAttributes().setDrawOffset(new Point(scaleImage.getWidth() / 2, 0));
                this.screenAnnotation.getAttributes().setBorderWidth(0);
                this.screenAnnotation.getAttributes().setCornerRadius(0);
                this.screenAnnotation.getAttributes().setBackgroundColor(new Color(0f, 0f, 0f, 0f));
                this.renderableLayer.addRenderable(this.screenAnnotation);

                // Redraw
                this.getWwd().redraw();
            }
            finally
            {
                computeButton.setEnabled(true);
                computeButton.setText("Compute");
            }
        }

        /**
         * Compute shadow intensity at a globe position.
         * @param globe the <code>Globe</code>.
         * @param lat the location latitude.
         * @param lon the location longitude.
         * @param light the light direction vector. Expected to be normalized.
         * @param ambiant the minimum ambiant light level (0..1).
         * @return  the shadow intensity for the location. No shadow = 0, totaly obscured = 1.
         */
        private static double computeShading(Globe globe, Angle lat, Angle lon, Vec4 light, double ambiant)
        {
            double thirtyMetersRadians = 30 / globe.getEquatorialRadius();
            Vec4 p0 = globe.computePointFromPosition(lat, lon, 0);
            Vec4 px = globe.computePointFromPosition(lat, Angle.fromRadians(lon.radians - thirtyMetersRadians), 0);
            Vec4 py = globe.computePointFromPosition(Angle.fromRadians(lat.radians + thirtyMetersRadians), lon, 0);

            double el0 = globe.getElevation(lat, lon);
            double elx = globe.getElevation(lat, Angle.fromRadians(lon.radians - thirtyMetersRadians));
            double ely = globe.getElevation(Angle.fromRadians(lat.radians + thirtyMetersRadians), lon);

            Vec4 vx = new Vec4(p0.distanceTo3(px), 0, elx - el0).normalize3();
            Vec4 vy = new Vec4(0, p0.distanceTo3(py), ely - el0).normalize3();
            Vec4 normal = vx.cross3(vy).normalize3();

            return 1d - Math.max(-light.dot3(normal), ambiant);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Line Of Sight Calculation", AppFrame.class);
    }
}
