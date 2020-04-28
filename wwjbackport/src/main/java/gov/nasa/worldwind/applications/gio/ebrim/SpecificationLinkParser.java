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
public class SpecificationLinkParser extends RegistryObjectParser implements SpecificationLink
{
    private UsageDescription usageDescription;
    private List<UsageParameter> usageParameterList;
    private String serviceBinding;
    private String specificationObject;
    public static final String ELEMENT_NAME = "SpecificationLink";
    private static final String SERVICE_BINDING_ATTRIBUTE_NAME = "serviceBinding";
    private static final String SPECIFICATION_OBJECT_ATTRIBUTE_NAME = "specificationObject";

    public SpecificationLinkParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        this.usageParameterList = new ArrayList<UsageParameter>();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (SERVICE_BINDING_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.serviceBinding = attributes.getValue(i);
            else if (SPECIFICATION_OBJECT_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.specificationObject = attributes.getValue(i);
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        super.doStartElement(name, attributes);

        if (UsageDescriptionParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            UsageDescriptionParser parser = new UsageDescriptionParser(name, attributes);
            this.usageDescription = parser;
            setCurrentElement(parser);
        }
        else if (UsageParameterParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            UsageParameterParser parser = new UsageParameterParser(name, attributes);
            this.usageParameterList.add(parser);
            setCurrentElement(parser);
        }
    }

    public UsageDescription getUsageDescription()
    {
        return this.usageDescription;
    }

    public void setUsageDescription(UsageDescription usageDescription)
    {
        this.usageDescription = usageDescription;
    }

    public int getUsageParameterCount()
    {
        return this.usageParameterList.size();
    }

    public int getIndex(UsageParameter p)
    {
        return this.usageParameterList.indexOf(p);
    }

    public UsageParameter getUsageParameter(int index)
    {
        if (index < 0 || index >= this.usageParameterList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.usageParameterList.get(index);
    }

    public void setUsageParameter(int index, UsageParameter p)
    {
        if (index < 0 || index >= this.usageParameterList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.usageParameterList.set(index, p);
    }

    public void addUsageParameter(int index, UsageParameter p)
    {
        if (index < 0 || index > this.usageParameterList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.usageParameterList.add(index, p);
    }

    public void addUsageParameter(UsageParameter p)
    {
        this.usageParameterList.add(p);
    }

    public void addUsageParameters(Collection<? extends UsageParameter> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.usageParameterList.addAll(c);
    }

    public void removeUsageParameter(int index)
    {
        if (index < 0 || index >= this.usageParameterList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.usageParameterList.remove(index);
    }

    public void clearUsageParameters()
    {
        this.usageParameterList.clear();
    }

    public Iterator<UsageParameter> getUsageParameterIterator()
    {
        return this.usageParameterList.iterator();
    }

    public String getServiceBinding()
    {
        return this.serviceBinding;
    }

    public void setServiceBinding(String serviceBinding)
    {
        this.serviceBinding = serviceBinding;
    }

    public String getSpecificationObject()
    {
        return this.specificationObject;
    }

    public void setSpecificationObject(String specificationObject)
    {
        this.specificationObject = specificationObject;
    }
}
