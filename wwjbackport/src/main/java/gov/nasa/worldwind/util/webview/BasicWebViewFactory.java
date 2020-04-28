/*
Copyright (C) 2001, 2010 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.util.webview;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: BasicWebViewFactory.java 14118 2010-11-14 02:36:51Z pabercrombie $
 */
public class BasicWebViewFactory implements WebViewFactory
{
    public BasicWebViewFactory()
    {
    }

    public WebView createWebView(Dimension frameSize)
    {
        if (frameSize == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (Configuration.isLinuxOS())
            return this.createLinuxWebView(frameSize);

        else if (Configuration.isMacOS())
            return this.createMacWebView(frameSize);

        else if (Configuration.isWindowsOS())
            return this.createWindowsWebView(frameSize);

        return this.createUnknownOSWebView(frameSize);
    }

    protected WebView createLinuxWebView(Dimension frameSize)
    {
        return this.createUnknownOSWebView(frameSize); // TODO: implement native WebView for Linux.
    }

    protected WebView createMacWebView(Dimension frameSize)
    {
        return new MacWebView(frameSize);
    }

    protected WebView createWindowsWebView(Dimension frameSize)
    {
        return new WindowsWebView(frameSize);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected WebView createUnknownOSWebView(Dimension frameSize)
    {
        String message = Logging.getMessage("NativeLib.UnsupportedOperatingSystem", "WebView",
            System.getProperty("os.name"));
        Logging.logger().severe(message);
        throw new UnsupportedOperationException(message);
    }
}
