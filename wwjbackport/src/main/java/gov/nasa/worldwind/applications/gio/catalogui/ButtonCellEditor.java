/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author dcollins
 * @version $Id: ButtonCellEditor.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ButtonCellEditor extends DefaultCellEditor
{
    protected JButton button;
    private Object value;

    public ButtonCellEditor()
    {
        super(new JCheckBox());
        this.button = new JButton();
        this.button.setOpaque(true);
        this.button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fireEditingStopped();
            }
        });
    }

    public JButton getButton()
    {
        return this.button;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        if (isSelected)
        {
            this.button.setForeground(table.getSelectionForeground());
            this.button.setBackground(table.getSelectionBackground());
        }
        else
        {
            this.button.setForeground(table.getForeground());
            this.button.setBackground(table.getBackground());
        }
        this.button.setText(value != null ? value.toString() : "");
        this.value = value;
        return this.button;
    }

    public Object getCellEditorValue()
    {
        return this.value;
    }
}