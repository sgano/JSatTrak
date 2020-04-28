/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.applications.gio.catalogui.treetable.AbstractTreeTableNode;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.TreeTableNode;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: AVListNode.java 10867 2009-05-05 15:42:33Z dcollins $
 */
public class AVListNode<T extends AVList> extends AbstractTreeTableNode
{
    private T avList;
    private boolean isSortChildren;
    private String sortKey;

    public AVListNode(T avList)
    {
        if (avList == null)
        {
            String message = Logging.getMessage("nullValue.AVListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.avList = avList;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (this.avList == o)
            return true;
        if (o == null)
            return false;

        if (o instanceof AVListNode)
        {
            AVListNode that = (AVListNode) o;
            return this.avList.equals(that.avList);
        }
        else
        {
            return this.avList.equals(o);
        }
    }

    public int hashCode()
    {
        return this.avList.hashCode();
    }

    public T getObject()
    {
        return this.avList;
    }

    public Object getValue(String key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.avList.getValue(key);
    }

    public void setValue(String key, Object aValue)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.avList.setValue(key, aValue);
    }

    public boolean isSortChildren()
    {
        return this.isSortChildren;
    }

    public void setSortChildren(boolean sortChildren)
    {
        this.isSortChildren = sortChildren;
    }

    public String getSortKey()
    {
        return this.sortKey;
    }

    public void setSortKey(String sortKey)
    {
        this.sortKey = sortKey;
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        
        this.avList.firePropertyChange(propertyName, oldValue, newValue);
        
        TreeTableNode parent = getParent();
        if (parent != null && parent instanceof AVListNode)
            ((AVListNode) parent).firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(String propertyName)
    {
        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        firePropertyChange(propertyName, null, this.avList);
    }

    protected void doSetChildren(List<? extends AVListNode> children)
    {
        if (children != null)
            if (this.isSortChildren)
                Collections.sort(children, new KeyComparator(this.sortKey));
        super.setChildren(children);
    }

    public static class KeyComparator implements Comparator<AVListNode>
    {
        private String key;

        public KeyComparator(String key)
        {
            this.key = key;
        }

        public int compare(AVListNode n1, AVListNode n2)
        {
            if (this.key == null)
                return 0;

            Object o1 = n1 != null ? n1.getValue(this.key) : null;
            Object o2 = n2 != null ? n2.getValue(this.key) : null;
            String s1 = o1 != null ? o1.toString() : null;
            String s2 = o2 != null ? o2.toString() : null;

            // If s1 == null and s2 != null, then return that n1 is greater than n2. Otherwise both are null, and we
            // return 0.
            if (s1 == null)
            {
                return (s2 != null) ? 1 : 0;
            }
            // We know s1 != null, so return that n1 is less than n2.
            else if (s2 == null)
            {
                return -1;
            }
            // Both s1 and s2 are not null. Compare the two strings with a case in sensitive comparator.
            else
            {
                return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
            }
        }
    }
}
