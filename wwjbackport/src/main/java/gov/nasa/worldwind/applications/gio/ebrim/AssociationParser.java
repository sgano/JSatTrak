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
public class AssociationParser extends RegistryObjectParser implements Association
{
    private String associationType;
    private String sourceObject;
    private String targetObject;
    public static final String ELEMENT_NAME = "Association";
    private static final String ASSOCIATION_TYPE_ATTRIBUTE_NAME = "associationType";
    private static final String SOURCE_OBJECT_ATTRIBUTE_NAME = "sourceObject";
    private static final String TARGET_OBJECT_ATTRIBUTE_NAME = "targetObject";

    public AssociationParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (ASSOCIATION_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.associationType = attributes.getValue(i);
            else if (SOURCE_OBJECT_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.sourceObject = attributes.getValue(i);
            else if (TARGET_OBJECT_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.targetObject = attributes.getValue(i);
        }
    }

    public String getAssociationType()
    {
        return this.associationType;
    }

    public void setAssociationType(String associationType)
    {
        this.associationType = associationType;
    }

    public String getSourceObject()
    {
        return this.sourceObject;
    }

    public void setSourceObject(String sourceObject)
    {
        this.sourceObject = sourceObject;
    }

    public String getTargetObject()
    {
        return this.targetObject;
    }

    public void setTargetObject(String targetObject)
    {
        this.targetObject = targetObject;
    }
}
