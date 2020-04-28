/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.URL;
import java.util.ArrayList;
import java.io.File;

/**
 * Displays image slides.
 *
 * @author Patrick Murris
 * @version $Id: SlideShowPlayer.java 11158 2009-05-14 18:58:07Z patrickmurris $
 */
public class SlideShowPlayer extends ImageViewerDialog
{
    private SlideShowPanel controlPanel;
    private ArrayList<Slide> slides;
    private int slideNumber = 0;

    public static class Slide
    {
        private final URL imageURL;
        private final String caption;

        private BufferedImage image;

        public Slide(URL imageURL, String caption)
        {
            this.imageURL = imageURL;
            this.caption = caption;
        }
    }

    public SlideShowPlayer()
    {
        super();
        this.controlPanel = new SlideShowPanel();
        this.add(this.controlPanel, BorderLayout.SOUTH);
    }

    public ArrayList<Slide> getSlides()
    {
        return this.slides;
    }

    public void setSlides(ArrayList<Slide> slides)
    {
        this.clear();
        this.slides = slides;
        if (slides != null && slides.size() > 0)
            this.setSlideNumber(1);
        this.controlPanel.update();
    }

    public void addSlide(Slide slide)
    {
        if (this.slides == null)
            this.slides = new ArrayList<Slide>();
        this.slides.add(slide);
        if (this.slideNumber == 0)
            this.setSlideNumber(1);
        this.controlPanel.update();
    }

    public void clear()
    {
        this.slides = null;
        this.slideNumber = 0;
        this.controlPanel.update();
    }

    public int getSlideNumber()
    {
        return this.slideNumber;
    }

    public void setSlideNumber(int slideNumber)
    {
        if (this.slides != null && this.slideNumber != 0 && this.slideNumber <= slides.size())
        {
            // Save current slide image
            Slide s = slides.get(this.slideNumber - 1);
            if (s.image == null)
                s.image = this.getImageViewer().getImage();
        }
        if (this.slides != null && slideNumber > 0 && slideNumber <= slides.size())
        {
            // Change image and caption
            this.slideNumber = slideNumber;
            Slide s = slides.get(this.slideNumber - 1);
            if (s.image != null)
                getImageViewer().setImage(s.image); // use saved image if available
            else
                getImageViewer().setImageURL(s.imageURL); // load from url otherwise
            setCaption(s.caption);
            this.controlPanel.update();
        }
    }

    protected void setupDropSupport()
    {
        // Set up drop support
        this.setTransferHandler(new TransferHandler()
        {
            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
            {
                if (!isDropEnabled())
                    return false;

                for (DataFlavor df : transferFlavors)
                    if (df.equals(DataFlavor.javaFileListFlavor))
                        return true;

                return false;
            }

            public boolean importData(JComponent comp, Transferable t)
            {
                java.util.List files;
                try
                {
                    files = (java.util.List)t.getTransferData(DataFlavor.javaFileListFlavor);
                }
                catch (Exception e)
                {
                    return false;
                }
                for (Object f : files)
                {
                    // Add each file as a new slide, use file name as caption
                    File inFile = (File)f;
                    try
                    {
                        addSlide(new Slide(inFile.toURI().toURL(), inFile.getName()));
                    }
                    catch (Exception ignore) {}
                }
                return true;
            }
        });
    }

    public class SlideShowPanel extends JPanel
    {
        private JButton previousButton;
        private JButton nextButton;
        private JLabel positionLabel;

        public SlideShowPanel()
        {
            // Init components
            initComponents();
        }

        @SuppressWarnings({"UnusedDeclaration"})
        private void previousButtonActionPerformed(ActionEvent e)
        {
            setSlideNumber(slideNumber - 1);
        }

        @SuppressWarnings({"UnusedDeclaration"})
        private void nextButtonActionPerformed(ActionEvent e)
        {
            setSlideNumber(slideNumber + 1);
        }

        private void initComponents()
        {

            JPanel controlPanel = new JPanel();
            //controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
            controlPanel.setLayout(new GridLayout(1, 3, 0, 0));
            {
                previousButton = new JButton("<");
                //previousButton.setPreferredSize(new Dimension(40, 20));
                previousButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        previousButtonActionPerformed(e);
                    }
                });
                controlPanel.add(previousButton);

                positionLabel = new JLabel("0 / 0", SwingConstants.CENTER);
                controlPanel.add(positionLabel);

                nextButton = new JButton(">");
                //nextButton.setPreferredSize(new Dimension(40, 30));
                nextButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        nextButtonActionPerformed(e);
                    }
                });
                controlPanel.add(nextButton);
            }
            this.add(controlPanel);
        }

        public void update()
        {
            if (slides != null)
            {
                positionLabel.setText(slideNumber + " / " + slides.size());
                previousButton.setEnabled(slideNumber > 1);
                nextButton.setEnabled(slideNumber < slides.size());
            }
            else
            {
                positionLabel.setText("0 / 0");
                previousButton.setEnabled(false);
                nextButton.setEnabled(false);
            }
        }


    }
}
