package eu.essi_lab.accessor.czchmi;

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
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.accessor.czchmi.CHMIClient.Inventory;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one record per CHMI surface water time series (station and series type). The station tables and the directory listings
 * are downloaded once, then for each station the file of the current day (end of the operational series) and the latest yearly
 * files of the datasets with a variable set of series are read. Groundwater data are not harvested, since the open data tree does
 * not publish the coordinates of the groundwater stations.
 *
 * @author boldrini
 */
public class CHMIConnector extends HarvestedQueryConnector<CHMIConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "CHMIConnector";

    /**
     * Maximum number of stations harvested in a single page
     */
    static final int STATIONS_PER_PAGE = 10;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private Inventory inventory;
    private List<String> stations;

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

	CHMIClient client = new CHMIClient(getSourceURL());

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    index = Integer.parseInt(resumptionToken);
	}

	List<CHMIParameter> parameters = getSetting().getParameters();

	ensureStationsLoaded(client, parameters);

	if (index >= stations.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + STATIONS_PER_PAGE, stations.size());

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving series of stations [{}-{}/{}]", index, nextIndex, stations.size());

	boolean stoppedByMax = false;

	for (String station : stations.subList(index, nextIndex)) {

	    for (CHMIEntity entity : client.retrieveSeries(inventory, station, parameters)) {

		if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		    stoppedByMax = true;
		    break;
		}

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(entity.toString());
		originalMetadata.setSchemeURI(CHMIMapper.CHMI_SCHEMA);
		response.addRecord(originalMetadata);
		partialNumbers++;
	    }

	    if (stoppedByMax) {
		break;
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
     * @param client
     * @param parameters
     * @throws GSException
     */
    private void ensureStationsLoaded(CHMIClient client, List<CHMIParameter> parameters) throws GSException {

	if (stations != null) {
	    return;
	}

	List<String> selected = getSetting().getStations();

	inventory = client.retrieveInventory(parameters);

	stations = new ArrayList<>();
	for (JSONObject station : inventory.getStations().values()) {
	    String id = station.getString(CHMIClient.OBJ_ID);
	    String number = station.optString(CHMIClient.DBC, "");
	    if (selected.isEmpty() || selected.contains(id) || selected.contains(number)) {
		stations.add(id);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Harvesting {} CHMI stations", stations.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(CHMIMapper.CHMI_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("opendata.chmi.cz");
    }

    @Override
    protected CHMIConnectorSetting initSetting() {

	return new CHMIConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
