/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.xml;

import gov.nasa.worldwind.util.Logging;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * @author Lado Garakanidze
 * @version $Id: Element.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class Element
{
    private xmlns ns;
    private String name;
    private List<Element> children;
    private Map<String, String> attributes;

    public Element(xmlns ns, String elementName)
    {
        if (ns == null)
        {
            String message = "nullValue.NamespaceIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (elementName == null)
        {
            String message = Logging.getMessage("nullValue.ElementNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.ns = ns;
        this.name = elementName;
        this.children = new ArrayList<Element>();
        this.attributes = new HashMap<String, String>();
    }

    public xmlns getNs()
    {
        return this.ns;
    }

    public String getName()
    {
        return this.name;
    }

    public int getElementCount()
    {
        return this.children.size();
    }

    public int getIndex(Element e)
    {
        if (e == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.children.indexOf(e);
    }

    public Element getElement(int index)
    {
        if (index < 0 || index >= this.children.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.children.get(index);
    }

    public void setElement(int index, Element e) throws Exception
    {
        if (index < 0 || index >= this.children.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (e == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.children.set(index, e);
    }

    public Element addElement(int index, Element e) throws Exception
    {
        if (index < 0 || index > this.children.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (e == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.children.add(index, e);
        return e;
    }

    public Element addElement(Element e) throws Exception
    {
        if (e == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.children.add(e);
        return e;
    }

    public void addElements(Collection<? extends Element> c) throws Exception
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.children.addAll(c);
    }

    public void removeElement(int index) throws Exception
    {
        if (index < 0 || index >= this.children.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.children.remove(index);
    }

    public void clearElements() throws Exception
    {
        this.children.clear();
    }

    public boolean hasContent()
    {
        return !this.children.isEmpty();
    }

    public String getAttribute(String name)
    {
        if (name == null)
        {
            String message = "nullValue.NameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.attributes.get(name);
    }

    public void setAttribute(String name, String value)
    {
        if (name == null)
        {
            String message = "nullValue.NameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.attributes.put(name, value);
    }

    public void removeAttribute(String name)
    {
        if (name == null)
        {
            String message = "nullValue.NameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.attributes.remove(name);
    }

    public void clearAttributes()
    {
        this.attributes.clear();
    }

    public String toXml()
    {
        StringWriter out = new StringWriter();
        try
        {
            write(out);
        }
        catch (IOException e)
        {
            // Exception already logged by write().
        }
        return out.toString();
    }

    protected void write(Writer out) throws IOException
    {
        if (out == null)
        {
            String message = "nullValue.WriterIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            writeBeginElement(out);
            if (hasContent())
            {
                writeElementContent(out);
                writeEndElement(out);
            }
        }
        catch (IOException e)
        {
            String message = "csw.ExceptionWhileWritingXml";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw e;
        }
    }

    protected void writeBeginElement(Writer out) throws IOException
    {
        if (out == null)
        {
            String message = "nullValue.WriterIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        out.write('<');
        out.write(getNs().getPrefix());
        out.write(':');
        out.write(getName());

        writeAttributes(out);

        if (hasContent())
            out.write(">");
        else
            out.write(" />");
    }

    protected void writeEndElement(Writer out) throws IOException
    {
        if (out == null)
        {
            String message = "nullValue.WriterIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // If there is no element content, the element is terminated in writeBeginElement().
        // Otherwise, terminate the element here.
        out.write("</");
        out.write(getNs().getPrefix());
        out.write(':');
        out.write(getName());
        out.write('>');
    }

    protected void writeAttributes(Writer out) throws IOException
    {
        if (out == null)
        {
            String message = "nullValue.WriterIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (Map.Entry<String, String> attr : this.attributes.entrySet())
        {
            String value = attr.getValue();
            out.write(' ');
            out.write(attr.getKey());
            out.write('=');
            out.write('\"');
            if (value != null)
                out.write(value);
            out.write('\"');
        }
    }

    protected void writeElementContent(Writer out) throws IOException
    {
        if (out == null)
        {
            String message = "nullValue.WriterIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (Element el : this.children)
        {
            el.write(out);
        }
    }
}
