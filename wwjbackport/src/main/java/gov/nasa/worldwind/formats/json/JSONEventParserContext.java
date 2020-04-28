/*
Copyright (C) 2001, 2010 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.formats.json;

import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: JSONEventParserContext.java 13952 2010-10-07 23:01:26Z dcollins $
 */
public interface JSONEventParserContext
{
    boolean hasNext();

    JSONEvent nextEvent() throws IOException;

    JSONEvent peek();

    String getCurrentFieldName();

    void pushFieldName(String name);

    void popFieldName();

    JSONEventParser allocate(JSONEvent event);

    JSONEventParser getUnrecognizedParser();

    void registerParser(String fieldName, BasicJSONEventParser parser);
}
