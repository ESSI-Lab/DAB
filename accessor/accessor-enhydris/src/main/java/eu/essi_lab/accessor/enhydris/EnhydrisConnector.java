package eu.essi_lab.accessor.enhydris;

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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one record per public time series of the stations of an Enhydris instance, by default OpenHi.net. Each time series group
 * (a variable at a station) can have more time series (e.g. the initial, the checked and the aggregated ones), each one harvested as
 * a separate record. The harvested stations can be limited to a list of stations and/or of owners.
 *
 * @author boldrini
 */
public class EnhydrisConnector extends HarvestedQueryConnector<EnhydrisConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "EnhydrisConnector";

    /**
     * Maximum number of stations harvested in a single page (about 3 requests per time series)
     */
    static final int STATIONS_PER_PAGE = 5;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<JSONObject> stations;
    private Map<Integer, JSONObject> variables;
    private Map<Integer, JSONObject> units;
    private Map<Integer, JSONObject> organizations;

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

	EnhydrisClient client = new EnhydrisClient(getSourceURL());

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    index = Integer.parseInt(resumptionToken);
	}

	ensureStationsLoaded(client);

	if (index >= stations.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + STATIONS_PER_PAGE, stations.size());

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving series of stations [{}-{}/{}]", index, nextIndex, stations.size());

	boolean stoppedByMax = false;

	stations: for (JSONObject station : stations.subList(index, nextIndex)) {

	    for (EnhydrisEntity entity : client.retrieveSeries(station, variables, units, organizations)) {

		if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		    stoppedByMax = true;
		    break stations;
		}

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(entity.toString());
		originalMetadata.setSchemeURI(EnhydrisMapper.ENHYDRIS_SCHEMA);
		response.addRecord(originalMetadata);
		partialNumbers++;
	    }
	}

	if (stoppedByMax || nextIndex >= stations.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	} else {
	    response.setResumptionToken(String.valueOf(nextIndex));
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * Loads the stations to harvest and the lookup tables of the variables, units and organizations
     *
     * @param client
     * @throws GSException
     */
    private void ensureStationsLoaded(EnhydrisClient client) throws GSException {

	if (stations != null) {
	    return;
	}

	variables = client.retrieveVariables();
	units = client.retrieveUnits();
	organizations = client.retrieveOrganizations();

	List<String> ids = getSetting().getStations();
	List<String> owners = getSetting().getOwners();

	stations = client.retrieveStations().stream().//
		filter(s -> ids.isEmpty() || ids.contains(String.valueOf(s.getInt(EnhydrisClient.ID)))).//
		filter(s -> owners.isEmpty() || owners.contains(String.valueOf(s.opt(EnhydrisClient.API_OWNER)))).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} Enhydris stations to harvest", stations.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(EnhydrisMapper.ENHYDRIS_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();

	return endpoint != null && (EnhydrisInstance.decode(endpoint).isPresent() || endpoint.toLowerCase().contains("enhydris"));
    }

    @Override
    protected EnhydrisConnectorSetting initSetting() {

	return new EnhydrisConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
