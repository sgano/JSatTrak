/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author dcollins
 * @version $Id$
 */
public interface ServiceBinding extends RegistryObject
{
    int getSpecificationLinkCount();

    int getIndex(SpecificationLink sl);

    SpecificationLink getSpecificationLink(int index);

    void setSpecificationLink(int index, SpecificationLink sl);

    void addSpecificationLink(int index, SpecificationLink sl);

    void addSpecificationLink(SpecificationLink sl);

    void addSpecificationLinks(Collection<? extends SpecificationLink> c);

    void removeSpecificationLink(int index);

    void clearSpecificationLinks();

    Iterator<SpecificationLink> getSpecificationLinkIterator();

    String getService();
    
    void setService(String service);

    String getAccessURI();

    void setAccessURI(String accessURI);

    String getTargetBinding();

    void setTargetBinding(String targetBinding);
}
