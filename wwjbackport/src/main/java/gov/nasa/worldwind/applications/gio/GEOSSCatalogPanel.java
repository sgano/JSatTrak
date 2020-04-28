/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio;

import gov.nasa.worldwind.avlist.AVListImpl;

/**
 * @author dcollins
 * @version $Id: GEOSSCatalogPanel.java 6662 2008-09-16 17:50:41Z dcollins $
 */
public class GEOSSCatalogPanel extends ESGCatalogPanel
{
    private static final String GEOSS_SERVICE = "http://www.geowebportal.org/wes/serviceManagerCSW/csw";

    public GEOSSCatalogPanel()
    {
        super(GEOSS_SERVICE, new AVListImpl());
    }
}