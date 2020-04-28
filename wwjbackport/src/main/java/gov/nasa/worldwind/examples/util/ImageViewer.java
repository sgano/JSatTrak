/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.net.URL;

/**
 * Component that displays an image.
 *
 * @author Patrick Murris
 * @version $Id: ImageViewer.java 8870 2009-02-17 14:49:15Z patrickmurris $
 */
public class ImageViewer extends JComponent
{
    public final static String SCALE_FIXED = "ImageViewer.ScaleFixed";
    public final static String SCALE_BEST_FIT = "ImageViewer.ScaleBestFit";
    public final static String SCALE_SHRINK_ONLY = "ImageViewer.ScaleShrinkOnly";

    private URL imageURL;
    private BufferedImage image;
    private String scaleMode = SCALE_SHRINK_ONLY;
    private float scale = 1;
    private Point offset = new Point(0, 0);
    private boolean enableMousePanAndZoom = true;
    private float zoomMultiplier = 1.05f;

    // Mouse tracking
    private Point mousePressed;

    // Image caching and transition
    private BufferedImage displayImage;
    private BufferedImage previousImage;
    private boolean fading = false;
    private int fadeTimeMillisec = 500;
    private long fadeStartTime;

    public ImageViewer()
    {
        this.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                mousePressed = e.getPoint();
            }

            public void mouseReleased(MouseEvent e)
            {
                mousePressed = null;
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseDragged(MouseEvent e)
            {
                if (image != null && enableMousePanAndZoom && mousePressed != null)
                {
                    offset.x += e.getPoint().x - mousePressed.x;
                    offset.y += e.getPoint().y - mousePressed.y;
                    mousePressed = e.getPoint();
                    clearCachedValues();
                    repaint();
                }
            }

        });

        this.addMouseWheelListener(new MouseWheelListener()
        {
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                if (image != null && enableMousePanAndZoom)
                {
                    float oldScale = scale;
                    int n = e.getUnitsToScroll();
                    for (int i = 1; i <= Math.abs(n); i++)
                        scale = n < 0 ? scale * zoomMultiplier : scale / zoomMultiplier;
                    updateOffsetForZoom(e.getPoint(), oldScale, scale);
                    clearCachedValues();
                    updateCursor();
                    repaint();
                }
            }
        });

    }

    public URL getImageURL()
    {
        return this.imageURL;
    }

    public boolean setImageURL(URL imageURL)
    {
        if (imageURL == null)
        {
            this.imageURL = null;
            this.image = null;
        }
        else
        {
            BufferedImage newImage = this.loadImage(imageURL);
            if (newImage != null)
            {
                this.imageURL = imageURL;
                this.image = newImage;
                this.startFading();
                this.resetPanAndZoom();
            }
            else
                return false;
        }
        this.repaint();
        return true;
    }

    private BufferedImage loadImage(URL imageURL)
    {
        try
        {
            return ImageIO.read(imageURL);
        }
        catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    public String getScaleMode()
    {
        return this.scaleMode;
    }

    public void setScaleMode(String scaleMode)
    {
        this.scaleMode = scaleMode;
        this.clearCachedValues();
        this.repaint();
    }

    public float getScale()
    {
        return this.scale;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
        this.clearCachedValues();
        this.repaint();
    }

    public Point getOffset()
    {
        return this.offset;
    }

    public void setOffset(Point offset)
    {
        this.offset = offset;
        this.clearCachedValues();
        this.repaint();
    }

    public BufferedImage getImage()
    {
        return this.image;
    }

    public void setImage(BufferedImage image)
    {
        this.imageURL = null;
        this.image = image;
        this.startFading();
        this.resetPanAndZoom();
    }

    public boolean isEnableMousePanAndZoom()
    {
        return this.enableMousePanAndZoom;
    }

    public void setEnableMousePanAndZoom(boolean state)
    {
        this.enableMousePanAndZoom = state;
    }

    public void resetPanAndZoom()
    {
        this.scale = 1f;
        this.offset.move(0, 0);
        this.clearCachedValues();
        this.repaint();
    }

    public float getZoomMultiplier()
    {
        return this.zoomMultiplier;
    }

    public void setZoomMultiplier(float multiplier)
    {
        this.zoomMultiplier = multiplier;
    }

    public boolean isFading()
    {
        return this.fading;
    }

    public int getFadeTimeMillisec()
    {
        return this.fadeTimeMillisec;
    }

    public void setFadeTimeMillisec(int millisec)
    {
        this.fadeTimeMillisec = millisec;
    }

    private void clearCachedValues()
    {
        this.displayImage = null;
    }

    private void startFading()
    {
        if (this.displayImage != null)
        {
            this.fading = true;
            this.previousImage = this.displayImage;
            this.fadeStartTime = System.currentTimeMillis();
        }
    }

    private void stopFading()
    {
        this.fading = false;
        this.previousImage = null;
    }

    public void paintComponent(Graphics g)
    {
        if (this.image == null)
            return;

        // Render display image if necessary
        if (this.displayImage == null || this.displayImage.getWidth() != this.getWidth()
            || this.displayImage.getHeight() != this.getHeight())
        {
            this.displayImage = (BufferedImage)createImage(this.getWidth(), this.getHeight());
            paintImage((Graphics2D) this.displayImage.getGraphics());
        }

        // Update component
        Graphics2D g2 = (Graphics2D)g;
        // If fading paint previous image first
        if (fading)
        {
            long elapsed = System.currentTimeMillis() - this.fadeStartTime;
            if (elapsed < this.fadeTimeMillisec)
            {
                g2.drawImage(this.previousImage, 0, 0, null);
                float alpha = (float)elapsed / this.fadeTimeMillisec;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }
            else
                this.stopFading();
        }
        // Paint display image
        g2.drawImage(this.displayImage, 0, 0, null);

        if (fading)
            this.repaint();
    }

    protected void paintImage(Graphics2D g2)
    {
        // Prepare to draw
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // Compute draw scale, draw offset and source image rectangle
        clampOffset();
        float drawScale = this.computeBestFitScale() * this.getScale();
        Point drawOffset = this.computeCenteringOffset(drawScale);
        drawOffset.translate(this.offset.x, this.offset.y);
        Rectangle r = computeSubImageRectangle(drawScale, drawOffset);
        // Draw
        g2.getTransform().setToIdentity();
        g2.transform(AffineTransform.getTranslateInstance(Math.max(0, drawOffset.x), Math.max(0, drawOffset.y)));
        g2.transform(AffineTransform.getScaleInstance(drawScale, drawScale));
        g2.drawImage(this.image.getSubimage(r.x,  r.y,  r.width,  r.height), 0, 0, null);
    }

    /**
     * Compute a scaling factor so that the image fits inside the component.
     *
     * @return the appropriate scaling factor.
     */
    protected float computeBestFitScale()
    {
        if (SCALE_FIXED.equals(this.scaleMode))
            return 1f;

        float scaleX = (float)this.getWidth() / (float)this.image.getWidth();
        float scaleY = (float)this.getHeight() / (float)this.image.getHeight();

        float scale = scaleX < scaleY ? scaleX : scaleY;
        if (SCALE_SHRINK_ONLY.equals(this.scaleMode))
            scale = Math.min(1f, scale);

        return scale;
    }

    /**
     * Compute offset so as to center the image inside the component.
     *
     * @param scale the current draw scale.
     * @return the appropriate offset <code>Point</code>.
     */
    protected Point computeCenteringOffset(float scale)
    {
        float dx = (this.getWidth() - (float)this.image.getWidth() * scale) / 2f;
        float dy = (this.getHeight() - (float)this.image.getHeight() * scale) / 2f;

        return new Point((int)dx, (int)dy);
    }

    /**
     * Make sure the image does not leave the component edges, except when it is smaller.
     */
    protected void clampOffset()
    {
        float drawScale = computeBestFitScale() * this.scale;
        float scaledWidth = this.image.getWidth() * drawScale;
        float scaledHeight = this.image.getHeight() * drawScale;
        Point centerOffset = computeCenteringOffset(drawScale);

        if (scaledWidth < this.getWidth())
            this.offset.x = 0;
        else if (this.offset.x + centerOffset.x > 0)
            this.offset.x = -centerOffset.x;
        else if (this.offset.x + centerOffset.x < this.getWidth() - scaledWidth)
            this.offset.x = this.getWidth() - (int)scaledWidth - centerOffset.x;

        if (scaledHeight < this.getHeight())
            this.offset.y = 0;
        else if (this.offset.y + centerOffset.y > 0)
            this.offset.y = -centerOffset.y;
        else if (this.offset.y + centerOffset.y < this.getHeight() - scaledHeight)
            this.offset.y = this.getHeight() - (int)scaledHeight - centerOffset.y;
    }

    /**
     * Adjust the current offset after a scale change so that the part of the image under the mouse doesn't move.
     * <p>
     * Note that the computeBestFit() scale combined with the computeCenteringOffset() results in the image
     * center not moving. Here we determine the mouse point relative position from the image center, scale it
     * and translate the current offset by the difference.
     *
     * @param mousePoint the mouse point relative to the component.
     * @param oldScale the scaling factor before the zoom.
     * @param newScale the scaling factor after the zoom.
     */
    protected void updateOffsetForZoom(Point mousePoint, float oldScale, float newScale)
    {
        float fitScale = computeBestFitScale();
        float oldDrawScale = fitScale * oldScale;
        float newDrawScale = fitScale * newScale;
        Point oldDrawOffset = computeCenteringOffset(oldDrawScale);
        oldDrawOffset.translate(this.offset.x, this.offset.y);
        Point newDrawOffset = computeCenteringOffset(newDrawScale);
        newDrawOffset.translate(this.offset.x, this.offset.y);
        float halfWidth = (float)this.image.getWidth() / 2f;
        float halfHeight = (float)this.image.getHeight() / 2f;
        // Determine mouse point position relative to image center
        Point oldCenter = new Point(oldDrawOffset.x + (int)(halfWidth * oldDrawScale),
            oldDrawOffset.y + (int)(halfHeight * oldDrawScale));
        Point oldMousePoint = new Point(mousePoint.x - oldCenter.x,  mousePoint.y - oldCenter.y);
        // Apply zoom to mouse point position
        Point newMousePoint = new Point((int)((float)oldMousePoint.x / oldDrawScale * newDrawScale),
            (int)((float)oldMousePoint.y / oldDrawScale * newDrawScale));
        // Translate offset of the difference
        this.offset.translate(oldMousePoint.x - newMousePoint.x,  oldMousePoint.y - newMousePoint.y);
    }

    /**
     * Change the cursor to 'move' when the image is larger then the component.
     */
    protected void updateCursor()
    {
        float drawScale = computeBestFitScale() * this.scale;
        float scaledWidth = this.image.getWidth() * drawScale;
        float scaledHeight = this.image.getHeight() * drawScale;

        if (!enableMousePanAndZoom || (scaledWidth <= this.getWidth() && scaledHeight <= this.getHeight()) )
            this.setCursor(Cursor.getDefaultCursor());
        else
            this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    /**
     * Compute the source image portion rectangle needed for display given the draw scale and the draw offset.
     *
     * @param drawScale the draw scale.
     * @param drawOffset the draw offset.
     * @return the source image portion rectangle needed for display.
     */
    private Rectangle computeSubImageRectangle(float drawScale, Point drawOffset)
    {
        int x = Math.max(0, (int)((float)-drawOffset.x / drawScale));
        int y = Math.max(0, (int)((float)-drawOffset.y / drawScale));
        int w = Math.min(image.getWidth(), (int)((float)this.getWidth() / drawScale) + 1);
        int h = Math.min(image.getHeight(), (int)((float)this.getHeight() / drawScale) + 1);

        return new Rectangle(x, y, w, h);
    }
}
