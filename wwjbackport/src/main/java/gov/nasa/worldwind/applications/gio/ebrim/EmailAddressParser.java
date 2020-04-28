/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id$
 */
public class EmailAddressParser extends ElementParser implements EmailAddress
{
    private String address;
    private String type;
    public static final String ELEMENT_NAME = "EmailAddress";
    private static final String ADDRESS_ATTRIBUTE_NAME = "address";
    private static final String TYPE_ATTRIBUTE_NAME = "type";

    public EmailAddressParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (ADDRESS_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.address = attributes.getValue(i);
            else if (TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.type = attributes.getValue(i);
        }
    }

    public String getAddress()
    {
        return this.address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
