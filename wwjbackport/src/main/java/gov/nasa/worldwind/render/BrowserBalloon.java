/*
Copyright (C) 2001, 2010 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.webview.*;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.URL;
import java.nio.DoubleBuffer;

/**
 * A {@link Balloon} that renders HTML.
 * <p/>
 * <b>Note on BrowserBalloon size:</b> The balloon size is specified as a {@link Size} object in the {@link
 * BalloonAttributes}. However, BrowserBalloon does not have a native size, so a {@link Size} with size mode {@link
 * Size#NATIVE_DIMENSION} or {@link Size#MAINTAIN_ASPECT_RATIO} is <b>invalid</b>, and will result in a
 * IllegalStateException during rendering. The size attribute must explicitly specify the size of the balloon using
 * {@link Size#EXPLICIT_DIMENSION}.
 *
 * @author dcollins
 * @version $Id: BrowserBalloon.java 14221 2010-12-10 23:47:55Z pabercrombie $
 */
public class BrowserBalloon extends AbstractBalloon implements OrderedRenderable, HotSpot
{
    /** Flag to control whether or not the balloon can be resized by the user. */
    protected boolean allowResize = true;

    /** The URL used to resolve relative URLs in the text content. {@code null} indicates the current working directory. */
    protected URL baseURL;
    /** Identifies the frame used to calculate the balloon points. */
    protected long geomFrameNumber = -1;
    /** Identifies the time when the balloon text was updated. */
    protected long textUpdateTime = -1;
    /**
     * The Cartesian point corresponding to the balloon position. This is null if the balloon's attachment mode is not
     * {@link #GLOBE_MODE}.
     */
    protected Vec4 placePoint;
    /**
     * The location of the balloon's placePoint in the viewport (on the screen). This is null if the balloon's
     * attachment mode is not {@link #GLOBE_MODE}.
     */
    protected Vec4 screenPlacePoint;
    /**
     * The top left corner of the frame in AWT coordinates. This point is necessary to handle pick events in screen
     * coordinates.
     */
    protected Point topLeftScreenPoint;
    /** The location and size of the balloon in the viewport (on the screen). */
    protected Rectangle screenRect;
    /** The location and size of the WebView in the viewport (on the screen). */
    protected Rectangle webViewRect;
    /** Used to order the balloon  as an ordered renderable. */
    protected double eyeDistance;
    /** The balloon geometry vertices passed to OpenGL. */
    protected DoubleBuffer vertexBuffer;
    /** Interface for interacting with the operating system's web browser control. */
    protected WebView webView;
    /** The layer active during the most recent pick pass. */
    protected Layer pickLayer;
    /** Support for setting up and restoring picking state, and resolving the picked object. */
    protected PickSupport pickSupport = new PickSupport();
    /** Support for setting up and restoring OpenGL state during rendering. */
    protected OGLStackHandler osh = new OGLStackHandler();
    /** Flag set if we fail to create the web view. We do not retry the web view creation if it fails. */
    protected boolean failedToCreateWebView;

    // UI controls
    protected BalloonResizeHotSpot resizeControl;
    /** Width of the pickable area on each resizable edge of the frame. */
    protected int resizePickWidth = 10;

    public BrowserBalloon(String text, Position position)
    {
        super(text, position);
    }

    public BrowserBalloon(String text, Point point)
    {
        super(text, point);
    }

    public URL getBaseURL()
    {
        return this.baseURL;
    }

    public void setBaseURL(URL baseURL)
    {
        this.baseURL = baseURL;
    }

    public Rectangle getBounds(DrawContext dc)
    {
        return null; //TODO: Determine what this method on Balloon is used for and remove it if it's unnecessary.
    }

    public double getDistanceFromEye()
    {
        return this.eyeDistance;
    }

    public void pick(DrawContext dc, Point pickPoint)
    {
        // This method is called only when ordered renderables are being drawn.
        // Arg checked within call to render.

        if (!this.isPickEnabled())
            return;

        this.pickSupport.clearPickList();
        try
        {
            this.pickSupport.beginPicking(dc);
            this.render(dc);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
        }
    }

    public void render(DrawContext dc)
    {
        // This render method is called three times during frame generation. It's first called as a Renderable during
        // picking. It's called again during normal rendering. And it's called a third time as an OrderedRenderable. The
        // first two calls determine whether to add the placemark  and its optional line to the ordered renderable list
        // during pick and render. The third call just draws the ordered renderable.

        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isVisible())
            return;

        if (dc.isOrderedRenderingMode())
            this.drawOrderedRenderable(dc);
        else
            this.makeOrderedRenderable(dc);
    }

    /**
     * Does nothing; BrowserBalloon does not handle select events.
     *
     * @param event The event to handle.
     */
    public void selected(SelectEvent event)
    {
    }

    /**
     * Forwards the key typed event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView} and
     * consumes the event. This consumes the event so the {@link gov.nasa.worldwind.View} doesn't respond to it.
     *
     * @param event The event to forward.
     */
    public void keyTyped(KeyEvent event)
    {
        if (event == null)
            return;

        this.sendToWebView(event);
        event.consume(); // Consume the event so the View doesn't respond to it.
    }

    /**
     * Forwards the key pressed event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView} and
     * consumes the event. This consumes the event so the {@link gov.nasa.worldwind.View} doesn't respond to it. The
     *
     * @param event The event to forward.
     */
    public void keyPressed(KeyEvent event)
    {
        if (event == null)
            return;

        this.sendToWebView(event);
        event.consume(); // Consume the event so the View doesn't respond to it.
    }

    /**
     * Forwards the key released event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView} and
     * consumes the event. This consumes the event so the {@link gov.nasa.worldwind.View} doesn't respond to it.
     *
     * @param event The event to forward.
     */
    public void keyReleased(KeyEvent event)
    {
        if (event == null)
            return;

        this.sendToWebView(event);
        event.consume(); // Consume the event so the View doesn't respond to it.
    }

    /**
     * Forwards the mouse clicked event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView}. This
     * does not consume the event, because the {@link gov.nasa.worldwind.event.InputHandler} implements the policy for
     * consuming or forwarding mouse clicked events to other objects.
     *
     * @param event The event to forward.
     */
    public void mouseClicked(MouseEvent event)
    {
        if (event == null)
            return;

        this.sendToWebView(event);
    }

    /**
     * Forwards the mouse pressed event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView}. This
     * does not consume the event, because the {@link gov.nasa.worldwind.event.InputHandler} implements the policy for
     * consuming or forwarding mouse pressed events to other objects.
     *
     * @param event The event to forward.
     */
    public void mousePressed(MouseEvent event)
    {
        if (event == null)
            return;

        this.sendToWebView(event);
    }

    /**
     * Forwards the mouse released event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView}. This
     * does not consume the event, because the {@link gov.nasa.worldwind.event.InputHandler} implements the policy for
     * consuming or forwarding mouse released events to other objects.
     *
     * @param event The event to forward.
     */
    public void mouseReleased(MouseEvent event)
    {
        if (event == null)
            return;

        this.sendToWebView(event);
    }

    /**
     * Does nothing; BrowserBalloon does not handle mouse entered events.
     *
     * @param event The event to handle.
     */
    public void mouseEntered(MouseEvent event)
    {
    }

    /**
     * Does nothing; BrowserBalloon does not handle mouse exited events.
     *
     * @param event The event to handle.
     */
    public void mouseExited(MouseEvent event)
    {
    }

    /**
     * Forwards the mouse dragged event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView}. This
     * does not consume the event, because the {@link gov.nasa.worldwind.event.InputHandler} implements the policy for
     * consuming or forwarding mouse dragged events to other objects.
     *
     * @param event The event to forward.
     */
    public void mouseDragged(MouseEvent event)
    {
        if (event == null)
            return;

        this.sendToWebView(event);
    }

    /**
     * Forwards the mouse moved event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView}. This
     * does not consume the event, because the {@link gov.nasa.worldwind.event.InputHandler} implements the policy for
     * consuming or forwarding mouse moved events to other objects.
     *
     * @param event The event to forward.
     */
    public void mouseMoved(MouseEvent event)
    {
        if (event == null)
            return;

        this.sendToWebView(event);
    }

    /**
     * Forwards the mouse wheel event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView} and
     * consumes the event. This consumes the event so the {@link gov.nasa.worldwind.View} doesn't respond to it.
     *
     * @param event The event to forward.
     */
    public void mouseWheelMoved(MouseWheelEvent event)
    {
        if (event == null)
            return;

        this.sendToWebView(event);
        event.consume(); // Consume the event so the View doesn't respond to it.
    }

    /**
     * Returns a {@code null} Cursor, indicating the default cursor should be used when the BrowserBalloon is active.
     * The Cursor is set by the {@link gov.nasa.worldwind.util.webview.WebView} in response to mouse moved events.
     *
     * @return A {@code null} Cursor.
     */
    public Cursor getCursor()
    {
        return null;
    }

    //**************************************************************//
    //********************  Rendering  *****************************//
    //**************************************************************//

    protected void makeOrderedRenderable(DrawContext dc)
    {
        // Determines whether to queue an ordered renderable for the balloon.

        // Re-use values already calculated this frame.
        if (dc.getFrameTimeStamp() != this.geomFrameNumber)
        {
            this.determineActiveAttributes();
            this.computeBalloonGeometry(dc);
            if (this.screenRect == null)
                return;

            this.geomFrameNumber = dc.getFrameTimeStamp();
        }

        if (this.intersectsFrustum(dc))
            dc.addOrderedRenderable(this);

        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();
    }

    protected void drawOrderedRenderable(DrawContext dc)
    {
        this.beginDrawing(dc);
        try
        {
            this.doDrawOrderedRenderable(dc);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    /**
     * Determines which attributes -- normal, highlight or default -- to use each frame.
     *
     * @throws IllegalStateException if the size attribute uses a size mode that is not valid for a BrowserBalloon. See
     *                               BrowserBalloon class documentation for details.
     */
    @Override
    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes();

        // The balloon does not have a native size, so the size must be specified explicitly as a pixel dimension,
        // or as a fraction of the screen. The MAINTAIN_ASPECT_RATIO and NATIVE_DIMENSION size modes are not valid
        // for BrowserBalloon.
        Size balloonSize = this.activeAttributes.getSize();
        if (balloonSize.getHeightMode() == Size.NATIVE_DIMENSION
            || balloonSize.getHeightMode() == Size.MAINTAIN_ASPECT_RATIO
            || balloonSize.getWidthMode() == Size.NATIVE_DIMENSION
            || balloonSize.getWidthMode() == Size.MAINTAIN_ASPECT_RATIO)
        {
            String message = Logging.getMessage("Geom.RequireExplicitSize");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
    }

    protected void beginDrawing(DrawContext dc)
    {
        GL gl = dc.getGL();

        int attrMask =
            GL.GL_COLOR_BUFFER_BIT // For alpha enable, blend enable, alpha func, blend func.
                | GL.GL_CURRENT_BIT // For current color
                | GL.GL_ENABLE_BIT // For depth test disable.
                | GL.GL_DEPTH_BUFFER_BIT
                | GL.GL_LINE_BIT // For outline width.
                | GL.GL_TEXTURE_BIT; // For texture enable, texture gen enable, texture binding, texture gen mode, texture gen plane equations.

        this.osh.pushAttrib(gl, attrMask);
        this.osh.pushClientAttrib(gl, GL.GL_CLIENT_VERTEX_ARRAY_BIT); // For vertex array enable, vertex array pointers.
        this.osh.pushProjectionIdentity(gl);
        // The browser balloon is drawn using a parallel projection sized to fit the viewport.
        gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);
        this.osh.pushTextureIdentity(gl);
        this.osh.pushModelviewIdentity(gl);

        gl.glEnableClientState(GL.GL_VERTEX_ARRAY); // All drawing uses vertex arrays.

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_LINE_SMOOTH); // Smooth balloon outlines when not picking.
            gl.glEnable(GL.GL_BLEND); // Enable interior and outline alpha blending when not picking.
            OGLUtil.applyBlending(gl, false);
        }
    }

    protected void endDrawing(DrawContext dc)
    {
        this.osh.pop(dc.getGL());
    }

    protected void setupDepthTest(DrawContext dc)
    {
        GL gl = dc.getGL();

        if (!this.isAlwaysOnTop() && GLOBE_MODE.equals(this.getAttachmentMode())
            && dc.getView().getEyePosition().getElevation() < (dc.getGlobe().getMaxElevation()
            * dc.getVerticalExaggeration()))
        {
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);

            // Adjust depth of image to bring it slightly forward
            double depth = this.screenPlacePoint.z - (8d * 0.00048875809d);
            depth = depth < 0d ? 0d : (depth > 1d ? 1d : depth);
            gl.glDepthFunc(GL.GL_LESS);
            gl.glDepthRange(depth, depth);
        }
        else
        {
            gl.glDisable(GL.GL_DEPTH_TEST);
        }
    }

    protected void doDrawOrderedRenderable(DrawContext dc)
    {
        GL gl = dc.getGL();

        if (!dc.isDeepPickingEnabled())
            this.setupDepthTest(dc);

        if (dc.isPickingMode())
        {
            // Apply a unique pick color and register the balloon with the picked object list if we're in picking mode.
            Color pickColor = dc.getUniquePickColor();
            PickedObject po = new PickedObject(pickColor.getRGB(),
                this.getDelegateOwner() != null ? this.getDelegateOwner() : this, this.globePosition, false);

            // If the delegate owner is not null the balloon is no longer the picked object. This prevents the
            // application from interacting with the balloon via the HotSpot interface. We attach the balloon to the
            // picked object's AVList under the key HOT_SPOT. The application can therefore find the HotSpot by looking
            // in the picked object's AVList.
            if (this.getDelegateOwner() != null)
                po.setValue(AVKey.HOT_SPOT, this);

            this.pickSupport.addPickableObject(po);
            gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
        }

        // Translate to the balloon's screen origin and bind the balloon's vertex buffer as source of GL vertex data.
        // Use integer coordinates to ensure that the image texels are aligned exactly with screen pixels.
        gl.glTranslatef(this.screenRect.x, this.screenRect.y, 0);
        gl.glVertexPointer(2, GL.GL_DOUBLE, 0, this.vertexBuffer);

        if (!dc.isPickingMode())
        {
            // Apply the balloon's background color if we're in normal rendering mode.
            Color color = this.getActiveAttributes().getBackgroundColor();
            gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
                (byte) color.getAlpha());
        }

        // Apply the balloon's WebView and draw the balloon's geometry as a triangle fan to display the interior. The
        // balloon's vertices are in screen coordinates, so we just translate to the screen origin and draw.
        this.applyWebView(dc);
        gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, this.vertexBuffer.remaining() / 2);

        if (!dc.isPickingMode())
        {
            // Apply the balloon's outline color if we're in normal rendering mode.
            Color color = this.getActiveAttributes().getBorderColor();
            gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
                (byte) color.getAlpha());
        }

        // Disable texturing to avoid applying the WebView to the balloon's outline, apply the balloon's outline width,
        // and draw the balloon's geometry as a line strip to display the outline. The balloon's vertices are in screen
        // coordinates, so we just translate to the screen origin and draw.
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glLineWidth((float) this.getActiveAttributes().getBorderWidth());
        gl.glDrawArrays(GL.GL_LINE_STRIP, 0, this.vertexBuffer.remaining() / 2);

        // Draw resize hotspot areas if resizing is allowed.
        if (dc.isPickingMode() && this.isAllowResize())
            this.pickResizeControls(dc);
    }

    /**
     * Draw pickable regions for the resize controls. A pickable region is drawn along the frame outline.
     *
     * @param dc Draw context.
     */
    protected void pickResizeControls(DrawContext dc)
    {
        GL gl = dc.getGL();

        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glLineWidth((float) this.resizePickWidth);

        Color color = dc.getUniquePickColor();
        this.pickSupport.addPickableObject(color.getRGB(), new BalloonResizeHotSpot());
        gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

        gl.glDrawArrays(GL.GL_LINE_STRIP, 0, this.vertexBuffer.remaining() / 2);
    }

    protected void applyWebView(DrawContext dc)
    {
        // Don't apply the WebView texture during picking.
        if (dc.isPickingMode())
            return;

        // Attempt to create the balloon's WebView. Exit immediately if this fails. Do not rety web view creationg if
        // we have already tried and failed.
        if (this.webView == null && !this.failedToCreateWebView)
        {
            try
            {
                this.webView = this.createWebView(this.webViewRect.getSize());
            }
            catch (Throwable t)
            {
                String message = Logging.getMessage("WebView.ExceptionCreatingWebView", t);
                Logging.logger().severe(message);

                dc.addRenderingException(t);

                // Set flag to prevent retrying the web view creation. We assume that if this fails once it will continue
                // to fail.
                this.failedToCreateWebView = true;
            }

            // Configure the balloon to forward the WebView's property change events to its listeners.
            if (this.webView != null)
                this.webView.addPropertyChangeListener(this);
        }

        if (this.webView == null)
            return;

        // The WebView's screen size can change each frame. Synchronize the WebView's frame size with the desired size
        // before attempting to use the WebView's texture. The WebView avoids doing unnecessary work when the same frame
        // size is specified.
        this.webView.setFrameSize(this.webViewRect.getSize());

        // Update the WebView's text content each time the balloon's decoded string changes.
        // TODO: resets content after user navigates away from page. Be sure this case can actually happen, and we care about preventing it before designing anything to handle it.
        String text = this.getTextDecoder().getDecodedText();
        if (this.getTextDecoder().getLastUpdateTime() != this.textUpdateTime)
        {
            this.webView.setHTMLString(text, this.getBaseURL());
            this.textUpdateTime = this.getTextDecoder().getLastUpdateTime();
        }

        // Attempt to get the WebView's texture representation. Exit immediately if this fails or if the texture cannot
        // be bound.
        WWTexture texture = this.webView.getTextureRepresentation(dc);
        if (texture == null)
            return;

        if (!texture.bind(dc))
            return;

        GL gl = dc.getGL();

        // The WebView's texture is successfully bound. Enable GL texturing and set up the texture environment to apply
        // the texture in decal mode. Decal mode uses the texture color where the texture's alpha is 1, and uses the
        // balloon's background color where it's 0. The texture's internal format must be RGBA to work correctly, and we
        // assume that the WebView's texture format is RGBA.
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);

        // Set up GL automatic texture coordinate generation to define texture coordinates equivalent to the balloon's
        // vertex coordinates in object space. The balloon's vertices are defined in screen coordinates, so the
        // generated texture coordinates are equivalent to the vertex coordinates. We transform texture coordinates from
        // screen space into WebView texture space below by applying the appropriate transforms on the texture matrix
        // stack.
        gl.glEnable(GL.GL_TEXTURE_GEN_S);
        gl.glEnable(GL.GL_TEXTURE_GEN_T);
        gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
        gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
        gl.glTexGenfv(GL.GL_S, GL.GL_OBJECT_PLANE, new float[] {1, 0, 0, 0}, 0);
        gl.glTexGenfv(GL.GL_T, GL.GL_OBJECT_PLANE, new float[] {0, 1, 0, 0}, 0);

        // Translate texture coordinates to place the WebView's texture in the WebView's screen rectangle. Use integer
        // coordinates when possible to ensure that the image texels are aligned exactly with screen pixels. This
        // transforms texture coordinates such that (webViewRect.getMinX(), webViewRect.getMinY()) maps to (0, 0) - the
        // texture's lower left corner, and (webViewRect.getMaxX(), webViewRect.getMaxY()) maps to (1, 1) - the
        // texture's upper right corner. Since texture coordinates are generated relative to the screenRect origin and
        // webViewRect is in screen coordinates, we translate the texture coordinates by the offset from the screenRect
        // origin to the webViewRect origin.
        texture.applyInternalTransform(dc);
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glScalef(1f / this.webViewRect.width, 1f / this.webViewRect.height, 1f);
        gl.glTranslatef(this.screenRect.x - this.webViewRect.x, this.screenRect.y - this.webViewRect.y, 0f);
    }

    protected WebView createWebView(Dimension frameSize)
    {
        WebViewFactory factory = (WebViewFactory) WorldWind.createConfigurationComponent(AVKey.WEB_VIEW_FACTORY);
        return factory.createWebView(frameSize);
    }

    /**
     * Computes and stores the balloon's Cartesian location and the screen-space projection of the balloon's point.
     * Applies the balloon's altitude mode when computing the Cartesian location.
     *
     * @param dc The current draw context.
     */
    protected void computeBalloonGeometry(DrawContext dc)
    {
        this.placePoint = null;
        this.screenRect = null;
        this.webViewRect = null;

        BalloonAttributes activeAttrs = this.getActiveAttributes();
        Size balloonSize = activeAttrs.getSize();

        Dimension screenSize = balloonSize.compute(1, 1, dc.getView().getViewport().width,
            dc.getView().getViewport().height);
        Point2D screenOffset = activeAttrs.getDrawOffset().computeOffset(screenSize.width, screenSize.height, 1d, 1d);

        if (GLOBE_MODE.equals(this.getAttachmentMode()))
        {
            if (this.altitudeMode == WorldWind.CLAMP_TO_GROUND)
            {
                this.placePoint = dc.computeTerrainPoint(
                    this.globePosition.getLatitude(), this.globePosition.getLongitude(), 0);
            }
            else if (this.altitudeMode == WorldWind.RELATIVE_TO_GROUND)
            {
                this.placePoint = dc.computeTerrainPoint(
                    this.globePosition.getLatitude(), this.globePosition.getLongitude(),
                    this.globePosition.getAltitude());
            }
            else // ABSOLUTE
            {
                // TODO: conditionally apply VE.
                double height = this.globePosition.getElevation() * dc.getVerticalExaggeration();
                this.placePoint = dc.getGlobe().computePointFromPosition(
                    this.globePosition.getLatitude(), this.globePosition.getLongitude(), height);
            }

            if (placePoint != null)
            {
                this.screenPlacePoint = dc.getView().project(this.placePoint);
                this.screenRect = new Rectangle((int) (this.screenPlacePoint.x + screenOffset.getX()),
                    (int) (this.screenPlacePoint.y + screenOffset.getY()),
                    screenSize.width, screenSize.height);

                this.eyeDistance = dc.getView().getEyePoint().distanceTo3(placePoint);

                this.vertexBuffer = FrameFactory.createShapeWithLeaderBuffer(activeAttrs.getBalloonShape(),
                    screenSize.width, screenSize.height,
                    new Point((int) -screenOffset.getX(), (int) -screenOffset.getY()),
                    activeAttrs.getLeaderWidth(), activeAttrs.getCornerRadius(), this.vertexBuffer);
            }
        }
        else if (SCREEN_MODE.equals(this.getAttachmentMode()))
        {
            this.screenRect = new Rectangle((int) (this.screenPoint.x + screenOffset.getX()),
                (int) (this.screenPoint.y + screenOffset.getY()), screenSize.width, screenSize.height);
            this.eyeDistance = 0;
            this.vertexBuffer = FrameFactory.createShapeBuffer(activeAttrs.getBalloonShape(), this.screenRect.width,
                this.screenRect.height, activeAttrs.getCornerRadius(), this.vertexBuffer);
        }

        // Find the top left corner of the frame in AWT coordinates
        this.topLeftScreenPoint = new Point(this.screenRect.x,
            (dc.getView().getViewport().height - this.screenRect.y) - this.screenRect.height);
        
        Insets insets = activeAttrs.getInsets();
        this.webViewRect = new Rectangle(
            this.screenRect.x + insets.left,
            this.screenRect.y + insets.bottom,
            this.screenRect.width - (insets.left + insets.right),
            this.screenRect.height - (insets.bottom + insets.top));
    }

    /**
     * Determines whether the balloon intersects the view frustum.
     *
     * @param dc The current draw context.
     *
     * @return {@code true} If the balloon intersects the frustum, otherwise {@code false}.
     */
    protected boolean intersectsFrustum(DrawContext dc)
    {
        View view = dc.getView();
        if (this.eyeDistance < view.getNearClipDistance() || this.eyeDistance > view.getFarClipDistance())
            return false;

        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(this.screenRect);
        else
            return dc.getView().getViewport().intersects(this.screenRect);
    }

    /**
     * Can the balloon be resized by the user?
     *
     * @return true if the balloon allows the user to resize the balloon frame by dragging the mouse.
     *
     * @see #setAllowResize(boolean)
     */
    public boolean isAllowResize()
    {
        return this.allowResize;
    }

    /**
     * Set the balloon to allow or disallow resizing the by the user. If resizing is allowed, the user can click the
     * edge of the balloon frame and drag the mouse to resize the balloon.
     *
     * @param allowResize True if resize is allowed.
     *
     * @see #isAllowResize()
     */
    public void setAllowResize(boolean allowResize)
    {
        this.allowResize = allowResize;
    }

    //**************************************************************//
    //********************  Input Events  **************************//
    //**************************************************************//

    /**
     * Sends the specified event to the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView}. If the event
     * is a {@link java.awt.event.MouseEvent}, the cursor point is converted from AWT coordinates to the WebView's local
     * coordinate system, and a copy of the event with the new point is sent to the WebView.
     * <p/>
     * This does nothing if the event is {@code null} or if the balloon's WebView is uninitialized.
     *
     * @param event The event to send.
     */
    protected void sendToWebView(InputEvent event)
    {
        if (event == null)
            return;

        if (this.webView == null)
            return;

        if (event instanceof MouseEvent)
        {
            if (!this.intersectsWebView((MouseEvent) event))
                return;

            event = this.convertToWebView((MouseEvent) event);
        }

        this.webView.sendEvent(event);
    }

    /**
     * Determines whether the balloon's internal {@link gov.nasa.worldwind.util.webview.WebView} intersects the mouse
     * event's screen point.
     *
     * @param e The event to test.
     *
     * @return {@code true} if the WebView intersects the mouse event's point, otherwise {@code false}.
     */
    protected boolean intersectsWebView(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();

        // Translate AWT coordinates to OpenGL screen coordinates by moving the Y origin from the upper left corner to
        // the lower left corner and flipping the direction of the Y axis.
        if (e.getSource() instanceof Component)
        {
            y = ((Component) e.getSource()).getHeight() - e.getY();
        }

        return this.webViewRect.contains(x, y);
    }

    /**
     * Converts the specified mouse event's screen point from AWT coordinates to local WebView coordinates, and returns
     * a new event who's screen point is in WebView local coordinates.
     *
     * @param e The event to convert.
     *
     * @return A new mouse event in the WebView's local coordinate system.
     */
    protected MouseEvent convertToWebView(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();

        // Translate AWT coordinates to OpenGL screen coordinates by moving the Y origin from the upper left corner to
        // the lower left corner and flipping the direction of the Y axis.
        if (e.getSource() instanceof Component)
        {
            y = ((Component) e.getSource()).getHeight() - e.getY();
        }

        x -= this.webViewRect.x;
        y -= this.webViewRect.y;

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

    class BalloonResizeHotSpot extends AbstractResizeHotSpot
    {
        @Override
        protected Dimension getSize()
        {
            Rectangle rect = BrowserBalloon.this.screenRect;
            return rect != null ? rect.getSize() : null;
        }

        @Override
        protected void setSize(Dimension newSize)
        {
            Size size = Size.fromPixels(newSize.width, newSize.height);
            getAttributes().setSize(size);
        }

        @Override
        protected Point getScreenPoint()
        {
            return BrowserBalloon.this.topLeftScreenPoint;
        }

        @Override
        protected void setScreenPoint(Point newPoint)
        {
            // We can only set the position of a screen balloon
            if (Balloon.SCREEN_MODE.equals(BrowserBalloon.this.getAttachmentMode()))
                setPosition(newPoint);
        }

        @Override
        protected Dimension getMinimumSize()
        {
            // Twice the corner radius of the frame is as small as the balloon can get without seriously distorting
            // the geometry.
            int cornerRadius = BrowserBalloon.this.getActiveAttributes().getCornerRadius();
            return new Dimension(cornerRadius * 2, cornerRadius * 2);
        }
    }
}
