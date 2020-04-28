/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.avlist.AVList;

import javax.swing.*;
import java.net.URI;

/**
 * @author dcollins
 * @version $Id: CSWGeodataController.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class CSWGeodataController extends AbstractGeodataController
{
    public CSWGeodataController()
    {
    }

    protected void doExecuteSearch(final AVList params) throws Exception
    {
        String service = Configuration.getStringValue(GeodataKey.CSW_SERVICE_URI);
        CSWGetRecordsRequest request = new CSWGetRecordsRequest(new URI(service));
        URI uri = request.getUri();

        if (Thread.currentThread().isInterrupted())
            return;

        String requestString = null;
        if (params != null)
            requestString = this.createRequestString(params);

        if (Thread.currentThread().isInterrupted())
            return;

        final RecordList recordList = CSWRecordList.retrieve(uri, requestString);

        if (Thread.currentThread().isInterrupted())
            return;

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                getGeodataWindow().setRecordList(recordList, params);
            }
        });
    }

    protected String createRequestString(AVList params)
    {
        CSWQueryBuilder qb = new CSWQueryBuilder(params);
        return qb.getGetRecordsString();
    }
}
