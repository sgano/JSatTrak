/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.xml.TextElementParser;
import org.xml.sax.Attributes;

/**
 * @author dcollins
 * @version $Id: TextParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class TextParser extends TextElementParser implements Text
{
    public TextParser(String elementName, Attributes attributes)
    {
        super(elementName, attributes);
    }
}
