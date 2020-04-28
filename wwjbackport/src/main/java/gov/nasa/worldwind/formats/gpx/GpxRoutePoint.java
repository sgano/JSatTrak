/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.formats.gpx;

/**
 * @author tag
 * @version $Id: GpxRoutePoint.java 12424 2009-08-06 01:09:22Z tgaskins $
 */
public class GpxRoutePoint extends GpxTrackPoint
{
    public GpxRoutePoint(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
    {
        super("rtept", uri, lname, qname, attributes);
    }
}
