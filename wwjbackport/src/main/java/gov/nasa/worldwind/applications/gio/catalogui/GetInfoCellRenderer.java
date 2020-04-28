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
 * @version $Id: GetInfoCellRenderer.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GetInfoCellRenderer extends IconButtonCellRenderer
{
    private String iconPath;
    private final ToolTipSupport toolTipSupport = new ToolTipSupport();
    private static final String DEFAULT_ICON_PATH = "images/info-up.gif";

    public GetInfoCellRenderer()
    {
        this.iconPath = DEFAULT_ICON_PATH;
    }

    public String getIconPath()
    {
        return this.iconPath;
    }

    public void setIconPath(String iconPath)
    {
        this.iconPath = iconPath;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        Component c = null;
        if (value != null)
        {
            updateIcon(getIconComponent());
            c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            updateButton(getCellRenderer());
            updateCursor(c);
            updateToolTip(c, value);
        }
        return c;
    }

    protected void updateIcon(IconComponent ic)
    {
        if (ic != null)
        {
            ic.setIconPath(this.iconPath);
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
        this.toolTipSupport.append("Get Info", Font.BOLD);

        if (value != null && value instanceof AVList)
        {
            AVList params = (AVList) value;
            
            String s = params.getStringValue(CatalogKey.TITLE);
            if (s != null)
            {
                this.toolTipSupport.appendLine();
                this.toolTipSupport.append(s);
            }
        }

        if (c != null && c instanceof JComponent)
            ((JComponent) c).setToolTipText(this.toolTipSupport.getText());
    }
}
