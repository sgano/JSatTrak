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
public class PersonNameParser extends ElementParser implements PersonName
{
    private String firstName;
    private String middleName;
    private String lastName;
    public static final String ELEMENT_NAME = "PersonName";
    private static final String FIRST_NAME_ATTRIBUTE_NAME = "firstName";
    private static final String MIDDLE_NAME_ATTRIBUTE_NAME = "middleName";
    private static final String LAST_NAME_ATTRIBUTE_NAME = "lastName";

    public PersonNameParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (FIRST_NAME_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.firstName = attributes.getValue(i);
            else if (MIDDLE_NAME_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.middleName = attributes.getValue(i);
            else if (LAST_NAME_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.lastName = attributes.getValue(i);
        }
    }

    public String getFirstName()
    {
        return this.firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getMiddleName()
    {
        return this.middleName;
    }

    public void setMiddleName(String middleName)
    {
        this.middleName = middleName;
    }

    public String getLastName()
    {
        return this.lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
}
