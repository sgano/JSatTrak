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
public class ClassificationParser extends RegistryObjectParser implements Classification
{
    private String classificationScheme;
    private String classifiedObject;
    private String classificationNode;
    private String nodeRepresentation;
    public static final String ELEMENT_NAME = "Classification";
    private static final String CLASSIFICATION_SCHEME_ATTRIBUTE_NAME = "classification_scheme";
    private static final String CLASSIFIED_OBJECT_ATTRIBUTE_NAME = "classified_object";
    private static final String CLASSIFICATION_NODE_ATTRIBUTE_NAME = "classification_node";
    private static final String NODE_REPRESENTATION_ATTRIBUTE_NAME = "node_representation";

    public ClassificationParser(String elementName, org.xml.sax.Attributes attributes)
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
            if (CLASSIFICATION_SCHEME_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.classificationScheme = attributes.getValue(i);
            else if (CLASSIFIED_OBJECT_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.classifiedObject = attributes.getValue(i);
            else if (CLASSIFICATION_NODE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.classificationNode = attributes.getValue(i);
            else if (NODE_REPRESENTATION_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.nodeRepresentation = attributes.getValue(i);
        }
    }

    public String getClassificationScheme()
    {
        return this.classificationScheme;
    }

    public void setClassificationScheme(String classificationScheme)
    {
        this.classificationScheme = classificationScheme;
    }

    public String getClassifiedObject()
    {
        return this.classifiedObject;
    }

    public void setClassifiedObject(String classifiedObject)
    {
        this.classifiedObject = classifiedObject;
    }

    public String getClassificationNode()
    {
        return this.classificationNode;
    }

    public void setClassificationNode(String classificationNode)
    {
        this.classificationNode = classificationNode;
    }

    public String getNodeRepresentation()
    {
        return this.nodeRepresentation;
    }

    public void setNodeRepresentation(String nodeRepresentation)
    {
        this.nodeRepresentation = nodeRepresentation;
    }
}
