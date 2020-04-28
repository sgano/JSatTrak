/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render;

/**
 * @author tag
 * @version $Id: PreRenderable.java 8732 2009-02-03 17:35:26Z tgaskins $
 */
public interface PreRenderable
{
    void preRender(DrawContext dc);
}
