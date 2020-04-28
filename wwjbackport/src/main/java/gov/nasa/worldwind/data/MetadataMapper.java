/*
Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author Lado Garakanidze
 * @version $Id$
 */

public interface MetadataMapper
{
    public void map(AVList fromParams, AVList toParams);
}
