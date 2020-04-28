/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id: SearchResults.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface SearchResults extends Iterable<Object>
{
    int getRecordCount();

    int getIndex(Object o);

    Object getRecord(int index);

    void setRecord(int index, Object o);

    void addRecord(int index, Object o);

    void addRecord(Object o);

    void addRecords(Collection<?> c);

    void removeRecord(int index);

    void clearRecords();

    String getResultSetId();

    void setResultSetId(String resultSetId);

    ElementSetType getElementSet();

    void setElementSet(ElementSetType elementSet);

    String getRecordSchema();

    void setRecordSchema(String recordSchema);

    int getNumberOfRecordsMatched();

    void setNumberOfRecordsMatched(int numberOfRecordsMatched);

    int getNumberOfRecordsReturned();

    void setNumberOfRecordsReturned(int numberOfRecordsReturned);

    int getNextRecord();

    void setNextRecord(int nextRecord);

    String getExpires();

    void setExpires(String expires);
}
