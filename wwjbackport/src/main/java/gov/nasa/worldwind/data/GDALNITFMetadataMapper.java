/*
Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.util.*;

/**
 * @author Lado Garakanidze
 * @version $Id$
 */

public class GDALNITFMetadataMapper extends AbstractMetadataMapper
{
    protected static final String NITF_ONAME = "NITF_ONAME";
    protected static final String NITF_ISORCE = "NITF_ISORCE";
    protected static final String NITF_IREP = "NITF_IREP";
    protected static final String NITF_ABPP = "NITF_ABPP";
    protected static final String NITF_FBKGC = "NITF_FBKGC";

    protected static final String NITF_DYNAMIC_RANGE = "NITF_USE00A_DYNAMIC_RANGE";

    @Override
    protected void doMapParams(AVList extParams, AVList params)
    {
        if (extParams.hasKey(NITF_ONAME))
        {
            // values: GeoEye, DigitalGlobe
        }

        if (extParams.hasKey(NITF_ISORCE))
        {
            // values: GEOEYE1,DigitalGlobe
        }

        if (extParams.hasKey(NITF_IREP))
        {
            // values: RGB/LUT/MONO/MULTI
        }

        // Extract Actual Bit-Per-Pixel
        if (extParams.hasKey(NITF_ABPP))
        {
            Object o = extParams.getValue(NITF_ABPP);
            if (!WWUtil.isEmpty(o) && o instanceof String)
            {
                Integer abpp = WWUtil.convertStringToInteger((String) o);
                if (null != abpp)
                    params.setValue(AVKey.RASTER_BAND_ACTUAL_BITS_PER_PIXEL, abpp);
            }
        }

        if (extParams.hasKey(NITF_DYNAMIC_RANGE))
        {
            Object o = extParams.getValue(NITF_DYNAMIC_RANGE);
            if (!WWUtil.isEmpty(o) && o instanceof String)
            {
                Double maxPixelValue = WWUtil.convertStringToDouble((String) o);
                if (null != maxPixelValue)
                    params.setValue(AVKey.RASTER_BAND_MAX_PIXEL_VALUE, maxPixelValue);
            }
        }

        if (extParams.hasKey(NITF_FBKGC))
        {
            Object o = extParams.getValue(NITF_FBKGC);
            if (!WWUtil.isEmpty(o) && o instanceof String)
            {
                try
                {
                    String[] s = ((String) o).split(",");
                    if (null != s)
                    {
//                        if( s.length == 3 )
//                        {
//                            Color bgc = new Color( Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]), 0xFF );
//                            long color = 0xFFFFFFFFL & bgc.getRGB();
//                            params.setValue(AVKey.MISSING_DATA_SIGNAL, (double)color );
//                        }
//                        else if( s.length == 1 )
//                        {
//                            int color = Integer.parseInt(s[0]);
//                            params.setValue(AVKey.MISSING_DATA_SIGNAL, (double)color );
//                        }
                    }
                }
                catch (Exception e)
                {
                    String msg = Logging.getMessage("generic.CannotCreateColor", o);
                    Logging.logger().severe(msg);
                }
            }
        }
    }
}
