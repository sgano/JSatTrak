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
// NOTE by SEG --- DOES NOT CONTAIN ABILITY TO HANDLE MILLISECONDS!!!!!!!!!!!
// ALSO MIGHT WANT TO UPDATE TO USE BIGFLOAT/INTERGER
//  to fix no milliseconds... make second field a double not an int!!
// OR add another int for milliseconds
// Updated to reduce rounding error
//
/**
 * Copyright (c) 2002, Raben Systems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the Raben Systems, Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package name.gano.astro;

import java.util.*;
import java.text.*;

/**
 * Routines for calculating and setting Julian day number
 * based on algorithms from Jean Meeus,
 * "Astronomical Algorithms", 2nd Edition, Willmann-Bell, Inc.,
 * 1998.
 *
 * @author Vern Raben (mailto:vern@raben.com), Shawn E. Gano
 * @version $Revision: 1.16 $ $Date: 2002/07/22 14:24:17 $
 */
public final class JulianDay implements java.io.Serializable, Cloneable {
    public final static int JD=100;
    public final static int MJD=101;
    public final static int YEAR=Calendar.YEAR;
    public final static int MONTH=Calendar.MONTH;
    public final static int DATE=Calendar.DATE;
    public final static int HOUR=Calendar.HOUR;
    public final static int HOUR_OF_DAY=Calendar.HOUR_OF_DAY;
    public final static int MINUTE=Calendar.MINUTE;
    public final static int SECOND=Calendar.SECOND;
    public final static int DAY_OF_YEAR=Calendar.DAY_OF_YEAR;
    public final static int DAY_OF_WEEK=Calendar.DAY_OF_WEEK;
    public final static int DAY_OF_MONTH=Calendar.DAY_OF_MONTH;
    public final static int JANUARY=Calendar.JANUARY;
    public final static int FEBRUARY=Calendar.FEBRUARY;
    public final static int MARCH=Calendar.MARCH;
    public final static int APRIL=Calendar.APRIL;
    public final static int MAY=Calendar.MAY;
    public final static int JUNE=Calendar.JUNE;
    public final static int JULY=Calendar.JULY;
    public final static int AUGUST=Calendar.AUGUST;
    public final static int SEPTEMBER=Calendar.SEPTEMBER;
    public final static int OCTOBER=Calendar.OCTOBER;
    public final static int NOVEMBER=Calendar.NOVEMBER;
    public final static int DECEMBER=Calendar.DECEMBER;
    public final static String[] MONTHS={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
    public final static String[] TIME_UNIT={"unk","yr","mo","unk","unk","day","unk","unk","unk","unk","unk","hr","min","sec"};
    public final static double EPOCH_1970=2440587.5;
    public final static String SQL_DATE_FORMAT="yyyy-MM-dd HH:mm:ss";
    private DateFormat dateFormat=null;
    private Integer year=new Integer(0);
    private Integer month=new Integer(0);
    private Integer date=new Integer(0);
    private Integer hour=new Integer(0);
    private Integer minute=new Integer(0);
    private Integer second=new Integer(0);
    private Double jd;
    private Double mjd;
    private Integer dayOfWeek;
    private Integer dayOfYear;
    private final static DecimalFormat fmt4Dig = new DecimalFormat("0000");
    private final static DecimalFormat fmt2Dig = new DecimalFormat("00");
    private final static TimeZone tz=TimeZone.getTimeZone("UTC");
    private static TimeZone tzStringFormat = TimeZone.getTimeZone("UTC");

    /**
    * JulianCalendar constructor - sets JD for current time
    */
    public JulianDay() {
        Calendar cal=new GregorianCalendar(tz);
        setTime(cal.getTime());
    }

    /**
     * JulianCalendar constructor - sets JD passed as double
     * @param jd double The Julian date
     */
    public JulianDay(double jd) {
        set(JulianDay.JD,jd);
        calcCalDate();
    }

    /**
     * Constructor to create Julian day given year, month, and decimal day
     * @param yr int
     * @param mo int
     * @param da double
     */
    public JulianDay(int yr, int mo, double da) {
        int day=(int)da;
        int hr=0;
        int min=0;
        int sec=0;
        double dhr=(da-day)*24.0;
        hr=(int)dhr;
        double dmin=(dhr-hr)*60.0;
        min=(int)(dmin);
        sec=(int)((dmin-min)*60.0);
        set(yr,mo,day,hr,min,sec);
        calcJD();
    }

    /**
     * Construct JulianDate given year, month, and date
     * @param yr int
     * @param mo int
     * @param da int
     */
    public JulianDay(int yr, int mo, int da) {
        int hr=0;
        int min=0;
        int sec=0;

        if (da < 1) {
            da = 1;
        }

        if (mo < 0) {
            mo = 0;
        }

        if (hr < 0) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        }

        if (sec < 0) {
            sec = 0;
        }

        set(yr, mo, da, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDate given year, month, date, hour and minute
     * @param yr int
     * @param mo int
     * @param da int
     */
    public JulianDay(int yr, int mo, int da,int hr,int min) {

        int sec=0;

        if (da < 1) {
            da = 1;
        }

        if (mo < 0) {
            mo = 0;
        }

        if (hr < 0) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        }

        if (sec < 0) {
            sec = 0;
        }

        set(yr, mo, da, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDate given year, month, day, hour, minute, and second
     * @param yr int
     * @param mo int
     * @param da int
     * @param hr int
     * @param min int
     * @param sec int
     */
    public JulianDay(int yr, int mo, int da, int hr, int min, int sec) {

        if (da < 1) {
            da = 1;
        }

        if (mo < 0) {
            mo = 0;
        }

        if (hr < 0) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        }

        if (sec < 0) {
            sec = 0;
        }

        set(yr, mo, da, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDay from system time in milli-seconds since Jan 1, 1970
     * @param systemTimeInMilliSeconds long
     */
    public JulianDay(long timeInMilliSec) {
        setDateTime("1970-01-01 0:00");
        add(JulianDay.DATE,((double)timeInMilliSec/86400000.0));
    }

    /**
     * Copy constructor for JulianDate
     * @param cal com.raben.util.JulianDate
     */
    public JulianDay(JulianDay cal) {
        if (cal!=null) {
            set(Calendar.YEAR,cal.get(Calendar.YEAR));
            set(Calendar.MONTH,cal.get(Calendar.MONTH));
            set(Calendar.DATE,cal.get(Calendar.DATE));
            set(Calendar.HOUR_OF_DAY,cal.get(Calendar.HOUR_OF_DAY));
            set(Calendar.MINUTE,cal.get(Calendar.MINUTE));
            set(Calendar.SECOND,cal.get(Calendar.SECOND));
            calcJD();
        }
        else {
            Calendar calendar=new GregorianCalendar(tz);
            setTime(calendar.getTime());
        }
    }

    /**
     * Set JulianDay from sql database compatible date/time string (yyyy-mm-dd hh:mm:ss)
     * @param dateStr java.lang.String
     */
    public JulianDay(String str) {
        setDateTime(str);
        calcJD();
    }

    /**
     * Construct JulianDate given Calendar as a parameter
     * @param cal java.util.Calendar
     */
    public JulianDay(Calendar cal) {
        set(YEAR,cal.get(YEAR));
        set(MONTH,cal.get(MONTH));
        set(DATE,cal.get(DATE));
        set(HOUR_OF_DAY,cal.get(HOUR_OF_DAY));
        set(MINUTE,cal.get(MINUTE));
        set(SECOND,cal.get(SECOND));
        calcJD();
        calcCalDate();
    }

    /**
     * Add specified value in specified time unit to current Julian Date
     * increments next higher field
     * ISSUE - meaning of incrementing YEAR and MONTH by fractional value is not clear since
     *         period of a month and year varies, that is ignored. Year is assumed to be 365 days and
     *         month is assumed to be 30 days for computing the fractional increment.
     * ISSUE - not thoroughly tested, typically 1-2 second errors may occur
     *         due to round-off. Will be refactored
     *         "real soon  now" :) to utilize BigDecimal internal representation
     *         of Julian Day.
     * @param unit int Time unit
     * @param val int Time increment
     */
    public void add(int unit, double val) {
        double da;

        switch(unit) {
            case YEAR:
                // issue - what this means if its not whole year
                int yr=year.intValue()+(int)val;
                set(YEAR,yr);
                da=(val-(int)val)*365.0;
                set(DATE,da);
                break;
            case MONTH:
                int mo=month.intValue()+(int)val;
                set(MONTH,mo);
                da=(val-(int)val)*30.0;
                set(DATE,da);
                break;

                case DATE:
                    set(JD,getJDN()+val);
                    break;
                case HOUR:
                case HOUR_OF_DAY:
                    set(JD,getJDN()+(double)val/24.0);
                    break;
                case MINUTE:
                    double min=minute.doubleValue()+val;
                    set(JD,getJDN()+(double)val/1440.0);
                    break;
                case SECOND:
                    double sec=second.doubleValue()+val;
                    set(JD,getJDN()+(double)val/86400.0);
                    break;
                default:
                    System.out.println("Error: JulianDate.add: The 'unit' parameter is not recognized="+unit);
                    set(JD,getJDN()+val);
                    break;
            }

            calcJD();

    }

    /**
     * Add specified value in specified time unit to current Julian Date
     * increments next higher field
     *
     * ISSUE - meaning of incrementing YEAR and MONTH by fractional value is not clear since
     *         period of a month and year varies, that is ignored. Year is assumed to be 365 days and
     *         month is assumed to be 30 days for computing the fractional increment.
     * ISSUE - not thoroughly tested, typically 1-2 second errors may occur
     *         due to round-off. Will be refactored
     *         "real soon  now" :) to utilize BigDecimal internal representation
     *         of Julian Day.
     * @param unit int Time unit
     * @param val int Time increment
     */
    public void add(int unit, int val) {
        int yr;
        int mo;
        switch(unit) {
            case YEAR:
                yr=year.intValue()+val;
                set(YEAR,yr);
                break;
            case MONTH:
                mo=month.intValue()+val;

                while (mo>=12) {
                    mo-=12;
                    yr=year.intValue()+1;
                    set(YEAR,yr);
                }

                while (mo<0) {
                    mo+=12;
                    yr=year.intValue()-1;
                    set(YEAR,yr);
                }

                set(MONTH,mo);
                break;

            case DATE:
                set(JD,getJDN()+val);
                break;
            case HOUR:
            case HOUR_OF_DAY:
                set(JD,getJDN()+val*0.041667);
                break;

            case MINUTE:
                set(JD,getJDN()+(double)val/1440.0);
                break;

            case SECOND:
                set(JD,getJDN()+(double)val/86400.0);
                break;
            default:
                System.out.println("Error: JulianDate.add: The 'unit' parameter is not recognized="+unit);
                set(JD,getJDN()+val); // default to adding days
                break;
        }

        calcJD();

    }

    /**
      * Calculate calendar date for Julian date field this.jd
      */
    private void calcCalDate() {

        Double jd2=new Double(jd.doubleValue()+0.5);
	long I=jd2.longValue();
	double F=jd2.doubleValue()-(double)I;
	long A=0;
	long B=0;

        if (I>2299160) {
            Double a1=new Double(((double)I-1867216.25)/36524.25);
            A=a1.longValue();
            Double a3=new Double((double)A/4.0);
            B=I+1+A-a3.longValue();
        }
        else {
            B=I;
        }

        double C=(double)B+1524;
        Double d1=new Double((C-122.1)/365.25);
        long D=d1.longValue();
        Double e1=new Double(365.25*(double)D);
        long E=e1.longValue();
        Double g1=new Double((double)(C-E)/30.6001);
        long G=g1.longValue();
        Double h=new Double((double)G*30.6001);
        long da=(long)C-E-h.longValue();
        date=new Integer((int)da);

        if (G<14L) {
            month=new Integer((int)(G-2L));
        }
        else {
            month=new Integer((int)(G-14L));
        }

        if (month.intValue()>1) {
            year=new Integer((int)(D-4716L));
        }
        else {
            year=new Integer((int)(D-4715L));
        }

        // Calculate fractional part as hours, minutes, and seconds
        Double dhr=new Double(24.0*F);
        hour=new Integer(dhr.intValue());
        Double dmin=new Double((dhr.doubleValue()-(double)dhr.longValue())*60.0);
        minute=new Integer(dmin.intValue());
        Double dsec=new Double((dmin.doubleValue()-(double)dmin.longValue())*60.0);
        second=new Integer(dsec.intValue());

    }

 /**
  * Calculate day of week class attribute for class attribute jd
  */
private void calcDayOfWeek() {
	JulianDay nJd=new JulianDay(getJDN());
	nJd.setStartOfDay();
	double nJdn=nJd.getJDN()+1.5;
	int dow=(int)(nJdn%7);
	dayOfWeek=new Integer(dow);
 }
  /**
   * Calculate day of year for jd (jd is a class attribute)
   */
  private void calcDayOfYear() {
	  JulianDay julCal=new JulianDay();
	  julCal.set(year.intValue(),0,1);
	  double doy=jd.doubleValue()-julCal.getJDN();
	  int idoy=(int)doy;
	  dayOfYear=new Integer(idoy);
  }
  /**
   * Calculate Julian Date class attribute for class attributes year, month,
   * date, hour, minute, and second
   */
private void calcJD() {
	int mo=month.intValue()+1;
	int da=date.intValue();
	int yr=year.intValue();
	int A=0;
	int B=0;
	int C=0;
	int D=0;

	if (mo<=2) {
		yr--;
		mo+=12;
	}
	else {
		mo=month.intValue()+1;
	}

	if ((year.intValue()>1582)||((year.intValue()==1582)&&(month.intValue()>=10)&&(date.intValue()>=15))) {
	  Double a1=new Double((double)yr/100.0);
	  A=a1.intValue();
	  Double b1=new Double((double)A/4.0);
	  B=2-A+b1.intValue();
	}
	else {
		B=0;
	}

	Double c1=new Double(365.25*(double)yr);
	if (yr<0) {
		c1=new Double(365.25*(double)yr-0.75);
	}

	C=c1.intValue();
	Double d1=new Double(30.6001*(mo+1));
	D=d1.intValue();

	double jdd=B+C+D+da + (hour.doubleValue()/24.0)+
	  (minute.doubleValue()/1440.0)+(second.doubleValue()/86400.0)+
	    1720994.5;
	jd=new Double(jdd);

}
/**
 * Returns time difference in days between date specified and the JulianDay of this object
 * (parameter date-this date)
 * @return double
 * @param date com.raben.util.JulianDate
 */
public double diff(JulianDay date) {
	return date!=null ? date.getJDN()-getJDN() : Double.NaN;
}
/**
 * Returns true if Julian day number is within 0.001 of parameter jd
 * @return boolean
 * @param jd double
 */
public boolean equals(double jd) {
	return Math.abs(jd-getJDN())<0.001 ? true : false;
}
/**
 * Return true if JulianDates are equal, false otherwise
 * @return boolean
 * @param date com.raben.util.JulianDate
 */
public boolean equals(JulianDay date) {
	boolean retVal=false;

	if (date!=null) {
		retVal=equals(date.getJDN());
	}

	return retVal;

}
/**
 * Returns the specified field
 * @param field int The specified field
 * @return int The field value
 */
public final int get(int field) {

	switch(field) {
	case YEAR:
			return year.intValue();
	case MONTH:
			return month.intValue();
	case DAY_OF_MONTH:
			return date.intValue();
	case HOUR:
			int hr=hour.intValue();
			hr=hr > 12 ? hr-=12:hr;
			return hr;
	case HOUR_OF_DAY:
		return hour.intValue();
	case MINUTE:
		return minute.intValue();
	case SECOND:
		return second.intValue();
	case DAY_OF_WEEK:
		calcDayOfWeek();
		return dayOfWeek.intValue();
	case DAY_OF_YEAR:
		calcDayOfYear();
		return dayOfYear.intValue();
	default:
		return -1; // ISSUE - should throw exception? - what does Calendar do?
	}

}
/*
 * Get the UTC date/time string in the format yyyy-mm-dd hh:mm:ss
 * If the dateFormat is set, the date must be more recent than Jan 1, 1970
 * otherwise the empty string "" will be returned.)
 * @return java.lang.String
 */
public String getDateTimeStr() {
    String retStr="";

    if ((dateFormat!=null)&&(getJDN()>=EPOCH_1970)) 
    {
        dateFormat.setTimeZone(tzStringFormat);
        retStr=dateFormat.format(getTime());
    }
    else {
	StringBuffer strBuf = new StringBuffer(fmt4Dig.format(get(JulianDay.YEAR)));
	strBuf.append("-");
	strBuf.append(fmt2Dig.format(get(JulianDay.MONTH)+1));
	strBuf.append("-");
	strBuf.append(fmt2Dig.format(get(JulianDay.DATE)));
	strBuf.append(" ");
	strBuf.append(fmt2Dig.format(get(JulianDay.HOUR_OF_DAY)));
	strBuf.append(":");
	strBuf.append(fmt2Dig.format(get(JulianDay.MINUTE)));
	strBuf.append(":");
	strBuf.append(fmt2Dig.format(get(JulianDay.SECOND)));
	retStr=strBuf.toString();
    }
    return retStr;
}
  /**
   * Returns the Julian Date Number as a double
   * @return double
   * @param field int
   */
   public final double getJDN() {
	   if (jd==null) {
		   calcJD();
	   }

		calcJD();
  
	   return jd.doubleValue();
   }
/**
 * Returns milli-seconds since Jan 1, 1970
 * @return long
 */
public long getMilliSeconds() {
	//JulianDay jd1970=new JulianDay("1970-01-01 0:00");
	//double diff=getJDN()-jd1970.getJDN();
        double diff=getJDN()-EPOCH_1970;
	return (long)(diff*86400000.0);
}
/**
  * Return the modified Julian date
  * @return double
  * @param field int
  */
public final double getMJD() {

	return (getJDN() - 2400000.5);
}
/**
 * Return date as YYYYMMDDHHSS string with the least unit to be returned specified
 * For example to to return YYYYMMDD specify least unit as JulianDay.DATE
 * @param leastUnit int least unit to be returned
 */
public String getYMD(int leastUnit) {

	StringBuffer retBuf = new StringBuffer();
	int yr = get(JulianDay.YEAR);
	int mo = get(JulianDay.MONTH) + 1;
	int da = get(JulianDay.DATE);
	int hr = get(JulianDay.HOUR_OF_DAY);
	int min = get(JulianDay.MINUTE);
	int sec = get(JulianDay.SECOND);

	String yrStr = fmt4Dig.format(yr);

	String moStr = fmt2Dig.format(mo);
	String daStr = fmt2Dig.format(da);
	String hrStr = fmt2Dig.format(hr);
	String minStr = fmt2Dig.format(min);
	String secStr = fmt2Dig.format(sec);

	switch (leastUnit) {
		case JulianDay.YEAR :
			retBuf.append(yrStr);
			break;

		case JulianDay.MONTH :
			retBuf.append(yrStr);
			retBuf.append(moStr);
			break;

		case JulianDay.DATE :
			retBuf.append(yrStr);
			retBuf.append(moStr);
			retBuf.append(daStr);
			break;

		case JulianDay.HOUR_OF_DAY :
		case JulianDay.HOUR :
			retBuf.append(yrStr);
			retBuf.append(moStr);
			retBuf.append(daStr);
			retBuf.append(hrStr);
			break;

		case JulianDay.MINUTE :
			retBuf.append(yrStr);
			retBuf.append(moStr);
			retBuf.append(daStr);
			retBuf.append(hrStr);
			retBuf.append(minStr);
			break;

		case JulianDay.SECOND :
			retBuf.append(yrStr);
			retBuf.append(moStr);
			retBuf.append(daStr);
			retBuf.append(hrStr);
			retBuf.append(minStr);
			retBuf.append(secStr);
			break;
	}

	return retBuf.toString();

}
/**
  * This method sets Julian day or modified Julian day
  * @param field int Field to be changed
  * @param value double The value the field is set to
  * ISSUE - double values are truncated when setting
  * YEAR, MONTH<DATE, HOUR,MINUTE, and SECOND - this is not
  * what should happen. (Should be able to set date to 1.5 to be
  * the 1st day of month plus 12 hours).
  */
public void set(int field,double value) {
	int ivalue=(int)value;

	switch(field) {

		case JD:
			jd=new Double(value);
			calcCalDate();
			break;

		case MJD:
			jd=new Double(value+2400000.5);
			calcCalDate();
			break;

		case YEAR:
			year=new Integer(ivalue);
			calcJD();
			break;

		case MONTH:
			if (ivalue>11) {
				int yr=year.intValue()+1;
				set(YEAR,ivalue);
				ivalue-=11;
			}
			month=new Integer(ivalue);
			calcJD();
			break;

		case DATE:
				date=new Integer(ivalue);
				calcJD();
				break;

		case HOUR_OF_DAY:
		case HOUR:
			  hour=new Integer(ivalue);
			  while (hour.intValue()>=24) {
				  add(DATE,1);
				  hour=new Integer(hour.intValue()-24);
			  }
			  calcJD();
			  break;

		case MINUTE:
			  minute=new Integer(ivalue);
			  while (minute.intValue()>=60) {
				  add(HOUR,1);
				  minute=new Integer(minute.intValue()-60);
			  }
			  calcJD();
			  break;

		case SECOND:
			  second=new Integer(ivalue);
			  while (second.intValue()>=60) {
				  add(MINUTE,1);
				  second=new Integer(second.intValue()-60);
			  }
			  calcJD();
			  break;

	}

  }
  /**
   * Set various JulianCalendar fields
   * Example:
   *    JulianDay jd=new JulianDay();
   *    jd.set(Calendar.YEAR,1999);
   * @param field int The field to be set
   * @param value int The field value
   */
  public final void set(int field,int value) {

	  switch (field) {
		  case YEAR:
			  year=new Integer(value);
			  break;

		  case MONTH:
			  month=new Integer(value);
			  break;

		  case DATE:
			  date=new Integer(value);
			  break;

		  case HOUR_OF_DAY:
		  case HOUR:
			  hour=new Integer(value);
			  break;

		  case MINUTE:
			  minute=new Integer(value);
			  break;

		  case SECOND:
			  second=new Integer(value);
			  break;
	  }
	  calcJD();

}
/**
 * Set year, month, and day
 * @param year int
 * @param month int Note - January is 0, December is 11
 * @param date int
 */
public final void set(int year,int month,int date) {
	this.year=new Integer(year);
	this.month=new Integer(month);
	this.date=new Integer(date);
	this.hour=new Integer(0);
	this.minute=new Integer(0);
	this.second=new Integer(0);
	calcJD();
}
/**
 * Set year, month,day, hour and minute
 * @param year int
 * @param month int January is 0, Dec is 11
 * @param date int
 * @param hour int
 * @param minute int
 */
public final void set(int year,int month,int date,int hour,int minute) {
	this.year=new Integer(year);
	this.month=new Integer(month);
	this.date=new Integer(date);
	this.hour=new Integer(hour);
	this.minute=new Integer(minute);
	this.second=new Integer(0);
	calcJD();
}
/**
 * Set year month, day, hour, minute and second
 * @param year int
 * @param month int January is 0, December is 11
 * @param date int
 * @param hour int
 * @param minute int
 * @param second int
 */
public final void set(int year,int month,int date,int hour,int minute, int second) {
	this.year=new Integer(year);
	this.month=new Integer(month);
	this.date=new Integer(date);
	this.hour=new Integer(hour);
	this.minute=new Integer(minute);
	this.second=new Integer(second);
	calcJD();
}
/**
 * Set date/time from string
 * @param str java.lang.String
 */
public void setDateTime(String str) {
	try {
		int vals[]={0,0,0,0,0,0};
                str=str.replace('T',' ');
		StringTokenizer tok=new StringTokenizer(str,"/:- ");

		if (tok.countTokens()>0) {

			// Check if its not a database time format yyyy-mm-dd
			int j=str.indexOf("-");

			if ((j==-1)&&(tok.countTokens()==1)) {
				setYMD(str);
			}
			else {
				int i=0;

				while(tok.hasMoreTokens()) {
					vals[i++]=Integer.parseInt(tok.nextToken());
				}

				set(vals[0],vals[1]-1,vals[2],vals[3],vals[4],vals[5]);

			}

		}

	} catch (NumberFormatException e) {
		throw new Error(e.toString());
	}

	calcJD();


}
/**
 * set hour to 23, minute and second to 59
 */
public void setEndOfDay() {
	int yr=get(YEAR);
	int mo=get(MONTH);
	int da=get(DATE);
	set(yr,mo,da,23,59,59);
}
/**
 * Set hour,minute, and second to 0
 */
public void setStartOfDay() {
	int yr=get(YEAR);
	int mo=get(MONTH);
	int da=get(DATE);
	set(yr,mo,da,0,0,0);
}
/**
 * Set date from Java Date
 * @param date java.util.Date
 */
public final void setTime(Date dat) {
	Calendar cal=new GregorianCalendar(tz);
	cal.setTime(dat);
	year=new Integer(cal.get(Calendar.YEAR));
	month=new Integer(cal.get(Calendar.MONTH));
	date=new Integer(cal.get(Calendar.DATE));
	hour=new Integer(cal.get(Calendar.HOUR_OF_DAY));
	minute=new Integer(cal.get(Calendar.MINUTE));
	second=new Integer(cal.get(Calendar.SECOND));
	//System.out.println("JulianCalendar.setTime: year="+year+" month="+month+" date="+date+" hour="+hour+" minute="+minute+" second="+second);
	calcJD();
	//System.out.println("jd="+jd);
}
/**
 * Set date from sting in the form YYYYMMDDhhmmss (YYYY=year MM=month DD=day hh=hr mm=min ss=sec)
 * @param str java.lang.String
 */
public void setYMD(String str) {

	int vals[]={0,0,0,0,0,0};

	if (str.length() >= 4) {
		vals[0] = Integer.parseInt(str.substring(0, 4));
	}
	if (str.length() >= 6) {
		vals[1] = Integer.parseInt(str.substring(4, 6));
	}

	if (str.length() >= 8) {
		vals[2] = Integer.parseInt(str.substring(6, 8));
	}

	if (str.length() >= 10) {
		vals[3] = Integer.parseInt(str.substring(8, 10));
	}
	if (str.length() >= 12) {
		vals[4] = Integer.parseInt(str.substring(10, 12));
	}

	if (str.length() >=14) {
		vals[5] = Integer.parseInt(str.substring(12,14));
	}

	set(YEAR,vals[0]);
	set(MONTH,vals[1]-1);
	set(DATE,vals[2]);
	set(HOUR_OF_DAY,vals[3]);
	set(MINUTE,vals[4]);
	set(SECOND,vals[5]);
}
public final String toString() {

	StringBuffer buf=new StringBuffer("JulianDay[jdn=");
	buf.append(getJDN());
	buf.append(",yr=");
	buf.append(get(Calendar.YEAR));
	buf.append(",mo=");
	buf.append(get(Calendar.MONTH));
	buf.append(",da=");
	buf.append(get(Calendar.DATE));
	buf.append(",hr=");
	buf.append(get(Calendar.HOUR_OF_DAY));
	buf.append(",min=");
	buf.append(get(Calendar.MINUTE));
	buf.append(",sec=");
	buf.append(get(Calendar.SECOND));
        buf.append(",dayOfWeek=");
        buf.append(get(DAY_OF_WEEK));
        buf.append(",dayOfYear=");
        buf.append(get(DAY_OF_YEAR));
        buf.append("]");

	return buf.toString();
  }

/**
 * Return clone of JulianDay object
 * @return Object;
 */
public Object clone() {
    JulianDay clone=null;
    try {
        clone = (JulianDay) super.clone();
    }
    catch (CloneNotSupportedException e) {
        e.printStackTrace();
    }
    return clone;
}

/**
 * Set SimpleDateFormat string
 * ISSUE - only valid after Jan 1, 1970
 */
public void setDateFormat(java.lang.String formatStr) {
    if ((formatStr!=null)&&(formatStr.length()>0)) {
        dateFormat=new SimpleDateFormat(formatStr);
    }
}

/**
 * Set SimpleDateFormat for displaying date/time string
 * @param dateFormat SimpleDateFormat
 */
public void setDateFormat(SimpleDateFormat dateFormat) {
    this.dateFormat=dateFormat;
}

/**
 * Return Java Date
 * @return Date
 */
public Date getTime() {
    return new Date(getMilliSeconds());
}

/**
 * Update JulianDay to current time
 */
public void update() {
    Calendar cal=new GregorianCalendar(tz);
    setTime(cal.getTime());
}

    /**
     * Get increment in days given time unit and increment
     * @param unit Time unit (DATE,HOUR,HOUR_OF_DAY,MINUTE, or SECOND
     * @param incr Time increment in unit specified
     * @return double Increment in days
     * @exception If unit is not Julian.DATE, HOUR, HOUR_OF_DAY, MINUTE or SECOND
     */
    public static double getIncrement(int unit,int incr) {
        double retVal=0.0;

        switch(unit) {
            case DATE:
                retVal=incr;
                break;
            case HOUR:
            case HOUR_OF_DAY:
                retVal=incr/24.0;
                break;
            case MINUTE:
                retVal=incr/1440.0;
                break;
            case SECOND:
                retVal=incr/86400.0;
                break;
            default:
                StringBuffer errMsg=new StringBuffer("JulianDay.getIncrement unit=");
                errMsg.append(unit);

                if ((unit>0)&&(unit<TIME_UNIT.length)) {
                    errMsg.append(" (");
                    errMsg.append(TIME_UNIT[unit]);
                    errMsg.append(" )");
                }

                throw new IllegalArgumentException(errMsg.toString());

        }

        return retVal;
    }

    /**
     * Get java Calendar equivalent of Julian Day
     * @return Calendar
     */
    public java.util.Calendar getCalendar() {
        Calendar cal=GregorianCalendar.getInstance(tz);
        cal.set(get(YEAR),get(MONTH),get(DATE),get(HOUR_OF_DAY),
            get(MINUTE),get(SECOND));
        return cal;
    }

    public static TimeZone getTzStringFormat()
    {
        return tzStringFormat;
    }

    public static void setTzStringFormat(TimeZone aTzStringFormat)
    {
        tzStringFormat = aTzStringFormat;
    }

}
