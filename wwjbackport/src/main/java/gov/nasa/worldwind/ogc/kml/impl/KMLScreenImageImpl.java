/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;

/**
 * Implements at KML <i>ScreenOverlay</i> element.
 *
 * @author pabercrombie
 * @version $Id $
 */
public class KMLScreenImageImpl extends ScreenImage implements KMLRenderable
{
    /** Size value that KML uses to indicate that the native image dimension should be maintained. */
    protected static final int KML_NATIVE_DIMENSION = -1;

    /** Size value that KML uses to indicate that the image aspect ration should be maintained. */
    protected static final int KML_MAINTAIN_ASPECT_RATIO = 0;

    /**
     * Tolerance used in floating point comparisons to determine if the size parameter from the KML file indicates an
     * actual image size of a special value like {@link #KML_NATIVE_DIMENSION} or {@link #KML_MAINTAIN_ASPECT_RATIO}.
     */
    protected double tolerance = 0.0001d;

    protected final KMLScreenOverlay parent;

    /**
     * Create an screen image.
     *
     * @param tc      the current {@link KMLTraversalContext}.
     * @param overlay the <i>Overlay</i> element containing.
     *
     * @throws NullPointerException     if the traversal context is null.
     * @throws IllegalArgumentException if the parent overlay or the traversal context is null.
     */
    public KMLScreenImageImpl(KMLTraversalContext tc, KMLScreenOverlay overlay)
    {
        this.parent = overlay;

        if (tc == null)
        {
            String msg = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (overlay == null)
        {
            String msg = Logging.getMessage("nullValue.ParentIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        KMLVec2 xy = this.parent.getScreenXY();
        if (xy != null)
        {
            this.screenOffset = new Offset(xy.getX(), xy.getY(), KMLUtil.kmlUnitsToWWUnits(xy.getXunits()),
                KMLUtil.kmlUnitsToWWUnits(xy.getYunits()));
        }

        xy = this.parent.getOverlayXY();
        if (xy != null)
        {
            this.imageOffset = new Offset(xy.getX(), xy.getY(), KMLUtil.kmlUnitsToWWUnits(xy.getXunits()),
                KMLUtil.kmlUnitsToWWUnits(xy.getYunits()));
        }

        this.setRotation(overlay.getRotation());

        xy = this.parent.getRotationXY();
        if (xy != null)
        {
            setRotationOffset(new Offset(xy.getX(), xy.getY(), KMLUtil.kmlUnitsToWWUnits(xy.getXunits()),
                KMLUtil.kmlUnitsToWWUnits(xy.getYunits())));
        }

        String colorStr = overlay.getColor();
        if (colorStr != null)
        {
            Color color = WWUtil.decodeColorABGR(colorStr);
            this.setColor(color);
        }

        // Compute desired image size, and the scale factor that will make it that size
        KMLVec2 kmlSize = this.parent.getSize();
        if (kmlSize != null)
        {
            Size size = new Size();
            size.setWidth(getSizeMode(kmlSize.getX()), kmlSize.getX(), KMLUtil.kmlUnitsToWWUnits(kmlSize.getXunits()));
            size.setHeight(getSizeMode(kmlSize.getY()), kmlSize.getY(), KMLUtil.kmlUnitsToWWUnits(kmlSize.getYunits()));
            this.setSize(size);
        }
    }

    /** {@inheritDoc} */
    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        // No pre-rendering
    }

    protected boolean mustResolveHref()
    {
        return this.getImageSource() == null
            && this.parent.getIcon() != null
            && this.parent.getIcon().getHref() != null;
    }

    /** {@inheritDoc} */
    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.mustResolveHref()) // resolve the href to either a local file or a remote URL
        {
            // The icon reference may be to a support file within a KMZ file, so check for that. If it's not, then just
            // let the normal ScreenImage code resolve the reference.
            String href = this.parent.getIcon().getHref();
            String localAddress = (String) this.parent.getRoot().resolveLocalReference(href, null);

            this.setImageSource((localAddress != null ? localAddress : href));
        }

        this.render(dc);
    }

    /**
     * Applies a transform to the DrawContext that maps the image's origin to its screen location. The screen coordinate
     * system is consistent with the KML specification: the origin is in the lower left corner, with the X axis pointing
     * right and the Y axis pointing up.
     *
     * @param dc the DrawContext the transform is applied to.
     */
    @Override
    protected void applyScreenLocationTransform(DrawContext dc)
    {
        // KML and OpenGL share the same screen coordinate system, so this interprets the screen location as OpenGL 
        // coordinates, then adds the image offset. The dx and dy parameters are the negative of the image offset X and
        // Y. Therefore we add dx and dy to the screen location instead of subtracted them.
        dc.getGL().glTranslated(this.screenLocation.x + this.dx, this.screenLocation.y + this.dy, 0d);
    }

    /**
     * Get the size mode for a size parameter. The KML size tag takes a numeric size attribute, but certain values of
     * this attribute change the interpretation of the tag.
     * <p/>
     * <ul> <li> A value of -1 indicates to use the native dimension</li>. <li> A value of 0 indicates to maintain the
     * aspect ratio</li>. <li> A value of n sets the value of the dimension</li>. </ul>
     * <p/>
     * The comparison is a floating point comparison to a tolerance of {@link #tolerance}. So a size attribute of
     * 0.000000000001 is considered equal 0, meaning that the image aspect ratio should be maintained.
     *
     * @param size The KML size attribute
     *
     * @return One of {@link gov.nasa.worldwind.render.Size#NATIVE_DIMENSION}, {@link gov.nasa.worldwind.render.Size#MAINTAIN_ASPECT_RATIO},
     *         or {@link gov.nasa.worldwind.render.Size#EXPLICIT_DIMENSION}.
     */
    protected int getSizeMode(double size)
    {
        if (Math.abs(size - KML_NATIVE_DIMENSION) < tolerance)
            return Size.NATIVE_DIMENSION;
        else if (Math.abs(size - KML_MAINTAIN_ASPECT_RATIO) < tolerance)
            return Size.MAINTAIN_ASPECT_RATIO;
        else
            return Size.EXPLICIT_DIMENSION;
    }
}
