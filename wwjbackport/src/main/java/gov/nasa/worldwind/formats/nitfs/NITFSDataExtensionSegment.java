package gov.nasa.worldwind.formats.nitfs;
/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author Lado Garakanidze
 * @version $Id: NitfsDataExtensionSegment Mar 31, 2007 1:01:41 AM
 */
class NITFSDataExtensionSegment extends NITFSSegment
{
    public NITFSDataExtensionSegment(java.nio.ByteBuffer buffer,
        int headerStartOffset, int headerLength,
        int dataStartOffset, int dataLength)
    {
        super(NITFSSegmentType.DATA_EXTENSION_SEGMENT, buffer, headerStartOffset, headerLength, dataStartOffset, dataLength);
    }
}
