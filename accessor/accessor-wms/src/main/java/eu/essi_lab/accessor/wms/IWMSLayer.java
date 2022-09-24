package eu.essi_lab.accessor.wms;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.xml.datatype.Duration;
// import javax.xml.datatype.Duration;
import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.data.CRS;
import net.opengis.gml.v_3_2_0.EnvelopeType;

public abstract class IWMSLayer {

    boolean continuePeriod = true;
    TreeSet<Date> dates = new TreeSet<>();

    public abstract String getName();

    public abstract String getTitle();

    public abstract String getAbstract();

    public abstract List<String> getKeywords();

    public abstract boolean isSubsettable() throws XPathExpressionException;

    public abstract Integer getFixedWidth() throws XPathExpressionException;

    public abstract Integer getFixedHeight() throws XPathExpressionException;

    public abstract List<EnvelopeType> getEnvelopes();

    public EnvelopeType getEnvelope(String crs) {
	List<EnvelopeType> envelopes = getEnvelopes();
	CRS crs0 = CRS.fromIdentifier(crs);
	for (EnvelopeType envelope : envelopes) {
	    CRS crs1 = CRS.fromIdentifier(envelope.getSrsName());
	    if (crs0.equals(crs1)) {
		return envelope;
	    }
	}
	return null;
    }

    public abstract List<String> getStyleNames() throws XPathExpressionException;

    public abstract List<String> getStyleTitles() throws XPathExpressionException;

    public abstract List<String> getCRS() throws XPathExpressionException;

    public abstract void setTitle(String title);

    public abstract void setLatLonBoundingBox(double south, double west, double north, double east);

    public abstract void addBoundingBox(String srs, double minx, double miny, double maxx, double maxy);

    public abstract void addFormat(String format);

    public abstract List<String> getFormat() throws XPathExpressionException;

    public abstract void addSRS(String crs);

    public abstract void addTimeDimension(String begin, String end, String timeResolution);

    public abstract void addElevationDimension(String elevationMin, String elevationMax, String resolution, String units);

    public Optional<Date> getBeginPosition() {

	scanTime();

	if (dates.isEmpty()) {
	    return Optional.empty();
	}

	return Optional.of(dates.first());

    }

    public Optional<Date> getEndPosition() {
	scanTime();

	if (dates.isEmpty()) {
	    return Optional.empty();
	}

	return Optional.of(dates.last());

    }

    public abstract Optional<Date> getDefaultPosition();

    public Date getNearestAvailableTime(Date date) {
	scanTime();
	if (continuePeriod) {
	    if (date.before(dates.first())) {
		return dates.first();
	    }
	    if (date.after(dates.last())) {
		return dates.last();
	    }
	    return date;

	} else {
	    Iterator<Date> iterator = dates.iterator();
	    Double diff = null;
	    Date ret = null;
	    while (iterator.hasNext()) {
		Date available = iterator.next();
		double newDiff = Math.abs(date.getTime() - available.getTime());
		if (diff == null) {
		    diff = newDiff;
		    ret = available;
		} else {
		    if (newDiff < diff) {
			diff = newDiff;
			ret = available;
		    } else {
			break;
		    }
		}
	    }
	    return ret;
	}

    }

    public void scanTime() {

	getDimensionAxis("time").ifPresent(this::scanTime);

    }

    public List<Date> getAvailableTimes() {

	return new ArrayList<>(dates);

    }

    public abstract String getVersion();

    public abstract Integer getDefaultHeight();

    public abstract Integer getDefaultWidth();

    public abstract String getGetMapURL();

    public String getCRSIdentifier(CRS crs) {
	List<String> crses;
	try {
	    crses = getCRS();
	    for (String identifier : crses) {
		CRS myCrs = CRS.fromIdentifier(identifier);
		if (myCrs.equals(crs)) {
		    return identifier;
		}
	    }
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't get crs identifier", e);

	}

	return null;
    }

    public abstract List<IWMSLayer> getChildren();

    public abstract Optional<String> getDimensionAxis(String dimensionName);

    public abstract String getDimensionAxisDefault(String dimensionName);

    @Override
    public String toString() {
	String ret = getName();
	if (ret != null) {
	    return "WMS Layer, name: " + ret;
	}
	return getTitle();
    }

    public Set<Date> scanTime(String value) {
	value = value.trim();
	if (dates.isEmpty()) {
	    if (value.contains(",")) {
		String[] split = value.split(",");
		for (String time : split) {
		    addTime(time.trim(), dates);
		}
	    } else {
		addTime(value, dates);
	    }
	}
	return dates;

    }

    public void addTime(String time, Set<Date> dates) {
	if (time.contains("/")) {

	    String[] split = time.split("/");

	    if (split.length == 2) {

		ISO8601DateTimeUtils.parseISO8601ToDate(split[0]).ifPresent(dates::add);

		ISO8601DateTimeUtils.parseISO8601ToDate(split[1]).ifPresent(dates::add);

	    }

	    if (split.length == 3) {

		Calendar tmpCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

		ISO8601DateTimeUtils.parseISO8601ToDate(split[0]).ifPresent(begin -> {

		    dates.add(begin);

		    tmpCalendar.setTime(begin);

		});

		ISO8601DateTimeUtils.parseISO8601ToDate(split[1]).ifPresent(end -> {

		    dates.add(end);

		    endCalendar.setTime(end);

		});

		Duration xmlDuration = null;
		Period period = null;
		try {
		    xmlDuration = ISO8601DateTimeUtils.getDuration(split[2]);
		} catch (Exception e) {
		    // TODO: handle exception
		    GSLoggerFactory.getLogger(getClass()).warn("Can't get duration. Try other way", e);
		    if (split[2].contains("T")) {
			String[] splitted = split[2].split("T");
			period = Period.parse(splitted[0]);
		    } else {
			period = Period.parse(split[2]);
		    }
		}

		// javax.xml.datatype.Duration xmlDuration = ISO8601DateTimeUtils.getDuration(split[2]);
		if (xmlDuration != null) {
		    while (tmpCalendar.before(endCalendar)) {
			dates.add(tmpCalendar.getTime());
			tmpCalendar.add(Calendar.YEAR, xmlDuration.getYears());
			tmpCalendar.add(Calendar.MONTH, xmlDuration.getMonths());
			tmpCalendar.add(Calendar.DAY_OF_YEAR, xmlDuration.getDays());
			tmpCalendar.add(Calendar.HOUR, xmlDuration.getHours());
			tmpCalendar.add(Calendar.MINUTE, xmlDuration.getMinutes());
			tmpCalendar.add(Calendar.SECOND, xmlDuration.getSeconds());
		    }
		} else if (period != null) {
		    while (tmpCalendar.before(endCalendar)) {
			dates.add(tmpCalendar.getTime());
			tmpCalendar.add(Calendar.YEAR, period.getYears());
			tmpCalendar.add(Calendar.MONTH, period.getMonths());
			tmpCalendar.add(Calendar.DAY_OF_YEAR, period.getDays());
			// tmpCalendar.add(Calendar.HOUR, xmlDuration.getHours());
			// tmpCalendar.add(Calendar.MINUTE, xmlDuration.getMinutes());
			// tmpCalendar.add(Calendar.SECOND, xmlDuration.getSeconds());
		    }
		}
		continuePeriod = false;

	    }
	} else {

	    ISO8601DateTimeUtils.parseISO8601ToDate(time).ifPresent(dates::add);

	}
    }

    public static void main(String[] args) {

	String s = "P11M4W2D";
	String s2 = "P11M4W2DT23H59M";
	String s3 = "P2W2D";

	String s4 = "P01Y11M4W2DT23H59M";

	Period period1 = Period.parse(s);
	Period period3 = Period.parse(s);

	String[] split1 = s2.split("T");
	String[] split2 = s4.split("T");
	Period period2 = Period.parse(split1[0]);
	Period period4 = Period.parse(split2[0]);

	System.out.println(period1.getDays());
	System.out.println(period2.getDays());
	System.out.println(period3.getDays());
	System.out.println(period4.getDays());

	// Duration duration = Duration.parse(s3);

    }

}
