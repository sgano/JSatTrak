/*
Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id$
 */

public abstract class AbstractMetadataMapper implements MetadataMapper
{
    public void map(AVList fromParams, AVList toParams)
    {
        if (null == fromParams)
        {
            String message = Logging.getMessage("nullValue.AVListIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        if (null == toParams)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        this.doMapParams(fromParams, toParams);
    }

    protected abstract void doMapParams(AVList fromParams, AVList toParams);
}
