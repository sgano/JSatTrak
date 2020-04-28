/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogException;
import gov.nasa.worldwind.applications.gio.csw.CSWConnection;
import gov.nasa.worldwind.applications.gio.csw.CSWConnectionException;
import gov.nasa.worldwind.applications.gio.csw.Request;
import gov.nasa.worldwind.applications.gio.csw.ResponseParser;
import gov.nasa.worldwind.applications.gio.ows.ExceptionReport;
import gov.nasa.worldwind.applications.gio.ows.ExceptionText;
import gov.nasa.worldwind.applications.gio.ows.ExceptionType;
import gov.nasa.worldwind.util.Logging;
import org.xml.sax.SAXException;

/**
 * @author dcollins
 * @version $Id: QueryUtils.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class QueryUtils
{
    public QueryUtils()
    {
    }

    public static void executeQuery(CSWConnection conn, Request request, ResponseParser response) throws Exception
    {
        if (conn == null)
        {
            String message = Logging.getMessage("nullValue.ConnectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (request == null)
        {
            String message = "nullValue.RequestIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (response == null)
        {
            String message = "nullValue.ResponseIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            conn.openConnection();
            conn.sendRequest(request, response);
        }
        finally
        {
            conn.closeConnection();
        }
    }

    public static void executeQueryLogExceptions(CSWConnection conn, Request request, ResponseParser response,
                                                 ESGResultModel resultModel)
    {
        try
        {
            executeQuery(conn, request, response);
        }
        catch (CSWConnectionException e)
        {
            String message = "Cannot send or receive service metadata.";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            if (resultModel != null)
            resultModel.addException(new CatalogException(message, null));
        }
        catch (SAXException e)
        {
            String message = "Service metadata document is malformed.";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            if (resultModel != null)
                resultModel.addException(new CatalogException(message, null));
        }
        catch (Exception e)
        {
            String message = "Error while querying service metadata.";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            if (resultModel != null)
                resultModel.addException(new CatalogException(message, null));
        }
    }

    public static void logExceptionReport(ExceptionReport exceptionReport)
    {
        if (exceptionReport == null)
        {
            String message = "nullValue.ExceptionReportIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (ExceptionType e : exceptionReport)
        {
            if (e != null)
            {
                String text = getExceptionText(e);
                if (text != null)
                    Logging.logger().severe("esg.ExceptionWhileCommunicatingwithESG: " + text);
            }
        }
    }

    public static String getExceptionText(ExceptionType e)
    {
        if (e == null)
        {
            String message = "nullValue.ExceptionIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        StringBuilder sb = null;
        for (ExceptionText text : e)
        {
            if (text != null)
            {
                sb = new StringBuilder();
                String s = text.getText();
                if (s != null)
                {
                    if (sb.length() > 0)
                        sb.append(" ");
                    sb.append(s);
                }
            }
        }
        return sb != null ? sb.toString() : null;
    }
}
