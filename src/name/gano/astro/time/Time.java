/*
 * Time.java
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
 * Shawn Gano
 * 1 Sept 2007 
 *
 * Function to hold current UTCG time and convert to TT/ET if needed or Julian Date / MJD etc.
 * Main date contruct is the GreogorianCalendar
 */

package name.gano.astro.time;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Used to keep track of a time and convert to multiple formats.
 * @author Shawn E. Gano
 */
public class Time implements java.io.Serializable
{     
    // time units
    /**
     * year type
     */
    public final static int YEAR=Calendar.YEAR;
    /**
     * month type
     */
    public final static int MONTH=Calendar.MONTH;
    /**
     * date type
     */
    public final static int DATE=Calendar.DATE;
    /**
     * hour type
     */
    public final static int HOUR=Calendar.HOUR;
    /**
     * Hour type
     */
    public final static int HOUR_OF_DAY=Calendar.HOUR_OF_DAY;
    /**
     * Minute type
     */
    public final static int MINUTE=Calendar.MINUTE;
    /**
     * Second type
     */
    public final static int SECOND=Calendar.SECOND;
    /**
     * Millisecond type
     */
    public final static int MILLISECOND=Calendar.MILLISECOND;
    
    // main calendar
    private GregorianCalendar currentTime;
    // other date formats
    private double mjd; // Modified Julian date
    private double mjde; // Modified Julian Day Ephemeris time 
    
    // decimal formats
    private final static DecimalFormat fmt4Dig = new DecimalFormat("0000");
    private final static DecimalFormat fmt2Dig = new DecimalFormat("00");
    
    // output format
    private DateFormat dateFormat=null;
    
    // time zone
    private final static TimeZone tz=TimeZone.getTimeZone("UTC");  // default internal timezone
    private TimeZone tzStringFormat = TimeZone.getTimeZone("UTC"); // SEG removed static April 16 2009
    
    
    public static void main(String args[])
    {
        Time t = new Time(2007,9,1,11,59,0.0);
        System.out.println("Jul Date:" + t.getJulianDate() + ", string:" + t.getDateTimeStr());
//        System.out.println("MJD  :" + t.getMJD());
//        System.out.println("MJDE :" + t.getMJDE());
//        System.out.println("DT: " + t.deltaT( t.getMJD() )*60*60*24 );
        // 2454344.5
        t.addSeconds(120.0); // add 60 sec
        System.out.println("Jul Date + 120 sec:" + t.getJulianDate()+ ", string:" + t.getDateTimeStr());
        
        t.add(Time.HOUR,12);
        System.out.println("Jul Date + 12 hour:" + t.getJulianDate()+ ", string:" + t.getDateTimeStr());
        
    }
    
    
    /**
     * Default Constructor
     */
    public Time()
    {
        // create new calendar with default timezone
        currentTime = new GregorianCalendar(tz);
        
        // update other time formates
        updateTimeMeasures();
    }
    
  
    /**
     * Constructor with given calendar date (UT)
     *
     * @param year year
     * @param month month (1-12)
     * @param day day of month
     * @param hour hour 0-24
     * @param min minute
     * @param sec second and fraction of second (accurate up to 1 millisecond)
     */
    public Time(int year, int month, int day, int hour, int min, double sec)
    {
        int secInt = new Double( Math.floor(sec) ).intValue();
        int millisec = new Double( Math.round((sec - Math.floor(sec))*1000.0) ).intValue();
        
        currentTime = new GregorianCalendar(tz); // set default timezone
        
        currentTime.set(Calendar.YEAR,year);
        currentTime.set(Calendar.MONTH,month-1);
        currentTime.set(Calendar.DATE,day);
        currentTime.set(Calendar.HOUR_OF_DAY,hour);
        currentTime.set(Calendar.MINUTE,min);
        currentTime.set(Calendar.SECOND,secInt);
        currentTime.set(Calendar.MILLISECOND,millisec);
        
        // update other time formats
        updateTimeMeasures();
    }
    
    
/**
 * Updates the time to current system time
 */
    public void update2CurrentTime()
    {
        // update to current time (which is faster?)
        //currentTime =new GregorianCalendar(tz);
        currentTime.setTimeInMillis( System.currentTimeMillis() );
        //currentTime.setTime( new Date() );
        
        // update other time formats
        updateTimeMeasures();
    } // update2CurrentTime

    /**
     * Set the current time (UT) to the number of milliseconds
     * @param milliseconds number of millisconds as in from the function Calendar.getTimeInMillis()
     */
    public void set(long milliseconds)
    {
        currentTime.setTimeInMillis(milliseconds);
        
        // update other time formats
        updateTimeMeasures();
    } // set
    
     /**
     * Add specified value in specified time unit to current time
     *
     * @param unit int Time unit
     * @param val int Time increment
     */
    public void add(int unit, int val) 
    {
        currentTime.add(unit, val);
        
        // update other time formats
        updateTimeMeasures();
    }
    
    /**
     * Add specified seconds to current time
     *
     * @param seconds number of seconds to add to current time (can be fractional)
     */
    public void addSeconds(double seconds)
    {
        if(seconds > Integer.MAX_VALUE)
        {
            Double hours2Add = new Double(seconds / (60 * 60));
            //hours must be less than 2,147,483,647 which is about 89,478,485
            //days, that should be plenty
            currentTime.add(Calendar.HOUR, hours2Add.intValue());
            seconds = seconds - hours2Add.intValue() * 60 * 60;
        }

        int seconds2Add = new Double(seconds).intValue();
        int millis2Add = new Double(Math.round((seconds - seconds2Add) * 1000)).intValue();
        currentTime.add(Calendar.SECOND, seconds2Add);
        currentTime.add(Calendar.MILLISECOND, millis2Add);

        // update other time formats
        updateTimeMeasures();
    }
    // old method -- above was corrected based off of a fourm post by csuski 28-Aug-2009
//    public void addSeconds(double seconds)
//    {
//        // multiply input by 1000 then round off and add this number of milliseconds to date
//        int millis2Add = new Double(Math.round( seconds*1000 )).intValue();
//
//        currentTime.add(Calendar.MILLISECOND, millis2Add);
//
//        // update other time formats
//        updateTimeMeasures();
//    }
    
    /**
     * Updates the Julian and Julian Epehermis Dates using Current GregorianCalendar
     */
    private void updateTimeMeasures()
    {
        mjd = calcMjd(currentTime);
        mjde = mjd + deltaT(mjd);
    }
    
    /**
     * Gets the Julian Date (UT)
     * @return Returns the Julian Date
     */
    public double getJulianDate()
    {
        return mjd + 2400000.5;
    }
    
    /**
     * Gets the Modified Julian Date (Julian date minus 2400000.5) (UT)
     * @return Returns the Modified Julian Date
     */
    public double getMJD()
    {
        return mjd ;
    }
    
    /**
     * Gets the Modified Julian Ephemeris Date (Julian date minus 2400000.5) (TT)
     * @return Returns the Modified Julian Ephemeris Date
     */
    public double getMJDE()
    {
        return mjde;
    }
    
    /**
     * Sets timezone for the output string to use via the function getDateTimeStr()
     * @param aTzStringFormat time zone to format output strings with
     */
    public void setTzStringFormat(TimeZone aTzStringFormat)
    {
        tzStringFormat = aTzStringFormat;
    }
    
    
/**
 * Set SimpleDateFormat for displaying date/time string
 * @param dateFormat SimpleDateFormat
 */
public void setDateFormat(SimpleDateFormat dateFormat) 
{
    this.dateFormat=dateFormat;
}

/**
 * Set SimpleDateFormat string
 * ISSUE - only valid after Jan 1, 1970
 *@param formatStr String format for simple date format to use for creating strings of the date
 */
public void setDateFormat(java.lang.String formatStr)
{
    if ((formatStr!=null)&&(formatStr.length()>0))
    {
        dateFormat=new SimpleDateFormat(formatStr);
    }
}

/**
 * Gets the date format
 * @return date format
 */
public DateFormat getDateFormat()
{
    return dateFormat;
}

/**
 * Returns the specified field
 * @param field int The specified field
 * @return int The field value
 */
public final int get(int field) 
{
    return currentTime.get(field);
}

/*
 * Get the UTC date/time string in the format  of yyyy-mm-dd hh:mm:ss
 * If the dateFormat is not set or if the date is before Jan 1, 1970
 * otherwise the empty string "" will be returned.)
 * @return java.lang.String
 */
public String getDateTimeStr()
{
    String retStr="";
    
    if ((dateFormat!=null) &&( getJulianDate() >= 2440587.5))
    {
        dateFormat.setTimeZone(tzStringFormat);
        retStr=dateFormat.format( currentTime.getTime() );
    }
    else
    {
        StringBuffer strBuf = new StringBuffer(fmt4Dig.format(get(Time.YEAR)));
        strBuf.append("-");
        strBuf.append(fmt2Dig.format(get(Time.MONTH)+1));
        strBuf.append("-");
        strBuf.append(fmt2Dig.format(get(Time.DATE)));
        strBuf.append(" ");
        strBuf.append(fmt2Dig.format(get(Time.HOUR_OF_DAY)));
        strBuf.append(":");
        strBuf.append(fmt2Dig.format(get(Time.MINUTE)));
        strBuf.append(":");
        strBuf.append(fmt2Dig.format(get(Time.SECOND)));
        strBuf.append(" ");
        strBuf.append( tz.getID() );
        retStr=strBuf.toString();
    }
    return retStr;
}
    
    // ============================== STATIC Functions ====================================
    
  /** 
   *  Calculate Modified Julian Date from calendar object
   *
   * @param cal  Calendar object
   * @return Modified Julian Date (UT)
   */
    public static double calcMjd(Calendar cal)
    {
        double sec = cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND)/1000.0;
        return calcMjd(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE),cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), sec);
    }
        
  /** 
   *  Calculate Modified Julian Date from calendar date and time elements
   *
   * @param year  calendar year
   * @param month calendar month
   * @param day   calendar day
   * @param hour  calendar hour (0-24)
   * @param min   calendar min
   * @param sec   calendar sec
   * @return Modified Julian Date (UT)
   */
	public static double calcMjd(int year, int month, int day, int hour, int min, double sec)
        {
            // Variables
            long    MjdMidnight;
            double  FracOfDay;
            int     b;
            
            if (month<=2)
            { month+=12; --year;}
            
            if ( (10000L*year+100L*month+day) <= 15821004L )
            {
                b = -2 + ((year+4716)/4) - 1179;     // Julian calendar
            }
            else
            {
                b = (year/400)-(year/100)+(year/4);  // Gregorian calendar
            }
            
            MjdMidnight = 365L*year - 679004L + b + (int) (30.6001*(month+1)) + day;
            FracOfDay   = (hour+min/60.0+sec/3600.0) / 24.0;
            
            return MjdMidnight + FracOfDay;
        } //calcMjd
        
        
    /**
     * Return TT minus UT.
     *
     * <p>Up to 1983 Ephemeris Time (ET) was used in place of TT, between
     * 1984 and 2000 Temps Dynamique Terrestrial (TDT) was used in place of TT.
     * The three time scales, while defined differently, form a continuous time
     * scale for most purposes.  TT has a fixed offset from TAI (Temps Atomique
     * International).
     *
     * <p>This method returns the difference TT - UT in days.  Usually this
     * would be looked up in a table published after the fact.  Here we use
     * polynomial fits for the distant past, for the future and also for the
     * time where the table exists.  Except for 1987 to 2015, the expressions
     * are taken from
     * Jean Meeus, 1991, <I>Astronomical Algorithms</I>, Willmann-Bell, Richmond VA, p.73f.
     * For the present (1987 to 2015 we use our own graphical linear fit to the
     * data 1987 to 2001 from
     * USNO/RAL, 2001, <I>Astronomical Almanach 2003</I>, U.S. Government Printing Office, Washington DC, Her Majesty's Stationery Office, London, p.K9:
     *
     * <p>t = Ep - 2002
     * <p>DeltaT/s = 9.2 * t / 15 + 65
     *
     * <p>Close to the present (1900 to 1987) we use Schmadl and Zech:
     *
     * <p>t = (Ep - 1900) / 100
     * <p>DeltaT/d = -0.000020      + 0.000297 * t
     *    + 0.025184 * t<sup>2</sup> - 0.181133 * t<sup>3</sup><BR>
     *    + 0.553040 * t<sup>4</sup> - 0.861938 * t<sup>5</sup>
     *    + 0.677066 * t<sup>6</sup> - 0.212591 * t<sup>7</sup>
     *
     * <p>This work dates from 1988 and the equation is supposed to be valid only
     * to 1987, but we extend its use into the near future.  For the 19th
     * century we use Schmadl and Zech:
     *
     * <p>t = (Ep - 1900) / 100
     * <p>DeltaT/d = -0.000009      +  0.003844 * t
     *     +  0.083563 * t<sup>2</sup> +  0.865736 * t<sup>3</sup><BR>
     *     +  4.867575 * t<sup>4</sup> + 15.845535 * t<sup>5</sup>
     *     + 31.332267 * t<sup>6</sup> + 38.291999 * t<sup>7</sup><BR>
     *     + 28.316289 * t<sup>8</sup> + 11.636204 * t<sup>9</sup>
     *     +  2.043794 * t<sup>10</sup>
     *
     * <p>Stephenson and Houlden are credited with the equations for times before
     * 1600.  First for the period 948 to 1600:
     *
     * <p>t = (Ep - 1850) / 100
     * <p>DeltaT/s = 22.5 * t<sup>2</sup>
     *
     * <p>and before 948:
     *
     * <p>t = (Ep - 948) / 100
     * <p>DeltaT/s = 1830 - 405 * t + 46.5 * t<sup>2</sup>
     *
     * <p>This leaves no equation for times between 1600 and 1800 and beyond
     * 2015.  For such times we use the equation of Morrison and Stephenson:
     *
     * <p>t = Ep - 1810
     * <p>DeltaT/s = -15 + 0.00325 * t<sup>2</sup> 
     *
     *  @param givenMJD Modified Julian Date (UT)
     *  @return TT minus UT in days
     */
    
    public static double deltaT(double givenMJD)
    {
        double theEpoch; /* Julian Epoch */
        double t; /* Time parameter used in the equations. */
        double D; /* The return value. */
        
        givenMJD -= 50000;
        
        theEpoch = 2000. + (givenMJD - 1545.) / 365.25;
        
    /* For 1987 to 2015 we use a graphical linear fit to the annual tabulation
     * from USNO/RAL, 2001, Astronomical Almanach 2003, p.K9.  We use this up
     * to 2015 about as far into the future as it is based on data in the past.
     * The result is slightly higher than the predictions from that source. */
        
        if (1987 <= theEpoch && 2015 >= theEpoch)
        {
            t = (theEpoch - 2002.);
            D = 9.2 * t / 15. + 65.;
            D /= 86400.;
        }
        
    /* For 1900 to 1987 we use the equation from Schmadl and Zech as quoted in
     * Meeus, 1991, Astronomical Algorithms, p.74.  This is precise within
     * 1.0 second. */
        
        else if (1900 <= theEpoch && 1987 > theEpoch)
        {
            t  = (theEpoch - 1900.) / 100.;
            D = -0.212591 * t * t * t * t * t * t * t
                    + 0.677066 * t * t * t * t * t * t
                    - 0.861938 * t * t * t * t * t
                    + 0.553040 * t * t * t * t
                    - 0.181133 * t * t * t
                    + 0.025184 * t * t
                    + 0.000297 * t
                    - 0.000020;
        }
        
    /* For 1800 to 1900 we use the equation from Schmadl and Zech as quoted in
     * Meeus, 1991, Astronomical Algorithms, p.74.  This is precise within 1.0
     * second. */
        
        else if (1800 <= theEpoch && 1900 > theEpoch)
        {
            t  = (theEpoch - 1900.) / 100.;
            D =  2.043794 * t * t * t * t * t * t * t * t * t * t
                    + 11.636204 * t * t * t * t * t * t * t * t * t
                    + 28.316289 * t * t * t * t * t * t * t * t
                    + 38.291999 * t * t * t * t * t * t * t
                    + 31.332267 * t * t * t * t * t * t
                    + 15.845535 * t * t * t * t * t
                    +  4.867575 * t * t * t * t
                    +  0.865736 * t * t * t
                    +  0.083563 * t * t
                    +  0.003844 * t
                    -  0.000009;
        }
        
    /* For 948 to 1600 we use the equation from Stephenson and Houlden as
     * quoted in Meeus, 1991, Astronomical Algorithms, p.73. */
        
        else if (948 <= theEpoch && 1600 >= theEpoch)
        {
            t  = (theEpoch - 1850.) / 100.;
            D  = 22.5 * t * t;
            D /= 86400.;
        }
        
    /* Before 948 we use the equation from Stephenson and Houlden as quoted
     * in Meeus, 1991, Astronomical Algorithms, p.73. */
        
        else if (948 > theEpoch)
        {
            t  = (theEpoch - 948.) / 100.;
            D  = 46.5 * t * t - 405. * t + 1830.;
            D /= 86400.;
        }
        
    /* Else (between 1600 and 1800 and after 2010) we use the equation from
     * Morrison and Stephenson, quoted as eqation 9.1 in Meeus, 1991,
     * Astronomical Algorithms, p.73. */
        
        else
        {
            t  = theEpoch - 1810.;
            D  = 0.00325 * t * t - 15.;
            D /= 86400.;
        }
        
        return D; // in days
    } // deltaT

    public GregorianCalendar getCurrentGregorianCalendar()
    {
        return currentTime;
    }
    
    // function to take a given Julian date and parse it to a Gerorian Calendar
    public static GregorianCalendar convertJD2Calendar(double jd)
    {
         /**
         * Calculate calendar date for Julian date field this.jd
         */
        Double jd2 = new Double(jd + 0.5);
        long I = jd2.longValue();
        double F = jd2.doubleValue() - (double) I;
        long A = 0;
        long B = 0;

        if (I > 2299160)
        {
            Double a1 = new Double(((double) I - 1867216.25) / 36524.25);
            A = a1.longValue();
            Double a3 = new Double((double) A / 4.0);
            B = I + 1 + A - a3.longValue();
        }
        else
        {
            B = I;
        }

        double C = (double) B + 1524;
        Double d1 = new Double((C - 122.1) / 365.25);
        long D = d1.longValue();
        Double e1 = new Double(365.25 * (double) D);
        long E = e1.longValue();
        Double g1 = new Double((double) (C - E) / 30.6001);
        long G = g1.longValue();
        Double h = new Double((double) G * 30.6001);
        long da = (long) C - E - h.longValue();
        
        Integer date = new Integer((int) da); // DATE

        Integer month;
        Integer year;
        
        if (G < 14L)
        {
            month = new Integer((int) (G - 2L));
        }
        else
        {
            month = new Integer((int) (G - 14L));
        }

        if (month.intValue() > 1)
        {
            year = new Integer((int) (D - 4716L));
        }
        else
        {
            year = new Integer((int) (D - 4715L));
        }

        // Calculate fractional part as hours, minutes, and seconds
        Double dhr = new Double(24.0 * F);
        Integer hour = new Integer(dhr.intValue());
        Double dmin = new Double((dhr.doubleValue() - (double) dhr.longValue()) * 60.0);
        Integer minute = new Integer(dmin.intValue());
        
        Double dsec = new Double((dmin.doubleValue() - (double) dmin.longValue()) * 60.0);
        Integer second=new Integer(dsec.intValue());
        
        //int ms = (int)((dsec.doubleValue() - (double) second.longValue()) * 1000.0);
        // rounding fix - e-mailed to SEG by Hani A. Altwaijry 28 May 2009
        int ms = (int) Math.round((dsec.doubleValue() - (double) second.longValue()) * 1000.0);
        
        // create Calendar object
        GregorianCalendar newTime = new GregorianCalendar(tz); // set default timezone
        
        newTime.set(Calendar.YEAR,year);
        newTime.set(Calendar.MONTH,month);
        newTime.set(Calendar.DATE,date);
        newTime.set(Calendar.HOUR_OF_DAY,hour);
        newTime.set(Calendar.MINUTE,minute);
        newTime.set(Calendar.SECOND,second);
        newTime.set(Calendar.MILLISECOND,ms);
        
        return newTime;
        
    } // convertJD2Calendar
    
    // function used to take a given Julian Date and parse it as a string using the current settings 
    public String convertJD2String(double jd)
    {
        // convert to calendar
        GregorianCalendar newTime = convertJD2Calendar(jd);
        
        // format as String -- ASSUMES dateFromat is not NULL!!
        dateFormat.setTimeZone(tzStringFormat);
        String retStr=dateFormat.format( newTime.getTime() );
        
        return retStr;
        

    } // convertJD2String
    

    
    
}
