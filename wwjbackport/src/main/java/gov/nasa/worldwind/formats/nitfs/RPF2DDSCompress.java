package gov.nasa.worldwind.formats.nitfs;
/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author Lado Garakanidze
 * @version $Id: Rpf2DdsCompress Apr 23, 2007 10:56:39 AM lado
 */
interface RPF2DDSCompress
{
    DDSBlock4x4     getDxt1TransparentBlock4x4  ();
    void            writeDxt1Header             (java.nio.ByteBuffer buffer, int width, int height);
    DDSBlock4x4     compressDxt1Block4x4        (NITFSImageBand imageBand, byte[] pixelCodes, boolean hasTransparentPixels );
}
