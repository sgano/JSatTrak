/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.event;

import gov.nasa.worldwind.geom.Position;

import java.util.EventObject;

/**
 * @author tag
 * @version $Id: PositionEvent.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class PositionEvent extends EventObject
{
    private final java.awt.Point screenPoint;
    private final Position position;
    private final Position previousPosition;

    public PositionEvent(Object source, java.awt.Point screenPoint, Position previousPosition, Position position)
    {
        super(source);
        this.screenPoint = screenPoint;
        this.position = position;
        this.previousPosition = previousPosition;
    }

    public java.awt.Point getScreenPoint()
    {
        return screenPoint;
    }

    public Position getPosition()
    {
        return position;
    }

    public Position getPreviousPosition()
    {
        return previousPosition;
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + " "
            + (this.previousPosition != null ? this.previousPosition : "null")
            + " --> "
            + (this.position != null ? this.position : "null");
    }
}
