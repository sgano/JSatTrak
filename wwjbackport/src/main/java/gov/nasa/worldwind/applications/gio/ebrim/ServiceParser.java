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
public class ServiceParser extends RegistryObjectParser implements Service
{
    private List<ServiceBinding> serviceBindingList;
    public static final String ELEMENT_NAME = "Service";

    public ServiceParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        this.serviceBindingList = new ArrayList<ServiceBinding>();
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        super.doStartElement(name, attributes);
        
        if (ServiceBindingParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ServiceBindingParser parser = new ServiceBindingParser(name, attributes);
            this.serviceBindingList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getServiceBindingCount()
    {
        return this.serviceBindingList.size();
    }

    public int getIndex(ServiceBinding sb)
    {
        return this.serviceBindingList.indexOf(sb);
    }

    public ServiceBinding getServiceBinding(int index)
    {
        if (index < 0 || index >= this.serviceBindingList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.serviceBindingList.get(index);
    }

    public void setServiceBinding(int index, ServiceBinding sb)
    {
        if (index < 0 || index >= this.serviceBindingList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceBindingList.set(index, sb);
    }

    public void addServiceBinding(int index, ServiceBinding sb)
    {
        if (index < 0 || index > this.serviceBindingList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceBindingList.add(index, sb);
    }

    public void addServiceBinding(ServiceBinding sb)
    {
        this.serviceBindingList.add(sb);
    }

    public void addServiceBindings(Collection<? extends ServiceBinding> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceBindingList.addAll(c);
    }

    public void removeServiceBinding(int index)
    {
        if (index < 0 || index >= this.serviceBindingList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceBindingList.remove(index);
    }

    public void clearServiceBindings()
    {
        this.serviceBindingList.clear();
    }

    public Iterator<ServiceBinding> getServiceBindingIterator()
    {
        return this.serviceBindingList.iterator();
    }
}
