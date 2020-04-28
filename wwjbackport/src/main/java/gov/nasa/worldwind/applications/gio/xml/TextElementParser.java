/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.xml;

/**
 * @author dcollins
 * @version $Id: TextElementParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class TextElementParser extends ElementParser
{
    private String textValue;

    public TextElementParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
    }

    protected void doEndElement(String name)
    {
        String s = getCharacters();
        if (s != null)
            this.textValue = s;
    }

    public String getValue()
    {
        return this.textValue;
    }

    public void setValue(String textValue)
    {
        this.textValue = textValue;
    }
}
