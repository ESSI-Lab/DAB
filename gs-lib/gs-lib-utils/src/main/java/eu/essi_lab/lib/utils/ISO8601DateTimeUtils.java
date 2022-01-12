package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class ISO8601DateTimeUtils {

    private static final String ISO_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final String NOT_STANDARD = "yyyyMMdd";

    private ISO8601DateTimeUtils() {
	// force static usage
    }

    public static void setGISuiteDefaultTimeZone() {
	TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    public static String getISO8601DateTime() {

	Date now = new Date();
	return getISO8601DateTime(now);
    }

    /**
     * @return
     */
    public static String getISO8601DateTimeWithMilliseconds() {

	Date now = new Date();
	return getISO8601DateTimeWithMilliseconds(now);
    }

    /**
     * @return
     */
    public static String getISO8601Date() {

	Date now = new Date();
	return getISO8601Date(now);
    }

    /**
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static String getISO8601DateTime(int year, int month, int day) {

	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

	TimeZone timeZone = TimeZone.getTimeZone("UTC");
	Calendar calendar = Calendar.getInstance(timeZone);
	calendar.set(year, month - 1, day);

	return dateFormat.format(calendar.getTime());
    }

    /**
     * @param year
     * @param month
     * @param day
     * @param hoursHH
     * @param minutes
     * @param seconds
     * @param millis
     * @return
     */
    public static String getISO8601DateTimeWithMilliseconds(int year, int month, int day, int hoursHH, int minutes, int seconds,
	    int millis) {

	DateFormat dateFormat = new SimpleDateFormat(ISO_WITH_MILLIS, Locale.US);
	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

	TimeZone timeZone = TimeZone.getTimeZone("UTC");
	Calendar calendar = Calendar.getInstance(timeZone);
	calendar.set(year, month - 1, day, hoursHH, minutes, seconds);
	calendar.set(Calendar.MILLISECOND, millis);

	return dateFormat.format(calendar.getTime());
    }

    /**
     * @param year
     * @param month
     * @param day
     * @param hoursHH
     * @param minutes
     * @param seconds
     * @param millis
     * @return
     */
    public static Date getISO8601DateTimeWithMillisecondsAsDate(int year, int month, int day, int hoursHH, int minutes, int seconds,
	    int millis) {

	DateFormat dateFormat = new SimpleDateFormat(ISO_WITH_MILLIS, Locale.US);
	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

	TimeZone timeZone = TimeZone.getTimeZone("UTC");
	Calendar calendar = Calendar.getInstance(timeZone);
	calendar.set(year, month - 1, day, hoursHH, minutes, seconds);
	calendar.set(Calendar.MILLISECOND, millis);

	return calendar.getTime();
    }

    /**
     * @param date
     * @return
     */
    public static String getISO8601Date(Date date) {
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	return dateFormat.format(date);
    }

    /**
     * @param date
     * @return
     */
    public static String getISO8601DateTime(Date date) {
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	return dateFormat.format(date);
    }

    /**
     * @param date
     * @return
     */
    public static String getISO8601DateTimeWithMilliseconds(Date date) {
	DateFormat dateFormat = new SimpleDateFormat(ISO_WITH_MILLIS, Locale.US);
	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	return dateFormat.format(date);
    }

    /**
     * Parses a string expressed as a ISO8601 date time (e.g.: "2015-02-02T01:05:05Z") or ISO date (e.g.: "2015-02-02")
     * This is deprecated
     * in favour of {@link #parseISO8601ToDate(String)} which returns an Optional
     *
     * @deprecated
     * @param dateTimeString
     * @return
     * @throws ParseException
     */
    @Deprecated
    public static Date parseISO8601(String dateTimeString) throws IllegalArgumentException {

	DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser().withChronology(ISOChronology.getInstance(DateTimeZone.UTC));
	DateTime parsed = parser.parseDateTime(dateTimeString);
	parsed = parsed.toDateTime(DateTimeZone.UTC);

	/**
	 * before JodaTime we used JAXB parser.. but it proved insufficient
	 * e.g. for treating time zones such as +0700 (it can parse only +07:00)
	 * Calendar parsed = DatatypeConverter.parseDateTime(timeString.trim());
	 */

	return parsed.toDate();
    }

    public static Optional<Date> parseISO8601ToDate(String dateTimeString) {

	if (dateTimeString == null) {
	    return Optional.empty();
	}
	try {
	    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser().withChronology(ISOChronology.getInstance(DateTimeZone.UTC));
	    DateTime parsed = parser.parseDateTime(dateTimeString);
	    parsed = parsed.toDateTime(DateTimeZone.UTC);
	    /**
	     * before JodaTime we used JAXB parser.. but it proved insufficient
	     * e.g. for treating time zones such as +0700 (it can parse only +07:00)
	     * Calendar parsed = DatatypeConverter.parseDateTime(timeString.trim());
	     */
	    return Optional.of(parsed.toDate());
	} catch (RuntimeException e) {
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Unparsable Date: {}", dateTimeString, e);
	}

	return Optional.empty();
    }

    public static Duration getDuration(String lexicalRepresentation) {
	DatatypeFactory df = null;
	try {
	    df = DatatypeFactory.newInstance();
	} catch (javax.xml.datatype.DatatypeConfigurationException e) {

	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Can't instantiate DatatypeFactory", e);

	    return null;
	}
	return df.newDuration(lexicalRepresentation.trim());

    }

    public static Date subtractDuration(Date date, Duration duration) {
	return addDuration(date, duration.negate());
    }
    
    public static Date addDuration(Date date, Duration duration) {
	XMLGregorianCalendar calendar;
	try {
	    calendar = getXMLGregorianCalendar(date);
	} catch (DatatypeConfigurationException e) {
	    e.printStackTrace();
	    return null;
	}
	calendar.add(duration);
	return calendar.toGregorianCalendar().getTime();
    }

    public static XMLGregorianCalendar getXMLGregorianCalendar(Date date) throws DatatypeConfigurationException {
	GregorianCalendar c = new GregorianCalendar();
	c.setTimeZone(TimeZone.getTimeZone("GMT"));
	c.setTime(date);
	XMLGregorianCalendar ret = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
	return ret;
    }

    // parse date in format yyyyMMdd
    // return the ISO Date
    public static Optional<Date> parseNotStandardToDate(String dateTimeString) throws ParseException {

	try {
	    SimpleDateFormat dateFormat = new SimpleDateFormat(NOT_STANDARD);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

	    Date date = dateFormat.parse(dateTimeString);

	    return Optional.of(date);

	} catch (RuntimeException e) {
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Unparsable Date: {}", dateTimeString, e);
	}

	return Optional.empty();
    }

    public static void main(String[] args) throws Exception {
	Optional<Date> res = ISO8601DateTimeUtils.parseNotStandardToDate("20110309");
	Date date = res.get();
	System.out.println(date);
	Duration duration = getDuration("P1M");
//	duration = duration.negate();
	Date result = subtractDuration(date, duration);
	System.out.println(result);
	
    }

}
