/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * A path to a node in a {@link Tree}. The path is expressed as a list of strings.
 * 
 * @author tag
 * @version $Id: TreePath.java 13985 2010-10-17 21:32:03Z pabercrombie $
 */
public class TreePath extends ArrayList<String>
{
    public TreePath()
    {
    }

    public TreePath(TreePath initialPath, String... args)
    {
        this.addAll(initialPath);

        for (String pathElement : args)
        {
            if (!WWUtil.isEmpty(pathElement))
                this.add(pathElement);
        }
    }

    public TreePath(String initialPathEntry, String... args)
    {
        this.add(initialPathEntry);

        for (String pathElement : args)
        {
            if (!WWUtil.isEmpty(pathElement))
                this.add(pathElement);
        }
    }

    public TreePath(List<String> initialPathEntries)
    {
        this.addAll(initialPathEntries);
    }

    public TreePath lastButOne()
    {
        return this.subPath(0, this.size() - 1);
    }

    public TreePath subPath(int start, int end)
    {
        return new TreePath(this.subList(start, end));
    }

    public static boolean isEmptyPath(TreePath path)
    {
        return path == null || path.size() == 0 || WWUtil.isEmpty(path.get(0));
    }

    @Override
    public String toString()
    {
        if (this.size() == 0)
            return "<empty path>";

        StringBuilder sb = new StringBuilder();

        for (String s : this)
        {
            if (WWUtil.isEmpty(s))
                s = "<empty>";

            if (sb.length() == 0)
                sb.append(s);
            else
                sb.append("/").append(s);
        }

        return sb.toString();
    }
}
