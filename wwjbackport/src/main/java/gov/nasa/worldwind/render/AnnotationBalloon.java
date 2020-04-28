/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;

import java.awt.*;
import java.awt.geom.*;

/**
 * Implementation of balloon using {@link Annotation}.
 *
 * @author pabercrombie
 * @version $Id: AnnotationBalloon.java 14159 2010-11-29 21:50:05Z pabercrombie $
 */
public class AnnotationBalloon extends AbstractBalloon
{
    /**
     * Annotation used to render the balloon, either a {@link ScreenAnnotation} or a {@link GlobeAnnotation}.
     */
    protected Annotation annotation;

    /**
     * Create a new annotation balloon attached to the globe.
     *
     * @param text     Balloon text. May not be null.
     * @param position Balloon's position.
     */
    public AnnotationBalloon(String text, Position position)
    {
        super(text, position);
        this.annotation = this.createAnnotation();
    }

    /**
     * Create a new annotation balloon attached to the screen.
     *
     * @param text  Balloon text. May not be null.
     * @param point Balloon's position on the screen.
     */
    public AnnotationBalloon(String text, Point point)
    {
        super(text, point);
        this.annotation = this.createAnnotation();
    }

    /** {@inheritDoc} */
    public Rectangle getBounds(DrawContext dc)
    {
        return this.annotation.getBounds(dc);
    }

    /**
     * Create an annotation to render the balloon.
     *
     * @return The new annotation.
     */
    protected Annotation createAnnotation()
    {
        Annotation annotation;
        if (this.globePosition != null)
            annotation = new GlobeAnnotation(this.getDecodedText(), this.globePosition);
        else
            annotation = new ScreenAnnotation(this.getDecodedText(), this.screenPoint);

        // Don't make the balloon bigger when it is highlighted, the text looks blurry when it is scaled up.
        annotation.getAttributes().setHighlightScale(1);

        return annotation;
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        if (!this.isVisible())
            return;

        this.determineActiveAttributes();
        this.applyAttributesToAnnotation();

        // Set position
        if (this.annotation instanceof GlobeAnnotation)
            this.computeGlobePosition((GlobeAnnotation) this.annotation, dc);
        else if (this.annotation instanceof ScreenAnnotation)
            this.computeScreenPosition((ScreenAnnotation) this.annotation, dc);

        this.computeOffsets(dc);
        this.annotation.render(dc);
    }

    /**
     * Compute the annotation position in globe attachment mode and set it in the annotation.
     *
     * @param annotation Annotation to be rendered.
     * @param dc         Draw context.
     */
    protected void computeGlobePosition(GlobeAnnotation annotation, DrawContext dc)
    {
        annotation.setPosition(this.globePosition);
        annotation.setAltitudeMode(this.getAltitudeMode());
    }

    /**
     * Compute the annotation position in screen attachment mode and set it in the annotation.
     *
     * @param annotation Annotation to be rendered.
     * @param dc         Draw context.
     */
    protected void computeScreenPosition(ScreenAnnotation annotation, DrawContext dc)
    {
        Rectangle viewport = dc.getView().getViewport();

        int y = (int) viewport.getHeight() - this.screenPoint.y - 1;
        annotation.setScreenPoint(new Point(this.screenPoint.x, y));
    }

    /** Apply the balloon attributes to the annotation. */
    protected void applyAttributesToAnnotation()
    {
        Object delegateOwner = this.getDelegateOwner();
        this.annotation.setDelegateOwner(delegateOwner != null ? delegateOwner : this);

        this.annotation.setAlwaysOnTop(this.isAlwaysOnTop());
        this.annotation.setPickEnabled(this.isPickEnabled());

        String text = this.getDecodedText();
        if (text != null)
            this.annotation.setText(text);

        this.annotation.setMinActiveAltitude(this.getMinActiveAltitude());
        this.annotation.setMaxActiveAltitude(this.getMaxActiveAltitude());

        AnnotationAttributes annotationAttrs = this.annotation.getAttributes();

        annotationAttrs.setHighlighted(this.isHighlighted());
        annotationAttrs.setVisible(this.isVisible());

        BalloonAttributes balloonAttrs = this.getActiveAttributes();

        if (balloonAttrs != null)
        {
            annotationAttrs.setTextColor(balloonAttrs.getTextColor());
            annotationAttrs.setBackgroundColor(balloonAttrs.getBackgroundColor());
            annotationAttrs.setBorderColor(balloonAttrs.getBorderColor());
            annotationAttrs.setBorderWidth(balloonAttrs.getBorderWidth());
            annotationAttrs.setCornerRadius(balloonAttrs.getCornerRadius());
            annotationAttrs.setLeader(balloonAttrs.getLeader());
            annotationAttrs.setLeaderGapWidth(balloonAttrs.getLeaderWidth());
            annotationAttrs.setFont(balloonAttrs.getFont());
            annotationAttrs.setFrameShape(balloonAttrs.getBalloonShape());
            annotationAttrs.setInsets(balloonAttrs.getInsets());

            annotationAttrs.setImageSource(balloonAttrs.getImageSource());
            annotationAttrs.setImageOffset(balloonAttrs.getImageOffset());
            annotationAttrs.setImageOpacity(balloonAttrs.getImageOpacity());
            annotationAttrs.setImageRepeat(balloonAttrs.getImageRepeat());
            annotationAttrs.setImageScale(balloonAttrs.getImageScale());
        }

        annotation.setAttributes(annotationAttrs);
    }

    /**
     * Compute the position and offsets, and set in them in the annotation.
     *
     * @param dc DrawContext in which the balloon is being rendered.
     */
    protected void computeOffsets(DrawContext dc)
    {
        Rectangle viewport = dc.getView().getViewport();

        BalloonAttributes balloonAttrs = this.getActiveAttributes();
        AnnotationAttributes annotationAttrs = this.annotation.getAttributes();

        if (balloonAttrs != null)
        {
            // Compute draw offset
            Offset offset = balloonAttrs.getDrawOffset();
            if (offset != null)
            {
                Point2D.Double offsetPoint = offset.computeOffset(viewport.getWidth(), viewport.getHeight(), 1.0, 1.0);
                annotationAttrs.setDrawOffset(new Point((int) offsetPoint.x, (int) offsetPoint.y));
            }

            // Compute preferred size
            Rectangle bounds = this.getBounds(dc);
            Size size = balloonAttrs.getSize();
            if (size != null)
            {
                Dimension preferredSize = size.compute(bounds.width, bounds.height, viewport.width, viewport.height);
                annotationAttrs.setSize(preferredSize);
            }
        }
    }
}
