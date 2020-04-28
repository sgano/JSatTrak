/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id$
 */
public interface ValueList extends Iterable<Value>
{
    int getValueCount();

    int getIndex(Value v);

    Value getValue(int index);

    void setValue(int index, Value v);

    void addValue(int index, Value v);

    void addValue(Value v);

    void addValues(Collection<? extends Value> c);

    void removeValue(int index);

    void clearValues();
}
