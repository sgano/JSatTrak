/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.xml;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: ElementParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ElementParser
{
    private String elementName;
    private StringBuilder currentCharacters = null;
    private ElementParser currentElement = null;

    @SuppressWarnings({"UnusedDeclaration"})
    public ElementParser(String elementName, org.xml.sax.Attributes attributes)
    {
        if (elementName == null)
        {
            String message = Logging.getMessage("nullValue.ElementNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.elementName = elementName;
    }

    public String getElementName()
    {
        return this.elementName;
    }

    public String getCharacters()
    {
        return this.currentCharacters != null ? this.currentCharacters.toString() : null;
    }

    public ElementParser getCurrentElement()
    {
        return this.currentElement;
    }

    public void setCurrentElement(ElementParser currentElement)
    {
        this.currentElement = currentElement;
    }

    public void startElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        if (name == null)
        {
            String message = "nullValue.NameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = "nullValue.AttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.currentElement != null)
            this.currentElement.startElement(name, attributes);
        else
            this.doStartElement(name, attributes);
    }

    public void endElement(String name) throws Exception
    {
        if (name == null)
        {
            String message = "nullValue.NameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.currentElement != null)
        {
            this.currentElement.endElement(name);
            if (name.equalsIgnoreCase(this.currentElement.elementName))
                this.currentElement = null;
        }

        this.doEndElement(name);
        this.currentCharacters = null;
    }

    public void characters(char[] data, int offset, int count) throws Exception
    {
        if (data == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (offset < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "offset=" + offset);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (count < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "count=" + count);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (offset + count > data.length)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "offset+count=" + count);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.currentElement != null)
            this.currentElement.characters(data, offset, count);
        else
            this.doCharacters(data, offset, count);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doEndElement(String name) throws Exception
    {
    }

    protected void doCharacters(char[] data, int offset, int count) throws Exception
    {
        if (this.currentCharacters == null)
            this.currentCharacters = new StringBuilder();
        this.currentCharacters.append(data, offset, count);
    }
}
