package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.terrain.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Open and view arbitrary surface images with accompanying world file.
 *
 * @author tag
 * @version $Id: SurfaceImageViewer.java 13494 2010-06-26 08:36:38Z tgaskins $
 */
public class SurfaceImageViewer extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private JFileChooser fileChooser = new JFileChooser();
        private JSlider opacitySlider;
        private SurfaceImageLayer layer;
        private JLabel statusLabel = new JLabel("status: ready");

        public AppFrame()
        {
            super(true, true, false);

            try
            {
                this.layer = new SurfaceImageLayer();
                this.layer.setOpacity(1);
                this.layer.setPickEnabled(false);
                this.layer.setName("Surface Images");

                insertBeforeCompass(this.getWwd(), layer);

                this.getLayerPanel().add(makeControlPanel(), BorderLayout.SOUTH);

                this.getLayerPanel().update(this.getWwd());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        Action openElevationsAction = new AbstractAction("Open Elevation File...")
        {
            public void actionPerformed(ActionEvent e)
            {
                int status = fileChooser.showOpenDialog(AppFrame.this);
                if (status != JFileChooser.APPROVE_OPTION)
                    return;

                final File imageFile = fileChooser.getSelectedFile();
                if (imageFile == null)
                    return;

                Thread t = new Thread(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            CompoundElevationModel cem
                                = (CompoundElevationModel) getWwd().getModel().getGlobe().getElevationModel();
                            LocalElevationModel em = new LocalElevationModel();
                            em.addElevations(imageFile.getPath());
                            cem.addElevationModel(em);
                        }
                        catch (IOException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                });
                t.setPriority(Thread.MIN_PRIORITY);
                t.start();
            }
        };

        Action openImageAction = new AbstractAction("Open Image File...")
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                int status = fileChooser.showOpenDialog(AppFrame.this);
                if (status != JFileChooser.APPROVE_OPTION)
                    return;

                final File imageFile = fileChooser.getSelectedFile();
                if (imageFile == null)
                    return;

                Thread t = new Thread(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            statusLabel.setText("status: Loading image");
                            // TODO: proper threading
                            layer.addImage(imageFile.getAbsolutePath());

                            getWwd().repaint();
                            statusLabel.setText("status: ready");
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                t.setPriority(Thread.MIN_PRIORITY);
                t.start();
            }
        };

        private JPanel makeControlPanel()
        {
            JPanel controlPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            JButton openImageButton = new JButton(openImageAction);
            controlPanel.add(openImageButton);

            this.opacitySlider = new JSlider();
            this.opacitySlider.setMaximum(100);
            this.opacitySlider.setValue((int) (layer.getOpacity() * 100));
            this.opacitySlider.setEnabled(true);
            this.opacitySlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    int value = opacitySlider.getValue();
                    layer.setOpacity(value / 100d);
                    getWwd().repaint();
                }
            });
            JPanel opacityPanel = new JPanel(new BorderLayout(5, 5));
            opacityPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
            opacityPanel.add(new JLabel("Opacity"), BorderLayout.WEST);
            opacityPanel.add(this.opacitySlider, BorderLayout.CENTER);

            controlPanel.add(opacityPanel);

            JButton openElevationsButton = new JButton(openElevationsAction);
            controlPanel.add(openElevationsButton);

            controlPanel.add(statusLabel);
            controlPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            return controlPanel;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Surface Images", SurfaceImageViewer.AppFrame.class);
    }
}
