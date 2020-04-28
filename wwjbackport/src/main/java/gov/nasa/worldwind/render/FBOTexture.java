/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: FBOTexture.java 12721 2009-10-14 19:57:40Z tgaskins $
 */
public class FBOTexture extends FramebufferTexture
{
    public FBOTexture(DrawContext dc, WWTexture imageSource, Sector sector, List<LatLon> corners)
    {
        super(dc, imageSource, sector, corners);

        this.width = 1024;
        this.height = 1024;
    }

    @Override
    protected void initialize(DrawContext dc)
    {
    }

    public boolean isTextureCurrent(DrawContext dc)
    {
        return true;
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

        if (t == null)
            t = this.initializeTexture(dc);

        if (t != null)
            t.bind();

        return t != null;
    }

    protected Texture initializeTexture(DrawContext dc)
    {
        // Ensure that the source texture size is available so that the FBO can be sized to match the source image
        if (this.sourceTexture != null && dc.getTextureCache().get(sourceTexture.getImageSource()) == null)
        {
            // Limit FBO size to the max OGL size or 4k, whichever is smaller
            int maxSize = Math.min(dc.getGLRuntimeCapabilities().getMaxTextureSize(), 4096);

            sourceTexture.bind(dc); // bind loads the image and sets the width and height
            if (sourceTexture.getWidth(dc) != 0 && sourceTexture.getHeight(dc) != 0)
            {
                this.width = Math.min(maxSize, sourceTexture.getWidth(dc));
                this.height = Math.min(maxSize, sourceTexture.getHeight(dc));
            }
        }

        GL gl = GLContext.getCurrent().getGL();

        int[] fbo = new int[1];
        gl.glGenFramebuffersEXT(1, fbo, 0);
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbo[0]);

        TextureData td = new TextureData(GL.GL_RGBA, this.width, this.height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
            false, false, true, BufferUtil.newByteBuffer(this.width * this.height * 4), null);
        Texture t = TextureIO.newTexture(td);
        t.bind();

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

        gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D,
            t.getTextureObject(), 0);

        int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
        if (status == GL.GL_FRAMEBUFFER_COMPLETE_EXT)
        {
            this.generateTexture(dc, this.width, this.height);
            gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
            gl.glDeleteFramebuffersEXT(1, fbo, 0);
        }
        else
        {
            throw new IllegalStateException("Frame Buffer Oject not created.");
        }

        dc.getTextureCache().put(this, t);

        return t;
    }
}
