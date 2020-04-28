/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: VPFRecord.java 12476 2009-08-18 07:56:38Z dcollins $
 */
public interface VPFRecord
{
    int getId();

    boolean hasValue(String parameterName);

    Object getValue(String parameterName);
}
