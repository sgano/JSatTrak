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
public class ExternalLinkParser extends RegistryObjectParser implements ExternalLink
{
    private String externalURI;
    public static final String ELEMENT_NAME = "ExternalLink";
    private static final String EXTERNAL_URI_ATTRIBUTE_NAME = "externalURI";

    public ExternalLinkParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (EXTERNAL_URI_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.externalURI = attributes.getValue(i);
        }
    }

    public String getExternalURI()
    {
        return this.externalURI;
    }

    public void setExternalURI(String externalURI)
    {
        this.externalURI = externalURI;
    }
}
