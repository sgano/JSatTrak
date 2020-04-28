/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author dcollins
 * @version $Id: ResultList.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface ResultList extends Iterable<ResultModel>
{
    void add(ResultModel result);

    void addAll(Collection<? extends ResultModel> c);

    void insert(int index, ResultModel result);

    void clear();

    boolean contains(ResultModel result);

    ResultModel get(int index);

    int indexOf(ResultModel result);

    boolean isEmpty();

    Iterator<ResultModel> iterator();

    boolean remove(ResultModel result);

    ResultModel remove(int index);

    ResultModel set(int index, ResultModel result);

    int size();

    void addResultListListener(ResultListListener l);

    void removeResultListListener(ResultListListener l);

    ResultListListener[] getResultListListeners();

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

    PropertyChangeListener[] getPropertyChangeListeners();
}
