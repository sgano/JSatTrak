/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ows;

import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: ExceptionReportParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ExceptionReportParser extends ElementParser implements ExceptionReport
{
    private List<ExceptionType> exceptionList;
    private String version;
    private String lang;
    public static final String ELEMENT_NAME = "ExceptionReport";
    private static final String VERSION_ATTRIBUTE_NAME = "version";
    private static final String LANG_ATTRIBUTE_NAME = "lang";

    public ExceptionReportParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        this.exceptionList = new ArrayList<ExceptionType>();

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (VERSION_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.version = attributes.getValue(i);
            else if (LANG_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.lang = attributes.getValue(i);
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes)
    {
        if (ExceptionTypeParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ExceptionTypeParser parser = new ExceptionTypeParser(name, attributes);
            this.exceptionList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getExceptionCount()
    {
        return this.exceptionList.size();
    }

    public int getIndex(ExceptionType e)
    {
        return this.exceptionList.indexOf(e);
    }

    public ExceptionType getException(int index)
    {
        if (index < 0 || index >= this.exceptionList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.exceptionList.get(index);
    }

    public void setException(int index, ExceptionType e)
    {
        if (index < 0 || index >= this.exceptionList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionList.set(index, e);
    }

    public void addException(int index, ExceptionType e)
    {
        if (index < 0 || index > this.exceptionList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionList.add(index, e);
    }

    public void addException(ExceptionType e)
    {
        this.exceptionList.add(e);
    }

    public void addExceptions(Collection<? extends ExceptionType> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionList.addAll(c);
    }

    public void removeException(int index)
    {
        if (index < 0 || index >= this.exceptionList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionList.remove(index);
    }

    public void clearExceptions()
    {
        this.exceptionList.clear();
    }

    public Iterator<ExceptionType> iterator()
    {
        return this.exceptionList.iterator();
    }

    public String getVersion()
    {
        return this.version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getLang()
    {
        return this.lang;
    }

    public void setLang(String lang)
    {
        this.lang = lang;
    }
}
