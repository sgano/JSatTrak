/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

/**
 * @author dcollins
 * @version $Id$
 */
public interface Slot
{
    ValueList getValueList();

    void setValueList(ValueList valueList);

    String getName();

    void setName(String name);

    String getSlotType();

    void setSlotType(String slotType);
}
