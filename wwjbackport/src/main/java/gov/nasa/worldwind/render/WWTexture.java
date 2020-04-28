/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render;

import com.sun.opengl.util.texture.*;

/**
 * Represents a texture derived from an image source such as an image file or a {@link java.awt.image.BufferedImage}.
 *
 * The interface contains a method, {@link #isTextureInitializationFailed()} to determine whether the instance failed to
 * convert an image source to a texture. If such a failure occurs, the method returns true and no further attempts are
 * made to create the texture.
 *
 * @author tag
 * @version $Id: WWTexture.java 12721 2009-10-14 19:57:40Z tgaskins $
 */
public interface WWTexture
{
    /**
     * Returns the texture's image source.
     *
     * @return the texture's image source.
     */
    public Object getImageSource();

    /**
     * Makes the texture the current texture for rendering. If the texture has not been created from its image source,
     * creates the texture from its source. <p/> If a texture cannot be created from its image source it cannot be
     * bound. This method returns an indication of whether the texture was bound or was not bound due to a failure
     * during creation.
     *
     * @param dc the current draw context.
     *
     * @return true if the texture was bound, otherwise false.
     */
    public boolean bind(DrawContext dc);

    /**
     * Applies any necessary transformations to the texture prior to its being rendered. A common transformation is
     * mapping texture coordinates from a flipped or non-square state to conventionally oriented OpenGL values.
     *
     * @param dc the current draw context.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public void applyInternalTransform(DrawContext dc);

    /**
     * Indicates whether the texture is currently available for use without regenerating it from its image source.
     *
     * @param dc the current draw context
     *
     * @return true if the texture is available and consistent with its image source, otherwise false.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    boolean isTextureCurrent(DrawContext dc);

    /**
     * Returns the texture's width.
     *
     * @param dc the current draw context
     *
     * @return the texture's width, or 0 if the texture's size is currently unknown.
     */
    int getWidth(DrawContext dc);

    /**
     * Returns the texture's height
     *
     * @param dc the current draw context
     *
     * @return the texture's height, or 0 if the texture's size is currently unknown.
     */
    int getHeight(DrawContext dc);

    /**
     * Returns the texture's texture coordinates, which may be other than [0,0],[1,1] if the texture size is not a power
     * of two or the texture must be flipped when rendered.
     *
     * @return returns the texture's texture coordinates.
     */
    TextureCoords getTexCoords();

    /**
     * Indicates whether an attempt to initialize the texture failed, which occurs when the image source is a
     * non-existent image file or for other reasons specific to the image source.
     *
     * @return true if texture initialization failed, otherwise false.
     */
    boolean isTextureInitializationFailed();
}
