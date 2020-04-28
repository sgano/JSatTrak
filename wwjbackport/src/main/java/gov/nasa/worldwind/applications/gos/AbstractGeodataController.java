/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.applications.gos.event.*;
import gov.nasa.worldwind.applications.gos.awt.StateCardPanel;
import gov.nasa.worldwind.avlist.AVList;

import javax.swing.*;
import java.util.concurrent.Future;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: AbstractGeodataController.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public abstract class AbstractGeodataController implements GeodataController, SearchListener
{
    protected GeodataWindow gwd;
    protected boolean searchEnabled = true;
    private Future taskFuture;

    public AbstractGeodataController()
    {
    }

    protected abstract void doExecuteSearch(AVList params) throws Exception;

    public boolean isSearchEnabled()
    {
        return this.searchEnabled;
    }

    public void setSearchEnabled(boolean enabled)
    {
        this.searchEnabled = enabled;

        if (this.gwd != null)
            this.gwd.setEnabled(enabled);
    }

    public GeodataWindow getGeodataWindow()
    {
        return this.gwd;
    }

    public void setGeodataWindow(GeodataWindow gwd)
    {
        if (this.gwd == gwd)
            return;

        if (this.gwd != null)
        {
            this.gwd.removeSearchListener(this);
        }

        this.gwd = gwd;

        if (this.gwd != null)
        {
            this.gwd.setEnabled(this.searchEnabled);
            this.gwd.addSearchListener(this);
        }
    }

    public void executeSearch(final AVList params)
    {
        if (!this.isSearchEnabled())
            return;

        this.beforeSearch();
        this.taskFuture = ResourceUtil.getAppTaskService().submit(new Runnable()
        {
            public void run()
            {
                try
                {
                    doExecuteSearch(params);
                    afterSearch(null);
                }
                catch (Exception e)
                {
                    afterSearch(e);
                }
            }
        });
    }

    public void cancelSearch()
    {
        if (this.taskFuture != null && !this.taskFuture.isDone() && !this.taskFuture.isCancelled())
        {
            this.taskFuture.cancel(true);
        }
    }

    public void searchPerformed(SearchEvent event)
    {
        if (event == null)
            return;

        this.executeSearch(event.getParams());
    }

    public void searchCancelled(SearchEvent event)
    {
        if (event == null)
            return;

        this.cancelSearch();
    }

    protected void beforeSearch()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    beforeSearch();
                }
            });
        }
        else
        {
            this.setSearchEnabled(false);
            this.gwd.setContentState(GeodataKey.STATE_WAITING);
        }
    }

    protected void afterSearch(final Exception e)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    afterSearch(e);
                }
            });
        }
        else
        {
            this.setSearchEnabled(true);
            if (e != null)
            {
                JComponent ec = StateCardPanel.createErrorComponent(ResourceUtil.createErrorMessage(e),
                    Component.LEFT_ALIGNMENT);
                this.gwd.setContentComponent(GeodataKey.STATE_ERROR, ec);
                this.gwd.setContentState(GeodataKey.STATE_ERROR);
            }
            else
            {
                this.gwd.setContentState(GeodataKey.STATE_NORMAL);
            }
            this.taskFuture = null;
        }
    }
}
