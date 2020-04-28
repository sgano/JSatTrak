/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.render.DrawContext;

import java.awt.*;

/**
 * An object that can be rendered in a {@link ScrollFrame}.
 *
 * @author pabercrombie
 * @version $Id$
 * @see ScrollFrame
 */
public interface Scrollable
{
    /**
     * Render the scrollable component. The component should render itself in the rectangle specified by {@code bounds}.
     * Note that some of the bounds rectangle may be clipped by the scroll frame.
     *
     * @param dc     Draw context.
     * @param bounds Rectangle in which to render scrollable content. The rectangle is specified in OpenGL coordinates,
     *               with the origin at the lower left corner of the screen.
     */
    public void renderScrollable(DrawContext dc, Rectangle bounds);

    /**
     * Get the size of the object on screen.
     *
     * @param dc Draw context.
     *
     * @return The size of the scrollable object.
     */
    public Dimension getSize(DrawContext dc);

    public void setHighlighted(boolean highlighted);
}
