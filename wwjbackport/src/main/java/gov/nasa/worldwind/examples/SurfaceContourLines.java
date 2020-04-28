/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Contour lines visualization.
 *
 * @author Patrick Murris
 * @version $Id: SurfaceContourLines.java 12530 2009-08-29 17:55:54Z jterhorst $
 */
public class SurfaceContourLines extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private JSlider elevationSlider;
        private JLabel elevationLabel;
        private ContourLine contourLine;
        private Sector sector;

        public AppFrame()
        {
            super(true, true, false);

            // Add a renderable layer
            RenderableLayer renderableLayer = new RenderableLayer();
            renderableLayer.setName("Surface Contour Lines");
            renderableLayer.setPickEnabled(false);
            insertBeforePlacenames(getWwd(), renderableLayer);

            // Add a global moving contour line to our layer
            this.contourLine = new SurfaceContourLine();
            renderableLayer.addRenderable(this.contourLine);

            // Add a local contour line set in southern Alps
            final ShapeAttributes defaultAttributes = new BasicShapeAttributes();
            defaultAttributes.setDrawInterior(false);
            defaultAttributes.setOutlineMaterial(Material.ORANGE);
            defaultAttributes.setOutlineWidth(1);
            final ShapeAttributes fiveHundredAttributes = new BasicShapeAttributes();
            fiveHundredAttributes.setDrawInterior(false);
            fiveHundredAttributes.setOutlineMaterial(Material.YELLOW);
            fiveHundredAttributes.setOutlineWidth(2);
            final ShapeAttributes oneThousandAttributes = new BasicShapeAttributes();
            oneThousandAttributes.setDrawInterior(false);
            oneThousandAttributes.setOutlineMaterial(Material.RED);
            oneThousandAttributes.setOutlineWidth(2);
            int numLines = 31;
            double[] elevations = new double[numLines];
            for (int i = 0; i < numLines; i++)
                elevations[i] = i * 100;
            sector = Sector.fromDegrees(44.1, 44.2, 6.9, 7);
            SurfaceContourLineSet cls = new SurfaceContourLineSet(elevations, sector)
            {
                protected ShapeAttributes getAttributes(double elevation)
                {
                    if (elevation % 1000 == 0)
                        return oneThousandAttributes;
                    if (elevation % 500 == 0)
                        return fiveHundredAttributes;
                    return defaultAttributes;
                }
            };
            renderableLayer.addRenderable(cls);

            // Update layer panel
            this.getLayerPanel().update(getWwd());

            // Add control panel
            this.getLayerPanel().add(makeControlPanel(),  BorderLayout.SOUTH);
        }

        private JPanel makeControlPanel()
        {
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Contour Lines")));

            // Elevation slider
            JPanel elevationPanel = new JPanel(new GridLayout(2, 1, 0, 0));
            elevationPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            elevationLabel = new JLabel("Elevation:");
            elevationPanel.add(elevationLabel);
            elevationSlider = new JSlider(0, 500, 0);
            elevationSlider.setMajorTickSpacing(100);
            elevationSlider.setMinorTickSpacing(50);
            elevationSlider.setPaintTicks(true);
            elevationSlider.setPaintLabels(true);
            elevationSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    double elevation = elevationSlider.getValue() * 10;
                    contourLine.setElevation(elevation);
                    elevationLabel.setText("Elevation: (" + elevation + "m)");
                    getWwd().redraw();
                }
            });
            elevationPanel.add(elevationSlider);

            // Help text
            JPanel helpPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            helpPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            helpPanel.add(new JLabel("Adjust line elevation with slider"));

            // Zoom onto local set button
            JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 0));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            JButton zoomButton = new JButton("Zoom on local set");
            zoomButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    Position targetPos = new Position(sector.getCentroid(), 0);
                    BasicOrbitView view = (BasicOrbitView) getWwd().getView();
                    view.addPanToAnimator(targetPos,
                            Angle.ZERO, Angle.ZERO, 30e3);
                }
            });
            buttonPanel.add(zoomButton);

            // Panel assembly
            controlPanel.add(elevationPanel);
            controlPanel.add(helpPanel);
            controlPanel.add(buttonPanel);

            return controlPanel;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Surface Contour Lines", AppFrame.class);
    }
}
