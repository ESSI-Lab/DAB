package eu.essi_lab.accessor.chexistenzbafu;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class CHExistenzBafuConnector extends HarvestedQueryConnector<CHExistenzBafuConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "CHExistenzBafuConnector";

    private int partialNumbers;

    private final List<CHExistenzBafuEntity> locations = new ArrayList<>();
    private final Map<String, CHExistenzBafuEntity> parameters = new HashMap<>();
    private boolean locationsLoaded = false;
    private boolean parametersLoaded = false;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("List records STARTED");

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();

	if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	CHExistenzBafuClient client = new CHExistenzBafuClient(getSourceURL());
	ensureParametersLoaded(client);

	int locationIndex = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    locationIndex = Integer.parseInt(resumptionToken);
	}

	ensureLocationsLoaded(client);

	if (locationIndex >= locations.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	CHExistenzBafuEntity location = locations.get(locationIndex);
	String locationId = CHExistenzBafuClient.getLocationId(location);

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving latest values for location {}", locationId);

	List<CHExistenzBafuEntity> latestValues = client.retrieveLatest(locationId);

	GSLoggerFactory.getLogger(getClass()).debug("Retrieved {} latest values", StringUtils.format(latestValues.size()));

	boolean stoppedByMax = false;

	for (CHExistenzBafuEntity reading : latestValues) {
	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    String parameterName = reading.getObject().optString(CHExistenzBafuClient.READING_PARAMETER, null);
	    if (parameterName == null || parameterName.isEmpty()) {
		continue;
	    }

	    CHExistenzBafuEntity parameter = parameters.get(parameterName);
	    if (parameter == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Unknown parameter {} at location {}", parameterName, locationId);
		continue;
	    }

	    long timestamp = reading.getObject().optLong(CHExistenzBafuClient.READING_TIMESTAMP, 0);
	    if (timestamp <= 0) {
		continue;
	    }

	    CHExistenzBafuEntity measure = CHExistenzBafuClient.createMeasure(location, parameter, timestamp);

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(measure.toString());
	    originalMetadata.setSchemeURI(CHExistenzBafuMapper.CH_EXISTENZ_BAFU_SCHEMA);
	    response.addRecord(originalMetadata);
	    partialNumbers++;
	}

	if (stoppedByMax) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    GSLoggerFactory.getLogger(getClass()).debug("List records ENDED (max records reached)");
	    return response;
	}

	if (locationIndex < (locations.size() - 1)) {
	    response.setResumptionToken(String.valueOf(locationIndex + 1));
	} else {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @param client
     */
    private void ensureLocationsLoaded(CHExistenzBafuClient client) {

	if (locationsLoaded) {
	    return;
	}

	String stationNamePrefix = getSetting().getStationNamePrefix();
	for (CHExistenzBafuEntity location : client.retrieveLocations()) {
	    String label = location.getObject().optJSONObject("details") != null
		    ? location.getObject().getJSONObject("details").optString("name", "")
		    : location.getObject().optString("name", "");
	    if (stationNamePrefix != null && !stationNamePrefix.isEmpty() && !label.startsWith(stationNamePrefix)) {
		continue;
	    }
	    locations.add(location);
	}

	locationsLoaded = true;
    }

    /**
     * @param client
     */
    private void ensureParametersLoaded(CHExistenzBafuClient client) {

	if (parametersLoaded) {
	    return;
	}

	parameters.putAll(client.retrieveParameters());
	parametersLoaded = true;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(CHExistenzBafuMapper.CH_EXISTENZ_BAFU_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("api.existenz.ch");
    }

    @Override
    protected CHExistenzBafuConnectorSetting initSetting() {

	return new CHExistenzBafuConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
