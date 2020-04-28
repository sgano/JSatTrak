/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml.gx;

import gov.nasa.worldwind.ogc.kml.KMLUpdate;

/**
 * @author tag
 * @version $Id: GXAnimatedUpdate.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class GXAnimatedUpdate extends GXAbstractTourPrimitive
{
    public GXAnimatedUpdate(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getDuration()
    {
        return (Double) this.getField("duration");
    }

    public KMLUpdate getUpdate()
    {
        return (KMLUpdate) this.getField("Update");
    }
}
