/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: GeodataController.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public interface GeodataController
{
    GeodataWindow getGeodataWindow();

    void setGeodataWindow(GeodataWindow gwd);

    void executeSearch(AVList params);

    void cancelSearch();
}
