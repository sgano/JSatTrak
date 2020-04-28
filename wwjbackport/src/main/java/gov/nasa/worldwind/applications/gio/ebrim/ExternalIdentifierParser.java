/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id$
 */
public class ExternalIdentifierParser extends RegistryObjectParser implements ExternalIdentifier
{
    private String registryObject;
    private String identificationScheme;
    private String value;
    public static final String ELEMENT_NAME = "ExternalIdentifier";
    private static final String REGISTRY_OBJECT_ATTRIBUTE_NAME = "registryObject";
    private static final String IDENTIFICATION_SCHEME_ATTRIBUTE_NAME = "identificationScheme";
    private static final String VALUE_ATTRIBUTE_NAME = "value";

    public ExternalIdentifierParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (REGISTRY_OBJECT_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.registryObject = attributes.getValue(i);
            else if (IDENTIFICATION_SCHEME_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.identificationScheme = attributes.getValue(i);
            else if (VALUE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.value = attributes.getValue(i);
        }
    }

    public String getRegistryObject()
    {
        return this.registryObject;
    }

    public void setRegistryObject(String registryObject)
    {
        this.registryObject = registryObject;
    }

    public String getIdentificationScheme()
    {
        return this.identificationScheme;
    }

    public void setIdentificationScheme(String identificationScheme)
    {
        this.identificationScheme = identificationScheme;
    }

    public String getValue()
    {
        return this.value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
