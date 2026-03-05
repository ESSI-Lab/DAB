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
import java.util.List;

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
	    o.put("Locations@iot.navigationLink", baseUrl + "Things(" + id + ")/Locations");
	    o.put("Datastreams@iot.navigationLink", baseUrl + "Things(" + id + ")/Datastreams");
	}
	return o;
    }

    public static JSONObject location(String id, BigDecimal lon, BigDecimal lat, String name, String baseUrl) {
	return location(id,lon,lat,null,name,baseUrl);
    }

    /**
     * Builds a STA Location entity (GeoJSON Point).
     */
    public static JSONObject location(String id, BigDecimal lon, BigDecimal lat,BigDecimal altitude, String name, String baseUrl) {
	JSONObject o = new JSONObject();
	o.put("@iot.id", id);
	o.put("name", name != null ? name : id);
	o.put("description", "");
	o.put("encodingType", "application/vnd.geo+json");
	JSONObject loc = new JSONObject();
	loc.put("type", "Point");
	JSONArray coords = new JSONArray().put(lon).put(lat);
	if (altitude != null) {
	    coords.put(altitude);
	}
	loc.put("coordinates", coords);
	o.put("location", loc);
	if (baseUrl != null) {
	    o.put("Things@iot.navigationLink", baseUrl + "Locations(" + id + ")/Things");
	}
	return o;
    }

    /**
     * Builds a STA FeatureOfInterest entity.
     */
    public static JSONObject featureOfInterest(String id, BigDecimal lon, BigDecimal lat, String name, String baseUrl) {
	JSONObject o = new JSONObject();
	o.put("@iot.id", id);
	o.put("name", name != null ? name : id);
	o.put("description", "");
	o.put("encodingType", "application/vnd.geo+json");
	JSONObject loc = new JSONObject();
	loc.put("type", "Point");
	loc.put("coordinates", new JSONArray().put(lon).put(lat));
	o.put("feature", loc);
	if (baseUrl != null) {
	    o.put("Observations@iot.navigationLink", baseUrl + "FeaturesOfInterest(" + id + ")/Observations");
	}
	return o;
    }

    /**
     * Builds a STA Observation entity.
     */
    public static JSONObject observation(String id, Object result, String phenomenonTime, String resultTime,
	    String datastreamId, String featureOfInterestId, String baseUrl) {
	JSONObject o = new JSONObject();
	o.put("@iot.id", id);
	o.put("result", result);
	o.put("phenomenonTime", phenomenonTime);
	o.put("resultTime", resultTime);
	if (datastreamId != null && baseUrl != null) {
	    o.put("Datastream@iot.navigationLink", baseUrl + "Observations(" + id + ")/Datastream");
	}
	if (featureOfInterestId != null && baseUrl != null) {
	    o.put("FeatureOfInterest@iot.navigationLink", baseUrl + "Observations(" + id + ")/FeatureOfInterest");
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
		    idx = base.indexOf("/FeaturesOfInterest");
		    if (idx >= 0) {
			base = base.substring(0, idx);
		    }
		}
	    }
	}
	if (!base.endsWith("/")) {
	    base += "/";
	}
	return base;
    }
}
