/**
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
 */
// package to download all new TLE files from web
// second version, updates progress via input gui items
// 22 march 2009 - 3rd version remove gui imputs and moved for loop out of downloadTLEs, so GUI could be updated properly - thread safely


package jsattrak.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class TLEDownloader implements java.io.Serializable
{
	// root URL for all TLEs
	String rootWeb = "http://celestrak.com/NORAD/elements/";
	
	// names of all TLE files to update
	public String[] fileNames = new String[] {
            // --- Special-Interest Satellites -----
            //"sts.txt", //removed, no more Space Shuttle flights :(
			"stations.txt",
            "tle-new.txt", // added 26 Sept 2008 - SEG
            "visual.txt", // SEG v4.2.2 added
            "active.txt", // SEG v4.2.2 added
            //"analyst.txt", // SEG v4.2.2 added
            //---- Weather & Earth Resources Satellites ----
			"weather.txt",
			"noaa.txt",
			"goes.txt",
			"resource.txt",
			"sarsat.txt",
			"dmc.txt",
			"tdrss.txt",
            "argos.txt", // SEG v4.2.2 added
            "planet.txt", // SEG v4.2.2 added
            "spire.txt", // SEG v4.2.2 added
            //---- Communications Satellites ----
			"geo.txt",
			"intelsat.txt",
			"gorizont.txt",
			"raduga.txt",
			"molniya.txt",
			"iridium.txt",
			"iridium-next.txt",
			"orbcomm.txt",
			"globalstar.txt",
            "ses.txt", // SEG v4.2.2 added
            "starlink.txt", // SEG v4.2.2 added
            "satnogs.txt", // SEG v4.2.2 added
			"amateur.txt",
			"x-comm.txt",
			"other-comm.txt",
			//---- Navigation Satellites ----
            "gps-ops.txt",
			"glo-ops.txt",
			"galileo.txt",
            "beidou.txt", // SEG v4.2.2 added
			"sbas.txt",
			"nnss.txt",
			"musson.txt",
            //---- Scientific Satellites ----
			"science.txt",
			"geodetic.txt",
			"engineering.txt",
			"education.txt",
            //---- Miscellaneous Satellites ----
			"military.txt",
			"radar.txt",
			"cubesat.txt",
			"other.txt",
			
	};
	
	// primary category for each TLE
	public String[] primCat = new String[] {
            //"Special-Interest",  //removed all : " Satellites" redundant //removed, no more Space Shuttle flights :( // SEG v4.2.1
			"Special-Interest",
            "Special-Interest", // new
            "Special-Interest", // SEG v4.2.2 added
            "Special-Interest", // SEG v4.2.2 added
            //"Special-Interest", // SEG v4.2.2 added
            //---- Weather & Earth Resources Satellites ----
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
            "Weather & Earth Resources", // SEG v4.2.2 added
            "Weather & Earth Resources", // SEG v4.2.2 added
            "Weather & Earth Resources", // SEG v4.2.2 added
            //---- Communications Satellites ----
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
            "Communications", // SEG v4.2.2 added
            "Communications", // SEG v4.2.2 added
            "Communications", // SEG v4.2.2 added
			"Communications",
			"Communications",
			"Communications",
            //---- Navigation Satellites ----
			"Navigation",
			"Navigation",
			"Navigation",
            "Navigation", // SEG v4.2.2 added
			"Navigation",
			"Navigation",
			"Navigation",
            //---- Scientific Satellites ----
			"Scientific",
			"Scientific",
			"Scientific",
			"Scientific",
            //---- Miscellaneous Satellites ----
			"Miscellaneous",
			"Miscellaneous",
			"Miscellaneous",
			"Miscellaneous"			
	};
	
	// secondary category for each TLE
	public String[] secondCat = new String[] {
            //"STS", //removed all : " Satellites" redundant //removed, no more Space Shuttle flights :( // SEG v4.2.1
			"Space Stations", // SEG v4.2.2 renamed from International Space Station to Space Stations
            "Last 30 Days' Launches", // new
            "100 (or so) Brightest", // SEG v4.2.2 added
            "Active Satellites", // SEG v4.2.2 added
            //"Analyst Satellites", // SEG v4.2.2 added
            //---- Weather & Earth Resources Satellites ----
			"Weather",
			"NOAA",
			"GOES",
			"Earth Resources",
			"Search & Rescue (SARSAT)",
			"Disaster Monitoring",
			"Tracking and Data Relay Satellite System (TDRSS)",
            "ARGOS Data Collection System", // SEG v4.2.2 added
            "Planet", // SEG v4.2.2 added
            "Spire", // SEG v4.2.2 added
			//---- Communications Satellites ----
            "Geosynchronous", // SEG v4.2.2 renamed Geostationary to Geosynchronous
			"Intelsat",
			"Gorizont",
			"Raduga",
			"Molniya",
			"Iridium",
			"Iridium NEXT",
			"Orbcomm",
			"Globalstar",
            "SES", // SEG v4.2.2 added
            "Starlink", // SEG v4.2.2 added
            "SatNOGS", // SEG v4.2.2 added
			"Amateur Radio",
			"Experimental",
			"Other Comm", // SEG v4.2.2 renammed from Other to Other Comm
			//---- Navigation Satellites ----
            "GPS Operational",
			"Glonass Operational",
			"Galileo",
            "Beidou", // SEG v4.2.2 added
			"Satellite-Based Augmentation System (WAAS/EGNOS/MSAS)",
			"Navy Navigation Satellite System (NNSS)",
			"Russian LEO Navigation",
            //---- Scientific Satellites ----
			"Space & Earth Science",
			"Geodetic",
			"Engineering",
			"Education",
            //---- Miscellaneous Satellites ----
			"Miscellaneous Military",
			"Radar Calibration",
			"CubeSats",
			"Other"			
	};
	
	// local path to save files
    private String localPath = "data/tle/";

    // proxy info
    private String proxyHost = "proxy1.lmco.com";
    private String proxyPort = "80";
    private boolean usingProxy = false;

    // error text
    String errorText = "";

    // GUI components to be updated and if in a gui
    //boolean inGUI = false;
    //JProgressBar progBar;
    //JLabel progLabel = null;

    boolean downloadINI = false; // download initialized
    int currentTLEindex = 0;

    // main constructor
    public TLEDownloader()
    {
    } //TLEDownloader

    // constructor for use in a GUI - with progress bar and text area
//    public TLEDownloader(JProgressBar progBar, JLabel progLabel)
//    {
//        // we are in a GUI
//        inGUI = true;
//
//        this.progBar = progBar;
//        this.progLabel = progLabel;
//
//    } //TLEDownloader

    /**
     * downloads all the TLEs without stopping inbetween each file
     * @return if all files were downloaded successfully (if returns false see getErrorText for reason)
     */
    public boolean downloadAllTLEs()
    {
         boolean success = true; // flag to tell if everyting worked
         
         // do download
         success = startTLEDownload();

         // if started okay download the files
         if(success)
         {
             // while there is more to download and there are no errors
             while( this.hasMoreToDownload() && success )
             {
                success = this.downloadNextTLE();
             }
         }
         
         //
         return success;
    } // downloadAllTLEs


    /**
     * Starts the TLE download process
     * return boolean indecating if everything is ready to download
     */
    public boolean startTLEDownload()
    {
        boolean success = true; // flag to tell if everyting worked

        // for proxy stuff: (if needed)
        if (usingProxy)
        {
            Properties systemSettings = System.getProperties();
            systemSettings.put("proxySet", "true");
            systemSettings.put("http.proxyHost", proxyHost);
            systemSettings.put("http.proxyPort", proxyPort);
        }

        // first see if local directory exists if not try to create it
        if (!(new File(getLocalPath()).exists()))
        {
            // local directory doesn't exsist try to create it --
            // Create a directory; all non-existent ancestor directories are
            // automatically created
            success = (new File(getLocalPath())).mkdirs();
            if (!success)
            {
                // Directory creation failed
                //JOptionPane.showMessageDialog(null, "Error Creating Local TLE Data Directory: Check File Permissions", "ERROR", JOptionPane.ERROR_MESSAGE);
                errorText = "Error Creating Local TLE Data Directory: Check File Permissions";
                return false; // return error
            }
        } // check local path

        // setup download index
        currentTLEindex = 0;

        // set ini to true if everything was okay , else false
        downloadINI = success;

        return success;
    } // startTLEDownload


    /**
     * a check to see if there are more files to download
     * @return
     */
    public boolean hasMoreToDownload()
    {
        if(!downloadINI)
        {
            return false; // not initialized
        }

        if( currentTLEindex >= fileNames.length)
        {
            return false;
        }

        return true;
    } // hasMoreToDownload

    public String getNextFileName()
    {
        String result = "";
        if(! this.hasMoreToDownload() )
        {
            return result;
        }

        result = fileNames[currentTLEindex];

        return result;
    } // getNextFileName

    /**
     * gets the percent complete - integers 0-100
     * @return percent complete
     */
    public int getPercentComplete()
    {
        if(!this.hasMoreToDownload())
        {
            return 0;
        }
        else
        {
            return (int) Math.round( (currentTLEindex * 100.0) / fileNames.length) ;
        }
    } // getPercentComplete

    /**
     * downloads all the TLEs without stopping inbetween each file
     * @return if all files were downloaded successfully
     */
    public boolean downloadNextTLE()
	{
		boolean success = true; // flag to tell if everyting worked

        if(!downloadINI)
        {
            errorText = "startTLEDownload() must be ran before downloadNextTLE() can begin";
            return false;
        }

        // check index
        if( !this.hasMoreToDownload() )
        {
            errorText = "There are no more TLEs to download";
            return false;
        }

        

        // put for loop around this
        //for (int i = 0; i < fileNames.length; i++)
        //{
        int i = currentTLEindex;
//            if (inGUI)
//            {
//
//                progBar.setValue((int) Math.round((i * 100.0) / fileNames.length));
//                progBar.repaint();
//                progLabel.setText("Downloading File: " + fileNames[i]);
//            }

        try
        {
            // open file on the web
            URL url = new URL(rootWeb + fileNames[i]);
            URLConnection c = url.openConnection();
            InputStreamReader isr = new InputStreamReader(c.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            // open file on local drive to save to
            File outFile = new File(localPath + fileNames[i]);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

            // save file locally
            String currentLine = "";
            while ((currentLine = br.readLine()) != null)
            {
                writer.write(currentLine);
                writer.newLine();   // Write system dependent end of line.
            }

            // close file and web connection
            br.close();
            writer.close();

//            if (i == (fileNames.length - 1)) // last one update to 100%
//            {
//                progBar.setValue(100);
//                progBar.repaint();
//            }

        } catch (Exception e)
        {
            System.out.println("Error Reading/Writing TLE - " + fileNames[i] + "\n" + e.toString());
            //e.printStackTrace();
            success = false;

            errorText = e.toString();

            return false; // return after first error, to prevent slow response
        }
        //} // for each TLE file

        currentTLEindex++; // increment index after this file has been downloaded (or attempted)

        return success;

    } // downloadNextTLE
	
	
	public String getErrorText()
	{
		return errorText;
	}
	
	
	   // test driving main fuction
    public static void main(String[] args)
    {
        TLEDownloader td = new TLEDownloader();
        boolean result = td.downloadAllTLEs();

        System.out.println("Update of TLEs was sucessful? : " + result);

    } // main

    public int getTleFileCount()
    {
        return fileNames.length;
    }

    public String getTleFilePath(int index)
    {
        return (localPath + fileNames[index]);
    }

    public String getTleWebPath(int index)
    {
        return rootWeb + fileNames[index];
    }

    public void setUsingProxy(boolean b)
    {
        this.usingProxy = b;
    }

    public boolean getUsingProxy()
    {
        return usingProxy;
    }

    public void setProxyPort(String portStr)
    {
        this.proxyPort = portStr;
    }

    public String getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public String getLocalPath()
    {
        return localPath;
    }

    public void setLocalPath(String localPath)
    {
        this.localPath = localPath;
    }
} // TLEDownloader
