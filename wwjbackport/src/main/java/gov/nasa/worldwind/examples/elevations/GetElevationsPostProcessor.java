/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples.elevations;

import gov.nasa.worldwind.geom.Position;

/**
 * @author Lado Garakanidze
 * @version $Id: GetElevationsPostProcessor.java 12715 2009-10-13 18:33:19Z garakl $
 */
public interface GetElevationsPostProcessor
{
    public void onSuccess( Position[] positions );

    public void onError( String error );
}
