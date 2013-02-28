/*
 * StkEphemerisReader.java
 * 
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 * Created: 10 Jul 2009
 */

package name.gano.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;
import jsattrak.utilities.StateVector;
import name.gano.astro.AstroConst;
import name.gano.astro.time.Time;

/**
 *
 * @author Shawn E. Gano, shawn@gano.name
 */
public class StkEphemerisReader
{

    private String stkVer = "";
    private String centralBody = "";
    private String coordSys = "";
    private String scenarioEpoch = "";
    private String filename = "";

    /**
     *
     */
    public StkEphemerisReader()
    {

    }

    // test main
    public static void main(String[] args)
    {
        String file = "GPS_BI-04.e";
        StkEphemerisReader r = new StkEphemerisReader();
        try
        {
            Vector<StateVector> e = r.readStkEphemeris(file);
             System.out.println( "Ephemeris Points Read: " + e.size() );
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e.toString());
        }

        r.printInfo();

    }

    /**
     * Reads in an STK .e formated epehermis file NOTE that time is returned in Terestrial time not UTC!! As epeheris data is typically stored in TT
     * @param filename
     * @return epehermis vector Julian Data, x,y,z, dx, dy, dz (meters, m/s) - can be null if file couldn't be read at all
     * @throws Exception error in reading file
     */
    public Vector<StateVector> readStkEphemeris(String filename) throws Exception
    {
        // clean up data
        stkVer = "";
        centralBody = "";
        coordSys = "";
        scenarioEpoch = "";
        this.filename = filename;

       Vector<StateVector> ephemeris  = new Vector<StateVector>(50,50); // ini size, increment

        try
        {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(filename));
            try
            {
                String line = null; //not declared within while loop
        /*
                 * readLine is a bit quirky :
                 * it returns the content of a line MINUS the newline.
                 * it returns null only for the END of the stream.
                 * it returns an empty String if two newlines appear in a row.
                 */
                

                boolean ephemerisBegin = false; // flag for when the ephemeris is about to begin

                while((line = input.readLine()) != null && !ephemerisBegin)
                {
                    //if(line.startsWith("EphemerisTimePosVel"))
                    
                    // from slnovak (gitHub):
                    // Remove leading and trailing spaces from line since some
                    // STK ephem implementations have extra white space.
                    // Additionally, the data header can be other strings, such
                    // as "EphemerisEciTimePosVel". Check for just the
                    // "Ephemeris" portion of the text.
                    line = line.trim();
                    if(line.startsWith("Ephemeris"))
                    {
                        ephemerisBegin = true;
                        break; // need to break out of loop without reading another line in the while statement
                    }
                    else if(line.startsWith("stk.v"))
                    {
                        stkVer = line.trim();
                    }
                    else if(line.startsWith("CentralBody"))
                    {
                        centralBody = line.substring(11).trim();
                    }
                    else if(line.startsWith("CoordinateSystem"))
                    {
                        coordSys = line.substring(16).trim();
                    }
                    else if(line.startsWith("ScenarioEpoch"))
                    {
                        scenarioEpoch = line.substring(13).trim();
                    }

                } // while reading file

                // convert ScenarioEpoch to Julian Date -- if not read in throw an Exception - the method throws exception
                // format: 1 Jul 2007 12:00:00.00    (implied UTC)
                double jdStart =  StkEphemerisReader.convertScenarioTimeString2JulianDate(scenarioEpoch + " UTC");

                // read ephemeris
                while((line = input.readLine()) != null && ephemerisBegin)
                {
                    if(line.length() < 1)
                    {
                        // no data on this line, ignore
                    }
                    else if(line.startsWith("END"))
                    {
                        ephemerisBegin = false; // no more data
                    }
                    else //so far seems ok lets parse the line and see if it is valid
                    {
                        String[] data = line.split(" ");
                        if(data.length  == 7) // data line has enough data points
                        {
                            // convert data to doubles and save to ephem vector
                            double[] state = new double[7];

                            // UTC time (as STK file has UTC time in the file
                            state[0] = jdStart + Double.parseDouble(data[0])/(86400.0); // JD // can throw exception
                            // convert to TT time
                            state[0] = state[0] + Time.deltaT(state[0] - AstroConst.JDminusMJD);

                            for(int i=1;i<7;i++)
                            {
                                state[i] = Double.parseDouble(data[i]); // can throw exception
                            }

                            ephemeris.add(new StateVector(state));

                        } // enough points
                    }

                } // while reading file

            }finally
            {
                input.close(); // always close file even if there is an exception
            }
        }
        catch(IOException ex)
        {
            throw ex;
        }

        return ephemeris;

    } //readStkEphemeris

    /**
     * Prints info about last read ephemeris
     */
    public void printInfo()
    {
         System.out.println("stkVer = "+stkVer);
         System.out.println("centralBody = "+centralBody);
         System.out.println("coordSys = "+coordSys);
         System.out.println("scenarioEpoch = "+scenarioEpoch);
         System.out.println("filename = "+filename);
    }


    /**
     * converts a time string to a Julian Date -- format string need to be somthing like: dd MMM y H:m:s.S z
     * @param scenarioTimeStr string with the date
     * @return julian date
     * @throws Exception if format is not correct
     */
    public static double convertScenarioTimeString2JulianDate(String scenarioTimeStr) throws Exception
    {
        GregorianCalendar currentTimeDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat dateformatShort1 = new SimpleDateFormat("dd MMM y H:m:s.S z");
        SimpleDateFormat dateformatShort2 = new SimpleDateFormat("dd MMM y H:m:s z"); // no Milliseconds

        try
        {
            currentTimeDate.setTime( dateformatShort1.parse(scenarioTimeStr) );
        }
        catch(Exception e2)
        {
            try
            {
                // try reading without the milliseconds
                currentTimeDate.setTime( dateformatShort2.parse(scenarioTimeStr) );
            }
            catch(Exception e3)
            {
                // bad date input
                throw new Exception("Scenario Date/Time format incorrect");
            } // catch 2

        } // catch 1

        // if we get here the date was acapted
        Time t = new Time();
        t.set(currentTimeDate.getTimeInMillis());

        return t.getJulianDate();
        
    } // convertScenarioTimeString2JulianDate

    /**
     * @return the stkVer
     */
    public String getStkVer()
    {
        return stkVer;
    }

    /**
     * @return the centralBody
     */
    public String getCentralBody()
    {
        return centralBody;
    }

    /**
     * @return the coordSys
     */
    public String getCoordSys()
    {
        return coordSys;
    }

    /**
     * @return the scenarioEpoch
     */
    public String getScenarioEpoch()
    {
        return scenarioEpoch;
    }

    /**
     * @return the filename
     */
    public String getFilename()
    {
        return filename;
    }

} // StkEphemerisReader
