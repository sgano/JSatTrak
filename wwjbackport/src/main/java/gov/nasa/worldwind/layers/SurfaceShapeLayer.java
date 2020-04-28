/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

/**
 * The SurfaceShapeLayer class manages a collection of {@link gov.nasa.worldwind.render.Renderable} objects for
 * rendering, picking, and disposal. SurfaceShapeLayer is deprecated because its batch processing of {@link
 * gov.nasa.worldwind.render.SurfaceShape} has been replaced by functionality in {@link
 * gov.nasa.worldwind.render.SurfaceObject}. Replace usage of SurfaceShapeLayer with {@link RenderableLayer}.
 *
 * @author dcollins
 * @version $Id: SurfaceShapeLayer.java 13588 2010-07-27 16:22:35Z dcollins $
 * @deprecated Use {@link gov.nasa.worldwind.layers.RenderableLayer} instead.
 */
public class SurfaceShapeLayer extends RenderableLayer
{
    /** Creates a new SurfaceShapeLayer with an empty collection of Renderables. */
    public SurfaceShapeLayer()
    {
    }
}