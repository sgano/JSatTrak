/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.applications.gio.catalogui.*;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.TreeTableModel;
import gov.nasa.worldwind.applications.gio.gidb.*;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author dcollins
 * @version $Id: GIDBCatalogPanel.java 5552 2008-07-19 01:38:46Z dcollins $
 */
public class GIDBCatalogPanel extends JPanel
{
    private String service;
    private GIDBController controller;
    private GIDBCatalogModel model;
    private GIDBQueryPanel queryPanel;
    private DefaultResultPanel resultPanel;
    private WorldWindow wwd;
    private static final String DEFAULT_SERVICE = "http://columbo.nrlssc.navy.mil/ogcwms/servlet/WMSServlet?REQUEST=XMLServerList";

    public GIDBCatalogPanel()
    {
        this.service = DEFAULT_SERVICE;

        this.model = new GIDBCatalogModel();
        this.controller = new GIDBController(this);
        this.model.addPropertyChangeListener(this.controller);
        
        makeComponents();
        layoutComponents();
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
            String message = "gidb.InvalidService";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
    }

    public int getThreadPoolSize()
    {
        return this.controller.getThreadPoolSize();
    }

    public void setThreadPoolSize(int size)
    {
        this.controller.setThreadPoolSize(size);
    }

    public GIDBCatalogModel getModel()
    {
        return this.model;
    }

    public GIDBController getController()
    {
        return this.controller;
    }

    public QueryModel getQueryModel()
    {
        return this.model.getQueryModel();
    }

    public ResultList getResultModel()
    {
        return this.model.getResultModel();
    }

    public GIDBQueryPanel getQueryPanel()
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
        this.wwd = wwd;
    }

    private void makeComponents()
    {
        this.queryPanel = new GIDBQueryPanel(this.model.getQueryModel());

        AVList tableParams = initTableParams(new AVListImpl());
        JComponent resultComponent = new GIDBTreeTable(this.model.getResultModel(), tableParams);
        this.resultPanel = new DefaultResultPanel();
        this.resultPanel.setResultComponent(resultComponent);
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
            params.setValue(CatalogKey.TABLE_COLUMN_COUNT, "3");

        if (params.getValue(CatalogKey.TABLE_COLUMN_PROPERTY_KEY) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_PROPERTY_KEY,
                GIDBResultNode.class.getName() + "," + CatalogKey.TITLE + "," + CatalogKey.ACTION_COMMAND_BROWSE + "," + CatalogKey.URI +
                "," + LayerNode.class.getName() + "," + CatalogKey.TITLE + "," + GIDBKey.ACTION_COMMAND_LAYER_PRESSED + "," + CatalogKey.DESCRIPTION);

        if (params.getValue(CatalogKey.TABLE_COLUMN_CLASS) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_CLASS, TreeTableModel.class.getName() + ",,");

        if (params.getValue(CatalogKey.TABLE_COLUMN_NAME) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_NAME, "Title,Action,URL");

        if (params.getValue(CatalogKey.TABLE_COLUMN_EDITABLE) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_EDITABLE, "true,true,false");

        if (params.getValue(CatalogKey.TABLE_COLUMN_CELL_EDITOR) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_CELL_EDITOR, 
                "," + ViewDataCellEditor.class.getName() +
                ",");

        if (params.getValue(CatalogKey.TABLE_COLUMN_CELL_RENDERER) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_CELL_RENDERER,
                "," + ViewDataCellRenderer.class.getName() +
                ",");

        if (params.getValue(CatalogKey.TABLE_COLUMN_PREFERRED_WIDTH) == null)
            params.setValue(CatalogKey.TABLE_COLUMN_PREFERRED_WIDTH, "200,60,300");

        return params;
    }
}
