/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.ogc.kml.gx.GXLatLongQuad;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

/**
 * Represents the KML <i>GroundOverlay</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLGroundOverlay.java 13844 2010-09-20 22:37:43Z pabercrombie $
 */
public class KMLGroundOverlay extends KMLAbstractOverlay implements KMLRenderable
{
    protected KMLRenderable renderable;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLGroundOverlay(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getAltitude()
    {
        return (Double) this.getField("altitude");
    }

    public String getAltitudeMode()
    {
        return (String) this.getField("altitudeMode");
    }

    public KMLLatLonBox getLatLonBox()
    {
        return (KMLLatLonBox) this.getField("LatLonBox");
    }

    public GXLatLongQuad getLatLonQuad()
    {
        return (GXLatLongQuad) this.getField("LatLonQuad");
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
        // We've already initialized the image renderable during the preRender pass. Render the image
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
        final String altitudeMode = this.getAltitudeMode();
        if ("absolute".equals(altitudeMode))
            renderable = new KMLGroundOverlayPolygonImpl(tc, this);
        else // Default to clampToGround 
            renderable = new KMLSurfaceImageImpl(tc, this);
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

