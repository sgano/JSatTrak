/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;

/**
 * @author dcollins
 * @version $Id: IconButtonCellEditor.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class IconButtonCellEditor implements TableCellEditor
{
    private IconComponent iconComponent;
    private ButtonCellEditor cellEditor;

    public IconButtonCellEditor()
    {
        this.iconComponent = new IconComponent();
        this.cellEditor = new ButtonCellEditor();
    }

    public IconComponent getIconComponent()
    {
        return this.iconComponent;
    }

    public ButtonCellEditor getCellEditor()
    {
        return this.cellEditor;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        this.cellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
        JButton button = this.cellEditor.getButton();
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setIcon(this.iconComponent.getIcon());
        return button;
    }

    public Object getCellEditorValue()
    {
        return this.cellEditor.getCellEditorValue();
    }

    public boolean isCellEditable(EventObject anEvent)
    {
        return this.cellEditor.isCellEditable(anEvent);
    }

    public boolean shouldSelectCell(EventObject anEvent)
    {
        return this.cellEditor.shouldSelectCell(anEvent);
    }

    public boolean stopCellEditing()
    {
        return this.cellEditor.stopCellEditing();
    }

    public void cancelCellEditing()
    {
        this.cellEditor.cancelCellEditing();
    }

    public void addCellEditorListener(CellEditorListener l)
    {
        this.cellEditor.addCellEditorListener(l);
    }

    public void removeCellEditorListener(CellEditorListener l)
    {
        this.cellEditor.removeCellEditorListener(l);
    }
}