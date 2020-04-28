/* Copyright (C) 2001, 2009 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: RecordList.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public interface RecordList extends AVList
{
    //int getPageSize();

    //int getStartIndex();

    int getRecordCount();

    Iterable<? extends Record> getRecords();
}
