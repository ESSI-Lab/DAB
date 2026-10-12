package eu.essi_lab.accessor.geuspromice;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

/**
 * Reads the PROMICE / GC-Net L3 AWS NetCDF files (CF-1.7, ACDD-1.3, one site per file, all the variables on the time
 * dimension)
 *
 * @author boldrini
 */
public class PROMICENetCDF {

    public static final String TIME_VARIABLE = "time";

    /**
     * The ACDD global attributes copied in the harvested metadata records
     */
    public static final String[] GLOBAL_ATTRIBUTES = new String[] { //
	    "site_id", "stations", "project", "location_type", "latitude", "longitude", "altitude", "id", "title", "summary", "license",
	    "institution", "creator_name", "creator_email", "creator_url", "publisher_name", "publisher_email", "publisher_url",
	    "references", "keywords", "processing_level", "product_version", "time_coverage_resolution", "date_modified" };

    /**
     * The variable attributes copied in the harvested metadata records
     */
    public static final String[] VARIABLE_ATTRIBUTES = new String[] { "standard_name", "long_name", "units", "coverage_content_type" };

    private static final Pattern TIME_UNITS = Pattern.compile("^\\s*(\\w+)\\s+since\\s+(\\d{4}-\\d{2}-\\d{2})(?:[ T](\\d{2}:\\d{2}(?::\\d{2})?))?.*$");

    private PROMICENetCDF() {
    }

    /**
     * @param file
     * @param variableNames the variables to describe; null for all the variables on the time dimension
     * @return the global attributes and, for each variable having at least one valid value, its attributes and the
     *         time extent of its valid values
     * @throws IOException
     */
    public static PROMICESite describe(File file, List<String> variableNames) throws IOException {

	try (NetcdfFile nc = NetcdfFiles.open(file.getAbsolutePath())) {

	    PROMICESite site = new PROMICESite();

	    for (String name : GLOBAL_ATTRIBUTES) {
		Attribute attribute = nc.findGlobalAttribute(name);
		if (attribute != null && attribute.getStringValue() != null && !attribute.getStringValue().isEmpty()) {
		    site.getGlobalAttributes().put(name, attribute.getStringValue());
		}
	    }

	    Date[] times = readTimes(nc);

	    for (Variable variable : nc.getVariables()) {

		String name = variable.getShortName();

		if (name.equals(TIME_VARIABLE) || variable.getRank() != 1 || !variable.getDimension(0).getShortName().equals(TIME_VARIABLE)
			|| !variable.getDataType().isNumeric()) {
		    continue;
		}

		if (variableNames != null && !variableNames.contains(name)) {
		    continue;
		}

		Optional<SimpleEntry<Date, Date>> extent = getValidExtent(times, variable.read());
		if (extent.isEmpty()) {
		    continue;
		}

		JSONObject description = new JSONObject();
		description.put(PROMICESite.VARIABLE_NAME, name);
		for (String attributeName : VARIABLE_ATTRIBUTES) {
		    Attribute attribute = variable.findAttribute(attributeName);
		    if (attribute != null && attribute.getStringValue() != null) {
			description.put(attributeName, attribute.getStringValue());
		    }
		}
		description.put(PROMICESite.VARIABLE_BEGIN, extent.get().getKey().getTime());
		description.put(PROMICESite.VARIABLE_END, extent.get().getValue().getTime());

		site.getVariables().add(description);
	    }

	    return site;
	}
    }

    /**
     * @param file
     * @param variableName
     * @param begin inclusive, null for no lower limit
     * @param end inclusive, null for no upper limit
     * @return the valid values of the given variable in the given time range
     * @throws IOException
     */
    public static List<SimpleEntry<Date, Double>> readValues(File file, String variableName, Date begin, Date end) throws IOException {

	List<SimpleEntry<Date, Double>> ret = new ArrayList<>();

	try (NetcdfFile nc = NetcdfFiles.open(file.getAbsolutePath())) {

	    Variable variable = nc.findVariable(variableName);
	    if (variable == null) {
		return ret;
	    }

	    Date[] times = readTimes(nc);
	    Array values = variable.read();

	    for (int i = 0; i < times.length && i < values.getSize(); i++) {

		double value = values.getDouble(i);
		if (!isValid(value) || times[i] == null) {
		    continue;
		}
		if ((begin != null && times[i].before(begin)) || (end != null && times[i].after(end))) {
		    continue;
		}

		ret.add(new SimpleEntry<>(times[i], value));
	    }
	}

	return ret;
    }

    /**
     * @param nc
     * @return
     * @throws IOException
     */
    private static Date[] readTimes(NetcdfFile nc) throws IOException {

	Variable time = nc.findVariable(TIME_VARIABLE);
	if (time == null) {
	    throw new IOException("Time variable not found");
	}

	Attribute units = time.findAttribute("units");
	if (units == null) {
	    throw new IOException("Time units not found");
	}

	Array array = time.read();
	Date[] ret = new Date[(int) array.getSize()];
	for (int i = 0; i < ret.length; i++) {
	    ret[i] = toDate(array.getDouble(i), units.getStringValue()).orElse(null);
	}

	return ret;
    }

    /**
     * Converts a CF time value (e.g. units "days since 1995-06-07 00:00:00") to a date. The files use the proleptic
     * gregorian calendar, the same as java.time.
     *
     * @param value
     * @param units
     * @return
     */
    public static Optional<Date> toDate(double value, String units) {

	if (!isValid(value) || units == null) {
	    return Optional.empty();
	}

	Matcher matcher = TIME_UNITS.matcher(units);
	if (!matcher.matches()) {
	    return Optional.empty();
	}

	double seconds;
	switch (matcher.group(1).toLowerCase()) {
	case "days":
	case "day":
	    seconds = value * 86400;
	    break;
	case "hours":
	case "hour":
	    seconds = value * 3600;
	    break;
	case "minutes":
	case "minute":
	    seconds = value * 60;
	    break;
	case "seconds":
	case "second":
	    seconds = value;
	    break;
	default:
	    return Optional.empty();
	}

	String time = matcher.group(3) == null ? "00:00:00" : matcher.group(3);
	if (time.length() == 5) {
	    time += ":00";
	}

	LocalDateTime origin = LocalDateTime.parse(matcher.group(2) + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

	return Optional.of(Date.from(origin.toInstant(ZoneOffset.UTC).plusMillis(Math.round(seconds * 1000))));
    }

    /**
     * @param resolution the ACDD time_coverage_resolution, e.g. "P1DT0H0M0S", "P0DT1H0M0S" or "P30DT0H0M0S" (monthly)
     * @return a compact ISO 8601 duration (e.g. "P1D", "PT1H", "P1M"), or null
     */
    public static String normalizeResolution(String resolution) {

	if (resolution == null || resolution.isEmpty()) {
	    return null;
	}

	try {
	    Duration duration = Duration.parse(resolution);
	    long days = duration.toDays();

	    if (days >= 28 && days <= 31) {
		return "P1M";
	    }
	    if (days >= 1 && duration.toHoursPart() == 0 && duration.toMinutesPart() == 0) {
		return "P" + days + "D";
	    }
	    if (duration.toMinutes() % 60 == 0) {
		return "PT" + duration.toHours() + "H";
	    }
	    return "PT" + duration.toMinutes() + "M";

	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @param times
     * @param values
     * @return the dates of the first and last valid values
     */
    private static Optional<SimpleEntry<Date, Date>> getValidExtent(Date[] times, Array values) {

	Date first = null;
	Date last = null;

	for (int i = 0; i < times.length && i < values.getSize(); i++) {
	    if (times[i] != null && isValid(values.getDouble(i))) {
		if (first == null) {
		    first = times[i];
		}
		last = times[i];
	    }
	}

	return first == null ? Optional.empty() : Optional.of(new SimpleEntry<>(first, last));
    }

    /**
     * @param value
     * @return
     */
    private static boolean isValid(double value) {

	return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
