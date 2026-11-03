package eu.essi_lab.accessor.gcw;

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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.time.CalendarDateUnit;

/**
 * Reads the CF time series published on OPeNDAP by the data centres of the GCW catalogue (MET Norway THREDDS, GEUS
 * THREDDS, IG PAS Hyrax). The datasets differ in conventions: the time coordinate can be named "time" or "obs", the
 * variables can be on the time dimension only, on (time, station) with a single station, or on (time, depth) for
 * timeSeriesProfile datasets, in which case one series is described for each vertical level.
 *
 * @author boldrini
 */
public class GCWOPeNDAP {

    /**
     * Maximum number of values read to check whether a variable has valid values and to locate a time range
     */
    private static final int SAMPLE_SIZE = 5000;

    /**
     * Time series longer than this are subset by first reading a strided sample of the time coordinate
     */
    private static final int COARSE_SEARCH_THRESHOLD = 100000;

    /**
     * Standard names of the variables that are station coordinates rather than measurements
     */
    private static final Set<String> COORDINATE_STANDARD_NAMES = Set.of("time", "latitude", "longitude", "altitude", "height",
	    "surface_altitude", "projection_x_coordinate", "projection_y_coordinate", "depth", "timeseries_id", "platform_name",
	    "platform_id");

    private static final Pattern SERIES_NAME = Pattern.compile("^(.+)\\[(\\d+)\\]$");

    /**
     * Waits before retrying a request refused for overload, e.g. HTTP 429 Too Many Requests from thredds.met.no
     */
    private static final long[] RETRY_DELAYS = new long[] { 10000, 30000, 60000 };

    /**
     * Minimum interval between two requests, to stay below the rate limits of the servers
     */
    private static final long REQUEST_INTERVAL = 250;

    private static long lastRequest;

    /**
     * A request to the OPeNDAP server
     */
    @FunctionalInterface
    private interface RemoteCall<T> {

	T call() throws IOException, InvalidRangeException;
    }

    private GCWOPeNDAP() {
    }

    /**
     * @param variable
     * @param levelIndex the vertical level index, or null for a series without levels
     * @return the series name used as online resource name, e.g. "TA" or "soil_temperature[3]"
     */
    public static String getSeriesName(String variable, Integer levelIndex) {

	return levelIndex == null ? variable : variable + "[" + levelIndex + "]";
    }

    /**
     * @param seriesName
     * @return the variable name and the vertical level index (null for a series without levels)
     */
    public static SimpleEntry<String, Integer> parseSeriesName(String seriesName) {

	Matcher matcher = SERIES_NAME.matcher(seriesName);
	if (matcher.matches()) {
	    return new SimpleEntry<>(matcher.group(1), Integer.valueOf(matcher.group(2)));
	}
	return new SimpleEntry<>(seriesName, null);
    }

    /**
     * @param url the OPeNDAP dataset URL
     * @param exclude variables whose name matches this pattern are not described; may be null
     * @return the dataset description
     * @throws IOException if the dataset can not be opened or has no time coordinate
     */
    public static GCWTimeSeriesDataset describe(String url, Pattern exclude) throws IOException {

	try (NetcdfDataset nc = open(url)) {

	    GCWTimeSeriesDataset ret = new GCWTimeSeriesDataset();

	    for (Attribute attribute : nc.getGlobalAttributes()) {
		if (attribute.isString() && attribute.getStringValue() != null && !attribute.getStringValue().isBlank()) {
		    ret.getGlobalAttributes().put(attribute.getShortName(), attribute.getStringValue().trim());
		}
	    }

	    Variable time = findTime(nc).orElseThrow(() -> new IOException("Time coordinate not found"));
	    Dimension timeDimension = time.getDimension(0);
	    int size = timeDimension.getLength();
	    ret.setTimeSize(size);

	    if (size > 0) {
		CalendarDateUnit unit = getDateUnit(time);
		ret.setBegin(toDate(unit, readTime(time, 0, 0, 1).getDouble(0)).map(Date::getTime).orElse(null));
		ret.setEnd(toDate(unit, readTime(time, size - 1, size - 1, 1).getDouble(0)).map(Date::getTime).orElse(null));
	    }

	    describeStation(nc, ret);

	    for (Variable variable : nc.getVariables()) {

		if (!isCandidate(variable, time, exclude)) {
		    continue;
		}

		Optional<Dimension> levelDimension = getLevelDimension(variable, timeDimension);
		if (levelDimension == null) {
		    continue;
		}

		JSONObject attributes = new JSONObject();
		attributes.put(GCWTimeSeriesDataset.SERIES_VARIABLE, variable.getShortName());
		for (Attribute attribute : variable.attributes()) {
		    if (attribute.isString() && attribute.getStringValue() != null && !attribute.getStringValue().isBlank()) {
			attributes.put(attribute.getShortName(), attribute.getStringValue().trim());
		    }
		}

		int levels = levelDimension.map(Dimension::getLength).orElse(1);
		Array sample = readSample(variable, timeDimension, size);

		for (int level = 0; level < levels; level++) {

		    if (size > 0 && !hasValidValues(sample, levels, level)) {
			continue;
		    }

		    JSONObject series = new JSONObject(attributes.toString());
		    if (levelDimension.isPresent()) {
			addLevel(nc, levelDimension.get(), level, series);
		    }
		    ret.getSeries().add(series);
		}
	    }

	    return ret;

	} catch (InvalidRangeException e) {

	    throw new IOException(e);
	}
    }

    /**
     * @param url the OPeNDAP dataset URL
     * @return the first and last time steps
     * @throws IOException
     */
    public static Optional<SimpleEntry<Date, Date>> getTimeExtent(String url) throws IOException {

	try (NetcdfDataset nc = open(url)) {

	    Variable time = findTime(nc).orElseThrow(() -> new IOException("Time coordinate not found"));
	    int size = time.getDimension(0).getLength();
	    if (size == 0) {
		return Optional.empty();
	    }

	    CalendarDateUnit unit = getDateUnit(time);
	    Optional<Date> begin = toDate(unit, readTime(time, 0, 0, 1).getDouble(0));
	    Optional<Date> end = toDate(unit, readTime(time, size - 1, size - 1, 1).getDouble(0));

	    return begin.isPresent() && end.isPresent() ? Optional.of(new SimpleEntry<>(begin.get(), end.get())) : Optional.empty();

	} catch (InvalidRangeException e) {

	    throw new IOException(e);
	}
    }

    /**
     * @param url the OPeNDAP dataset URL
     * @param seriesName the series name, see {@link #getSeriesName(String, Integer)}
     * @param begin inclusive, null for no lower limit
     * @param end inclusive, null for no upper limit
     * @return the valid values of the series in the given time range
     * @throws IOException
     */
    public static List<SimpleEntry<Date, Double>> readValues(String url, String seriesName, Date begin, Date end) throws IOException {

	List<SimpleEntry<Date, Double>> ret = new ArrayList<>();

	SimpleEntry<String, Integer> parsed = parseSeriesName(seriesName);

	try (NetcdfDataset nc = open(url)) {

	    Variable variable = nc.findVariable(parsed.getKey());
	    if (variable == null) {
		throw new IOException("Variable " + parsed.getKey() + " not found");
	    }

	    Variable time = findTime(nc).orElseThrow(() -> new IOException("Time coordinate not found"));
	    Dimension timeDimension = time.getDimension(0);
	    int size = timeDimension.getLength();
	    if (size == 0) {
		return ret;
	    }

	    CalendarDateUnit unit = getDateUnit(time);
	    int[] window = findWindow(time, unit, size, begin, end);
	    if (window == null) {
		return ret;
	    }

	    Array times = readTime(time, window[0], window[1], 1);
	    List<Range> ranges = new ArrayList<>();
	    for (Dimension dimension : variable.getDimensions()) {
		if (dimension.equals(timeDimension)) {
		    ranges.add(new Range(window[0], window[1]));
		} else if (dimension.getLength() > 1) {
		    int level = parsed.getValue() == null ? 0 : parsed.getValue();
		    ranges.add(new Range(level, level));
		} else {
		    ranges.add(new Range(0, 0));
		}
	    }
	    Array values = retry(() -> variable.read(ranges));

	    for (int i = 0; i < times.getSize() && i < values.getSize(); i++) {

		double value = values.getDouble(i);
		if (!isValid(value)) {
		    continue;
		}

		Optional<Date> date = toDate(unit, times.getDouble(i));
		if (date.isEmpty() || (begin != null && date.get().before(begin)) || (end != null && date.get().after(end))) {
		    continue;
		}

		ret.add(new SimpleEntry<>(date.get(), value));
	    }

	    return ret;

	} catch (InvalidRangeException e) {

	    throw new IOException(e);
	}
    }

    /**
     * Finds the indexes of the time steps to read. For long series a strided sample of the time coordinate is read
     * first, so that only the window containing the requested range is transferred; the time coordinate is assumed to
     * be increasing, otherwise the whole series is read.
     *
     * @return the first and last indexes, or null if no time step is in the range
     */
    private static int[] findWindow(Variable time, CalendarDateUnit unit, int size, Date begin, Date end)
	    throws IOException, InvalidRangeException {

	if (size <= COARSE_SEARCH_THRESHOLD || (begin == null && end == null)) {
	    return new int[] { 0, size - 1 };
	}

	int stride = (int) Math.ceil(size / (double) SAMPLE_SIZE);
	Array coarse = readTime(time, 0, size - 1, stride);

	int first = 0;
	int last = (int) coarse.getSize() - 1;
	double previous = Double.NEGATIVE_INFINITY;

	for (int i = 0; i < coarse.getSize(); i++) {

	    double value = coarse.getDouble(i);
	    if (isValid(value) && value < previous) {
		// not increasing
		return new int[] { 0, size - 1 };
	    }
	    if (isValid(value)) {
		previous = value;
	    }

	    Optional<Date> date = toDate(unit, value);
	    if (date.isEmpty()) {
		continue;
	    }
	    if (begin != null && date.get().before(begin)) {
		first = i;
	    }
	    if (end != null && date.get().after(end) && last == coarse.getSize() - 1) {
		last = i;
	    }
	}

	int from = first * stride;
	int to = last == coarse.getSize() - 1 ? size - 1 : Math.min(size - 1, last * stride);

	return from > to ? null : new int[] { from, to };
    }

    /**
     * @param nc
     * @return the time coordinate: the 1D time axis found by the CF conventions or, as a fallback, a 1D variable having
     *         units in the form "unit since date"
     */
    static Optional<Variable> findTime(NetcdfDataset nc) {

	for (CoordinateAxis axis : nc.getCoordinateAxes()) {
	    if (axis.getAxisType() == AxisType.Time && axis.getRank() == 1 && axis.getDataType().isNumeric()) {
		return Optional.of(axis);
	    }
	}

	for (Variable variable : nc.getVariables()) {
	    String units = variable.getUnitsString();
	    if (variable.getRank() == 1 && variable.getDataType().isNumeric() && units != null && units.toLowerCase().contains(" since ")) {
		return Optional.of(variable);
	    }
	}

	return Optional.empty();
    }

    /**
     * @param variable
     * @param time
     * @param exclude
     * @return true if the variable is a numeric measurement on the time dimension
     */
    private static boolean isCandidate(Variable variable, Variable time, Pattern exclude) {

	if (variable.getShortName().equals(time.getShortName()) || variable instanceof CoordinateAxis || !variable.getDataType().isNumeric()) {
	    return false;
	}

	if (variable.findDimensionIndex(time.getDimension(0).getShortName()) < 0) {
	    return false;
	}

	if (variable.findAttribute("flag_values") != null || variable.findAttribute("flag_meanings") != null
		|| variable.findAttribute("flag_masks") != null || variable.findAttribute("cf_role") != null) {
	    return false;
	}

	String standardName = getString(variable, "standard_name");
	if (standardName != null && COORDINATE_STANDARD_NAMES.contains(standardName.toLowerCase())) {
	    return false;
	}

	String name = variable.getShortName().toLowerCase();
	if (name.equals("lat") || name.equals("lon") || name.equals("latitude") || name.equals("longitude")) {
	    return false;
	}

	return exclude == null || !exclude.matcher(variable.getShortName()).matches();
    }

    /**
     * @param variable
     * @param timeDimension
     * @return an empty optional if the variable has no other dimension with more than one element, the dimension
     *         of the vertical levels if it is the only other one with more than one element, null if the variable is not
     *         a time series
     */
    private static Optional<Dimension> getLevelDimension(Variable variable, Dimension timeDimension) {

	Dimension ret = null;
	for (Dimension dimension : variable.getDimensions()) {
	    if (dimension.equals(timeDimension) || dimension.getLength() <= 1) {
		continue;
	    }
	    if (ret != null) {
		return null;
	    }
	    ret = dimension;
	}

	return Optional.ofNullable(ret);
    }

    /**
     * Reads a strided sample of a variable, with all its vertical levels, ordered as (time, level)
     */
    private static Array readSample(Variable variable, Dimension timeDimension, int size) throws IOException, InvalidRangeException {

	if (size == 0) {
	    return null;
	}

	int stride = (int) Math.ceil(size / (double) SAMPLE_SIZE);
	List<Range> ranges = new ArrayList<>();
	boolean timeFirst = true;
	boolean timeSeen = false;
	for (Dimension dimension : variable.getDimensions()) {
	    if (dimension.equals(timeDimension)) {
		ranges.add(new Range(0, size - 1, stride));
		timeSeen = true;
	    } else if (dimension.getLength() > 1) {
		ranges.add(new Range(0, dimension.getLength() - 1));
		if (!timeSeen) {
		    timeFirst = false;
		}
	    } else {
		ranges.add(new Range(0, 0));
	    }
	}

	Array ret = retry(() -> variable.read(ranges)).reduce();
	if (!timeFirst && ret.getRank() == 2) {
	    ret = ret.transpose(0, 1);
	}
	return ret;
    }

    /**
     * @param sample the sample ordered as (time, level), or (time) when levels is 1
     * @param levels
     * @param level
     * @return
     */
    private static boolean hasValidValues(Array sample, int levels, int level) {

	if (sample == null) {
	    return false;
	}

	if (levels <= 1 || sample.getRank() < 2) {
	    for (int i = 0; i < sample.getSize(); i++) {
		if (isValid(sample.getDouble(i))) {
		    return true;
		}
	    }
	    return false;
	}

	Array slice = sample.slice(1, level);
	for (int i = 0; i < slice.getSize(); i++) {
	    if (isValid(slice.getDouble(i))) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Adds the vertical level index and, if the level dimension has a coordinate variable, its value and units
     */
    private static void addLevel(NetcdfDataset nc, Dimension dimension, int level, JSONObject series) throws IOException {

	series.put(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX, level);
	series.put(GCWTimeSeriesDataset.SERIES_LEVEL_NAME, dimension.getShortName());

	Variable coordinate = nc.findVariable(dimension.getShortName());
	if (coordinate == null || coordinate.getRank() != 1 || !coordinate.getDataType().isNumeric()) {
	    return;
	}

	try {
	    double value = retry(() -> coordinate.read(new int[] { level }, new int[] { 1 })).getDouble(0);
	    if (isValid(value)) {
		series.put(GCWTimeSeriesDataset.SERIES_LEVEL_VALUE, value);
	    }
	} catch (InvalidRangeException e) {
	    throw new IOException(e);
	}

	String units = coordinate.getUnitsString();
	if (units != null && !units.isBlank()) {
	    series.put(GCWTimeSeriesDataset.SERIES_LEVEL_UNITS, units.trim());
	}
	String positive = getString(coordinate, "positive");
	if (positive != null) {
	    series.put(GCWTimeSeriesDataset.SERIES_LEVEL_POSITIVE, positive);
	}
    }

    /**
     * Reads the station identifier (the timeseries_id variable or the WMO number of SYNOP datasets) and location (the
     * latitude, longitude and altitude variables, or the ACDD geospatial attributes)
     */
    private static void describeStation(NetcdfDataset nc, GCWTimeSeriesDataset dataset) {

	JSONObject station = dataset.getStation();

	for (Variable variable : nc.getVariables()) {

	    String standardName = getString(variable, "standard_name");
	    String name = variable.getShortName().toLowerCase();

	    try {
		if (variable.findAttribute("cf_role") != null || name.equals("station_wmonr")) {
		    Optional<String> id = readFirst(variable);
		    if (id.isPresent() && !station.has(GCWTimeSeriesDataset.STATION_ID)) {
			station.put(GCWTimeSeriesDataset.STATION_ID, id.get());
		    }
		} else if ("latitude".equals(standardName) || name.equals("lat") || name.equals("latitude")) {
		    readFirst(variable).ifPresent(v -> station.put(GCWTimeSeriesDataset.STATION_LATITUDE, v));
		} else if ("longitude".equals(standardName) || name.equals("lon") || name.equals("longitude")) {
		    readFirst(variable).ifPresent(v -> station.put(GCWTimeSeriesDataset.STATION_LONGITUDE, v));
		} else if ("surface_altitude".equals(standardName) || "altitude".equals(standardName) || name.equals("alt")
			|| name.equals("altitude") || name.equals("orog")) {
		    readFirst(variable).ifPresent(v -> station.put(GCWTimeSeriesDataset.STATION_ALTITUDE, v));
		}
	    } catch (IOException | InvalidRangeException e) {
		// the location is completed from the global attributes
	    }
	}

	putAttribute(dataset, station, GCWTimeSeriesDataset.STATION_LATITUDE, "geospatial_lat_min", "latitude", "station_latitude");
	putAttribute(dataset, station, GCWTimeSeriesDataset.STATION_LONGITUDE, "geospatial_lon_min", "longitude", "station_longitude");
	putAttribute(dataset, station, GCWTimeSeriesDataset.STATION_ALTITUDE, "geospatial_vertical_min", "altitude", "station_altitude");
	putAttribute(dataset, station, GCWTimeSeriesDataset.STATION_ID, "station_id", "site_id", "station_name", "platform_id", "platform");
    }

    /**
     * Puts the first available global attribute, if the key is not already set
     */
    private static void putAttribute(GCWTimeSeriesDataset dataset, JSONObject station, String key, String... attributes) {

	if (station.has(key)) {
	    return;
	}
	for (String attribute : attributes) {
	    String value = dataset.getGlobalAttributes().get(attribute);
	    if (value != null && !value.isBlank()) {
		station.put(key, value);
		return;
	    }
	}
    }

    /**
     * @param variable a scalar or 1D variable
     * @return its first valid value, as string
     */
    private static Optional<String> readFirst(Variable variable) throws IOException, InvalidRangeException {

	if (variable.getSize() == 0 || variable.getRank() > 2) {
	    return Optional.empty();
	}

	if (variable.getDataType() == DataType.CHAR) {
	    Array array = retry(() -> variable.read());
	    String value = array instanceof ArrayChar chars ? (chars.getRank() <= 1 ? chars.getString() : chars.getString(0)) : null;
	    return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.trim());
	}
	if (variable.getDataType() == DataType.STRING) {
	    Object value = retry(() -> variable.read()).getObject(0);
	    return value == null || value.toString().isBlank() ? Optional.empty() : Optional.of(value.toString().trim());
	}

	int[] origin = new int[variable.getRank()];
	int[] shape = new int[variable.getRank()];
	for (int i = 0; i < shape.length; i++) {
	    shape[i] = 1;
	}
	double value = retry(() -> variable.read(origin, shape)).getDouble(0);
	if (!isValid(value)) {
	    return Optional.empty();
	}
	return Optional.of(value == Math.rint(value) && Math.abs(value) < 1e15 ? String.valueOf((long) value) : String.valueOf(value));
    }

    /**
     * @param time
     * @param from
     * @param to
     * @param stride
     * @return the raw values of the time coordinate in the given index range
     */
    private static Array readTime(Variable time, int from, int to, int stride) throws IOException, InvalidRangeException {

	List<Range> ranges = new ArrayList<>();
	ranges.add(new Range(from, to, stride));
	return retry(() -> time.read(ranges));
    }

    /**
     * @param url
     * @return the dataset, enhanced with coordinate systems and with missing values converted to NaN
     * @throws IOException
     */
    private static NetcdfDataset open(String url) throws IOException {

	try {
	    return retry(() -> NetcdfDatasets.openDataset(url));
	} catch (InvalidRangeException e) {
	    throw new IOException(e);
	}
    }

    /**
     * Executes the request, retrying it after increasing delays if the server is overloaded
     */
    private static <T> T retry(RemoteCall<T> call) throws IOException, InvalidRangeException {

	for (int attempt = 0;; attempt++) {
	    try {
		pace();
		return call.call();
	    } catch (IOException e) {
		if (attempt >= RETRY_DELAYS.length || !isTransient(e)) {
		    throw e;
		}
		GSLoggerFactory.getLogger(GCWOPeNDAP.class).warn("OPeNDAP request failed ({}), retrying in {} s", e.getMessage(),
			RETRY_DELAYS[attempt] / 1000);
		try {
		    Thread.sleep(RETRY_DELAYS[attempt]);
		} catch (InterruptedException ie) {
		    Thread.currentThread().interrupt();
		    throw e;
		}
	    }
	}
    }

    /**
     * Waits until {@link #REQUEST_INTERVAL} has passed since the previous request
     */
    private static synchronized void pace() {

	long wait = lastRequest + REQUEST_INTERVAL - System.currentTimeMillis();
	if (wait > 0) {
	    try {
		Thread.sleep(wait);
	    } catch (InterruptedException e) {
		Thread.currentThread().interrupt();
	    }
	}
	lastRequest = System.currentTimeMillis();
    }

    /**
     * @param e
     * @return true for timeouts and for the HTTP status codes of an overloaded server (429, 502, 503, 504)
     */
    private static boolean isTransient(IOException e) {

	for (Throwable t = e; t != null; t = t.getCause()) {
	    if (t instanceof SocketTimeoutException) {
		return true;
	    }
	    String message = t.getMessage();
	    if (message != null && (message.contains(" 429 ") || message.contains(" 502 ") || message.contains(" 503 ") || message.contains(" 504 "))) {
		return true;
	    }
	}
	return false;
    }

    /**
     * @param time
     * @return
     * @throws IOException
     */
    private static CalendarDateUnit getDateUnit(Variable time) throws IOException {

	String units = time.getUnitsString();
	if (units == null) {
	    throw new IOException("Time units not found");
	}
	try {
	    return CalendarDateUnit.of(getString(time, "calendar"), units.trim());
	} catch (IllegalArgumentException e) {
	    throw new IOException("Unsupported time units: " + units);
	}
    }

    /**
     * @param unit
     * @param value
     * @return
     */
    static Optional<Date> toDate(CalendarDateUnit unit, double value) {

	if (!isValid(value)) {
	    return Optional.empty();
	}
	return Optional.of(unit.makeCalendarDate(value).toDate());
    }

    /**
     * @param variable
     * @param name
     * @return the string value of the attribute, or null
     */
    private static String getString(Variable variable, String name) {

	Attribute attribute = variable.findAttribute(name);
	return attribute != null && attribute.isString() && attribute.getStringValue() != null ? attribute.getStringValue().trim() : null;
    }

    /**
     * @param value
     * @return
     */
    private static boolean isValid(double value) {

	return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
