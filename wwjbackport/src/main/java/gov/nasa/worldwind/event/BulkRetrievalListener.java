/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.event;

import java.util.EventListener;

/**
 * Interface for listening for bulk-download events.
 *
 * @author tag
 * @version $Id: BulkRetrievalListener.java 12882 2009-12-09 23:28:34Z tgaskins $
 */
public interface BulkRetrievalListener extends EventListener
{
    /**
     * A bulk-download event occurred, either a succes, a failure or an extended event.
     *
     * @param event the event that occurred.
     * @see gov.nasa.worldwind.retrieve.BulkRetrievable 
     */
    void eventOccurred(BulkRetrievalEvent event);
}
