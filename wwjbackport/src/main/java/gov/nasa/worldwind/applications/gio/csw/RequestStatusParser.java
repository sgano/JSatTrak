/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: RequestStatusParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class RequestStatusParser extends ElementParser implements RequestStatus
{
    private String timestamp;
    public static final String ELEMENT_NAME = "RequestStatus";
    private static final String TIMESTAMP_ATTRIBUTE_NAME = "timestamp";

    public RequestStatusParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (TIMESTAMP_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.timestamp = attributes.getValue(i);
        }
    }

    public String getTimestamp()
    {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }
}
