/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui.treetable;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

/**
 * @author dcollins
 * @version $Id: TableModelAdapter.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class TableModelAdapter extends AbstractTableModel
{
    private TreeTableModel treeTableModel;
    private JTree tree;

    public TableModelAdapter(TreeTableModel treeTableModel, JTree tree)
    {
        if (treeTableModel == null)
        {
            String message = "nullValue.TreeTableModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (tree == null)
        {
            String message = "nullValue.TreeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.treeTableModel = treeTableModel;
        this.tree = tree;

        // Installs a TreeModelListener that can update the table when
        // the tree changes. We use delayedFireTableDataChanged as we can
        // not be guaranteed the tree will have finished processing
        // the event before us.
        treeTableModel.addTreeTableModelListener(new TreeTableModelListener()
        {
            public void treeTableNodesChanged(TreeTableModelEvent e)
            {
                delayedFireTableDataChanged();
            }

            public void treeTableNodesInserted(TreeTableModelEvent e)
            {
                delayedFireTableDataChanged();
            }

            public void treeTableNodesRemoved(TreeTableModelEvent e)
            {
                delayedFireTableDataChanged();
            }

            public void treeTableStructureChanged(TreeTableModelEvent e)
            {
                delayedFireTableStructureChanged();
            }

            public void treeTableHeaderChanged(TreeTableModelEvent e)
            {
                delayedFireTableStructureChanged();
            }
        });

        this.tree.addTreeExpansionListener(new TreeExpansionListener()
        {
            // Don't use fireTableRowsInserted() here; the selection model
            // would get updated twice.
            public void treeExpanded(TreeExpansionEvent event)
            {
                fireTableDataChanged();
            }

            public void treeCollapsed(TreeExpansionEvent event)
            {
                fireTableDataChanged();
            }
        });
    }

    public int getColumnCount()
    {
        return this.treeTableModel.getColumnCount();
    }

    public Class<?> getColumnClass(int columnIndex)
    {
        return this.treeTableModel.getColumnClass(columnIndex);
    }

    public String getColumnName(int columnIndex)
    {
        return this.treeTableModel.getColumnName(columnIndex);
    }

    public int getRowCount()
    {
        return this.tree.getRowCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return this.treeTableModel.getValueAt(nodeForRow(rowIndex), columnIndex);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        this.treeTableModel.setValueAt(aValue, nodeForRow(rowIndex), columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return this.treeTableModel.isCellEditable(nodeForRow(rowIndex), columnIndex);
    }

    protected Object nodeForRow(int rowIndex)
    {
        TreePath treePath = this.tree.getPathForRow(rowIndex);
        return treePath != null ? treePath.getLastPathComponent() : null;
    }

    protected void delayedFireTableDataChanged()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                fireTableDataChanged();
            }
        });
    }

    protected void delayedFireTableStructureChanged()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                fireTableStructureChanged();
            }
        });
    }
}
