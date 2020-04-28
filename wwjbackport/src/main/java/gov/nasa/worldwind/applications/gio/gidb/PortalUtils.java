/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: PortalUtils.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class PortalUtils
{
    public static void makeServerParams(Server src, AVList dest)
    {
        if (src == null)
        {
            String message = "nullValue.SrcIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Text t = src.getTitle();
        if (t != null)
        {
            String s = t.getValue();
            if (s != null)
                if (dest.getValue(CatalogKey.TITLE) == null)
                    dest.setValue(CatalogKey.TITLE, s);
        }

        t = src.getURL();
        if (t != null)
        {
            String s = t.getValue();
            if (s != null)
                if (dest.getValue(CatalogKey.URI) == null)
                    dest.setValue(CatalogKey.URI, s);
        }
    }
}
