package gov.nasa.worldwind.examples.Hawaii;

import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.terrain.LocalElevationModel;

import java.io.IOException;

/**
 * @author tag
 * @version $Id: HawaiianIslandsElevationModel.java 9600 2009-03-22 20:04:40Z tgaskins $
 */
public class HawaiianIslandsElevationModel extends CompoundElevationModel
{
    private final static String[] files = new String[]
    {
//        "testData/Hawaii/Hawaii/elevations/hawaii-elevations-geo.bil",
//        "testData/Hawaii/Kahoolawe/elevations/kahoolawe-elevations-geo.bil",
        "testData/Hawaii/Kauai/elevations/kauai-elevations-geo.bil",
//        "testData/Hawaii/Lanai/elevations/lanai-elevations-geo.bil",
//        "testData/Hawaii/Maui/elevations/maui-elevations-geo.bil",
//        "testData/Hawaii/Molokai/elevations/molokai-elevations-geo.bil",
//        "testData/Hawaii/Niihau/elevations/niihau-elevations-geo.bil",
//        "testData/Hawaii/Oahu/elevations/oahu-elevations-geo.bil",
    };

    public HawaiianIslandsElevationModel()
    {
        for (String filePath : files)
        {
            LocalElevationModel em = new LocalElevationModel();
            em.setMissingDataSignal(-9999d);
            try
            {
                em.addElevations(filePath);
                this.addElevationModel(em);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
