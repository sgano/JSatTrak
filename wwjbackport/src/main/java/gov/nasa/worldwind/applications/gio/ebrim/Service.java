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
public interface Service extends RegistryObject
{
    int getServiceBindingCount();

    int getIndex(ServiceBinding sb);

    ServiceBinding getServiceBinding(int index);

    void setServiceBinding(int index, ServiceBinding sb);

    void addServiceBinding(int index, ServiceBinding sb);

    void addServiceBinding(ServiceBinding sb);

    void addServiceBindings(Collection<? extends ServiceBinding> c);

    void removeServiceBinding(int index);

    void clearServiceBindings();

    Iterator<ServiceBinding> getServiceBindingIterator();
}
