/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.webview;

import java.awt.event.*;
import java.beans.PropertyChangeListener;

/**
 * @author pabercrombie
 * @version $Id $
 */
public class WindowsWebViewJNI
{
    static
    {
        System.loadLibrary("webview");
        initNativeCache();
    }

    protected static native void initNativeCache();

    protected static native void runMessageLoop();

    protected static native void releaseBrowser(long webViewWindowPtr);

    protected static native void releaseComObject(long unknownPtr);

    protected static native long newWebViewWindow(int frameWidth, int frameHeight);

    public static native long newNotificationAdapter(PropertyChangeListener listener);
    
    protected static native void setHTMLString(long webViewWindowPtr, String htmlString, String baseUrlString);

    protected static native void setFrameSize(long webViewWindowPtr, int width, int height);

    protected static native void sendEvent(long webViewWindowPtr, InputEvent event);

    protected static native long getUpdateTime(long webViewWindowPtr);

    public static native void addWindowUpdateObserver(long webViewWindowPtr, long observerPtr);

    public static native void removeWindowUpdateObserver(long webViewWindowPtr, long observerPtr);
    
    protected static native void loadDisplayInGLTexture(long webViewWindowPtr, int target);
}
