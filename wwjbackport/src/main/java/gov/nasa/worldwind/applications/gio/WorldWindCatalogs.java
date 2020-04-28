/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.catalogui.SwingUtils;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.examples.LayerPanel;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author dcollins
 * @version $Id: WorldWindCatalogs.java 12432 2009-08-10 16:44:42Z tgaskins $
 */
public class WorldWindCatalogs extends ApplicationTemplate
{
    private static final String ESG_TITLE_LONG = "NASA Earth Science Gateway";
    private static final String GEOSS_TITLE_LONG = "Global Earth Observation System of Systems";
    private static final String GIDB_TITLE_LONG = "NRL Geospatial Information Database";
    private static final String ACTION_COMMAND_SHOW_CATALOGS = "ActionCommandShowCatalogs";

    public static class AppFrame extends ApplicationTemplate.AppFrame implements PropertyChangeListener
    {
        private Controller controller;
        private CatalogFrame catalogFrame;
        private LayerPanel layerPanel;

        public AppFrame()
        {
            // We add our own LayerPanel, but keep the StatusBar from ApplicationTemplate.
            super(true, false, false);
            this.controller = new Controller(this);
            makeComponents();
            if (getWwd() != null && getWwd().getModel() != null && getWwd().getModel().getLayers() != null)
                getWwd().getModel().getLayers().addPropertyChangeListener(this);
        }

        public LayerPanel getLayerPanel()
        {
            return this.layerPanel;
        }

        private void makeComponents()
        {
            JMenuBar menuBar = new JMenuBar();
            {
                JMenu menu = new JMenu("Catalogs");
                {
                    JMenuItem menuItem = new JMenuItem("Search Catalogs...");
                    menuItem.setActionCommand(ACTION_COMMAND_SHOW_CATALOGS);
                    menuItem.addActionListener(this.controller);
                    menu.add(menuItem);
                }
                menuBar.add(menu);
            }
            setJMenuBar(menuBar);

            JPanel panel = new JPanel(new BorderLayout());
            {
                panel.setBorder(new EmptyBorder(10, 0, 10, 0));

                JPanel btnPanel = new JPanel(new BorderLayout());
                btnPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
                JButton btn = new JButton("Search Catalogs...");
                btn.setActionCommand(ACTION_COMMAND_SHOW_CATALOGS);
                btn.addActionListener(this.controller);
                btnPanel.add(btn, BorderLayout.CENTER);
                panel.add(btnPanel, BorderLayout.SOUTH);

                this.layerPanel = new LayerPanel(getWwd(), null);
                panel.add(this.layerPanel, BorderLayout.CENTER);
            }
            getContentPane().add(panel, BorderLayout.WEST);
        }

        public boolean isCatalogFrameVisible()
        {
            return this.catalogFrame != null && this.catalogFrame.isVisible();
        }

        public void setCatalogFrameVisible(boolean b)
        {
            if (this.catalogFrame == null)
            {
                this.catalogFrame = new CatalogFrame();
                this.catalogFrame.setTitle("Search Catalogs");

                ESGCatalogPanel esgPanel = new ESGCatalogPanel();
                esgPanel.setWorldWindow(getWwd());
                esgPanel.addPropertyChangeListener(this);
                esgPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
                // Set initial query parameters.
                esgPanel.getQueryModel().setWCSEnabled(true);
                esgPanel.getQueryModel().setWFSEnabled(true);
                esgPanel.getQueryModel().setWMSEnabled(true);
                // Invoke a query on startup.
                esgPanel.getController().propertyChange(new PropertyChangeEvent(this, CatalogKey.ACTION_COMMAND_QUERY, null, null));
                this.catalogFrame.addCatalogPanel(ESG_TITLE_LONG, esgPanel);

                GEOSSCatalogPanel geossPanel = new GEOSSCatalogPanel();
                geossPanel.setWorldWindow(getWwd());
                geossPanel.addPropertyChangeListener(this);
                geossPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
                // Set initial query parameters.
                geossPanel.getQueryModel().setWCSEnabled(true);
                geossPanel.getQueryModel().setWFSEnabled(true);
                geossPanel.getQueryModel().setWMSEnabled(true);
                // Invoke a query on startup.
                geossPanel.getController().propertyChange(new PropertyChangeEvent(this, CatalogKey.ACTION_COMMAND_QUERY, null, null));
                this.catalogFrame.addCatalogPanel(GEOSS_TITLE_LONG, geossPanel);

                GIDBCatalogPanel gidbPanel = new GIDBCatalogPanel();
                gidbPanel.setWorldWindow(getWwd());
                gidbPanel.addPropertyChangeListener(this);
                gidbPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
                // Invoke a query on startup.
                gidbPanel.getController().propertyChange(new PropertyChangeEvent(this, CatalogKey.ACTION_COMMAND_QUERY, null, null));
                this.catalogFrame.addCatalogPanel(GIDB_TITLE_LONG, gidbPanel);

                this.catalogFrame.pack();
                positionCatalogFrame(this, this.catalogFrame);
            }
            
            this.catalogFrame.setVisible(b);
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt != null && evt.getPropertyName() != null)
            {
                // World Wind layers changed.
                if (evt.getPropertyName().contains(AVKey.LAYERS))
                {
                    updateLayerPanelInEventThread();
                }
            }
        }

        private void updateLayerPanelInEventThread()
        {
            try
            {
                SwingUtils.invokeInEventThread(new Runnable()
                {
                        public void run()
                        {
                            updateLayerPanel();
                        }
                    });
            }
            catch (Exception ex)
            {
                String message = "esg.ExceptionWhilePassingToEventThread";
                Logging.logger().log(java.util.logging.Level.SEVERE, message, ex);
            }
        }

        private void updateLayerPanel()
        {
            if (getLayerPanel() != null)
                getLayerPanel().update(getWwd());     
        }
    }

    private static void positionCatalogFrame(Window appFrame, Window esgFrame)
    {
        Rectangle appBounds = appFrame.getBounds();

        int preferredWidth = 800;
        esgFrame.setPreferredSize(new Dimension(preferredWidth, appBounds.height));
        esgFrame.pack();

        Rectangle esgBounds = esgFrame.getBounds();
        esgFrame.setLocation(appBounds.x - esgBounds.width, appBounds.y);
        SwingUtils.fitWindowInDesktop(esgFrame);
    }

    public static class CatalogFrame extends JFrame
    {
        private JTabbedPane tabbedPane;

        public CatalogFrame()
        {
            makeComponents();
            layoutComponents();
        }

        public void addCatalogPanel(String name, JPanel catalogPanel)
        {
            if (name == null)
            {
                String message = Logging.getMessage("nullValue.NameIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (catalogPanel == null)
            {
                String message = Logging.getMessage("nullValue.CatalogPanelIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.tabbedPane.addTab(name, catalogPanel);
            this.tabbedPane.revalidate();
            this.tabbedPane.repaint();
        }

        private void makeComponents()
        {
            this.tabbedPane = new JTabbedPane();
        }

        private void layoutComponents()
        {
            getContentPane().setLayout(new BorderLayout());

            JPanel appPanel = new JPanel();
            {
                appPanel.setLayout(new BorderLayout());
                appPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
                appPanel.add(this.tabbedPane, BorderLayout.CENTER);
            }

            getContentPane().add(appPanel, BorderLayout.CENTER);
        }
    }

    private static class Controller implements ActionListener
    {
        private AppFrame appFrame;

        private Controller(AppFrame appFrame)
        {
            this.appFrame = appFrame;
        }

        public void actionPerformed(ActionEvent e)
        {
            if (e != null && e.getActionCommand() != null && this.appFrame != null)
            {
                String actionCommand = e.getActionCommand();
                if (actionCommand.equals(ACTION_COMMAND_SHOW_CATALOGS))
                {
                    this.appFrame.setCatalogFrameVisible(true);
                }
            }
        }
    }

    public static void start(String appName)
    {
        try
        {
            final AppFrame frame = new AppFrame();
            if (appName != null)
                frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            SwingUtils.centerWindowInDesktop(frame);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                    frame.setCatalogFrameVisible(true);
                }
            });
        }
        catch (Exception e)
        {
            String message = "esg.CannotCreateApplicationWindow";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            JOptionPane.showMessageDialog(null, "Unable to launch World Wind Web Service Catalogs application", null,
                    JOptionPane.ERROR_MESSAGE);
            stop();
        }
    }

    public static void stop()
    {
        System.exit(0);
    }

    public static final String APP_NAME = "World Wind Web Service Catalogs";
    public static final String APP_VERSION = "1.0 (September 16, 2008)";
    public static final String APP_NAME_AND_VERSION = APP_NAME + ", " + APP_VERSION;

    static
    {
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    public static void main(String[] args)
    {
        start(APP_NAME);
    }
}
