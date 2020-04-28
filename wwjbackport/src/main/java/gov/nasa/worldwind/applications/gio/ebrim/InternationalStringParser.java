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
public class InternationalStringParser extends ElementParser implements InternationalString
{
    private List<LocalizedString> localizedStringList;
    public static final String ELEMENT_NAME = "InternationalString";

    public InternationalStringParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
        this.localizedStringList = new ArrayList<LocalizedString>();
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        if (LocalizedStringParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            LocalizedStringParser parser = new LocalizedStringParser(name, attributes);
            this.localizedStringList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getLocalizedStringCount()
    {
        return this.localizedStringList.size();
    }

    public int getIndex(LocalizedString ls)
    {
        return this.localizedStringList.indexOf(ls);
    }

    public LocalizedString getLocalizedString(int index)
    {
        if (index < 0 || index >= this.localizedStringList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.localizedStringList.get(index);
    }

    public void setLocalizedString(int index, LocalizedString ls)
    {
        if (index < 0 || index >= this.localizedStringList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.localizedStringList.set(index, ls);
    }

    public void addLocalizedString(int index, LocalizedString ls)
    {
        if (index < 0 || index > this.localizedStringList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.localizedStringList.add(index, ls);
    }

    public void addLocalizedString(LocalizedString ls)
    {
        this.localizedStringList.add(ls);
    }

    public void addLocalizedStrings(Collection<? extends LocalizedString> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.localizedStringList.addAll(c);
    }

    public void removeLocalizedString(int index)
    {
        if (index < 0 || index >= this.localizedStringList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.localizedStringList.remove(index);
    }

    public void clearLocalizedStrings()
    {
        this.localizedStringList.clear();
    }

    public Iterator<LocalizedString> iterator()
    {
        return this.localizedStringList.iterator();
    }
}
