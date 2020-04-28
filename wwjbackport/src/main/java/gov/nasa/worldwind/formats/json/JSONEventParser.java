/*
Copyright (C) 2001, 2010 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.formats.json;

import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: JSONEventParser.java 13564 2010-07-21 04:09:33Z dcollins $
 */
public interface JSONEventParser
{
    Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException;
}
