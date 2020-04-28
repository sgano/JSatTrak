/* Copyright (C) 2001, 2008 United States Government as represented by
   the Administrator of the National Aeronautics and Space Administration.
   All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.airspaces.*;

import javax.media.opengl.*;

/**
 * @author brownrigg
 * @version $Id: SurfaceCircle2.java 9230 2009-03-06 05:36:26Z dcollins $
 */

public class SurfaceCircle2 extends CappedCylinder
{
    public SurfaceCircle2(LatLon location, double radius)
    {
        super(location, radius);
    }

    public SurfaceCircle2(AirspaceAttributes shapeAttributes)
    {
        super(shapeAttributes);
    }

    public SurfaceCircle2()
    {
        super();
    }

    protected void doRenderGeometry(DrawContext dc, String drawStyle)
    {
        beginDrawShape(dc);
        super.doRenderGeometry(dc, drawStyle);
        endDrawShape(dc);
    }

    protected void beginDrawShape(DrawContext dc)
    {
        // Modify the projection transform to shift the depth values slightly toward the camera in order to
        // ensure the shape is selected during depth buffering.
        GL gl = dc.getGL();

        float[] pm = new float[16];
        gl.glGetFloatv(GL.GL_PROJECTION_MATRIX, pm, 0);
        pm[10] *= .8; // TODO: See Lengyel 2 ed. Section 9.1.2 to compute optimal/minimal offset

        gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadMatrixf(pm, 0);
    }

    protected void endDrawShape(DrawContext dc)
    {
        GL gl = dc.getGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glPopAttrib();
    }
}
