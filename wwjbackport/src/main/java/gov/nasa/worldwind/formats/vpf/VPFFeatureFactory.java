/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.vpf;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id: VPFFeatureFactory.java 12401 2009-07-31 14:01:06Z dcollins $
 */
public interface VPFFeatureFactory
{
    Collection<? extends VPFFeature> createPointFeatures(VPFFeatureClass featureClass);

    Collection<? extends VPFFeature> createLineFeatures(VPFFeatureClass featureClass);

    Collection<? extends VPFFeature> createAreaFeatures(VPFFeatureClass featureClass);

    Collection<? extends VPFFeature> createTextFeatures(VPFFeatureClass featureClass);

    Collection<? extends VPFFeature> createComplexFeatures(VPFFeatureClass featureClass);
}
