/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import gov.nasa.worldwind.applications.gio.xml.TextElementParser;

/**
 * @author dcollins
 * @version $Id$
 */
public class UsageParameterParser extends TextElementParser implements UsageParameter
{
    public static final String ELEMENT_NAME = "UsageParameter";
    
    public UsageParameterParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
    }
}
