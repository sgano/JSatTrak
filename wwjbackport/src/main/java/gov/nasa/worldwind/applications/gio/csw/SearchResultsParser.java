/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: SearchResultsParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class SearchResultsParser extends ElementParser implements SearchResults
{
    private List<Object> recordList;
    private String resultSetId;
    private ElementSetType elementSet;
    private String recordSchema;
    private int numberOfRecordsMatched;
    private int numberOfRecordsReturned;
    private int nextRecord;
    private String expires;
    public static final String ELEMENT_NAME = "SearchResults";
    private static final String RESULT_SET_ID_ATTRIBUTE_NAME = "resultSetId";
    private static final String ELEMENT_SET_ATTRIBUTE_NAME = "elementSet";
    private static final String RECORD_SCHEMA_ATTRIBUTE_NAME = "recordSchema";
    private static final String NUMBER_OF_RECORDS_MATCHED_ATTRIBUTE_NAME = "numberOfRecordsMatched";
    private static final String NUMBER_OF_RECORDS_RETURNED_ATTRIBUTE_NAME = "numberOfRecordsReturned";
    private static final String NEXT_RECORD_ATTRIBUTE_NAME = "nextRecord";
    private static final String EXPIRES_ATTRIBUTE_NAME = "expires";

    public SearchResultsParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.recordList = new ArrayList<Object>();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attribName = attributes.getLocalName(i);
            if (RESULT_SET_ID_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.resultSetId = attributes.getValue(i);
            else if (ELEMENT_SET_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.elementSet = parseElementSetType(attributes.getValue(i));
            else if (RECORD_SCHEMA_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.recordSchema = attributes.getValue(i);
            else if (NUMBER_OF_RECORDS_MATCHED_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.numberOfRecordsMatched = parseInt(attributes.getValue(i));
            else if (NUMBER_OF_RECORDS_RETURNED_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.numberOfRecordsReturned = parseInt(attributes.getValue(i));
            else if (NEXT_RECORD_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.nextRecord = parseInt(attributes.getValue(i));
            else if (EXPIRES_ATTRIBUTE_NAME.equalsIgnoreCase(attribName))
                this.expires = attributes.getValue(i);
        }
    }

    protected static ElementSetType parseElementSetType(String s)
    {
        ElementSetType type = null;
        if (ElementSetType.BRIEF.getType().equalsIgnoreCase(s))
            type = ElementSetType.BRIEF;
        else if (ElementSetType.FULL.getType().equalsIgnoreCase(s))
            type = ElementSetType.FULL;
        else if (ElementSetType.SUMMARY.getType().equalsIgnoreCase(s))
            type = ElementSetType.SUMMARY;
        return type;
    }

    protected static int parseInt(String s)
    {
        int i = -1;
        try
        {
            if (s != null)
                i = Integer.parseInt(s);
        }
        catch (Exception e)
        {
            String message = "csw.ErrorParsingValue " + s;
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return i;
    }

    public int getRecordCount()
    {
        return this.recordList.size();
    }

    public int getIndex(Object o)
    {
        return this.recordList.indexOf(o);
    }

    public Object getRecord(int index)
    {
        if (index < 0 || index >= this.recordList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.recordList.get(index);
    }

    public void setRecord(int index, Object o)
    {
        if (index < 0 || index >= this.recordList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.recordList.set(index, o);
    }

    public void addRecord(int index, Object o)
    {
        if (index < 0 || index > this.recordList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.recordList.add(index, o);
    }

    public void addRecord(Object o)
    {
        this.recordList.add(o);
    }

    public void addRecords(Collection<?> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.recordList.addAll(c);
    }

    public void removeRecord(int index)
    {
        if (index < 0 || index >= this.recordList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.recordList.remove(index);
    }

    public void clearRecords()
    {
        this.recordList.clear();
    }

    public Iterator<Object> iterator()
    {
        return this.recordList.iterator();
    }

    public String getResultSetId()
    {
        return this.resultSetId;
    }

    public void setResultSetId(String resultSetId)
    {
        this.resultSetId = resultSetId;
    }

    public ElementSetType getElementSet()
    {
        return this.elementSet;
    }

    public void setElementSet(ElementSetType elementSet)
    {
        this.elementSet = elementSet;
    }

    public String getRecordSchema()
    {
        return this.recordSchema;
    }

    public void setRecordSchema(String recordSchema)
    {
        this.recordSchema = recordSchema;
    }

    public int getNumberOfRecordsMatched()
    {
        return this.numberOfRecordsMatched;
    }

    public void setNumberOfRecordsMatched(int numberOfRecordsMatched)
    {
        this.numberOfRecordsMatched = numberOfRecordsMatched;
    }

    public int getNumberOfRecordsReturned()
    {
        return this.numberOfRecordsReturned;
    }

    public void setNumberOfRecordsReturned(int numberOfRecordsReturned)
    {
        this.numberOfRecordsReturned = numberOfRecordsReturned;
    }

    public int getNextRecord()
    {
        return this.nextRecord;
    }

    public void setNextRecord(int nextRecord)
    {
        this.nextRecord = nextRecord;
    }

    public String getExpires()
    {
        return this.expires;
    }

    public void setExpires(String expires)
    {
        this.expires = expires;
    }
}
