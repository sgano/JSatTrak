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
 * @version $Id: ExceptionTypeParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ExceptionTypeParser extends ElementParser implements ExceptionType
{
    private List<ExceptionText> exceptionTextList;
    private String exceptionCode;
    private String locator;
    public static final String ELEMENT_NAME = "Exception";
    private static final String EXCEPTION_CODE_ATTRIBUTE_NAME = "exceptionCode";
    private static final String LOCATOR_ATTRIBUTE_NAME = "locator";

    public ExceptionTypeParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        this.exceptionTextList = new ArrayList<ExceptionText>();

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (EXCEPTION_CODE_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.exceptionCode = attributes.getValue(i);
            else if (LOCATOR_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.locator = attributes.getValue(i);
        }
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes)
    {
        if (ExceptionTextParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ExceptionTextParser parser = new ExceptionTextParser(name, attributes);
            this.exceptionTextList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getExceptionTextCount()
    {
        return this.exceptionTextList.size();
    }

    public int getIndex(ExceptionText exceptionText)
    {
        return this.exceptionTextList.indexOf(exceptionText);
    }

    public ExceptionText getExceptionText(int index)
    {
        if (index < 0 || index >= this.exceptionTextList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.exceptionTextList.get(index);
    }

    public void setExceptionText(int index, ExceptionText exceptionText)
    {
        if (index < 0 || index >= this.exceptionTextList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionTextList.set(index, exceptionText);
    }

    public void addExceptionText(int index, ExceptionText exceptionText)
    {
        if (index < 0 || index > this.exceptionTextList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionTextList.add(index, exceptionText);
    }

    public void addExceptionText(ExceptionText exceptionText)
    {
        this.exceptionTextList.add(exceptionText);
    }

    public void addExceptionTexts(Collection<? extends ExceptionText> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionTextList.addAll(c);
    }

    public void removeExceptionText(int index)
    {
        if (index < 0 || index >= this.exceptionTextList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.exceptionTextList.remove(index);
    }

    public void clearExceptionTexts()
    {
        this.exceptionTextList.clear();
    }

    public Iterator<ExceptionText> iterator()
    {
        return this.exceptionTextList.iterator();
    }

    public String getExceptionCode()
    {
        return this.exceptionCode;
    }

    public void setExceptionCode(String exceptionCode)
    {
        this.exceptionCode = exceptionCode;
    }

    public String getLocator()
    {
        return this.locator;
    }

    public void setLocator(String locator)
    {
        this.locator = locator;
    }
}
