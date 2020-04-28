/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.layers.Earth.BMNGOneImage;
import gov.nasa.worldwind.avlist.AVKey;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/** Using the AnaglyphSceneController
 * @author tag
 * @version $Id: AnaglyphStereo.java 3155 2007-09-29 07:29:54Z patrickmurris $
 */
public class AnaglyphStereo extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private String displayMode;
        private Angle focusAngle;
        private JSlider focusAngleSlider;

        public AppFrame()
        {
            super(true, true, false);

            // Retreive current settings
            this.displayMode = ((AnaglyphSceneController)this.getWwd().getSceneController()).getDisplayMode();
            this.focusAngle = ((AnaglyphSceneController)this.getWwd().getSceneController()).getFocusAngle();

            // Add a stereo control panel to the layer panel
            this.getLayerPanel().add(makeStereoPanel(),  BorderLayout.SOUTH);
        }

        private JPanel makeStereoPanel()
        {
            JPanel stereoPanel = new JPanel(new GridLayout(0, 1, 0, 0));

            // Mono/Stereo button group
            JPanel buttons = new JPanel(new GridLayout(0, 2, 0, 0));
            ButtonGroup group = new ButtonGroup();
            JRadioButton bMono = new JRadioButton("Mono");
            bMono.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    displayMode = AnaglyphSceneController.DISPLAY_MODE_MONO;
                    update();
                }
            });
            bMono.setSelected(!this.displayMode.equals(AnaglyphSceneController.DISPLAY_MODE_STEREO));
            group.add(bMono);
            buttons.add(bMono);
            JRadioButton bStereo = new JRadioButton("Stereo");
            bStereo.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    displayMode = AnaglyphSceneController.DISPLAY_MODE_STEREO;
                    update();
                }
            });
            bStereo.setSelected(this.displayMode.equals(AnaglyphSceneController.DISPLAY_MODE_STEREO));
            group.add(bStereo);
            buttons.add(bStereo);

            // Focus angle slider
            JPanel slider = new JPanel(new GridLayout(0, 1, 0, 0));
            JSlider s = new JSlider(JSlider.HORIZONTAL, 0, 50, (int)(this.focusAngle.degrees * 10));  // 0 - 5 in tenth of degrees
            s.setMajorTickSpacing(10);
            s.setMinorTickSpacing(1);
            s.setPaintTicks(true);
            s.setPaintLabels(true);
            s.setToolTipText("Focus angle 1/10 degrees");
            s.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    JSlider s = (JSlider)event.getSource();
                    if (!s.getValueIsAdjusting())
                    {
                        focusAngle = Angle.fromDegrees( (double)s.getValue() / 10);
                        update();
                    }
                }

            });
            slider.add(s);
            s.setEnabled(this.displayMode.equals(AnaglyphSceneController.DISPLAY_MODE_STEREO));
            this.focusAngleSlider = s;

            // Help label
            JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
            JLabel l = new JLabel("Tip: pitch view for stereo effect.");
            l.setHorizontalAlignment(SwingConstants.CENTER);
            text.add(l);

            // Assembly
            stereoPanel.add(buttons);
            stereoPanel.add(slider);
            stereoPanel.add(text);
            stereoPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("View")));
            stereoPanel.setToolTipText("Stereo controls");
            return stereoPanel;
        }

        // Update worldwind
        private void update()
        {
            AnaglyphSceneController asc = (AnaglyphSceneController)this.getWwd().getSceneController();
            asc.setDisplayMode(this.displayMode);
            asc.setFocusAngle(this.focusAngle);
            this.focusAngleSlider.setEnabled(this.displayMode.equals(AnaglyphSceneController.DISPLAY_MODE_STEREO));
            this.getWwd().redraw();
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, AnaglyphSceneController.class.getName());
        ApplicationTemplate.start("World Wind Anaglyph Stereo", AppFrame.class);
    }
}
