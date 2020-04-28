/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.render.DrawContext;

/**
 * Represents the KML <i>ScreenOverlay</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLScreenOverlay.java 13712 2010-09-04 00:01:04Z pabercrombie $
 */
public class KMLScreenOverlay extends KMLAbstractOverlay
{
    protected KMLRenderable renderable;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLScreenOverlay(String namespaceURI)
    {
        super(namespaceURI);
    }

    public KMLVec2 getOverlayXY()
    {
        return (KMLVec2) this.getField("overlayXY");
    }

    public KMLVec2 getScreenXY()
    {
        return (KMLVec2) this.getField("screenXY");
    }

    public KMLVec2 getRotationXY()
    {
        return (KMLVec2) this.getField("rotationXY");
    }

    public KMLVec2 getSize()
    {
        return (KMLVec2) this.getField("size");
    }

    public Double getRotation()
    {
        return (Double) this.getField("rotation");
    }

    @Override
    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getRenderable() == null)
            this.initializeRenderable(tc, dc);

        KMLRenderable r = this.getRenderable();
        if (r != null)
        {
            r.preRender(tc, dc);
        }
    }

    @Override
    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        // We've already initialized the screen image renderable during the preRender pass. Render the screen image
        // without any further preparation.

        KMLRenderable r = this.getRenderable();
        if (r != null)
        {
            r.render(tc, dc);
        }
    }

    /**
     * Create the renderable that will represent the overlay.
     */
    protected void initializeRenderable(KMLTraversalContext tc, DrawContext dc)
    {
        renderable = new KMLScreenImageImpl(tc, this);
    }

    /**
     * Get the renderable that represents the screen overlay. The renderable is created the first time that the overlay
     * is rendered. Until then, the method will return null.
     *
     * @return The renderable, or null if the renderable has not been created yet.
     */
    public KMLRenderable getRenderable()
    {
        return renderable;
    }
}
