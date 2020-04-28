/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.dds;

/**
 * 32 bit 8888 ARGB color.
 *
 * @author dcollins
 * @version $Id: Color32.java 8856 2009-02-14 00:51:20Z dcollins $
 */
public class Color32
{
    /**
     * The alpha component.
     */
    public int a;
    /**
     * The red color component.
     */
    public int r;
    /**
     * The green color component.
     */
    public int g;
    /**
     * The blue color component.
     */
    public int b;

    /**
     * Creates a 32 bit 8888 ARGB color with all values set to 0.
     */
    public Color32()
    {
    }
}
