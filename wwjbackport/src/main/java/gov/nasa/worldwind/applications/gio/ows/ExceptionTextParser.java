/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ows;

import gov.nasa.worldwind.applications.gio.xml.TextElementParser;

/**
 * @author dcollins
 * @version $Id: ExceptionTextParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ExceptionTextParser extends TextElementParser implements ExceptionText
{
    public static final String ELEMENT_NAME = "ExceptionText";
    
    public ExceptionTextParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
    }

    public String getText()
    {
        return getValue();
    }

    public void setText(String text)
    {
        setValue(text);
    }
}
