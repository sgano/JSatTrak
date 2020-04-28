/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVList;

/**
 * An interface provided by the major World Wind components to provide attribute-value list management and
 * property change management. Classifies implementors as property-change listeners, allowing them to receive
 * property-change events.
 *
 * @author Tom Gaskins
 * @version $Id: WWObject.java 2422 2007-07-25 23:07:49Z tgaskins $
 */
public interface WWObject extends AVList, java.beans.PropertyChangeListener
{
}
