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
public class VersionInfoParser extends ElementParser implements VersionInfo
{
    private String versionName;
    private String comment;
    public static final String ELEMENT_NAME = "VersionInfo";
    private static final String VERSION_NAME_ATTRIBUTE_NAME = "versionName";
    private static final String COMMENT_ATTRIBUTE_NAME = "comment";

    public VersionInfoParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (VERSION_NAME_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.versionName = attributes.getValue(i);
            else if (COMMENT_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.comment = attributes.getValue(i);
        }
    }

    public String getVersionName()
    {
        return this.versionName;
    }

    public void setVersionName(String versionName)
    {
        this.versionName = versionName;
    }

    public String getComment()
    {
        return this.comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
