/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.applications.gio.catalogui.*;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.TreeTableModel;
import gov.nasa.worldwind.applications.gio.esg.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author dcollins
 * @version $Id: ESGCatalogPanel.java 6662 2008-09-16 17:50:41Z dcollins $
 */
public class ESGCatalogPanel extends JPanel
{
    private String service;
    private String serviceDetailsContentPath;
    private String serviceDetailsContentType;
    private ESGCatalogModel model;
    private ESGController controller;
    private ESGQueryPanel queryPanel;
    private DefaultResultPanel resultPanel;
    @SuppressWarnings({"FieldCanBeLocal"})
    private PropertyChangeListener queryModelPropertyEvents;
    private PropertyChangeListener wwdPropertyEvents;
    private WorldWindow wwd;
    private static final String DEFAULT_SERVICE = "http://esg.gsfc.nasa.gov/wes/serviceManagerCSW/csw";
    private static final String DEFAULT_SERVICE_DETAILS_CONTENT_PATH = "gov/nasa/worldwind/applications/gio/esg/details.html";
    private static final String DEFAULT_SERVICE_DETAILS_CONTENT_TYPE = "text/html";

    public ESGCatalogPanel(String service, AVList tableParams)
    {
        if (service == null)
        {
            String message = Logging.getMessage("nullValue.ServiceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.service = service;
        this.serviceDetailsContentPath = DEFAULT_SERVICE_DETAILS_CONTENT_PATH;
        this.serviceDetailsContentType = DEFAULT_SERVICE_DETAILS_CONTENT_TYPE;

        this.model = new ESGCatalogModel();
        this.controller = new ESGController(this);
        this.model.addPropertyChangeListener(this.controller);

        if (tableParams == null)
            tableParams = new AVListImpl();

        makeComponents(tableParams);
        layoutComponents();
    }

    public ESGCatalogPanel()
    {
        this(DEFAULT_SERVICE, new AVListImpl());
    }

    public String getService()
    {
        return this.service;
    }

    public void setService(String service)
    {
        if (service == null)
        {
            String message = Logging.getMessage("nullValue.ServiceIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.service = service;
    }

    public URL getServiceURL()
    {
        try
        {
            return new URL(this.service);
        }
        catch (MalformedURLException e)
        {
            String message = "esg.InvalidService";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
    }

    public String getServiceDetailsContentPath()
    {
        return this.serviceDetailsContentPath;
    }

    public void setServiceDetailsContentPath(String path)
    {
        this.serviceDetailsContentPath = path;
    }

    public String getServiceDetailsContentType()
    {
        return this.serviceDetailsContentType;
    }

    public void setServiceDetailsContentType(String contentType)
    {
        this.serviceDetailsContentType = contentType;
    }

    public int getThreadPoolSize()
    {
        return this.controller.getThreadPoolSize();
    }

    public void setThreadPoolSize(int size)
    {
        this.controller.setThreadPoolSize(size);
    }

    public ESGCatalogModel getModel()
    {
        return this.model;
    }

    public ESGController getController()
    {
        return this.controller;
    }

    public ESGQueryModel getQueryModel()
    {
        return (ESGQueryModel) this.model.getQueryModel();
    }

    public ResultList getResultModel()
    {
        return this.model.getResultModel();
    }

    public ESGQueryPanel getQueryPanel()
    {
        return this.queryPanel;
    }

    public DefaultResultPanel getResultPanel()
    {
        return this.resultPanel;
    }

    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    public void setWorldWindow(WorldWindow wwd)
    {
        if (this.wwd != null)
        {
            if (this.wwd.getModel() != null)
                this.wwd.getModel().removePropertyChangeListener(this.wwdPropertyEvents);
            if (this.wwd.getSceneController() != null)
                this.wwd.getSceneController().removePropertyChangeListener(this.wwdPropertyEvents);
        }

        this.wwd = wwd;

        if (this.wwd != null)
        {
            if (this.wwd.getModel() != null)
                this.wwd.getModel().addPropertyChangeListener(this.wwdPropertyEvents);
            if (this.wwd.getSceneController() != null)
                this.wwd.getSceneController().addPropertyChangeListener(this.wwdPropertyEvents);
        }
    }

    protected void onQueryModelChanged(PropertyChangeEvent evt)
    {
        if (evt != null && evt.getPropertyName() != null)
        {
            String propertyName = evt.getPropertyName();
            Object newValue = evt.getNewValue();
            if (propertyName.equalsIgnoreCase(CatalogKey.LINK_WITH_WWJ_VIEW))
            {
                if (newValue != null && newValue instanceof Boolean && ((Boolean) newValue))
                {
                    updateQueryModel();
                }
            }
        }
    }

    protected void onWorldWindowChanged(PropertyChangeEvent evt)
    {
        if (evt != null && evt.getPropertyName() != null)
        {
            String propertyName = evt.getPropertyName();
            if (propertyName.equalsIgnoreCase(AVKey.VIEW))
                updateQueryModel();
        }
    }

    protected void updateQueryModel()
    {
        ESGQueryModel queryModel = (ESGQueryModel) this.model.getQueryModel();
        Boolean isLink = queryModel.isLinkWithWWJView();
        if (isLink != null && isLink)
        {
            if (this.wwd != null &&
                this.wwd.getSceneController() != null &&
                this.wwd.getSceneController().getDrawContext() != null &&
                this.wwd.getSceneController().getDrawContext().getVisibleSector() != null)
            {
                Sector sector = this.wwd.getSceneController().getDrawContext().getVisibleSector();
                queryModel.setMinLatitude(sector.getMinLatitude());
                queryModel.setMaxLatitude(sector.getMaxLatitude());
                queryModel.setMinLongitude(sector.getMinLongitude());
                queryModel.setMaxLongitude(sector.getMaxLongitude());
            }
        }
    }

    private void makeComponents(AVList tableParams)
    {
        this.queryPanel = new ESGQueryPanel((ESGQueryModel) this.model.getQueryModel());
        this.queryModelPropertyEvents = new QueryModelPropertyEvents(this);
        this.model.getQueryModel().addPropertyChangeListener(this.queryModelPropertyEvents);

        initTableParams(tableParams);
        JComponent resultComponent = new ESGTreeTable(this.model.getResultModel(), tableParams);
        this.resultPanel = new DefaultResultPanel();
        this.resultPanel.setResultComponent(resultComponent);

        this.wwdPropertyEvents = new WorldWindowPropertyEvents(this);
    }

    private void layoutComponents()
    {
        setLayout(new BorderLayout(0, 10));

        add(this.queryPanel, BorderLayout.NORTH);
        add(this.resultPanel, BorderLayout.CENTER);
    }

    private AVList initTableParams(AVList params)
    {
        if (params == null)
        {
            String message = "nullValue.ParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params.getValue(CatalogKey.TABLE_COLUMN_COUNT) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_COUNT, "6");

        if (params.getValue(CatalogKey.TABLE_COLUMN_PROPERTY_KEY) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_PROPERTY_KEY,
                ESGResultNode.class.getName() + "," + CatalogKey.TITLE + ",," + CatalogKey.ORIGINATOR + "," + ESGKey.ACTION_COMMAND_SHOW_SERVICE_DETAILS + "," + CatalogKey.SERVICE_TYPE + "," + ESGKey.NATIONAL_APPLICATIONS +
                "," + ServiceDataNode.class.getName() + "," + CatalogKey.TITLE + "," + ESGKey.ACTION_COMMAND_SERVICE_DATA_PRESSED + "," + CatalogKey.ORIGINATOR + ",,," +
                "," + ServiceDataLinkNode.class.getName() + "," + CatalogKey.URI + "," + CatalogKey.ACTION_COMMAND_BROWSE + ",,,,");

        if (params.getValue(CatalogKey.TABLE_COLUMN_CLASS) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_CLASS, TreeTableModel.class.getName() + ",,,,,");

        if (params.getValue(CatalogKey.TABLE_COLUMN_NAME) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_NAME, "Title,Action,Originator,Get Info,Type,NASA National Applications");

        if (params.getValue(CatalogKey.TABLE_COLUMN_EDITABLE) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_EDITABLE, "true,true,false,true,false,false");

        if (params.getValue(CatalogKey.TABLE_COLUMN_CELL_EDITOR) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_CELL_EDITOR,
                "," + ViewDataCellEditor.class.getName() +
                ",," + GetInfoCellEditor.class.getName() +
                ",,,");

        if (params.getValue(CatalogKey.TABLE_COLUMN_CELL_RENDERER) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_CELL_RENDERER,
                "," + ViewDataCellRenderer.class.getName() +
                ",," + GetInfoCellRenderer.class.getName() +
                "," + ServiceTypeCellRenderer.class.getName() +
                "," + NationalAppsCellRenderer.class.getName());

        if (params.getValue(CatalogKey.TABLE_COLUMN_PREFERRED_WIDTH) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_PREFERRED_WIDTH, "300,60,100,60,100,100");

        return params;
    }

    private static class QueryModelPropertyEvents implements PropertyChangeListener
    {
        private ESGCatalogPanel panel;

        private QueryModelPropertyEvents(ESGCatalogPanel panel)
        {
            this.panel = panel;
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt != null)
                if (this.panel != null)
                    this.panel.onQueryModelChanged(evt);
        }
    }

    private static class WorldWindowPropertyEvents implements PropertyChangeListener
    {
        private ESGCatalogPanel panel;

        private WorldWindowPropertyEvents(ESGCatalogPanel panel)
        {
            this.panel = panel;
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt != null)
                if (this.panel != null)
                    this.panel.onWorldWindowChanged(evt);
        }
    }
}
