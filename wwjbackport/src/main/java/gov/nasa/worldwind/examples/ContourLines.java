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
import gov.nasa.worldwind.view.orbit.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Contour lines visualization.
 *
 * @author Patrick Murris
 * @version $Id: ContourLines.java 12530 2009-08-29 17:55:54Z jterhorst $
 */
public class ContourLines extends ApplicationTemplate
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
            renderableLayer.setName("Contour Lines");
            renderableLayer.setPickEnabled(false);
            insertBeforePlacenames(getWwd(), renderableLayer);

            // Add a global moving contour line to our layer
            this.contourLine = new ContourLine();
            renderableLayer.addRenderable(this.contourLine);

            // Haute Tinée contour lines - test local sector bounded line set
//            sector = Sector.fromDegrees(44.16, 44.30, 6.82, 7.09);
//            for (int elevation = 0; elevation <= 3000; elevation += 100)
//            {
//                ContourLine cl = new ContourLine(elevation, sector);
//                cl.setColor(new Color(.2f, .2f, .8f));
//                renderableLayer.addRenderable(cl);
//                if (elevation % 1000 == 0)
//                {
//                    cl.setLineWidth(2);
//                    cl.setColor(new Color(0f, .1f, .6f));
//                }
//                if (elevation % 500 == 0)
//                    cl.setLineWidth(2);
//            }

            // Haute Tinée contour lines - test local polygon bounded line set
            ArrayList<LatLon> positions = new ArrayList<LatLon>();
            positions.add(LatLon.fromDegrees(44.16, 6.82));
            positions.add(LatLon.fromDegrees(44.16, 7.09));
            positions.add(LatLon.fromDegrees(44.30, 6.95));
            positions.add(LatLon.fromDegrees(44.16, 6.82));
            sector = Sector.boundingSector(positions);
            for (int elevation = 0; elevation <= 3000; elevation += 100)
            {
                ContourLinePolygon cl = new ContourLinePolygon(elevation, positions);
                cl.setColor(new Color(.2f, .2f, .8f));
                renderableLayer.addRenderable(cl);
                if (elevation % 1000 == 0)
                {
                    cl.setLineWidth(2);
                    cl.setColor(new Color(0f, .1f, .6f));
                }
                if (elevation % 500 == 0)
                    cl.setLineWidth(2);
            }

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
        ApplicationTemplate.start("World Wind Contour Lines Visualization", AppFrame.class);
    }
}
