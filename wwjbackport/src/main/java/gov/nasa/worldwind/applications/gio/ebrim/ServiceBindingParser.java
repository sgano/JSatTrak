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
public class ServiceBindingParser extends RegistryObjectParser implements ServiceBinding
{
    private List<SpecificationLink> specificationLinkList;
    private String service;
    private String accessURI;
    private String targetBinding;
    public static final String ELEMENT_NAME = "ServiceBinding";
    private static final String SERVICE_ATTRIBUTE_NAME = "service";
    private static final String ACCESS_URI_ATTRIBUTE_NAME = "accessURI";
    private static final String TARGET_BINDING_ATTRIBUTE_NAME = "targetBinding";

    public ServiceBindingParser(String name, org.xml.sax.Attributes attributes)
    {
        super(name, attributes);

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.specificationLinkList = new ArrayList<SpecificationLink>();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (SERVICE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.service = attributes.getValue(i);
            else if (ACCESS_URI_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.accessURI = attributes.getValue(i);
            else if (TARGET_BINDING_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.targetBinding = attributes.getValue(i);
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        super.doStartElement(name, attributes);

        if (SpecificationLinkParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            SpecificationLinkParser parser = new SpecificationLinkParser(name, attributes);
            this.specificationLinkList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getSpecificationLinkCount()
    {
        return this.specificationLinkList.size();
    }

    public int getIndex(SpecificationLink sl)
    {
        return this.specificationLinkList.indexOf(sl);
    }

    public SpecificationLink getSpecificationLink(int index)
    {
        if (index < 0 || index >= this.specificationLinkList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.specificationLinkList.get(index);
    }

    public void setSpecificationLink(int index, SpecificationLink sl)
    {
        if (index < 0 || index >= this.specificationLinkList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.specificationLinkList.set(index, sl);
    }

    public void addSpecificationLink(int index, SpecificationLink sl)
    {
        if (index < 0 || index > this.specificationLinkList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.specificationLinkList.add(index, sl);
    }

    public void addSpecificationLink(SpecificationLink sl)
    {
        this.specificationLinkList.add(sl);
    }

    public void addSpecificationLinks(Collection<? extends SpecificationLink> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.specificationLinkList.addAll(c);
    }

    public void removeSpecificationLink(int index)
    {
        if (index < 0 || index >= this.specificationLinkList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.specificationLinkList.remove(index);
    }

    public void clearSpecificationLinks()
    {
        this.specificationLinkList.clear();
    }

    public Iterator<SpecificationLink> getSpecificationLinkIterator()
    {
        return this.specificationLinkList.iterator();
    }

    public String getService()
    {
        return this.service;
    }

    public void setService(String service)
    {
        this.service = service;
    }

    public String getAccessURI()
    {
        return this.accessURI;
    }

    public void setAccessURI(String accessURI)
    {
        this.accessURI = accessURI;
    }

    public String getTargetBinding()
    {
        return this.targetBinding;
    }

    public void setTargetBinding(String targetBinding)
    {
        this.targetBinding = targetBinding;
    }
}
