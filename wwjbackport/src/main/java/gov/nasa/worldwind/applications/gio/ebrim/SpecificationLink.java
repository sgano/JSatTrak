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
public interface SpecificationLink
{
    UsageDescription getUsageDescription();

    void setUsageDescription(UsageDescription usageDescription);

    int getUsageParameterCount();

    int getIndex(UsageParameter p);

    UsageParameter getUsageParameter(int index);

    void setUsageParameter(int index, UsageParameter p);

    void addUsageParameter(int index, UsageParameter p);

    void addUsageParameter(UsageParameter p);

    void addUsageParameters(Collection<? extends UsageParameter> c);

    void removeUsageParameter(int index);

    void clearUsageParameters();

    Iterator<UsageParameter> getUsageParameterIterator();

    String getServiceBinding();

    void setServiceBinding(String serviceBinding);

    String getSpecificationObject();

    void setSpecificationObject(String specificationObject);
}
