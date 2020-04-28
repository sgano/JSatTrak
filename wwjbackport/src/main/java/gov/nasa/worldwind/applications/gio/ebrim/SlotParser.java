/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id$
 */
public class SlotParser extends ElementParser implements Slot
{
    private ValueList valueList;
    private String name;
    private String slotType;
    public static final String ELEMENT_NAME = "Slot";
    private static final String NAME_ATTRIB_NAME = "name";

    public SlotParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (NAME_ATTRIB_NAME.equalsIgnoreCase(attribName))
                this.name = attributes.getValue(i);
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes)
    {
        if (ValueListParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ValueListParser parser = new ValueListParser(name, attributes);
            this.valueList = parser;
            setCurrentElement(parser);
        }
    }

    public ValueList getValueList()
    {
        return this.valueList;
    }

    public void setValueList(ValueList valueList)
    {
        this.valueList = valueList;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSlotType()
    {
        return this.slotType;
    }

    public void setSlotType(String slotType)
    {
        this.slotType = slotType;
    }
}
