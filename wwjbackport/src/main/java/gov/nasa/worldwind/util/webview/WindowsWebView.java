/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.webview;

import com.sun.opengl.util.texture.Texture;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author pabercrombie
 * @version $Id $
 */
public class WindowsWebView extends AVListImpl implements WebView
{
    /** The address of the native WindowsWebView object. Initialized during construction. */
    protected AtomicLong webViewWindowPtr = new AtomicLong(0);
    /** The address of the native NotificationAdapter object. Initialized during construction. */
    protected AtomicLong observerPtr = new AtomicLong(0);
    /** The size of the WebView frame in pixels. Initially null, indicating the default size is used. */
    protected Dimension frameSize;
    /** The WebView's current texture representation. Lazily created in {@link #getTextureRepresentation}. */
    protected WWTexture textureRep;
    /** The property change listener notified when the native WebViewWindow is updated. */
    protected final PropertyChangeListener windowUpdateListener = new WebViewUpdateListener(this);

    // TODO: throw an exception if wrong os
    // TODO: Hyperlink listener
    // TODO: catch and handle native exceptions

    public WindowsWebView(Dimension frameSize)
    {
        if (frameSize == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.frameSize = frameSize;

        // Copy the width and height to ensure the values don't change when accessed from the AppKit thread.
        final int frameWidth = this.frameSize.width;
        final int frameHeight = this.frameSize.height;

        final SynchronousQueue<Long> syncQueue = new SynchronousQueue<Long>();

        // Run a message loop for the web view in its own thread.
        // TODO: run only one message pump shared by all the web view instances.
        new Thread("Web View UI") {
            public void run()
            {
                long address = WindowsWebViewJNI.newWebViewWindow(frameWidth, frameHeight);
                try
                {
                    syncQueue.put(address);
                }
                catch (InterruptedException ignored) { }

                long notificationAdapterAddress = WindowsWebViewJNI.newNotificationAdapter(windowUpdateListener);
                observerPtr.set(notificationAdapterAddress);
                WindowsWebViewJNI.addWindowUpdateObserver(address, observerPtr.get());

                WindowsWebViewJNI.runMessageLoop();
            }
        }.start();

        // Wait for the web view UI thread to supply the window pointer
        Long address = 0L;
        try
        {
            address = syncQueue.take();
        }
        catch (InterruptedException ignored) { }

        this.webViewWindowPtr.set(address);
    }

    @Override
    protected void finalize() throws Throwable                          
    {
        super.finalize();

        // Remove the notification adapter
        if (webViewWindowPtr.get() != 0 && observerPtr.get() != 0)
            WindowsWebViewJNI.removeWindowUpdateObserver(webViewWindowPtr.get(), observerPtr.get());

        // Free the native WebView object associated with this Java WebView object.
        if (webViewWindowPtr.get() != 0)
            WindowsWebViewJNI.releaseBrowser(webViewWindowPtr.get());
        if (observerPtr.get() != 0)
            WindowsWebViewJNI.releaseComObject(observerPtr.get());

        this.webViewWindowPtr.set(0);
        this.observerPtr.set(0);
    }

    /** {@inheritDoc} */
    public void setHTMLString(final String string, URL baseUrl)
    {
        final String htmlString = string != null ? string : "";
        final String baseUrlString = baseUrl != null ? baseUrl.toString() : this.getDefaultBaseURL().toString();

        WindowsWebViewJNI.setHTMLString(webViewWindowPtr.get(), htmlString, baseUrlString);
    }

    /** {@inheritDoc} */
    public Dimension getFrameSize()
    {
        return this.frameSize;
    }

    /** {@inheritDoc} */
    public void setFrameSize(Dimension size)
    {
        if (size == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Setting the frame size requires a call into native code, and requires us to regenerate the texture. Only
        // do this if the size has actually changed.
        if (!size.equals(this.frameSize))
        {
            this.frameSize = size;

            WindowsWebViewJNI.setFrameSize(webViewWindowPtr.get(), this.frameSize.width, this.frameSize.height);

            // The texture needs to be regenerated because the frame size changed.
            this.textureRep = null;
        }
    }

    /** {@inheritDoc} */
    public void sendEvent(InputEvent event)
    {
        if (event == null)
            return;

        // Convert OpenGL coordinates to Windows
        if (event instanceof MouseEvent)
            event = convertToWindows((MouseEvent)event);

        // Send the AWT InputEvent to the native WebView object
        WindowsWebViewJNI.sendEvent(webViewWindowPtr.get(), event);
    }

    /**
     * Converts the specified mouse event's screen point from WebView coordinates to Windows coordinates, and returns
     * a new event who's screen point is in Windows coordinates, with the origin at the upper left corner of the webview
     * window.
     *
     * @param e The event to convert.
     *
     * @return A new mouse event in the Windows coordinate system.
     */
    protected MouseEvent convertToWindows(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();

        // Translate OpenGL screen coordinates to Windows by moving the Y origin from the lower left corner to
        // the upper left corner and flipping the direction of the Y axis.
        y = this.frameSize.height - y;

        if (e instanceof MouseWheelEvent)
        {
            return new MouseWheelEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), x, y,
                e.getClickCount(), e.isPopupTrigger(), ((MouseWheelEvent) e).getScrollType(),
                ((MouseWheelEvent) e).getScrollAmount(), ((MouseWheelEvent) e).getWheelRotation());
        }
        else
        {
            return new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), x, y,
                e.getClickCount(), e.isPopupTrigger(), e.getButton());
        }
    }


    /** {@inheritDoc} */
    public void addHyperlinkListener(HyperlinkListener listener)
    {
        // TODO: implement hyperlink listener
    }

    /** {@inheritDoc} */
    public void removeHyperlinkListener(HyperlinkListener listener)
    {
        // TODO: implement hyperlink listener
    }

    /** {@inheritDoc} */
    public WWTexture getTextureRepresentation(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.textureRep == null)
            this.textureRep = this.createTextureRepresentation(dc);

        return this.textureRep;
    }

    protected URL getDefaultBaseURL()
    {
        File file = new File(Configuration.getCurrentWorkingDirectory());

        try
        {
            return file.toURI().toURL();
        }
        catch (MalformedURLException e)
        {
            return null; // TODO: determine if WebKit requires a base url.
        }
    }

    //**********************************************************************//
    //********************  Texture Representation  ************************//
    //**********************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected WWTexture createTextureRepresentation(DrawContext dc)
    {
        BasicWWTexture texture = new WindowsWebViewTexture(this.getFrameSize(), false);
        texture.setUseAnisotropy(false); // Do not use anisotropic texture filtering.

        return texture;
    }

    protected class WindowsWebViewTexture extends WebViewTexture
    {
        protected long updateTime = -1;

        public WindowsWebViewTexture(Dimension frameSize, boolean useMipMaps)
        {
            super(frameSize, useMipMaps, true);
        }

        @Override
        protected void updateIfNeeded(DrawContext dc)
        {
            // Return immediately if the native WebViewWindow object isn't initialized, and wait to update until the
            // native object is initialized. This method is called after the texture is bound, so we'll get another
            // chance to update as long as the WebView generates repaint events when it changes.
            long webViewWindowPtr = WindowsWebView.this.webViewWindowPtr.get();
            if (webViewWindowPtr == 0)
                return;

            // Return immediately if the texture isn't in the texture cache, and wait to update until the texture is
            // initialized and placed in the cache. This method is called after the texture is bound, so we'll get
            // another chance to update as long as the WebView generates repaint events when it changes.
            Texture texture = this.getTextureFromCache(dc);
            if (texture == null)
                return;

            // Load the WebViewWindow's current display pixels into the currently bound OGL texture if our update time
            // is different than the WebViewWindow's update time.
            if (WindowsWebViewJNI.getUpdateTime(webViewWindowPtr) != this.updateTime)
            {
                WindowsWebViewJNI.loadDisplayInGLTexture(webViewWindowPtr, texture.getTarget());
                this.updateTime = WindowsWebViewJNI.getUpdateTime(webViewWindowPtr);
            }
        }
    }
}
