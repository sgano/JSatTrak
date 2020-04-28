/* Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gos.awt.*;
import gov.nasa.worldwind.applications.gos.globe.GlobeModel;
import gov.nasa.worldwind.applications.gos.services.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/**
 * @author dcollins
 * @version $Id: GeodataRecentChangesPanel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class GeodataRecentChangesPanel extends JPanel
{
    protected enum UpdateFeed
    {
        ALL_UPDATES("All Updates"),
        LIVE_MAP_SERVER_UPDATES("Live Map Server Updates"),
        RELATED_UPDATES("Most Recent Search Updates");

        private final String text;

        UpdateFeed(String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return (this.text != null) ? this.text : super.toString();
        }
    }

    protected static class Duration
    {
        public static Duration LAST_HOUR = new Duration(1, TimeUnit.HOURS, "1 hour");
        public static Duration LAST_DAY = new Duration(24, TimeUnit.HOURS, "24 hours");
        public static Duration LAST_WEEK = new Duration(7, TimeUnit.DAYS, "7 days");

        private final long duration;
        private final String text;

        public Duration(long duration, TimeUnit timeUnit, String text)
        {
            this.duration = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
            this.text = text;
        }

        public long getDurationInMilliseconds()
        {
            return this.duration;
        }

        @Override
        public String toString()
        {
            return (this.text != null) ? this.text : super.toString();
        }
    }

    protected ScheduledExecutorService service;
    protected long period;
    protected long lastUpdateTime;
    protected AVList queryParams;
    protected JComboBox feedComboBox;
    protected JComboBox dateModifiedComboBox;
    protected RecordListPanel recordListPanel;
    protected StateCardPanel contentPanel;
    protected final Pattern durationPattern = Pattern.compile("(\\d+)(.*)");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

    protected Future taskFuture;
    protected ActionListener cancelUpdateActionListener = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            cancelUpdate();
        }
    };

    public GeodataRecentChangesPanel()
    {
        this.feedComboBox = new JComboBox(UpdateFeed.values());
        this.feedComboBox.setEditable(false);
        this.feedComboBox.setSelectedItem(UpdateFeed.LIVE_MAP_SERVER_UPDATES);

        this.dateModifiedComboBox = new JComboBox(
            new Object[] {Duration.LAST_HOUR, Duration.LAST_DAY, Duration.LAST_WEEK});
        this.dateModifiedComboBox.setEditable(true);
        this.dateModifiedComboBox.setSelectedItem(Duration.LAST_DAY);
        JComponent editor = (JComponent) this.dateModifiedComboBox.getEditor().getEditorComponent();
        editor.setInputVerifier(new InputVerifier()
        {
            public boolean verify(JComponent input)
            {
                String text = ((JTextComponent) input).getText();
                return parseDuration(text) != null;
            }
        });

        // TODO: resource panel
        this.recordListPanel = new RecordListPanel()
        {
            protected RecordPanel createRecordPanel(Record record, GlobeModel globeModel)
            {
                return new RecordPanel(record, globeModel)
                {
                    @Override
                    protected JComponent createTitleComponent()
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("<html>");
                        sb.append("<b>");
                        sb.append(makeModifiedTimeText(this.record, lastUpdateTime));
                        sb.append("</b>");
                        sb.append(" - ");
                        sb.append(this.record.getTitle());
                        sb.append("</html>");

                        return new JLabel(sb.toString());
                    }
                };
            }
        };

        this.contentPanel = new StateCardPanel();
        this.contentPanel.setComponent(GeodataKey.STATE_NORMAL, this.recordListPanel);
        this.contentPanel.setState(GeodataKey.STATE_NORMAL);

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        box.add(Box.createHorizontalGlue());
        box.add(this.feedComboBox);
        box.add(Box.createHorizontalStrut(10));
        box.add(new JLabel("in the past"));
        box.add(Box.createHorizontalStrut(10));
        box.add(this.dateModifiedComboBox);
        box.add(Box.createHorizontalStrut(20));
        box.add(new JButton(new AbstractAction("Update Now")
        {
            public void actionPerformed(ActionEvent e)
            {
                update();
            }
        }));
        box.add(Box.createHorizontalGlue());

        this.setLayout(new BorderLayout(0, 0)); // hgap, vgap
        this.add(box, BorderLayout.NORTH);
        this.add(this.contentPanel, BorderLayout.CENTER);
    }

    public long getUpdatePeriodInMilliseconds()
    {
        return this.period;
    }

    public AVList getQueryParams()
    {
        return this.queryParams;
    }

    public void setQueryParams(AVList params)
    {
        this.queryParams = (params != null) ? params.copy() : null;
    }

    public RecordList getRecordList()
    {
        return this.recordListPanel.getRecordList();
    }

    public void setRecordList(RecordList recordList, AVList params)
    {
        this.recordListPanel.setRecordList(recordList, params);
    }

    public GlobeModel getGlobeModel()
    {
        return this.recordListPanel.getGlobeModel();
    }

    public void setGlobeModel(GlobeModel globeModel)
    {
        this.recordListPanel.setGlobeModel(globeModel);
    }

    public void start(long period, TimeUnit timeUnit)
    {
        this.period = TimeUnit.MILLISECONDS.convert(period, timeUnit);

        if (this.service == null)
            this.service = Executors.newScheduledThreadPool(1);

        this.service.scheduleAtFixedRate(new UpdateTask(), 0L, this.period, TimeUnit.MILLISECONDS);
    }

    public void stop()
    {
        this.service.shutdown();
        this.service = null;
    }

    public void update()
    {
        if (this.service == null)
            this.service = Executors.newScheduledThreadPool(1);

        this.taskFuture = this.service.schedule(new UpdateTask(), 0L, TimeUnit.MILLISECONDS);
    }

    protected void cancelUpdate()
    {
        if (this.taskFuture != null && !this.taskFuture.isDone() && !this.taskFuture.isCancelled())
        {
            this.taskFuture.cancel(true);
        }
    }

    protected class UpdateTask implements Runnable
    {
        public void run()
        {
            beforeUpdate(true);
            try
            {
                doUpdate();
                afterUpdate(null);
            }
            catch (Exception e)
            {
                afterUpdate(e);
            }
        }
    }

    protected void doUpdate() throws Exception
    {
        this.lastUpdateTime = System.currentTimeMillis();
        String service = Configuration.getStringValue(GeodataKey.CSW_SERVICE_URI);
        CSWGetRecordsRequest request = new CSWGetRecordsRequest(new URI(service));
        URI uri = request.getUri();

        if (Thread.currentThread().isInterrupted())
            return;

        final AVList params = this.getRequestParams(this.lastUpdateTime);
        CSWQueryBuilder qb = new CSWQueryBuilder(params);
        String requestString = qb.getGetRecordsString();

        if (Thread.currentThread().isInterrupted())
            return;

        final RecordList records = CSWRecordList.retrieve(uri, requestString);

        if (Thread.currentThread().isInterrupted())
            return;

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                setRecordList(records, params);
            }
        });
    }

    protected void beforeUpdate(final boolean enableCancel)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    beforeUpdate(enableCancel);
                }
            });
        }
        else
        {
            this.contentPanel.setComponent(GeodataKey.STATE_WAITING, this.createWaitingComponent(enableCancel));
            this.contentPanel.setState(GeodataKey.STATE_WAITING);
        }
    }

    protected void afterUpdate(final Exception e)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    afterUpdate(e);
                }
            });
        }
        else
        {
            if (e != null)
            {
                JComponent ec = StateCardPanel.createErrorComponent(ResourceUtil.createErrorMessage(e),
                    Component.CENTER_ALIGNMENT);
                this.contentPanel.setComponent(GeodataKey.STATE_ERROR, ec);
                this.contentPanel.setState(GeodataKey.STATE_ERROR);
            }
            else
            {
                this.contentPanel.setState(GeodataKey.STATE_NORMAL);
            }

            this.taskFuture = null;
        }
    }

    protected JComponent createWaitingComponent(boolean enableCancel)
    {
        JComponent wc = StateCardPanel.createWaitingComponent("Retrieving update feed...", Component.CENTER_ALIGNMENT,
            enableCancel ? this.cancelUpdateActionListener : null);
        wc.setBorder(BorderFactory.createEmptyBorder(0, 100, 200, 100)); // top, left, bottom, right

        return wc;
    }

    protected Duration parseDuration(String text)
    {
        Matcher matcher = this.durationPattern.matcher(text);
        if (!matcher.matches() || matcher.groupCount() < 2)
            return null;

        TimeUnit timeUnit = parseTimeUnit(matcher.group(2));
        if (timeUnit == null)
            return null;

        Long duration = WWUtil.makeLong(matcher.group(1));
        if (duration == null)
            return null;

        return new Duration(duration, timeUnit, text);
    }

    protected TimeUnit parseTimeUnit(String text)
    {
        if (WWUtil.isEmpty(text))
            return null;

        String s = text.toLowerCase().trim();

        TimeUnit timeUnit = null;

        if (s.startsWith("second") || s.equals("sec") || s.equals("s"))
        {
            timeUnit = TimeUnit.SECONDS;
        }
        if (s.startsWith("minute") || s.equals("min") || s.equals("m"))
        {
            timeUnit = TimeUnit.MINUTES;
        }
        else if (s.startsWith("hour") || s.equals("hr") || s.equals("h"))
        {
            timeUnit = TimeUnit.HOURS;
        }
        else if (s.startsWith("day") || s.equals("dy") || s.equals("d"))
        {
            timeUnit = TimeUnit.DAYS;
        }

        return timeUnit;
    }

    protected AVList getRequestParams(long currentTime)
    {
        AVList params = new AVListImpl();
        params.setValue(GeodataKey.SORT_ORDER, "dateDescending");
        params.setValue(GeodataKey.RECORD_START_INDEX, 1);
        params.setValue(GeodataKey.RECORD_PAGE_SIZE, 2147483647);
        params.setValue(GeodataKey.MAX_RECORDS, 2147483647);

        if (this.feedComboBox.getSelectedItem() == UpdateFeed.LIVE_MAP_SERVER_UPDATES)
        {
            params.setValue(GeodataKey.CONTENT_TYPE_LIST, Arrays.asList("liveData"));
            params.setValue(GeodataKey.RECORD_FORMAT, "wms");
        }
        else if (this.feedComboBox.getSelectedItem() == UpdateFeed.RELATED_UPDATES)
        {
            if (this.queryParams != null)
                params.setValues(this.queryParams);
        }

        Long modifiedTime = null;

        if (this.dateModifiedComboBox.getSelectedItem() != null &&
            this.dateModifiedComboBox.getSelectedItem() instanceof Duration)
        {
            Duration duration = (Duration) this.dateModifiedComboBox.getSelectedItem();
            modifiedTime = currentTime - duration.getDurationInMilliseconds();
        }
        else if (this.dateModifiedComboBox.getSelectedItem() != null)
        {
            Duration duration = this.parseDuration(this.dateModifiedComboBox.getSelectedItem().toString());
            if (duration != null)
                modifiedTime = currentTime - duration.getDurationInMilliseconds();
        }

        if (modifiedTime != null)
        {
            params.setValue(GeodataKey.MODIFIED_TIME, modifiedTime);
        }

        return params;
    }

    protected String makeModifiedTimeText(Record record, long currentTime)
    {
        long modifiedTime = record.getModifiedTime();
        if (modifiedTime < 0)
        {
            return null;
        }

        if (currentTime < 0)
        {
            return this.dateFormat.format(new Date(modifiedTime));
        }

        long milliseconds = currentTime - modifiedTime;
        if (milliseconds < 0)
        {
            return this.dateFormat.format(new Date(modifiedTime));
        }

        int days = (int) Math.floor(WWMath.convertMillisToDays(milliseconds));
        int hours = (int) Math.floor(WWMath.convertMillisToHours(milliseconds));
        int minutes = (int) Math.floor(WWMath.convertMillisToMinutes(milliseconds));

        StringBuilder sb = new StringBuilder();

        if (days > 0)
            sb.append(days).append(" days ago");
        else if (hours > 0)
            sb.append(hours).append(" hours ago");
        else if (minutes > 0)
            sb.append(minutes).append(" minutes ago");
        else if (milliseconds >= 0)
            sb.append("Less than 1 minute ago");

        return sb.toString();
    }
}
