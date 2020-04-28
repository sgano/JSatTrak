/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.ebrim.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class SearchResultsParserEBRIM extends SearchResultsParser
{
    public SearchResultsParserEBRIM(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        super.doStartElement(name, attributes);

        if (AssociationParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            AssociationParser parser = new AssociationParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (ClassificationParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ClassificationParser parser = new ClassificationParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (ClassificationNodeParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ClassificationNodeParser parser = new ClassificationNodeParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (ClassificationSchemeParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ClassificationSchemeParser parser = new ClassificationSchemeParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (ExternalIdentifierParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ExternalIdentifierParser parser = new ExternalIdentifierParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (ExternalLinkParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ExternalLinkParser parser = new ExternalLinkParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (ExtrinsicObjectParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ExtrinsicObjectParser parser = new ExtrinsicObjectParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (OrganizationParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            OrganizationParser parser = new OrganizationParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (PersonParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            PersonParser parser = new PersonParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (ServiceParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ServiceParser parser = new ServiceParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (ServiceBindingParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ServiceBindingParser parser = new ServiceBindingParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
        else if (UserParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            UserParser parser = new UserParser(name, attributes);
            addRecord(parser);
            setCurrentElement(parser);
        }
    }
}
