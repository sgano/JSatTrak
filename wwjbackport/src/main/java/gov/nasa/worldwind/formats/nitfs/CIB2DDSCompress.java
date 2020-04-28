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
 * @version $Id: Cib2DdsCompress Apr 23, 2007 10:59:01 AM lado
 */
class CIB2DDSCompress extends AbstractRPF2DDSCompress
{
    public DDSBlock4x4 compressDxt1Block4x4(NITFSImageBand imageBand, byte[] pixelCodes, boolean hasTransparentPixels)
    {
        int[]   grayPixels = new int[16];
        int minColor = Integer.MAX_VALUE;
        int maxColor = Integer.MIN_VALUE;

        for(int i = 0; i < pixelCodes.length; i++ )
        {
            grayPixels[i] = imageBand.lookupGray( 0xFF & pixelCodes[i]);
            if(grayPixels[i] < minColor)
                minColor = grayPixels[i];
            if(grayPixels[i] > maxColor)
                maxColor = grayPixels[i];
        }

        DDSBlock4x4 ddsBlock = new DDSBlock4x4(
            (short) DDSConverter.getPixel565( new Color( maxColor, maxColor, maxColor ) ),
            (short) DDSConverter.getPixel565( new Color( minColor, minColor, minColor ) ),
            0);

        if(maxColor != minColor)
        {
            int[] ext = new int[] { maxColor, minColor, (2 * maxColor + minColor)/3, (maxColor + 2 * minColor)/3 };
            ddsBlock.bitmask = 0;
            for (int i = 0; i < grayPixels.length; i++)
            {
                int closest = Integer.MAX_VALUE;
                int mask = 0;
                for (int j = 0; j < ext.length; j++)
                {
                    int d = ( ext[j] >= grayPixels[i] ) ? (ext[j] - grayPixels[i]) : (grayPixels[i] - ext[j]);
                    if (d < closest)
                    {
                        closest = d;
                        mask = j;
                    }
                }
                ddsBlock.bitmask |= mask << i * 2;
            }
        }
        return  ddsBlock;
    }
}
