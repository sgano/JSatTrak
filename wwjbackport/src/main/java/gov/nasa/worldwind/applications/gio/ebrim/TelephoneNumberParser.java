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
public class TelephoneNumberParser extends ElementParser implements TelephoneNumber
{
    private String areaCode;
    private String countryCode;
    private String extension;
    private String number;
    private String phoneType;
    public static final String ELEMENT_NAME = "TelephoneNumber";
    private static final String AREA_CODE_ATTRIBUTE_NAME = "areaCode";
    private static final String COUNTRY_CODE_ATTRIBUTE_NAME = "countryCode";
    private static final String EXTENSION_ATTRIBUTE_NAME = "extension";
    private static final String NUMBER_ATTRIBUTE_NAME = "number";
    private static final String PHONE_TYPE_ATTRIBUTE_NAME = "phoneType";

    public TelephoneNumberParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (AREA_CODE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.areaCode = attributes.getValue(i);
            else if (COUNTRY_CODE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.countryCode = attributes.getValue(i);
            else if (EXTENSION_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.extension = attributes.getValue(i);
            else if (NUMBER_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.number = attributes.getValue(i);
            else if (PHONE_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.phoneType = attributes.getValue(i);
        }
    }

    public String getAreaCode()
    {
        return this.areaCode;
    }

    public void setAreaCode(String areaCode)
    {
        this.areaCode = areaCode;
    }

    public String getCountryCode()
    {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
    }

    public String getExtension()
    {
        return this.extension;
    }

    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    public String getNumber()
    {
        return this.number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public String getPhoneType()
    {
        return this.phoneType;
    }

    public void setPhoneType(String phoneType)
    {
        this.phoneType = phoneType;
    }
}
