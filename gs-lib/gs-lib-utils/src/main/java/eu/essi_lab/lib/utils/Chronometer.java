package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.text.DecimalFormat;

/**
 * @author Fabrizio
 */
public class Chronometer {

    protected long startTime;

    /**
     * @author Fabrizio
     */
    public enum TimeFormat {

	DAYS_HOUR_MIN_SEC_MLS, //
	DAYS_HOUR_MIN_SEC, //
	DAYS_HOUR_MIN, //
	HOUR_MIN_SEC_MLS, //
	HOUR_MIN_SEC, //
	HOUR_MIN, //
	MIN_SEC, MIN_SEC_MLS, //
	SEC_MLS //
    }

    private TimeFormat format;

    /**
     * 
     */
    public Chronometer() {

	this(TimeFormat.MIN_SEC);
    }

    /**
     * @param format
     */
    public Chronometer(TimeFormat format) {

	setTimeFormat(format);
    }

    /**
     * @param millis
     * @param format
     * @return
     */
    public static String formatElapsedTime(long millis, TimeFormat format) {
    
        DecimalFormat decFormat = new DecimalFormat();
    
        decFormat.setMinimumIntegerDigits(2);
        decFormat.setMaximumIntegerDigits(2);
    
        String days = decFormat.format(convertMilliSeconds(millis)[0]);
        String hours = decFormat.format(convertMilliSeconds(millis)[1]);
        String min = decFormat.format(convertMilliSeconds(millis)[2]);
        String sec = decFormat.format(convertMilliSeconds(millis)[3]);
    
        decFormat.setMinimumIntegerDigits(3);
        decFormat.setMaximumIntegerDigits(3);
    
        String mls = decFormat.format(convertMilliSeconds(millis)[4]);
    
        switch (format) {
    
        case DAYS_HOUR_MIN_SEC_MLS:
            return days + ":" + hours + ":" + min + ":" + sec + ":" + mls;
        case DAYS_HOUR_MIN_SEC:
            return days + ":" + hours + ":" + min + ":" + sec;
        case DAYS_HOUR_MIN:
            return days + ":" + hours + ":" + min;
        case HOUR_MIN_SEC_MLS:
            return hours + ":" + min + ":" + sec + ":" + mls;
        case HOUR_MIN_SEC:
            return hours + ":" + min + ":" + sec;
        case HOUR_MIN:
            return hours + ":" + min;
        case MIN_SEC_MLS:
            return min + ":" + sec + ":" + mls;
        case MIN_SEC:
            return min + ":" + sec;
        case SEC_MLS:
            return sec + ":" + mls;
        }
    
        return null;
    }

    /**
     * @param mls
     * @return
     */
    public static long[] convertMilliSeconds(long mls) {
    
        long seconds = mls / 1000;
    
        return new long[] {
    
        	seconds / (24 * 60 * 60), // days
        	(seconds % (24 * 60 * 60)) / (60 * 60), // hours
        	(seconds / 60) % 60, // minutes
        	(seconds % 60), // seconds
        	(mls) // mls
        };
    }

    /**
     * @param format
     */
    public void setTimeFormat(TimeFormat format) {

	this.format = format;
    }

    /**
     * 
     */
    public void start() {

	startTime = System.currentTimeMillis();
    }

    public long getElapsedTimeMillis() {

	return System.currentTimeMillis() - startTime;
    }

    /**
     * 
     */
    public void formatAndPrintElapsedTime() {

	System.out.println("- Elapsed time : [ " + formatElapsedTime() + " ]");
    }

    /**
     * @return
     */
    public String formatElapsedTime() {

	return formatElapsedTime(getElapsedTimeMillis());
    }

    /**
     * @return
     */
    public String formatElapsedTime(long millis) {

	return formatElapsedTime(millis, format);
    }
}
