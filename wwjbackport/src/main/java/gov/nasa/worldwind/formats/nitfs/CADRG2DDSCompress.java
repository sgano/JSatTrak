package gov.nasa.worldwind.formats.nitfs;

import gov.nasa.worldwind.formats.dds.DDSConverter;
/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author Lado Garakanidze
 * @version $Id: Cadrg2DdsCompress Apr 23, 2007 11:00:07 AM lado
 */
class CADRG2DDSCompress extends AbstractRPF2DDSCompress
{
    public DDSBlock4x4 compressDxt1Block4x4(NITFSImageBand imageBand, byte[] pixelCodes, boolean hasTransparentPixels)
    {
        int[]   pixels565 = new int[16];
        Color[] colors565 = new Color[16];
        for(int i = 0; i < pixelCodes.length; i++ )
        {
            pixels565[i] = imageBand.lookupR5G6B5( 0x00FF & pixelCodes[i] );
            colors565[i] = DDSConverter.getColor565( pixels565[i] );
        }

        int[] extremaIndices = determineExtremeColors( colors565 );

        if(pixels565[extremaIndices[0]] < pixels565[extremaIndices[1]])
        {
            int t = extremaIndices[0];
            extremaIndices[0] = extremaIndices[1];
            extremaIndices[1] = t;
        }

        return  new DDSBlock4x4(
            (short) pixels565[extremaIndices[0]],
            (short) pixels565[extremaIndices[1]],
            (int) DDSConverter.computeBitMask(colors565, extremaIndices)
            );
    }
}
