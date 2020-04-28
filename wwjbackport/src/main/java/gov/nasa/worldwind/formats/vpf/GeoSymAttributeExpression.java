/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: GeoSymAttributeExpression.java 12413 2009-08-05 18:43:56Z dcollins $
 */
public interface GeoSymAttributeExpression
{
    boolean evaluate(AVList featureAttributes);
}
