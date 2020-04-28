package gov.nasa.worldwind.formats.nitfs;

import java.nio.ByteBuffer;
/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author lado
 * @version $Id: DDSBlock4x4 Apr 21, 2007 10:33:06 AM
 */
class DDSBlock4x4
{
    public short color0, color1;
    public int   bitmask;

    public DDSBlock4x4( short color0, short color1, int bitmask )
    {
        this.color0 = color0;
        this.color1 = color1;
        this.bitmask = bitmask;
    }

    public void writeTo(ByteBuffer buffer)
    {
        if(null != buffer)
        {
            buffer.putShort( this.color0 );
            buffer.putShort( this.color1 );
            buffer.putInt( this.bitmask );
        }
    }
}
