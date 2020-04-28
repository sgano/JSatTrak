/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.util.Logging;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author dcollins
 * @version $Id: ParserUtils.java 5472 2008-06-26 20:11:53Z dcollins $
 */
public class ParserUtils
{
    public ParserUtils()
    {
    }

    public static Date parseWMSDate(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        // Web Map Service Implementation Specification, Annex D
        // ccyy-mm-ddThh:mm:ss.sssZ
        Date date = null;
        try
        {
            s = s.trim();
            if (s.length() > 0)
            {
                //DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSZ");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                date = df.parse(s);
            }
        }
        catch (ParseException e)
        {
            String message = "csw.ExceptionWhileParsingCSWDate";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return date;
    }

    public static String formatAsOGCDate(Date date)
    {
        if (date == null)
        {
            String message = Logging.getMessage("nullValue.DateIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        // Web Map Service Implementation Specification, Annex D
        DateFormat df = new SimpleDateFormat("yyyy-MM");
        return df.format(date);
    }
}
