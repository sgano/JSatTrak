/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.util.Logging;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * @author Lado Garakanidze
 * @version $Id: DOMResponseParser.java 9411 2009-03-16 21:03:25Z tgaskins $
 */
public class DOMResponseParser implements ResponseParser
{
    private org.w3c.dom.Document doc;

    public DOMResponseParser()
    {
    }

    public void parseResponse(InputStream is) throws Exception
    {
        if (is == null)
        {
            String message = "nullValue.InputStreamIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doc = parse(is);
    }

    public org.w3c.dom.Document getDocument()
    {
        return this.doc;
    }

    public org.w3c.dom.Node getDocumentElement()
    {
        return this.doc != null ? this.doc.getDocumentElement() : this.doc;
    }

    public String toXml()
    {
        javax.xml.transform.TransformerFactory transformerFactory =
                javax.xml.transform.TransformerFactory.newInstance();
        try
        {
            // The StringWriter will receive the document xml.
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            // Attempt to write the Document to the StringWriter.
            javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(
                    new javax.xml.transform.dom.DOMSource(this.doc),
                    new javax.xml.transform.stream.StreamResult(stringWriter));
            // If successful, return the StringWriter contents as a String.
            return stringWriter.toString();
        }
        catch (javax.xml.transform.TransformerConfigurationException e)
        {
            String message = Logging.getMessage("generic.ExceptionWritingXml");
            Logging.logger().severe(message);
            return null;
        }
        catch (javax.xml.transform.TransformerException e)
        {
            String message = Logging.getMessage("generic.ExceptionWritingXml");
            Logging.logger().severe(message);
            return null;
        }
    }

    public String toString()
    {
        return toXml();
    }

    private static org.w3c.dom.Document parse(InputStream is)
    {
        try
        {
            javax.xml.parsers.DocumentBuilderFactory docBuilderFactory =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            BufferedInputStream bi = new BufferedInputStream(is);
            return docBuilder.parse(bi);
        }
        catch (Exception e)
        {
            String message = "csw.ExceptionParsingResponse";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }
    }
}
