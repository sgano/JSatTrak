/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: ServiceTypeCellRenderer.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ServiceTypeCellRenderer extends DefaultTableCellRenderer
{
    private final ToolTipSupport toolTipSupport = new ToolTipSupport();
    public static final String SERVICE_TYPE_ERROR_TEXT = "Unknown";
    public static final String SERVICE_TYPE_WMS_TEXT = "Web Map Service";
    public static final String SERVICE_TYPE_WFS_TEXT = "Web Feature Service";
    public static final String SERVICE_TYPE_WCS_TEXT = "Web Coverage Service";

    public ServiceTypeCellRenderer()
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
        String text = valueToText(value);

        if (c != null && c instanceof JLabel)
            ((JLabel) c).setText(text);
    }

    protected void updateToolTip(Component c, Object value)
    {
        this.toolTipSupport.clear();

        String text = valueToText(value);
        if (text != null)
            this.toolTipSupport.append(text, Font.BOLD);

        if (c != null && c instanceof JComponent)
            ((JComponent) c).setToolTipText(this.toolTipSupport.getText());
    }

    protected String valueToText(Object o)
    {
        String text = null;
        if (o != null)
        {
            if (o.equals(CatalogKey.WMS))
                text = SERVICE_TYPE_WMS_TEXT;
            else if (o.equals(CatalogKey.WFS))
                text = SERVICE_TYPE_WFS_TEXT;
            else if (o.equals(CatalogKey.WCS))
                text = SERVICE_TYPE_WCS_TEXT;
            else if (o.equals(CatalogKey.ERROR))
                text = SERVICE_TYPE_ERROR_TEXT;
        }
        return text;
    }
}
