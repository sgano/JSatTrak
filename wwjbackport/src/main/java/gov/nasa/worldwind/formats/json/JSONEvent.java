/*
Copyright (C) 2001, 2010 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.formats.json;

/**
 * @author dcollins
 * @version $Id: JSONEvent.java 13564 2010-07-21 04:09:33Z dcollins $
 */
public interface JSONEvent
{
    boolean isStartObject();

    boolean isEndObject();

    boolean isStartArray();

    boolean isEndArray();

    boolean isFieldName();

    boolean isScalarValue();

    boolean isNumericValue();

    String getFieldName();

    Object asScalarValue();

    double asNumericValue();
}
