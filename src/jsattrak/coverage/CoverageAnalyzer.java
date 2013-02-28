/*
 * CoverageAnalyzer.java
 * 
 * Class to analyze Earth coverage metrics from Satellite(s). Also has the 
 * capability to render metrics to 2D and 3D windows.
 * 
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

package jsattrak.coverage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Vector;
import jsattrak.gui.J2dEarthLabel2;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import name.gano.astro.GeoFunctions;
import name.gano.astro.time.Time;

/**
 *
 * @author Shawn
 */
public class CoverageAnalyzer implements JSatTrakRenderable,JSatTrakTimeDependent
{
    // data arrays
    private  double[][] coverageCumTime;  // cumulative coverage time array [latPanels x longPanels] in days
     // in degrees
    private double[] latPanelMidPoints; // middle point latitude of each division/panel
    private double[] lonPanelMidPoints; // middle point longitude of each division/panel
    private double[] latGridPoints; // grid end points for latitude
    private double[] lonGridPoints; // grid end points for longitude
   
    private double minNotZeroVal = 1;  // current maximum and minimum (NOT ZERO) values 
    private double maxVal = 100;
    
    private Time startTime = new Time(); // keep track of start time
    
    // color map for data
    ColorMap colorMap = new ColorMap();
    
    private double lastMJD = -1; // last MJD update time
    
    Vector<String> satsUsedInCoverage = new Vector<String>(); // vector of satellites used in Coverage anaylsis
    
    // settings ===========
    // grid sizing >=1
    private int latPanels = 36; //72;//36; //18// number of divisons along lines of latitude (grid points -1)
    private int longPanels = 72;  //144;//72; //36 // number of divisions along lines of longitude (grid points -1)
    
    // in degrees
    private double[] latBounds = {-90.0, 90.0}; // minimum,maxium latitude to use in coverage anaylsis
    private double[] longBounds = {-180.0, 180.0}; // minimum,maxium longitude to use in coverage anaylsis
    
    private int alpha = 150; //151; // tranparency of colored panels, 0=can't see it, 255=solid
    
    private boolean dynamicUpdating = true; // if dynamic updating from GUI time stepping is enabled
    private boolean plotCoverageGrid = false; // plot panel grid and center points of panels
    private double elevationLimit = 15; //15; // elevation limit for ground coverage [degrees] (must be higher for this to count as coverage
    private NumberFormat colorBarNumberFormat = new DecimalFormat("0.00E0");
    private boolean showColorBar = true; // Color bar settings
    private int pixelsFromBottom = 20;
    private int pixelsFromLeft = 20;
    private int colorBarLen = 100;
    private int colorBarHeight = 10;
    private int colorBarTextSpacing = 5;
    private // pixels below bar where text is displayed
    Color colorbarBGcolor = new Color(255, 255, 255, 180);
    private Color colorBarTextcolor = Color.BLACK;
    
    // default constructor
    public CoverageAnalyzer()
    {
        iniParamters();
    } // constructor
    
    /**
     * Constructor with current time - this will allow coverage to start on next time step
     * @param currentJulianDate current Julian Date
     */
    public CoverageAnalyzer(final Time currentJulianDate)
    {
        iniParamters();
        lastMJD = currentJulianDate.getMJD();
        startTime.set(currentJulianDate.getCurrentGregorianCalendar().getTimeInMillis());        
    }
    
//    /**
//     * NEEDS TO INCLUDE - SATS INTO VECTOR BEFORE THIS WILL WORK PROPERLY
//     * Constructor with current time and current time step - this will allow coverage anaylsis to immediately start
//     * @param currentJulianDate current Julian Date
//     * @param timeStepDays time step to be used in days
//     * @param satHash Hashtable of current satellites
//     */
//    public CoverageAnalyzer(final Time currentJulianDate, double timeStepDays, final Hashtable<String,AbstractSatellite> satHash)
//    {
//        iniParamters();
//        lastMJD = currentJulianDate.getMJD()-timeStepDays;
//        
//        performCoverageAnalysis(currentJulianDate, satHash);
//    }
    
    /**
     * Clear coverage data and initalizie update time for next simulation step
     * @param currentJulianDate
     */
    public void clearCoverageData(final Time currentJulianDate)
    {
        iniParamters();
        lastMJD = currentJulianDate.getMJD();
        startTime.set(currentJulianDate.getCurrentGregorianCalendar().getTimeInMillis());
    }
    
    /**
     * Clears the coverage data and resets last update date
     */
    public void clearCoverageData()
    {
        iniParamters();
    }
    
    // initalized all parameters (used at class construction to create all arrays, etc)
    private void iniParamters()
    {
        // cumulative time create new array (default 0)
        coverageCumTime = new double[latPanels][longPanels];
        // mid points
        latPanelMidPoints = new double[latPanels];
        lonPanelMidPoints = new double[longPanels];
        // grid points
        latGridPoints = new double[latPanels+1];
        lonGridPoints = new double[longPanels+1];
        
        // calulate grid points, mid points
        for(int i=0;i<latPanels+1;i++)
        {
            latGridPoints[i] = i*(latBounds[1]-latBounds[0])/(latPanels)+latBounds[0];
            if(i>0)
            {
                latPanelMidPoints[i-1] = (getLatGridPoints()[i]+getLatGridPoints()[i-1])/2.0;
            }
        }
        for(int i=0;i<longPanels+1;i++)
        {
            lonGridPoints[i] = i*(longBounds[1]-longBounds[0])/(longPanels)+longBounds[0];
            if(i>0)
            {
                lonPanelMidPoints[i-1] = (getLonGridPoints()[i]+getLonGridPoints()[i-1])/2.0;
            }
        }
        
        // clear last mjd update
        lastMJD = -1;
        
    }// iniParamters
    
    // test main function
    public static void main(String[] args)
    {
        CoverageAnalyzer ca = new CoverageAnalyzer();
    } // main
    
    
    /**
     *  Performa an update of time and data of Coverage Metrics
     * @param currentJulianDate
     * @param satHash
     * @param gsHash
     */
    @Override
    public void updateTime(final Time currentJulianDate, final Hashtable<String,AbstractSatellite> satHash, final Hashtable<String,GroundStation> gsHash)
    {
        if(!dynamicUpdating)
        {
            return; // don't update converage anlysis from JSatTrack GUI
        }
        
        performCoverageAnalysis(currentJulianDate,satHash); // do the analysis
        
    } // updateTime
    
    // internal function to actually perform the anaylsis - so it can be used by GUI update calls or coverage tool
    /**
     * Performs coverage anaylsis with given time and satellite array, this fuction should be called only when directly performing coverage analysis, otherwise call updateTime
     * @param currentJulianDate
     * @param satHash
     */
    public void performCoverageAnalysis(final Time currentJulianDate, final Hashtable<String,AbstractSatellite> satHash)
    {
        // if first time update, save time and quit (only start calc after first time step)
        if(lastMJD == -1)
        {
            lastMJD = currentJulianDate.getMJD();
            startTime.set(currentJulianDate.getCurrentGregorianCalendar().getTimeInMillis());
            return;
        }
        
        // check time make sure this time is past when the last time update was 
        if(currentJulianDate.getMJD() <= lastMJD)
        {
            return; // do nothing as this time is later
        }
        // calc time diff, and save time
        double timeDiffDays = currentJulianDate.getMJD() - lastMJD;
        lastMJD = currentJulianDate.getMJD();
        
        // create temp array for time cumlation (so we don't double count sat coverage)
        // each panel either has access or it doesn't for the current time step -- boolean 
        boolean[][] tempAcessArray = new boolean[latPanels][longPanels];
        
        // === do coverage anaylsis, for each satellite ===
        for(String satName : satsUsedInCoverage)
        {
            // get sat Object
            AbstractSatellite currentSat = satHash.get(satName);
            
            // check to see if satellite is in lat/long AOI coverage box
            if(currentSat.getLatitude()*180/Math.PI  >= latBounds[0]  && 
               currentSat.getLatitude()*180/Math.PI  <= latBounds[1]  &&
               currentSat.getLongitude()*180/Math.PI >= longBounds[0] &&
               currentSat.getLongitude()*180/Math.PI <= longBounds[1]    )
            {
                
                // find closest panel under satellite and the index of that panel
                double latPercentile = (currentSat.getLatitude()*180/Math.PI-latBounds[0]) / (latBounds[1]-latBounds[0]);
                int latIndex = (int)Math.floor(latPercentile*latPanels);
                double longPercentile = (currentSat.getLongitude()*180/Math.PI-longBounds[0]) / (longBounds[1]-longBounds[0]);
                int longIndex = (int)Math.floor(longPercentile*longPanels);
                
                // Coverage assumes sat doesn't have a shaped sensor and it can look straight down (nadir)
                // debug for now mark point as access added
                double[] aer = new double[3];
//                aer = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(), 
//                        new double[]{latPanelMidPoints[latIndex],lonPanelMidPoints[longIndex],0},  // sea level
//                        currentSat.getPosMOD());
//                
//                // see if sat sub-point panel has access (otherwise nothing does)
//                if(aer[1] >= elevationLimit)
//                {
//                    tempAcessArray[latIndex][longIndex] = true; // can't assume access here!
//                }
                
                // Search up=====================================================
                // search upwards until no access (careful of lat >90)
                int i = latIndex; // includes satellite sub point
                double tempElevation2=0; // used in searching to the left and right
                do
                {
                    // take care of when i >= latPanels (reflection for longitude index and make lat go down instead of up (and stay at top one iter)
                    
                    aer = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(), 
                        new double[]{getLatPanelMidPoints()[i],getLonPanelMidPoints()[longIndex],0},  // sea level
                        currentSat.getTEMEPos());
                    
                    if(aer[1] >= elevationLimit)
                    {
                        tempAcessArray[i][longIndex] = true;
                        // search to the left =============================
                        int j=longIndex-1;
                        int longSearchCount = 0;
                        int jWrappedIndex = j; // updated so seach can wrap around map
                        do
                        {
                            // take car of i and j with wrap arounds
                            if(j < 0)
                            {
                                jWrappedIndex = longPanels + j;
                            }
                            else if(j >= longPanels)
                            {
                                jWrappedIndex = j - longPanels;
                            }
                            else
                            {
                                jWrappedIndex = j;
                            }
                            
                            tempElevation2 = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(), 
                                new double[]{getLatPanelMidPoints()[i],getLonPanelMidPoints()[jWrappedIndex],0},  // sea level
                                currentSat.getTEMEPos())[1];
                            if(tempElevation2 >= elevationLimit)
                            {
                                tempAcessArray[i][jWrappedIndex] = true;
                            }
                            
                            j--;
                            longSearchCount++; // make sure we don't get stuck on seaching a row over and over
                        }while(tempElevation2 >= elevationLimit && longSearchCount < longPanels); 
                        // search to the left =============================
                        // search to the Right =============================
                        j=longIndex+1;
                        longSearchCount = 0;
                        jWrappedIndex = j; // updated so seach can wrap around map
                        do
                        {
                            // take car of i and j with wrap arounds
                            if(j < 0)
                            {
                                jWrappedIndex = longPanels + j;
                            }
                            else if(j >= longPanels)
                            {
                                jWrappedIndex = j - longPanels;
                            }
                            else
                            {
                                jWrappedIndex = j;
                            }
                            
                            tempElevation2 = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(), 
                                new double[]{getLatPanelMidPoints()[i],getLonPanelMidPoints()[jWrappedIndex],0},  // sea level
                                currentSat.getTEMEPos())[1];
                            if(tempElevation2 >= elevationLimit)
                            {
                                tempAcessArray[i][jWrappedIndex] = true;
                            }
                            
                            j++;
                            longSearchCount++; // make sure we don't get stuck on seaching a row over and over
                        }while(tempElevation2 >= elevationLimit && longSearchCount < longPanels); 
                        // search to the Right =============================
                        
                    }
                    
                    i++;
                }while(aer[1] >= elevationLimit && i < latPanels); // do while - only search up to top of panel
                // Search up=====================================================
                // Search down=====================================================
                // search down until no access (careful of lat >90)
                i = latIndex - 1; // includes satellite sub point
                tempElevation2 = 0; // used in searching to the left and right
                if (i >= 0) // avoid searching down if i is already 0
                {
                    do
                    {
                        // take care of when i >= latPanels (reflection for longitude index and make lat go down instead of up (and stay at top one iter)
                        aer = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(),
                                new double[]
                                {
                                    getLatPanelMidPoints()[i], getLonPanelMidPoints()[longIndex], 0
                                
                                }, // sea level
                                currentSat.getTEMEPos());

                        if (aer[1] >= elevationLimit)
                        {
                            tempAcessArray[i][longIndex] = true;
                            // search to the left =============================
                            int j = longIndex - 1;
                            int longSearchCount = 0;
                            int jWrappedIndex = j; // updated so seach can wrap around map

                            do
                            {
                                // take car of i and j with wrap arounds
                                if (j < 0)
                                {
                                    jWrappedIndex = longPanels + j;
                                }
                                else if (j >= longPanels)
                                {
                                    jWrappedIndex = j - longPanels;
                                }
                                else
                                {
                                    jWrappedIndex = j;
                                }

                                tempElevation2 = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(),
                                        new double[] {getLatPanelMidPoints()[i], getLonPanelMidPoints()[jWrappedIndex], 0}, // sea level
                                        currentSat.getTEMEPos())[1];
                                if (tempElevation2 >= elevationLimit)
                                {
                                    tempAcessArray[i][jWrappedIndex] = true;
                                }

                                j--;
                                longSearchCount++; // make sure we don't get stuck on seaching a row over and over
                            }while (tempElevation2 >= elevationLimit && longSearchCount < longPanels);
                            // search to the left =============================
                            // search to the Right =============================
                            j = longIndex + 1;
                            longSearchCount = 0;
                            jWrappedIndex = j; // updated so seach can wrap around map
                            do
                            {
                                // take car of i and j with wrap arounds
                                if (j < 0)
                                {
                                    jWrappedIndex = longPanels + j;
                                }
                                else if (j >= longPanels)
                                {
                                    jWrappedIndex = j - longPanels;
                                }
                                else
                                {
                                    jWrappedIndex = j;
                                }
                                
                                tempElevation2 = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(),
                                        new double[]{getLatPanelMidPoints()[i], getLonPanelMidPoints()[jWrappedIndex], 0}, // sea level
                                        currentSat.getTEMEPos())[1];
                                if (tempElevation2 >= elevationLimit)
                                {
                                    tempAcessArray[i][jWrappedIndex] = true;
                                }

                                j++;
                                longSearchCount++; // make sure we don't get stuck on seaching a row over and over
                            }
                            while (tempElevation2 >= elevationLimit && longSearchCount < longPanels);
                        // search to the Right =============================

                        } // if in elecation limit

                        i--;
                    }while (aer[1] >= elevationLimit && i >= 0); // do while
                } // if already at 0 no need to search down
                // Search down=====================================================
                
                
            }// sat is in coverage AOI           
        } // for each satellite - Coverage anaylsis
        
        // merge temp and timecumarray // and update max and min values
        minNotZeroVal = Double.MAX_VALUE; // really high to start
        maxVal = -1; // really low to start
        for(int i=0;i<latPanels;i++) 
        {
            for(int j=0;j<longPanels;j++)
            {
                // DEBUG CLEAR VALUE SO ONLY POINTS CURRENTLY IN VIEW SHOW UP
                //coverageCumTime[i][j] = 0;
                
                if(tempAcessArray[i][j])
                {
                    coverageCumTime[i][j] += timeDiffDays;
                } // if access at this point
                
                // update max and min
                if(getCoverageCumTime()[i][j] > maxVal)
                {
                    maxVal = getCoverageCumTime()[i][j];
                }
                if(getCoverageCumTime()[i][j] < minNotZeroVal && getCoverageCumTime()[i][j] > 0)
                {
                   minNotZeroVal =  getCoverageCumTime()[i][j];
                }
                
            } // long panels (j)
        } // lat panels (i) (merge data)
        

        
    } // performCoverageAnalysis
    
    // draw 2d
    public void draw2d(Graphics2D g2, J2dEarthLabel2 earthLabel, int totWidth, int totHeight, int imgWidth, int imgHeight, double zoomFac, double cLat, double cLong)
    {
        int[] xy = new int[2];
        int[] xy_old = new int[2];
        
        // draw in grid lines for lat and long of coverage area
        if (plotCoverageGrid)
        {
            g2.setColor(new Color(0.0f, 1.0f, 0.0f, 0.2f));
            for (double lat : getLatGridPoints())
            {
                xy = earthLabel.findXYfromLL(lat, longBounds[0], totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);
                xy_old = earthLabel.findXYfromLL(lat, longBounds[1], totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);

                g2.drawLine(xy_old[0], xy_old[1], xy[0], xy[1]); // draw a line across the map

            }
            g2.setColor(new Color(0.0f, 1.0f, 0.0f, 0.2f));
            for (double lon : getLonGridPoints())
            {
                xy = earthLabel.findXYfromLL(latBounds[0], lon, totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);
                xy_old = earthLabel.findXYfromLL(latBounds[1], lon, totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);

                g2.drawLine(xy_old[0], xy_old[1], xy[0], xy[1]); // draw a line across the map

            }
            // draw center points
            g2.setColor(new Color(0.0f, 1.0f, 0.0f, 0.2f));
            int dotSize = 1;
            for (double lat : getLatPanelMidPoints())
            {
                for (double lon : getLonPanelMidPoints())
                {
                    xy = earthLabel.findXYfromLL(lat, lon, totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);

                    g2.drawRect(xy[0] - dotSize / 2, xy[1] - dotSize / 2, dotSize, dotSize);
                }
            }
        } // graw grid and center points
        // fill in color scaled panels based on cumulative Coverage time
        // should combine with above... just use alpha =0.0 when panel is blank
        int xmax, xmin, ymax, ymin;
        for(int i=0;i<getLatPanelMidPoints().length;i++)
        {
            for(int j=0;j<getLonPanelMidPoints().length;j++)
            {
                xy = earthLabel.findXYfromLL(getLatGridPoints()[i], getLonGridPoints()[j],totWidth,totHeight,imgWidth,imgHeight,zoomFac,cLat,cLong);
                xy_old = earthLabel.findXYfromLL(getLatGridPoints()[i+1], getLonGridPoints()[j+1],totWidth,totHeight,imgWidth,imgHeight,zoomFac,cLat,cLong);
                
                xmax = Math.max(xy[0], xy_old[0]);
                xmin = Math.min(xy[0], xy_old[0]);
                ymax = Math.max(xy[1], xy_old[1]);
                ymin = Math.min(xy[1], xy_old[1]);
                
                // color based on: coverageCumTime[i][j]
                // dummy way for now black or white
                if(getCoverageCumTime()[i][j]>0)
                {
                    g2.setColor( colorMap.getColor(getCoverageCumTime()[i][j], minNotZeroVal, maxVal, alpha)  );
                    g2.fillRect(xmin, ymin, xmax-xmin,ymax-ymin);//xy_old[0]-xy[0], xy_old[1]-xy[1]);
                }
                else
                {
                    // don't draw anything
                }
                
            } // for lon panels
        } // for lat panels
        
        // Draw color bar if wanted!!
        if(showColorBar)
        {
            // colorbar background
            g2.setColor( colorbarBGcolor );
            g2.fillRect(pixelsFromLeft-5, totHeight-pixelsFromBottom-colorBarHeight-3, colorBarLen+12+30, colorBarHeight+colorBarTextSpacing+15);
            
            // color bar specturm
            for(int i=0;i<colorBarLen;i++)
            {
                g2.setColor( colorMap.getColor(i, 0, colorBarLen, 255) );
                g2.drawLine(pixelsFromLeft+i, totHeight-pixelsFromBottom, pixelsFromLeft+i, totHeight-pixelsFromBottom-colorBarHeight);
            }
            
            // color bar labeling
            // 0 %
            int textHeight = 10;
            g2.setColor( colorBarTextcolor );
            g2.drawLine(pixelsFromLeft-1, totHeight-pixelsFromBottom+colorBarTextSpacing, pixelsFromLeft-1,totHeight-pixelsFromBottom-colorBarHeight);
            g2.drawString(colorBarNumberFormat.format(minNotZeroVal*24*60*60) + " sec", pixelsFromLeft-1, totHeight-pixelsFromBottom+colorBarTextSpacing+textHeight);
            
            // at 100%
            g2.setColor( Color.BLACK );
            g2.drawLine(pixelsFromLeft+colorBarLen, totHeight-pixelsFromBottom+colorBarTextSpacing, pixelsFromLeft+colorBarLen,totHeight-pixelsFromBottom-colorBarHeight);
            g2.drawString(colorBarNumberFormat.format(maxVal*24*60*60), pixelsFromLeft+colorBarLen, totHeight-pixelsFromBottom+colorBarTextSpacing+textHeight);
            
        } // showColorBar
        
    } // draw 2d
    
    // draw 3d
    public void draw3d()
    {
        
    } // draw 3d
    
    // Settings ==================================
    
    public void addSatToCoverageAnaylsis(String satName)
    {
        // first check to make sure sat isn't already in list
        for(String name : satsUsedInCoverage)
        {
            if(satName.equalsIgnoreCase(name))
            {
                return; // already in the list
            }
        }
        
        satsUsedInCoverage.add(satName);
    } // addSatToCoverageAnaylsis
    
    public void clearSatCoverageVector()
    {
        satsUsedInCoverage.clear();
    }
    
    public void removeSatFromCoverageAnaylsis(String satName)
    {
        // make sure name is in the Vector
        int i=0; // counter
        for(String name : satsUsedInCoverage)
        {
            if(satName.equalsIgnoreCase(name))
            {
                satsUsedInCoverage.remove(i);
                return; // already in the list
            }
            i++;
        }
    } // removeSatFromCoverageAnaylsis
    
    public Vector<String> getSatVector()
    {
        return satsUsedInCoverage;
    }
    
    // ======================================================
    
    public void setColorMap(ColorMap colorMap)
    {
        this.colorMap = colorMap;
    }
    
    public ColorMap getColorMap()
    {
        return colorMap;
    }

    public int getLatPanels()
    {
        return latPanels;
    }

    public void setLatPanels(int latPanels)
    {
        this.latPanels = latPanels;
    }

    public int getLongPanels()
    {
        return longPanels;
    }

    public void setLongPanels(int longPanels)
    {
        this.longPanels = longPanels;
    }

    public double[] getLatBounds()
    {
        return latBounds;
    }

    public void setLatBounds(double[] latBounds)
    {
        this.latBounds = latBounds;
    }

    public double[] getLongBounds()
    {
        return longBounds;
    }

    public void setLongBounds(double[] longBounds)
    {
        this.longBounds = longBounds;
    }

    public int getAlpha()
    {
        return alpha;
    }

    public void setAlpha(int alpha)
    {
        this.alpha = alpha;
    }

    public boolean isDynamicUpdating()
    {
        return dynamicUpdating;
    }

    public void setDynamicUpdating(boolean dynamicUpdating)
    {
        this.dynamicUpdating = dynamicUpdating;
    }

    public boolean isPlotCoverageGrid()
    {
        return plotCoverageGrid;
    }

    public void setPlotCoverageGrid(boolean plotCoverageGrid)
    {
        this.plotCoverageGrid = plotCoverageGrid;
    }

    public double getElevationLimit()
    {
        return elevationLimit;
    }

    public void setElevationLimit(double elevationLimit)
    {
        this.elevationLimit = elevationLimit;
    }

    public NumberFormat getColorBarNumberFormat()
    {
        return colorBarNumberFormat;
    }

    public void setColorBarNumberFormat(NumberFormat colorBarNumberFormat)
    {
        this.colorBarNumberFormat = colorBarNumberFormat;
    }

    public boolean isShowColorBar()
    {
        return showColorBar;
    }

    public void setShowColorBar(boolean showColorBar)
    {
        this.showColorBar = showColorBar;
    }

    public int getPixelsFromBottom()
    {
        return pixelsFromBottom;
    }

    public void setPixelsFromBottom(int pixelsFromBottom)
    {
        this.pixelsFromBottom = pixelsFromBottom;
    }

    public int getPixelsFromLeft()
    {
        return pixelsFromLeft;
    }

    public void setPixelsFromLeft(int pixelsFromLeft)
    {
        this.pixelsFromLeft = pixelsFromLeft;
    }

    public int getColorBarLen()
    {
        return colorBarLen;
    }

    public void setColorBarLen(int colorBarLen)
    {
        this.colorBarLen = colorBarLen;
    }

    public int getColorBarHeight()
    {
        return colorBarHeight;
    }

    public void setColorBarHeight(int colorBarHeight)
    {
        this.colorBarHeight = colorBarHeight;
    }

    public int getColorBarTextSpacing()
    {
        return colorBarTextSpacing;
    }

    public void setColorBarTextSpacing(int colorBarTextSpacing)
    {
        this.colorBarTextSpacing = colorBarTextSpacing;
    }

    public Color getColorbarBGcolor()
    {
        return colorbarBGcolor;
    }

    public void setColorbarBGcolor(Color colorbarBGcolor)
    {
        this.colorbarBGcolor = colorbarBGcolor;
    }

    public Color getColorBarTextcolor()
    {
        return colorBarTextcolor;
    }

    public void setColorBarTextcolor(Color colorBarTextcolor)
    {
        this.colorBarTextcolor = colorBarTextcolor;
    }

    public double[][] getCoverageCumTime()
    {
        return coverageCumTime;
    }

    public double[] getLatPanelMidPoints()
    {
        return latPanelMidPoints;
    }

    public double[] getLonPanelMidPoints()
    {
        return lonPanelMidPoints;
    }

    public double[] getLatGridPoints()
    {
        return latGridPoints;
    }

    public double[] getLonGridPoints()
    {
        return lonGridPoints;
    }
    
    public Color getColorForIndex(int i, int j)
    {
        return colorMap.getColor(getCoverageCumTime()[i][j], minNotZeroVal, maxVal);
    }

    public double getMinNotZeroVal()
    {
        return minNotZeroVal;
    }

    public double getMaxVal()
    {
        return maxVal;
    }
    
    public String getLowerBoundLabel()
    {
        return colorBarNumberFormat.format(minNotZeroVal*24*60*60) + " sec";
    }
    
    public String getUpperBoundLabel()
    {
        return colorBarNumberFormat.format(maxVal*24*60*60);
    }

    public Time getStartTime() {
        return startTime;
    }

    public double getLastMJD() {
        return lastMJD;
    }
    
} // CoverageAnalyzer
