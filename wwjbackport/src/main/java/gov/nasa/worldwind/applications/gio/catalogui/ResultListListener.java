/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import java.util.EventListener;

/**
 * @author dcollins
 * @version $Id: ResultListListener.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface ResultListListener extends EventListener
{
    public void listChanged(ResultListEvent e);
}
