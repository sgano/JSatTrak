package gov.nasa.worldwind.formats.nitfs;
/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author Lado Garakanidze
 * @version $Id: NitfsSymbolSegment Mar 31, 2007 12:55:42 AM
 */
public class NITFSSymbolSegment extends NITFSSegment
{
    public NITFSSymbolSegment(java.nio.ByteBuffer buffer, int headerStartOffset, int headerLength, int dataStartOffset, int dataLength)
    {
        super(NITFSSegmentType.SYMBOL_SEGMENT, buffer, headerStartOffset, headerLength, dataStartOffset, dataLength);

        this.restoreBufferPosition();
    }
}
