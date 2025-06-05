package eu.essi_lab.accessor.kisters;

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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.kisters.KISTERSEntity.EntityType;
import eu.essi_lab.lib.net.downloader.Downloader;

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
    private String endpoint;

    public KISTERSClient(String endpoint) {
	this.endpoint = endpoint;
    }

    /**
     * @return
     */
    private List<String> getStationFields() {

	return Arrays.asList(

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
	);
    }

    /**
     * @return
     */
    private List<String> getTimeSeriesFields() {

	return Arrays.asList(

		TS_ID, //
		STATION_ID, //
		SITE_ID, //
		TS_UNIT_SYMBOL, //
		TS_NAME, //
		TS_UNIT_NAME, //
		TS_SPACING, //
		"coverage", //
		"ca_sta&ca_sta_returnfields=GRDCCOUNTRY");
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
	String response = downloader.downloadOptionalString(query).get();

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

	String returnfields = getStationFields().//
		stream().//
		collect(Collectors.joining(","));

	return retrieveEntities(
		"service=kisters&type=queryServices&request=getStationList&format=json&returnfields=" + returnfields + "&flatten=true");
    }
    
    /**
     * @return
     */
    public List<KISTERSEntity> retrieveStationsBySiteName(String name) {

	String returnfields = getStationFields().//
		stream().//
		collect(Collectors.joining(","));

	return retrieveEntities(
		"service=kisters&type=queryServices&request=getStationList&site_name="+name+"&format=json&returnfields=" + returnfields + "&flatten=true");
    }

    /**
     * @param timeSeriesId
     * @return
     */
    public KISTERSEntity retrieveTimeSeries(String timeSeriesId) {

	String returnfields = getTimeSeriesFields().//
		stream().//
		collect(Collectors.joining(","));

	return retrieveEntities("service=kisters&type=queryServices&request=getTimeseriesList&ts_id=" + timeSeriesId
		+ "&format=json&station_no=*&returnfields=" + returnfields).get(0);
    }

    /**
     * @return
     */
    public List<KISTERSEntity> retrieveTimeSeries() {

	String returnfields = getTimeSeriesFields().//
		stream().//
		collect(Collectors.joining(","));

	return retrieveEntities(
		"service=kisters&type=queryServices&request=getTimeseriesList&format=json&station_no=*&returnfields=" + returnfields);
    }

    /**
     * @return
     */
    public List<KISTERSEntity> retrieveTimeSeriesByStation(String stationNumber) {

	String returnfields = getTimeSeriesFields().//
		stream().//
		collect(Collectors.joining(","));

	return retrieveEntities("service=kisters&type=queryServices&request=getTimeseriesList&format=json&station_id=" + stationNumber
		+ "&returnfields=" + returnfields);
    }

    /**
     * @param query
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<KISTERSEntity> retrieveEntities(String query) {

	query = getEndpoint() + query;

	Downloader downloader = new Downloader();
	String response = downloader.downloadOptionalString(query).get();

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
     * @return
     */
    private String getEndpoint() {

	return endpoint;
    }
}
