/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.filter;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: UnaryLogicalOperator.java 5466 2008-06-24 02:17:32Z dcollins $
 */
public abstract class UnaryLogicalOperator extends LogicalOperator
{
    protected UnaryLogicalOperator(String name)
    {
        super(name);
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