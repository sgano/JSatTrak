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
public class ClassificationNodeParser extends RegistryObjectParser implements ClassificationNode
{
    private List<ClassificationNode> childList;
    private String parent;
    private String code;
    private String path;
    public static final String ELEMENT_NAME = "ClassificationNode";
    private static final String PARENT_ATTRIBUTE_NAME = "parent";
    private static final String CODE_ATTRIBUTE_NAME = "code";
    private static final String PATH_ATTRIBUTE_NAME = "path";

    public ClassificationNodeParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.childList = new ArrayList<ClassificationNode>();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (PARENT_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.parent = attributes.getValue(i);
            else if (CODE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.code = attributes.getValue(i);
            else if (PATH_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.path = attributes.getValue(i);
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        super.doStartElement(name, attributes);

        if (ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ClassificationNodeParser parser = new ClassificationNodeParser(name, attributes);
            this.childList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getChildCount()
    {
        return this.childList.size();
    }

    public int getIndex(ClassificationNode child)
    {
        return this.childList.indexOf(child);
    }

    public ClassificationNode getChild(int index)
    {
        if (index < 0 || index >= this.childList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.childList.get(index);
    }

    public void setChild(int index, ClassificationNode child)
    {
        if (index < 0 || index >= this.childList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.childList.set(index, child);
    }

    public void addChild(int index, ClassificationNode child)
    {
        if (index < 0 || index > this.childList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.childList.add(index, child);
    }

    public void addChild(ClassificationNode child)
    {
        this.childList.add(child);
    }

    public void addChildren(Collection<? extends ClassificationNode> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.childList.addAll(c);
    }

    public void removeChild(int index)
    {
        if (index < 0 || index >= this.childList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.childList.remove(index);
    }

    public void clearChildren()
    {
        this.childList.clear();
    }

    public Iterator<ClassificationNode> iterator()
    {
        return this.childList.iterator();
    }

    public String getParent()
    {
        return this.parent;
    }

    public void setParent(String parent)
    {
        this.parent = parent;
    }

    public String getCode()
    {
        return this.code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getPath()
    {
        return this.path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
