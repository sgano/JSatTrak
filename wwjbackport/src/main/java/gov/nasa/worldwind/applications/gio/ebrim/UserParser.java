/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

/**
 * @author dcollins
 * @version $Id: UserParser.java 5472 2008-06-26 20:11:53Z dcollins $
 */
public class UserParser extends PersonParser implements User
{
    public static final String ELEMENT_NAME = "User";

    public UserParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
    }
}