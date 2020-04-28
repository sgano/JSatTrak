/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui.treetable;

import gov.nasa.worldwind.util.Logging;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;

/**
 * @author dcollins
 * @version $Id: AbstractTreeTableModel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public abstract class AbstractTreeTableModel implements TreeTableModel
{
    private TreeTableNode root;
    private boolean asksAllowsChildren;
    private EventListenerList listenerList = new EventListenerList();

    public AbstractTreeTableModel(TreeTableNode root, boolean asksAllowsChildren)
    {
        if (root == null)
        {
            String message = "nullValue.RootIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.root = root;
        this.asksAllowsChildren = asksAllowsChildren;
    }

    public AbstractTreeTableModel()
    {
        this.root = null;
        this.asksAllowsChildren = false;
    }

    public boolean isAsksAllowsChildren()
    {
        return this.asksAllowsChildren;
    }

    public void setAsksAllowsChildren(boolean asksAllowsChildren)
    {
        this.asksAllowsChildren = asksAllowsChildren;
    }

    public Object getRoot()
    {
        return this.root;
    }

    public void setRoot(TreeTableNode root)
    {
        this.root = root;
    }

    public int getChildCount(Object parent)
    {
        int count = 0;
        if (parent != null && parent instanceof TreeTableNode)
            count = ((TreeTableNode) parent).getChildCount();
        return count;
    }

    public Object getChild(Object parent, int index)
    {
        Object child = null;
        if (parent != null && parent instanceof TreeTableNode)
            child = ((TreeTableNode) parent).getChildAt(index);
        return child;
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        int index = -1;
        if (parent != null && child != null && parent instanceof TreeTableNode && child instanceof TreeTableNode)
            index = ((TreeTableNode) parent).getIndex((TreeTableNode) child);
        return index;
    }

    public boolean isLeaf(Object node)
    {
        boolean leaf = false;
        if (node != null && node instanceof TreeTableNode)
        {
            if (this.asksAllowsChildren)
                leaf = !((TreeTableNode) node).isAllowsChildren();
            else
                leaf = ((TreeTableNode) node).isLeaf();
        }
        return leaf;
    }

    // getColumnCount() left to subclass

    // getColumnClass() left to subclass

    public String getColumnName(int columnIndex)
    {
        String result = "";
        for (; columnIndex >= 0; columnIndex = columnIndex / 26 - 1)
        {
            result = (char) ((char) (columnIndex % 26) + 'A') + result;
        }
        return result;
    }

    // getValueAt() left to subclass

    // setValueAt() left to subclass

    // isCellEditable() left to subclass

    public void reload()
    {
        reload(this.root);
    }

    public void reload(TreeTableNode node)
    {
        if (node != null)
            fireTreeTableStructureChanged(this, getPathToRoot(node), null, null);
    }

    public void nodeChanged(TreeTableNode node)
    {
        if (node != null)
        {
            TreeTableNode parent = node.getParent();
            if (parent != null)
            {
                int index = parent.getIndex(node);
                if (index != -1)
                {
                    int[] childIndices = new int[1];
                    childIndices[0] = index;
                    nodesChanged(parent, childIndices);
                }
            }
            else if (node == getRoot())
            {
                nodesChanged(node, null);
            }
        }
    }

    public void nodesChanged(TreeTableNode node, int[] childIndices)
    {
        if (node != null)
        {
            if (childIndices != null)
            {
                int numChildren = childIndices.length;
                if (numChildren > 0)
                {
                    Object[] children = new Object[numChildren];
                    for (int i = 0; i < numChildren; i++)
                        children[i] = node.getChildAt(childIndices[i]);
                    fireTreeTableNodesChanged(this, getPathToRoot(node), childIndices, children);
                }
            }
            else if (node == getRoot())
            {
                fireTreeTableNodesChanged(this, getPathToRoot(node), null, null);
            }
        }
    }

    public TreeTableNode[] getPathToRoot(TreeTableNode aNode)
    {
        return getPathToRoot(aNode, 0);
    }

    protected TreeTableNode[] getPathToRoot(TreeTableNode aNode, int depth)
    {
        TreeTableNode[] retNodes;
        // This method recurses, traversing towards the root in order
        // size the array. On the way back, it fills in the nodes,
        // starting from the root and working back to the original node.

        // Check for null, in case someone passed in a null node, or
        // they passed in an element that isn't rooted at root.
        if (aNode == null)
        {
            if (depth == 0)
                return null;
            else
                retNodes = new TreeTableNode[depth];
        }
        else
        {
            depth++;
            if (aNode == this.root)
                retNodes = new TreeTableNode[depth];
            else
                retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }

    public TreeTableNode[] getPathForValue(Object value)
    {
        TreeTableNode aNode = null;
        Object root = getRoot();
        if (root != null && root instanceof TreeTableNode)
            aNode = getNodeForValue(value, (TreeTableNode) root);
        return aNode != null ? getPathToRoot(aNode) : null;
    }

    public TreeTableNode getNodeForValue(Object value)
    {
        TreeTableNode aNode = null;
        Object root = getRoot();
        if (root != null && root instanceof TreeTableNode)
            aNode = getNodeForValue(value, (TreeTableNode) root);
        return aNode;
    }

    protected TreeTableNode getNodeForValue(Object value, TreeTableNode aNode)
    {
        TreeTableNode retNode = null;
        if (aNode != null)
        {
            if (aNode.equals(value))
            {
                retNode = aNode;
            }
            else
            {
                Iterable<TreeTableNode> children = aNode.getChildren();
                if (children != null)
                {
                    for (TreeTableNode child : children)
                    {
                        retNode = getNodeForValue(value, child);
                        if (retNode != null)
                            break;
                    }
                }
            }
        }
        return retNode;
    }

    public void addTreeTableModelListener(TreeTableModelListener l)
    {
        this.listenerList.add(TreeTableModelListener.class, l);
    }

    public void removeTreeTableModelListener(TreeTableModelListener l)
    {
        this.listenerList.remove(TreeTableModelListener.class, l);
    }

    public TableModelListener[] getTableModelListeners()
    {
        return this.listenerList.getListeners(TableModelListener.class);
    }

    protected void fireTreeTableNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeTableModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeTableModelListener.class)
            {
                if (e == null)
                    e = new TreeTableModelEvent(source, path, childIndices, children);
                ((TreeTableModelListener) listeners[i + 1]).treeTableNodesChanged(e);
            }
        }
    }

    protected void fireTreeTableNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeTableModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeTableModelListener.class)
            {
                if (e == null)
                    e = new TreeTableModelEvent(source, path, childIndices, children);
                ((TreeTableModelListener) listeners[i + 1]).treeTableNodesInserted(e);
            }
        }
    }

    protected void fireTreeTableNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeTableModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeTableModelListener.class)
            {
                if (e == null)
                    e = new TreeTableModelEvent(source, path, childIndices, children);
                ((TreeTableModelListener) listeners[i + 1]).treeTableNodesRemoved(e);
            }
        }
    }

    protected void fireTreeTableStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeTableModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeTableModelListener.class)
            {
                if (e == null)
                    e = new TreeTableModelEvent(source, path, childIndices, children);
                ((TreeTableModelListener) listeners[i + 1]).treeTableStructureChanged(e);
            }
        }
    }

    protected void fireTreeTableHeaderChanged(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeTableModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeTableModelListener.class)
            {
                if (e == null)
                    e = new TreeTableModelEvent(source, path, childIndices, children);
                ((TreeTableModelListener) listeners[i + 1]).treeTableStructureChanged(e);
            }
        }
    }
}
