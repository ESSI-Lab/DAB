package eu.essi_lab.profiler.timeseries;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class Feature {

    private BigDecimal lat;
    private BigDecimal lon;
    private String onlineResource;

    public String getOnlineResource() {
	return onlineResource;
    }

    public void setOnlineResource(String onlineResource) {
	this.onlineResource = onlineResource;
    }

    private JSONTimeseries series;

    public BigDecimal getLat() {
	return lat;
    }

    public void setLat(BigDecimal lat) {
	this.lat = lat;
    }

    public BigDecimal getLon() {
	return lon;
    }

    public void setLon(BigDecimal lon) {
	this.lon = lon;
    }

    public JSONTimeseries getSeries() {
	return series;
    }

    public void setSeries(JSONTimeseries series) {
	this.series = series;
    }

    public JSONObject getJSONObject() {
	JSONObject feature = new JSONObject();
	feature.put("type", "Feature");

	JSONObject geometry = new JSONObject();
	geometry.put("type", "Point");
	List<BigDecimal> coordinates = new ArrayList<>();
	coordinates.add(lon);
	coordinates.add(lat);
	geometry.put("coordinates", coordinates);
	feature.put("geometry", geometry);

	JSONObject properties = new JSONObject();
	properties.put("timeseries", series.getJSONObject());

	feature.put("properties", properties);
	return feature;
    }

}
