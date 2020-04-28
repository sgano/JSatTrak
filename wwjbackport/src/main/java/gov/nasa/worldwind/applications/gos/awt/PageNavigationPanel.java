/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.avlist.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author dcollins
 * @version $Id: PageNavigationPanel.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class PageNavigationPanel extends ActionPanel
{
    //protected RecordList recordList;
    protected int recordStartIndex;

    public PageNavigationPanel()
    {
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createEtchedBorder());
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // top, left, bottom, right)
    }

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        AWTUtil.setTreeEnabled(this, enabled);
    }

    public void getParams(AVList outParams)
    {
        outParams.setValue(GeodataKey.RECORD_START_INDEX, this.recordStartIndex);
    }

    public void setRecordList(RecordList recordList, AVList searchParams)
    {
        //this.recordList = recordList;
        this.recordStartIndex = (searchParams != null) ?
            AVListImpl.getIntegerValue(searchParams, GeodataKey.RECORD_START_INDEX, 0) : 0;
        this.onRecordListChanged(recordList, searchParams);
    }

    protected void onRecordListChanged(RecordList recordList, AVList searchParams)
    {
        this.removeAll();

        if (recordList == null || searchParams == null)
        {
            this.validate();
            return;
        }

        PageButtonActionListener listener = new PageButtonActionListener()
        {
            public void pageButtonActionPerformed(ActionEvent event, int pageIndex)
            {
                recordStartIndex = pageIndex;
                fireActionPerformed(event);
            }
        };

        PageButtonBuilder pb = new PageButtonBuilder(recordList, searchParams, listener);
        if (pb.getLastPage() < 2)
            return;

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));        
        this.add(Box.createHorizontalGlue());
        pb.addPrevious(this);
        this.add(Box.createHorizontalStrut(20));

        if (pb.getLastPage() <= 7)
        {
            for (int i = 1; i <= pb.getLastPage(); i++)
            {
                pb.addPage(this, i);
            }
        }
        else if (pb.getPage() < 5)
        {
            for (int i = 1; i <= 5; i++)
            {
                pb.addPage(this, i);
            }
            pb.addText(this, "...");
            pb.addPage(this, pb.getLastPage());
        }
        else if (pb.getPage() > (pb.getLastPage() - 4))
        {
            pb.addPage(this, 1);
            pb.addText(this, "...");
            for (int i = pb.getLastPage() - 4; i <= pb.getLastPage(); i++)
            {
                pb.addPage(this, i);
            }
        }
        else
        {
            pb.addPage(this, 1);
            pb.addText(this, "...");
            for (int i = pb.getPage() - 2; i <= pb.getPage() + 2; i++)
            {
                pb.addPage(this, i);
            }
            pb.addText(this, "...");
            pb.addPage(this, pb.getLastPage());
        }

        this.add(Box.createHorizontalStrut(20));
        pb.addNext(this);
        this.add(Box.createHorizontalGlue());
        
        this.validate();
    }

    protected interface PageButtonActionListener
    {
        void pageButtonActionPerformed(ActionEvent event, int pageIndex);
    }

    protected static class PageButton extends JButton
    {
        protected boolean allowEnabled = true;

        public PageButton(String text, final int pageIndex, final PageButtonActionListener listener)
        {
            super(text);
            this.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    listener.pageButtonActionPerformed(event, pageIndex);
                }
            });
        }

        public boolean isAllowEnabled()
        {
            return this.allowEnabled;
        }

        public void setAllowEnabled(boolean allowEnabled)
        {
            this.allowEnabled = allowEnabled;
            this.setEnabled(this.allowEnabled);
        }

        public void setEnabled(boolean enabled)
        {
            super.setEnabled(enabled && this.allowEnabled);
        }
    }

    protected static class PageButtonBuilder
    {
        private final int pageSize;
        private final int curPage;
        private final int numPages;
        private final PageButtonActionListener actionListener;
        private int pagesAdded = 0;

        public PageButtonBuilder(RecordList recordList, AVList searchParams, PageButtonActionListener actionListener)
        {
            Integer startIndex = AVListImpl.getIntegerValue(searchParams, GeodataKey.RECORD_START_INDEX);
            Integer recordsPerPage =  AVListImpl.getIntegerValue(searchParams, GeodataKey.RECORD_PAGE_SIZE);
            Integer maxRecords = Configuration.getIntegerValue(GeodataKey.MAX_RECORDS);
            Integer numRecords = recordList.getRecordCount();
            if (numRecords > maxRecords)
                numRecords = maxRecords;

            this.pageSize = recordsPerPage;
            this.curPage = (int) Math.floor(startIndex / (double) this.pageSize) + 1;
            this.numPages = (int) Math.ceil(numRecords / (double) this.pageSize);
            this.actionListener = actionListener;
        }

        public final int getPage()
        {
            return this.curPage;
        }

        public final int getLastPage()
        {
            return this.numPages;
        }

        public void addText(Container c, String text)
        {
            JLabel label = new JLabel(text);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            c.add(Box.createHorizontalStrut(5));
            c.add(label);
        }

        public void addPage(Container c, int page)
        {
            if (this.pagesAdded > 0)
                c.add(Box.createHorizontalStrut(5));

            PageButton button = this.addPageButton(c, page, Integer.toString(page));
            button.setAllowEnabled(this.curPage != page);
            this.pagesAdded++;
        }

        public void addPrevious(Container c)
        {
            PageButton button = this.addPageButton(c, this.curPage - 1, "prev");
            button.setAllowEnabled(this.curPage > 1);
        }

        public void addNext(Container c)
        {
            PageButton button = this.addPageButton(c, this.curPage + 1, "next");
            button.setAllowEnabled(this.curPage < this.numPages);
        }

        private PageButton addPageButton(Container c, int page, String text)
        {
            int pageIndex = this.pageSize * (page - 1);
            PageButton button = new PageButton(text, pageIndex, actionListener);
            c.add(button);

            return button;
        }
    }
}
