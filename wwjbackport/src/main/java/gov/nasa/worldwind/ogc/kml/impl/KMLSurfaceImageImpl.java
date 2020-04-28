/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.ogc.kml.gx.GXLatLongQuad;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

/**
 * @author pabercrombie
 * @version $Id: KMLSurfaceImageImpl.java 13844 2010-09-20 22:37:43Z pabercrombie $
 */
// TODO: rotation
public class KMLSurfaceImageImpl extends SurfaceImage implements KMLRenderable
{
    protected KMLGroundOverlay parent;

    /**
     * Create an screen image.
     *
     * @param tc      the current {@link KMLTraversalContext}.
     * @param overlay the <i>Overlay</i> element containing.
     *
     * @throws NullPointerException     if the traversal context is null.
     * @throws IllegalArgumentException if the parent overlay or the traversal context is null.
     */
    public KMLSurfaceImageImpl(KMLTraversalContext tc, KMLGroundOverlay overlay)
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

        // Positions are specified either as a kml:LatLonBox or a gx:LatLonQuad
        KMLLatLonBox box = overlay.getLatLonBox();
        if (box != null)
        {
            Sector sector = KMLUtil.createSectorFromLatLonBox(box);
            this.initializeGeometry(sector);

            // TODO: rotation
        }
        else
        {
            GXLatLongQuad latLonQuad = overlay.getLatLonQuad();
            if (latLonQuad != null && latLonQuad.getCoordinates() != null)
            {
                this.initializeGeometry(latLonQuad.getCoordinates().list);
            }
        }

        this.setPickEnabled(false);
    }

    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.mustResolveHref()) // resolve the href to either a local file or a remote URL
        {
            // The icon reference may be to a support file within a KMZ file, so check for that. If it's not, then just
            // let the normal SurfaceImage code resolve the reference.
            String href = this.parent.getIcon().getHref();
            String localAddress = (String) this.parent.getRoot().resolveLocalReference(href, null);

            this.setImageSource((localAddress != null ? localAddress : href), this.getCorners());
        }

        super.preRender(dc);
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
        super.render(dc);
    }
}
