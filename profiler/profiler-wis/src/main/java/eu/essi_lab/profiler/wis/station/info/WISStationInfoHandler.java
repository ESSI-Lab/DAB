/**
 * 
 */
package eu.essi_lab.profiler.wis.station.info;

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

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISRequest.CollectionItems;
import eu.essi_lab.profiler.wis.WISRequest.CollectionOperation;
import eu.essi_lab.profiler.wis.WISRequest.Parameter;
import eu.essi_lab.profiler.wis.WISRequest.TopRequest;
import eu.essi_lab.profiler.wis.WISTransformer;
import eu.essi_lab.profiler.wis.WISUtils;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;

/**
 * @author boldrini
 */
public class WISStationInfoHandler extends DefaultRequestHandler {

    private static final String WIS_HANDLER_ERROR = "WIS_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	// try {
	// WISRequest wr = new WISRequest(request);
	// TopRequest topRequest = wr.getTopRequest();
	// if (topRequest.equals(TopRequest.PROCESSES)) {
	// ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	// } else {
	// ret.setResult(ValidationResult.VALIDATION_FAILED);
	// }
	// } catch (Exception e) {
	// ret.setResult(ValidationResult.VALIDATION_FAILED);
	// }
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
	    TopRequest topRequest = wis.getTopRequest();

	    String limistString = wis.getParameterValue(Parameter.LIMIT);

	    Integer limit = 10000;
	    if (limistString != null && !limistString.isEmpty()) {
		limit = Integer.parseInt(limistString);
	    }

	    String offsetString = wis.getParameterValue(Parameter.OFFSET);

	    Integer offset = 0;
	    if (offsetString != null && !offsetString.isEmpty()) {
		offset = Integer.parseInt(offsetString);
	    }

	    CollectionItems item = wis.getCollectionItem();
	    CollectionOperation operation = wis.getCollectionOperation();

	    String ret = null;
	    boolean execution = wis.isProcessExecution();

	    if (topRequest.equals(TopRequest.PROCESSES) && !execution) {
		String template = WISUtils.getResourceAsString("wis2box/stationInfoProcess.json");

		// ret = json.toString();
		ret = WISUtils.filter(webRequest, template);
		return ret;
	    }

	    String topic = null;
	    // String view = null;

	    if (topRequest.equals(TopRequest.PROCESSES)) {

		String request = IOUtils.toString(wis.getBody(), StandardCharsets.UTF_8);
		JSONObject json = new JSONObject(request);
		JSONObject inputs = json.getJSONObject("inputs");
		topic = inputs.getString("collection");

	    }

	    if (topRequest != null && topRequest.equals(TopRequest.COLLECTIONS) && item != null && item.equals(CollectionItems.STATIONS)
		    && operation != null && operation.equals(CollectionOperation.GET_ITEMS)) {
		// view = webRequest.extractViewId().get();
	    }

	    JSONObject response = new JSONObject();

	    response.put("id", "path");
	    response.put("code", "success");
	    JSONObject value = new JSONObject();
	    value.put("type", "FeatureCollection");
	    response.put("value", value);

	    JSONArray features = new JSONArray();

	    HashMap<String, StationRecord> map = new HashMap<>();
	    GSSource source = null;
	    String parameterURI = null;

	    if (topic != null) {
		// String[] split = topic.split("\\.");
		// String country3 = split[0];
		// String org = split[1];
		//
		// Country c = Country.decode(country3);
		// if (c != null) {
		//
		// source = findSource(c, org);
		// if (source != null) {
		// view = "gs-view-source(" + source.getUniqueIdentifier() + ")";
		// }
		//
		// }
		SimpleEntry<String, String> sourceParameter = WISUtils.extractSourceAndParameter(topic);
		if (sourceParameter.getValue() != null) {
		    parameterURI = "http://hydro.geodab.eu/hydro-ontology/concept/" + sourceParameter.getValue();
		}
		source = ConfigurationWrapper.getSource(sourceParameter.getKey());
		// view = "gs-view-and(gs-view-source(" + sourceParameter.getKey()k + "))";
	    }
	    String url = WISUtils.getUrl(webRequest);

	    if (source != null) {
		// here stations are retrieved from the data cache

		String view = WISUtils.getWHOSView(source.getUniqueIdentifier(), parameterURI);
		url = url.replace("/view/whos/", "/view/" + view + "/");

		List<SimpleEntry<String, String>> properties = new ArrayList<>();
		properties.add(new SimpleEntry<String, String>("sourceIdentifier", source.getUniqueIdentifier()));

		List<StationRecord> srs = dataCacheConnector.getStationsWithProperties(null, offset, limit, false,
			Arrays.asList(new String[] { "platformIdentifier" }), properties.toArray(new SimpleEntry[] {}));// platformIdentifier
															// must
															// Exist
		GSLoggerFactory.getLogger(getClass()).info("adding {} datasets", srs.size());
		addToMap(map, srs);

		GSLoggerFactory.getLogger(getClass()).info("added {} stations", map.size());

	    } else {
		// here stations are retrieved from the database
		// (there isn't however information about number of observations)
		ServiceLoader<IDiscoveryStringExecutor> loader = ServiceLoader.load(IDiscoveryStringExecutor.class);
		IDiscoveryStringExecutor discoveryExecutor = loader.iterator().next();
		WISTransformer transformer = new WISTransformer();
		DiscoveryMessage discoveryMessage = transformer.transform(webRequest);
		ResultSet<String> resultSet = null;
		int tempSize = 0;
		List<String> stations;
		int page = 1000;
		int brokerOffset = 1;
		// while (!(stations = getStations(discoveryExecutor, discoveryMessage, brokerOffset, page)).isEmpty())
		// {
		stations = getStations(discoveryExecutor, discoveryMessage, brokerOffset, page);
		List<SimpleEntry<String, String>> properties = new ArrayList<>();
		for (String station : stations) {
		    properties.add(new SimpleEntry<String, String>("platformIdentifier", station));
		}
		List<StationRecord> tmp = dataCacheConnector.getStationsWithProperties(null, 0, 10000, false,
			properties.toArray(new SimpleEntry[] {}));
		addToMap(map, tmp);
		brokerOffset += page;
		// break;
		// }
	    }

	    Date now = new Date();
	    Date lastYear = new Date(now.getTime() - TimeUnit.DAYS.toMillis(365));

	    for (StationRecord station : map.values()) {

		String top = topic;
		if (!wis.isProcessExecution()) {
		    top = station.getSourceIdentifier();
		}

		JSONObject feature = new JSONObject();
		String stationId = station.getPlatformIdentifier();
		if (stationId == null) {
		    continue;
		}
		feature.put("id", station.getPlatformIdentifier());
		feature.put("type", "Feature");
		WISUtils.addGeometry(feature, station.getBbox4326().getWest().doubleValue(),
			station.getBbox4326().getSouth().doubleValue());
		JSONObject properties = new JSONObject();
		properties.put("wigos_station_identifier", station.getPlatformIdentifier());
		properties.put("name", station.getPlatformName());
		properties.put("id", station.getPlatformIdentifier());

		properties.put("url", "https://oscar.wmo.int/surface");
		properties.put("topic", top);
		Date lastObservation = station.getLastObservation();
		if (lastObservation.after(lastYear)) {
		    properties.put("status", "operational");
		}
		Integer observations = station.getLastDayObservations();
		if (observations != null) {
		    properties.put("num_obs", observations);
		}
		feature.put("properties", properties);
		JSONArray links = new JSONArray();
		WISUtils.addLink(links, "application/json", "canonical", top, url + "/collections/discovery-metadata/items/" + top);
		feature.put("links", links);
		features.put(feature);

	    }

	    value.put("features", features);

	    value.put("numberMatched", features.length());
	    value.put("numberReturned", features.length());

	    JSONArray links = new JSONArray();

	    WISUtils.addLink(links, "application/json", "collection", "Stations", url + "/collections/stations/items");
	    String t = "";
	    if (topic != null && !topic.isEmpty()) {
		t = "&topic=" + topic;
	    }
	    WISUtils.addLink(links, "application/geo+json", "self", "This document as GeoJSON",
		    url + "/collections/stations/items?f=json" + t);

	    //
	    // WISUtils.addLink(links, "application/json", "collection", "Discovery metadata", url +
	    // "/collections/discovery-metadata/items");

	    value.put("links", links);
	    //
	    value.put("timeStamp", ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds());
	    // ret = json.toString();

	    if (topRequest != null && topRequest.equals(TopRequest.COLLECTIONS) && item != null && item.equals(CollectionItems.STATIONS)
		    && operation != null && operation.equals(CollectionOperation.GET_ITEMS)) {
		return value.toString();
	    }
	    // if (topRequest != null && topRequest.equals(TopRequest.PROCESSES)) {
	    return response.toString();
	    // }

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

    private GSSource findSource(Country c, String org) {
	GSSource ret = ConfigurationWrapper.getSource(c.getShortName().toLowerCase() + "-" + org.toLowerCase());
	if (ret != null) {
	    return ret;
	}
	ret = ConfigurationWrapper.getSource(c.getISO3().toLowerCase() + "-" + org.toLowerCase());
	return ret;

    }

    private void addToMap(HashMap<String, StationRecord> map, List<StationRecord> tmp) {
	for (StationRecord station : tmp) {

	    String id = station.getPlatformIdentifier();
	    // System.out.println(id);
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
	GSLoggerFactory.getLogger(getClass()).info("added " + map.values().size() + " stations");

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
