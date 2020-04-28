/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Mercator.examples;

import com.sun.opengl.util.texture.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.awt.*;
import java.io.*;

public class VirtualEarthLogo implements OrderedRenderable
{
	public final static String NORTHWEST = "NorthWest";
	public final static String SOUTHWEST = "SouthWest";
	public final static String NORTHEAST = "NorthEast";
	public final static String SOUTHEAST = "SouthEast";

	public final static String RESIZE_STRETCH = "Stretch";
	public final static String RESIZE_SHRINK_ONLY = "ShrinkOnly";
	public final static String RESIZE_KEEP_FIXED_SIZE = "FixedSize";

	private String iconFilePath;
	private int iconWidth;
	private int iconHeight;
	private double iconScale = 1.0;
	private double toViewportScale = 1.0;
	private String resizeBehavior = RESIZE_SHRINK_ONLY;
	private String position = SOUTHWEST;
	private int borderWidth = 20;
	private int borderHeight = 20;
	private double opacity = 0.7;

	public VirtualEarthLogo()
	{
		iconFilePath = "logo_msve.png";
	}

	public double getDistanceFromEye()
	{
		return 0;
	}

	public void pick(DrawContext dc, Point pickPoint)
	{
		drawIcon(dc);
	}

	public void render(DrawContext dc)
	{
		drawIcon(dc);
	}

	private void drawIcon(DrawContext dc)
	{
		if (this.iconFilePath == null)
			return;

		GL gl = dc.getGL();

		boolean attribsPushed = false;
		boolean modelviewPushed = false;
		boolean projectionPushed = false;

		try
		{
			gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT
					| GL.GL_ENABLE_BIT | GL.GL_TEXTURE_BIT
					| GL.GL_TRANSFORM_BIT | GL.GL_VIEWPORT_BIT
					| GL.GL_CURRENT_BIT);
			attribsPushed = true;

			// Initialize texture if not done yet
			Texture iconTexture = dc.getTextureCache().get(this);
			if (iconTexture == null)
			{
				this.initializeTexture(dc);
				iconTexture = dc.getTextureCache().get(this);
				if (iconTexture == null)
				{
					// TODO: log warning
					return;
				}
			}

			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
			gl.glDisable(GL.GL_DEPTH_TEST);

			double width = this.getScaledIconWidth();
			double height = this.getScaledIconHeight();

			// Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
			// into the GL projection matrix.
			java.awt.Rectangle viewport = dc.getView().getViewport();
			gl.glMatrixMode(javax.media.opengl.GL.GL_PROJECTION);
			gl.glPushMatrix();
			projectionPushed = true;
			gl.glLoadIdentity();
			double maxwh = width > height ? width : height;
			gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh,
					0.6 * maxwh);

			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glPushMatrix();
			modelviewPushed = true;
			gl.glLoadIdentity();

			// Translate and scale
			double scale = this.computeScale(viewport);
			Vec4 locationSW = this.computeLocation(viewport, scale);
			gl.glTranslated(locationSW.x(), locationSW.y(), locationSW.z());
			// Scale to 0..1 space
			gl.glScaled(scale, scale, 1);
			gl.glScaled(width, height, 1d);

			if (!dc.isPickingMode())
			{
				// Draw world map icon
				gl.glColor4d(1d, 1d, 1d, this.getOpacity());
				gl.glEnable(GL.GL_TEXTURE_2D);
				iconTexture.bind();

				TextureCoords texCoords = iconTexture.getImageTexCoords();
				dc.drawUnitQuad(texCoords);
			}
		}
		finally
		{
			if (projectionPushed)
			{
				gl.glMatrixMode(GL.GL_PROJECTION);
				gl.glPopMatrix();
			}
			if (modelviewPushed)
			{
				gl.glMatrixMode(GL.GL_MODELVIEW);
				gl.glPopMatrix();
			}
			if (attribsPushed)
				gl.glPopAttrib();
		}
	}

	private void initializeTexture(DrawContext dc)
	{
		Texture iconTexture = dc.getTextureCache().get(this);
		if (iconTexture != null)
			return;

		try
		{
			InputStream iconStream = this.getClass().getResourceAsStream(
					this.iconFilePath);
			if (iconStream == null)
			{
				File iconFile = new File(this.iconFilePath);
				if (iconFile.exists())
				{
					iconStream = new FileInputStream(iconFile);
				}
			}

			iconTexture = TextureIO.newTexture(iconStream, true, null);
			iconTexture.bind();
			this.iconWidth = iconTexture.getWidth();
			this.iconHeight = iconTexture.getHeight();
			dc.getTextureCache().put(this, iconTexture);
		}
		catch (IOException e)
		{
			String msg = Logging
					.getMessage("layers.IOExceptionDuringInitialization");
			Logging.logger().severe(msg);
			throw new WWRuntimeException(msg, e);
		}

		GL gl = dc.getGL();
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
				GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
				GL.GL_CLAMP_TO_EDGE);
		// Enable texture anisotropy, improves "tilted" world map quality.
		/*int[] maxAnisotropy = new int[1];
		gl
				.glGetIntegerv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT,
						maxAnisotropy, 0);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
				maxAnisotropy[0]);*/
	}

	private double getScaledIconWidth()
	{
		return this.iconWidth * this.iconScale;
	}

	private double getScaledIconHeight()
	{
		return this.iconHeight * this.iconScale;
	}

	private double computeScale(java.awt.Rectangle viewport)
	{
		if (this.resizeBehavior.equals(RESIZE_SHRINK_ONLY))
		{
			return Math.min(1d, (this.toViewportScale) * viewport.width
					/ this.getScaledIconWidth());
		}
		else if (this.resizeBehavior.equals(RESIZE_STRETCH))
		{
			return (this.toViewportScale) * viewport.width
					/ this.getScaledIconWidth();
		}
		else if (this.resizeBehavior.equals(RESIZE_KEEP_FIXED_SIZE))
		{
			return 1d;
		}
		else
		{
			return 1d;
		}
	}

	private Vec4 computeLocation(java.awt.Rectangle viewport, double scale)
	{
		double width = this.getScaledIconWidth();
		double height = this.getScaledIconHeight();

		double scaledWidth = scale * width;
		double scaledHeight = scale * height;

		double x;
		double y;

		/*if (this.locationCenter != null)
		{
		    x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
		    y = viewport.getHeight() - scaledHeight / 2 - this.borderHeight;
		}
		else */if (this.position.equals(NORTHEAST))
		{
			x = viewport.getWidth() - scaledWidth - this.borderWidth;
			y = viewport.getHeight() - scaledHeight - this.borderHeight;
		}
		else if (this.position.equals(SOUTHEAST))
		{
			x = viewport.getWidth() - scaledWidth - this.borderWidth;
			y = 0d + this.borderHeight;
		}
		else if (this.position.equals(NORTHWEST))
		{
			x = 0d + this.borderWidth;
			y = viewport.getHeight() - scaledHeight - this.borderHeight;
		}
		else if (this.position.equals(SOUTHWEST))
		{
			x = 0d + this.borderWidth;
			y = 0d + this.borderHeight;
		}
		else
		// use North East
		{
			x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
			y = viewport.getHeight() - scaledHeight / 2 - this.borderHeight;
		}

		return new Vec4(x, y, 0);
	}

	public double getOpacity()
	{
		return opacity;
	}

	public void setOpacity(double opacity)
	{
		this.opacity = opacity;
	}
}
