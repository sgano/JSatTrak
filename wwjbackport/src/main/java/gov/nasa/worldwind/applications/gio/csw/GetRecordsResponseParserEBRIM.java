/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

/**
 * @author dcollins
 * @version $Id$
 */
public class GetRecordsResponseParserEBRIM extends GetRecordsResponseParser
{
    public GetRecordsResponseParserEBRIM(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
    }

    protected SearchResults doParseSearchResults(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        SearchResultsParserEBRIM parser = new SearchResultsParserEBRIM(name, attributes);
        setCurrentElement(parser);
        return parser;
    }
}
