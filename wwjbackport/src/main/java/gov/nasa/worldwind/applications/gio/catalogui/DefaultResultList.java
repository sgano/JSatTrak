/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.util.Logging;

import javax.swing.event.EventListenerList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author dcollins
 * @version $Id: DefaultResultList.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class DefaultResultList implements ResultList
{
    private final List<ResultModel> listImpl = new CopyOnWriteArrayList<ResultModel>();
    private EventListenerList listenerList = new EventListenerList();
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private final PropertyChangeListener propertyEvents = new PropertyChangeListener()
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt != null)
            {
                ResultModel result = null;
                if (evt.getSource() != null && evt.getSource() instanceof ResultModel)
                    result = (ResultModel) evt.getSource();
                else if (evt.getNewValue() != null && evt.getNewValue() instanceof ResultModel)
                    result = (ResultModel) evt.getNewValue();

                int type = ResultListEvent.UPDATE;
                if (evt.getPropertyName() != null && !evt.getPropertyName().equalsIgnoreCase(CatalogKey.RESULT_MODEL))
                    type = ResultListEvent.PASSIVE_UPDATE;

                if (result != null)
                {
                    int index = indexOf(result);
                    if (index != -1)
                        fireResultUpdated(index, evt, type);
                }
            }
        }
    };

    public DefaultResultList()
    {
    }

    public void add(ResultModel result)
    {
        if (result == null)
        {
            String message = "nullValue.ResultIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.listImpl.add(result);
        result.addPropertyChangeListener(this.propertyEvents);
        int index = size() - 1;
        fireResultsInserted(index, index);
    }

    public void addAll(Collection<? extends ResultModel> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int oldSize = this.listImpl.size();
        this.listImpl.addAll(c);
        int newSize = this.listImpl.size();

        for (ResultModel result : c)
            if (result != null)
                result.addPropertyChangeListener(this.propertyEvents);

        if (oldSize < newSize)
            fireResultsInserted(oldSize, newSize - 1);
    }

    public void insert(int index, ResultModel result)
    {
        if (index < 0 || index > this.listImpl.size())
        {
            String message = "nullValue.IndexOutOfRange " + index;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (result == null)
        {
            String message = "nullValue.ResultIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        this.listImpl.add(index, result);
        result.addPropertyChangeListener(this.propertyEvents);
        fireResultsInserted(index, index);
    }

    public void clear()
    {
        for (ResultModel element : this.listImpl)
            if (element != null)
                element.removePropertyChangeListener(this.propertyEvents);
        this.listImpl.clear();
        fireResultsDeleted(-1, -1);
    }

    public boolean contains(ResultModel result)
    {
        if (result == null)
        {
            String message = "nullValue.ResultIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        return this.listImpl.contains(result);
    }

    public ResultModel get(int index)
    {
        if (index < 0 || index >= this.listImpl.size())
        {
            String message = "nullValue.IndexOutOfRange " + index;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        return this.listImpl.get(index);
    }

    public int indexOf(ResultModel result)
    {
        if (result == null)
        {
            String message = "nullValue.ResultIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        return this.listImpl.indexOf(result);
    }

    public boolean isEmpty()
    {
        return this.listImpl.isEmpty();
    }

    public Iterator<ResultModel> iterator()
    {
        return this.listImpl.iterator();
    }

    public boolean remove(ResultModel result)
    {
        if (result == null)
        {
            String message = "nullValue.ResultIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean ok = this.listImpl.remove(result);
        if (ok)
        {
            result.removePropertyChangeListener(this.propertyEvents);
            int index = this.indexOf(result);
            fireResultsDeleted(index, index);
        }
        return ok;
    }

    public ResultModel remove(int index)
    {
        if (index < 0 || index >= this.listImpl.size())
        {
            String message = "nullValue.IndexOutOfRange " + index;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ResultModel element = this.listImpl.remove(index);
        if (element != null)
        {
            element.removePropertyChangeListener(this.propertyEvents);
            fireResultsDeleted(index, index);
        }
        return element;
    }

    public ResultModel set(int index, ResultModel result)
    {
        if (index < 0 || index >= this.listImpl.size())
        {
            String message = "nullValue.IndexOutOfRange " + index;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (result == null)
        {
            String message = "nullValue.ResultIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        result.addPropertyChangeListener(this.propertyEvents);

        ResultModel oldElement = this.listImpl.set(index, result);
        if (oldElement != null)
            oldElement.removePropertyChangeListener(this.propertyEvents);

        fireResultsUpdated(index, index, ResultListEvent.UPDATE);
        return oldElement;
    }

    public int size()
    {
        return this.listImpl.size();
    }

    public void addResultListListener(ResultListListener l)
    {
        this.listenerList.add(ResultListListener.class, l);
    }

    public void removeResultListListener(ResultListListener l)
    {
        this.listenerList.remove(ResultListListener.class, l);
    }

    public ResultListListener[] getResultListListeners()
    {
        return this.listenerList.getListeners(ResultListListener.class);
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

    protected void fireResultsInserted(int startIndex, int endIndex)
    {
        ResultListEvent e = new ResultListEvent(this, startIndex, endIndex, ResultListEvent.ADD);
        fireModelEvent(e);
        firePropertyChange();
    }

    protected void fireResultsDeleted(int startIndex, int endIndex)
    {
        ResultListEvent e = new ResultListEvent(this, startIndex, endIndex, ResultListEvent.REMOVE);
        fireModelEvent(e);
        firePropertyChange();
    }

    protected void fireResultsUpdated(int startIndex, int endIndex, int type)
    {
        ResultListEvent e = new ResultListEvent(this, startIndex, endIndex, type);
        fireModelEvent(e);
        firePropertyChange();
    }

    protected void fireResultUpdated(int index, PropertyChangeEvent evt, int type)
    {
        ResultListEvent me = new ResultListEvent(this, index, index, type);
        fireModelEvent(me);
        firePropertyChange(evt);
    }

    protected void fireModelEvent(ResultListEvent e)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == ResultListListener.class)
            {
                // Lazily create the event:
                if (e == null)
                    e = new ResultListEvent(this);
                ((ResultListListener) listeners[i + 1]).listChanged(e);
            }
        }
    }

    public void firePropertyChange(PropertyChangeEvent evt)
    {
        this.changeSupport.firePropertyChange(evt);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        this.changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange()
    {
        this.changeSupport.firePropertyChange(CatalogKey.RESULT_MODEL, null, this);
    }
}
