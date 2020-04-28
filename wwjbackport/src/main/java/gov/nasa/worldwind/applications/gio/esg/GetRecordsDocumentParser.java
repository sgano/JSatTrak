/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.csw.GetRecordsResponse;
import gov.nasa.worldwind.applications.gio.csw.GetRecordsResponseParser;
import gov.nasa.worldwind.applications.gio.csw.GetRecordsResponseParserEBRIM;
import gov.nasa.worldwind.applications.gio.csw.SAXResponseParser;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: GetRecordsDocumentParser.java 5479 2008-06-30 09:16:17Z dcollins $
 */
public class GetRecordsDocumentParser extends SAXResponseParser
{
    private String schema;
    private GetRecordsResponse response;

    public GetRecordsDocumentParser(String schema)
    {
        if (schema == null)
        {
            String message = "nullValue.SchemaIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.schema = schema;
    }

    public String getSchema()
    {
        return this.schema;
    }

    public GetRecordsResponse getResponse()
    {
        return this.response;
    }

    protected void doStartDocument(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        if ("EBRIM".equalsIgnoreCase(this.schema))
        {
            GetRecordsResponseParserEBRIM parser = new GetRecordsResponseParserEBRIM(name, attributes);
            this.response = parser;
            setDocumentElement(parser);
        }
        else
        {
            GetRecordsResponseParser parser = new GetRecordsResponseParser(name, attributes);
            this.response = parser;
            setDocumentElement(parser);
        }
    }
}
