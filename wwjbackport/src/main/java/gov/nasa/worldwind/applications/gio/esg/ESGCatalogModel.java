/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogModel;
import gov.nasa.worldwind.applications.gio.catalogui.DefaultResultList;
import gov.nasa.worldwind.applications.gio.catalogui.QueryModel;
import gov.nasa.worldwind.applications.gio.catalogui.ResultList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author dcollins
 * @version $Id: ESGCatalogModel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ESGCatalogModel implements CatalogModel, PropertyChangeListener
{
    private ESGQueryModel queryModel;
    private DefaultResultList resultModel;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public ESGCatalogModel()
    {
        this.queryModel = new ESGQueryModel();
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
