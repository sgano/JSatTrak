/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render;

import java.awt.*;

/**
 * @author tag
 * @version $Id: ScreenCredit.java 13058 2010-01-27 22:58:05Z tgaskins $
 */
public interface ScreenCredit extends Renderable
{
    void setViewport(Rectangle viewport);

    Rectangle getViewport();

    void setOpacity(double opacity);

    double getOpacity();

    void setLink(String link);

    String getLink();

    public void pick(DrawContext dc, java.awt.Point pickPoint);
}
