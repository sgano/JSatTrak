/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.catalogui.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author dcollins
 * @version $Id: GIDBCatalogModel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GIDBCatalogModel implements CatalogModel, PropertyChangeListener
{
    private DefaultQueryModel queryModel;
    private DefaultResultList resultModel;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public GIDBCatalogModel()
    {
        this.queryModel = new DefaultQueryModel();
        this.resultModel = new DefaultResultList();
        this.queryModel.addPropertyChangeListener(this);
        this.resultModel.addPropertyChangeListener(this);
    }

    public QueryModel getQueryModel()
    {
        return this.queryModel;
    }

    public ResultList getResultModel()
    {
        return this.resultModel;
    }
    
    public void propertyChange(PropertyChangeEvent event)
    {
        firePropertyChange(event);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.changeSupport.removePropertyChangeListener(listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners()
    {
        return this.changeSupport.getPropertyChangeListeners();
    }

    protected void firePropertyChange(PropertyChangeEvent event)
    {
        this.changeSupport.firePropertyChange(event);
    }
}
