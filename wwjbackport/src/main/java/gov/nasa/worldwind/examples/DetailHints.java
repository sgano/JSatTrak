/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.terrain.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

/**
 * Illustrates use of the detail hints for elevation models and tiled image layers. Sets the hint on the current
 * elevation model and the Blue Marble image layer.
 *
 * @author Patrick Murris
 * @version $Id: DetailHints.java 14006 2010-10-22 04:08:19Z tgaskins $
 */
public class DetailHints extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Add detail hint slider panel
            this.getLayerPanel().add(makeDetailHintControlPanel(), BorderLayout.SOUTH);
        }

        private JPanel makeDetailHintControlPanel()
        {
            JPanel controlPanel = new JPanel(new BorderLayout(0, 10));
            controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Detail Hint")));

            JPanel elevationSliderPanel = new JPanel(new BorderLayout(0, 5));
            {
                int MIN = -10;
                int MAX = 10;
                int cur = (int) (
                    this.getWwd().getModel().getGlobe().getElevationModel().getDetailHint(Sector.FULL_SPHERE) * 10);
                cur = cur < MIN ? MIN : (cur > MAX ? MAX : cur);
                JSlider slider = new JSlider(MIN, MAX, cur);
                slider.setMajorTickSpacing(10);
                slider.setMinorTickSpacing(1);
                slider.setPaintTicks(true);
                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
                labelTable.put(-10, new JLabel("-1.0"));
                labelTable.put(0, new JLabel("0.0"));
                labelTable.put(10, new JLabel("1.0"));
                slider.setLabelTable(labelTable);
                slider.setPaintLabels(true);
                slider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        double hint = ((JSlider) e.getSource()).getValue() / 10d;
                        setElevationDetailHint(hint);
                        getWwd().redraw();
                    }
                });
                elevationSliderPanel.add(slider, BorderLayout.SOUTH);
            }

            JPanel imageSliderPanel = new JPanel(new BorderLayout(0, 5));
            {
                int MIN = -10;
                int MAX = 10;
                int cur = (int) (getLayer().getDetailHint() * 10);
                cur = cur < MIN ? MIN : (cur > MAX ? MAX : cur);
                JSlider slider = new JSlider(MIN, MAX, cur);
                slider.setMajorTickSpacing(10);
                slider.setMinorTickSpacing(1);
                slider.setPaintTicks(true);
                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
                labelTable.put(-10, new JLabel("-1.0"));
                labelTable.put(0, new JLabel("0.0"));
                labelTable.put(10, new JLabel("1.0"));
                slider.setLabelTable(labelTable);
                slider.setPaintLabels(true);
                slider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        double hint = ((JSlider) e.getSource()).getValue() / 10d;
                        setImageDetailHint(hint);
                        getWwd().redraw();
                    }
                });
                imageSliderPanel.add(slider, BorderLayout.SOUTH);
            }

            JPanel elevationsWireframeCheckBoxPanel = new JPanel(new BorderLayout(0, 5));
            {

                JCheckBox checkBox = new JCheckBox("Show wireframe terrain");
                checkBox.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean selected = ((JCheckBox) e.getSource()).isSelected();
                        getWwd().getSceneController().getModel().setShowWireframeInterior(selected);
                        getWwd().redraw();
                    }
                });
                elevationsWireframeCheckBoxPanel.add(checkBox, BorderLayout.SOUTH);
            }

            JPanel imageBorderCheckBoxPanel = new JPanel(new BorderLayout(0, 5));
            {

                JCheckBox checkBox = new JCheckBox("Show image tiles");
                checkBox.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean selected = ((JCheckBox) e.getSource()).isSelected();
                        TiledImageLayer layer = getLayer();
                        layer.setDrawTileBoundaries(true);
                        layer.setDrawTileIDs(true);
                        getWwd().redraw();
                    }
                });
                imageBorderCheckBoxPanel.add(checkBox, BorderLayout.SOUTH);
            }

            JPanel checkBoxPanel = new JPanel(new GridLayout(2, 0));
            checkBoxPanel.add(elevationsWireframeCheckBoxPanel);
            checkBoxPanel.add(imageBorderCheckBoxPanel);

            JPanel sliderPanel = new JPanel(new GridLayout(2, 0));
            sliderPanel.add(elevationSliderPanel);
            sliderPanel.add(imageSliderPanel);

            controlPanel.add(checkBoxPanel, BorderLayout.NORTH);
            controlPanel.add(sliderPanel, BorderLayout.SOUTH);
            return controlPanel;
        }

        protected TiledImageLayer getLayer()
        {
            for (Layer layer : getWwd().getModel().getLayers())
            {
//                if (layer.getName().contains("i-cubed"))
                if (layer.getName().contains("Blue Marble (WMS)"))
                {
                    return (TiledImageLayer) layer;
                }
            }

            return null;
        }

        private void setElevationDetailHint(double hint)
        {
            ElevationModel em = getWwd().getModel().getGlobe().getElevationModel();
            if (em instanceof BasicElevationModel)
            {
                ((BasicElevationModel) em).setDetailHint(hint);
                System.out.println("Elevation detail hint set to " + hint);
            }
            else if (em instanceof CompoundElevationModel)
            {
                for (ElevationModel m : ((CompoundElevationModel) em).getElevationModels())
                {
                    if (m instanceof BasicElevationModel)
                    {
                        ((BasicElevationModel) m).setDetailHint(hint);
                        System.out.println("Detail hint set to " + hint);
                    }
                }
            }
        }

        private void setImageDetailHint(double hint)
        {
            getLayer().setDetailHint(hint);
            System.out.println("Image detail hint set to " + hint);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Terrain Level Of Detail", AppFrame.class);
    }
}
