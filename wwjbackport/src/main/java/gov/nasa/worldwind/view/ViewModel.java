/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;

/**
 * @author jym
 * @version $Id: ViewModel.java 12632 2009-09-22 02:33:48Z jterhorst $
 */
public interface ViewModel
{
    Position getPosition();

    Angle getHeading();

    Angle getPitch();

    Angle getRoll();

    Matrix computeTransformMatrix(Globe globe, Position position, Angle heading, Angle pitch, Angle roll);
}
