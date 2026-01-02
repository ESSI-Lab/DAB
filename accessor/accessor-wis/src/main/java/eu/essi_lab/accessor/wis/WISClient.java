package eu.essi_lab.accessor.wis;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class WISClient {

    private String endpoint;
    private String observationsUrl;
    private String stationsUrl;
    private String metadataUrl;

    public WISClient(String endpoint) {
	if (endpoint.endsWith("/")) {
	    endpoint = endpoint.substring(0, endpoint.length() - 1);
	}
	this.endpoint = endpoint;
	String collectionURL = endpoint + "/collections?f=json";

	JSONObject json = retrieveJSONObject(collectionURL);
	JSONArray collections = json.getJSONArray("collections");
	for (int i = 0; i < collections.length(); i++) {
	    JSONObject collection = collections.getJSONObject(i);
	    JSONArray keywords = collection.getJSONArray("keywords");
	    for (int j = 0; j < keywords.length(); j++) {
		String keyword = keywords.getString(j);
		if (keyword.equals("default")||keyword.equals("observations")) {
		    this.observationsUrl = getLink(collection, "application/geo+json", "items");
		}
	    }
	    String id = collection.getString("id");
	    if (id.toLowerCase().contains("station")) {
		this.stationsUrl = getLink(collection, "application/geo+json", "items");
	    }
	    if (id.toLowerCase().contains("metadata")) {
		this.metadataUrl = getLink(collection, "application/geo+json", "items");
	    }
	}

    }

    private HashMap<String, Station> stations = new HashMap<>();

    public JSONObject getMetadata() {
	JSONObject json = retrieveJSONObject(metadataUrl);
	return json;
    }
    
    public void retrieveStations() {
	if (stations.isEmpty()) {
	    synchronized (stations) {
		if (stations.isEmpty()) {
		    String url = stationsUrl;
		    while (url != null) {
			JSONObject json = retrieveJSONObject(url);
			JSONArray features = json.getJSONArray("features");
			for (int i = 0; i < features.length(); i++) {
			    JSONObject feature = features.getJSONObject(i);
			    Station station = new Station();
			    if (feature.has("geometry")) {
				JSONObject geometry = feature.getJSONObject("geometry");
				String type = geometry.getString("type");
				if (type.equals("Point")) {
				    JSONArray coordinates = geometry.getJSONArray("coordinates");
				    switch (coordinates.length()) {
				    case 3:
					station.setAltitude(coordinates.getBigDecimal(2));
				    case 2:
					station.setLatitude(coordinates.getBigDecimal(1));
					station.setLongitude(coordinates.getBigDecimal(0));
					break;
				    default:
					break;
				    }
				}
			    }
			    if (feature.has("properties")) {
				JSONObject props = feature.getJSONObject("properties");
				if (props.has("name")) {
				    station.setName(props.getString("name"));
				}
				if (props.has("wigos_station_identifier")) {
				    station.setWigosId(props.getString("wigos_station_identifier"));
				    if (props.has("topic")) { // only stations having a topic are harvested
					stations.put(station.getWigosId(), station);
				    }
				}

			    }
			}
			url = getLink(json, "application/geo+json", "next");
		    }
		}
	    }
	}

    }

    public List<Station> getStations() {
	retrieveStations();

	return new ArrayList<>(stations.values());
    }

    public Station getStation(String id) {
	retrieveStations();

	return stations.get(id);
    }

    private String getLink(JSONObject collection, String linkType, String relation) {
	JSONArray links = collection.getJSONArray("links");
	for (int i = 0; i < links.length(); i++) {
	    JSONObject link = links.getJSONObject(i);
	    String type = link.getString("type");
	    if (type.equals(linkType)) {
		if (relation != null) {
		    String rel = link.getString("rel");
		    if (rel.equals(relation)) {
			return link.getString("href");
		    }
		} else {
		    return link.getString("href");
		}
	    }
	}
	return null;
    }

    private JSONObject retrieveJSONObject(String url) {
	Downloader downloader = new Downloader();
	String string = downloader.downloadOptionalString(url).get();
	JSONObject ret = new JSONObject(string);
	return ret;
    }

    public HashSet<ObservedProperty> getVariables(String wigosId) {
	HashSet<ObservedProperty> ret = new HashSet<>();
	String url = observationsUrl + "&wigos_station_identifier=" + wigosId + "&properties=name,units&skipGeometry=true&limit=10000";
	JSONObject json = retrieveJSONObject(url);
	JSONArray features = json.getJSONArray("features");
	for (int i = 0; i < features.length(); i++) {
	    JSONObject feature = features.getJSONObject(i);
	    if (feature.has("properties")) {
		JSONObject props = feature.getJSONObject("properties");
		if (props.has("name")) {
		    String name = props.getString("name");
		    String units = null;
		    if (props.has("units")) {
			units = props.getString("units");
		    }
		    ObservedProperty op = new ObservedProperty(name, units);
		    ret.add(op);
		}
	    }
	}
	return ret;
    }

    public List<Observation> getObservations(String wigosId, String propertyName, Date begin, Date end, Integer maxRecords,
	    boolean ascendingOrder) {
	List<Observation> ret = new ArrayList<>();
	String sorting = "";
	if (!ascendingOrder) {
	    sorting = "-";
	}

	int pageSize = 1000;

	if (maxRecords != null && maxRecords < pageSize) {
	    pageSize = maxRecords;
	}

	String dateTime = "";
	if (begin != null || end != null) {
	    String beginParameter = "..";
	    if (begin != null) {
		beginParameter = ISO8601DateTimeUtils.getISO8601DateTime(begin);
	    }
	    String endParameter = "..";
	    if (end != null) {
		endParameter = ISO8601DateTimeUtils.getISO8601DateTime(end);
	    }
	    dateTime = "&datetime=" + beginParameter + "/" + endParameter;
	}

	String url = observationsUrl + "&lang=en-US&limit=" + pageSize + "&properties=reportTime,value&skipGeometry=true" + //
		"&sortby=" + sorting + "reportTime" + "&offset=0&name=" + propertyName + "&wigos_station_identifier=" + wigosId + //
		dateTime //
	;
	w: while (url != null) {
	    JSONObject json = retrieveJSONObject(url);
	    JSONArray features = json.getJSONArray("features");
	    for (int i = 0; i < features.length(); i++) {
		JSONObject feature = features.getJSONObject(i);
		if (feature.has("properties")) {
		    JSONObject props = feature.getJSONObject("properties");
		    String time = props.optString("resultTime");
		    if (time ==null||time.isEmpty()) {
			time = props.optString("reportTime");
		    }
		    if (time!=null && props.has("value")) {
			if (time.contains("/")) {
			    time = time.substring(time.indexOf("/") + 1);
			}
			Optional<Date> date = ISO8601DateTimeUtils.parseISO8601ToDate(time);
			if (date.isPresent()) {
			    String value = props.get("value").toString();
			    Observation o = new Observation(date.get(), value);
			    if (maxRecords == null || ret.size() < maxRecords) {
				ret.add(o);
			    } else {
				break w;
			    }

			} else {
			    GSLoggerFactory.getLogger(getClass()).error("error parsing date " + time);
			}
		    }
		}
	    }
	    url = getLink(json, "application/geo+json", "next");
	}
	return ret;
    }

}
