/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.filter;

import gov.nasa.worldwind.applications.gio.xml.Element;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: PropertyIsBetween.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class PropertyIsBetween extends ComparisonOperator
{
    public PropertyIsBetween()
    {
        super("PropertyIsBetween");
    }

    public Element addExpression(Expression expression) throws Exception
    {
        if (expression == null)
        {
            String message = "nullValue.ExpressionIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!(expression instanceof Element))
        {
            String message = "nullValue.ExpressionIsNotAnElement";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        addElement((Element) expression);
        return (Element) expression;
    }

    public LowerBoundary addLowerBoundary(Expression expression) throws Exception
    {
        if (expression == null)
        {
            String message = "nullValue.ExpressionIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LowerBoundary lb = new LowerBoundary();
        lb.addExpression(expression);
        addElement(lb);
        return lb;
    }

    public UpperBoundary addUpperBoundary(Expression expression) throws Exception
    {
        if (expression == null)
        {
            String message = "nullValue.ExpressionIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        UpperBoundary ub = new UpperBoundary();
        ub.addExpression(expression);
        addElement(ub);
        return ub;
    }
}
