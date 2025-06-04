package eu.essi_lab.profiler.om;

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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
