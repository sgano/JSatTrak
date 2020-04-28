/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: CatalogExceptionList.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class CatalogExceptionList implements Iterable<CatalogException>
{
    private List<CatalogException> exceptionList;

    public CatalogExceptionList()
    {
        this.exceptionList = new ArrayList<CatalogException>();
    }

    public int getExceptionCount()
    {
        return this.exceptionList.size();
    }

    public int getIndex(CatalogException cls)
    {
        return this.exceptionList.indexOf(cls);
    }

    public CatalogException getException(int index)
    {
        if (index < 0 || index >= this.exceptionList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.exceptionList.get(index);
    }

    public void setException(int index, CatalogException e)
    {
        if (index < 0 || index >= this.exceptionList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionList.set(index, e);
    }

    public void addException(int index, CatalogException e)
    {
        if (index < 0 || index > this.exceptionList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionList.add(index, e);
    }

    public void addException(CatalogException e)
    {
        this.exceptionList.add(e);
    }

    public void addExceptions(Collection<? extends CatalogException> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionList.addAll(c);
    }

    public void removeException(int index)
    {
        if (index < 0 || index >= this.exceptionList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionList.remove(index);
    }

    public void clearExceptions()
    {
        this.exceptionList.clear();
    }

    public Iterator<CatalogException> iterator()
    {
        return this.exceptionList.iterator();
    }
}
