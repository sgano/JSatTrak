/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui.treetable;

import java.util.EventListener;

/**
 * @author dcollins
 * @version $Id: TreeTableModelListener.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface TreeTableModelListener extends EventListener
{
    void treeTableNodesChanged(TreeTableModelEvent e);

    void treeTableNodesInserted(TreeTableModelEvent e);

    void treeTableNodesRemoved(TreeTableModelEvent e);

    void treeTableStructureChanged(TreeTableModelEvent e);

    void treeTableHeaderChanged(TreeTableModelEvent e);
}
