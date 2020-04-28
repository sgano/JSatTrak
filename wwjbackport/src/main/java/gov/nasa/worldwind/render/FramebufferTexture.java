/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render;

import com.sun.opengl.util.texture.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: FramebufferTexture.java 12721 2009-10-14 19:57:40Z tgaskins $
 */
public class FramebufferTexture implements WWTexture
{
    protected WWTexture sourceTexture;
    protected Sector sector;
    protected List<LatLon> corners;

    protected int width;
    protected int height;
    protected TextureCoords textureCoords = new TextureCoords(0f, 0f, 1f, 1f);
    protected int tessellationDensity;

    protected static final int DEFAULT_TESSELLATION_DENSITY = 32;

    public FramebufferTexture(DrawContext dc, WWTexture imageSource, Sector sector, List<LatLon> corners)
    {
        if (imageSource == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sourceTexture = imageSource;
        this.sector = sector;
        this.corners = corners;

        // TODO: make this configurable
        this.tessellationDensity = DEFAULT_TESSELLATION_DENSITY;

        this.initialize(dc);
    }

    protected void initialize(DrawContext dc)
    {
        this.initializeTexture(dc);
    }

    public int getWidth(DrawContext dc)
    {
        return width;
    }

    public int getHeight(DrawContext dc)
    {
        return height;
    }

    public Sector getSector()
    {
        return sector;
    }

    public List<LatLon> getCorners()
    {
        return corners;
    }

    public boolean isTextureCurrent(DrawContext dc)
    {
        return dc.getTextureCache().get(this) != null;
    }

    public Object getImageSource()
    {
        return this.sourceTexture;
    }

    public TextureCoords getTexCoords()
    {
        return this.textureCoords;
    }

    public boolean isTextureInitializationFailed()
    {
        return false;
    }

    public boolean bind(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Texture t = dc.getTextureCache().get(this);
        if (t != null)
            t.bind();

        return t != null;
    }

    public void applyInternalTransform(DrawContext dc)
    {
    }

    protected int getTessellationDensity()
    {
        return this.tessellationDensity;
    }

    protected Texture initializeTexture(DrawContext dc)
    {
        GL gl = GLContext.getCurrent().getGL();

        // TODO: limit texture dimensions to size of source texture
        this.width = Math.min(1024, dc.getDrawableWidth());
        this.height = Math.min(1024, dc.getDrawableHeight());

        this.generateTexture(dc, this.width, this.height);

        TextureData td = new TextureData(GL.GL_RGBA, this.width, this.height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
            false, false, false, null, null);
        Texture t = TextureIO.newTexture(td);
        t.bind(); // must do this after generating texture because another texture is bound then

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

        gl.glCopyTexImage2D(GL.GL_TEXTURE_2D, 0, td.getInternalFormat(), 0, 0, td.getWidth(), td.getHeight(),
            td.getBorder());
        
        dc.getTextureCache().put(this, t);

        return t;
    }

    protected void generateTexture(DrawContext dc, int width, int height)
    {
        GL gl = dc.getGL();
        OGLStackHandler ogsh = new OGLStackHandler();

        Matrix geoToCartesian = this.computeGeographicToCartesianTransform(this.sector);

        try
        {
            ogsh.pushAttrib(gl, GL.GL_COLOR_BUFFER_BIT
                | GL.GL_ENABLE_BIT
                | GL.GL_TEXTURE_BIT
                | GL.GL_TRANSFORM_BIT
                | GL.GL_VIEWPORT_BIT);

            // Fill the framebuffer with transparent black.
            gl.glClearColor(0f, 0f, 0f, 0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);

            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_CULL_FACE);
            gl.glDisable(GL.GL_DEPTH_TEST);

            // Setup a viewport with the dimensions of the texture, and a projection matrix of dimension 2.0 (along
            // each axis) centered at the origin. Using a projection matrix with these dimensions ensures that incoming
            // vertices are rasterized without any rounding error.
            ogsh.pushProjectionIdentity(gl);
            gl.glViewport(0, 0, width, height);
            gl.glOrtho(-1d, 1d, -1d, 1d, -1d, 1d);

            ogsh.pushModelviewIdentity(gl);
            ogsh.pushTextureIdentity(gl);

            if (this.sourceTexture != null)
            {
                gl.glEnable(GL.GL_TEXTURE_2D);
                this.sourceTexture.bind(dc);
                this.sourceTexture.applyInternalTransform(dc);
                
                // Setup the texture to replace the fragment color at each pixel.
                gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

                int tessellationDensity = this.getTessellationDensity();
                this.drawQuad(dc, geoToCartesian, tessellationDensity, tessellationDensity);
            }
        }
        finally
        {
            ogsh.pop(gl);
        }
    }

    protected Matrix computeGeographicToCartesianTransform(Sector sector)
    {
        // Compute a transfrom that will map the geographic region defined by sector onto a cartesian region of width
        // and height 2.0 centered at the origin.

        double sx = 2.0 / sector.getDeltaLonDegrees();
        double sy = 2.0 / sector.getDeltaLatDegrees();

        double tx = -sector.getMinLongitude().degrees;
        double ty = -sector.getMinLatitude().degrees;

        Matrix transform = Matrix.IDENTITY;
        transform = transform.multiply(Matrix.fromTranslation(-1.0, -1.0, 0.0));
        transform = transform.multiply(Matrix.fromScale(sx, sy, 1.0));
        transform = transform.multiply(Matrix.fromTranslation(tx, ty, 0.0));
        
        return transform;
    }

    protected Vec4 transformToQuadCoordinates(Matrix geoToCartesian, LatLon latLon)
    {
        return new Vec4(latLon.getLongitude().degrees, latLon.getLatitude().degrees, 0.0).transformBy4(geoToCartesian);
    }

    protected void drawQuad(DrawContext dc, Matrix geoToCartesian, int slices, int stacks)
    {
        Vec4 ll = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(0));
        Vec4 lr = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(1));
        Vec4 ur = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(2));
        Vec4 ul = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(3));
        BilinearInterpolator interp = new BilinearInterpolator(ll, lr, ur, ul);

        GL gl = dc.getGL();

        gl.glBegin(GL.GL_TRIANGLE_STRIP);
        try
        {
            this.drawQuad(dc, interp, slices, stacks);
        }
        finally
        {
            gl.glEnd();
        }
    }

    protected void drawQuad(DrawContext dc, BilinearInterpolator interp, int slices, int stacks)
    {
        double[] compArray = new double[4];
        double du = 1.0f / (float) slices;
        double dv = 1.0f / (float) stacks;

        GL gl = dc.getGL();

        for (int vi = 0; vi < stacks; vi++)
        {
            double v = vi * dv;
            double vn = (vi + 1) * dv;

            if (vi != 0)
            {
                interp.interpolate(slices * du, v, compArray);
                gl.glTexCoord2d(slices * du, v);
                gl.glVertex3dv(compArray, 0);

                interp.interpolate(0, v, compArray);
                gl.glTexCoord2d(0, v);
                gl.glVertex3dv(compArray, 0);
            }

            for (int ui = 0; ui <= slices; ui++)
            {
                double u = ui * du;

                interp.interpolate(u, v, compArray);
                gl.glTexCoord2d(u, v);
                gl.glVertex3dv(compArray, 0);

                interp.interpolate(u, vn, compArray);
                gl.glTexCoord2d(u, vn);
                gl.glVertex3dv(compArray, 0);
            }
        }
    }
}
