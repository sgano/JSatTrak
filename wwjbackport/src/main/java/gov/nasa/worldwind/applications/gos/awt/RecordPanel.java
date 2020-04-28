/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.applications.gos.globe.GlobeModel;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.URL;
import java.util.concurrent.Future;

/**
 * @author dcollins
 * @version $Id: RecordPanel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class RecordPanel extends JPanel
{
    protected Record record;
    protected GlobeModel globeModel;
    // Swing components.
    protected StateCardPanel contentPanel;
    protected JButton toggleContentButton;
    protected JLabel serviceStatusLabel;
    // Content components.
    protected JComponent contentComponent;
    protected Future taskFuture;

    public RecordPanel(Record record, GlobeModel globeModel)
    {
        this.record = record;
        this.globeModel = globeModel;

        this.contentComponent = this.createContentComponent();
        if (this.contentComponent != null)
        {
            JComponent wc = StateCardPanel.createWaitingComponent("Downloading content...", Component.LEFT_ALIGNMENT,
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        cancelSetContentVisible();
                    }
                });
            wc.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 200));

            this.toggleContentButton = new JButton(new ShowContentAction());
            this.contentPanel = new StateCardPanel();
            this.contentPanel.setVisible(false);
            this.contentPanel.setBackground(Color.WHITE);
            this.contentPanel.setComponent(GeodataKey.STATE_NORMAL, this.contentComponent);
            this.contentPanel.setComponent(GeodataKey.STATE_WAITING, wc);
            this.contentPanel.setState(GeodataKey.STATE_NORMAL);
        }

        // Retrieve the record's service status via the GOS Application's threaded task service. Update the record's
        // text once the retrieval completes.
        OnlineResource r = record.getResource(GeodataKey.SERVICE_STATUS);
        if (r != null && r.getURL() != null)
        {
            ResourceUtil.getOrRetrieveServiceStatus(r.getURL(), new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    updateServiceStatus();
                }
            });
        }

        this.setBackground(Color.WHITE);
        this.layoutComponents();
    }

    public Record getRecord()
    {
        return this.record;
    }

    public GlobeModel getGlobeModel()
    {
        return this.globeModel;
    }

    public boolean isContentVisible()
    {
        return this.contentPanel.isVisible();
    }

    public void setContentVisible(boolean visible)
    {
        this.contentPanel.setVisible(visible);

        if (!visible)
            return;

        this.beforeShowContent();
        this.taskFuture = ResourceUtil.getAppTaskService().submit(new Runnable()
        {
            public void run()
            {
                try
                {
                    doShowContent();
                    afterShowContent(null, Thread.currentThread().isInterrupted());
                }
                catch (Exception e)
                {
                    afterShowContent(e, Thread.currentThread().isInterrupted());
                }
            }
        });
    }

    public void cancelSetContentVisible()
    {
        if (this.taskFuture != null && !this.taskFuture.isDone() && !this.taskFuture.isCancelled())
        {
            this.taskFuture.cancel(true);
        }
    }

    protected void layoutComponents()
    {
        if (this.record == null)
            return;

        Box contentBox = Box.createVerticalBox();
        Box iconBox = Box.createVerticalBox();

        // Configure record title label.
        JComponent c = this.createTitleComponent();
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentBox.add(c);
        contentBox.add(Box.createVerticalStrut(5));

        // Configure record abstract label.
        c = this.createAbstractComponent();
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentBox.add(c);
        contentBox.add(Box.createVerticalStrut(5));

        // Configure record button controls.
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        OnlineResource r = this.record.getResource(GeodataKey.METADATA);
        if (r != null)
        {
            buttonBox.add(new JButton(new OpenBrowserAction(r.getDisplayName(), r.getURL())));
            buttonBox.add(Box.createHorizontalStrut(5));
        }

        r = this.record.getResource(GeodataKey.SERVICE_STATUS_METADATA);
        if (r != null)
        {
            buttonBox.add(new JButton(new OpenBrowserAction(r.getDisplayName(), r.getURL())));
            buttonBox.add(Box.createHorizontalStrut(5));
        }

        if (this.toggleContentButton != null)
        {
            buttonBox.add(this.toggleContentButton);
            buttonBox.add(Box.createHorizontalStrut(5));
        }

        if (this.globeModel != null && this.record.getSector() != null)
        {
            buttonBox.add(new JButton(new GoToAction(this.globeModel, this.record.getSector())));
        }

        buttonBox.add(Box.createHorizontalGlue());
        contentBox.add(buttonBox);

        // Configure record content panel.
        if (this.contentPanel != null)
        {
            this.contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 30, 30));
            contentBox.add(this.contentPanel);
        }

        // Configure record image icon.
        r = this.record.getResource(GeodataKey.IMAGE);
        if (r != null)
        {
            JLabel label = new JLabel(this.createIcon(r));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            iconBox.add(label);
            iconBox.add(Box.createVerticalStrut(20));
        }

        // Configure record service status icon.
        r = this.record.getResource(GeodataKey.SERVICE_STATUS);
        if (r != null)
        {
            this.serviceStatusLabel = new JLabel();
            this.serviceStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            iconBox.add(this.serviceStatusLabel);
            this.updateServiceStatus();
        }

        contentBox.add(Box.createVerticalGlue());
        iconBox.add(Box.createVerticalGlue());

        // Configure main panel layout.
        Box mainBox = Box.createHorizontalBox();
        mainBox.add(iconBox);
        mainBox.add(Box.createHorizontalStrut(10));
        mainBox.add(contentBox);
        this.setLayout(new BorderLayout(0, 0)); // hgap, vgap
        this.add(mainBox, BorderLayout.NORTH);
        this.validate();
    }

    protected JComponent createTitleComponent()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<b>");
        sb.append(this.record.getTitle());
        sb.append("</b>");
        sb.append("</html>");

        return new JLabel(sb.toString());
    }

    protected JComponent createAbstractComponent()
    {
        StringBuilder sb = new StringBuilder();
        int maxCharacters = 200;

        if (maxCharacters < this.record.getAbstract().length())
        {
            sb.append(this.record.getAbstract(), 0, maxCharacters);
            sb.append("...");
        }
        else
        {
            sb.append(this.record.getAbstract());
        }

        JTextArea ta = new JTextArea(sb.toString());
        ta.setLineWrap(true);
        return ta;
    }

    protected JComponent createContentComponent()
    {
        OnlineResource capsResource = this.record.getResource(GeodataKey.CAPABILITIES);
        if (capsResource == null)
            return null;

        return new WMSLayerPanel(this.record, this.globeModel);
    }

    protected void doShowContent() throws Exception
    {
        OnlineResource r = this.record.getResource(GeodataKey.CAPABILITIES);
        if (r == null || r.getURL() == null)
            return;

        String key = r.getURL().toString();
        WMSCapabilities caps = (WMSCapabilities) WorldWind.getSessionCache().get(key);
        if (caps == null && !WorldWind.getSessionCache().contains(key))
        {
            caps = WMSCapabilities.retrieve(r.getURL().toURI());

            if (Thread.currentThread().isInterrupted())
                return;

            if (caps != null)
                caps.parse();

            WorldWind.getSessionCache().put(key, caps);
        }

        if (Thread.currentThread().isInterrupted())
            return;

        WMSLayerPanel layerPanel = (WMSLayerPanel) this.contentPanel.getComponent(GeodataKey.STATE_NORMAL);
        if (layerPanel.getCapabilities() != caps)
            layerPanel.setCapabilities(caps);
    }

    protected void beforeShowContent()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    beforeShowContent();
                }
            });
        }
        else
        {
            this.toggleContentButton.setEnabled(false);
            this.contentPanel.setState(GeodataKey.STATE_WAITING);
        }
    }

    protected void afterShowContent(final Exception e, final boolean cancelled)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    afterShowContent(e, cancelled);
                }
            });
        }
        else
        {
            this.contentPanel.setVisible(!cancelled);
            if (e != null)
            {
                JComponent ec = StateCardPanel.createErrorComponent(ResourceUtil.createErrorMessage(e),
                    Component.LEFT_ALIGNMENT);
                this.contentPanel.setComponent(GeodataKey.STATE_ERROR, ec);
                this.contentPanel.setState(GeodataKey.STATE_ERROR);
            }
            else
            {
                this.contentPanel.setState(GeodataKey.STATE_NORMAL);
            }

            this.toggleContentButton.setEnabled(true);
            this.toggleContentButton.setAction(cancelled ? new ShowContentAction() : new HideContentAction());
            this.taskFuture = null;
        }
    }

    protected void updateServiceStatus()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    updateServiceStatus();
                }
            });
        }
        else
        {
            OnlineResource r = this.record.getResource(GeodataKey.SERVICE_STATUS);
            if (r != null && r.getURL() != null)
            {
                ServiceStatus serviceStatus = (ServiceStatus) WorldWind.getSessionCache().get(r.getURL().toString());
                if (serviceStatus != null)
                {
                    OnlineResource sr = serviceStatus.getScoreImageResource();
                    if (sr != null)
                    {
                        this.serviceStatusLabel.setIcon(this.createIcon(sr));
                        this.serviceStatusLabel.setToolTipText(sr.getDisplayName());
                    }
                }
            }
        }
    }

    protected ImageIcon createIcon(OnlineResource r)
    {
        //BufferedImage image = ResourceUtil.getCachedImage(r.getURL());
        return new ImageIcon(r.getURL());
    }

    protected static class OpenBrowserAction extends AbstractAction
    {
        public URL url;

        public OpenBrowserAction(String name, URL url)
        {
            super(name);
            this.url = url;
        }

        public void actionPerformed(ActionEvent event)
        {
            if (this.url == null)
                return;

            try
            {
                BrowserOpener.browse(this.url);
            }
            catch (Exception e)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE,
                    Logging.getMessage("generic.ExceptionAttemptingToInvokeWebBrower", this.url.toString()), e);
            }
        }
    }

    protected static class GoToAction extends AbstractAction
    {
        protected GlobeModel globe;
        protected Sector sector;

        public GoToAction(GlobeModel globe, Sector sector)
        {
            super("Go There");
            this.globe = globe;
            this.sector = sector;
        }

        public void actionPerformed(ActionEvent event)
        {
            this.globe.moveViewTo(this.sector);
        }
    }

    protected class ShowContentAction extends AbstractAction
    {
        public ShowContentAction()
        {
            super("Show Content");
        }

        public void actionPerformed(ActionEvent event)
        {
            setContentVisible(true);
            ((AbstractButton) event.getSource()).setAction(new HideContentAction());
        }
    }

    protected class HideContentAction extends AbstractAction
    {
        public HideContentAction()
        {
            super("Hide Content");
        }

        public void actionPerformed(ActionEvent event)
        {
            setContentVisible(false);
            ((AbstractButton) event.getSource()).setAction(new ShowContentAction());
        }
    }
}
