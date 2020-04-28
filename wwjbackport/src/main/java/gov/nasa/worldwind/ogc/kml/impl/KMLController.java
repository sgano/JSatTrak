/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.render.*;

/**
 * Executes the mapping from KML to World Wind. Traverses a parsed KML document and creates the appropriate World Wind
 * object to represent the KML.
 *
 * @author tag
 * @version $Id: KMLController.java 13707 2010-09-03 18:53:50Z pabercrombie $
 */
public class KMLController implements PreRenderable, Renderable
{
    protected KMLRoot kmlRoot;
    protected KMLTraversalContext tc;

    public KMLController(KMLRoot root)
    {
        this.setKmlRoot(root);
        this.setTraversalContext(new KMLTraversalContext());
    }

    public KMLRoot getKmlRoot()
    {
        return this.kmlRoot;
    }

    public void setKmlRoot(KMLRoot kmlRoot)
    {
        this.kmlRoot = kmlRoot;
    }

    public void setTraversalContext(KMLTraversalContext tc)
    {
        this.tc = tc;
    }

    public KMLTraversalContext getTraversalContext()
    {
        return this.tc;
    }

    public void preRender(DrawContext dc)
    {
        this.kmlRoot.preRender(this.getTraversalContext(), dc);
    }

    public void render(DrawContext dc)
    {
        this.kmlRoot.render(this.getTraversalContext(), dc);
    }
}
