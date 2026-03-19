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

package eu.essi_lab.profiler.sta;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utilities for building OGC STA compliant JSON responses.
 */
public final class STAJsonWriter {

    private STAJsonWriter() {
    }

    /**
     * Builds a STA collection response with value array and optional nextLink.
     */
    public static String collectionResponse(List<JSONObject> items, String nextLink, Integer count) {
	JSONObject root = new JSONObject();
	JSONArray value = new JSONArray();
	for (JSONObject item : items) {
	    value.put(item);
	}
	root.put("value", value);
	if (nextLink != null && !nextLink.isEmpty()) {
	    root.put("@iot.nextLink", nextLink);
	}
	if (count != null) {
	    root.put("@iot.count", count);
	}
	return root.toString();
    }

    /**
     * Builds a STA Thing entity.
     */
    public static JSONObject thing(String id, String name, String description, String baseUrl) {
	JSONObject o = new JSONObject();
	o.put("@iot.id", id);
	o.put("name", name != null ? name : id);
	o.put("description", description != null ? description : "");
	o.put("properties", new JSONObject());
	if (baseUrl != null) {
	    o.put("@iot.selfLink", baseUrl + "Things(" + id + ")");
	    o.put("Locations@iot.navigationLink", baseUrl + "Things(" + id + ")/Locations");
	    o.put("HistoricalLocations@iot.navigationLink", baseUrl + "Things(" + id + ")/HistoricalLocations");
	    o.put("Datastreams@iot.navigationLink", baseUrl + "Things(" + id + ")/Datastreams");
	    o.put("MultiDatastreams@iot.navigationLink", baseUrl + "Things(" + id + ")/MultiDatastreams");
	}
	return o;
    }

    public static JSONObject location(String id, BigDecimal lon, BigDecimal lat, String name,String description, String baseUrl) {
	return location(id,lon,lat,null,name, description, baseUrl);
    }

    /**
     * Builds a STA Location entity (GeoJSON Point).
     */
    public static JSONObject location(String id, BigDecimal lon, BigDecimal lat,BigDecimal altitude, String name, String description, String baseUrl) {
	JSONObject o = new JSONObject();
	o.put("@iot.id", id);
	o.put("name", name != null ? name : id);
	o.put("description", description);
	o.put("encodingType", "application/geo+json");
	JSONObject loc = new JSONObject();
	loc.put("type", "Point");
	JSONArray coords = new JSONArray().put(lon).put(lat);
	if (altitude != null) {
	    coords.put(altitude);
	}
	loc.put("coordinates", coords);
	o.put("location", loc);
	if (baseUrl != null) {
	    o.put("@iot.selfLink", baseUrl + "Locations(" + id + ")");
	    o.put("Things@iot.navigationLink", baseUrl + "Locations(" + id + ")/Things");
	    o.put("HistoricalLocations@iot.navigationLink", baseUrl + "Locations(" + id + ")/HistoricalLocations");
	}
	return o;
    }

    /**
     * Builds a STA FeatureOfInterest entity.
     */
    public static JSONObject featureOfInterest(String id, BigDecimal lon, BigDecimal lat, String name, String baseUrl) {
	return featureOfInterest(id, lon, lat, null, name, baseUrl);
    }

    /**
     * Builds a STA FeatureOfInterest entity (GeoJSON Point) with optional altitude.
     */
    public static JSONObject featureOfInterest(String id, BigDecimal lon, BigDecimal lat, BigDecimal altitude,
	    String name, String baseUrl) {
	JSONObject o = new JSONObject();
	if (baseUrl != null) {
	    o.put("@iot.selfLink", baseUrl + "FeaturesOfInterest(" + id + ")");
	}
	o.put("@iot.id", id);
	o.put("name", name != null ? name : id);
	o.put("description", "");
	o.put("encodingType", "application/geo+json");
	JSONObject feature = new JSONObject();
	JSONArray coords = new JSONArray().put(lon).put(lat);
	if (altitude != null) {
	    coords.put(altitude);
	}
	feature.put("coordinates", coords);
	feature.put("type", "Point");
	o.put("feature", feature);
	return o;
    }

    /**
     * Builds a STA Observation entity.
     */
    public static JSONObject observation(String id, Object result, String phenomenonTime, String resultTime,
	    String datastreamId, String featureOfInterestId, String baseUrl) {
	return observation((Object) id, result, phenomenonTime, resultTime, datastreamId, featureOfInterestId, baseUrl);
    }

    /**
     * Builds a STA Observation entity. Accepts id as String or Number (e.g. Long for numeric @iot.id).
     */
    public static JSONObject observation(Object id, Object result, String phenomenonTime, String resultTime,
	    String datastreamId, String featureOfInterestId, String baseUrl) {
	JSONObject o = new JSONObject();
	o.put("@iot.id", id);
	o.put("result", result);
	o.put("phenomenonTime", phenomenonTime);
	o.put("resultTime", resultTime);
	if (baseUrl != null) {
	    o.put("@iot.selfLink", baseUrl + "Observations(" + id + ")");
	    if (datastreamId != null) {
		o.put("Datastream@iot.navigationLink", baseUrl + "Datastreams(" + datastreamId + ")");
	    }
	    if (featureOfInterestId != null) {
		o.put("FeatureOfInterest@iot.navigationLink", baseUrl + "FeaturesOfInterest(" + featureOfInterestId + ")");
	    }
	}
	return o;
    }

    /**
     * Builds a STA Datastream entity (timeseries record).
     */
    public static JSONObject datastream(String id, String name, String description, String observationType,
	    String unitName, String unitSymbol, String unitDefinition, BigDecimal lon, BigDecimal lat,
	    String phenomenonTime, JSONObject properties, String thingId, String baseUrl) {
	JSONObject o = new JSONObject();
	if (baseUrl != null) {
	    o.put("@iot.selfLink", baseUrl + "Datastreams(" + id + ")");
	}
	o.put("@iot.id", id);
	o.put("name", name != null ? name : id);
	o.put("description", description != null ? description : "");
	o.put("observationType", observationType != null ? observationType : "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
	JSONObject uom = new JSONObject();
	uom.put("name", unitName != null ? unitName : "");
	uom.put("symbol", unitSymbol != null ? unitSymbol : "");
	uom.put("definition", unitDefinition != null ? unitDefinition : JSONObject.NULL);
	o.put("unitOfMeasurement", uom);
	if (lon != null && lat != null) {
	    JSONObject observedArea = new JSONObject();
	    observedArea.put("coordinates", new JSONArray().put(lon).put(lat));
	    observedArea.put("type", "Point");
	    o.put("observedArea", observedArea);
	}
	o.put("phenomenonTime", phenomenonTime != null ? phenomenonTime : "");
	o.put("properties", properties != null ? properties : new JSONObject());
	if (baseUrl != null) {
	    o.put("ObservedProperty@iot.navigationLink", baseUrl + "Datastreams(" + id + ")/ObservedProperty");
	    o.put("Sensor@iot.navigationLink", baseUrl + "Datastreams(" + id + ")/Sensor");
	    o.put("Thing@iot.navigationLink", baseUrl + "Datastreams(" + id + ")/Thing");
	    o.put("Observations@iot.navigationLink", baseUrl + "Datastreams(" + id + ")/Observations");
	}
	return o;
    }

    /**
     * Builds a STA Sensor entity.
     */
    public static JSONObject sensor(String id, String name, String description, JSONObject properties, String baseUrl) {
	JSONObject o = new JSONObject();
	if (baseUrl != null) {
	    o.put("@iot.selfLink", baseUrl + "Sensors(" + id + ")");
	    o.put("Datastreams@iot.navigationLink", baseUrl + "Sensors(" + id + ")/Datastreams");
	    o.put("MultiDatastreams@iot.navigationLink", baseUrl + "Sensors(" + id + ")/MultiDatastreams");
	}
	o.put("@iot.id", id);
	o.put("name", name != null ? name : id);
	o.put("description", description != null ? description : "");
	o.put("properties", properties != null ? properties : new JSONObject());
	return o;
    }

    /**
     * Builds a STA ObservedProperty entity.
     */
    public static JSONObject observedProperty(long id, String name, String description, String definition,
	    JSONObject properties, String baseUrl) {
	JSONObject o = new JSONObject();
	o.put("@iot.id", id);
	o.put("name", name != null ? name : "");
	o.put("description", description != null ? description : "");
	o.put("definition", definition != null ? definition : JSONObject.NULL);
	o.put("properties", properties != null ? properties : new JSONObject());
	if (baseUrl != null) {
	    o.put("@iot.selfLink", baseUrl + "ObservedProperties(" + id + ")");
	    o.put("Datastreams@iot.navigationLink", baseUrl + "ObservedProperties(" + id + ")/Datastreams");
	    o.put("MultiDatastreams@iot.navigationLink", baseUrl + "ObservedProperties(" + id + ")/MultiDatastreams");
	}
	return o;
    }

    public static String buildBaseUrl(String requestUrl) {
	String base = requestUrl;
	int idx = base.indexOf("/Things");
	if (idx >= 0) {
	    base = base.substring(0, idx);
	} else {
	    idx = base.indexOf("/Locations");
	    if (idx >= 0) {
		base = base.substring(0, idx);
	    } else {
		idx = base.indexOf("/Observations");
		if (idx >= 0) {
		    base = base.substring(0, idx);
		} else {
		    idx = base.indexOf("/Datastreams");
		    if (idx >= 0) {
			base = base.substring(0, idx);
		    } else {
			idx = base.indexOf("/FeaturesOfInterest");
			if (idx >= 0) {
			    base = base.substring(0, idx);
			} else {
			    idx = base.indexOf("/ObservedProperties");
			    if (idx >= 0) {
				base = base.substring(0, idx);
			    } else {
				idx = base.indexOf("/Sensors");
				if (idx >= 0) {
				    base = base.substring(0, idx);
				}
			    }
			}
		    }
		}
	    }
	}
	if (!base.endsWith("/")) {
	    base += "/";
	}
	return base;
    }

    /**
     * Builds the @iot.nextLink URL for pagination when more results exist.
     * Uses resumptionToken only, with value from SearchAfter.
     *
     * @param baseUrl         base URL (e.g. https://host/FROST-Server/v1.1/)
     * @param entityPath      entity set path (e.g. Things, Locations)
     * @param resumptionToken value from SearchAfter (cursor for next page)
     * @param queryString     original query string to preserve filter params, or null
     * @return full URL for the next page
     */
    public static String buildNextLink(String baseUrl, String entityPath, String resumptionToken, String queryString) {
	String base = baseUrl + entityPath;
	Map<String, String> params = new LinkedHashMap<>();
	if (queryString != null && !queryString.isEmpty()) {
	    String toParse = queryString;
	    try {
		toParse = URLDecoder.decode(queryString, StandardCharsets.UTF_8);
	    } catch (Exception e) {
		/* keep original */
	    }
	    for (String pair : toParse.split("&")) {
		int eq = pair.indexOf('=');
		if (eq > 0) {
		    String key = pair.substring(0, eq);
		    String value = eq < pair.length() - 1 ? pair.substring(eq + 1) : "";
		    if (!"resumptionToken".equalsIgnoreCase(key) && !"$skip".equalsIgnoreCase(key)) {
			params.put(key, value);
		    }
		}
	    }
	}
	params.put("resumptionToken", resumptionToken);
	try {
	    String qs = params.entrySet().stream()
		    .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
		    .collect(Collectors.joining("&"));
	    return base + "?" + qs;
	} catch (Exception e) {
	    return base + "?resumptionToken=" + encode(resumptionToken);
	}
    }

    private static String encode(String s) {
	try {
	    return URLEncoder.encode(s != null ? s : "", StandardCharsets.UTF_8);
	} catch (Exception e) {
	    return s != null ? s : "";
	}
    }
}
