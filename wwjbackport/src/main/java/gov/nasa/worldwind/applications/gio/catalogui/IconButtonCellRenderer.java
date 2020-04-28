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
 * @version $Id: IconButtonCellRenderer.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class IconButtonCellRenderer implements TableCellRenderer
{
    private IconComponent iconComponent;
    private ButtonCellRenderer cellRenderer;

    public IconButtonCellRenderer()
    {
        this.iconComponent = new IconComponent();
        this.cellRenderer = new ButtonCellRenderer();
    }

    public IconComponent getIconComponent()
    {
        return this.iconComponent;
    }

    public ButtonCellRenderer getCellRenderer()
    {
        return this.cellRenderer;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        this.cellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.cellRenderer.setContentAreaFilled(false);
        this.cellRenderer.setBorderPainted(false);
        this.cellRenderer.setFocusPainted(false);
        this.cellRenderer.setIcon(this.iconComponent.getIcon());
        return this.cellRenderer;
    }
}