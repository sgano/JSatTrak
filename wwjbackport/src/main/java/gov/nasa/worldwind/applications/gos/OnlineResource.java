/* Copyright (C) 2001, 2009 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.avlist.*;

import java.net.URL;

/**
 * @author dcollins
 * @version $Id: OnlineResource.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class OnlineResource extends AVListImpl
{
    public OnlineResource(String name, String displayName, URL url)
    {
        this.setValue(AVKey.NAME, name);
        this.setValue(AVKey.DISPLAY_NAME, displayName);
        this.setValue(AVKey.URL, url);
    }

    public String getName()
    {
        return (String) this.getValue(AVKey.NAME);
    }

    public String getDisplayName()
    {
        return (String) this.getValue(AVKey.DISPLAY_NAME);
    }

    public URL getURL()
    {
        return (URL) this.getValue(AVKey.URL);
    }
}

