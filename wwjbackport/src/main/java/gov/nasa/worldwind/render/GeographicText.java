/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: GeographicText.java 9571 2009-03-20 14:10:31Z jparsons $
 */
public interface GeographicText
{
    CharSequence getText();

    void setText(CharSequence text);

    Position getPosition();

    void setPosition(Position position);

    Font getFont();

    void setFont(Font font);

    Color getColor();

    void setColor(Color color);

    Color getBackgroundColor();

    void setBackgroundColor(Color background);

    boolean isVisible();

    void setVisible(boolean visible);

    void setPriority(double d);

    double getPriority();
}
