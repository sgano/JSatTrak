/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.ToolTipSupport;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: NationalAppsCellRenderer.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class NationalAppsCellRenderer extends DefaultTableCellRenderer
{
    private final ToolTipSupport toolTipSupport = new ToolTipSupport();

    public NationalAppsCellRenderer()
    {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        updateText(c, value);
        updateToolTip(c, value);
        return c;
    }

    protected void updateText(Component c, Object value)
    {
        String text = null;
        if (value != null && value instanceof String[])
        {
            StringBuilder sb = new StringBuilder();
            for (String s : (String[]) value)
            {
                if (s != null)
                {
                    s = s.trim();
                    if (s.length() > 0)
                    {
                        if (sb.length() > 0)
                            sb.append(",");
                        sb.append(s);
                    }
                }
            }
            if (sb.length() > 0)
                text = sb.toString();
        }

        if (c != null && c instanceof JLabel)
            ((JLabel) c).setText(text);
    }

    protected void updateToolTip(Component c, Object value)
    {
        this.toolTipSupport.clear();
        if (value != null && value instanceof String[])
        {
            this.toolTipSupport.append((String[]) value, Font.BOLD);
        }

        if (c != null && c instanceof JComponent)
            ((JComponent) c).setToolTipText(this.toolTipSupport.getText());
    }
}
