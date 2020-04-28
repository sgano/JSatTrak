/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util.webview;

import com.sun.opengl.util.texture.Texture;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dcollins
 * @version $Id: MacWebView.java 14160 2010-11-29 22:42:37Z pabercrombie $
 */
public class MacWebView extends AVListImpl implements WebView
{
    /** The address of the native WebViewWindow object. Initialized during construction. */
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
    // TODO: catch and handle native exceptions

    public MacWebView(Dimension frameSize)
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

        MacWebViewJNI.invokeInAppKitThread(new Runnable()
        {
            public void run()
            {
                webViewWindowPtr.set(MacWebViewJNI.newWebViewWindow(frameWidth, frameHeight));
                observerPtr.set(MacWebViewJNI.newNotificationAdapter(windowUpdateListener));
                MacWebViewJNI.addWindowUpdateObserver(webViewWindowPtr.get(), observerPtr.get());
            }
        });
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();

        // Free the native WebView object associated with this Java WebView object.
        MacWebViewJNI.invokeInAppKitThread(new Runnable()
        {
            public void run()
            {
                if (webViewWindowPtr.get() != 0 && observerPtr.get() != 0)
                    MacWebViewJNI.removeWindowUpdateObserver(webViewWindowPtr.get(), observerPtr.get());
                if (webViewWindowPtr.get() != 0)
                    MacWebViewJNI.releaseNSObject(webViewWindowPtr.get());
                if (observerPtr.get() != 0)
                    MacWebViewJNI.releaseNSObject(observerPtr.get());

                webViewWindowPtr.set(0);
                observerPtr.set(0);
            }
        });
    }

    /** {@inheritDoc} */
    public void setHTMLString(final String string, URL baseUrl)
    {
        final String htmlString = string != null ? string : "";
        final String baseUrlString = baseUrl != null ? baseUrl.toString() : this.getDefaultBaseURL().toString();

        MacWebViewJNI.invokeInAppKitThread(new Runnable()
        {
            public void run()
            {
                MacWebViewJNI.setHTMLString(webViewWindowPtr.get(), htmlString, baseUrlString);
            }
        });
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

            // Copy the width and height to ensure the values don't change when accessed from the AppKit thread.
            final int width = this.frameSize.width;
            final int height = this.frameSize.height;

            MacWebViewJNI.invokeInAppKitThread(new Runnable()
            {
                public void run()
                {
                    MacWebViewJNI.setFrameSize(webViewWindowPtr.get(), width, height);
                }
            });

            // The texture needs to be regenerated because the frame size changed.
            this.textureRep = null;
        }
    }

    /** {@inheritDoc} */
    public void sendEvent(final InputEvent event)
    {
        if (event == null)
            return;

        // Send the AWT InputEvent to the native WebView object on the AppKit thread.
        MacWebViewJNI.invokeInAppKitThread(new Runnable()
        {
            public void run()
            {
                MacWebViewJNI.sendEvent(webViewWindowPtr.get(), event);
            }
        });
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

    @SuppressWarnings({"UnusedDeclaration"})
    protected WWTexture createTextureRepresentation(DrawContext dc)
    {
        BasicWWTexture texture = new MacWebViewTexture(this.getFrameSize(), false);
        texture.setUseAnisotropy(false); // Do not use anisotropic texture filtering.            

        return texture;
    }

    protected class MacWebViewTexture extends WebViewTexture
    {
        protected long updateTime = -1;

        public MacWebViewTexture(Dimension frameSize, boolean useMipMaps)
        {
            super(frameSize, useMipMaps, true);
        }

        @Override
        protected void updateIfNeeded(DrawContext dc)
        {
            // Return immediately if the native WebViewWindow object isn't initialized, and wait to update until the
            // native object is initialized. This method is called after the texture is bound, so we'll get another
            // chance to update as long as the WebView generates repaint events when it changes.
            long webViewWindowPtr = MacWebView.this.webViewWindowPtr.get();
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
            if (MacWebViewJNI.getUpdateTime(webViewWindowPtr) != this.updateTime)
            {
                MacWebViewJNI.loadDisplayInGLTexture(webViewWindowPtr, texture.getTarget());
                this.updateTime = MacWebViewJNI.getUpdateTime(webViewWindowPtr);
            }
        }
    }
}
