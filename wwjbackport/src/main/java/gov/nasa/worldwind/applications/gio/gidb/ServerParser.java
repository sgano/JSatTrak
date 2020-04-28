/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.xml.ElementParser;


/**
 * @author dcollins
 * @version $Id: ServerParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ServerParser extends ElementParser implements Server
{
    private Text title;
    private Text url;
    public static final String ELEMENT_NAME = "server";
    private static final String TITLE_ELEMENT_NAME = "title";
    private static final String URL_ELEMENT_NAME = "url";

    public ServerParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        if (TITLE_ELEMENT_NAME.equalsIgnoreCase(name))
        {
            TextParser parser = new TextParser(name, attributes);
            this.title = parser;
            setCurrentElement(parser);
        }
        if (URL_ELEMENT_NAME.equalsIgnoreCase(name))
        {
            TextParser parser = new TextParser(name, attributes);
            this.url = parser;
            setCurrentElement(parser);
        }
    }

    public Text getTitle()
    {
        return this.title;
    }

    public void setTitle(Text title)
    {
        this.title = title;
    }

    public Text getURL()
    {
        return this.url;
    }

    public void setURL(Text url)
    {
        this.url = url;
    }
}
