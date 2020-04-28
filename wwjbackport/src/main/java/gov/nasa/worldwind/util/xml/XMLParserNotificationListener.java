/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.xml;

/**
 * The interface that receives {@link gov.nasa.worldwind.util.xml.XMLEventParserContext} notifications.
 *
 * @author tag
 * @version $Id: XMLParserNotificationListener.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public interface XMLParserNotificationListener
{
    /**
     * Receives notification events from the parser context.
     *
     * @param notification the notification object containing the notificaton type and data.
     */
    public void notify(XMLParserNotification notification);
}
