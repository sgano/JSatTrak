/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui.treetable;

/**
 * @author dcollins
 * @version $Id: TreeTableModel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface TreeTableModel
{
    Object getRoot();

    int getChildCount(Object parent);

    Object getChild(Object parent, int index);

    int getIndexOfChild(Object parent, Object child);

    boolean isLeaf(Object node);

    int getColumnCount();

    Class<?> getColumnClass(int columnIndex);

    String getColumnName(int columnIndex);

    Object getValueAt(Object node, int columnIndex);

    void setValueAt(Object aValue, Object node, int columnIndex);

    boolean isCellEditable(Object node, int columnIndex);

    void addTreeTableModelListener(TreeTableModelListener l);

    void removeTreeTableModelListener(TreeTableModelListener l);
}
