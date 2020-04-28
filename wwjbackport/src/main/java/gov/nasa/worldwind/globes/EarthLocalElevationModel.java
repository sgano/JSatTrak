package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.terrain.*;

import java.io.*;

/**
 * @author tag
 * @version $Id: EarthLocalElevationModel.java 6195 2008-08-25 05:42:06Z tgaskins $
 */
public class EarthLocalElevationModel extends LocalElevationModel
{
    public EarthLocalElevationModel()
    {
        try
        {
            this.addElevations("testdata/kauai.bil",
            Sector.fromDegrees(21.865017, 22.243289, -159.793761, -159.290875), 5174, 4169);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
