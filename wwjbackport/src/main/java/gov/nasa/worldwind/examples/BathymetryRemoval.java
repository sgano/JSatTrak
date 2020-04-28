/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.BathymetryFilterElevationModel;

/**
 * Illustrates how to suppress an elevation model's bathymetry.
 *
 * @author tag
 * @version $Id: BathymetryRemoval.java 13114 2010-02-10 06:22:19Z tgaskins $
 */
public class BathymetryRemoval extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Get the current elevation model.
            ElevationModel currentElevationModel = this.getWwd().getModel().getGlobe().getElevationModel();

            // Wrap it with the no-bathymetry elevation model.
            BathymetryFilterElevationModel noDepthModel = new BathymetryFilterElevationModel(currentElevationModel);

            // Have the globe use the no-bathymetry elevation model.
            this.getWwd().getModel().getGlobe().setElevationModel(noDepthModel);

            // Increase vertical exaggeration to make it clear that bathymetry is suppressed.
            this.getWwd().getSceneController().setVerticalExaggeration(5d);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Bathymetry Removal", AppFrame.class);
    }
}
