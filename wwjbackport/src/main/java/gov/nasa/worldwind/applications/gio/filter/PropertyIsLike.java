/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.filter;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: PropertyIsLike.java 5466 2008-06-24 02:17:32Z dcollins $
 */
public class PropertyIsLike extends ComparisonOperator
{
    public PropertyIsLike()
    {
        super("PropertyIsLike");
        setWildCard("*");
        setSingleChar("?");
        setEscapeChar("\\");
    }

    public void setWildCard(String wildCard)
    {
        if (wildCard == null)
        {
            String message = "nullValue.WildCardIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("wildCard", wildCard);
    }

    public void setSingleChar(String singleChar)
    {
        if (singleChar == null)
        {
            String message = "nullValue.SingleCharIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("singleChar", singleChar);
    }

    public void setEscapeChar(String escapeChar)
    {
        if (escapeChar == null)
        {
            String message = "nullValue.EscapeCharIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("escapeChar", escapeChar);
    }

    public PropertyName addPropertyName(String propertyName) throws Exception
    {
        if (propertyName == null)
        {
            String message = "nullValue.PropertyNameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        PropertyName pn = new PropertyName(propertyName);
        addElement(pn);
        return pn;
    }

    public Literal addLiteral(String literalValue) throws Exception
    {
        if (literalValue == null)
        {
            String message = "nullValue.LiteralValueIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Literal literal = new Literal(literalValue);
        addElement(literal);
        return literal;
    }
}
