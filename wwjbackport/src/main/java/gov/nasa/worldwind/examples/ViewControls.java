/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Shows the ViewControlsLayer and how to use picking on the compass to handle the view orientation.
 *
 * @author Patrick Murris
 * @version $Id: ViewControls.java 13434 2010-06-10 18:49:34Z tgaskins $
 * @see gov.nasa.worldwind.layers.ViewControlsLayer
 * @see gov.nasa.worldwind.layers.ViewControlsSelectListener
 * @see gov.nasa.worldwind.layers.CompassLayer
 */
public class ViewControls extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        ViewControlsLayer viewControlsLayer;

        public AppFrame()
        {
            super(true, true, false);

            // Add view controls layer and select listener
            viewControlsLayer = new ViewControlsLayer();
            insertBeforeCompass(getWwd(), viewControlsLayer);
            getLayerPanel().update(getWwd());
            this.getWwd().addSelectListener(new ViewControlsSelectListener(this.getWwd(), viewControlsLayer));

            // Find Compass layer and enable picking
            for (Layer layer : this.getWwd().getModel().getLayers())
            {
                if (layer instanceof CompassLayer)
                    layer.setPickEnabled(true);
            }

            // Add select listener to handle drag events on the compass
            this.getWwd().addSelectListener(new SelectListener()
            {
                Angle dragStartHeading = null;
                Angle viewStartHeading = null;
                View view = getWwd().getView();

                public void selected(SelectEvent event)
                {
                    if (event.getTopObject() instanceof CompassLayer)
                    {
                        Angle heading = (Angle) event.getTopPickedObject().getValue("Heading");
                        if (event.getEventAction().equals(SelectEvent.DRAG) && dragStartHeading == null)
                        {
                            dragStartHeading = heading;
                            viewStartHeading = view.getHeading();
                        }
                        else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && dragStartHeading != null)
                        {
                            double move = heading.degrees - dragStartHeading.degrees;
                            double newHeading = viewStartHeading.degrees - move;
                            newHeading = newHeading >= 0 ? newHeading : newHeading + 360;
                            view.stopAnimations();
                            view.setHeading(Angle.fromDegrees(newHeading));
                        }
                        else if (event.getEventAction().equals(SelectEvent.DRAG_END))
                        {
                            dragStartHeading = null;
                        }
                    }
                }
            });

            // Add view controls selection panel
            this.getLayerPanel().add(makeControlPanel(), BorderLayout.SOUTH);
        }

        private JPanel makeControlPanel()
        {
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("View Controls")));
            controlPanel.setToolTipText("Select active view controls");

            // Radio buttons - layout
            JPanel layoutPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            layoutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            ButtonGroup group = new ButtonGroup();
            JRadioButton button = new JRadioButton("Horizontal", true);
            group.add(button);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setLayout(AVKey.HORIZONTAL);
                    getWwd().redraw();
                }
            });
            layoutPanel.add(button);
            button = new JRadioButton("Vertical", false);
            group.add(button);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setLayout(AVKey.VERTICAL);
                    getWwd().redraw();
                }
            });
            layoutPanel.add(button);

            // Scale slider
            JPanel scalePanel = new JPanel(new GridLayout(0, 1, 0, 0));
            scalePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            scalePanel.add(new JLabel("Scale:"));
            JSlider scaleSlider = new JSlider(1, 20, 10);
            scaleSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    viewControlsLayer.setScale(((JSlider) event.getSource()).getValue() / 10d);
                    getWwd().redraw();
                }
            });
            scalePanel.add(scaleSlider);

            // Check boxes
            JPanel checkPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            checkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JCheckBox check = new JCheckBox("Pan");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowPanControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Look");
            check.setSelected(false);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowLookControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Zoom");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowZoomControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Heading");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowHeadingControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Pitch");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowPitchControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Field of view");
            check.setSelected(false);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowFovControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            checkPanel.add(check);

            controlPanel.add(layoutPanel);
            controlPanel.add(scalePanel);
            controlPanel.add(checkPanel);
            return controlPanel;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind View Controls", AppFrame.class);
    }
}
