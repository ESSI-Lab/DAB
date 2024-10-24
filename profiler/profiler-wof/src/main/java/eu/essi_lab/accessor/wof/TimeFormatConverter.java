package eu.essi_lab.accessor.wof;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini
 */
public class TimeFormatConverter {
    private List<SimpleDateFormat> getSeriesDateFormats = new ArrayList<>();
    private List<SimpleDateFormat> getValuesDateFormats = new ArrayList<>();
    private SimpleDateFormat iso8601DateTimeFormat;

    public TimeFormatConverter() {
	this.getSeriesDateFormats.add(new SimpleDateFormat("MM/dd/yyyy"));
	this.getSeriesDateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
	for (SimpleDateFormat getSeriesDateFormat : getSeriesDateFormats) {
	    getSeriesDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	this.getValuesDateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"));
	this.getValuesDateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
	for (SimpleDateFormat getValuesDateFormat : getValuesDateFormats) {
	    getValuesDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	this.iso8601DateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	iso8601DateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    }

    public Date convertGetSeriesTimeFormatToJavaDate(String timeString) {

	for (SimpleDateFormat getSeriesDateFormat : getSeriesDateFormats) {
	    try {
		Date date = getSeriesDateFormat.parse(timeString);
		return date;
	    } catch (ParseException e) {
	    }
	}
	GSLoggerFactory.getLogger(TimeFormatConverter.class).warn("Parse exception for time string {}", timeString);
	return null;

    }

    public String convertGetSeriesTimeFormatToISO8601(String timeString) {
	Date date = convertGetSeriesTimeFormatToJavaDate(timeString);
	if (date == null) {
	    return null;
	}
	return iso8601DateTimeFormat.format(date);

    }

    /**
     * Parses date and date time values as supported by Hydro Server specification
     * e.g. HydroDesktop sends requests in the form yyyy-MM-dd'T'HH:mm
     * e.g. Jupiter Notebook cuahsi_gap sends requests in the form yyyy-MM-dd
     * ... more to follow?
     * 
     * @param timeString
     * @return
     */
    public Date convertGetValuesTimeFormatToJavaDate(String timeString) {
	for (SimpleDateFormat getValuesDateFormat : getValuesDateFormats) {
	    try {
		Date date = getValuesDateFormat.parse(timeString);
		return date;
	    } catch (ParseException e) {
	    }
	}
	GSLoggerFactory.getLogger(TimeFormatConverter.class).warn("Parse exception for time string {}", timeString, null);
	return null;

    }

    public String convertGetValuesTimeFormatToISO8601(String timeString) {
	Date date = convertGetValuesTimeFormatToJavaDate(timeString);
	if (date == null) {
	    return null;
	}
	return iso8601DateTimeFormat.format(date);

    }

}
