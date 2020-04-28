/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.applications.gos.globe.GlobeModel;
import gov.nasa.worldwind.applications.gos.html.HTMLFormatter;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.util.WWUtil;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author dcollins
 * @version $Id: RecordListPanel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class RecordListPanel extends JPanel
{
    protected RecordList recordList;
    protected GlobeModel globeModel;
    // Swing components.
    protected JPanel contentPanel;
    protected JScrollPane scrollPane;
    protected ArrayList<RecordPanel> recordPanelList = new ArrayList<RecordPanel>();
    protected JTextPane infoPane;

    public RecordListPanel()
    {
        this.contentPanel = new JPanel();
        this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.Y_AXIS));
        this.contentPanel.setBackground(Color.WHITE);
        this.contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right

        this.infoPane = new JTextPane();
        this.infoPane.setEditable(false);
        this.infoPane.setOpaque(false);
        this.infoPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right
        ((DefaultCaret) this.infoPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        JPanel dummyPanel = new JPanel(new BorderLayout(0, 0));
        dummyPanel.setBackground(Color.WHITE);
        dummyPanel.add(this.contentPanel, BorderLayout.NORTH);

        this.scrollPane = new JScrollPane(dummyPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollPane.setAutoscrolls(false);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.setBackground(Color.WHITE);
        this.setLayout(new BorderLayout(0, 0)); // hgap, vgap
        this.add(this.infoPane, BorderLayout.NORTH);
        this.add(this.scrollPane, BorderLayout.CENTER);
    }

    public RecordList getRecordList()
    {
        return this.recordList;
    }

    public void setRecordList(RecordList recordList, AVList params)
    {
        this.recordList = recordList;
        this.updateInfoPanel(params);
        this.updateRecordPanels();
    }

    public GlobeModel getGlobeModel()
    {
        return this.globeModel;
    }

    public void setGlobeModel(GlobeModel globeModel)
    {
        this.globeModel = globeModel;
    }

    protected void updateInfoPanel(AVList params)
    {
        StringBuilder sb = new StringBuilder();
        HTMLFormatter formatter = new HTMLFormatter();
        formatter.beginHTMLBody(sb);
        sb.append("<table><tr><td align=\"right\">");
        this.createInfoPanelText(sb, this.recordList, params);
        sb.append("</td></tr></table>");
        formatter.endHTMLBody(sb);

        this.infoPane.setContentType("text/html");
        this.infoPane.setText(sb.toString());
    }

    protected void updateRecordPanels()
    {
        this.contentPanel.removeAll();
        this.recordPanelList.clear();

        if (this.recordList == null)
            return;

        Iterable<? extends Record> records = this.recordList.getRecords();
        if (records == null)
            return;

        for (Record r : records)
        {
            if (r == null)
                continue;

            RecordPanel rp = this.createRecordPanel(r, this.getGlobeModel());
            rp.setAlignmentX(Component.LEFT_ALIGNMENT);
            rp.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0)); // top, left, bottom, right
            this.contentPanel.add(rp);
            this.contentPanel.add(Box.createVerticalStrut(10));
            this.recordPanelList.add(rp);
        }
    }

    protected void createInfoPanelText(StringBuilder sb, RecordList recordList, AVList searchParams)
    {
        if (recordList == null || searchParams == null)
        {
            sb.append(" ");
            return;
        }

        int count = recordList.getRecordCount();
        int startIndex = AVListImpl.getIntegerValue(searchParams, GeodataKey.RECORD_START_INDEX) + 1;
        int pageSize = AVListImpl.getIntegerValue(searchParams, GeodataKey.RECORD_PAGE_SIZE);
        int endIndex = startIndex + pageSize - 1;
        if (endIndex > count)
            endIndex = count;
        Integer max = Configuration.getIntegerValue(GeodataKey.MAX_RECORDS);

        Iterable<? extends Record> records = recordList.getRecords();
        if (records == null)
            sb.append("No Results");

        if (records != null && count <= pageSize)
            sb.append("<b>").append(count).append("</b>").append(" results");

        if (records != null && count > pageSize && count <= max)
            sb.append("Results ").append("<b>").append(startIndex).append(" - ").append(endIndex).append("</b>")
                .append(" of ").append("<b>").append(count).append("</b>");

        if (records != null && count > pageSize && count > max)
            sb.append("Results ").append("<b>").append(startIndex).append(" - ").append(endIndex).append("</b>")
                .append(" of about ").append("<b>").append(max).append("</b>");

        if (searchParams != null)
        {
            String s = searchParams.getStringValue(GeodataKey.SEARCH_TEXT);
            if (!WWUtil.isEmpty(s))
                sb.append(" for ").append("<b>").append(s).append("</b>");
        }

        sb.append(".");
    }

    protected RecordPanel createRecordPanel(Record record, GlobeModel globeModel)
    {
        return new RecordPanel(record, this.getGlobeModel());
    }
}
