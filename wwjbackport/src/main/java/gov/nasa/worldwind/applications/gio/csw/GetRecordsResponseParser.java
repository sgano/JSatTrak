/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: GetRecordsResponseParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GetRecordsResponseParser extends ElementParser implements GetRecordsResponse
{
    private RequestId requestId;
    private RequestStatus searchStatus;
    private SearchResults searchResults;
    private String version;
    public static final String ELEMENT_NAME = "GetRecordsResponse";
    private static final String SEARCH_STATUS_ELEMENT_NAME = "SearchStatus";
    private static final String VERSION_ATTRIBUTE_NAME = "version";

    public GetRecordsResponseParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (VERSION_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.version = attributes.getValue(i);
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        if (RequestIdParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            this.requestId = doParseRequestId(name, attributes);
        }
        else if (SEARCH_STATUS_ELEMENT_NAME.equalsIgnoreCase(name))
        {
            this.searchStatus = doParseSearchStatus(name, attributes);
        }
        else if (SearchResultsParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            this.searchResults = doParseSearchResults(name, attributes);
        }
    }

    protected RequestId doParseRequestId(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        RequestIdParser parser = new RequestIdParser(name, attributes);
        setCurrentElement(parser);
        return parser;
    }

    protected RequestStatus doParseSearchStatus(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        RequestStatusParser parser = new RequestStatusParser(name, attributes);
        setCurrentElement(parser);
        return parser;
    }

    protected SearchResults doParseSearchResults(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        SearchResultsParser parser = new SearchResultsParser(name, attributes);
        setCurrentElement(parser);
        return parser;
    }

    public RequestId getRequestId()
    {
        return this.requestId;
    }

    public void setRequestId(RequestId requestId)
    {
        this.requestId = requestId;
    }

    public RequestStatus getSearchStatus()
    {
        return this.searchStatus;
    }

    public void setSearchStatus(RequestStatus searchStatus)
    {
        this.searchStatus = searchStatus;
    }

    public SearchResults getSearchResults()
    {
        return this.searchResults;
    }

    public void setSearchResults(SearchResults searchResults)
    {
        this.searchResults = searchResults;
    }

    public String getVersion()
    {
        return this.version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
