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
public class ExtrinsicObjectParser extends RegistryObjectParser implements ExtrinsicObject
{
    private ContentVersionInfo contentVersionInfo;
    private String mimeType;
    private boolean isOpaque;
    public static final String ELEMENT_NAME = "ExtrinsicObject";
    private static final String MIME_TYPE_ATTRIBUTE_NAME = "mimeType";
    private static final String IS_OPAQUE_ATTRIBUTE_NAME = "isOpaque";

    public ExtrinsicObjectParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (MIME_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
            {
                this.mimeType = attributes.getValue(i);
            }
            else if (IS_OPAQUE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
            {
                String value = attributes.getValue(i);
                if (value != null)
                    this.isOpaque = Boolean.parseBoolean(value);
            }
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        super.doStartElement(name, attributes);

        if (ContentVersionInfoParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ContentVersionInfoParser parser = new ContentVersionInfoParser(name, attributes);
            this.contentVersionInfo = parser;
            setCurrentElement(parser);
        }
    }

    public ContentVersionInfo getContentVersionInfo()
    {
        return this.contentVersionInfo;
    }

    public void setContentVersionInfo(ContentVersionInfo contentVersionInfo)
    {
        this.contentVersionInfo = contentVersionInfo;
    }

    public String getMimeType()
    {
        return this.mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public boolean isOpaque()
    {
        return this.isOpaque;
    }

    public void setOpaque(boolean opaque)
    {
        this.isOpaque = opaque;
    }
}
