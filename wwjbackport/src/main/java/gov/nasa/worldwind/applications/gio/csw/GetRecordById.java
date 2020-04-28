/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.xml.Element;
import gov.nasa.worldwind.applications.gio.xml.TextElement;
import gov.nasa.worldwind.applications.gio.xml.xmlns;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id: GetRecordById.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GetRecordById extends Request
{
    public GetRecordById()
    {
        super(xmlns.csw, "GetRecordById");
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

    public Element addId(String id) throws Exception
    {
        if (id == null)
        {
            String message = "nullValue.IdIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        TextElement el = new TextElement(getNs(), "Id", id);
        addElement(el);
        return el;
    }

    public ElementSetName addElementSetName(ElementSetType elementSetType) throws Exception
    {
        if (elementSetType == null)
        {
            String message = "nullValue.ElementSetTypeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ElementSetName el = new ElementSetName(elementSetType.getType());
        addElement(el);
        return el;
    }
}
