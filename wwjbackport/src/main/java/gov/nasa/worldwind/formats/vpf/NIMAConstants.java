/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: NIMAConstants.java 11938 2009-06-26 05:28:16Z dcollins $
 */
public interface NIMAConstants
{
    // Reserved Directory Names
    // MIL-PRF-0089049, section 3.16
    final String REFERENCE_LIBRARY = "rference";
    final String DATABASE_REFERENCE_COVERAGE = "dbref";

    // Reserved File Names
    // MIL-PRF-0089049, section 3.16.4.2.1
    final String NOTES_RELATED_ATTRIBUTE_TABLE = "notes.rat";
    final String SYMBOL_RELATED_ATTRIBUTE_TABLE = "symbol.rat";

    // Reserved Table Name Extensions
    final String NOTES_RELATED_JOIN_TABLE = ".njt";
}
