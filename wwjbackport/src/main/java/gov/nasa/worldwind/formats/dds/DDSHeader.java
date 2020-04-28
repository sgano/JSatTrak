/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.dds;

import gov.nasa.worldwind.util.Logging;

/**
 * Documentation on the DDS header format is available at http://msdn.microsoft.com/en-us/library/bb943982(VS.85).aspx
 * 
 * @author dcollins
 * @version $Id: DDSHeader.java 8915 2009-02-19 21:31:36Z dcollins $
 */
public class DDSHeader
{
    private final int size;
    private int flags;
    private int width;
    private int height;
    private int linearSize;
    private int depth;
    private int mipMapCount;
    //private int[] reserved1 = new int[11]; // Unused
    private DDSPixelFormat pixelFormat;
    private int caps;
    private int caps2;
    private int caps3;
    private int caps4;
    //private int reserved2; // Unused

    public DDSHeader()
    {
        this.size = 124;
        this.pixelFormat = new DDSPixelFormat();
    }

    /**
     * Returns the size of the header structure in bytes. Will always return 124.
     * 
     * @return header size in bytes.
     */
    public final int getSize()
    {
        return this.size;
    }

    public int getFlags()
    {
        return this.flags;
    }

    public void setFlags(int flags)
    {
        this.flags = flags;
    }

    public int getWidth()
    {
        return this.width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getLinearSize()
    {
        return this.linearSize;
    }

    public void setLinearSize(int size)
    {
        this.linearSize = size;
    }

    public int getDepth()
    {
        return this.depth;
    }

    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    public int getMipMapCount()
    {
        return this.mipMapCount;
    }

    public void setMipMapCount(int mipMapCount)
    {
        this.mipMapCount = mipMapCount;
    }

    public DDSPixelFormat getPixelFormat()
    {
        return this.pixelFormat;
    }

    public void setPixelFormat(DDSPixelFormat pixelFormat)
    {
        if (pixelFormat == null)
        {
            String message = Logging.getMessage("nullValue.PixelFormatIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pixelFormat = pixelFormat;
    }

    public int getCaps()
    {
        return this.caps;
    }

    public void setCaps(int caps)
    {
        this.caps = caps;
    }

    public int getCaps2()
    {
        return this.caps2;
    }

    public void setCaps2(int caps)
    {
        this.caps2 = caps;
    }

    public int getCaps3()
    {
        return this.caps3;
    }

    public void setCaps3(int caps)
    {
        this.caps3 = caps;
    }

    public int getCaps4()
    {
        return this.caps4;
    }

    public void setCaps4(int caps)
    {
        this.caps4 = caps;
    }
}
