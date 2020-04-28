/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.filter;

import gov.nasa.worldwind.applications.gio.xml.Element;
import gov.nasa.worldwind.applications.gio.xml.xmlns;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id: Filter.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class Filter extends Element
{
    public Filter()
    {
        super(xmlns.ogc, "Filter");
    }

    public SpatialOperator addSpatialOperator(SpatialOperator spatialOps) throws Exception
    {
        if (spatialOps == null)
        {
            String message = "nullValue.SpatialOpsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        addElement(spatialOps);
        return spatialOps;
    }

    public ComparisonOperator addComparisonOperator(ComparisonOperator comparisonOps) throws Exception
    {
        if (comparisonOps == null)
        {
            String message = "nullValue.ComparisonOpsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        addElement(comparisonOps);
        return comparisonOps;
    }

    public LogicalOperator addLogicalOperator(LogicalOperator logicOps) throws Exception
    {
        if (logicOps == null)
        {
            String message = "nullValue.LogicOpsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        addElement(logicOps);
        return logicOps;
    }
}
