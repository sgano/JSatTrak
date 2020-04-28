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
public class ValueListParser extends ElementParser implements ValueList
{
    private List<Value> valueList;
    public static final String ELEMENT_NAME = "ValueList";

    public ValueListParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
        this.valueList = new ArrayList<Value>();
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes)
    {
        if (ValueParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ValueParser parser = new ValueParser(name, attributes);
            this.valueList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getValueCount()
    {
        return this.valueList.size();
    }

    public int getIndex(Value v)
    {
        return this.valueList.indexOf(v);
    }

    public Value getValue(int index)
    {
        if (index < 0 || index >= this.valueList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.valueList.get(index);
    }

    public void setValue(int index, Value v)
    {
        if (index < 0 || index >= this.valueList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.valueList.set(index, v);
    }

    public void addValue(int index, Value v)
    {
        if (index < 0 || index > this.valueList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.valueList.add(index, v);
    }

    public void addValue(Value v)
    {
        this.valueList.add(v);
    }

    public void addValues(Collection<? extends Value> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.valueList.addAll(c);
    }

    public void removeValue(int index)
    {
        if (index < 0 || index >= this.valueList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.valueList.remove(index);
    }

    public void clearValues()
    {
        this.valueList.clear();
    }

    public Iterator<Value> iterator()
    {
        return this.valueList.iterator();
    }
}
