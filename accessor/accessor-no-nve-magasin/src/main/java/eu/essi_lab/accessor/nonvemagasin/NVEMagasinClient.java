package eu.essi_lab.accessor.nonvemagasin;

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

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the reservoir statistics API (Magasinstatistikk) of the Norwegian Water Resources and Energy Directorate (NVE)
 * (https://biapi.nve.no/magasinstatistikk/swagger/index.html).<br>
 * Every week NVE collects the water level of the most important reservoirs (about 490) and publishes the filling of the reservoirs
 * aggregated over the whole country, the electricity price areas and the watercourse regions. The API has no parameters: the
 * whole archive (weekly values since 1995, about 4 MB) is returned by a single request, the most recent week by another one.<br>
 * A time series is identified by an area (type and number) and a property (see {@link NVEMagasinProperty}); each value is dated
 * at the date of its ISO week published by the API (dato_Id, the Sunday ending the week), at midnight UTC.
 *
 * @author boldrini
 */
public class NVEMagasinClient {

    public static final String DEFAULT_ENDPOINT = "https://biapi.nve.no/magasinstatistikk/";

    static final String API_PATH = "api/Magasinstatistikk/";

    static final String DATA_PATH = API_PATH + "HentOffentligData";
    static final String LATEST_WEEK_PATH = API_PATH + "HentOffentligDataSisteUke";
    // HentOmråder
    static final String AREAS_PATH = API_PATH + "HentOmr%C3%A5der";

    // keys of the areas and of the weekly values
    public static final String AREA_TYPE = "omrType";
    public static final String AREA_NUMBER = "omrnr";
    public static final String AREA_NAME = "navn";
    public static final String AREA_LONG_NAME = "navn_langt";
    public static final String AREA_DESCRIPTION = "beskrivelse";
    public static final String DATE = "dato_Id";
    public static final String NEXT_PUBLICATION = "neste_Publiseringsdato";

    // keys of the groups of areas
    static final String[] AREA_GROUPS = new String[] { "land", "elspot", "vassdrag" };

    // keys added by the client
    public static final String SERIES_ID = "seriesId";
    public static final String PROPERTY = "property";
    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String VALUES_COUNT = "valuesCount";

    private static final String SERIES_ID_SEPARATOR = "|";

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public NVEMagasinClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public NVEMagasinClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the time series of the given properties in the areas of the given types. Series without values are discarded (e.g.
     * the watercourse region 4, listed by the API without values)
     *
     * @param properties
     * @param areaTypes the area types (e.g. "NO", "EL", "VASS"), empty for all
     * @return the series sorted by area (in the order of the API) and property
     * @throws GSException
     */
    public List<NVEMagasinEntity> retrieveSeries(Collection<NVEMagasinProperty> properties, Collection<String> areaTypes)
	    throws GSException {

	List<JSONObject> areas = parseAreas(new JSONArray(get(AREAS_PATH)));
	JSONArray data = new JSONArray(get(DATA_PATH));

	return createSeries(areas, data, properties, areaTypes);
    }

    /**
     * @param areas the areas listed by the API
     * @param data the weekly values
     * @param properties
     * @param areaTypes the area types, empty for all
     * @return
     */
    static List<NVEMagasinEntity> createSeries(List<JSONObject> areas, JSONArray data, Collection<NVEMagasinProperty> properties,
	    Collection<String> areaTypes) {

	LinkedHashMap<String, JSONObject> areaMap = new LinkedHashMap<>();
	for (JSONObject area : areas) {
	    areaMap.put(buildAreaCode(area.optString(AREA_TYPE), area.optInt(AREA_NUMBER, -1)), area);
	}

	// areas with values not listed by the API are added at the end
	for (int i = 0; i < data.length(); i++) {
	    JSONObject row = data.getJSONObject(i);
	    String code = buildAreaCode(row.optString(AREA_TYPE), row.optInt(AREA_NUMBER, -1));
	    if (!areaMap.containsKey(code)) {
		JSONObject area = new JSONObject();
		area.put(AREA_TYPE, row.optString(AREA_TYPE));
		area.put(AREA_NUMBER, row.optInt(AREA_NUMBER, -1));
		areaMap.put(code, area);
	    }
	}

	List<NVEMagasinEntity> ret = new ArrayList<>();

	for (JSONObject area : areaMap.values()) {

	    String type = area.optString(AREA_TYPE);
	    if (!areaTypes.isEmpty() && areaTypes.stream().noneMatch(t -> t.equalsIgnoreCase(type))) {
		continue;
	    }

	    for (NVEMagasinProperty property : properties) {

		JSONObject object = new JSONObject(area.toString());
		object.put(PROPERTY, property.getField());
		object.put(SERIES_ID, buildSeriesId(type, area.optInt(AREA_NUMBER, -1), property));

		NVEMagasinEntity series = new NVEMagasinEntity(object);

		TreeMap<Date, BigDecimal> values = parseObservations(data, series, null, null);
		if (values.isEmpty()) {
		    continue;
		}

		object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
		object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(values.lastKey()));
		object.put(VALUES_COUNT, values.size());

		ret.add(series);
	    }
	}

	return ret;
    }

    /**
     * @param response the response of the HentOmråder operation
     * @return the areas of all the groups
     */
    static List<JSONObject> parseAreas(JSONArray response) {

	List<JSONObject> ret = new ArrayList<>();

	for (int i = 0; i < response.length(); i++) {

	    JSONObject groups = response.getJSONObject(i);

	    for (String group : AREA_GROUPS) {
		JSONArray areas = groups.optJSONArray(group);
		if (areas != null) {
		    for (int j = 0; j < areas.length(); j++) {
			ret.add(areas.getJSONObject(j));
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * Retrieves the values of the given period. The API has no parameters, so the whole archive is downloaded and filtered
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(NVEMagasinEntity series, Date begin, Date end) throws GSException {

	return parseObservations(new JSONArray(get(DATA_PATH)), series, begin, end);
    }

    /**
     * @param series
     * @return the date of the most recent value of the series, read from the values of the most recent week
     * @throws GSException
     */
    public Optional<Date> retrieveLatestObservation(NVEMagasinEntity series) throws GSException {

	TreeMap<Date, BigDecimal> values = parseObservations(new JSONArray(get(LATEST_WEEK_PATH)), series, null, null);

	return values.isEmpty() ? Optional.empty() : Optional.of(values.lastKey());
    }

    /**
     * @param data the weekly values
     * @param series
     * @param begin the begin of the period, or null
     * @param end the end of the period, or null
     * @return the values of the series in the given period, missing values discarded
     */
    static TreeMap<Date, BigDecimal> parseObservations(JSONArray data, NVEMagasinEntity series, Date begin, Date end) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	Optional<NVEMagasinProperty> property = series.getProperty();
	if (property.isEmpty()) {
	    return ret;
	}

	String field = property.get().getField();

	for (int i = 0; i < data.length(); i++) {

	    JSONObject row = data.getJSONObject(i);

	    if (!row.optString(AREA_TYPE).equalsIgnoreCase(series.getAreaType()) || row.optInt(AREA_NUMBER, -1) != series.getAreaNumber()) {
		continue;
	    }

	    if (!row.has(field) || row.isNull(field)) {
		continue;
	    }

	    BigDecimal value = row.optBigDecimal(field, null);
	    if (value == null) {
		continue;
	    }

	    Optional<Date> date = parseDate(row.optString(DATE, null));
	    if (date.isEmpty() || (begin != null && date.get().before(begin)) || (end != null && date.get().after(end))) {
		continue;
	    }

	    ret.put(date.get(), value);
	}

	return ret;
    }

    /**
     * @param type
     * @param number
     * @return e.g. "EL|1"
     */
    static String buildAreaCode(String type, int number) {

	return type + SERIES_ID_SEPARATOR + number;
    }

    /**
     * @param type
     * @param number
     * @param property
     * @return e.g. "EL|1|fyllingsgrad"
     */
    static String buildSeriesId(String type, int number, NVEMagasinProperty property) {

	return buildAreaCode(type, number) + SERIES_ID_SEPARATOR + property.getField();
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static NVEMagasinEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] split = seriesId.split("\\" + SERIES_ID_SEPARATOR, -1);
	if (split.length != 3 || split[0].isEmpty()) {
	    return null;
	}

	int number;
	try {
	    number = Integer.parseInt(split[1]);
	} catch (NumberFormatException e) {
	    return null;
	}

	Optional<NVEMagasinProperty> property = NVEMagasinProperty.decode(split[2]);
	if (property.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(split[0], number, property.get()));
	object.put(AREA_TYPE, split[0]);
	object.put(AREA_NUMBER, number);
	object.put(PROPERTY, property.get().getField());

	return new NVEMagasinEntity(object);
    }

    /**
     * @param text e.g. "2026-10-18"
     * @return the date at midnight UTC
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isEmpty() || text.equals("null")) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(LocalDate.parse(text.trim()).atStartOfDay(ZoneOffset.UTC).toInstant()));
	} catch (DateTimeParseException e) {
	    return ISO8601DateTimeUtils.parseISO8601ToDate(text);
	}
    }

    /**
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() != 200) {
		throw createException("NVE Magasinstatistikk " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    if (text.isBlank()) {
		throw createException("NVE Magasinstatistikk " + path + " returned an empty response", null);
	    }

	    return text;

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("NVE Magasinstatistikk " + path + " request failed", e);
	}
    }

    /**
     * @param message
     * @param cause
     * @return
     */
    private GSException createException(String message, Exception cause) {

	GSLoggerFactory.getLogger(getClass()).error(message);

	// the overload taking only the cause uses its message as description, and fails when there is no cause
	return GSException.createException(//
		getClass(), //
		message, //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		getClass().getSimpleName() + "_ERROR", //
		cause);
    }

    /**
     * @param endpoint
     * @return
     */
    static String normalizeEndpoint(String endpoint) {

	if (endpoint == null || endpoint.isEmpty()) {
	    return DEFAULT_ENDPOINT;
	}

	String normalized = endpoint.trim();
	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	return normalized;
    }
}
