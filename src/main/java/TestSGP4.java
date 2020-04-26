// Test use of SGP4 objects -- requested by:
//Bradley Crosby Jr.
//SPAWAR Atlantic 5.3.3.4
//Office: 843-218-4343
//Mobile: 843-822-5177
//bradley.crosby@navy.mil
//crosbyb@spawar-chas.navy.smil.mil


import jsattrak.objects.SatelliteTleSGP4;
import jsattrak.utilities.TLE;


/**
 *
 * @author Shawn Gano, 9 June 2009
 */
public class TestSGP4
{
    public static void main(String[] args)
    {
        // create TLE object
        // TLE = name, line 1, line 2
        TLE newTLE = new TLE("ISS","1 25544U 98067A   09160.12255947  .00017740  00000-0  12823-3 0    24","2 25544  51.6405 348.2892 0009223  92.2562   9.3141 15.73542580604683");

        // Julian Date we are interested in
        double julianDate = 2454992.0; // 09 Jun 2009 12:00:00.000 UTC

        // Create SGP4 satelite propogator
        SatelliteTleSGP4 prop = null;
        try
        {
            prop = new SatelliteTleSGP4(newTLE.getSatName(), newTLE.getLine1(), newTLE.getLine2());
            prop.setShowGroundTrack(false); // if we arn't using the JSatTrak plots midas well turn this off to save CPU time
        }
        catch(Exception e)
        {
            System.out.println("Error Creating SGP4 Satellite");
            System.exit(1);
        }

        // prop to the desired time
        prop.propogate2JulDate(julianDate);

        // get the lat/long/altitude [radians, radians, meters]
        double[] lla = prop.getLLA();

        System.out.println("Lat [deg]:" + lla[0]*180.0/Math.PI);
        System.out.println("Lon [deg]:" + lla[1]*180.0/Math.PI);
        System.out.println("Alt [m]  :" + lla[2]); 

    } // main
}
