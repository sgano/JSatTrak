/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.event;

import java.util.EventListener;

/**
 * Listener for events that occur on a {@link gov.nasa.worldwind.util.tree.Tree}.
 *
 * @author pabercrombie
 * @version $Id: TreeListener.java 13985 2010-10-17 21:32:03Z pabercrombie $
 * @see gov.nasa.worldwind.util.tree.Tree
 */
public interface TreeListener extends EventListener
{
    /**
     * Invoked when an event occurs.
     *
     * @param event Event that occurred.
     */
    void onTreeEvent(TreeEvent event);
}
