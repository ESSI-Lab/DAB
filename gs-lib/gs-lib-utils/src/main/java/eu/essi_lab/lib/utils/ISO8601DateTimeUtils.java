package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
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

    public static final String ISO_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final String NOT_STANDARD = "yyyyMMdd";

    private static final String NOT_STANDARD2 = "yyyyMMddHHmm";

    private static final String NOT_STANDARD3 = "yyyy";

    private static DatatypeFactory df = null;

    static {

	try {
	    df = DatatypeFactory.newInstance();
	} catch (javax.xml.datatype.DatatypeConfigurationException e) {
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).error(e);
	}
    }

    private ISO8601DateTimeUtils() {
	// force static usage
    }

    /**
     * 
     */
    public static long EPOCH = ISO8601DateTimeUtils.parseISO8601ToDate("1970-01-01T00:00:00Z").get().getTime();

    /**
     * 
     */
    public static long MIN_REASONABLE_DATE = ISO8601DateTimeUtils.parseISO8601ToDate("1000-01-01T00:00:00Z").get().getTime();

    /**
     * 
     */
    public static void setGISuiteDefaultTimeZone() {

	TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    /**
     * @return
     */
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
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Unparsable Date: {}", dateTimeString);
	}

	return Optional.empty();
    }

    public static Duration getDuration(BigDecimal value, String timeUnits) {

	BigInteger bigIntegerExact = null;
	try {
	    bigIntegerExact = value.toBigIntegerExact();

	} catch (ArithmeticException ex) {

	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).error(ex);
	    return null;
	}

	switch (timeUnits.toLowerCase()) {
	case "seconds":
	case "second":
	case "secs":
	case "sec":
	    return getDuration("PT" + value + "S");
	case "milliseconds":
	case "millisecond":
	case "millisec":
	case "millisecs":
	    return getDuration("PT" + value.divide(new BigDecimal("1000")).toString() + "S");
	case "m":
	case "min":
	case "mins":
	case "minute":
	case "minutes":
	    return getDuration("PT" + bigIntegerExact + "M");
	case "hours":
	case "hour":
	case "h":
	    return getDuration("PT" + bigIntegerExact + "H");
	case "days":
	case "day":
	case "d":
	    return getDuration("P" + bigIntegerExact + "D");
	case "weeks":
	case "week":
	    return getDuration("P" + bigIntegerExact + "W");
	case "months":
	case "month":
	    return getDuration("P" + bigIntegerExact + "M");
	case "y":
	case "years":
	case "year":
	case "common year":
	case "common years":
	    return getDuration("P" + bigIntegerExact + "Y");
	default:
	    break;
	}
	return null;
    }

    public synchronized static Duration getDuration(String lexicalRepresentation) {

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
    public static Optional<Date> parseNotStandardToDate(String dateTimeString) {

	return parseToDate(dateTimeString, NOT_STANDARD);
    }

    // parse date in format yyyyMMddHHmm
    // return the ISO Date
    public static Optional<Date> parseNotStandard2ToDate(String dateTimeString) {

	return parseToDate(dateTimeString, NOT_STANDARD2);
    }

  

    /**
     * @param dateTimeString
     * @param pattern
     * @return
     */
    public static Optional<Date> parseToDate(String dateTimeString, String pattern) {

	try {
	    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

	    Date date = dateFormat.parse(dateTimeString);

	    return Optional.of(date);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Unparsable Date: {}", dateTimeString);
	}

	return Optional.empty();

    }

    /**
     * Given a date time expressed in ISO8601 (e.g.: 2021-08-04T10:40:00) which refers to the given
     * <code>dateTimeZone</code>,
     * this method creates the related
     * {@link Date} object which refers to the UTC time zone (since when the GI-Project is started, UTC is set
     * as default time zone).
     * So if we are in Italy in summer, with local time zone GMT+02:00 DST (Europe/Berlin), and
     * <code>iso8601dateTime</code> is
     * '2021-08-04T10:40:00', this method returns a GMT Date 'Wednesday 4 August 2021 08:40:00'
     * 
     * @param iso8601dateTime
     * @param dateTimeZone
     * @return
     */
    public static Date toGMTDateTime(String iso8601dateTime, DateTimeZone dateTimeZone) {

	DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser().withChronology(ISOChronology.getInstance(dateTimeZone));

	DateTime parsed = parser.parseDateTime(iso8601dateTime);
	parsed = parsed.toDateTime(dateTimeZone);

	return parsed.toDate();
    }

    /**
     * @param iso8601DateTime
     * @param dateTimeZone
     * @return
     */
    public static DateTime toDateTime(String iso8601DateTime, DateTimeZone dateTimeZone) {

	DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser().withChronology(ISOChronology.getInstance(dateTimeZone));

	DateTime parsed = parser.parseDateTime(iso8601DateTime);

	return parsed.toDateTime(dateTimeZone);
    }

    /**
     * @param date
     * @param dateTimeZone
     * @return
     */
    public static DateTime toDateTime(Date date, DateTimeZone dateTimeZone) {

	String iso8601DateTime = getISO8601DateTime(date);

	return toDateTime(iso8601DateTime, dateTimeZone);
    }

    public static void main(String[] args) throws Exception {

	TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

	System.out.println(toDateTime(new Date(), DateTimeZone.forID("Europe/Berlin")));

    }

    public static SimpleEntry<BigDecimal, String> getUnitsValueFromDuration(Duration duration) {
	if (duration == null) {
	    return null;
	}
	BigDecimal decimal = null;
	String units = null;
	int seconds = duration.getSeconds();
	int minutes = duration.getMinutes();
	int hours = duration.getHours();
	int days = duration.getDays();
	int months = duration.getMonths();
	int years = duration.getYears();

	if (seconds > 0) {
	    units = "seconds";
	    decimal = new BigDecimal(seconds);
	} else if (minutes > 0) {
	    units = "minutes";
	    decimal = new BigDecimal(minutes);
	} else if (hours > 0) {
	    units = "hours";
	    decimal = new BigDecimal(hours);
	} else if (days > 0) {
	    units = "days";
	    decimal = new BigDecimal(days);
	} else if (months > 0) {
	    units = "months";
	    decimal = new BigDecimal(months);
	} else if (years > 0) {
	    units = "years";
	    decimal = new BigDecimal(years);
	}

	SimpleEntry<BigDecimal, String> ret = new SimpleEntry<>(decimal, units);
	return ret;
    }

    public static String getTimeUnitsAbbreviation(String timeUnits) {
	if (timeUnits == null) {
	    return null;
	}
	switch (timeUnits.toLowerCase()) {
	case "seconds":
	case "second":
	case "secs":
	case "sec":
	    return "sec";
	case "milliseconds":
	case "millisecond":
	case "millisec":
	case "millisecs":
	    return "ms";
	case "m":
	case "min":
	case "mins":
	case "minute":
	case "minutes":
	    return "min";
	case "hours":
	case "hour":
	case "h":
	    return "h";
	case "days":
	case "day":
	case "d":
	    return "d";
	case "weeks":
	case "week":
	    return "w";
	case "months":
	case "month":
	    return "months";
	case "y":
	case "years":
	case "year":
	case "common year":
	case "common years":
	    return "years";
	default:
	    break;
	}
	return null;
    }

}
