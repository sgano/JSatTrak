/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.placename;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;

import java.util.*;

/**
 * @author Paul Collins
 * @version $Id: PlaceNameServiceSet.java 12471 2009-08-17 23:40:14Z tgaskins $
 */
public class PlaceNameServiceSet extends WWObjectImpl implements AVList
{
    private final List<PlaceNameService> serviceList = new LinkedList<PlaceNameService>();
    private long expiryTime = 0;

    public PlaceNameServiceSet()
    {
    }

    /**
     * @param placeNameService
     * @param replace
     * @return
     * @throws IllegalArgumentException if <code>placeNameService</code> is null
     */
    public boolean addService(PlaceNameService placeNameService, boolean replace)
    {
        if (placeNameService == null)
        {
            String message = Logging.getMessage("nullValue.PlaceNameServiceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < this.serviceList.size(); i++)
        {
            final PlaceNameService other = this.serviceList.get(i);
            if (placeNameService.getService().equals(other.getService()) && placeNameService.getDataset().equals(
                other.getDataset()))
            {
                if (replace)
                {
                    this.serviceList.set(i, placeNameService);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        this.serviceList.add(placeNameService);
        return true;
    }

    public PlaceNameServiceSet deepCopy()
    {
        PlaceNameServiceSet copy = new PlaceNameServiceSet();

        // Copy params
        copy.setValues(this);

        // Creates a deep copy of this.serviceList in copy.serviceList.
        for (int i = 0; i < this.serviceList.size(); i++)
        {
            copy.serviceList.add(i, this.serviceList.get(i).deepCopy());
        }

        copy.expiryTime = this.expiryTime;

        return copy;
    }

    public final int getServiceCount()
    {
        return this.serviceList.size();
    }

    public final PlaceNameService getService(int index)
    {
        return this.serviceList.get(index);
    }

    public final long getExpiryTime()
    {
        return this.expiryTime;
    }

    public final void setExpiryTime(long expiryTime)
    {
        this.expiryTime = expiryTime;
    }

    public List<PlaceNameService> getServices()
    {
        return serviceList;
    }

    public PlaceNameService getService(String name)
    {
        for (int i = 0; i < this.serviceList.size(); i++)
        {
            if (this.serviceList.get(i).getDataset().equalsIgnoreCase(name))
                return this.serviceList.get(i);
        }

        return null;
    }
}
