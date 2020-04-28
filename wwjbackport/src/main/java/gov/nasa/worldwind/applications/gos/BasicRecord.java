/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: BasicRecord.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class BasicRecord extends AVListImpl implements Record
{
    protected String uuid;
    protected String title;
    protected Sector sector;
    protected long modifiedTime;
    protected String abstractText;
    protected Map<String, OnlineResource> resourceMap;
    protected ShapeAttributes shapeAttributes;

    public BasicRecord(String uuid, String title, Sector sector, long modifiedTime, String abstractText,
        Map<String, OnlineResource> resourceMap, ShapeAttributes shapeAttributes)
    {
        this.uuid = uuid;
        this.title = title;
        this.sector = sector;
        this.modifiedTime = modifiedTime;
        this.abstractText = abstractText;
        this.resourceMap = resourceMap;
        this.shapeAttributes = shapeAttributes;
    }

    public String getIdentifier()
    {
        return this.uuid;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getType()
    {
        return null;
    }

    public Sector getSector()
    {
        return this.sector;
    }

    public long getModifiedTime()
    {
        return this.modifiedTime;
    }

    public String getAbstract()
    {
        return this.abstractText;
    }

    public OnlineResource getResource(String key)
    {
        return this.resourceMap.get(key);
    }

    public Iterable<OnlineResource> getResources()
    {
        return this.resourceMap.values();
    }

    public Iterable<LatLon> getLocations()
    {
        return (this.sector != null) ? this.sector.asList() : null;
    }

    public ShapeAttributes getShapeAttributes()
    {
        return this.shapeAttributes;
    }
}
