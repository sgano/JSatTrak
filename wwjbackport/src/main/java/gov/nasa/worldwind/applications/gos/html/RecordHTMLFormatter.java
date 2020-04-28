/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.html;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.util.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author dcollins
 * @version $Id: RecordHTMLFormatter.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class RecordHTMLFormatter extends HTMLFormatter
{
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

    public RecordHTMLFormatter()
    {
    }

    public void addRecordDescription(StringBuilder sb, Record record, int maxCharacters)
    {
        String s = record.getAbstract();
        if (!WWUtil.isEmpty(s))
        {
            this.addText(sb, s, maxCharacters);
        }
    }

    public void addRecordDescription(StringBuilder sb, Record record)
    {
        this.addRecordDescription(sb, record, 200);
    }

    public void addRecordIcons(StringBuilder sb, Record record)
    {
        OnlineResource r = record.getResource(GeodataKey.IMAGE);
        if (r != null)
        {
            this.addResourceImage(sb, r);
            this.addLineBreak(sb);
            this.addLineBreak(sb);
        }

        r = record.getResource(GeodataKey.SERVICE_STATUS);
        if (r != null && r.getURL() != null)
        {
            ServiceStatus serviceStatus = (ServiceStatus) WorldWind.getSessionCache().get(r.getURL().toString());
            if (serviceStatus != null)
            {
                OnlineResource sr = serviceStatus.getScoreImageResource();
                if (sr != null)
                    this.addResourceImage(sb, sr);
            }
        }
    }

    public void addRecordLinks(StringBuilder sb, Record record)
    {
        this.beginHyperlinkSeries(sb);

        OnlineResource r = record.getResource(GeodataKey.METADATA);
        if (r != null)
            this.addResourceHyperlinkInSeries(sb, r);

        r = record.getResource(GeodataKey.SERVICE_STATUS_METADATA);
        if (r != null)
            this.addResourceHyperlinkInSeries(sb, r);

        this.endHyperlinkSeries(sb);
    }

    public void addRecordModifiedDate(StringBuilder sb, Record record)
    {
        long modifiedTime = record.getModifiedTime();
        if (modifiedTime < 0)
            return;

        this.addText(sb, this.dateFormat.format(new Date(modifiedTime)), -1);
    }

    public void addRecordModifiedDate(StringBuilder sb, Record record, long currentTime)
    {
        long modifiedTime = record.getModifiedTime();
        if (modifiedTime < 0)
            return;

        if (currentTime < 0)
        {
            this.addRecordModifiedDate(sb, record);
            return;
        }

        long milliseconds = currentTime - modifiedTime;
        if (milliseconds < 0)
        {
            this.addRecordModifiedDate(sb, record);
            return;
        }

        int days = (int) Math.floor(WWMath.convertMillisToDays(milliseconds));
        int hours = (int) Math.floor(WWMath.convertMillisToHours(milliseconds));
        int minutes = (int) Math.floor(WWMath.convertMillisToMinutes(milliseconds));

        if (days > 0)
            sb.append(days).append(" days ago");
        else if (hours > 0)
            sb.append(hours).append(" hours ago");
        else if (minutes > 0)
            sb.append(minutes).append(" minutes ago");
        else if (milliseconds >= 0)
            sb.append("Less than 1 minute ago");
    }

    public void addRecordTitle(StringBuilder sb, Record record)
    {
        String s = record.getTitle();
        if (!WWUtil.isEmpty(s))
        {
            this.addResourceHyperlink(sb, record.getResource(GeodataKey.WEBSITE), s, null);
        }
    }
}
