/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.xml.atom;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id: AtomAbstractObject.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class AtomAbstractObject extends AbstractXMLEventParser
{
    public AtomAbstractObject(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getBase()
    {
        return (String) this.getField("base");
    }

    public String getLang()
    {
        return (String) this.getField("lang");
    }
}
