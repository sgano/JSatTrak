/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.event;

import java.util.EventListener;

/**
 * @author tag
 * @version $Id: SelectListener.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public interface SelectListener extends EventListener
{
    public void selected(SelectEvent event);
}
