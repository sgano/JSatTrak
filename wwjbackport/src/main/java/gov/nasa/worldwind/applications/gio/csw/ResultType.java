/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: ResultType.java 5465 2008-06-24 00:17:03Z dcollins $
 */
public class ResultType
{
    private String type;
    public static final ResultType RESULTS = new ResultType("results");
    public static final ResultType HITS = new ResultType("hits");
    public static final ResultType VALIDATE = new ResultType("validate");

    protected ResultType(String type)
    {
        if (type == null)
        {
            String message = "csw.TypeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }

    public String toString()
    {
        return getType();
    }
}
