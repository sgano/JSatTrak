/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui.treetable;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: AbstractTreeTableNode.java 5551 2008-07-18 23:50:47Z dcollins $
 */
public abstract class AbstractTreeTableNode implements TreeTableNode
{
    private TreeTableNode[] children;
    private Map<TreeTableNode, Integer> childIndexMap;
    private TreeTableNode parent;
    private boolean allowsChildren;
    private boolean leaf;

    public AbstractTreeTableNode()
    {
        this.children = null;
        this.childIndexMap = null;
        this.parent = null;
        this.allowsChildren = false;
    }

    public int getChildCount()
    {
        return this.children != null ? this.children.length : 0;
    }

    public TreeTableNode getChildAt(int index)
    {
        return (this.children != null && index >= 0 && index < this.children.length) ? this.children[index] : null;
    }

    public int getIndex(TreeTableNode node)
    {
        int index = -1;
        if (this.childIndexMap != null)
        {
            Integer i = this.childIndexMap.get(node);
            if (i != null)
                index = i;
        }
        else if (this.children != null)
        {
            for (int i = 0; i < this.children.length; i++)
                if (this.children[i] != null && this.children[i].equals(node))
                    index = i;
        }
        return index;
    }

    public Iterable<TreeTableNode> getChildren()
    {
        return this.children != null ? Arrays.asList(this.children) : null;
    }

    public void setChildren(Collection<? extends TreeTableNode> newChildren)
    {
        if (this.children != null)
            for (TreeTableNode node : this.children)
                if (node != null && node instanceof AbstractTreeTableNode)
                    ((AbstractTreeTableNode) node).setParent(null);

        this.children = null;
        if (newChildren != null)
        {
            int newSize = newChildren.size();
            this.children = new TreeTableNode[newSize];
            this.childIndexMap = new HashMap<TreeTableNode, Integer>(newSize);

            int index = 0;
            for (TreeTableNode node : newChildren)
            {
                this.children[index] = node;
                this.childIndexMap.put(node, index);
                if (node != null && node instanceof AbstractTreeTableNode)
                    ((AbstractTreeTableNode) node).setParent(this);
                index++;
            }
        }
    }

    public TreeTableNode getParent()
    {
        return this.parent;
    }

    public void setParent(TreeTableNode parent)
    {
        this.parent = parent;
    }

    public boolean isAllowsChildren()
    {
        return this.allowsChildren;
    }

    public void setAllowsChildren(boolean allowsChildren)
    {
        this.allowsChildren = allowsChildren;
    }

    public boolean isLeaf()
    {
        return this.leaf;
    }

    public void setLeaf(boolean leaf)
    {
        this.leaf = leaf;
    }

    public void setValue(String key, Object aValue)
    {
    }

    public void update()
    {
    }
}
