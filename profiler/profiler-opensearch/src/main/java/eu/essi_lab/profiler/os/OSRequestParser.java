package eu.essi_lab.profiler.os;

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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;

/**
 * @author Fabrizio
 */
public class OSRequestParser {

    private KeyValueParser parser;

    /**
     * @param parser
     */
    public OSRequestParser(KeyValueParser parser) {

	this.parser = parser;
    }

    /**
     * @param parser
     */
    public OSRequestParser(WebRequest request) {

	if (!request.getFormData().isPresent()) {
	    this.parser = new KeyValueParser("");
	} else {
	    this.parser = new KeyValueParser(request.getFormData().get());
	}
    }

    /**
     * @param parameter
     * @return
     */
    public String parse(OSParameter parameter) throws IllegalArgumentException {

	String value = parser.getValue(parameter.getName());
	if (value == null || value.equals("") || value.equals(KeyValueParser.UNDEFINED)) {
	    if (parameter.getDefaultValue() != null) {
		return parameter.getDefaultValue();
	    }
	    return null;
	}
 
	switch (parameter.getValueType()) {
	case "freeText":
	    break;
	case "int":
	    Integer intValue = parseInt(value);
	    if (intValue == null) {
		throw new IllegalArgumentException("Invalid integer value: " + value);
	    }
	    break;
	case "dateTime":
	    return parseISO8601DateTime(value);
	case "bbox":
	    if (value != null && value.length() > 0) {
		// this is to support the GEOSS Web Portal
		if(value.equals(",,,")){
		    return null;
		}
		String[] split = value.split("_");
		for (String box : split) {
		    new OSBox(box);
		}
	    }
	}

	try {
	    value = URLDecoder.decode(value, "UTF-8");
	    value = value.contains("&") ? value.replace("&", "&amp;") : value;
	} catch (UnsupportedEncodingException e) {
	}

	return value;
    }

    static Integer parseInt(String value) {

	try {
	    return Integer.parseInt(value);
	} catch (NumberFormatException ex) {
	}

	return null;
    }

    static Double parseDouble(String value) {

	try {
	    return Double.parseDouble(value);
	} catch (NumberFormatException ex) {
	}

	return null;
    }

    private String parseISO8601DateTime(String dateTime) throws IllegalArgumentException {

	if (dateTime != null) {

	    DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser().withChronology(ISOChronology.getInstance(DateTimeZone.UTC));
	    DateTime parsed = formatter.parseDateTime(dateTime);
	    parsed = parsed.toDateTime(DateTimeZone.UTC);

	    return parsed.toString();
	}
	return null;
    }
}
