/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogException;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogExceptionList;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.ebrim.ClassificationNode;
import gov.nasa.worldwind.applications.gio.ebrim.ExtrinsicObject;
import gov.nasa.worldwind.applications.gio.ebrim.Service;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: ServiceData.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ServiceData extends AVListImpl
{
    private ExtrinsicObject extrinsicObject;
    private Service service;
    // Ignoring LayerStyle
    private List<ClassificationNode> classificationList;
    private List<ServiceDataLink> linkList;
    private DatasetDescription datasetDescription;
    private Geometry geometry;
    private CatalogExceptionList exceptionList;

    public ServiceData()
    {
        this.classificationList = new ArrayList<ClassificationNode>();
        this.linkList = new ArrayList<ServiceDataLink>();
    }

    public ExtrinsicObject getExtrinsicObject()
    {
        return this.extrinsicObject;
    }

    public void setExtrinsicObject(ExtrinsicObject extrinsicObject)
    {
        this.extrinsicObject = extrinsicObject;
    }

    public Service getService()
    {
        return this.service;
    }

    public void setService(Service service)
    {
        this.service = service;
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

    public int getLinkCount()
    {
        return this.linkList.size();
    }

    public int getIndex(ServiceDataLink link)
    {
        return this.linkList.indexOf(link);
    }

    public ServiceDataLink getLink(int index)
    {
        if (index < 0 || index >= this.linkList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.linkList.get(index);
    }

    public void setLink(int index, ServiceDataLink link)
    {
        if (index < 0 || index >= this.linkList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.linkList.set(index, link);
    }

    public void addLink(int index, ServiceDataLink link)
    {
        if (index < 0 || index > this.linkList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.linkList.add(index, link);
    }

    public void addLink(ServiceDataLink link)
    {
        this.linkList.add(link);
    }

    public void addLinks(Collection<? extends ServiceDataLink> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.linkList.addAll(c);
    }

    public void removeLink(int index)
    {
        if (index < 0 || index >= this.linkList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.linkList.remove(index);
    }

    public void clearLinks()
    {
        this.linkList.clear();
    }

    public Iterator<ServiceDataLink> getLinkIterator()
    {
        return this.linkList.iterator();
    }

    public DatasetDescription getDatasetDescription()
    {
        return this.datasetDescription;
    }

    public void setDatasetDescription(DatasetDescription datasetDescription)
    {
        this.datasetDescription = datasetDescription;
    }

    public Geometry getGeometry()
    {
        return this.geometry;
    }

    public void setGeometry(Geometry geometry)
    {
        this.geometry = geometry;
    }

    public CatalogExceptionList getExceptionList()
    {
        return this.exceptionList;
    }

    public void setExceptionList(CatalogExceptionList exceptionList)
    {
        this.exceptionList = exceptionList;
    }

    public void addException(CatalogException e)
    {
        if (this.exceptionList == null)
        {
            this.exceptionList = new CatalogExceptionList();
            if (getValue(CatalogKey.EXCEPTIONS) == null)
                setValue(CatalogKey.EXCEPTIONS, this.exceptionList);
        }
        this.exceptionList.addException(e);
    }
}
