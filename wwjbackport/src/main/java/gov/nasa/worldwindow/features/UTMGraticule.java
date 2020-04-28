/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.features;

import gov.nasa.worldwind.layers.Earth.UTMGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindow.core.*;

/**
 * @author tag
 * @version $Id: UTMGraticule.java 13266 2010-04-10 02:47:09Z tgaskins $
 */
public class UTMGraticule extends GraticuleLayer
{
    public UTMGraticule()
    {
        this(null);
    }

    public UTMGraticule(Registry registry)
    {
        super("UTM Graticule", Constants.FEATURE_UTM_GRATICULE, null, null, registry);
    }

    @Override
    protected Layer doCreateLayer()
    {
        return new UTMGraticuleLayer();
    }
}
