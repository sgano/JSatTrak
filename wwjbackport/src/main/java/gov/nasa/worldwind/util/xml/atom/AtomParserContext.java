/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.xml.atom;

import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tag
 * @version $Id: AtomParserContext.java 13388 2010-05-19 17:44:46Z tgaskins $
 */
public class AtomParserContext extends BasicXMLEventParserContext
{
    protected static final String[] StringFields = new String[]
        {
            "base",
            "email",
            "lang",
            "name",
            "uri",
        };

    public static Map<QName, XMLEventParser> getDefaultParsers()
    {
        ConcurrentHashMap<QName, XMLEventParser> parsers = new ConcurrentHashMap<QName, XMLEventParser>();

        String ans = AtomConstants.ATOM_NAMESPACE;
        parsers.put(new QName(ans, "author"), new AtomPerson(ans));
        parsers.put(new QName(ans, "link"), new AtomLink(ans));

        StringXMLEventParser stringParser = new StringXMLEventParser();
        for (String s : StringFields)
        {
            parsers.put(new QName(ans, s), stringParser);
        }

        return parsers;
    }
}
