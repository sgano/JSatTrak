/* Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.applications.gos.event.*;
import gov.nasa.worldwind.applications.gos.globe.WorldWindowGlobeModel;
import gov.nasa.worldwind.applications.gos.awt.GeodataLayerPanel;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * @author dcollins
 * @version $Id: GeodataPortalFrame.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class GeodataPortalFrame extends JFrame
{
    protected GeodataWindowJPanel searchPanel;
    protected GeodataLayerPanel layerPanel;
    protected GeodataRecentChangesPanel recentChangesPanel;
    protected final EventListenerList listenerList = new EventListenerList();

    public GeodataPortalFrame(long feedUpdatePeriod, TimeUnit timeUnit) throws HeadlessException
    {
        super(Configuration.getStringValue(GeodataKey.DISPLAY_NAME_SHORT));

        this.searchPanel = this.createSearchPanel();
        this.searchPanel.addSearchListener(new SearchListener()
        {
            public void searchPerformed(SearchEvent event)
            {
                recentChangesPanel.setQueryParams(event.getParams());
            }

            public void searchCancelled(SearchEvent event)
            {
            }
        });

        this.layerPanel = this.createLayerPanel();

        this.recentChangesPanel = this.createRecentChangesPanel();
        this.recentChangesPanel.setBackground(Color.WHITE);
        this.recentChangesPanel.setPreferredSize(new Dimension(700, 800));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // top, left, bottom, right
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.add("Search", this.searchPanel);
        tabbedPane.add("Layers", this.layerPanel);
        tabbedPane.add("Update Feed", this.recentChangesPanel);

        this.setBackground(Color.WHITE);
        this.getContentPane().setLayout(new BorderLayout(0, 0)); // hgap, vgap
        this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        this.pack();

        this.searchPanel.requestFocusInWindow();

        this.startUpdateFeed(feedUpdatePeriod, timeUnit);
    }

    public GeodataPortalFrame() throws HeadlessException
    {
        this(1, TimeUnit.MINUTES);
    }

    public GeodataWindow getGeodataWindow()
    {
        return this.searchPanel;
    }

    public GeodataRecentChangesPanel getRecentChangesPanel()
    {
        return this.recentChangesPanel;
    }

    public WorldWindow getWorldWindow()
    {
        return this.searchPanel.getWorldWindow();
    }

    public void setWorldWindow(WorldWindow wwd)
    {
        this.searchPanel.setWorldWindow(wwd);
        this.layerPanel.setWorldWindow(wwd);
        this.recentChangesPanel.setGlobeModel(new WorldWindowGlobeModel(wwd));
    }

    public void stopUpdateFeed()
    {
        this.recentChangesPanel.stop();
    }

    public void startUpdateFeed(long period, TimeUnit timeUnit)
    {
        this.recentChangesPanel.start(period, timeUnit);
    }

    protected GeodataWindowJPanel createSearchPanel()
    {
        return new GeodataWindowJPanel();
    }

    protected GeodataRecentChangesPanel createRecentChangesPanel()
    {
        return new GeodataRecentChangesPanel();
    }

    protected GeodataLayerPanel createLayerPanel()
    {
        return new GeodataLayerPanel();
    }
}
