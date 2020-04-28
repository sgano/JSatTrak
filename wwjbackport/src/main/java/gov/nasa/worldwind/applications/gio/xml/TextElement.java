/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.xml;

import gov.nasa.worldwind.util.Logging;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Lado Garakanidze
 * @version $Id: TextElement.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class TextElement extends Element
{
    private String textValue;

    public TextElement(xmlns ns, String elementName, String textValue)
    {
        super(ns, elementName);
        this.textValue = textValue;
    }

    public TextElement(xmlns ns, String elementName)
    {
        super(ns, elementName);
        this.textValue = null;
    }

    public String getValue()
    {
        return this.textValue;
    }

    public void setValue(String textValue)
    {
        this.textValue = textValue;
    }

    public boolean hasContent()
    {
        return this.textValue != null && this.textValue.length() > 0;
    }

    protected void writeElementContent(Writer out) throws IOException
    {
        if (out == null)
        {
            String message = "nullValue.WriterIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        if (this.textValue != null)
        {
            out.write(this.textValue);
        }
    }
}
