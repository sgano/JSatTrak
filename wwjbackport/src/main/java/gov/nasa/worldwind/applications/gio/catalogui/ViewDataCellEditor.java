/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.avlist.AVList;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: ViewDataCellEditor.java 5552 2008-07-19 01:38:46Z dcollins $
 */
public class ViewDataCellEditor extends IconButtonCellEditor
{
    private String layerErrorIconPath;
    private String layerInstalledIconPath;
    private String layerReadyIconPath;
    private String linkIconPath;
    private static final String DEFAULT_LAYER_ERROR_PATH = "images/layer-error.gif";
    private static final String DEFAULT_LAYER_INSTALLED_ICON_PATH = "images/layer-installed-down.gif";
    private static final String DEFAULT_LAYER_READY_ICON_PATH = "images/layer-ready-down.gif";
    private static final String DEFAULT_LINK_ICON_PATH = "images/link-down.gif";

    public ViewDataCellEditor()
    {
        this.layerErrorIconPath = DEFAULT_LAYER_ERROR_PATH;
        this.layerInstalledIconPath = DEFAULT_LAYER_INSTALLED_ICON_PATH;
        this.layerReadyIconPath = DEFAULT_LAYER_READY_ICON_PATH;
        this.linkIconPath = DEFAULT_LINK_ICON_PATH;
    }

    public String getLayerErrorIconPath()
    {
        return this.layerErrorIconPath;
    }

    public void setLayerErrorIconPath(String layerErrorIconPath)
    {
        this.layerErrorIconPath = layerErrorIconPath;
    }

    public String getLayerInstalledIconPath()
    {
        return this.layerInstalledIconPath;
    }

    public void setLayerInstalledIconPath(String path)
    {
        this.layerInstalledIconPath = path;
    }

    public String getLayerReadyIconPath()
    {
        return this.layerReadyIconPath;
    }

    public void setLayerReadyIconPath(String path)
    {
        this.layerReadyIconPath = path;
    }

    public String getLinkIconPath()
    {
        return this.linkIconPath;
    }

    public void setLinkIconPath(String path)
    {
        this.linkIconPath = path;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        Component c = null;
        if (value != null)
        {
            updateIcon(getIconComponent(), value);
            c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            updateButton(getCellEditor().getButton());
            updateCursor(c);
        }
        return c;
    }

    protected void updateIcon(IconComponent ic, Object value)
    {
        if (ic != null)
        {
            String iconPath = null;

            if (value != null && value instanceof AVList)
            {
                AVList params = (AVList) value;
                Object o = params.getValue(CatalogKey.URI);
                if (o != null)
                {
                    iconPath = this.linkIconPath;
                }
                
                o = params.getValue(CatalogKey.LAYER_STATE);
                if (o != null)
                {
                    if (CatalogKey.LAYER_STATE_INSTALLED.equals(o))
                        iconPath = this.layerInstalledIconPath;
                    else if (CatalogKey.LAYER_STATE_READY.equals(o))
                        iconPath = this.layerReadyIconPath;
                    else if (CatalogKey.LAYER_STATE_ERROR.equals(o))
                        iconPath = this.layerErrorIconPath;
                }

                o = params.getValue(CatalogKey.EXCEPTIONS);
                if (o != null && o instanceof CatalogExceptionList)
                {
                    iconPath = this.layerErrorIconPath;
                }
            }

            ic.setIconPath(iconPath);
        }
    }

    protected void updateButton(JButton button)
    {
        if (button != null)
        {
            button.setText(null);
        }
    }

    protected void updateCursor(Component c)
    {
        if (c != null)
        {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            if (cursor != null)
                c.setCursor(cursor);
        }
    }
}
