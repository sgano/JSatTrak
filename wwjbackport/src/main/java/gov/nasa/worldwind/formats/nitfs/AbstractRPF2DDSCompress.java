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
 * @version $Id: AbstractRpf2DdsCompress Apr 23, 2007 11:06:16 AM lado
 */
abstract class AbstractRPF2DDSCompress extends DDSConverter implements RPF2DDSCompress
{
    private static DDSBlock4x4 Dxt1TransparentBlock4x4 = new DDSBlock4x4( (short)0, (short)0, 0xFFFFFFFF );

    public DDSBlock4x4 getDxt1TransparentBlock4x4()
    {
        return Dxt1TransparentBlock4x4;
    }

    public void writeDxt1Header(java.nio.ByteBuffer buffer, int width, int height)
    {
        DDSConverter.buildHeaderDxt1( buffer, width, height );
    }
}
