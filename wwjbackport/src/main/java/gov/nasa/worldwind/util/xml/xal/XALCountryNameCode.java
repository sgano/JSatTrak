/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.xml.xal;

/**
 * @author tag
 * @version $Id: XALCountryNameCode.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class XALCountryNameCode extends XALAbstractObject
{
    public XALCountryNameCode(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getScheme()
    {
        return (String) this.getField("Scheme");
    }
}
