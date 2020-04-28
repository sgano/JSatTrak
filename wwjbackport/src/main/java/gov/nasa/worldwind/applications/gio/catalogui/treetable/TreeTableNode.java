/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui.treetable;

/**
 * @author dcollins
 * @version $Id: TreeTableNode.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface TreeTableNode
{
    int getChildCount();

    TreeTableNode getChildAt(int index);

    int getIndex(TreeTableNode node);

    Iterable<TreeTableNode> getChildren();

    TreeTableNode getParent();

    boolean isAllowsChildren();

    boolean isLeaf();

    Object getValue(String key);

    void setValue(String key, Object aValue);
}
