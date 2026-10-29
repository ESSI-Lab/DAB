package eu.essi_lab.accessor.kisters;

import java.util.ArrayList;

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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.kisters.KISTERSEntity.EntityType;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class KISTERSClient {

    public static final String STATION_NAME = "station_name";
    public static final String STATION_ID = "station_id";
    public static final String STATION_LAT = "station_latitude";
    public static final String STATION_LON = "station_longitude";
    public static final String SITE_ID = "site_id";
    public static final String SITE_NAME = "site_name";
    public static final String SITE_LONG_NAME = "site_longname";
    public static final String PARAM_TYPE_ID = "parametertype_id";
    public static final String PARAM_TYPE_NAME = "parametertype_name";
    public static final String PARAM_TYPE_LONG_NAME = "parametertype_longname";
    public static final String RIVER_NAME = "river_name";
    public static final String OBJECT_TYPE = "object_type";

    public static final String TS_ID = "ts_id";
    public static final String TS_NAME = "ts_name";
    public static final String TS_UNIT_SYMBOL = "ts_unitsymbol";
    public static final String TS_UNIT_NAME = "ts_unitname";
    public static final String TS_SPACING = "ts_spacing";
    public static final String TS_FROM = "from";
    public static final String TS_TO = "to";
    public static final String GRDC_COUNTRY = "GRDCCOUNTRY";
    public static final String STATION_NO = "station_no";
    public static final String STATION_PARAM_LONG_NAME = "stationparameter_longname";
    private String endpoint;

    /**
     * @param endpoint the KiWIS endpoint, optionally with instance specific parameters (e.g.
     *        "https://timeseries.sepa.org.uk/KiWIS/KiWIS?datasource=0"); the request parameters are appended to it
     */
    public KISTERSClient(String endpoint) {
	this.endpoint = normalizeEndpoint(endpoint);
    }

    /**
     * Makes the endpoint ready to have the request parameters appended, by adding '?' or '&' if needed
     * 
     * @param endpoint
     * @return
     */
    static String normalizeEndpoint(String endpoint) {

	if (endpoint == null || endpoint.endsWith("?") || endpoint.endsWith("&")) {

	    return endpoint;
	}

	return endpoint + (endpoint.contains("?") ? "&" : "?");
    }

    /**
     * @param full if <code>false</code>, the site long name, the parameter type long name and the river name are not requested,
     *        since some KiWIS instances (e.g. HIC Flanders) do not support them and answer with an error
     * @return
     */
    private List<String> getStationFields(boolean full) {

	List<String> fields = new ArrayList<>(Arrays.asList(

		STATION_NAME, //
		STATION_ID, //
		STATION_LAT, //
		STATION_LON, //
		SITE_ID, //
		SITE_NAME, //
		SITE_LONG_NAME, //
		PARAM_TYPE_ID, //
		PARAM_TYPE_NAME, //
		PARAM_TYPE_LONG_NAME, //
		RIVER_NAME, //
		OBJECT_TYPE //
	));

	if (!full) {

	    fields.removeAll(Arrays.asList(SITE_LONG_NAME, PARAM_TYPE_LONG_NAME, RIVER_NAME));
	}

	return fields;
    }

    /**
     * The fields of the time series retrieved by group, which also carry the station name and position, and the long name of the
     * station parameter (e.g. "River Discharge")
     * 
     * @return
     */
    private List<String> getGroupTimeSeriesFields() {

	List<String> fields = getTimeSeriesFields(true);

	fields.addAll(fields.size() - 1, Arrays.asList(//
		STATION_NO, //
		STATION_NAME, //
		STATION_LAT, //
		STATION_LON, //
		SITE_NAME, //
		STATION_PARAM_LONG_NAME));

	return fields;
    }

    /**
     * @param withParameterType if <code>true</code>, the parameter type id and name of each time series are also requested; this
     *        is needed for KiWIS instances (e.g. SEPA) where a station measures more than one parameter, since the station list
     *        returns all of them joined by ';'
     * @return
     */
    private List<String> getTimeSeriesFields(boolean withParameterType) {

	List<String> fields = new ArrayList<>(Arrays.asList(

		TS_ID, //
		STATION_ID, //
		SITE_ID, //
		TS_UNIT_SYMBOL, //
		TS_NAME, //
		TS_UNIT_NAME, //
		TS_SPACING, //
		"coverage"));

	if (withParameterType) {

	    fields.add(PARAM_TYPE_ID);
	    fields.add(PARAM_TYPE_NAME);
	}

	// must be the last one, since it carries its own parameter
	fields.add("ca_sta&ca_sta_returnfields=GRDCCOUNTRY");

	return fields;
    }

    /**
     * @param timeSeriesId
     * @param from
     * @param to
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<KISTERSEntity> retrieveTimeSeriesValues(String timeSeriesId, String from, String to) {

	String query = getEndpoint() + "service=kisters&type=queryServices&request=getTimeseriesValues&format=json&ts_id=" + timeSeriesId
		+ "&from=" + from + "&to=" + to;

	Downloader downloader = new Downloader();
	Optional<String> optString = downloader.downloadOptionalString(query);
	
	if(optString.isEmpty()) {
	    
	    return new ArrayList<>();
	}
	
	String response = optString.get();

	if (isError(response)) {

	    return new ArrayList<>();
	}

	JSONArray responseArray = new JSONArray(response);

	JSONObject responseObject = responseArray.getJSONObject(0);
	List<String> columns = Arrays.asList(responseObject.getString("columns").split(","));

	List<KISTERSEntity> entities = responseObject.//
		getJSONArray("data").//
		toList().//
		stream().//
		filter(e -> !((List<?>) e).stream().anyMatch(Objects::isNull)).// filters out null values
		map(e -> new KISTERSEntity(columns, (List<String>) e, EntityType.TIME_SERIES_VALUES)).//
		collect(Collectors.toList());

	return entities;
    }

    /**
     * @return
     */
    public List<KISTERSEntity> retrieveStations() {

	return retrieveStationEntities("service=kisters&type=queryServices&request=getStationList&format=json&flatten=true");
    }

    /**
     * @return
     */
    public List<KISTERSEntity> retrieveStationsBySiteName(String name) {

	return retrieveStationEntities(
		"service=kisters&type=queryServices&request=getStationList&site_name=" + name + "&format=json&flatten=true");
    }

    /**
     * Requests the stations with all the return fields; if nothing is returned (the instance does not support some of them), the
     * request is repeated without the optional ones
     * 
     * @param query
     * @return
     */
    private List<KISTERSEntity> retrieveStationEntities(String query) {

	List<KISTERSEntity> entities = retrieveEntities(query + "&returnfields=" + joinFields(getStationFields(true)));

	if (entities.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).debug("No stations with the full return fields, retrying with the basic fields");

	    entities = retrieveEntities(query + "&returnfields=" + joinFields(getStationFields(false)));
	}

	return entities;
    }

    /**
     * Retrieves the time series of the given time series group (e.g. the "SPW-WS-DebitHR" group of Wallonia), with the name and
     * position of their station
     * 
     * @param groupId
     * @return
     */
    public List<KISTERSEntity> retrieveTimeSeriesByGroup(String groupId) {

	return retrieveEntities("service=kisters&type=queryServices&request=getTimeseriesList&format=json&timeseriesgroup_id=" + groupId
		+ "&returnfields=" + joinFields(getGroupTimeSeriesFields()));
    }

    /**
     * @param timeSeriesId
     * @return
     */
    public KISTERSEntity retrieveTimeSeries(String timeSeriesId) {

	List<KISTERSEntity> entities = retrieveTimeSeriesEntities(
		"service=kisters&type=queryServices&request=getTimeseriesList&ts_id=" + timeSeriesId + "&format=json&station_no=*");

	if (entities.isEmpty()) {

	    // some instances (e.g. HIC Flanders) fail when the station number wildcard is combined with the time series id
	    entities = retrieveTimeSeriesEntities(
		    "service=kisters&type=queryServices&request=getTimeseriesList&ts_id=" + timeSeriesId + "&format=json");
	}

	return entities.get(0);
    }

    /**
     * @return
     */
    public List<KISTERSEntity> retrieveTimeSeries() {

	return retrieveTimeSeriesEntities("service=kisters&type=queryServices&request=getTimeseriesList&format=json&station_no=*");
    }

    /**
     * @return
     */
    public List<KISTERSEntity> retrieveTimeSeriesByStation(String stationNumber) {

	return retrieveTimeSeriesEntities(
		"service=kisters&type=queryServices&request=getTimeseriesList&format=json&station_id=" + stationNumber);
    }

    /**
     * Requests the time series also with their parameter type; if nothing is returned (KiWIS answers with an error, which the
     * downloader turns into an empty response, when a return field is not supported by the instance), the request is repeated
     * with the legacy return fields
     * 
     * @param query
     * @return
     */
    private List<KISTERSEntity> retrieveTimeSeriesEntities(String query) {

	List<KISTERSEntity> entities = retrieveEntities(query + "&returnfields=" + joinFields(getTimeSeriesFields(true)));

	if (entities.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).debug("No time series with parameter type return fields, retrying with legacy fields");

	    entities = retrieveEntities(query + "&returnfields=" + joinFields(getTimeSeriesFields(false)));
	}

	return entities;
    }

    /**
     * @param fields
     * @return
     */
    private String joinFields(List<String> fields) {

	return fields.stream().collect(Collectors.joining(","));
    }

    /**
     * @param query
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<KISTERSEntity> retrieveEntities(String query) {

	query = getEndpoint() + query;

	Downloader downloader = new Downloader();
	Optional<String> optString = downloader.downloadOptionalString(query);
	
	if(optString.isEmpty()) {
	    
	    return new ArrayList<>();
	}
		
	String response = optString.get();

	if (isError(response)) {

	    return new ArrayList<>();
	}

	JSONArray responseArray = new JSONArray(response);

	JSONArray fiealdsArray = responseArray.getJSONArray(0);
	responseArray.remove(0);

	return responseArray.toList().//
		stream().//
		map(o -> (List<String>) o).//
		map(o -> new KISTERSEntity(fiealdsArray, o)).//
		collect(Collectors.toList());
    }

    /**
     * KiWIS answers with a JSON object instead of the expected JSON array in case of error (with HTTP status 200 in some
     * instances, e.g. when a return field is not supported)
     * 
     * @param response
     * @return
     */
    static boolean isError(String response) {

	String trimmed = response.trim();

	if (trimmed.startsWith("[")) {

	    return false;
	}

	String message = trimmed;

	try {
	    message = new JSONObject(trimmed).optString("message", trimmed);
	} catch (Exception e) {
	    // not a JSON object, e.g. an HTML page
	}

	GSLoggerFactory.getLogger(KISTERSClient.class).warn("KiWIS error response: {}",
		message.substring(0, Math.min(message.length(), 300)));

	return true;
    }

    /**
     * @return
     */
    private String getEndpoint() {

	return endpoint;
    }
}
