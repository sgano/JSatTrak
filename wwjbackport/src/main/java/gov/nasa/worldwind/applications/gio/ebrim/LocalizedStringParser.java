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
public class LocalizedStringParser extends ElementParser implements LocalizedString
{
    private String charset;
    private String lang;
    private String value;
    public static final String ELEMENT_NAME = "LocalizedString";
    private static final String CHARSET_ATTRIB_NAME = "charset";
    private static final String LANG_ATTRIB_NAME = "lang";
    private static final String VALUE_ATTRIB_NAME = "value";

    public LocalizedStringParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (CHARSET_ATTRIB_NAME.equalsIgnoreCase(attribName))
                this.charset = attributes.getValue(i);
            else if (LANG_ATTRIB_NAME.equalsIgnoreCase(attribName))
                this.lang = attributes.getValue(i);
            else if (VALUE_ATTRIB_NAME.equalsIgnoreCase(attribName))
                this.value = attributes.getValue(i);
        }
    }

    public String getCharset()
    {
        return this.charset;
    }

    public void setCharset(String charset)
    {
        this.charset = charset;
    }

    public String getLang()
    {
        return this.lang;
    }

    public void setLang(String lang)
    {
        this.lang = lang;
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
