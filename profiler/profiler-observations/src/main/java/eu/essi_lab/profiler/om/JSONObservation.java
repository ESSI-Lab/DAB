package eu.essi_lab.profiler.om;

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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class JSONObservation {

    JSONObject timeseries = new JSONObject();
    JSONObject metadata = new JSONObject();
    JSONObject defaultPointMetadata = new JSONObject();
    JSONArray points = new JSONArray();
    JSONArray parameters = new JSONArray();
    private ObservationType type;

    public enum ObservationType {
	TrajectoryObservation, TimeSeriesObservation, SamplingSurfaceObservation
    }

    public ObservationType getType() {
	return type;
    }

    public JSONObservation(ObservationType type) {
	this.type = type;
	timeseries.put("type", type.name());
	JSONObject result = new JSONObject();
	result.put("metadata", metadata);
	result.put("defaultPointMetadata", defaultPointMetadata);
	result.put("points", points);
	timeseries.put("result", result);
	timeseries.put("parameter", parameters);
    }

    // OBSERVATION
    public void setId(String id) {
	timeseries.put("id", id);
    }

    public void setPhenomenonTime(Date begin, Date end) {
	JSONObject time = new JSONObject();
	time.put("begin", ISO8601DateTimeUtils.getISO8601DateTime(begin));
	time.put("end", ISO8601DateTimeUtils.getISO8601DateTime(end));
	timeseries.put("phenomenonTime", time);
    }

    public void setObservedProperty(String href, String title) {
	JSONObject observedProperty = new JSONObject();
	observedProperty.put("href", href);
	observedProperty.put("title", title);
	timeseries.put("observedProperty", observedProperty);
    }

    public String getId() {
	if (timeseries.has("id")) {
	    return timeseries.getString("id");
	}
	return null;
    }

    public String getObservedPropertyTitle() {
	if (timeseries.has("observedProperty")) {
	    JSONObject observedProperty = timeseries.getJSONObject("observedProperty");
	    if (observedProperty.has("title")) {
		return observedProperty.getString("title");
	    }
	}
	return null;
    }

    public void setProcedure(String href, String title) {
	JSONObject link = new JSONObject();
	link.put("href", href);
	link.put("title", title);
	timeseries.put("procedure", link);
    }

    public void setFeatureOfInterest(String href, String title) {
	JSONObject link = new JSONObject();
	link.put("href", href);
	link.put("title", title);
	timeseries.put("featureOfInterest", link);
    }

    public void setFeatureOfInterest(JSONFeature platform) {

	timeseries.put("featureOfInterest", platform.getJSONObject());
    }

    public JSONFeature getFeatureOfInterest() {
	if (timeseries.has("featureOfInterest")) {
	    JSONObject foi = timeseries.getJSONObject("featureOfInterest");
	    JSONFeature ret = getJSONFeature(foi);
	    return ret;
	}
	return null;
    }

    public JSONFeature getJSONFeature(JSONObject foi) {
	return new JSONFeature(foi);
    }

    public void setResultTime(Date date) {
	timeseries.put("resultTime", ISO8601DateTimeUtils.getISO8601DateTime(date));
    }

    // METADATA

    public void setIntendedObservationSpacing(Duration period) {
	metadata.put("intendedObservationSpacing", period.toString());
    }

    // DEFAULT POINT METADATA
    public void setInterpolationType(String href, String title) {
	JSONObject link = new JSONObject();
	link.put("href", href);
	link.put("title", title);
	defaultPointMetadata.put("interpolationType", link);
    }

    public void setQuality(String href, String title) {
	JSONObject link = new JSONObject();
	link.put("href", href);
	link.put("title", title);
	defaultPointMetadata.put("quality", link);
    }

    public void setUOM(String uom) {
	defaultPointMetadata.put("uom", uom);
    }

    public void setAggregationDuration(Duration duration) {
	defaultPointMetadata.put("aggregationDuration", duration.toString());
    }

    // POINTS
    public void addPoint(Date date, BigDecimal value) {
	JSONObject point = new JSONObject();
	JSONObject time = new JSONObject();
	time.put("instant", ISO8601DateTimeUtils.getISO8601DateTime(date));
	point.put("time", time);
	point.put("value", value);
	points.put(point);
    }

    public void addPointAndLocation(Date date, BigDecimal value, List<Double> coordinates) {
	JSONObject point = new JSONObject();
	JSONObject time = new JSONObject();
	time.put("instant", ISO8601DateTimeUtils.getISO8601DateTime(date));
	JSONObject shape = new JSONObject();
	shape.put("type","Point");
	JSONArray coords = new JSONArray();
	for (Double c : coordinates) {
	    coords.put(c);
	}
	shape.put("coordinates", coords);
	point.put(getGeometryName(), shape);
	point.put("time", time);
	point.put("value", value);
	points.put(point);
    }

    public String getGeometryName() {
   	return "shape";
       }

    // UTILS
    public JSONObject getJSONObject() {
	return timeseries;
    }

    public void addParameter(String name, String value) {
	JSONObject parameter = new JSONObject();
	parameter.put("name", name);
	parameter.put("value", value);
	parameters.put(parameter);

    }

}
