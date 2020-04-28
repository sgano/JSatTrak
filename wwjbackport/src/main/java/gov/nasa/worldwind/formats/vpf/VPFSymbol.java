/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: VPFSymbol.java 12503 2009-08-21 06:48:37Z dcollins $
 */
public class VPFSymbol
{
    protected VPFFeature feature;
    protected VPFSymbolAttributes attributes;
    protected Object mapObject;

    public VPFSymbol(VPFFeature feature, VPFSymbolAttributes attributes, Object mapObject)
    {
        this.feature = feature;
        this.attributes = attributes;
        this.mapObject = mapObject;
    }

    public VPFFeature getFeature()
    {
        return this.feature;
    }

    public VPFSymbolAttributes getAttributes()
    {
        return this.attributes;
    }

    public Object getMapObject()
    {
        return this.mapObject;
    }
}
