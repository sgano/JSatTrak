/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.event;

import gov.nasa.worldwind.pick.PickedObjectList;

import java.awt.event.*;

/**
 * @author tag
 * @version $Id: DragSelectEvent.java 2426 2007-07-26 00:14:16Z tgaskins $
 */
public class DragSelectEvent extends SelectEvent
{
    private final java.awt.Point previousPickPoint;

    public DragSelectEvent(Object source, String eventAction, MouseEvent mouseEvent, PickedObjectList pickedObjects,
        java.awt.Point previousPickPoint)
    {
        super(source, eventAction, mouseEvent, pickedObjects);
        this.previousPickPoint = previousPickPoint;
    }

    public java.awt.Point getPreviousPickPoint()
    {
        return this.previousPickPoint;
    }
}
