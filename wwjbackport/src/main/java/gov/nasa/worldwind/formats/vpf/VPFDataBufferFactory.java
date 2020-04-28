/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: VPFDataBufferFactory.java 12459 2009-08-14 20:13:09Z dcollins $
 */
public interface VPFDataBufferFactory
{
    VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow);
}
