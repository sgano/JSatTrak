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
 * @version $Id: ViewDataCellRenderer.java 5552 2008-07-19 01:38:46Z dcollins $
 */
public class ViewDataCellRenderer extends IconButtonCellRenderer
{
    private String layerErrorIconPath;
    private String layerInstalledIconPath;
    private String layerReadyIconPath;
    private String linkIconPath;
    private final ToolTipSupport toolTipSupport = new ToolTipSupport();
    private static final String DEFAULT_LAYER_ERROR_PATH = "images/layer-error.gif";
    private static final String DEFAULT_LAYER_INSTALLED_PATH = "images/layer-installed-up.gif";
    private static final String DEFAULT_LAYER_READY_ICON_PATH = "images/layer-ready-up.gif";
    private static final String DEFAULT_URI_ICON_PATH = "images/link-up.gif";

    public ViewDataCellRenderer()
    {
        this.layerErrorIconPath = DEFAULT_LAYER_ERROR_PATH;
        this.layerInstalledIconPath = DEFAULT_LAYER_INSTALLED_PATH;
        this.layerReadyIconPath = DEFAULT_LAYER_READY_ICON_PATH;
        this.linkIconPath = DEFAULT_URI_ICON_PATH;
    }

    public String getLayerErrorIconPath()
    {
        return this.layerErrorIconPath;
    }

    public void setLayerErrorIconPath(String layerErrorIconPath)
    {
        this.layerErrorIconPath = layerErrorIconPath;
    }

    public String getLayerReadyIconPath()
    {
        return this.layerReadyIconPath;
    }

    public void setLayerReadyIconPath(String layerReadyIconPath)
    {
        this.layerReadyIconPath = layerReadyIconPath;
    }

    public String getLayerInstalledIconPath()
    {
        return this.layerInstalledIconPath;
    }

    public void setLayerInstalledIconPath(String layerInstalledIconPath)
    {
        this.layerInstalledIconPath = layerInstalledIconPath;
    }

    public String getLinkIconPath()
    {
        return this.linkIconPath;
    }

    public void setLinkIconPath(String linkIconPath)
    {
        this.linkIconPath = linkIconPath;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        Component c = null;
        if (value != null)
        {
            updateIcon(getIconComponent(), value);
            c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            updateButton(getCellRenderer());
            updateCursor(c);
            updateToolTip(c, value);
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
                    if (o.equals(CatalogKey.LAYER_STATE_READY))
                        iconPath = this.layerReadyIconPath;
                    else if (o.equals(CatalogKey.LAYER_STATE_INSTALLED))
                        iconPath = this.layerInstalledIconPath;
                    else if (o.equals(CatalogKey.LAYER_STATE_ERROR))
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

    protected void updateToolTip(Component c, Object value)
    {
        this.toolTipSupport.clear();

        if (value != null && value instanceof AVList)
        {
            AVList params = (AVList) value;

            Object o = params.getValue(CatalogKey.URI);
            if (o != null)
            {
                this.toolTipSupport.append("Open in Browser", Font.BOLD);
                this.toolTipSupport.appendLine();
                this.toolTipSupport.append(o.toString());
            }

            o = params.getValue(CatalogKey.LAYER_STATE);
            if (o != null)
            {
                if (CatalogKey.LAYER_STATE_READY.equals(o))
                    this.toolTipSupport.append("Add to World Wind", Font.BOLD);
                else if (CatalogKey.LAYER_STATE_INSTALLED.equals(o))
                    this.toolTipSupport.append("Remove from World Wind", Font.BOLD);

                o = params.getValue(CatalogKey.TITLE);
                if (o != null)
                {
                    if (this.toolTipSupport.hasText())
                        this.toolTipSupport.appendLine();
                    this.toolTipSupport.append(o.toString(), Font.ITALIC);
                }

                o = params.getValue(CatalogKey.DESCRIPTION);
                if (o != null)
                {
                    if (this.toolTipSupport.hasText())
                        this.toolTipSupport.appendParagraph();
                    this.toolTipSupport.appendWrapped(o.toString());
                }
            }

            o = params.getValue(CatalogKey.EXCEPTIONS);
            if (o != null && o instanceof CatalogExceptionList)
            {
                if (this.toolTipSupport.hasText())
                    this.toolTipSupport.appendParagraph();
                CatalogExceptionList list = (CatalogExceptionList) o;
                this.toolTipSupport.append(list);
            }
        }

        if (c != null && c instanceof JComponent)
            ((JComponent) c).setToolTipText(this.toolTipSupport.getText());
    }
}
