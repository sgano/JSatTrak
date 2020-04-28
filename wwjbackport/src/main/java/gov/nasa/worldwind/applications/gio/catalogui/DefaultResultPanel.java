/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * @author dcollins
 * @version $Id: DefaultResultPanel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class DefaultResultPanel extends JPanel
{
    private String waitingIconPath;
    private JComponent resultComponent;
    private JRootPane rootPane;
    private JLabel statusLabel;
    private static final String DEFAULT_WAITING_ICON_PATH = "images/indicator-66.gif";

    public DefaultResultPanel()
    {
        this.waitingIconPath = DEFAULT_WAITING_ICON_PATH;
        makeComponents();
        layoutComponents();
    }

    public String getWaitingIconPath()
    {
        return this.waitingIconPath;
    }

    public void setWaitingIconPath(String waitingIconPath)
    {
        this.waitingIconPath = waitingIconPath;
    }

    public JComponent getResultComponent()
    {
        return this.resultComponent;
    }

    public void setResultComponent(JComponent component)
    {
        this.resultComponent = component;

        if (this.rootPane.getContentPane() != null &&
            this.rootPane.getContentPane() instanceof ContentPane)
        {
            ContentPane contentPane = (ContentPane) this.rootPane.getContentPane();
            if (contentPane.getScrollPane() != null)
                contentPane.getScrollPane().setViewportView(this.resultComponent);
        }
    }

    public void setStatusText(String text)
    {
        this.statusLabel.setText(text);
    }

    public String getStatusText()
    {
        return this.statusLabel.getText();
    }

    public boolean isWaiting()
    {
        // Result panel is waiting only if the "waiting" GlassPane
        // is installed and visible.
        return this.rootPane.getGlassPane() != null &&
               this.rootPane.getGlassPane() instanceof GlassPane && 
               this.rootPane.getGlassPane().isVisible();
    }

    public void setWaiting(boolean waiting)
    {
        // Result panel can be set to waiting only if the "waiting" GlassPane
        // is installed. 
        if (this.rootPane.getGlassPane() != null &&
            this.rootPane.getGlassPane() instanceof GlassPane)
        {
            this.rootPane.getGlassPane().setVisible(waiting);

            // Disable ContentPane components when waiting.
            if (this.rootPane.getContentPane() != null)
            {
                this.rootPane.getContentPane().setEnabled(!waiting);
            }
        }
    }

    private void makeComponents()
    {
        this.rootPane = new JRootPane();
        this.rootPane.setContentPane(new ContentPane());

        if (this.waitingIconPath != null)
        {
            ImageIcon image = readImage(this.waitingIconPath);
            if (image == null)
            {
                String message = "catalog.UnableToReadWaitingImage " + this.waitingIconPath;
                Logging.logger().warning(message);
            }
            else
            {
                this.rootPane.setGlassPane(new GlassPane(image));
            }
        }

        this.statusLabel = new JLabel(" ");
    }

    private void layoutComponents()
    {
        setLayout(new BorderLayout());

        this.statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.rootPane.getContentPane().add(this.statusLabel, BorderLayout.NORTH);
        add(this.rootPane, BorderLayout.CENTER);
    }

    private static class ContentPane extends JPanel
    {
        private JScrollPane scrollPane;
        
        public ContentPane()
        {
            setLayout(new BorderLayout());

            this.scrollPane = new JScrollPane();
            add(this.scrollPane, BorderLayout.CENTER);
        }

        public JScrollPane getScrollPane()
        {
            return this.scrollPane;
        }

        public void setEnabled(boolean b)
        {
            super.setEnabled(b);
            // Decorate with the capability to disable ScrollBars and to disable the Viewport
            // View.
            if (this.scrollPane != null)
            {
                this.scrollPane.setEnabled(b);
                //if (this.scrollPane.getHorizontalScrollBar() != null)
                //    this.scrollPane.getHorizontalScrollBar().setEnabled(b);
                //if (this.scrollPane.getVerticalScrollBar() != null)
                //    this.scrollPane.getVerticalScrollBar().setEnabled(b);
                if (this.scrollPane.getViewport() != null)
                    this.scrollPane.getViewport().setEnabled(b);
                if (this.scrollPane.getViewport().getView() != null)
                    this.scrollPane.getViewport().getView().setEnabled(b);
            }
        }
    }

    private static class GlassPane extends JPanel
    {
        @SuppressWarnings({"FieldCanBeLocal"})
        private JLabel label;

        public GlassPane(ImageIcon image)
        {
            setLayout(new BorderLayout());
            setOpaque(false);

            this.label = new JLabel(image);
            this.label.setOpaque(false);
            image.setImageObserver(this.label);
            add(this.label, BorderLayout.CENTER);
        }
    }

    private ImageIcon readImage(String path)
    {
        ImageIcon image = null;
        try
        {
            URL url = getClass().getResource("/" + path);
            if (url != null)
            {
                image = new ImageIcon(url);
            }
        }
        catch (Exception e)
        {
            String message = "catalog.ExceptionWhileReadingImageResource " + path;
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return image;
    }
}
