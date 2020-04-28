/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id$
 */
public class ClassificationSchemeParser extends RegistryObjectParser implements ClassificationScheme
{
    private List<ClassificationNode> classificationNodeList;
    private boolean isInternal;
    private String nodeType;
    public static final String ELEMENT_NAME = "ClassificationScheme";
    private static final String IS_INTERNAL_ATTRIBUTE_NAME = "isInternal";
    private static final String NODE_TYPE_ATTRIBUTE_NAME = "nodeType";

    public ClassificationSchemeParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationNodeList = new ArrayList<ClassificationNode>();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (IS_INTERNAL_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
            {
                String value = attributes.getValue(i);
                if (value != null)
                    this.isInternal = Boolean.parseBoolean(value);
            }
            else if (NODE_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
            {
                this.nodeType = attributes.getValue(i);
            }
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        super.doStartElement(name, attributes);

        if (ClassificationNodeParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ClassificationNodeParser parser = new ClassificationNodeParser(name, attributes);
            this.classificationNodeList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getClassificationNodeCount()
    {
        return this.classificationNodeList.size();
    }

    public int getIndex(ClassificationNode node)
    {
        return this.classificationNodeList.indexOf(node);
    }

    public ClassificationNode getClassificationNode(int index)
    {
        if (index < 0 || index >= this.classificationNodeList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.classificationNodeList.get(index);
    }

    public void setClassificationNode(int index, ClassificationNode node)
    {
        if (index < 0 || index >= this.classificationNodeList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationNodeList.set(index, node);
    }

    public void addClassificationNode(int index, ClassificationNode node)
    {
        if (index < 0 || index > this.classificationNodeList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationNodeList.add(index, node);
    }

    public void addClassificationNode(ClassificationNode node)
    {
        this.classificationNodeList.add(node);
    }

    public void addClassificationNodes(Collection<? extends ClassificationNode> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationNodeList.addAll(c);
    }

    public void removeClassificationNode(int index)
    {
        if (index < 0 || index >= this.classificationNodeList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationNodeList.remove(index);
    }

    public void clearClassificationNodes()
    {
        this.classificationNodeList.clear();
    }

    public Iterator<ClassificationNode> iterator()
    {
        return this.classificationNodeList.iterator();
    }

    public boolean isInternal()
    {
        return this.isInternal;
    }

    public void setInternal(boolean internal)
    {
        this.isInternal = internal;
    }

    public String getNodeType()
    {
        return this.nodeType;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }
}
