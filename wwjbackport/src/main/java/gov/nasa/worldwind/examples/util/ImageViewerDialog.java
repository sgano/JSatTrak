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
import java.io.File;

/**
 * Displays an image.
 *
 * @author Patrick Murris
 * @version $Id: ImageViewerDialog.java 11158 2009-05-14 18:58:07Z patrickmurris $
 */
public class ImageViewerDialog extends JFrame
{
    private static final String TITLE_TEXT = "Image Viewer";
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 250;

    private final ImageViewerPanel panel;
    private boolean dropEnabled = false;

    public ImageViewerDialog()
    {
        this.setTitle(TITLE_TEXT);
        this.setLayout(new BorderLayout());
        this.panel = new ImageViewerPanel();
        this.add(this.panel, BorderLayout.CENTER);
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setupDropSupport();
    }

    public void paint(final Graphics g)
    {
        super.paint(g);
        this.panel.repaint();
    }

    public ImageViewer getImageViewer()
    {
        return this.panel.imageViewer;
    }

    public String getCaption()
    {
        return this.panel.caption.getText();
    }

    public void setCaption(String caption)
    {
        if (caption != null && caption.length() > 0)
        {
            this.panel.caption.setText(caption);
            this.panel.caption.setVisible(true);
        }
        else
        {
            this.panel.caption.setText("");
            this.panel.caption.setVisible(false);
        }
    }

    public JLabel getCaptionLabel()
    {
        return this.panel.caption;
    }

    public boolean isDropEnabled()
    {
        return this.dropEnabled;
    }

    public void setDropEnabled(boolean state)
    {
        this.dropEnabled = state;
    }

    public void setTransferHandler(TransferHandler transferHandler)
    {
        this.panel.setTransferHandler(transferHandler);
    }

    protected void setupDropSupport()
    {
        // Set up drop support
        this.setTransferHandler(new TransferHandler()
        {
            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
            {
                if (!dropEnabled)
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
                    File inFile = (File)f;
                    try
                    {
                        if (getImageViewer().setImageURL(inFile.toURI().toURL()))
                            return true;
                    }
                    catch (Exception ignore) {}
                }
                return false;
            }

        });
    }

    public class ImageViewerPanel extends JPanel
    {
        private ImageViewer imageViewer;
        private JLabel caption;

        public ImageViewerPanel()
        {
            // Init components
            initComponents();
        }

        private void initComponents()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.imageViewer = new ImageViewer();
            this.add(this.imageViewer);
            this.caption = new JLabel("");
            this.caption.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.caption.setVisible(false);
            this.add(caption);
        }
    }
}
