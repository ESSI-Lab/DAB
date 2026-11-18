package eu.essi_lab.accessor.hiscentral.sicilia;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests the stations and sensors (elements) of the Protezione Civile Regione Siciliana (CAE Datascape API, e.g.
 * https://siciliadrpc-aegis.caedns.it/platform or https://www.protezionecivilesicilia.it:8443/DatascapeA). One record
 * per element.
 *
 * @author Roberto
 */
public class HISCentralSiciliaConnector extends HarvestedQueryConnector<HISCentralSiciliaConnectorSetting> {

    static final String TYPE = "HISCentralSiciliaConnector";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	HISCentralSiciliaClient client = new HISCentralSiciliaClient(getSourceURL());

	Map<String, JSONObject> stations = new HashMap<>();
	JSONArray stationsArray = client.getStations();
	for (int i = 0; i < stationsArray.length(); i++) {
	    JSONObject station = stationsArray.getJSONObject(i);
	    stations.put(station.optString("i"), flatten(station));
	}

	JSONArray elements = client.getElements();

	GSLoggerFactory.getLogger(getClass()).info("Found {} stations and {} elements", stations.size(), elements.length());

	int maxRecords = getSetting().getMaxRecords().orElse(Integer.MAX_VALUE);
	int virtual = 0;
	int inactive = 0;

	for (int i = 0; i < elements.length() && ret.getRecordsAsList().size() < maxRecords; i++) {

	    JSONObject element = elements.getJSONObject(i);

	    if (isVirtual(element)) {
		virtual++;
		continue;
	    }

	    JSONObject station = stations.get(element.optString("stationId"));

	    if (station == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Station {} of element {} not found, skipping", //
			element.optString("stationId"), element.optString("elementId"));
		continue;
	    }

	    if (!isActive(station)) {
		inactive++;
		continue;
	    }

	    if (station.has(SUB_LOCATIONS)) {
		station = locateElement(client, station, element);
	    }

	    ret.addRecord(HISCentralSiciliaMapper.create(station, element));
	}

	GSLoggerFactory.getLogger(getClass()).info("Skipped {} virtual elements and {} elements of inactive stations", virtual, inactive);

	return ret;
    }

    /**
     * Key of the station state in the {@code /v3/locations} items
     */
    static final String STATE = "s";

    /**
     * Only the stations with state 0 provide data: the other ones (mostly s = 40, e.g. Lipari) return no data with
     * any data type and are not published. The enabling filter of {@code /v3/elements} does not exclude them.
     */
    static boolean isActive(JSONObject station) {

	return station.optInt(STATE, 0) == 0;
    }

    /**
     * Virtual elements are computed by Datascape from other elements of the same station (e.g. "Pioggia Raccolta in 1
     * Ora" from "Pioggia Cumulata") and are not published
     */
    static boolean isVirtual(JSONObject element) {

	return element.optBoolean("isVirtual", false);
    }

    /**
     * Key of the sub-locations array of the stations having more than one location (e.g. the dams, where some sensors
     * are placed downstream)
     */
    static final String SUB_LOCATIONS = "l";

    /**
     * Some stations (e.g. "Arancio Diga R2") have no name and coordinates at the first level, but an array "l" of
     * sub-locations, each one with its own name and coordinates. The main sub-location (t = 0, otherwise the first
     * one) provides the station name and coordinates; the array is kept to locate the sensors.
     *
     * @param station a {@code /v3/locations} item
     * @return the station with name and coordinates at the first level
     */
    static JSONObject flatten(JSONObject station) {

	JSONArray subLocations = station.optJSONArray(SUB_LOCATIONS);
	if (subLocations == null || subLocations.isEmpty() || station.has("n")) {
	    return station;
	}

	JSONObject main = subLocations.getJSONObject(0);
	for (int i = 0; i < subLocations.length(); i++) {
	    if (subLocations.getJSONObject(i).optInt("t", -1) == 0) {
		main = subLocations.getJSONObject(i);
		break;
	    }
	}

	JSONObject ret = new JSONObject(main.toString());
	ret.put("i", station.opt("i"));
	ret.put("o", station.optString("o"));
	if (station.has(STATE)) {
	    ret.put(STATE, station.opt(STATE));
	}
	ret.put(SUB_LOCATIONS, subLocations);
	return ret;
    }

    /**
     * Sensors of multi-location stations can have their own position (_strictElementLongitude/Latitude in the element
     * detail); in this case the station coordinates are replaced with the sensor ones
     */
    static JSONObject locateElement(HISCentralSiciliaClient client, JSONObject station, JSONObject element) {

	String elementId = element.optString("elementId");

	try {

	    JSONObject detail = client.getElement(elementId);
	    return locate(station, detail);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(HISCentralSiciliaConnector.class).warn("Unable to get the detail of element {}, using the station location: {}",
		    elementId, e.getMessage());
	    return station;
	}
    }

    /**
     * @param station a flattened station
     * @param detail a {@code /v3/elements/{id}} response
     */
    static JSONObject locate(JSONObject station, JSONObject detail) {

	if (detail.isNull("_strictElementLongitude") || detail.isNull("_strictElementLatitude")) {
	    return station;
	}

	double lon = detail.getDouble("_strictElementLongitude");
	double lat = detail.getDouble("_strictElementLatitude");

	JSONObject ret = new JSONObject(station.toString());
	ret.put("x", lon);
	ret.put("y", lat);
	ret.remove("z"); // the station altitude does not apply to a sensor placed elsewhere

	if (!detail.isNull("altitude")) {
	    ret.put("z", detail.get("altitude"));
	} else {
	    // altitude of the sub-location having the sensor coordinates
	    JSONArray subLocations = station.optJSONArray(SUB_LOCATIONS);
	    for (int i = 0; subLocations != null && i < subLocations.length(); i++) {
		JSONObject sub = subLocations.getJSONObject(i);
		if (Math.abs(sub.optDouble("x", Double.NaN) - lon) < 1e-6 && Math.abs(sub.optDouble("y", Double.NaN) - lat) < 1e-6) {
		    ret.put("z", sub.opt("z"));
		}
	    }
	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_SICILIA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();
	return endpoint.contains("protezionecivilesicilia.it") || endpoint.contains("siciliadrpc-aegis.caedns.it");
    }

    @Override
    protected HISCentralSiciliaConnectorSetting initSetting() {

	return new HISCentralSiciliaConnectorSetting();
    }
}
