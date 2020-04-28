/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.ebrim.ClassificationNode;
import gov.nasa.worldwind.applications.gio.ebrim.Service;
import gov.nasa.worldwind.applications.gio.ebrim.ServiceBinding;
import gov.nasa.worldwind.applications.gio.ebrim.User;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: ServicePackage.java 5472 2008-06-26 20:11:53Z dcollins $
 */
public class ServicePackage extends AVListImpl
{
    private Service service;
    private User user;
    private List<ClassificationNode> classificationList;
    private List<ServiceData> serviceDataList;
    private ContextDocument contextDocument;
    private Geometry geometry;

    public ServicePackage()
    {
        this.classificationList = new ArrayList<ClassificationNode>();
        this.serviceDataList = new ArrayList<ServiceData>();
    }

    public Service getService()
    {
        return this.service;
    }

    public void setService(Service service)
    {
        this.service = service;
    }

    public int getServiceBindingCount()
    {
        if (this.service == null)
            return 0;
        return this.service.getServiceBindingCount();
    }

    public int getIndex(ServiceBinding sb)
    {
        if (this.service == null)
            return -1;
        return this.service.getIndex(sb);
    }

    public ServiceBinding getServiceBinding(int index)
    {
        if (this.service == null)
            return null;
        return this.service.getServiceBinding(index);
    }

    public void setServiceBinding(int index, ServiceBinding sb)
    {
        if (this.service == null)
            return;
        this.service.setServiceBinding(index, sb);
    }

    public void addServiceBinding(int index, ServiceBinding sb)
    {
        if (this.service == null)
            return;
        this.service.addServiceBinding(index, sb);
    }

    public void addServiceBinding(ServiceBinding sb)
    {
        if (this.service == null)
            return;
        this.service.addServiceBinding(sb);
    }

    public void addServiceBindings(Collection<? extends ServiceBinding> c)
    {
        if (this.service == null)
            return;
        this.service.addServiceBindings(c);
    }

    public void removeServiceBinding(int index)
    {
        if (this.service == null)
            return;
        this.service.removeServiceBinding(index);
    }

    public void clearServiceBindings()
    {
        if (this.service == null)
            return;
        this.service.clearServiceBindings();
    }

    public Iterator<ServiceBinding> getServiceBindingIterator()
    {
        if (this.service == null)
            return null;
        return this.service.getServiceBindingIterator();
    }

    public User getUser()
    {
        return this.user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public int getClassificationCount()
    {
        return this.classificationList.size();
    }

    public int getIndex(ClassificationNode cls)
    {
        return this.classificationList.indexOf(cls);
    }

    public ClassificationNode getClassification(int index)
    {
        if (index < 0 || index >= this.classificationList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.classificationList.get(index);
    }

    public void setClassification(int index, ClassificationNode cls)
    {
        if (index < 0 || index >= this.classificationList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationList.set(index, cls);
    }

    public void addClassification(int index, ClassificationNode cls)
    {
        if (index < 0 || index > this.classificationList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.classificationList.add(index, cls);
    }

    public void addClassification(ClassificationNode cls)
    {
        this.classificationList.add(cls);
    }

    public void addClassifications(Collection<? extends ClassificationNode> c)
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

    public Iterator<ClassificationNode> getClassificationIterator()
    {
        return this.classificationList.iterator();
    }

    public int getServiceDataCount()
    {
        return this.serviceDataList.size();
    }

    public int getIndex(ServiceData serviceData)
    {
        return this.serviceDataList.indexOf(serviceData);
    }

    public ServiceData getServiceData(int index)
    {
        if (index < 0 || index >= this.serviceDataList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.serviceDataList.get(index);
    }

    public void setServiceData(int index, ServiceData serviceData)
    {
        if (index < 0 || index >= this.serviceDataList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceDataList.set(index, serviceData);
    }

    public void addServiceData(int index, ServiceData serviceData)
    {
        if (index < 0 || index > this.serviceDataList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceDataList.add(index, serviceData);
    }

    public void addServiceData(ServiceData serviceData)
    {
        this.serviceDataList.add(serviceData);
    }

    public void addServiceData(Collection<? extends ServiceData> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceDataList.addAll(c);
    }

    public void removeServiceData(int index)
    {
        if (index < 0 || index >= this.serviceDataList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceDataList.remove(index);
    }

    public void clearServiceData()
    {
        this.serviceDataList.clear();
    }

    public Iterator<ServiceData> getServiceDataIterator()
    {
        return this.serviceDataList.iterator();
    }

    public ContextDocument getContextDocument()
    {
        return this.contextDocument;
    }

    public void setContextDocument(ContextDocument contextDocument)
    {
        this.contextDocument = contextDocument;
    }

    public Geometry getGeometry()
    {
        return this.geometry;
    }

    public void setGeometry(Geometry geometry)
    {
        this.geometry = geometry;
    }
}
