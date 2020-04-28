/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.xml.xmlns;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id: GetRecords.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GetRecords extends Request
{
    public GetRecords()
    {
        super(xmlns.csw, "GetRecords");
    }

    public void setResultType(ResultType resultType)
    {
        if (resultType == null)
        {
            String message = "nullValue.ResultTypeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("resultType", resultType.getType());
    }

    public void setOutputFormat(String outputFormat)
    {
        if (outputFormat == null)
        {
            String message = "nullValue.OutputFormatIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("outputFormat", outputFormat);
    }

    public void setOutputSchema(xmlns ns)
    {
        if (ns == null)
        {
            String message = "nullValue.xmlnsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("outputSchema", ns.getUrl());
    }

    public void setOutputSchema(String outputSchema)
    {
        if (outputSchema == null)
        {
            String message = "nullValue.OutputSchemaIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setAttribute("outputSchema", outputSchema);
    }

    public void setStartPosition(int startPosition)
    {
        setAttribute("startPosition", Integer.toString(startPosition));
    }

    public void setMaxRecords(int maxRecords)
    {
        setAttribute("maxRecords", Integer.toString(maxRecords));
    }

    //public void setUpdateSequence(int updateSequence)
    //{
    //    setAttribute("updateSequence", updateSequence);
    //}

    public Query addQuery(String typeNames) throws Exception
    {
        if (typeNames == null)
        {
            String message = "nullValue.TypeNamesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Query el = new Query(typeNames);
        addElement(el);
        return el;
    }    
}
