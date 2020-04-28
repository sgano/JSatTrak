/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.ows.ExceptionReport;
import gov.nasa.worldwind.applications.gio.ows.ExceptionReportParser;
import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.util.Logging;

import java.io.InputStream;

/**
 * @author dcollins
 * @version $Id: SAXResponseParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class SAXResponseParser implements ResponseParser
{
    private ElementParser documentElement = null;
    private ExceptionReport exceptionReport = null;
    private boolean hasExceptions = false;

    public SAXResponseParser(ElementParser documentElement)
    {
        this.documentElement = documentElement;
    }

    public SAXResponseParser()
    {
        this(null);
    }

    public void parseResponse(InputStream is) throws Exception
    {
        if (is == null)
        {
            String message = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        javax.xml.parsers.SAXParser parser = newSAXParser();
        try
        {
            doParseResponse(parser, is);
        }
        catch (Exception e)
        {
            String message = "csw.ExceptionParsingResponse";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw e;
        }
    }

    protected void doParseResponse(javax.xml.parsers.SAXParser parser, InputStream is) throws Exception
    {
        //BufferedInputStream bi = new BufferedInputStream(is);
        Handler handler = new Handler(this);
        parser.parse(is, handler);
    }

    public ElementParser getDocumentElement()
    {
        return this.documentElement;
    }

    public void setDocumentElement(ElementParser documentElement)
    {
        this.documentElement = documentElement;
    }

    public ExceptionReport getExceptionReport()
    {
        return this.exceptionReport;
    }

    public boolean hasExceptions()
    {
        return this.hasExceptions;
    }

    public void startDocument(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        if (name == null)
        {
            String message = "nullValue.NameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = "nullValue.AttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (ExceptionReportParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            this.hasExceptions = true;
            ExceptionReportParser parser = new ExceptionReportParser(name, attributes);
            this.exceptionReport = parser;
            setDocumentElement(parser);
        }
        else
        {
            doStartDocument(name, attributes);
        }
    }

    public void endDocument(String name) throws Exception
    {
        if (name == null)
        {
            String message = "nullValue.NameIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        doEndDocument(name);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doStartDocument(String name, org.xml.sax.Attributes attributes) throws Exception
    {
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doEndDocument(String name) throws Exception
    {
    }

    protected static javax.xml.parsers.SAXParser newSAXParser() throws Exception
    {
        try
        {
            javax.xml.parsers.SAXParserFactory parserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            return parserFactory.newSAXParser();
        }
        catch (Exception e)
        {
            String message = "csw.ExceptionWhileCreatingSAXParser";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw e;
        }
    }

    protected static class Handler extends org.xml.sax.helpers.DefaultHandler
    {
        private SAXResponseParser responseParser;
        private boolean firstElement = true;

        public Handler(SAXResponseParser responseParser)
        {
            this.responseParser = responseParser;
        }

        public void startElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
            throws org.xml.sax.SAXException
        {
            if (lname == null)
            {
                String message = "nullValue.LNameIsNull";
                Logging.logger().severe(message);
                throw new org.xml.sax.SAXException(message);
            }
            if (attributes == null)
            {
                String message = "nullValue.AttributesIsNull";
                Logging.logger().severe(message);
                throw new org.xml.sax.SAXException(message);
            }

            try
            {
                if (this.firstElement)
                {
                    this.firstElement = false;
                    this.responseParser.startDocument(lname, attributes);
                }
                else if (this.responseParser.documentElement != null)
                {
                    this.responseParser.documentElement.startElement(lname, attributes);
                }
            }
            catch (Exception e)
            {
                // Exception should already be logged.
                throw new org.xml.sax.SAXException(e);
            }
        }

        public void endElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
        {
            if (lname == null)
            {
                String message = "nullValue.LNameIsNull";
                Logging.logger().severe(message);
                throw new org.xml.sax.SAXException(message);
            }

            try
            {
                if (this.responseParser.documentElement != null)
                {
                    this.responseParser.documentElement.endElement(lname);

                    if (lname.equalsIgnoreCase(this.responseParser.documentElement.getElementName()))
                    {
                        this.responseParser.documentElement = null;
                        this.responseParser.endDocument(lname);
                    }
                }
            }
            catch (Exception e)
            {
                // Exception should already be logged.
                throw new org.xml.sax.SAXException(e);
            }
        }

        public void characters(char[] data, int start, int length) throws org.xml.sax.SAXException
        {
            try
            {
                if (this.responseParser.documentElement != null)
                    this.responseParser.documentElement.characters(data, start, length);
            }
            catch (Exception e)
            {
                // Exception should already be logged.
                throw new org.xml.sax.SAXException(e);
            }
        }
    }
}
