/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;

/**
 * @author dcollins
 * @version $Id: CSWRecordList.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class CSWRecordList extends AVListImpl implements RecordList
{
    protected Document doc;
    protected XPath xpath;
    protected Iterable<CSWRecord> recordIterable;

    protected CSWRecordList(Document doc, XPath xpath, Iterable<CSWRecord> recordIterable)
    {
        this.doc = doc;
        this.xpath = xpath;
        this.recordIterable = recordIterable;
    }

    public static RecordList retrieve(URI uri, String postString) throws Exception
    {
        // TODO: draw defaults form configuration
        return retrieve(uri, postString, 20000, 20000);
    }

    public static RecordList retrieve(URI uri, String postString, Integer connectTimeout, Integer readTimeout)
        throws Exception
    {
        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        InputStream is = null;
        try
        {
            URLRetriever retriever = new HTTPPostRetriever(uri.toURL(), postString, null);

            if (connectTimeout != null)
                retriever.setConnectTimeout(connectTimeout);

            if (readTimeout != null)
                retriever.setReadTimeout(readTimeout);

            retriever.call();

            if (retriever.getState().equals(URLRetriever.RETRIEVER_STATE_INTERRUPTED))
                return null;

            if (!retriever.getState().equals(URLRetriever.RETRIEVER_STATE_SUCCESSFUL))
            {
                String message = Logging.getMessage("generic.RetrievalFailed", uri.toString());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            if (retriever.getBuffer() == null || retriever.getBuffer().limit() == 0)
            {
                String message = Logging.getMessage("generic.RetrievalReturnedNoContent", uri.toString());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            is = WWIO.getInputStreamFromByteBuffer(retriever.getBuffer());

            if (Thread.currentThread().isInterrupted())
                return null;

            return CSWRecordList.parse(WWXML.openDocumentStream(is, true));
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri.toString());
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
        finally
        {
            WWIO.closeStream(is, uri.toString());
        }
    }

    public static RecordList parse(Document doc)
    {
        XPath xpath = WWXML.makeXPath();
        xpath.setNamespaceContext(new CSWNamespaceContext());

        Iterable<CSWRecord> records = parseRecords(doc.getDocumentElement(), xpath);

        return new CSWRecordList(doc, xpath, records);
    }

    //public int getPageSize()
    //{
    //    Integer i = WWXML.getInteger(this.doc.getDocumentElement(), "/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsReturned", this.xpath);
    //    return (i != null) ? i : 0;
    //}

    //public int getStartIndex()
    //{
    //    Integer a = WWXML.getInteger(this.doc.getDocumentElement(), "/csw:GetRecordsResponse/csw:SearchResults/@nextRecord", this.xpath);
    //    Integer b = WWXML.getInteger(this.doc.getDocumentElement(), "/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsReturned", this.xpath);
    //    if (a == null || b == null)
    //        return 0;
    //
    //    return a - b - 1;
    //}

    public int getRecordCount()
    {
        Integer i = WWXML.getInteger(this.doc.getDocumentElement(),
            "/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched", this.xpath);
        return (i != null) ? i : 0;
    }

    public Iterable<? extends Record> getRecords()
    {
        return this.recordIterable;
    }

    protected static Iterable<CSWRecord> parseRecords(Element domElement, XPath xpath)
    {
        Element[] els = WWXML.getElements(domElement, "/csw:GetRecordsResponse/csw:SearchResults/csw:Record", xpath);
        if (els == null || els.length == 0)
            return null;

        if (Thread.currentThread().isInterrupted())
            return null;

        ArrayList<CSWRecord> recordList = new ArrayList<CSWRecord>();

        for (Element el : els)
        {
            if (el == null)
                continue;

            recordList.add(new CSWRecord(el, xpath));

            if (Thread.currentThread().isInterrupted())
                return null;
        }

        return (recordList.size() > 0) ? recordList : null;
    }
}
