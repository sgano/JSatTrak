/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui.treetable;

import gov.nasa.worldwind.util.Logging;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author dcollins
 * @version $Id: TreeModelAdapter.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class TreeModelAdapter implements TreeModel
{
    private TreeTableModel treeTableModel;
    private EventListenerList listenerList = new EventListenerList();

    public TreeModelAdapter(TreeTableModel treeTableModel)
    {
        if (treeTableModel == null)
        {
            String message = "nullValue.TreeTableModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        this.treeTableModel = treeTableModel;
        this.treeTableModel.addTreeTableModelListener(new TreeTableModelListener()
        {
            public void treeTableNodesChanged(TreeTableModelEvent e)
            {
                fireTreeNodesChanged(e.getPath(), e.getChildIndices(), e.getChildren());
            }

            public void treeTableNodesInserted(TreeTableModelEvent e)
            {
                fireTreeNodesInserted(e.getPath(), e.getChildIndices(), e.getChildren());
            }

            public void treeTableNodesRemoved(TreeTableModelEvent e)
            {
                fireTreeNodesRemoved(e.getPath(), e.getChildIndices(), e.getChildren());
            }

            public void treeTableStructureChanged(TreeTableModelEvent e)
            {
                fireTreeStructureChanged(e.getPath(), e.getChildIndices(), e.getChildren());
            }

            public void treeTableHeaderChanged(TreeTableModelEvent e)
            {
                // Tree did not change.
            }
        });
    }

    public Object getRoot()
    {
        return this.treeTableModel.getRoot();
    }

    public int getChildCount(Object parent)
    {
        return this.treeTableModel.getChildCount(parent);
    }

    public Object getChild(Object parent, int index)
    {
        return this.treeTableModel.getChild(parent, index);
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        return this.treeTableModel.getIndexOfChild(parent, child);
    }

    public boolean isLeaf(Object node)
    {
        return this.treeTableModel.isLeaf(node);
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }

    public void addTreeModelListener(TreeModelListener l)
    {
        this.listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
        this.listenerList.remove(TreeModelListener.class, l);
    }

    protected void fireTreeNodesChanged(Object[] path, int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                if (e == null)
                    e = new TreeModelEvent(this, path, childIndices, children);
                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    protected void fireTreeNodesInserted(Object[] path, int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                if (e == null)
                    e = new TreeModelEvent(this, path, childIndices, children);
                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }

    protected void fireTreeNodesRemoved(Object[] path, int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                if (e == null)
                    e = new TreeModelEvent(this, path, childIndices, children);
                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    protected void fireTreeStructureChanged(Object[] path, int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                if (e == null)
                    e = new TreeModelEvent(this, path, childIndices, children);
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }
}
