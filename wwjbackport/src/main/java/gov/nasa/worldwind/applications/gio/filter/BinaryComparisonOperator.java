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
 * @version $Id: BinaryComparisonOperator.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public abstract class BinaryComparisonOperator extends ComparisonOperator
{
    protected BinaryComparisonOperator(String name)
    {
        super(name);
    }

    public void setMatchCase(boolean matchCase)
    {
        setAttribute("matchCase", Boolean.toString(matchCase));
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
}
