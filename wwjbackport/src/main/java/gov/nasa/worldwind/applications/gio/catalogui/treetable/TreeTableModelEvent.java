/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui.treetable;

import java.util.EventObject;

/**
 * @author dcollins
 * @version $Id: TreeTableModelEvent.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class TreeTableModelEvent extends EventObject
{
    private Object[] path;
    private int[] childIndices;
    private Object[] children;

    public TreeTableModelEvent(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        super(source);
        this.path = path;
        this.childIndices = childIndices;
        this.children = children;
    }

    public TreeTableModelEvent(Object source, Object[] path)
    {
        super(source);
        this.path = path;
        this.childIndices = null;
        this.children = null;
    }

    public TreeTableModelEvent(Object source)
    {
        super(source);
        this.path = null;
        this.childIndices = null;
        this.children = null;
    }

    public Object[] getPath()
    {
        return this.path;
    }

    public int[] getChildIndices()
    {
        return this.childIndices;
    }

    public Object[] getChildren()
    {
        return this.children;
    }
}
