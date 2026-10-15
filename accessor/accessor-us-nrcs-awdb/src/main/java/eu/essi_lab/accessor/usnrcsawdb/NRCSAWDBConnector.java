package eu.essi_lab.accessor.usnrcsawdb;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one metadata record for each station element (station, element, height/depth, sensor ordinal) of the
 * configured AWDB networks. The station elements are requested in batches of stations, since the API limits the
 * number of station elements returned by a single request.
 *
 * @author boldrini
 */
public class NRCSAWDBConnector extends HarvestedQueryConnector<NRCSAWDBConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "NRCSAWDBConnector";

    private static final int STATIONS_BATCH_SIZE = 25;

    private List<JSONObject> stations;
    private Map<String, JSONObject> elements;
    private Map<String, String> networkNames;
    private Map<String, String> stateCountries;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("List records STARTED");

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();

	NRCSAWDBClient client = new NRCSAWDBClient(getSourceURL());

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null && !resumptionToken.isEmpty()) {
	    index = Integer.parseInt(resumptionToken);
	}

	if (stations == null || index == 0) {
	    init(client);
	    partialNumbers = 0;
	}

	int end = Math.min(index + STATIONS_BATCH_SIZE, stations.size());

	List<String> triplets = new ArrayList<>();
	for (int i = index; i < end; i++) {
	    triplets.add(stations.get(i).getString(NRCSAWDBClient.STATION_TRIPLET));
	}

	List<String> durations = getSetting().getDurations();
	boolean maxReached = false;

	if (!triplets.isEmpty()) {

	    List<JSONObject> batch = client.retrieveStationElements(triplets, getSetting().getElements(), durations);

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieved station elements of stations [{}-{}] of {}", index, end,
		    StringUtils.format(stations.size()));

	    for (JSONObject station : batch) {

		for (JSONObject stationElement : selectFinestDurations(station, durations)) {

		    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
			maxReached = true;
			break;
		    }

		    OriginalMetadata originalMetadata = new OriginalMetadata();
		    originalMetadata.setMetadata(createRecord(station, stationElement).toString());
		    originalMetadata.setSchemeURI(NRCSAWDBMapper.AWDB_SCHEMA);
		    response.addRecord(originalMetadata);
		    partialNumbers++;
		}
	    }
	}

	if (end < stations.size() && !maxReached) {
	    response.setResumptionToken(String.valueOf(end));
	} else {
	    response.setResumptionToken(null);
	    stations = null;
	    partialNumbers = 0;
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @param client
     */
    private void init(NRCSAWDBClient client) {

	elements = client.retrieveElements();
	networkNames = client.retrieveNetworkNames();
	stateCountries = client.retrieveStateCountries();
	stations = new ArrayList<>();

	for (String network : getSetting().getNetworks()) {

	    List<JSONObject> networkStations = client.retrieveStations(network);

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieved {} {} stations", StringUtils.format(networkStations.size()), network);

	    stations.addAll(networkStations);
	}

	stations.sort(Comparator.comparing(s -> s.getString(NRCSAWDBClient.STATION_TRIPLET)));
    }

    /**
     * The AWDB publishes the same element at several durations (e.g. daily values and their semimonthly and monthly
     * aggregations): only the finest of the configured durations is kept, for each element, height/depth and ordinal
     *
     * @param station
     * @param durations
     * @return
     */
    static List<JSONObject> selectFinestDurations(JSONObject station, List<String> durations) {

	Map<String, JSONObject> ret = new LinkedHashMap<>();

	JSONArray stationElements = station.optJSONArray(NRCSAWDBClient.STATION_ELEMENTS);
	if (stationElements == null) {
	    return new ArrayList<>();
	}

	for (int i = 0; i < stationElements.length(); i++) {

	    JSONObject stationElement = stationElements.getJSONObject(i);

	    String duration = stationElement.optString(NRCSAWDBClient.ELEMENT_DURATION);
	    if (!durations.isEmpty() && !durations.contains(duration)) {
		continue;
	    }

	    String key = stationElement.optString(NRCSAWDBClient.ELEMENT_CODE) + ":" + stationElement.opt(NRCSAWDBClient.ELEMENT_HEIGHT_DEPTH)
		    + ":" + stationElement.optInt(NRCSAWDBClient.ELEMENT_ORDINAL, 1);

	    JSONObject existing = ret.get(key);
	    if (existing == null || rank(duration) < rank(existing.optString(NRCSAWDBClient.ELEMENT_DURATION))) {
		ret.put(key, stationElement);
	    }
	}

	return new ArrayList<>(ret.values());
    }

    /**
     * @param duration
     * @return
     */
    private static int rank(String duration) {

	int rank = NRCSAWDBClient.DURATIONS.indexOf(duration);
	return rank < 0 ? Integer.MAX_VALUE : rank;
    }

    /**
     * @param station
     * @param stationElement
     * @return
     */
    private JSONObject createRecord(JSONObject station, JSONObject stationElement) {

	JSONObject record = new JSONObject(station.toString());
	record.remove(NRCSAWDBClient.STATION_ELEMENTS);

	String triplet = station.getString(NRCSAWDBClient.STATION_TRIPLET);
	String networkCode = station.optString(NRCSAWDBClient.STATION_NETWORK_CODE);

	record.put(NRCSAWDBClient.RECORD_STATION_ELEMENT, stationElement);
	record.put(NRCSAWDBClient.RECORD_TIMESERIES_ID, NRCSAWDBTimeseriesId.of(triplet, stationElement).toString());
	record.put(NRCSAWDBClient.RECORD_NETWORK_NAME, networkNames.getOrDefault(networkCode, networkCode));

	String countryCode = stateCountries.get(station.optString(NRCSAWDBClient.STATION_STATE_CODE));
	if (countryCode != null) {
	    record.put(NRCSAWDBClient.RECORD_COUNTRY_CODE, countryCode);
	}

	JSONObject element = elements.get(stationElement.getString(NRCSAWDBClient.ELEMENT_CODE));
	if (element != null) {
	    record.put(NRCSAWDBClient.RECORD_ELEMENT, element);
	}

	return record;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(NRCSAWDBMapper.AWDB_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("wcc.sc.egov.usda.gov/awdbRestApi");
    }

    @Override
    protected NRCSAWDBConnectorSetting initSetting() {

	return new NRCSAWDBConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
