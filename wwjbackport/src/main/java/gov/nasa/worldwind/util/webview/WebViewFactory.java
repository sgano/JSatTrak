/*
Copyright (C) 2001, 2010 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.util.webview;

import java.awt.*;

/**
 * Factory interface for creating {@link gov.nasa.worldwind.util.webview.WebView} instances.
 *
 * @author dcollins
 * @version $Id: WebViewFactory.java 14093 2010-11-06 20:21:49Z dcollins $
 */
public interface WebViewFactory
{
    /**
     * Returns a new WebView with the specified {@code frameSize}.
     *
     * @param frameSize The size in pixels of the WebView's window frame.
     *
     * @return a new WebView with the specified {@code frameSize}.
     *
     * @throws IllegalArgumentException if {@code frameSize} is {@code null}.
     */
    WebView createWebView(Dimension frameSize);
}
