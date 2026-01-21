package eu.essi_lab.profiler.om;

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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.wml._2.WML2QualityCategory;

public class PointBuilder {

    JSONObject point = new JSONObject();
    
    public PointBuilder() {
    }
    
    public void setDateValue(Date date, BigDecimal value) {
	JSONObject time = new JSONObject();
	time.put("instant", ISO8601DateTimeUtils.getISO8601DateTime(date));
	point.put("time", time);
	point.put("value", value);
    }

    public JSONObject build() {
	return point;
    }

    public void setQuality(String quality) {
	if (quality==null) {
	    return;
	}
	JSONObject metadata = null;
	if (point.has("metadata")) {
	    metadata = point.getJSONObject("metadata");
	}else {
	    metadata = new JSONObject();
	    point.put("metadata", metadata);
	}
	WML2QualityCategory wmlQuality = WML2QualityCategory.decodeUri(quality);
	if (wmlQuality!=null) {
	    JSONObject qualityObject = new JSONObject();
	    qualityObject.put("term", wmlQuality.getLabel());
	    qualityObject.put("vocabulary", wmlQuality.getVocabulary());
	    metadata.put("quality", qualityObject);    
	}else{
	    JSONObject qualityObject = new JSONObject();
	    qualityObject.put("term", quality);
	    metadata.put("quality", qualityObject);
	}
	
    }

    public void setQualifiers(Map<String, String> qualifiers) {
	if (qualifiers == null || qualifiers.isEmpty()) {
	    return;
	}
	JSONObject metadata = null;
	if (point.has("metadata")) {
	    metadata = point.getJSONObject("metadata");
	} else {
	    metadata = new JSONObject();
	    point.put("metadata", metadata);
	}
	
	JSONArray qualifiersArray = new JSONArray();
	for (Map.Entry<String, String> entry : qualifiers.entrySet()) {
	    String key = entry.getKey();
	    String value = entry.getValue();
	    
	    if (value == null || value.trim().isEmpty()) {
		continue;
	    }
	    
	    JSONObject qualifierObject = new JSONObject();
	    
	    // Check if the value is a URI (starts with http)
	    WML2QualityCategory wmlQuality = null;
	    if (value.startsWith("http")) {
		wmlQuality = WML2QualityCategory.decodeUri(value);
		if (wmlQuality != null) {
		    qualifierObject.put("term", wmlQuality.getLabel());
		    qualifierObject.put("vocabulary", wmlQuality.getVocabulary());
		} else {
		    qualifierObject.put("term", value);
		}
	    } else {
		qualifierObject.put("term", value);
	    }
	    
	    // Add the qualifier key as a property if it's not the default "quality" key
	    if (!key.equals("quality") && !key.equals("qualityControlLevelCode")) {
		qualifierObject.put("key", key);
	    }
	    
	    qualifiersArray.put(qualifierObject);
	}
	
	if (qualifiersArray.length() > 0) {
	    if (qualifiersArray.length() == 1) {
		// If only one qualifier, put it as "quality" for backward compatibility
		metadata.put("quality", qualifiersArray.get(0));
	    } else {
		// If multiple qualifiers, put them as an array
		metadata.put("qualifiers", qualifiersArray);
		// Also put the first one as "quality" for backward compatibility
		metadata.put("quality", qualifiersArray.get(0));
	    }
	}
    }

    public void setLocation(String geometryName, List<Double> coordinates) {
	JSONObject shape = new JSONObject();
	shape.put("type", "Point");
	JSONArray coords = new JSONArray();
	for (Double c : coordinates) {
	    coords.put(c);
	}
	shape.put("coordinates", coords);
	point.put(geometryName, shape);
	
    }

}
