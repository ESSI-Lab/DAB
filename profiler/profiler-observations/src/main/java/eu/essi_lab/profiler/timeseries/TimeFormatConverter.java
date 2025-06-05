package eu.essi_lab.profiler.timeseries;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
    private List<SimpleDateFormat> timeFormats = new ArrayList<>();

    public TimeFormatConverter() {
	timeFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
	timeFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
	timeFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
	
	for (SimpleDateFormat getSeriesDateFormat : timeFormats) {
	    getSeriesDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

    }

    public Date convertToJavaDate(String timeString) {

	for (SimpleDateFormat getSeriesDateFormat : timeFormats) {
	    try {
		Date date = getSeriesDateFormat.parse(timeString);
		return date;
	    } catch (ParseException e) {
	    }
	}
	GSLoggerFactory.getLogger(TimeFormatConverter.class).warn("Parse exception for time string {}", timeString);
	return null;

    }


}
