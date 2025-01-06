package eu.essi_lab.profiler.om;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONFeature {

    JSONObject platform = new JSONObject();
    JSONArray parameters = new JSONArray();
    JSONArray relatedParties = new JSONArray();

    public JSONFeature(JSONObject platform) {
	this.platform = platform;
    }

    public JSONFeature() {
	platform.put("type", "MonitoringPoint");
	platform.put("parameter", parameters);
	platform.put("relatedParty", relatedParties);
    }

    // OBSERVATION

    public void setId(String href) {

	platform.put("id", href);

    }

    public void setName(String name) {

	platform.put("name", name);

    }

    public String getSampledFeatureTitle() {
	if (platform.has("name")) {
	    return platform.getString("name");
	}
	return null;
    }

    public void addParameter(String name, String value) {
	JSONObject parameter = new JSONObject();
	parameter.put("name", name);
	parameter.put("value", value);
	parameters.put(parameter);

    }

    public void addRelatedParty(String organisationName, String individualName, String email, String role, String url) {
	JSONObject party = new JSONObject();
	if (isDocumented(organisationName))
	    party.put("organisationName", organisationName);
	if (isDocumented(individualName))
	    party.put("individualName", individualName);
	if (isDocumented(email))
	    party.put("electronicMailAddress", email);
	if (isDocumented(role))
	    party.put("role", role);
	if (isDocumented(url))
	    party.put("URL", url);
	relatedParties.put(party);

    }

    public void addRelatedParty(JSONObject party) {

	relatedParties.put(party);
    }

    public static JSONObject createRelatedParty(String organisationName, String individualName, String email, String role, String url) {
	JSONObject party = new JSONObject();
	if (isDocumented(organisationName))
	    party.put("organisationName", organisationName);
	if (isDocumented(individualName))
	    party.put("individualName", individualName);
	if (isDocumented(email))
	    party.put("electronicMailAddress", email);
	if (isDocumented(role))
	    party.put("role", role);
	if (isDocumented(url))
	    party.put("URL", url);
	return party;
    }

    private static boolean isDocumented(String organisationName) {
	return organisationName != null && !organisationName.equals("");
    }

    // UTILS
    public JSONObject getJSONObject() {
	return platform;
    }

    public List<Double> getCoordinates() {
	List<Double> ret = new ArrayList<Double>();

	JSONObject coordObj = platform.optJSONObject(getGeometryName());
	if (coordObj != null && !coordObj.isEmpty()) {
	    JSONArray coordinates = coordObj.optJSONArray("coordinates");
	    for (int i = 0; i < coordinates.length(); i++) {
		BigDecimal coord = coordinates.getBigDecimal(i);
		ret.add(coord.doubleValue());
	    }
	    return ret;
	}

	return null;
    }

    public void setLatLon(BigDecimal lat, BigDecimal lon) {
	JSONObject shape = new JSONObject();
	shape.put("type", "Point");
	shape.put("coordinates", getCoordinateArray(lon, lat));
	platform.put(getGeometryName(), shape);

    }

    public void setMultiPoints(List<List<BigDecimal>> points) {
	JSONObject shape = new JSONObject();
	shape.put("type", "MultiPoint");
	JSONArray coordinates = new JSONArray();
	for (List<BigDecimal> point : points) {
	    coordinates.put(getCoordinateArray(point.toArray(new BigDecimal[] {})));
	}
	shape.put("coordinates", coordinates);
	platform.put(getGeometryName(), shape);

    }

    public void setBBOX(BigDecimal bign, BigDecimal bigw, BigDecimal bigs, BigDecimal bige) {
	JSONObject shape = new JSONObject();
	shape.put("type", "Polygon");
	JSONArray coordinates = new JSONArray();

	JSONArray coordinateArray = new JSONArray();

	JSONArray exteriorRing = new JSONArray();

	exteriorRing.put(getCoordinateArray(bigw, bigs));
	exteriorRing.put(getCoordinateArray(bigw, bign));
	exteriorRing.put(getCoordinateArray(bige, bign));
	exteriorRing.put(getCoordinateArray(bige, bigs));
	exteriorRing.put(getCoordinateArray(bigw, bigs));
	coordinateArray.put(exteriorRing);

	coordinates.put(coordinateArray);

	shape.put("coordinates", coordinates);
	platform.put(getGeometryName(), shape);

    }

    public String getGeometryName() {
	return "shape";
    }

    private JSONArray getCoordinateArray(BigDecimal... coordinates) {
	JSONArray ret = new JSONArray();
	for (BigDecimal coordinate : coordinates) {
	    ret.put(coordinate.doubleValue());
	}
	return ret;
    }

}
