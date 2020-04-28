/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

/**
 * @author dcollins
 * @version $Id$
 */
public class AddressParser extends PostalAddressParser implements Address
{
    public static final String ELEMENT_NAME = "Address";

    public AddressParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
    }
}
