/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL;
import java.io.*;
import java.nio.*;

/**
 * Renders a star background based on a subset of ESA Hipparcos catalog.
 *
 * @author Patrick Murris
 * @version $Id: StarsLayer.java 13802 2010-09-15 08:31:18Z tgaskins $
 */
public class StarsLayer extends RenderableLayer
{
    // TODO: make configurable
    protected String starsFileName = "config/Hipparcos_Stars_Mag6x5044.dat";
    protected FloatBuffer starsBuffer = null;
    protected int starsBufferId = 0;
    protected int numStars = 0;
    protected boolean rebuild = false;            // True if need to rebuild GL list
    protected Double radius; // radius is either set explicitly or taken from the star file
    protected Angle longitudeOffset = Angle.ZERO;    // Star sphere rotation longitude
    protected Angle latitudeOffset = Angle.ZERO;    // Star sphere rotation latitude

    /** A RenderableLayer that displays a star background */
    public StarsLayer()
    {
        this.initialize(null, null);
    }

    /**
     * A RenderableLayer that displays a star background
     *
     * @param starsFileName the path and filename of the star catalog file
     */
    public StarsLayer(String starsFileName)
    {
        this.initialize(starsFileName, null);
    }

    /**
     * A RenderableLayer that displays a star background
     *
     * @param starsFileName the path and filename of the star catalog file.
     * @param radius        the radius of the stars sphere. May be null, in which case the radius in the stars file is
     *                      used.
     */
    public StarsLayer(String starsFileName, Double radius)
    {
        if (starsFileName == null || starsFileName.length() == 0)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.initialize(starsFileName, radius);
    }

    protected void initialize(String starsFileName, Double radius)
    {
        if (starsFileName != null)
            this.setStarsFileName(starsFileName);

        if (radius != null)
            this.radius = radius;

        // Turn the layer off to eliminate its overhead when the user zooms in.
        this.setMinActiveAltitude(100e3);
    }

    // Public properties

    /**
     * Get the path and filename of the stars catalog file.
     *
     * @return name of stars catalog file.
     */
    public String getStarsFileName()
    {
        return this.starsFileName;
    }

    /**
     * Set the path and filename of the stars catalog file.
     *
     * @param fileName the path and filename
     */
    public void setStarsFileName(String fileName)
    {
        if (fileName == null || fileName.length() == 0)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.starsFileName = fileName;
        this.rebuild = true;
    }

    /**
     * Returns the latitude offset or relative tilt for the star sphere.
     *
     * @return the latitude offset.
     */
    public Angle getLatitudeOffset()
    {
        return this.latitudeOffset;
    }

    /**
     * Sets the latitude offset or relative tilt of the star sphere.
     *
     * @param offset the latitude offset.
     */
    public void setLatitudeOffset(Angle offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.latitudeOffset = offset;
    }

    /**
     * Returns the longitude offset or rotation of the star sphere.
     *
     * @return the longitude offset.
     */
    public Angle getLongitudeOffset()
    {
        return this.longitudeOffset;
    }

    /**
     * Sets the longitude offset or rotation of the star sphere.
     *
     * @param offset the longitude offset.
     */
    public void setLongitudeOffset(Angle offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.longitudeOffset = offset;
    }

    @Override
    public void doRender(DrawContext dc)
    {
        GL gl = dc.getGL();
        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        // Load or reload stars if needed
        if ((this.starsBuffer == null && this.starsBufferId == 0) || this.rebuild)
        {
            this.loadStars(dc); // Create glList
            this.rebuild = false;
        }

        // Still no stars to render ?
        if (this.starsBuffer == null && this.starsBufferId == 0)
            return;

        try
        {
            // GL set up
            // Save GL state
/*            gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT
                | GL.GL_POLYGON_BIT | GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT
                | GL.GL_CURRENT_BIT);    */
            gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_POLYGON_BIT);
            attribsPushed = true;
            gl.glDisable(GL.GL_TEXTURE_2D);        // no textures
            gl.glDisable(GL.GL_DEPTH_TEST);        // no depth testing

            // Set far clipping far enough - is this the right way to do it ?
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            double ditanceFromOrigin = dc.getView().getEyePoint().getLength3();
            //noinspection UnnecessaryLocalVariable
            double near = ditanceFromOrigin;
            double far = this.radius + ditanceFromOrigin;
            dc.getGLU().gluPerspective(dc.getView().getFieldOfView().degrees,
                dc.getView().getViewport().getWidth() / dc.getView().getViewport().getHeight(),
                near, far);

            // Rotate sphere
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glRotatef((float) this.longitudeOffset.degrees, 0.0f, 1.0f, 0.0f);
            gl.glRotatef((float) -this.latitudeOffset.degrees, 1.0f, 0.0f, 0.0f);

            // Draw
            gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);

            if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
            {
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, this.starsBufferId);
                gl.glInterleavedArrays(GL.GL_C3F_V3F, 0, 0);
                gl.glDrawArrays(GL.GL_POINTS, 0, this.numStars);
            }
            else
            {
                gl.glInterleavedArrays(GL.GL_C3F_V3F, 0, this.starsBuffer);
                gl.glDrawArrays(GL.GL_POINTS, 0, this.numStars);
            }

            gl.glPopClientAttrib();
        }
        finally
        {
            // Restore GL state
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (projectionPushed)
            {
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (attribsPushed)
                gl.glPopAttrib();
        }
    }

    /**
     * Read stars catalog file and draw into a glList
     *
     * @param dc the current DrawContext
     */
    protected void loadStars(DrawContext dc)
    {
        ByteBuffer byteBuffer = null;

        if (WWIO.getSuffix(this.starsFileName).equals("dat"))
        {
            try
            {
                //Try loading from a resource
                InputStream starsStream = WWIO.openFileOrResourceStream(this.starsFileName, this.getClass());
                if (starsStream == null)
                {
                    String message = Logging.getMessage("layers.StarLayer.CannotReadStarFile");
                    Logging.logger().severe(message);
                    return;
                }

                //Read in the binary buffer
                try
                {
                    byteBuffer = WWIO.readStreamToBuffer(starsStream, true); // Read stars to a direct ByteBuffer.
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                }
                finally
                {
                    WWIO.closeStream(starsStream, starsFileName);
                }
            }
            catch (IOException e)
            {
                String message = "IOException while loading stars data from " + this.starsFileName;
                Logging.logger().severe(message);
            }
        }
        else
        {
            //Assume it is a tsv text file
            byteBuffer = StarsConvertor.convertTsvToByteBuffer(this.starsFileName);
        }

        if (byteBuffer == null)
        {
            String message = "IOException while loading stars data from " + this.starsFileName;
            Logging.logger().severe(message);
            return;
        }

        //Grab the radius from the first value in the buffer
        if (this.radius == null)
            this.radius = (double) byteBuffer.getFloat();
        else
            byteBuffer.getFloat(); // skip over it

        //View the rest of the ByteBuffer as a FloatBuffer
        this.starsBuffer = byteBuffer.asFloatBuffer();

        //byteBuffer is Little-Endian. If native order is not Little-Endian, switch to Big-Endian.
        if (byteBuffer.order() != ByteOrder.nativeOrder())
        {
            //tmpByteBuffer is allocated as Big-Endian on all systems
            ByteBuffer tmpByteBuffer = ByteBuffer.allocateDirect(byteBuffer.limit());

            //View it as a Float Buffer
            FloatBuffer fbuffer = tmpByteBuffer.asFloatBuffer();

            //Fill it with the floats in starsBuffer
            for (int i = 0; i < fbuffer.limit(); i++)
            {
                fbuffer.put(this.starsBuffer.get(i));
            }

            fbuffer.flip();

            //Make the starsBuffer the Big-Endian buffer
            this.starsBuffer = fbuffer;
        }

        //Number of stars = limit / 6 floats per star -> (R,G,B,X,Y,Z)
        this.numStars = this.starsBuffer.limit() / 6;

        if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
        {
            GL gl = dc.getGL();

            //Create a new bufferId
            int glBuf[] = new int[1];
            gl.glGenBuffers(1, glBuf, 0);
            starsBufferId = glBuf[0];

            gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, starsBufferId);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, this.starsBuffer.limit() * 4, this.starsBuffer, GL.GL_STATIC_DRAW);

            gl.glPopClientAttrib();

            //The buffer is no longer needed.
            this.starsBuffer = null;
        }
    }

    public void dispose()
    {

    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.StarsLayer.Name");
    }
}
