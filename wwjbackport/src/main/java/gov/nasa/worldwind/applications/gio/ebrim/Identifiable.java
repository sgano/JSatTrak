/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author dcollins
 * @version $Id$
 */
public interface Identifiable
{
    int getSlotCount();

    int getIndex(Slot s);

    Slot getSlot(int index);

    void setSlot(int index, Slot s);

    void addSlot(int index, Slot s);

    void addSlot(Slot s);

    void addSlots(Collection<? extends Slot> c);

    void removeSlot(int index);

    void clearSlots();

    Iterator<Slot> getSlotIterator();

    String getId();

    void setId(String id);

    String getHome();

    void setHome(String home);
}
