/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: VPFPrimitiveDataFactory.java 12264 2009-07-14 18:47:22Z dcollins $
 */
public interface VPFPrimitiveDataFactory
{
    VPFPrimitiveData createPrimitiveData(VPFCoverage coverage);
}
