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
public class PostalAddressParser extends ElementParser implements PostalAddress
{
    private String city;
    private String country;
    private String postalCode;
    private String stateOrProvince;
    private String street;
    private String streetNumber;
    public static final String ELEMENT_NAME = "PostalAddress";
    private static final String CITY_ATTRIBUTE_NAME = "city";
    private static final String COUNTRY_ATTRIBUTE_NAME = "country";
    private static final String POSTAL_CODE_ATTRIBUTE_NAME = "postalCode";
    private static final String STATE_OR_PROVINCE_ATTRIBUTE_NAME = "stateOrProvince";
    private static final String STREET_ATTRIBUTE_NAME = "street";
    private static final String STREET_NUMBER_ATTRIBUTE_NAME = "streetNumber";

    public PostalAddressParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (CITY_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.city = attributes.getValue(i);
            else if (COUNTRY_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.country = attributes.getValue(i);
            else if (POSTAL_CODE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.postalCode = attributes.getValue(i);
            else if (STATE_OR_PROVINCE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.stateOrProvince = attributes.getValue(i);
            else if (STREET_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.street = attributes.getValue(i);
            else if (STREET_NUMBER_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.streetNumber = attributes.getValue(i);
        }
    }

    public String getCity()
    {
        return this.city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getCountry()
    {
        return this.country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getPostalCode()
    {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode)
    {
        this.postalCode = postalCode;
    }

    public String getStateOrProvince()
    {
        return this.stateOrProvince;
    }

    public void setStateOrProvince(String stateOrProvince)
    {
        this.stateOrProvince = stateOrProvince;
    }

    public String getStreet()
    {
        return this.street;
    }

    public void setStreet(String street)
    {
        this.street = street;
    }

    public String getStreetNumber()
    {
        return this.streetNumber;
    }

    public void setStreetNumber(String streetNumber)
    {
        this.streetNumber = streetNumber;
    }
}
