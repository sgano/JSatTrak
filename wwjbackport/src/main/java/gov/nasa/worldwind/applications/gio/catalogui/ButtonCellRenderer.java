/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: ButtonCellRenderer.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ButtonCellRenderer extends JButton implements TableCellRenderer
{
    public ButtonCellRenderer()
    {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        if (isSelected)
        {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        }
        else
        {
            setForeground(table.getForeground());
            setBackground(UIManager.getColor("Button.background"));
        }

        setText(value != null ? value.toString() : "");

        return this;
    }
}
