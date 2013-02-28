/*
 * Reads world land mass data for plotting
 *=====================================================================
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
 * Created on September 2, 2007, 10:34 PM
 *
 * Accepts two formats - .dat and .rl 
 * .dat - files with lists of lat, long pairs and regions seperated by #
 * .rl - STK's RegionList format
 * 
 */

package jsattrak.utilities;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author sgano
 */
public class LandMassRegions
{
    
    private String rootDir = "data/regions/";
    private String dataFileName = "worldlandmass.rl"; // "usstates.rl"  "worldlandmass.rl"  "new_coast_50.dat"
    
    // hashtable to store region name and array containint the lat/long surrounding the region
    private Hashtable<String,Vector<float[]>> landRegionHash = null;
    
    private boolean showLandMassOutlines = false;
    private Color landOutlineColor = Color.WHITE;
    
    
    /** Creates a new instance of LandMassRegions */
    public LandMassRegions()
    {
        
        if(showLandMassOutlines)
        {
            readInLandMassRegions();
        }
    }
    
    // general function to load in data
    public void readInLandMassRegions()
    {
        String[] parts = (rootDir+dataFileName).split("\\."); // split by period
        
        if( parts[1].equalsIgnoreCase("rl") )
        {
            readInLandMassRegionsRL();
        }
        else
        {
            readInLandMassRegionsDat();
        }
    }
    
    // read in land mass regions data from txt file -- with format RL
    private void readInLandMassRegionsRL()
    {
        
         landRegionHash = new Hashtable<String,Vector<float[]>>();
        
         try
         {
             BufferedReader dataReader = null; // initalization of reader (either local or web)
             
             // create local file reader
             File dataFile = new File(rootDir+dataFileName);
             FileReader dataFileReader = new FileReader(dataFile);
             dataReader = new BufferedReader(dataFileReader); // from local file
             
             String nextLine;
             String[] dataArray;
             String currentRegionName;
             Vector<float[]> tempLLVector;
             
             while( (nextLine = dataReader.readLine()) != null)
             {
                 
                 dataArray = nextLine.split("\\s+"); // split by spaces
                 
                 if(dataArray[0].equalsIgnoreCase("RegionName"))
                 {
                     currentRegionName = dataArray[1]; 
                     
                     tempLLVector = new Vector<float[]>(50,50);  // ini size=50, increment=50
                     
                     nextLine = dataReader.readLine();
                     
                     
                     while( nextLine!= null && !nextLine.startsWith("END"))
                     {
                         if(nextLine.length() < 1 || nextLine.startsWith("B"))
                         {
                             // skip line
                         }
                         else
                         {
                             // good line:
                             dataArray = nextLine.split("\\s+"); // split by spaces of any size"   "
                             
                             //System.out.println("line: " + nextLine);
                             
                             float lat = Float.parseFloat(dataArray[0]);
                             float lon = Float.parseFloat(dataArray[1]);
                             tempLLVector.add(new float[] {lat,lon});
                         }
                         
                         // read next line
                         nextLine = dataReader.readLine();
                     } // until END is reached
                     
                     // save name and vector
                     landRegionHash.put(currentRegionName, tempLLVector);
                     
                 } // region name
                 
             } // outer file reader

             
             // close file
             dataFileReader.close();
             
             // debug
             System.out.println("Number of Regions Read: " + landRegionHash.size() );
             
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         
    } // readInLandMassRegions
    
    
     private void readInLandMassRegionsDat()
    {
     
         landRegionHash = new Hashtable<String,Vector<float[]>>();
         
         try
         {
             BufferedReader dataReader = null; // initalization of reader (either local or web)
             
             // create local file reader
             File dataFile = new File(rootDir+dataFileName);
             FileReader dataFileReader = new FileReader(dataFile);
             dataReader = new BufferedReader(dataFileReader); // from local file
             
             String nextLine;
             String[] dataArray;
             String currentRegionName;
             Vector<float[]> tempLLVector;
             
             tempLLVector = new Vector<float[]>(50,20);  // ini size=50, increment=50
             
             int landmassCount = 0;
             
             while( (nextLine = dataReader.readLine()) != null)
             {
                 
                 if( nextLine.startsWith("#") )
                 {
                     // save off previous section
                     landRegionHash.put("landmass"+landmassCount, tempLLVector);
                     
                     landmassCount++; // add to count
                     
                     // then a new array is starting
                     tempLLVector = new Vector<float[]>(50,50);  // ini size=50, increment=50
                 }
                 else
                 {
                     dataArray = nextLine.trim().split("\\s+"); // split by spaces
                     
                     if(dataArray[0].length() > 1) // make sure line is not blank
                     {
                         float lon = Float.parseFloat(dataArray[0]);
                         float lat = Float.parseFloat(dataArray[1]);
                         tempLLVector.add(new float[] {lat,lon});
                     }
                     
                 } // save current line to Vector
                 
                 
             } // while loop
             
             // final save to Hash
             landRegionHash.put("landmass"+landmassCount, tempLLVector);
                     
             // close file
             dataFileReader.close();
             
             // debug
             System.out.println("Number of Regions Read: " + landRegionHash.size() );
             //System.out.println("Size 0 = " + landRegionHash.get("landmass0").size());

         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         
    } // readInLandMassRegionsDAT
    
    // get hash
    public Hashtable<String,Vector<float[]>> getLandMassHash()
    {
        return landRegionHash;
    }
    
    // get vector from has using key
    public Vector<float[]> getLandMassVector(String key)
    {
        return landRegionHash.get(key);
    }

    public String getDataFileName()
    {
        return dataFileName;
    }


    public boolean isShowLandMassOutlines()
    {
        return showLandMassOutlines;
    }

    public void setRegionOptions(boolean showLandMassOutlines, String dataPath, Color landOutlineColor)
    {
        boolean reloadData = false;
        
        // set color
        this.landOutlineColor = landOutlineColor;
        
        // data file name
        if(!this.dataFileName.equalsIgnoreCase(dataPath))
        {
            this.dataFileName = dataPath;
            if(showLandMassOutlines)
            {
                // new file name -- reread data
                //readInLandMassRegions();
                reloadData = true;
            }
        }
        
        // boolean for showing outlines
        if(this.showLandMassOutlines != showLandMassOutlines)
        {
            this.showLandMassOutlines = showLandMassOutlines;
            
            //System.out.println("here  - show");
            
            if(showLandMassOutlines)
            {
                reloadData = true;
            }
            else
            {
                // off -- save memory
                landRegionHash.clear();
                System.gc(); // garbage collect
            }
            
        } // if change
        
        
        // update data?
        if(reloadData)
        {
            readInLandMassRegions(); // reread data
        }
        
    } // setRegionOptions

    public Color getLandOutlineColor()
    {
        return landOutlineColor;
    }

    
    public String getRootDir()
    {
        return rootDir;
    }


} // LandMassRegions
