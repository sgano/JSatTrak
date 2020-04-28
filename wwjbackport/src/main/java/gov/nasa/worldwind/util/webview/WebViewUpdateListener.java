/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.webview;

import gov.nasa.worldwind.avlist.AVKey;

import javax.swing.*;
import java.beans.*;

/**
 * @author pabercrombie
 * @version $Id: WebViewUpdateListener.java 14147 2010-11-23 00:55:41Z pabercrombie $
 */
public class WebViewUpdateListener implements PropertyChangeListener
{
    protected WebView webView;

    public WebViewUpdateListener(WebView webView)
    {
        this.webView = webView;
    }

    public void propertyChange(final PropertyChangeEvent event)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    propertyChange(event);
                }
            });
        }
        else
        {
            this.webView.firePropertyChange(AVKey.REPAINT, null, this.webView);
        }
    }
}
