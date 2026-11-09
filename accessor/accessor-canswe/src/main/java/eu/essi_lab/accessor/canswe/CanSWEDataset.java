package eu.essi_lab.accessor.canswe;

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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

/**
 * The content of the CanSWE NetCDF file (CF-1.9, dimensions station_id and time, one value per station and day), kept
 * in memory in sparse form: only the valid values of the snow variables are loaded (about one million values instead of
 * the about one hundred million cells of each variable).<br>
 * The last loaded file is cached, since the harvesting and the downloads read the same file many times.
 *
 * @author boldrini
 */
public class CanSWEDataset {

    /**
     * Snow water equivalent
     */
    public static final String SNW = "snw";

    /**
     * Snow depth
     */
    public static final String SND = "snd";

    /**
     * Bulk snow density
     */
    public static final String DEN = "den";

    /**
     * The time series variables
     */
    public static final List<String> VARIABLES = Collections.unmodifiableList(Arrays.asList(SNW, SND, DEN));

    /**
     * The variable attributes copied in the harvested metadata records
     */
    public static final String[] VARIABLE_ATTRIBUTES = new String[] { "standard_name", "long_name", "units", "description" };

    /**
     * The global attributes copied in the harvested metadata records
     */
    public static final String[] GLOBAL_ATTRIBUTES = new String[] { "title", "source", "comment", "latest_update", "redistribution" };

    private static final String TIME_VARIABLE = "time";

    private static final Pattern TIME_UNITS = Pattern.compile("^\\s*(\\w+)\\s+since\\s+(\\d{4}-\\d{2}-\\d{2})(?:[ T](\\d{2}:\\d{2}(?::\\d{2})?))?.*$");

    /**
     * Default block sizes along the station and time dimensions, used if the chunk sizes are not available
     */
    private static final int DEFAULT_STATION_BLOCK = 585;
    private static final int DEFAULT_TIME_BLOCK = 6983;

    private static CanSWEDataset cached;
    private static String cachedKey;

    private final List<CanSWEStation> stations = new ArrayList<>();
    private final Map<String, CanSWEStation> stationsById = new HashMap<>();
    private final Map<String, Map<String, String>> variableAttributes = new LinkedHashMap<>();
    private final Map<String, String> globalAttributes = new LinkedHashMap<>();
    private long[] times = new long[] {};

    private CanSWEDataset() {
    }

    /**
     * @param file
     * @return the dataset of the given file, reusing the last loaded one if the file is unchanged
     * @throws IOException
     */
    public static synchronized CanSWEDataset get(File file) throws IOException {

	String key = file.getAbsolutePath() + "@" + file.lastModified() + "#" + file.length();

	if (cached == null || !key.equals(cachedKey)) {

	    // releases the previous dataset before loading the new one
	    cached = null;
	    cached = load(file);
	    cachedKey = key;
	}

	return cached;
    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    public static CanSWEDataset load(File file) throws IOException {

	long start = System.currentTimeMillis();

	CanSWEDataset ret = new CanSWEDataset();

	try (NetcdfFile nc = NetcdfFiles.open(file.getAbsolutePath())) {

	    for (String name : GLOBAL_ATTRIBUTES) {
		Attribute attribute = nc.findGlobalAttribute(name);
		if (attribute != null && attribute.getStringValue() != null && !attribute.getStringValue().isEmpty()) {
		    ret.globalAttributes.put(name, attribute.getStringValue());
		}
	    }

	    ret.times = readTimes(nc);
	    ret.readStations(nc);

	    for (String name : VARIABLES) {
		Variable variable = nc.findVariable(name);
		if (variable == null) {
		    continue;
		}

		Map<String, String> attributes = new LinkedHashMap<>();
		for (String attributeName : VARIABLE_ATTRIBUTES) {
		    Attribute attribute = variable.findAttribute(attributeName);
		    if (attribute != null && attribute.getStringValue() != null) {
			attributes.put(attributeName, attribute.getStringValue());
		    }
		}
		ret.variableAttributes.put(name, attributes);

		ret.readValues(variable);
	    }

	} catch (InvalidRangeException e) {

	    throw new IOException(e);
	}

	GSLoggerFactory.getLogger(CanSWEDataset.class).info("CanSWE file {} loaded in {} ms: {} stations, {} times", file.getName(),
		System.currentTimeMillis() - start, ret.stations.size(), ret.times.length);

	return ret;
    }

    /**
     * @param nc
     * @throws IOException
     */
    private void readStations(NetcdfFile nc) throws IOException {

	String[] ids = readStrings(nc, "station_id");
	String[] names = readStrings(nc, "station_name");
	String[] namesSec = readStrings(nc, "station_name_sec");
	String[] namesTer = readStrings(nc, "station_name_ter");
	String[] idsSec = readStrings(nc, "station_id_sec");
	String[] idsTer = readStrings(nc, "station_id_ter");
	String[] sources = readStrings(nc, "source");
	double[] lats = readDoubles(nc, "lat", ids.length);
	double[] lons = readDoubles(nc, "lon", ids.length);
	double[] elevations = readDoubles(nc, "elevation", ids.length);

	Variable typeVariable = nc.findVariable("type_mes");
	Array types = typeVariable == null ? null : typeVariable.read();

	for (int i = 0; i < ids.length; i++) {

	    CanSWEStation station = new CanSWEStation();
	    station.setId(ids[i] == null ? "station-" + i : ids[i].trim());
	    station.setName(names[i]);
	    station.setNameSecondary(namesSec[i]);
	    station.setNameTertiary(namesTer[i]);
	    station.setIdSecondary(idsSec[i]);
	    station.setIdTertiary(idsTer[i]);
	    station.setSource(sources[i]);
	    station.setLatitude(lats[i]);
	    station.setLongitude(lons[i]);
	    station.setElevation(elevations[i]);
	    if (types != null && i < types.getSize()) {
		station.setMeasurementType(types.getLong(i));
	    }

	    stations.add(station);
	    stationsById.put(station.getId(), station);
	}
    }

    /**
     * Reads the valid values of a (station_id, time) variable, block by block to limit the memory usage
     *
     * @param variable
     * @throws IOException
     * @throws InvalidRangeException
     */
    private void readValues(Variable variable) throws IOException, InvalidRangeException {

	int[] shape = variable.getShape();
	int nStations = Math.min(shape[0], stations.size());
	int nTimes = Math.min(shape[1], times.length);

	int stationBlock = DEFAULT_STATION_BLOCK;
	int timeBlock = DEFAULT_TIME_BLOCK;
	Attribute chunks = variable.findAttribute("_ChunkSizes");
	if (chunks != null && chunks.getLength() == 2) {
	    stationBlock = Math.max(1, chunks.getNumericValue(0).intValue());
	    timeBlock = Math.max(1, chunks.getNumericValue(1).intValue());
	}

	double fill = Double.NaN;
	Attribute fillAttribute = variable.findAttribute("_FillValue");
	if (fillAttribute != null && fillAttribute.getNumericValue() != null) {
	    fill = fillAttribute.getNumericValue().doubleValue();
	}

	String name = variable.getShortName();

	for (int s0 = 0; s0 < nStations; s0 += stationBlock) {

	    int ns = Math.min(stationBlock, nStations - s0);

	    int[] counts = new int[ns];
	    int[][] stationTimes = new int[ns][16];
	    float[][] stationValues = new float[ns][16];

	    for (int t0 = 0; t0 < nTimes; t0 += timeBlock) {

		int nt = Math.min(timeBlock, nTimes - t0);

		Array array = variable.read(new int[] { s0, t0 }, new int[] { ns, nt });

		for (int s = 0; s < ns; s++) {
		    for (int t = 0; t < nt; t++) {

			float value = array.getFloat(s * nt + t);
			if (!isValid(value, fill)) {
			    continue;
			}

			if (counts[s] == stationTimes[s].length) {
			    stationTimes[s] = Arrays.copyOf(stationTimes[s], counts[s] * 2);
			    stationValues[s] = Arrays.copyOf(stationValues[s], counts[s] * 2);
			}
			stationTimes[s][counts[s]] = t0 + t;
			stationValues[s][counts[s]] = value;
			counts[s]++;
		    }
		}
	    }

	    for (int s = 0; s < ns; s++) {
		stations.get(s0 + s).setSeries(name, Arrays.copyOf(stationTimes[s], counts[s]), Arrays.copyOf(stationValues[s], counts[s]));
	    }
	}
    }

    /**
     * @param value
     * @param fill
     * @return
     */
    static boolean isValid(double value, double fill) {

	if (Double.isNaN(value) || Double.isInfinite(value)) {
	    return false;
	}

	if (!Double.isNaN(fill) && (value == fill || Math.abs(value - fill) <= Math.abs(fill) * 1e-6)) {
	    return false;
	}

	// the netCDF default float fill value
	return Math.abs(value) < 9.9e36;
    }

    /**
     * @param nc
     * @param name
     * @return
     * @throws IOException
     */
    private static String[] readStrings(NetcdfFile nc, String name) throws IOException {

	Variable variable = nc.findVariable(name);
	if (variable == null) {
	    Variable ids = nc.findVariable("station_id");
	    return new String[ids == null ? 0 : (int) ids.getSize()];
	}

	Array array = variable.read();
	String[] ret = new String[(int) array.getSize()];
	for (int i = 0; i < ret.length; i++) {
	    Object object = array.getObject(i);
	    ret[i] = object == null ? null : object.toString();
	}

	return ret;
    }

    /**
     * @param nc
     * @param name
     * @param size
     * @return
     * @throws IOException
     */
    private static double[] readDoubles(NetcdfFile nc, String name, int size) throws IOException {

	double[] ret = new double[size];
	Arrays.fill(ret, Double.NaN);

	Variable variable = nc.findVariable(name);
	if (variable == null) {
	    return ret;
	}

	Array array = variable.read();
	for (int i = 0; i < size && i < array.getSize(); i++) {
	    ret[i] = array.getDouble(i);
	}

	return ret;
    }

    /**
     * @param nc
     * @return the times in milliseconds
     * @throws IOException
     */
    private static long[] readTimes(NetcdfFile nc) throws IOException {

	Variable time = nc.findVariable(TIME_VARIABLE);
	if (time == null) {
	    throw new IOException("Time variable not found");
	}

	Attribute units = time.findAttribute("units");
	if (units == null) {
	    throw new IOException("Time units not found");
	}

	Array array = time.read();
	long[] ret = new long[(int) array.getSize()];
	for (int i = 0; i < ret.length; i++) {
	    ret[i] = toDate(array.getDouble(i), units.getStringValue()).map(Date::getTime).orElseThrow(() -> new IOException("Unsupported time"));
	}

	return ret;
    }

    /**
     * Converts a CF time value (e.g. units "days since 1928-01-01 00:00:00", proleptic gregorian calendar) to a date
     *
     * @param value
     * @param units
     * @return
     */
    public static Optional<Date> toDate(double value, String units) {

	if (Double.isNaN(value) || Double.isInfinite(value) || units == null) {
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
     * @param stationId
     * @return the station with the given id
     */
    public Optional<CanSWEStation> getStation(String stationId) {

	return stationId == null ? Optional.empty() : Optional.ofNullable(stationsById.get(stationId.trim()));
    }

    /**
     * @param station
     * @param variable
     * @param begin inclusive, null for no lower limit
     * @param end inclusive, null for no upper limit
     * @return the valid values of the given station and variable in the given time range
     */
    public List<SimpleEntry<Date, Double>> getValues(CanSWEStation station, String variable, Date begin, Date end) {

	Integer beginIndex = getFirstIndexNotBefore(begin);
	Integer endIndex = getLastIndexNotAfter(end);

	int[] indexes = station.getTimeIndexes(variable);
	float[] values = station.getValues(variable);

	List<SimpleEntry<Date, Double>> ret = new ArrayList<>();
	for (int i = 0; i < indexes.length; i++) {
	    if ((beginIndex != null && indexes[i] < beginIndex) || (endIndex != null && indexes[i] > endIndex)) {
		continue;
	    }
	    // the values are stored as floats
	    ret.add(new SimpleEntry<>(getDate(indexes[i]), Double.valueOf(Float.toString(values[i]))));
	}

	return ret;
    }

    /**
     * @return
     */
    public List<CanSWEStation> getStations() {

	return Collections.unmodifiableList(stations);
    }

    /**
     * @param index index of the time dimension
     * @return
     */
    public Date getDate(int index) {

	return new Date(times[index]);
    }

    /**
     * @param date
     * @return the index of the first time not before the given date, or null if the date is null
     */
    public Integer getFirstIndexNotBefore(Date date) {

	if (date == null) {
	    return null;
	}

	int index = Arrays.binarySearch(times, date.getTime());
	return index >= 0 ? index : -index - 1;
    }

    /**
     * @param date
     * @return the index of the last time not after the given date, or null if the date is null
     */
    public Integer getLastIndexNotAfter(Date date) {

	if (date == null) {
	    return null;
	}

	int index = Arrays.binarySearch(times, date.getTime());
	return index >= 0 ? index : -index - 2;
    }

    /**
     * @param variable
     * @return the attributes of the given variable
     */
    public Map<String, String> getVariableAttributes(String variable) {

	return variableAttributes.getOrDefault(variable, Collections.emptyMap());
    }

    /**
     * @return
     */
    public Map<String, String> getGlobalAttributes() {

	return globalAttributes;
    }
}
