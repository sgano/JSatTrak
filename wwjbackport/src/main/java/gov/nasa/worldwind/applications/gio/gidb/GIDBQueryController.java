/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.catalogui.QueryModel;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author dcollins
 * @version $Id: GIDBQueryController.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GIDBQueryController implements PropertyChangeListener, ActionListener
{
    private GIDBQueryPanel queryPanel;
    private boolean ignoreActionEvents = false;

    public GIDBQueryController(GIDBQueryPanel queryPanel)
    {
        if (queryPanel == null)
        {
            String message = "catalog.QueryPanelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.queryPanel = queryPanel;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        this.ignoreActionEvents = true;
        try
        {
            if (evt != null && evt.getPropertyName() != null)
            {
                String propertyName = evt.getPropertyName();
                Object newValue = evt.getNewValue();

                if (propertyName.equals(CatalogKey.KEYWORD_TEXT))
                {
                    setValue(this.queryPanel.getKeywordsBox(), newValue);
                }
            }
        }
        finally
        {
            this.ignoreActionEvents = false;
        }
    }

    public void actionPerformed(ActionEvent event)
    {
        if (!this.ignoreActionEvents)
        {
            QueryModel model = this.queryPanel.getModel();
            if (event != null && event.getActionCommand() != null && model != null)
            {
                String actionCommand = event.getActionCommand();
                if (actionCommand.equals(CatalogKey.ACTION_COMMAND_QUERY))
                {
                    model.firePropertyChange(CatalogKey.ACTION_COMMAND_QUERY, null, model);
                }
                else if (actionCommand.equals(CatalogKey.KEYWORD_TEXT))
                {
                    Object obj = this.queryPanel.getKeywordsBox().getSelectedItem();
                    model.setValue(CatalogKey.KEYWORD_TEXT, obj != null ? obj.toString() : null);
                }
            }
        }
    }

    public void synchronizeView()
    {
        this.ignoreActionEvents = true;
        try
        {
            QueryModel model = this.queryPanel.getModel();
            if (model != null)
            {
                // Assign view components values from model.
                String s = model.getStringValue(CatalogKey.KEYWORD_TEXT);
                setValue(this.queryPanel.getKeywordsBox(), s);
            }
        }
        finally
        {
            this.ignoreActionEvents = false;
        }
    }

    private void setValue(JComboBox b, Object value)
    {
        if (b != null)
        {
            b.setSelectedItem(value);
        }
    }
}
