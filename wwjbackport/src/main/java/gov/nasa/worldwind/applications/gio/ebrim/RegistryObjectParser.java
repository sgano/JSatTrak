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
public class RegistryObjectParser extends IdentifiableParser implements RegistryObject
{
    private Name name;
    private Description description;
    private VersionInfo versionInfo;
    private List<Classification> classificationList;
    private List<ExternalIdentifier> externalIdentifierList;
    private String lid;
    private String objectType;
    private String status;
    public static final String ELEMENT_NAME = "RegistryObject";
    private static final String LID_ATTRIBUTE_NAME = "lid";
    private static final String OBJECT_TYPE_ATTRIBUTE_NAME = "objectType";
    private static final String STATUS_ATTRIBUTE_NAME = "status";

    public RegistryObjectParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationList = new ArrayList<Classification>();
        this.externalIdentifierList = new ArrayList<ExternalIdentifier>();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (LID_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.lid = attributes.getValue(i);
            else if (OBJECT_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.objectType = attributes.getValue(i);
            else if (STATUS_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.status = attributes.getValue(i);
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        super.doStartElement(name, attributes);
        
        if (NameParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            NameParser parser = new NameParser(name, attributes);
            this.name = parser;
            setCurrentElement(parser);
        }
        else if (DescriptionParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            DescriptionParser parser = new DescriptionParser(name, attributes);
            this.description = parser;
            setCurrentElement(parser);
        }
        else if (VersionInfoParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            VersionInfoParser parser = new VersionInfoParser(name, attributes);
            this.versionInfo = parser;
            setCurrentElement(parser);
        }
        else if (ClassificationNodeParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ClassificationParser parser = new ClassificationParser(name, attributes);
            this.classificationList.add(parser);
            setCurrentElement(parser);
        }
        else if (ExternalIdentifierParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ExternalIdentifierParser parser = new ExternalIdentifierParser(name, attributes);
            this.externalIdentifierList.add(parser);
            setCurrentElement(parser);
        }
    }

    public Name getName()
    {
        return this.name;
    }

    public void setName(Name name)
    {
        this.name = name;
    }

    public Description getDescription()
    {
        return this.description;
    }

    public void setDescription(Description description)
    {
        this.description = description;
    }

    public VersionInfo getVersionInfo()
    {
        return this.versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo)
    {
        this.versionInfo = versionInfo;
    }

    public int getClassificationCount()
    {
        return this.classificationList.size();
    }

    public int getIndex(Classification classification)
    {
        return this.classificationList.indexOf(classification);
    }

    public Classification getClassification(int index)
    {
        if (index < 0 || index >= this.classificationList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.classificationList.get(index);
    }

    public void setClassification(int index, Classification classification)
    {
        if (index < 0 || index >= this.classificationList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationList.set(index, classification);
    }

    public void addClassification(int index, Classification classification)
    {
        if (index < 0 || index > this.classificationList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationList.add(index, classification);
    }

    public void addClassification(Classification classification)
    {
        this.classificationList.add(classification);
    }

    public void addClassifications(Collection<? extends Classification> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationList.addAll(c);
    }

    public void removeClassification(int index)
    {
        if (index < 0 || index >= this.classificationList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationList.remove(index);
    }

    public void clearClassifications()
    {
        this.classificationList.clear();
    }

    public Iterator<Classification> getClassificationIterator()
    {
        return this.classificationList.iterator();
    }

    public int getExternalIdentifierCount()
    {
        return this.externalIdentifierList.size();
    }

    public int getIndex(ExternalIdentifier externalIdentifier)
    {
        return this.externalIdentifierList.indexOf(externalIdentifier);
    }

    public ExternalIdentifier getExternalIdentifier(int index)
    {
        if (index < 0 || index >= this.externalIdentifierList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.externalIdentifierList.get(index);
    }

    public void setExternalIdentifier(int index, ExternalIdentifier externalIdentifier)
    {
        if (index < 0 || index >= this.externalIdentifierList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.externalIdentifierList.set(index, externalIdentifier);
    }

    public void addExternalIdentifier(int index, ExternalIdentifier externalIdentifier)
    {
        if (index < 0 || index > this.externalIdentifierList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.externalIdentifierList.add(index, externalIdentifier);
    }

    public void addExternalIdentifier(ExternalIdentifier externalIdentifier)
    {
        this.externalIdentifierList.add(externalIdentifier);
    }

    public void addExternalIdentifiers(Collection<? extends ExternalIdentifier> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.externalIdentifierList.addAll(c);
    }

    public void removeExternalIdentifier(int index)
    {
        if (index < 0 || index >= this.externalIdentifierList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.externalIdentifierList.remove(index);
    }

    public void clearExternalIdentifiers()
    {
        this.externalIdentifierList.clear();
    }

    public Iterator<ExternalIdentifier> getExternalIdentifierIterator()
    {
        return this.externalIdentifierList.iterator();
    }

    public String getLid()
    {
        return this.lid;
    }

    public void setLid(String lid)
    {
        this.lid = lid;
    }

    public String getObjectType()
    {
        return this.objectType;
    }

    public void setObjectType(String objectType)
    {
        this.objectType = objectType;
    }

    public String getStatus()
    {
        return this.status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
