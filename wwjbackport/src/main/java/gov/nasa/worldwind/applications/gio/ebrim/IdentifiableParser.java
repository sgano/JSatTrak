/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id$
 */
public class IdentifiableParser extends ElementParser implements Identifiable
{
    private List<Slot> slotList;
    private String id;
    private String home;
    public static final String ELEMENT_NAME = "Identifiable";
    private static final String ID_ATTRIBUTE_NAME = "id";
    private static final String HOME_ATTRIBUTE_NAME = "home";

    public IdentifiableParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.slotList = new ArrayList<Slot>();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (ID_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.id = attributes.getValue(i);
            else if (HOME_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.home = attributes.getValue(i);
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        if (SlotParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            SlotParser parser = new SlotParser(name, attributes);
            this.slotList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getSlotCount()
    {
        return this.slotList.size();
    }

    public int getIndex(Slot s)
    {
        return this.slotList.indexOf(s);
    }

    public Slot getSlot(int index)
    {
        if (index < 0 || index >= this.slotList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.slotList.get(index);
    }

    public void setSlot(int index, Slot s)
    {
        if (index < 0 || index >= this.slotList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.slotList.set(index, s);
    }

    public void addSlot(int index, Slot s)
    {
        if (index < 0 || index > this.slotList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.slotList.add(index, s);
    }

    public void addSlot(Slot s)
    {
        this.slotList.add(s);
    }

    public void addSlots(Collection<? extends Slot> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.slotList.addAll(c);
    }

    public void removeSlot(int index)
    {
        if (index < 0 || index >= this.slotList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.slotList.remove(index);
    }

    public void clearSlots()
    {
        this.slotList.clear();
    }

    public Iterator<Slot> getSlotIterator()
    {
        return this.slotList.iterator();
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getHome()
    {
        return this.home;
    }

    public void setHome(String home)
    {
        this.home = home;
    }
}
