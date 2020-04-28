/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.applications.gio.catalogui.treetable.AbstractTreeTableModel;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.TreeTableNode;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: TreeTableModelAdapter.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class TreeTableModelAdapter extends AbstractTreeTableModel
{
    private AVList params;

    public TreeTableModelAdapter(AVList params)
    {
        if (params == null)
        {
            String message = "nullValue.ParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.params = params;

        setAsksAllowsChildren(true);
    }

    public int getColumnCount()
    {
        Object o = this.params.getValue(CatalogKey.TABLE_COLUMN_COUNT);
        return o != null && o instanceof Integer ? (Integer) o : 0;
    }

    public String getColumnName(int columnIndex)
    {
        Object o = getValueForColumn(CatalogKey.TABLE_COLUMN_NAME, columnIndex);
        return o != null ? o.toString() : null;
    }

    public Class<?> getColumnClass(int columnIndex)
    {
        Object o = getValueForColumn(CatalogKey.TABLE_COLUMN_CLASS, columnIndex);
        return o != null && o instanceof Class<?> ? (Class<?>) o : Object.class;
    }

    public Object getValueAt(Object node, int columnIndex)
    {
        Object value = null;
        if (node != null && node instanceof TreeTableNode)
        {
            Object propertyKey = getPropertyKey(node, columnIndex);
            if (propertyKey != null)
                value = ((TreeTableNode) node).getValue(propertyKey.toString());
        }
        return value;
    }

    public void setValueAt(Object aValue, Object node, int columnIndex)
    {
        Object propertyKey = getPropertyKey(node, columnIndex);
        if (propertyKey != null)
        {
            if (node != null)
            {
                if (node instanceof AVListNode)
                    ((AVListNode) node).firePropertyChange(propertyKey.toString());
                else if (node instanceof TreeTableNode)
                    ((TreeTableNode) node).setValue(propertyKey.toString(), aValue);
            }
        }
    }

    public boolean isCellEditable(Object node, int columnIndex)
    {
        Object o = getValueForColumn(CatalogKey.TABLE_COLUMN_EDITABLE, columnIndex);
        return o != null && o instanceof Boolean ? (Boolean) o : false;
    }

    private Object getValueForColumn(String key, int columnIndex)
    {
        Object[] values = null;
        Object o = this.params.getValue(key);
        if (o != null && o instanceof Object[])
            values = (Object[]) o;
        else if (o != null)
            values = o.toString().split(",");
        return values != null && columnIndex >= 0 && columnIndex < values.length ? values[columnIndex] : null;
    }

    private Object getPropertyKey(Object node, int columnIndex)
    {
        Object propertyKey = null;
        if (node != null)
        {
            Object o = this.params.getValue(CatalogKey.TABLE_COLUMN_PROPERTY_KEY);
            if (o != null && o instanceof Object[])
            {
                Class<?> cls = node.getClass();
                Object[] values = (Object[]) o;

                int i;
                for (i = 0; i < values.length; i++)
                    if (cls.equals(values[i]))
                        break;

                int index = 1 + i + columnIndex;
                if (index < values.length)
                    if (values[index] != null)
                        propertyKey = values[index];
            }
        }
        return propertyKey;
    }
}
