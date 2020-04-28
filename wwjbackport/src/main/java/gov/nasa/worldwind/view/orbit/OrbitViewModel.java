/*
Copyright (C) 2001, 2007 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

/**
 * @author dcollins
 * @version $Id: OrbitViewModel.java 4100 2008-01-08 02:49:54Z dcollins $
 */
public interface OrbitViewModel
{
    public interface ModelCoordinates
    {
        Position getCenterPosition();
        
        Angle getHeading();

        Angle getPitch();

        double getZoom();
    }

    Matrix computeTransformMatrix(Globe globe, Position center, Angle heading, Angle pitch, double zoom);

    ModelCoordinates computeModelCoordinates(Globe globe, Vec4 eyePoint, Vec4 centerPoint, Vec4 up);

    ModelCoordinates computeModelCoordinates(Globe globe, Matrix modelview, Vec4 centerPoint);
}
