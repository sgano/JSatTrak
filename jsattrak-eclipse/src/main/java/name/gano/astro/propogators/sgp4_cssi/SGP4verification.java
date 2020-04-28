/*
 * =====================================================================
 *   This file is part of JSatTrak.
 *
 *   Copyright 2007-2013 Shawn E. Gano
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =====================================================================
 * Created June 2009
 */
// runs the Verification TLEs just like Vallado's C++ code does
//
// 19 June 2009 Results: only one digit different in the entire file: ( for OPSMODE_IMPROVED, wgs72)
//line 655 - only diff  x value of position:
// all error codes and times of errors where also the same
// value that was different (x pos line 655)
//cpp  = -23575.69186056
//java = -23575.69186057
//
// other combinations for the opsmode and gravity constants had similar results with very few
// differences in the cpp vs java output files

package name.gano.astro.propogators.sgp4_cssi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 *
 * @author Shawn E. Gano, shawn@gano.name
 */
public class SGP4verification
{
    public static void main(String[] args)
    {
        // settings
        char opsmode = SGP4utils.OPSMODE_IMPROVED; // OPSMODE_IMPROVED
        SGP4unit.Gravconsttype gravconsttype = SGP4unit.Gravconsttype.wgs72;
        
        // tle verification file (with extra start, stop, timestep params on line 2)
        String verTLEfile = "sgp4-ver.tle"; // sgp4-ver.tle shawn_ver.tle

        // output results to this file
        String javaResults = "java_sgp4_ver.out";

        // comparison file, cpp results
        String cppResultsFile = "tcppver.out";

        // internal variables -------------------------
        double[] ro = new double[3];
        double[] vo = new double[3];

        // get constants -------------------------------
        double[] gtt = SGP4unit.getgravconst(gravconsttype);//, tumin, mu, radiusearthkm, xke, j2, j3, j4, j3oj2 );
        double tumin = gtt[0];
        double mu = gtt[1];
        double radiusearthkm = gtt[2];
        double xke = gtt[3];
        double j2 = gtt[4];
        double j3 = gtt[5];
        double j4 = gtt[6];
        double j3oj2 = gtt[7];

         System.out.println("======  PROPOGATING VERIFICATION TLEs ====== ");
        // open the TLE file and propogate each TLE --------------------
        try
        {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(verTLEfile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            // output file
            // Create file
            FileWriter outStream = new FileWriter(javaResults);
            BufferedWriter out = new BufferedWriter(outStream);

            String strLine1;
            String strLine2;
            //Read File Line By Line
            while((strLine1 = br.readLine()) != null)
            {
                if(!strLine1.startsWith("#")) // ignore lines starting with #
                {
                    // there should always be a second line
                    strLine2 = br.readLine();

                    // retrive parts of the line that contain start/stop/timestep info
                    // split on white space after normal line 2 info
                    String[] sst = strLine2.substring(69).trim().split("\\s+");

                    double startmfe = Double.parseDouble(sst[0]);
                    double stopmfe =  Double.parseDouble(sst[1]);
                    double deltamin = Double.parseDouble(sst[2]);

                    // convert the char string to sgp4 elements
                    // includes initialization of sgp4
                    SGP4SatData satrec = new SGP4SatData();
                    SGP4utils.readTLEandIniSGP4("", strLine1, strLine2, opsmode, gravconsttype, satrec);

                    out.write(satrec.satnum + " xx\n");
                    System.out.println(" "+ satrec.satnum);
                    // call the propagator to get the initial state vector value
                    SGP4unit.sgp4(satrec, 0.0, ro, vo);

                    out.write(String.format(" %16.8f %16.8f %16.8f %16.8f %12.9f %12.9f %12.9f\n",
                        satrec.t,ro[0],ro[1],ro[2],vo[0],vo[1],vo[2]));

                    double tsince = startmfe;

                    // check so the first value isn't written twice
                    if ( Math.abs(tsince) > 1.0e-8 )
                    {
                        tsince = tsince - deltamin;
                    }

                    // ----------------- loop to perform the propagation ----------------
                    while((tsince < stopmfe) && (satrec.error == 0))
                    {
                        tsince = tsince + deltamin;

                        if(tsince > stopmfe)
                        {
                            tsince = stopmfe;
                        }

                        SGP4unit.sgp4(satrec, tsince, ro, vo);

                        if(satrec.error > 0)
                        {
                            System.out.print("# *** error: t:= " + satrec.t + " *** code = " + satrec.error + "\n");
                        }

                        if(satrec.error == 0)
                        {

                            //double jd = satrec.jdsatepoch + tsince/1440.0;
                            //invjday( jd, year,mon,day,hr,min, sec );

                            out.write(String.format(" %16.8f %16.8f %16.8f %16.8f %12.9f %12.9f %12.9f",
                                    tsince, ro[0], ro[1], ro[2], vo[0], vo[1], vo[2]));

                            double jd = satrec.jdsatepoch + tsince/1440.0;
                            double[] ymd = SGP4utils.invjday( jd ); //, year,mon,day,hr,min, sec );
                            int year = (int) ymd[0];
                            int mon = (int) ymd[1];
                            int day = (int) ymd[2];
                            int hr = (int) ymd[3];
                            int min = (int) ymd[4];
                            double sec = ymd[5];

                            double[] coe = SGP4utils.rv2coe(ro, vo, mu); // , p, a, ecc, incl, node, argp, nu, m, arglat, truelon, lonper);
                            double p = coe[0];
                            double a = coe[1];
                            double ecc = coe[2];
                            double incl = coe[3];
                            double node = coe[4];
                            double argp = coe[5];
                            double nu = coe[6];
                            double m = coe[7];
                            double arglat = coe[8];
                            double truelon = coe[9];
                            double lonper = coe[10];

                            double rad = 180.0 / Math.PI;

                            out.write( String.format(" %14.6f %8.6f %10.5f %10.5f %10.5f %10.5f %10.5f %5d%3d%3d %2d:%2d:%9.6f\n",
                                    a, ecc, incl * rad, node * rad, argp * rad, nu * rad,
                                    m * rad, year, mon, day, hr, min, sec));
                            out.flush(); // make sure the write is caught up

                        } // if satrec.error == 0

                    } // while propagating the orbit
                    
                } // ignore lines starting with #
            }
            //Close the input stream
            in.close();
            outStream.close();
        }
        catch(Exception e)
        {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        // -- end main prop loop -------------------------------------------


        System.out.println("======  RUNNNING COMPARISON ====== ");
        // now compare ------------------------------------------------
        try
        {
            // Open the results files
            FileInputStream fstream = new FileInputStream(javaResults);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader javaResultsBR = new BufferedReader(new InputStreamReader(in));

            FileInputStream fstream2 = new FileInputStream(cppResultsFile);
            DataInputStream in2 = new DataInputStream(fstream2);
            BufferedReader cppResultsBR = new BufferedReader(new InputStreamReader(in2));

            String cppLine = javaResultsBR.readLine();
            String javaLine = cppResultsBR.readLine();

            int line = 1;
            int lineMismatches = 0;
            do
            {
                if( !cppLine.equals(javaLine))
                {
                    lineMismatches++;

                    // line not equal
                    System.out.print("Line " + line + " doesn't match:  ");

                    // figure out how many chars are different
                    int charMismatch = 0;
                    System.out.print("pos=[");
                    try
                    {
                        for (int i = 0; i < cppLine.length(); i++)
                        {
                            if (cppLine.charAt(i) != javaLine.charAt(i))
                            {
                                charMismatch++;
                                System.out.print((i+1) + " ");
                            }
                        }

                    } catch (Exception e)
                    {
                        System.out.print("(Error checking line details) ");
                    }
                    System.out.print("] ");

                    //
                    System.out.print(charMismatch + " of " +cppLine.length() +  " mismatched characters\n");
                }

                cppLine = javaResultsBR.readLine();
                javaLine = cppResultsBR.readLine();
                line++;
            }while(cppLine != null && javaLine != null);

            System.out.println("---------------------");

            if(cppLine == null && javaLine == null)
            {
                System.out.println("** Files have the same number of lines **");
            }
            else
            {
                System.out.println("** Files have DIFFERENT number of lines **");
            }
            
            System.out.println("Total lines that don't match: " + lineMismatches);
            System.out.println("Total number of lines in shortest file: " + (line-1));

            in.close();
            in2.close();

        }
        catch(Exception e)
        {
            System.out.println("Error in comparing verification results:\n" + e.toString());
        }

    }
}
