/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import java.util.EventObject;

/**
 * @author dcollins
 * @version $Id: ResultListEvent.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ResultListEvent extends EventObject
{
    private int startIndex;
    private int endIndex;
    private int type;

    public static final int ADD = 1;
    public static final int REMOVE = 2;
    public static final int UPDATE = 3;
    public static final int PASSIVE_UPDATE = 4;

    public ResultListEvent(ResultList source)
    {
        this(source, -1, -1, -1);
    }

    public ResultListEvent(ResultList source, int index)
    {
        this(source, index, index, -1);
    }

    public ResultListEvent(ResultList source, int startIndex, int endIndex)
    {
        this(source, startIndex, endIndex, -1);
    }

    public ResultListEvent(ResultList source, int startIndex, int endIndex, int type)
    {
        super(source);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
    }

    public int getStartIndex()
    {
        return this.startIndex;
    }

    public int getEndIndex()
    {
        return this.endIndex;
    }

    public int getType()
    {
        return this.type;
    }
}
