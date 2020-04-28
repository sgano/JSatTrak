/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import java.beans.PropertyChangeListener;

/**
 * @author dcollins
 * @version $Id: CatalogModel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface CatalogModel
{
    QueryModel getQueryModel();

    ResultList getResultModel();

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

    PropertyChangeListener[] getPropertyChangeListeners();
}
