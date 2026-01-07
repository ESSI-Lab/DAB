/**
 * 
 */
package eu.essi_lab.profiler.wis.observations;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.access.datacache.Response;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISRequest.CollectionItems;
import eu.essi_lab.profiler.wis.WISRequest.Parameter;
import eu.essi_lab.profiler.wis.WISRequest.TopRequest;
import eu.essi_lab.profiler.wis.WISUtils;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;

/**
 * @author boldrini
 */
public class WISObservationsHandler extends DefaultRequestHandler {

    private static final String WIS_HANDLER_ERROR = "WIS_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    WISRequest wr = new WISRequest(request);
	    TopRequest topRequest = wr.getTopRequest();
	    CollectionItems collectionItem = wr.getCollectionItem();
	    if (topRequest != null && topRequest.equals(TopRequest.COLLECTIONS) && collectionItem.equals(CollectionItems.OBSERVATIONS)) {
		ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	    } else {
		ret.setResult(ValidationResult.VALIDATION_FAILED);
	    }
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}
	return ret;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	DataCacheConnector dataCacheConnector = null;

	try {

	    dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();

	    if (dataCacheConnector == null) {
		DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
		dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
		String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
		String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
		String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
		dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
		dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
		dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
		DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
	    }

	    WISRequest wis = new WISRequest(webRequest);

	    String ret = null;

	    String topic = wis.getTopic();

	    JSONObject value = new JSONObject();
	    value.put("type", "FeatureCollection");

	    String sortBy = wis.getParameterValue(Parameter.SORT_BY);
	    boolean ascendOrder = true;
	    if (sortBy != null && !sortBy.isEmpty()) {
		if (sortBy.startsWith("-")) {
		    ascendOrder = false;
		}
	    }
	    String limitString = wis.getParameterValue(Parameter.LIMIT);
	    Integer limit = 10;
	    if (limitString != null && !limitString.isEmpty()) {
		limit = Integer.parseInt(limitString);
	    }
	    JSONArray features = new JSONArray();

	    HashMap<String, StationRecord> map = new HashMap<>();

	    List<SimpleEntry<String, String>> sufficientProperties = new ArrayList<>();
	    List<SimpleEntry<String, String>> necessaryProperties = new ArrayList<>();

	    if (topic.equals("whos-plata") || topic.equals("whos-arctic")) {

		switch (topic) {
		case "whos-plata":
		    sufficientProperties.add(new SimpleEntry<String, String>("sourceIdentifier", "brazil-ana"));
		    sufficientProperties.add(new SimpleEntry<String, String>("sourceIdentifier", "brazil-inmet"));
		    sufficientProperties.add(new SimpleEntry<String, String>("sourceIdentifier", "brazil-inmet-plata"));
		    sufficientProperties.add(new SimpleEntry<String, String>("sourceIdentifier", "brazil-ana-sar"));
		    sufficientProperties.add(new SimpleEntry<String, String>("sourceIdentifier", "argentina-ina"));
		    sufficientProperties.add(new SimpleEntry<String, String>("sourceIdentifier", "uruguay-dinagua"));
		    sufficientProperties.add(new SimpleEntry<String, String>("sourceIdentifier", "uruguay-inumet"));
		    sufficientProperties.add(new SimpleEntry<String, String>("sourceIdentifier", "paraguay-dmh"));
		    break;
		case "whos-arctic":
		    sufficientProperties.add(new SimpleEntry<String, String>("sourceIdentifier", "wmo-bnhs-arctic"));
		default:
		    break;
		}
	    }
	    HashMap<String, StationRecord> dss = new HashMap<String, StationRecord>();
	    String stationIdentifier = wis.getParameterValue(Parameter.WIGOS_STATION_IDENTIFIER);
	    List<String> dataIdentifiers = new ArrayList<>();
	    String nameParameter = wis.getParameterValue(Parameter.NAME);

	    if (stationIdentifier != null) {
		List<StationRecord> datasets = dataCacheConnector.getStationsWithProperties(null, 0, 10000, true,
			new SimpleEntry<String, String>("platformIdentifier", stationIdentifier));
		for (StationRecord dataset : datasets) {
		    if (nameParameter != null && !nameParameter.isEmpty()) {
			if (!dataset.getObservedProperty().equalsIgnoreCase(nameParameter)) {
			    continue;
			}
		    }
		    dss.put(dataset.getDataIdentifier(), dataset);
		    dataIdentifiers.add(dataset.getDataIdentifier());
		}
		if (!dataIdentifiers.isEmpty()) {
		    sufficientProperties.clear();
		    for (String dataIdentifier : dataIdentifiers) {
			sufficientProperties.add(new SimpleEntry<>("dataIdentifier", dataIdentifier));
		    }
		}
	    }

	    Date begin = null;
	    Date end = null;
	    String dateTime = wis.getParameterValue(Parameter.DATE_TIME);
	    if (dateTime != null && !dateTime.isEmpty()) {
		if (dateTime.contains("/")) {
		    String[] split = dateTime.split("/");
		    begin = parseTime(split[0]);
		    end = parseTime(split[1]);
		} else {
		    begin = parseTime(dateTime);
		    end = parseTime(dateTime);
		}
	    }

	    Response<DataRecord> response = dataCacheConnector.getRecordsWithProperties(limit, begin, end, ascendOrder, necessaryProperties,
		    sufficientProperties);
	    List<DataRecord> records = response.getRecords();

	    // Date now = new Date();
	    // Date lastYear = new Date(now.getTime() - TimeUnit.DAYS.toMillis(365));
	    String url = WISUtils.getUrl(webRequest);
	    SimpleEntry<String, String> sourceParameter = WISUtils.extractSourceAndParameter(topic);
	    String sourceId = sourceParameter.getKey();
	    String parameterURI = sourceParameter.getValue();
	    String view = WISUtils.getWHOSView(sourceId, parameterURI);
	    url = url.replace("/view/whos/", "/view/" + view + "/");

	    for (DataRecord record : records) {
		JSONObject feature = new JSONObject();
		JSONArray conformArray = new JSONArray();
		conformArray.put("http://www.wmo.int/spec/om-profile-1/1.0/req/geojson");
		feature.put("conformsTo", conformArray);
		JSONObject properties = new JSONObject();
		properties.put("description", (String) null);
		properties.put("fxxyyy", "undefined");
		StationRecord station = dss.get(record.getDataIdentifier());
		if (station == null) {
		    continue;
		}
		feature.put("id", record.getDataIdentifier() + "-" + record.getDate().getTime());
		properties.put("index", 25);
		String name = station.getObservedProperty();
		if (name == null) {
		    name = "undefined";
		}
		properties.put("name", name);

		properties.put("phenomenonTime", ISO8601DateTimeUtils.getISO8601DateTime(record.getDate()));
		properties.put("resultTime", ISO8601DateTimeUtils.getISO8601DateTime(record.getDate()));
		String units = station.getUnits();
		if (units == null) {
		    units = "undefined";
		}
		properties.put("units", units);
		JSONArray metadata = new JSONArray();
		JSONObject stationName = new JSONObject();
		stationName.put("name", "station_or_site_name");
		stationName.put("units", "CCITT IA5");
		// properties.put("url", "https://oscar.wmo.int/surface");
		properties.put("value", record.getValue());
		if (station != null) {
		    WISUtils.addGeometry(feature, station.getBbox4326().getWest().doubleValue(),
			    station.getBbox4326().getSouth().doubleValue());
		    properties.put("wigos_station_identifier", station.getPlatformIdentifier());
		}
		stationName.put("description", station.getPlatformName());
		metadata.put(stationName);
		properties.put("metadata", metadata);

		// Date lastObservation = station.getLastObservation();
		// if (lastObservation.after(lastYear)) {
		// properties.put("status", "operational");
		// }
		// Integer observations = station.getLastDayObservations();
		// if (observations != null) {
		// properties.put("num_obs", observations);
		// }
		feature.put("properties", properties);

		feature.put("type", "Feature");

		JSONArray links = new JSONArray();
		// WISUtils.addLink(links, "application/json", "collection", view, url + "/collections/" + view);
		// feature.put("links", links);
		features.put(feature);

	    }

	    value.put("features", features);

	    value.put("numberMatched", response.getTotal());
	    value.put("numberReturned", features.length());

	    JSONArray links = new JSONArray();

	    WISUtils.addLink(links, "application/json", "collection", "Surface weather observations from " + topic,
		    url + "/collections/" + topic + "/items");

	    WISUtils.addLink(links, "application/geo+json", "self", "This document as GeoJSON",
		    url + "/collections/" + topic + "/items?f=json");

	    // WISUtils.addLink(links, "application/json", "collection", "Discovery metadata", url +
	    // "/collections/discovery-metadata/items");

	    value.put("links", links);
	    //
	    value.put("timeStamp", ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds());
	    // ret = json.toString();

	    return value.toString();

	} catch (

	Exception e) {
	    e.printStackTrace();

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WIS_HANDLER_ERROR, //
		    e);
	}

    }

    private Date parseTime(String time) {
	if (time.length() < 4) {
	    return null;
	}
	Optional<Date> ret = ISO8601DateTimeUtils.parseISO8601ToDate(time);
	if (ret.isPresent()) {
	    return ret.get();
	}
	return null;
    }

    private void addToMap(HashMap<String, StationRecord> map, List<StationRecord> tmp) {
	for (StationRecord station : tmp) {
	    String id = station.getPlatformIdentifier();
	    StationRecord cached = map.get(id);
	    if (cached == null) {
		map.put(id, station);
	    } else {
		Integer a = cached.getLastDayObservations();
		if (a == null) {
		    a = 0;
		}
		Integer b = station.getLastDayObservations();
		if (b == null) {
		    b = 0;
		}
		cached.setLastDayObservations(a + b);
		map.put(id, cached);
	    }

	}

    }

    private List<String> getStations(IDiscoveryStringExecutor discoveryExecutor, DiscoveryMessage discoveryMessage, int offset, int page)
	    throws Exception {

	List<String> ret = new ArrayList<>();
	Page userPage = discoveryMessage.getPage();
	userPage.setStart(offset);
	userPage.setSize(page);
	discoveryMessage.setPage(userPage);

	ResultSet<String> resultSet = discoveryExecutor.retrieveStrings(discoveryMessage);
	List<String> results = resultSet.getResultsList();
	for (String result : results) {
	    String id = result.substring(result.indexOf("<gs:uniquePlatformId>"), result.indexOf("</gs:uniquePlatformId>"));
	    id = id.replace("<gs:uniquePlatformId>", "");
	    ret.add(id);
	}

	return ret;

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }
}
