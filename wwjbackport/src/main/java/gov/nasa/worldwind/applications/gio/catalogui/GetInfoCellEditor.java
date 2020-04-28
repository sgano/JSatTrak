/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: GetInfoCellEditor.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GetInfoCellEditor extends IconButtonCellEditor
{
    private String iconPath;
    private static final String DEFAULT_ICON_PATH = "images/info-down.gif";

    public GetInfoCellEditor()
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

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        Component c = null;
        if (value != null)
        {
            updateIcon(getIconComponent());
            c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            updateButton(getCellEditor().getButton());
            updateCursor(c);
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
}
