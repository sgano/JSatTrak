/* Copyright (C) 2001, 2009 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.applications.gos.event;

import gov.nasa.worldwind.avlist.*;

import java.util.EventObject;

/**
 * @author dcollins
 * @version $Id: SearchEvent.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class SearchEvent extends EventObject
{
    protected AVList params;

    public SearchEvent(Object source, AVList params)
    {
        super(source);
        this.params = params.copy();
    }

    public SearchEvent(Object source)
    {
        super(source);
        this.params = new AVListImpl();
    }

    public AVList getParams()
    {
        return params.copy();
    }
}
