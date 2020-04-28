/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml.gx;

/**
 * @author tag
 * @version $Id: GXTourControl.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class GXTourControl extends GXAbstractTourPrimitive
{
    public GXTourControl(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getPlayMode()
    {
        return (String) this.getField("playMode");
    }
}
